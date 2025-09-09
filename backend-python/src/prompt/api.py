from __future__ import annotations

from pathlib import Path

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy import delete, func, select
from sqlalchemy.orm import Session, selectinload

from src.auth.api import require_admin
from src.database import get_db
from src.image import StorageLocations

from ._internal.entities import (
    Prompt,
    PromptCategory,
    PromptSlotType,
    PromptSlotVariant,
    PromptSlotVariantMapping,
    PromptSubCategory,
)
from .schemas import (
    PromptCategoryCreate,
    PromptCategoryRead,
    PromptCategoryUpdate,
    PromptCreate,
    PromptRead,
    PromptSlotTypeCreate,
    PromptSlotTypeRead,
    PromptSlotTypeUpdate,
    PromptSlotVariantCreate,
    PromptSlotVariantRead,
    PromptSlotVariantUpdate,
    PromptSubCategoryCreate,
    PromptSubCategoryRead,
    PromptSubCategoryUpdate,
    PromptSummaryRead,
    PromptUpdate,
    PublicPromptCategoryRead,
    PublicPromptRead,
    PublicPromptSlotRead,
    PublicPromptSlotTypeRead,
    PublicPromptSubCategoryRead,
)

router = APIRouter()


# -----------------------------
# Utilities
# -----------------------------
def _ensure_exists(entity: object | None, label: str, id_value: int) -> None:
    if not entity:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f"{label} not found")


def _count_prompts_for_category(db: Session, category_id: int) -> int:
    return db.scalar(select(func.count()).select_from(Prompt).where(Prompt.category_id == category_id)) or 0


def _count_subcategories_for_category(db: Session, category_id: int) -> int:
    return (
        db.scalar(
            select(func.count())
            .select_from(PromptSubCategory)
            .where(PromptSubCategory.prompt_category_id == category_id)
        )
        or 0
    )


def _count_prompts_for_subcategory(db: Session, subcategory_id: int) -> int:
    return db.scalar(select(func.count()).select_from(Prompt).where(Prompt.subcategory_id == subcategory_id)) or 0


def _public_prompt_example_url(filename: str | None) -> str | None:
    if not filename:
        return None
    locations = StorageLocations()
    base = locations.PROMPT_EXAMPLE
    rel = base.relative_to(locations.root).as_posix()
    return f"/{rel}/{filename}"


def _public_slot_variant_example_url(filename: str | None) -> str | None:
    if not filename:
        return None
    locations = StorageLocations()
    base = locations.PROMPT_SLOT_VARIANT_EXAMPLE
    rel = base.relative_to(locations.root).as_posix()
    return f"/{rel}/{filename}"


def _safe_delete_image(filename: str | None, base_dir: Path) -> None:
    if not filename:
        return
    try:
        path = base_dir / filename
        if path.exists():
            path.unlink()
    except Exception:
        # Best-effort deletion, ignore errors
        pass


def _load_prompt_with_relations(db: Session, id_value: int) -> Prompt | None:
    stmt = (
        select(Prompt)
        .where(Prompt.id == id_value)
        .options(
            selectinload(Prompt.category),
            selectinload(Prompt.subcategory),
            selectinload(Prompt.prompt_slot_variant_mappings)
            .selectinload(PromptSlotVariantMapping.prompt_slot_variant)
            .selectinload(PromptSlotVariant.slot_type),
        )
    )
    return db.execute(stmt).scalars().first()


def _all_prompts_with_relations(db: Session) -> list[Prompt]:
    stmt = (
        select(Prompt)
        .options(
            selectinload(Prompt.category),
            selectinload(Prompt.subcategory),
            selectinload(Prompt.prompt_slot_variant_mappings)
            .selectinload(PromptSlotVariantMapping.prompt_slot_variant)
            .selectinload(PromptSlotVariant.slot_type),
        )
        .order_by(Prompt.id.desc())
    )
    return db.execute(stmt).scalars().all()


# -----------------------------
# Admin: Prompt slot types
# -----------------------------
admin_slot_types = APIRouter(
    prefix="/api/admin/prompts/prompt-slot-types",
    tags=["prompts"],
    dependencies=[Depends(require_admin)],
)


