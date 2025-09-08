from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select, update
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from src.auth.api import require_admin
from src.database import get_db

from ._internal.entities import ValueAddedTax
from .schemas import ValueAddedTaxCreate, ValueAddedTaxRead, ValueAddedTaxUpdate

router = APIRouter(
    prefix="/api/admin/vat",
    tags=["vat"],
    dependencies=[Depends(require_admin)],
)


@router.get("/", response_model=list[ValueAddedTaxRead])
def get_vats(db: Session = Depends(get_db)):
    """Return all VAT rows with all columns."""
    result = db.execute(select(ValueAddedTax))
    return result.scalars().all()


@router.get("/{id}", response_model=ValueAddedTaxRead)
def get_vat(id: int, db: Session = Depends(get_db)):
    """Return VAT row by ID. 404 if not found."""
    result = db.execute(select(ValueAddedTax).where(ValueAddedTax.id == id))
    vat = result.scalar_one_or_none()
    if not vat:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="VAT not found")
    return vat


@router.post("/", response_model=ValueAddedTaxRead, status_code=status.HTTP_201_CREATED)
def create_vat(payload: ValueAddedTaxCreate, db: Session = Depends(get_db)):
    """Create a new VAT entry.

    - Enforces unique `name` (409 if already exists).
    - If `is_default` is true, clears default flag on existing rows.
    """
    try:
        if payload.is_default:
            db.execute(update(ValueAddedTax).values(is_default=False))

        vat = ValueAddedTax(**payload.model_dump(mode="json"))
        db.add(vat)
        db.commit()
        db.refresh(vat)
        return vat
    except IntegrityError as e:
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="A VAT with this name already exists.",
        ) from e


@router.put("/{id}", response_model=ValueAddedTaxRead)
def update_vat(id: int, payload: ValueAddedTaxUpdate, db: Session = Depends(get_db)):
    """Update an existing VAT entry by ID.

    - Returns 404 if the VAT does not exist.
    - Enforces unique `name` (409 if already exists).
    - If `is_default` is true, clears default flag on existing rows before setting this one.
    """
    # Fetch existing VAT
    result = db.execute(select(ValueAddedTax).where(ValueAddedTax.id == id))
    vat = result.scalar_one_or_none()
    if not vat:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="VAT not found")

    try:
        if payload.is_default:
            # Ensure only one default at any time
            db.execute(update(ValueAddedTax).values(is_default=False))

        # Apply updates
        data = payload.model_dump(mode="json")
        vat.name = data.get("name")
        vat.percent = data.get("percent")
        vat.description = data.get("description")
        vat.is_default = data.get("is_default", False)

        db.add(vat)
        db.commit()
        db.refresh(vat)
        return vat
    except IntegrityError as e:
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="A VAT with this name already exists.",
        ) from e
