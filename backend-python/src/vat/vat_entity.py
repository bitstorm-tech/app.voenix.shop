from __future__ import annotations

from datetime import datetime

from sqlalchemy import Boolean, Column, DateTime, Integer, String, Text, func
from sqlmodel import Field, SQLModel


class ValueAddedTax(SQLModel, table=True):
    __tablename__ = "value_added_taxes"

    id: int | None = Field(default=None, primary_key=True)
    name: str = Field(sa_column=Column(String(255), unique=True, nullable=False))
    percent: int = Field(sa_column=Column(Integer, nullable=False))
    description: str | None = Field(default=None, sa_column=Column(Text, nullable=True))
    is_default: bool = Field(default=False, sa_column=Column(Boolean, nullable=False))

    created_at: datetime | None = Field(
        default=None,
        sa_column=Column(
            DateTime(timezone=True), nullable=False, server_default=func.now()
        ),
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
        return (
            f"ValueAddedTax(id={self.id!r}, name={self.name!r}, percent={self.percent!r}, "
            f"is_default={self.is_default!r})"
        )