@admin_slot_types.get("/", response_model=list[PromptSlotTypeRead], response_model_by_alias=True)
@admin_slot_types.get("", response_model=list[PromptSlotTypeRead], response_model_by_alias=True)
def get_all_prompt_slot_types(db: Session = Depends(get_db)):
    result = db.execute(select(PromptSlotType))
    return result.scalars().all()


@admin_slot_types.get("/{id:int}", response_model=PromptSlotTypeRead, response_model_by_alias=True)
def get_prompt_slot_type(id: int, db: Session = Depends(get_db)):
    result = db.execute(select(PromptSlotType).where(PromptSlotType.id == id))
    entity = result.scalar_one_or_none()
    _ensure_exists(entity, "PromptSlotType", id)
    return entity


@admin_slot_types.post(
    "/", response_model=PromptSlotTypeRead, response_model_by_alias=True, status_code=status.HTTP_201_CREATED
)
def create_prompt_slot_type(payload: PromptSlotTypeCreate, db: Session = Depends(get_db)):
    # Unique checks
    if db.scalar(select(func.count()).select_from(PromptSlotType).where(PromptSlotType.name == payload.name)):
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="PromptSlotType name already exists")
    if db.scalar(select(func.count()).select_from(PromptSlotType).where(PromptSlotType.position == payload.position)):
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="PromptSlotType position already exists")

    entity = PromptSlotType(name=payload.name, position=payload.position)
    db.add(entity)
    db.commit()
    db.refresh(entity)
    return entity


@admin_slot_types.put("/{id:int}", response_model=PromptSlotTypeRead, response_model_by_alias=True)
def update_prompt_slot_type(id: int, payload: PromptSlotTypeUpdate, db: Session = Depends(get_db)):
    entity = db.execute(select(PromptSlotType).where(PromptSlotType.id == id)).scalar_one_or_none()
    _ensure_exists(entity, "PromptSlotType", id)

    if payload.name is not None and payload.name != entity.name:
        exists = db.scalar(
            select(func.count())
            .select_from(PromptSlotType)
            .where((PromptSlotType.name == payload.name) & (PromptSlotType.id != id))
        )
        if exists:
            raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="PromptSlotType name already exists")
        entity.name = payload.name

    if payload.position is not None and payload.position != entity.position:
        exists = db.scalar(
            select(func.count())
            .select_from(PromptSlotType)
            .where((PromptSlotType.position == payload.position) & (PromptSlotType.id != id))
        )
        if exists:
            raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="PromptSlotType position already exists")
        entity.position = payload.position

    db.add(entity)
    db.commit()
    db.refresh(entity)
    return entity


@admin_slot_types.delete("/{id:int}", status_code=status.HTTP_204_NO_CONTENT)
def delete_prompt_slot_type(id: int, db: Session = Depends(get_db)):
    entity = db.execute(select(PromptSlotType).where(PromptSlotType.id == id)).scalar_one_or_none()
    _ensure_exists(entity, "PromptSlotType", id)
    db.delete(entity)
    db.commit()


# -----------------------------
# Admin: Prompt slot variants
# -----------------------------
admin_slot_variants = APIRouter(
    prefix="/api/admin/prompts/slot-variants",
    tags=["prompts"],
    dependencies=[Depends(require_admin)],
)


@admin_slot_variants.get("/", response_model=list[PromptSlotVariantRead], response_model_by_alias=True)
@admin_slot_variants.get("", response_model=list[PromptSlotVariantRead], response_model_by_alias=True)
def get_all_slot_variants(db: Session = Depends(get_db)):
    result = db.execute(
        select(PromptSlotVariant)
        .options(selectinload(PromptSlotVariant.slot_type))
        .order_by(PromptSlotVariant.id.desc())
    )
    items = result.scalars().all()
    return [PromptSlotVariantRead.from_entity(it) for it in items]


