from __future__ import annotations

from fastapi import APIRouter, Depends
from sqlalchemy import select
from sqlalchemy.orm import Session

from src.database import get_db

from ._internal.entities import Country
from .schemas import CountryRead

router = APIRouter(
    prefix="/api/public/countries",
    tags=["countries"],
)


@router.get("/", response_model=list[CountryRead])
def get_countries(db: Session = Depends(get_db)):
    """Return all countries (public endpoint).

    Mirrors Kotlin controller `GET /api/public/countries`.
    """
    result = db.execute(select(Country))
    return result.scalars().all()
