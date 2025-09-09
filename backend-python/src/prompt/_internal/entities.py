from __future__ import annotations

from datetime import datetime

from sqlalchemy import Boolean, Column, DateTime, Integer, String, Text, func
from sqlalchemy.orm import relationship
from sqlmodel import Field, Relationship, SQLModel


class PromptCategory(SQLModel, table=True):
    __tablename__ = "prompt_categories"
    __allow_unmapped__ = True

    id: int | None = Field(default=None, primary_key=True)
    name: str = Field(sa_column=Column(String(255), unique=True, nullable=False))

    # Relationships
    subcategories: list[PromptSubCategory] = Relationship(
        sa_relationship=relationship("PromptSubCategory", back_populates="prompt_category")
    )

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
        return f"PromptCategory(id={self.id!r}, name={self.name!r})"


class PromptSubCategory(SQLModel, table=True):
    __tablename__ = "prompt_subcategories"
    __allow_unmapped__ = True

    id: int | None = Field(default=None, primary_key=True)

    prompt_category_id: int = Field(foreign_key="prompt_categories.id", nullable=False)
    prompt_category: PromptCategory | None = Relationship(
        sa_relationship=relationship("PromptCategory", back_populates="subcategories")
    )

    name: str = Field(sa_column=Column(String(255), nullable=False))
    description: str | None = Field(default=None, sa_column=Column(Text, nullable=True))

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
        return f"PromptSubCategory(id={self.id!r}, name={self.name!r}, category_id={self.prompt_category_id!r})"


class PromptSlotType(SQLModel, table=True):
    __tablename__ = "prompt_slot_types"
    __allow_unmapped__ = True

    id: int | None = Field(default=None, primary_key=True)
    name: str = Field(sa_column=Column(String(255), unique=True, nullable=False))
    position: int = Field(sa_column=Column(Integer, unique=True, nullable=False))

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
        return f"PromptSlotType(id={self.id!r}, name={self.name!r}, position={self.position!r})"


class PromptSlotVariant(SQLModel, table=True):
    __tablename__ = "prompt_slot_variants"
    __allow_unmapped__ = True

    id: int | None = Field(default=None, primary_key=True)

    prompt_slot_type_id: int = Field(foreign_key="prompt_slot_types.id", nullable=False)
    prompt_slot_type: PromptSlotType | None = Relationship(sa_relationship=relationship("PromptSlotType"))

    name: str = Field(sa_column=Column(String(255), unique=True, nullable=False))
    prompt: str | None = Field(default=None, sa_column=Column(Text, nullable=True))
    description: str | None = Field(default=None, sa_column=Column(Text, nullable=True))
    example_image_filename: str | None = Field(default=None, sa_column=Column(String(500), nullable=True))

    # Backref to mapping table
    prompt_slot_variant_mappings: list[PromptSlotVariantMapping] = Relationship(
        sa_relationship=relationship("PromptSlotVariantMapping", back_populates="prompt_slot_variant")
    )

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
        return f"PromptSlotVariant(id={self.id!r}, name={self.name!r})"


class Prompt(SQLModel, table=True):
    __tablename__ = "prompts"
    __allow_unmapped__ = True

    id: int | None = Field(default=None, primary_key=True)

    title: str = Field(sa_column=Column(String(500), nullable=False))
    prompt_text: str | None = Field(default=None, sa_column=Column(Text, nullable=True))

    category_id: int | None = Field(default=None, foreign_key="prompt_categories.id")
    category: PromptCategory | None = Relationship(sa_relationship=relationship("PromptCategory", lazy="joined"))

    subcategory_id: int | None = Field(default=None, foreign_key="prompt_subcategories.id")
    subcategory: PromptSubCategory | None = Relationship(
        sa_relationship=relationship("PromptSubCategory", lazy="joined")
    )

    active: bool = Field(default=True, sa_column=Column(Boolean, nullable=False, server_default="true"))
    example_image_filename: str | None = Field(default=None, sa_column=Column(String(500), nullable=True))

    # Mapping relations
    prompt_slot_variant_mappings: list[PromptSlotVariantMapping] = Relationship(
        sa_relationship=relationship("PromptSlotVariantMapping", back_populates="prompt")
    )

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
        return f"Prompt(id={self.id!r}, title={self.title!r}, active={self.active!r})"


class PromptSlotVariantMapping(SQLModel, table=True):
    """Association entity between Prompt and PromptSlotVariant.

    Composite PK: (prompt_id, slot_id). Stores creation timestamp for mapping.
    """

    __tablename__ = "prompt_slot_variant_mappings"
    __allow_unmapped__ = True

    prompt_id: int | None = Field(default=None, foreign_key="prompts.id", primary_key=True)
    slot_id: int | None = Field(default=None, foreign_key="prompt_slot_variants.id", primary_key=True)

    # Relationships
    prompt: Prompt | None = Relationship(
        sa_relationship=relationship("Prompt", back_populates="prompt_slot_variant_mappings")
    )
    prompt_slot_variant: PromptSlotVariant | None = Relationship(
        sa_relationship=relationship("PromptSlotVariant", back_populates="prompt_slot_variant_mappings")
    )

    created_at: datetime | None = Field(
        default=None,
        sa_column=Column(DateTime(timezone=True), nullable=False, server_default=func.now()),
    )

    def __repr__(self) -> str:  # pragma: no cover - debug representation only
        return f"PromptSlotVariantMapping(prompt_id={self.prompt_id!r}, slot_id={self.slot_id!r})"