@admin_slot_variants.get("/{id:int}", response_model=PromptSlotVariantRead, response_model_by_alias=True)
def get_slot_variant(id: int, db: Session = Depends(get_db)):
    entity = (
        db.execute(
            select(PromptSlotVariant)
            .where(PromptSlotVariant.id == id)
            .options(selectinload(PromptSlotVariant.slot_type))
        )
        .scalars()
        .first()
    )
    _ensure_exists(entity, "PromptSlotVariant", id)
    return PromptSlotVariantRead.from_entity(entity)


@admin_slot_variants.post(
    "/", response_model=PromptSlotVariantRead, response_model_by_alias=True, status_code=status.HTTP_201_CREATED
)
def create_slot_variant(payload: PromptSlotVariantCreate, db: Session = Depends(get_db)):
    # Validate slot type exists
    if not db.scalar(
        select(func.count()).select_from(PromptSlotType).where(PromptSlotType.id == payload.prompt_slot_type_id)
    ):
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="PromptSlotType not found")

    # Unique name
    if db.scalar(select(func.count()).select_from(PromptSlotVariant).where(PromptSlotVariant.name == payload.name)):
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="PromptSlotVariant name already exists")

    entity = PromptSlotVariant(
        slot_type_id=payload.prompt_slot_type_id,
        name=payload.name,
        prompt=payload.prompt,
        description=payload.description,
        example_image_filename=payload.example_image_filename,
    )
    db.add(entity)
    db.commit()
    db.refresh(entity)
    return get_slot_variant(entity.id or 0, db)


@admin_slot_variants.put("/{id:int}", response_model=PromptSlotVariantRead, response_model_by_alias=True)
def update_slot_variant(id: int, payload: PromptSlotVariantUpdate, db: Session = Depends(get_db)):
    entity = db.execute(select(PromptSlotVariant).where(PromptSlotVariant.id == id)).scalar_one_or_none()
    _ensure_exists(entity, "PromptSlotVariant", id)

    # Validate/assign slot type
    if payload.prompt_slot_type_id is not None and payload.prompt_slot_type_id != entity.slot_type_id:
        if not db.scalar(
            select(func.count()).select_from(PromptSlotType).where(PromptSlotType.id == payload.prompt_slot_type_id)
        ):
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="PromptSlotType not found")
        entity.slot_type_id = payload.prompt_slot_type_id

    # Unique name if changed
    if payload.name is not None and payload.name != entity.name:
        exists = db.scalar(
            select(func.count())
            .select_from(PromptSlotVariant)
            .where((PromptSlotVariant.name == payload.name) & (PromptSlotVariant.id != id))
        )
        if exists:
            raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="PromptSlotVariant name already exists")
        entity.name = payload.name

    if payload.prompt is not None:
        entity.prompt = payload.prompt
    if payload.description is not None:
        entity.description = payload.description

    if payload.example_image_filename is not None:
        old = entity.example_image_filename
        new = payload.example_image_filename
        if old and old != new:
            _safe_delete_image(old, StorageLocations().PROMPT_SLOT_VARIANT_EXAMPLE)
        entity.example_image_filename = new

    db.add(entity)
    db.commit()
    return get_slot_variant(id, db)


@admin_slot_variants.delete("/{id:int}", status_code=status.HTTP_204_NO_CONTENT)
def delete_slot_variant(id: int, db: Session = Depends(get_db)):
    entity = db.execute(select(PromptSlotVariant).where(PromptSlotVariant.id == id)).scalar_one_or_none()
    _ensure_exists(entity, "PromptSlotVariant", id)

    # Delete image if present
    if entity.example_image_filename:
        _safe_delete_image(entity.example_image_filename, StorageLocations().PROMPT_SLOT_VARIANT_EXAMPLE)

    db.delete(entity)
    db.commit()


# -----------------------------
# Admin: Prompt categories
# -----------------------------
admin_categories = APIRouter(
    prefix="/api/admin/prompts/categories",
    tags=["prompts"],
    dependencies=[Depends(require_admin)],
)


