from __future__ import annotations

from datetime import datetime

from sqlalchemy import Column, DateTime, String, func
from sqlalchemy.orm import relationship
from sqlmodel import Field, Relationship, SQLModel


class UserRoleLink(SQLModel, table=True):
    """Association table for many-to-many relation between users and roles."""

    __tablename__ = "user_roles"  # type: ignore[assignment]

    user_id: int | None = Field(default=None, foreign_key="users.id", primary_key=True)
    role_id: int | None = Field(default=None, foreign_key="roles.id", primary_key=True)


class Role(SQLModel, table=True):
    __tablename__ = "roles"  # type: ignore[assignment]

    id: int | None = Field(default=None, primary_key=True)
    name: str = Field(sa_column=Column(String(50), unique=True, nullable=False))
    description: str | None = Field(default=None, sa_column=Column(String(255), nullable=True))

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

    # Back-reference to users (many-to-many)
    users: list[User] = Relationship(
        sa_relationship=relationship(
            "User",
            secondary="user_roles",
            back_populates="roles",
        )
    )

    def __repr__(self) -> str:  # pragma: no cover - debug representation only
        return f"Role(id={self.id!r}, name={self.name!r})"


class User(SQLModel, table=True):
    __tablename__ = "users"  # type: ignore[assignment]

    id: int | None = Field(default=None, primary_key=True)

    email: str = Field(sa_column=Column(String(255), unique=True, nullable=False))
    first_name: str | None = Field(default=None, sa_column=Column(String(255), nullable=True))
    last_name: str | None = Field(default=None, sa_column=Column(String(255), nullable=True))
    phone_number: str | None = Field(default=None, sa_column=Column(String(255), nullable=True))

    # Authentication-related fields
    password: str | None = Field(default=None, sa_column=Column(String(255), nullable=True))
    one_time_password: str | None = Field(default=None, sa_column=Column(String(255), nullable=True))
    one_time_password_created_at: datetime | None = Field(
        default=None, sa_column=Column(DateTime(timezone=True), nullable=True)
    )

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
    deleted_at: datetime | None = Field(
        default=None,
        sa_column=Column(DateTime(timezone=True), nullable=True),
    )

    # Many-to-many: users <-> roles
    roles: list[Role] = Relationship(
        sa_relationship=relationship(
            "Role",
            secondary="user_roles",
            back_populates="users",
        )
    )

    def __repr__(self) -> str:  # pragma: no cover - debug representation only
        return f"User(id={self.id!r}, email={self.email!r})"

    @property
    def is_active(self) -> bool:
        """True if the user is not soft-deleted."""
        return self.deleted_at is None


class Session(SQLModel, table=True):
    """Server-side session stored in the database.

    Links a random session ID to a user. Optionally supports expiration.
    """

    __tablename__ = "sessions"  # type: ignore[assignment]

    # Random, URL-safe token as the session identifier
    id: str = Field(sa_column=Column(String(128), primary_key=True))

    # Owner of this session
    user_id: int = Field(foreign_key="users.id", nullable=False)

    # Timestamps
    created_at: datetime | None = Field(
        default=None,
        sa_column=Column(DateTime(timezone=True), nullable=False, server_default=func.now()),
    )
    expires_at: datetime | None = Field(
        default=None,
        sa_column=Column(DateTime(timezone=True), nullable=True),
    )
