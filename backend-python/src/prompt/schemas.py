from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field
from pydantic.alias_generators import to_camel

from ._internal.entities import (
    Prompt,
    PromptSlotVariant,
)


# -----------------------------
# Helpers / Public URL builders
# -----------------------------
def _public_prompt_example_url(filename: str | None) -> str | None:
    if not filename:
        return None
    return f"/public/images/prompt-example-images/{filename}"


def _public_slot_variant_example_url(filename: str | None) -> str | None:
    if not filename:
        return None
    return f"/public/images/prompt-slot-variant-example-images/{filename}"


# -----------------------------
# Slot types
# -----------------------------
class PromptSlotTypeCreate(BaseModel):
    name: str = Field(..., max_length=255)
    position: int


class PromptSlotTypeUpdate(BaseModel):
    name: str | None = Field(default=None, max_length=255)
    position: int | None = None


class PromptSlotTypeRead(BaseModel):
    model_config = ConfigDict(from_attributes=True, alias_generator=to_camel, populate_by_name=True)

    id: int
    name: str
    position: int
    created_at: datetime | None = None
    updated_at: datetime | None = None


# -----------------------------
# Slot variants
# -----------------------------
class PromptSlotVariantCreate(BaseModel):
    prompt_slot_type_id: int = Field(..., ge=1)
    name: str = Field(..., max_length=255)
    prompt: str | None = None
    description: str | None = None
    example_image_filename: str | None = Field(default=None, max_length=500)


class PromptSlotVariantUpdate(BaseModel):
    prompt_slot_type_id: int | None = Field(default=None, ge=1)
    name: str | None = Field(default=None, max_length=255)
    prompt: str | None = None
    description: str | None = None
    example_image_filename: str | None = Field(default=None, max_length=500)


class PromptSlotVariantRead(BaseModel):
    model_config = ConfigDict(from_attributes=True, alias_generator=to_camel, populate_by_name=True)

    id: int
    prompt_slot_type_id: int
    prompt_slot_type: PromptSlotTypeRead | None = None
    name: str
    prompt: str | None = None
    description: str | None = None
    example_image_url: str | None = None
    created_at: datetime | None = None
    updated_at: datetime | None = None

    @classmethod
    def from_entity(cls, entity: PromptSlotVariant) -> PromptSlotVariantRead:
        return cls(
            id=entity.id or 0,
            prompt_slot_type_id=entity.slot_type_id,
            prompt_slot_type=PromptSlotTypeRead.model_validate(entity.slot_type) if entity.slot_type else None,
            name=entity.name,
            prompt=entity.prompt,
            description=entity.description,
            example_image_url=_public_slot_variant_example_url(entity.example_image_filename),
            created_at=entity.created_at,
            updated_at=entity.updated_at,
        )


# -----------------------------
# Categories
# -----------------------------
class PromptCategoryCreate(BaseModel):
    name: str = Field(..., max_length=255)


class PromptCategoryUpdate(BaseModel):
    name: str | None = Field(default=None, max_length=255)


class PromptCategoryRead(BaseModel):
    model_config = ConfigDict(from_attributes=True, alias_generator=to_camel, populate_by_name=True)

    id: int
    name: str
    prompts_count: int = 0
    subcategories_count: int = 0
    created_at: datetime | None = None
    updated_at: datetime | None = None


# -----------------------------
# Subcategories
# -----------------------------
class PromptSubCategoryCreate(BaseModel):
    prompt_category_id: int
    name: str = Field(..., max_length=255)
    description: str | None = Field(default=None, max_length=1000)


class PromptSubCategoryUpdate(BaseModel):
    prompt_category_id: int | None = None
    name: str | None = Field(default=None, max_length=255)
    description: str | None = Field(default=None, max_length=1000)


class PromptSubCategoryRead(BaseModel):
    model_config = ConfigDict(from_attributes=True, alias_generator=to_camel, populate_by_name=True)

    id: int
    prompt_category_id: int
    name: str
    description: str | None = None
    prompts_count: int = 0
    created_at: datetime | None = None
    updated_at: datetime | None = None