@admin_categories.get("/", response_model=list[PromptCategoryRead], response_model_by_alias=True)
@admin_categories.get("", response_model=list[PromptCategoryRead], response_model_by_alias=True)
def get_all_categories(db: Session = Depends(get_db)):
    categories = db.execute(select(PromptCategory)).scalars().all()
    result: list[PromptCategoryRead] = []
    for c in categories:
        prompts_count = _count_prompts_for_category(db, c.id or 0)
        subcats_count = _count_subcategories_for_category(db, c.id or 0)
        result.append(
            PromptCategoryRead(
                id=c.id or 0,
                name=c.name,
                prompts_count=prompts_count,
                subcategories_count=subcats_count,
                created_at=c.created_at,
                updated_at=c.updated_at,
            )
        )
    return result


@admin_categories.post(
    "/", response_model=PromptCategoryRead, response_model_by_alias=True, status_code=status.HTTP_201_CREATED
)
def create_category(payload: PromptCategoryCreate, db: Session = Depends(get_db)):
    entity = PromptCategory(name=payload.name)
    db.add(entity)
    db.commit()
    db.refresh(entity)
    return PromptCategoryRead(
        id=entity.id or 0,
        name=entity.name,
        created_at=entity.created_at,
        updated_at=entity.updated_at,
    )


@admin_categories.put("/{id:int}", response_model=PromptCategoryRead, response_model_by_alias=True)
def update_category(id: int, payload: PromptCategoryUpdate, db: Session = Depends(get_db)):
    entity = db.execute(select(PromptCategory).where(PromptCategory.id == id)).scalar_one_or_none()
    _ensure_exists(entity, "PromptCategory", id)
    if payload.name is not None:
        entity.name = payload.name
    db.add(entity)
    db.commit()
    prompts_count = _count_prompts_for_category(db, entity.id or 0)
    subcats_count = _count_subcategories_for_category(db, entity.id or 0)
    return PromptCategoryRead(
        id=entity.id or 0,
        name=entity.name,
        prompts_count=prompts_count,
        subcategories_count=subcats_count,
        created_at=entity.created_at,
        updated_at=entity.updated_at,
    )


@admin_categories.delete("/{id:int}", status_code=status.HTTP_204_NO_CONTENT)
def delete_category(id: int, db: Session = Depends(get_db)):
    entity = db.execute(select(PromptCategory).where(PromptCategory.id == id)).scalar_one_or_none()
    _ensure_exists(entity, "PromptCategory", id)
    db.delete(entity)
    db.commit()


# -----------------------------
# Admin: Prompt subcategories
# -----------------------------
admin_subcategories = APIRouter(
    prefix="/api/admin/prompts/subcategories",
    tags=["prompts"],
    dependencies=[Depends(require_admin)],
)


@admin_subcategories.get("/", response_model=list[PromptSubCategoryRead], response_model_by_alias=True)
@admin_subcategories.get("", response_model=list[PromptSubCategoryRead], response_model_by_alias=True)
def get_all_subcategories(db: Session = Depends(get_db)):
    items = db.execute(select(PromptSubCategory)).scalars().all()
    out: list[PromptSubCategoryRead] = []
    for it in items:
        out.append(
            PromptSubCategoryRead(
                id=it.id or 0,
                prompt_category_id=it.prompt_category_id,
                name=it.name,
                description=it.description,
                prompts_count=_count_prompts_for_subcategory(db, it.id or 0),
                created_at=it.created_at,
                updated_at=it.updated_at,
            )
        )
    return out


@admin_subcategories.get(
    "/category/{category_id:int}", response_model=list[PromptSubCategoryRead], response_model_by_alias=True
)
def get_subcategories_by_category(category_id: int, db: Session = Depends(get_db)):
    items = (
        db.execute(select(PromptSubCategory).where(PromptSubCategory.prompt_category_id == category_id)).scalars().all()
    )
    out: list[PromptSubCategoryRead] = []
    for it in items:
        out.append(
            PromptSubCategoryRead(
                id=it.id or 0,
                prompt_category_id=it.prompt_category_id,
                name=it.name,
                description=it.description,
                prompts_count=_count_prompts_for_subcategory(db, it.id or 0),
                created_at=it.created_at,
                updated_at=it.updated_at,
            )
        )
    return out


