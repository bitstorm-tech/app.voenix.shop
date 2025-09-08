from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel


class ValueAddedTaxCreate(BaseModel):
    name: str
    percent: int
    description: str | None = None
    is_default: bool = False


class ValueAddedTaxUpdate(ValueAddedTaxCreate):
    pass


class ValueAddedTaxRead(BaseModel):
    model_config = dict(from_attributes=True)

    id: int
    name: str
    percent: int
    description: str | None = None
    is_default: bool
    created_at: datetime | None = None
    updated_at: datetime | None = None