# -----------------------------
# Prompt (create/update/read)
# -----------------------------
class PromptSlotVariantRef(BaseModel):
    slot_id: int = Field(..., ge=1)


class PromptCreate(BaseModel):
    title: str = Field(..., max_length=500)
    prompt_text: str | None = None
    category_id: int | None = None
    subcategory_id: int | None = None
    example_image_filename: str | None = Field(default=None, max_length=500)
    slots: list[PromptSlotVariantRef] = []


class PromptUpdate(BaseModel):
    title: str | None = Field(default=None, max_length=500)
    prompt_text: str | None = None
    category_id: int | None = None
    subcategory_id: int | None = None
    active: bool | None = None
    example_image_filename: str | None = Field(default=None, max_length=500)
    slots: list[PromptSlotVariantRef] | None = None


class PromptRead(BaseModel):
    model_config = ConfigDict(from_attributes=True, alias_generator=to_camel, populate_by_name=True)

    id: int
    title: str
    prompt_text: str | None = None
    category_id: int | None = None
    category: PromptCategoryRead | None = None
    subcategory_id: int | None = None
    subcategory: PromptSubCategoryRead | None = None
    active: bool
    slots: list[PromptSlotVariantRead] = []
    example_image_url: str | None = None
    created_at: datetime | None = None
    updated_at: datetime | None = None

    @classmethod
    def from_entity(
        cls,
        entity: Prompt,
        *,
        category_counts: tuple[int, int] | None = None,
        subcategory_prompts_count: int | None = None,
    ) -> PromptRead:
        cat_dto: PromptCategoryRead | None = None
        if entity.category is not None:
            prompts_count, subcats_count = category_counts or (0, 0)
            cat_dto = PromptCategoryRead(
                id=entity.category.id or 0,
                name=entity.category.name,
                prompts_count=prompts_count,
                subcategories_count=subcats_count,
                created_at=entity.category.created_at,
                updated_at=entity.category.updated_at,
            )

        subcat_dto: PromptSubCategoryRead | None = None
        if entity.subcategory is not None:
            subcat_dto = PromptSubCategoryRead(
                id=entity.subcategory.id or 0,
                prompt_category_id=entity.subcategory.prompt_category_id,
                name=entity.subcategory.name,
                description=entity.subcategory.description,
                prompts_count=subcategory_prompts_count or 0,
                created_at=entity.subcategory.created_at,
                updated_at=entity.subcategory.updated_at,
            )

        # Gracefully handle missing relationship collections (can be None on partially-loaded entities)
        mappings = entity.prompt_slot_variant_mappings or []
        slots = [
            PromptSlotVariantRead.from_entity(m.prompt_slot_variant)
            for m in mappings
            if m is not None and m.prompt_slot_variant is not None
        ]

        return cls(
            id=entity.id or 0,
            title=entity.title,
            prompt_text=entity.prompt_text,
            category_id=entity.category_id,
            category=cat_dto,
            subcategory_id=entity.subcategory_id,
            subcategory=subcat_dto,
            active=entity.active,
            slots=slots,
            example_image_url=_public_prompt_example_url(entity.example_image_filename),
            created_at=entity.created_at,
            updated_at=entity.updated_at,
        )


# -----------------------------
# Public DTOs and summaries
# -----------------------------
class PublicPromptCategoryRead(BaseModel):
    id: int
    name: str


class PublicPromptSubCategoryRead(BaseModel):
    id: int
    name: str
    description: str | None = None


class PublicPromptSlotTypeRead(BaseModel):
    id: int
    name: str
    position: int


class PublicPromptSlotRead(BaseModel):
    id: int
    name: str
    description: str | None = None
    example_image_url: str | None = None
    slot_type: PublicPromptSlotTypeRead | None = None


class PublicPromptRead(BaseModel):
    id: int
    title: str
    example_image_url: str | None = None
    category: PublicPromptCategoryRead | None = None
    subcategory: PublicPromptSubCategoryRead | None = None
    slots: list[PublicPromptSlotRead] = []


class PromptSummaryRead(BaseModel):
    id: int
    title: str