@admin_subcategories.post(
    "/", response_model=PromptSubCategoryRead, response_model_by_alias=True, status_code=status.HTTP_201_CREATED
)
def create_subcategory(payload: PromptSubCategoryCreate, db: Session = Depends(get_db)):
    # Ensure category exists
    cat = db.execute(select(PromptCategory).where(PromptCategory.id == payload.prompt_category_id)).scalar_one_or_none()
    _ensure_exists(cat, "PromptCategory", payload.prompt_category_id)

    entity = PromptSubCategory(
        prompt_category_id=payload.prompt_category_id,
        name=payload.name,
        description=payload.description,
    )
    db.add(entity)
    db.commit()
    db.refresh(entity)
    return PromptSubCategoryRead(
        id=entity.id or 0,
        prompt_category_id=entity.prompt_category_id,
        name=entity.name,
        description=entity.description,
        prompts_count=0,
        created_at=entity.created_at,
        updated_at=entity.updated_at,
    )


@admin_subcategories.put("/{id:int}", response_model=PromptSubCategoryRead, response_model_by_alias=True)
def update_subcategory(id: int, payload: PromptSubCategoryUpdate, db: Session = Depends(get_db)):
    entity = db.execute(select(PromptSubCategory).where(PromptSubCategory.id == id)).scalar_one_or_none()
    _ensure_exists(entity, "PromptSubCategory", id)

    if payload.prompt_category_id is not None and payload.prompt_category_id != entity.prompt_category_id:
        cat = db.execute(
            select(PromptCategory).where(PromptCategory.id == payload.prompt_category_id)
        ).scalar_one_or_none()
        _ensure_exists(cat, "PromptCategory", payload.prompt_category_id)
        entity.prompt_category_id = payload.prompt_category_id

    if payload.name is not None:
        entity.name = payload.name
    if payload.description is not None:
        entity.description = payload.description

    db.add(entity)
    db.commit()
    return PromptSubCategoryRead(
        id=entity.id or 0,
        prompt_category_id=entity.prompt_category_id,
        name=entity.name,
        description=entity.description,
        prompts_count=_count_prompts_for_subcategory(db, entity.id or 0),
        created_at=entity.created_at,
        updated_at=entity.updated_at,
    )


@admin_subcategories.delete("/{id:int}", status_code=status.HTTP_204_NO_CONTENT)
def delete_subcategory(id: int, db: Session = Depends(get_db)):
    entity = db.execute(select(PromptSubCategory).where(PromptSubCategory.id == id)).scalar_one_or_none()
    _ensure_exists(entity, "PromptSubCategory", id)
    db.delete(entity)
    db.commit()


# -----------------------------
# Admin: Prompts
# -----------------------------
admin_prompts = APIRouter(
    prefix="/api/admin/prompts",
    tags=["prompts"],
    dependencies=[Depends(require_admin)],
)


@admin_prompts.get("/", response_model=list[PromptRead], response_model_by_alias=True)
def get_all_prompts(db: Session = Depends(get_db)):
    entities = _all_prompts_with_relations(db)
    out: list[PromptRead] = []
    for e in entities:
        cat_counts = (0, 0)
        sub_count = 0
        if e.category_id:
            cat_counts = (
                _count_prompts_for_category(db, e.category_id),
                _count_subcategories_for_category(db, e.category_id),
            )
        if e.subcategory_id:
            sub_count = _count_prompts_for_subcategory(db, e.subcategory_id)
        out.append(PromptRead.from_entity(e, category_counts=cat_counts, subcategory_prompts_count=sub_count))
    return out


@admin_prompts.get("/{id:int}", response_model=PromptRead, response_model_by_alias=True)
def get_prompt(id: int, db: Session = Depends(get_db)):
    e = _load_prompt_with_relations(db, id)
    _ensure_exists(e, "Prompt", id)
    cat_counts = (0, 0)
    sub_count = 0
    if e.category_id:
        cat_counts = (
            _count_prompts_for_category(db, e.category_id),
            _count_subcategories_for_category(db, e.category_id),
        )
    if e.subcategory_id:
        sub_count = _count_prompts_for_subcategory(db, e.subcategory_id)
    return PromptRead.from_entity(e, category_counts=cat_counts, subcategory_prompts_count=sub_count)


