from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, ConfigDict
from pydantic.alias_generators import to_camel


class CountryRead(BaseModel):
    """DTO for exposing countries publicly (mirrors Kotlin CountryDto)."""

    model_config = ConfigDict(from_attributes=True, alias_generator=to_camel, populate_by_name=True)

    id: int
    name: str
    created_at: datetime | None = None
    updated_at: datetime | None = None
