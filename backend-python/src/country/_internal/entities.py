from __future__ import annotations

from datetime import datetime

from sqlalchemy import Column, DateTime, String, func
from sqlmodel import Field, SQLModel


class Country(SQLModel, table=True):
    __tablename__ = "countries"

    id: int | None = Field(default=None, primary_key=True)
    name: str = Field(sa_column=Column(String(255), unique=True, nullable=False))

    created_at: datetime | None = Field(
        default=None,
        sa_column=Column(DateTime(timezone=True), nullable=False, server_default=func.now()),
    )
    updated_at: datetime | None = Field(
        default=None,
        sa_column=Column(
            DateTime(timezone=True),
            nullable=False,
            server_default=func.now(),
            onupdate=func.now(),
        ),
    )

    def __repr__(self) -> str:  # pragma: no cover - debug representation only
        return f"Country(id={self.id!r}, name={self.name!r})"