@admin_prompts.post("/", response_model=PromptRead, response_model_by_alias=True, status_code=status.HTTP_201_CREATED)
def create_prompt(payload: PromptCreate, db: Session = Depends(get_db)):
    # Validate category/subcategory when provided
    if payload.category_id is not None:
        cat = db.execute(select(PromptCategory).where(PromptCategory.id == payload.category_id)).scalar_one_or_none()
        _ensure_exists(cat, "PromptCategory", payload.category_id)
    if payload.subcategory_id is not None:
        sub = db.execute(
            select(PromptSubCategory).where(PromptSubCategory.id == payload.subcategory_id)
        ).scalar_one_or_none()
        _ensure_exists(sub, "PromptSubCategory", payload.subcategory_id)
        # If both provided, ensure subcategory belongs to category
        if payload.category_id is not None and sub.prompt_category_id != payload.category_id:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Subcategory does not belong to the specified category",
            )

    # Validate slot IDs exist
    # Deduplicate while preserving order
    slot_ids: list[int] = []
    seen: set[int] = set()
    for s in payload.slots or []:
        if s.slot_id not in seen:
            seen.add(s.slot_id)
            slot_ids.append(s.slot_id)
    if slot_ids:
        existing_ids = set(
            db.execute(select(PromptSlotVariant.id).where(PromptSlotVariant.id.in_(slot_ids))).scalars().all()
        )
        missing = set(slot_ids) - existing_ids
        if missing:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"PromptSlotVariant ids not found: {sorted(missing)}",
            )

    entity = Prompt(
        title=payload.title,
        prompt_text=payload.prompt_text,
        category_id=payload.category_id,
        subcategory_id=payload.subcategory_id,
        example_image_filename=payload.example_image_filename,
    )
    db.add(entity)
    db.flush()  # obtain entity.id for mappings

    # Create mappings
    for sid in slot_ids:
        db.add(PromptSlotVariantMapping(prompt_id=entity.id, slot_id=sid))

    db.commit()
    return get_prompt(entity.id or 0, db)


@admin_prompts.put("/{id:int}", response_model=PromptRead, response_model_by_alias=True)
def update_prompt(id: int, payload: PromptUpdate, db: Session = Depends(get_db)):
    entity = db.execute(select(Prompt).where(Prompt.id == id)).scalar_one_or_none()
    _ensure_exists(entity, "Prompt", id)

    # Validate category/subcategory
    if payload.category_id is not None:
        cat = db.execute(select(PromptCategory).where(PromptCategory.id == payload.category_id)).scalar_one_or_none()
        _ensure_exists(cat, "PromptCategory", payload.category_id)
    if payload.subcategory_id is not None:
        sub = db.execute(
            select(PromptSubCategory).where(PromptSubCategory.id == payload.subcategory_id)
        ).scalar_one_or_none()
        _ensure_exists(sub, "PromptSubCategory", payload.subcategory_id)
        if payload.category_id is not None and sub.prompt_category_id != payload.category_id:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Subcategory does not belong to the specified category",
            )

    if payload.title is not None:
        entity.title = payload.title
    if payload.prompt_text is not None:
        entity.prompt_text = payload.prompt_text
    if payload.category_id is not None:
        entity.category_id = payload.category_id
    if payload.subcategory_id is not None:
        entity.subcategory_id = payload.subcategory_id
    if payload.active is not None:
        entity.active = payload.active
    if payload.example_image_filename is not None:
        old = entity.example_image_filename
        new = payload.example_image_filename
        if old and old != new:
            _safe_delete_image(old, StorageLocations().PROMPT_EXAMPLE)
        entity.example_image_filename = new

    # Update mappings when provided
    if payload.slots is not None:
        new_ids: list[int] = []
        seen: set[int] = set()
        for s in payload.slots:
            if s.slot_id not in seen:
                seen.add(s.slot_id)
                new_ids.append(s.slot_id)
        if new_ids:
            existing_ids = set(
                db.execute(select(PromptSlotVariant.id).where(PromptSlotVariant.id.in_(new_ids))).scalars().all()
            )
            missing = set(new_ids) - existing_ids
            if missing:
                raise HTTPException(
                    status_code=status.HTTP_404_NOT_FOUND,
                    detail=f"PromptSlotVariant ids not found: {sorted(missing)}",
                )

        # Clear existing mappings and add new ones
        # Delete existing mappings then add new ones
        db.execute(delete(PromptSlotVariantMapping).where(PromptSlotVariantMapping.prompt_id == id))
        for sid in new_ids:
            db.add(PromptSlotVariantMapping(prompt_id=id, slot_id=sid))

    db.add(entity)
    db.commit()
    return get_prompt(id, db)


