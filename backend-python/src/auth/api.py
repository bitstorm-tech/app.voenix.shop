from __future__ import annotations

import base64
import hashlib
import secrets
from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException, Response, Security, status
from fastapi.security import APIKeyCookie, OAuth2PasswordRequestForm
from sqlalchemy import select
from sqlalchemy.orm import Session
from sqlmodel import SQLModel

from src.database import get_db

from ._internal.entities import User

router = APIRouter(prefix="/api/auth", tags=["auth"])


# In-memory session store: session_id -> user_id
_SESSIONS: dict[str, int] = {}


# Cookie extractor using FastAPI's built-in APIKeyCookie security scheme
session_cookie = APIKeyCookie(name="session_id", auto_error=False)


class UserPublic(SQLModel):
    id: int
    email: str
    first_name: str | None = None
    last_name: str | None = None
    phone_number: str | None = None


def _user_to_public(user: User) -> UserPublic:
    return UserPublic(
        id=user.id or 0,
        email=user.email,
        first_name=user.first_name,
        last_name=user.last_name,
        phone_number=user.phone_number,
    )


def _verify_password(plain_password: str, stored_password: str | None) -> bool:
    """Verify a plaintext password against the stored representation.

    Supports:
    - PBKDF2-SHA256 format: "pbkdf2_sha256$<iterations>$<salt_b64>$<hash_b64>"
    - Plain text fallback (for existing un-hashed records)
    """
    if not stored_password:
        return False

    if stored_password.startswith("pbkdf2_sha256$"):
        try:
            _, iter_str, salt_b64, hash_b64 = stored_password.split("$", 3)
            iterations = int(iter_str)
            salt = base64.b64decode(salt_b64)
            expected = base64.b64decode(hash_b64)
            derived = hashlib.pbkdf2_hmac("sha256", plain_password.encode("utf-8"), salt, iterations)
            # Use constant-time compare on bytes
            return secrets.compare_digest(derived, expected)
        except Exception:
            return False

    # Fallback: direct compare for legacy plaintext passwords
    return secrets.compare_digest(plain_password, stored_password)


def _get_user_by_email(db: Session, email: str) -> User | None:
    result = db.execute(select(User).where(User.email == email))
    return result.scalar_one_or_none()


def _create_session_for_user(user_id: int) -> str:
    session_id = secrets.token_urlsafe(32)
    _SESSIONS[session_id] = user_id
    return session_id


def _pop_session(session_id: str | None) -> None:
    if session_id and session_id in _SESSIONS:
        _SESSIONS.pop(session_id, None)


def _get_user_from_session(db: Session, session_id: str | None) -> User | None:
    if not session_id:
        return None
    user_id = _SESSIONS.get(session_id)
    if not user_id:
        return None
    result = db.execute(select(User).where(User.id == user_id))
    return result.scalar_one_or_none()


@router.post("/login", response_model=UserPublic)
def login(
    response: Response,
    form_data: Annotated[OAuth2PasswordRequestForm, Depends()],
    db: Session = Depends(get_db),
):
    """Login with username/password and set an HttpOnly session cookie.

    Notes:
    - `form_data.username` is treated as the user's email.
    - Session IDs are stored in-memory and not persisted.
    """
    user = _get_user_by_email(db, form_data.username)
    if not user or not user.is_active or not _verify_password(form_data.password, user.password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username or password",
            headers={"WWW-Authenticate": "Bearer"},
        )

    session_id = _create_session_for_user(user.id or 0)

    # Set cookie: HttpOnly for security; adjust samesite/secure for your environment
    response.set_cookie(
        key="session_id",
        value=session_id,
        httponly=True,
        samesite="lax",
        secure=False,
        max_age=60 * 60 * 24 * 7,  # 7 days
        path="/",
    )

    return _user_to_public(user)


def get_current_user(
    session_id: Annotated[str | None, Security(session_cookie)],
    db: Session = Depends(get_db),
) -> User:
    user = _get_user_from_session(db, session_id)
    if not user or not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Not authenticated",
        )
    return user


def require_roles(*allowed_roles: str):
    """Dependency factory that ensures the current user has any of the given roles.

    Usage:
        Depends(require_roles("ADMIN"))
    """

    def _dep(current_user: User = Depends(get_current_user)) -> User:
        role_names = {r.name for r in (current_user.roles or [])}
        if not any(role in role_names for role in allowed_roles):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Forbidden: insufficient role")
        return current_user

    return _dep


def require_admin(current_user: User = Depends(get_current_user)) -> User:
    """Dependency that restricts access to users with the ADMIN role."""
    role_names = {r.name for r in (current_user.roles or [])}
    if "ADMIN" not in role_names:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Forbidden: admin only")
    return current_user


@router.get("/me", response_model=UserPublic)
def read_me(current_user: User = Depends(get_current_user)):
    return _user_to_public(current_user)


@router.post("/logout")
def logout(
    response: Response,
    session_id: Annotated[str | None, Security(session_cookie)],
):
    _pop_session(session_id)

    # Remove cookie by setting it expired
    response.delete_cookie(key="session_id", path="/")
    return {"ok": True}
