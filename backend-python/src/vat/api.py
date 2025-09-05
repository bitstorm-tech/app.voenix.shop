from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select, update
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session
from sqlmodel import SQLModel

from src.database import get_db

from ._internal.entities import ValueAddedTax

router = APIRouter(prefix="/api/vat", tags=["vat"])


@router.get("/", response_model=list[ValueAddedTax])
def get_vats(db: Session = Depends(get_db)):
    """Return all VAT rows with all columns."""
    result = db.execute(select(ValueAddedTax))
    return result.scalars().all()


class CreateValueAddedTax(SQLModel):
    name: str
    percent: int
    description: str | None = None
    is_default: bool = False


@router.post("/", response_model=ValueAddedTax, status_code=status.HTTP_201_CREATED)
def create_vat(payload: CreateValueAddedTax, db: Session = Depends(get_db)):
    """Create a new VAT entry.

    - Enforces unique `name` (409 if already exists).
    - If `is_default` is true, clears default flag on existing rows.
    """
    try:
        if payload.is_default:
            db.execute(update(ValueAddedTax).values(is_default=False))

        vat = ValueAddedTax(**payload.model_dump())
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