@admin_prompts.delete("/{id:int}", status_code=status.HTTP_204_NO_CONTENT)
def delete_prompt(id: int, db: Session = Depends(get_db)):
    entity = db.execute(select(Prompt).where(Prompt.id == id)).scalar_one_or_none()
    _ensure_exists(entity, "Prompt", id)
    if entity.example_image_filename:
        _safe_delete_image(entity.example_image_filename, StorageLocations().PROMPT_EXAMPLE)
    # Delete mappings first (due to FK constraints in some DBs)
    db.execute(delete(PromptSlotVariantMapping).where(PromptSlotVariantMapping.prompt_id == id))
    db.delete(entity)
    db.commit()


# -----------------------------
# Public APIs
# -----------------------------
public_router = APIRouter(prefix="/api/prompts", tags=["prompts"])


@public_router.get("/", response_model=list[PublicPromptRead])
def get_all_public_prompts(db: Session = Depends(get_db)):
    stmt = (
        select(Prompt)
        .where(Prompt.active.is_(True))
        .options(
            selectinload(Prompt.category),
            selectinload(Prompt.subcategory),
            selectinload(Prompt.prompt_slot_variant_mappings)
            .selectinload(PromptSlotVariantMapping.prompt_slot_variant)
            .selectinload(PromptSlotVariant.slot_type),
        )
        .order_by(Prompt.id.desc())
    )
    prompts = db.execute(stmt).scalars().all()

    results: list[PublicPromptRead] = []
    for p in prompts:
        category = (
            PublicPromptCategoryRead(id=p.category.id or 0, name=p.category.name) if p.category is not None else None
        )
        subcategory = (
            PublicPromptSubCategoryRead(
                id=p.subcategory.id or 0, name=p.subcategory.name, description=p.subcategory.description
            )
            if p.subcategory is not None
            else None
        )
        slots: list[PublicPromptSlotRead] = []
        for m in p.prompt_slot_variant_mappings:
            v = m.prompt_slot_variant
            slot_type = (
                PublicPromptSlotTypeRead(id=v.slot_type.id or 0, name=v.slot_type.name, position=v.slot_type.position)
                if v.slot_type is not None
                else None
            )
            slots.append(
                PublicPromptSlotRead(
                    id=v.id or 0,
                    name=v.name,
                    description=v.description,
                    example_image_url=_public_slot_variant_example_url(v.example_image_filename),
                    slot_type=slot_type,
                )
            )
        results.append(
            PublicPromptRead(
                id=p.id or 0,
                title=p.title,
                example_image_url=_public_prompt_example_url(p.example_image_filename),
                category=category,
                subcategory=subcategory,
                slots=slots,
            )
        )
    return results


@public_router.get("/batch", response_model=list[PromptSummaryRead])
def get_prompt_summaries_by_ids(ids: list[int] = Query(default=[]), db: Session = Depends(get_db)):
    if not ids:
        return []
    items = db.execute(select(Prompt).where(Prompt.id.in_(ids))).scalars().all()
    return [PromptSummaryRead(id=p.id or 0, title=p.title) for p in items]


# Mount subrouters under the exported router
router.include_router(admin_slot_types)
router.include_router(admin_slot_variants)
router.include_router(admin_categories)
router.include_router(admin_subcategories)
router.include_router(admin_prompts)
router.include_router(public_router)
