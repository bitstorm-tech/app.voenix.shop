from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel


class CountryRead(BaseModel):
    """DTO for exposing countries publicly (mirrors Kotlin CountryDto)."""

    model_config = dict(from_attributes=True)

    id: int
    name: str
    created_at: datetime | None = None
    updated_at: datetime | None = None
