from __future__ import annotations

from pydantic import BaseModel


class UserPublic(BaseModel):
    id: int
    email: str
    firstName: str | None = None
    lastName: str | None = None
    phoneNumber: str | None = None
    roles: list[str] = []


class LoginResponse(BaseModel):
    user: UserPublic
    sessionId: str
    roles: list[str]


class SessionInfo(BaseModel):
    authenticated: bool
    user: UserPublic
    roles: list[str]
