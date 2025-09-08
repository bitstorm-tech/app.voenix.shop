from __future__ import annotations

from pydantic import BaseModel, ConfigDict
from pydantic.alias_generators import to_camel


class UserPublic(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    id: int
    email: str
    first_name: str | None = None
    last_name: str | None = None
    phone_number: str | None = None
    roles: list[str] = []


class LoginResponse(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    user: UserPublic
    session_id: str
    roles: list[str]


class SessionInfo(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    authenticated: bool
    user: UserPublic
    roles: list[str]
