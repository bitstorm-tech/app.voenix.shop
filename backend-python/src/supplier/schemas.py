from __future__ import annotations

from datetime import datetime
from typing import ClassVar

from pydantic import AnyUrl, BaseModel, ConfigDict, EmailStr, field_validator
from pydantic.alias_generators import to_camel

from ._internal.entities import Supplier


class SupplierBase(BaseModel):
    """Base schema with validation rules (mirrors Kotlin SupplierRequest)."""

    # Accept camelCase input while keeping snake_case internally
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    name: str | None = None
    title: str | None = None
    first_name: str | None = None
    last_name: str | None = None

    street: str | None = None
    house_number: str | None = None
    city: str | None = None
    postal_code: int | None = None
    country_id: int | None = None

    phone_number1: str | None = None
    phone_number2: str | None = None
    phone_number3: str | None = None
    email: EmailStr | None = None
    website: AnyUrl | None = None

    # --- Validation helpers (mirror Kotlin SupplierRequest) ---
    # _PHONE_RE = re.compile(r"^[+]?[0-9\s\-\(\)]+$")

    @staticmethod
    def _validate_len(value: str | None, max_len: int, field: str) -> str | None:
        if value is None:
            return value
        if len(value) > max_len:
            raise ValueError(f"{field} must not exceed {max_len} characters")
        return value

    @field_validator("name")
    @classmethod
    def _v_name(cls, v: str | None):
        return cls._validate_len(v, 255, "name")

    @field_validator("title")
    @classmethod
    def _v_title(cls, v: str | None):
        return cls._validate_len(v, 100, "title")

    @field_validator("first_name", "last_name", "street", "city")
    @classmethod
    def _v_common_255(cls, v: str | None):
        return cls._validate_len(v, 255, "field")

    @field_validator("house_number")
    @classmethod
    def _v_house_number(cls, v: str | None):
        return cls._validate_len(v, 50, "house_number")

    @field_validator("phone_number1", "phone_number2", "phone_number3")
    @classmethod
    def _v_phone(cls, v: str | None):
        if v is None:
            return v
        if len(v) > 50:
            raise ValueError("phone number must not exceed 50 characters")
        # if not cls._PHONE_RE.match(v):
        #     raise ValueError("Invalid phone number format")
        return v

    @field_validator("email")
    @classmethod
    def _v_email_len(cls, v: EmailStr | None):
        if v is None:
            return v
        if len(str(v)) > 255:
            raise ValueError("email must not exceed 255 characters")
        return v

    @field_validator("website")
    @classmethod
    def _v_website_len(cls, v: AnyUrl | None):
        if v is None:
            return v
        if len(str(v)) > 500:
            raise ValueError("website must not exceed 500 characters")
        return v

    @field_validator("postal_code")
    @classmethod
    def _v_postal_code(cls, v: int | None):
        if v is None:
            return v
        if v < 1:
            raise ValueError("postal_code must be positive")
        return v


class SupplierCreate(SupplierBase):
    """Schema for create requests (all fields optional)."""


class SupplierUpdate(SupplierBase):
    """Schema for update requests (all fields optional)."""

    # Keep a whitelist of fields that may be updated through the API
    _UPDATABLE_FIELDS: ClassVar[set[str]] = {
        "name",
        "title",
        "first_name",
        "last_name",
        "street",
        "house_number",
        "city",
        "postal_code",
        "country_id",
        "phone_number1",
        "phone_number2",
        "phone_number3",
        "email",
        "website",
    }

    def apply(self, supplier: Supplier) -> None:
        """Apply provided (set) fields onto the given Supplier instance.

        Uses snake_case internal names and respects exclude_unset to avoid clearing fields.
        """
        data = self.model_dump(mode="json", exclude_unset=True)
        for field in self._UPDATABLE_FIELDS:
            if field in data:
                setattr(supplier, field, data[field])


class SupplierRead(BaseModel):
    """Response DTO for suppliers (parses from ORM)."""

    model_config = ConfigDict(from_attributes=True, alias_generator=to_camel, populate_by_name=True)

    id: int

    name: str | None = None
    title: str | None = None
    first_name: str | None = None
    last_name: str | None = None

    street: str | None = None
    house_number: str | None = None
    city: str | None = None
    postal_code: int | None = None
    country_id: int | None = None
    country_name: str | None = None  # reserved for enrichment

    phone_number1: str | None = None
    phone_number2: str | None = None
    phone_number3: str | None = None
    email: str | None = None
    website: str | None = None

    created_at: datetime | None = None
    updated_at: datetime | None = None
