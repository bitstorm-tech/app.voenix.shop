from __future__ import annotations

from datetime import datetime

from sqlalchemy import Column, DateTime, Integer, String, func
from sqlmodel import Field, SQLModel


class Supplier(SQLModel, table=True):
    __tablename__ = "suppliers"

    id: int | None = Field(default=None, primary_key=True)

    # Core identity and contact info
    name: str | None = Field(default=None, sa_column=Column(String(255), nullable=True))
    title: str | None = Field(default=None, sa_column=Column(String(100), nullable=True))
    first_name: str | None = Field(default=None, sa_column=Column(String(255), nullable=True))
    last_name: str | None = Field(default=None, sa_column=Column(String(255), nullable=True))

    # Address
    street: str | None = Field(default=None, sa_column=Column(String(255), nullable=True))
    house_number: str | None = Field(default=None, sa_column=Column(String(50), nullable=True))
    city: str | None = Field(default=None, sa_column=Column(String(255), nullable=True))
    postal_code: int | None = Field(default=None, sa_column=Column(Integer, nullable=True))
    country_id: int | None = Field(default=None, sa_column=Column(Integer, nullable=True))

    # Communication
    phone_number1: str | None = Field(default=None, sa_column=Column(String(50), nullable=True))
    phone_number2: str | None = Field(default=None, sa_column=Column(String(50), nullable=True))
    phone_number3: str | None = Field(default=None, sa_column=Column(String(50), nullable=True))
    email: str | None = Field(default=None, sa_column=Column(String(255), nullable=True))
    website: str | None = Field(default=None, sa_column=Column(String(500), nullable=True))

    # Timestamps
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
        core = f"id={self.id!r}, name={self.name!r}"
        person = f"first_name={self.first_name!r}, last_name={self.last_name!r}"
        return f"Supplier({core}, {person})"
