from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, ConfigDict
from pydantic.alias_generators import to_camel


class ValueAddedTaxCreate(BaseModel):
    name: str
    percent: int
    description: str | None = None
    is_default: bool = False


class ValueAddedTaxUpdate(ValueAddedTaxCreate):
    pass


class ValueAddedTaxRead(BaseModel):
    model_config = ConfigDict(from_attributes=True, alias_generator=to_camel, populate_by_name=True)

    id: int
    name: str
    percent: int
    description: str | None = None
    is_default: bool
    created_at: datetime | None = None
    updated_at: datetime | None = None
