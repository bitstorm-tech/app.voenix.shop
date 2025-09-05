from __future__ import annotations

from fastapi import APIRouter, Depends
from sqlalchemy import select
from sqlalchemy.orm import Session

from src.database import get_db

from ._internal.entity import ValueAddedTax

router = APIRouter(prefix="/api/vat", tags=["vat"])


@router.get("/", response_model=list[ValueAddedTax])
def get_vats(db: Session = Depends(get_db)):  # noqa: B008
    """Return all VAT rows with all columns."""
    result = db.execute(select(ValueAddedTax))
    return result.scalars().all()
