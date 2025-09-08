from __future__ import annotations

import base64
import hashlib
import secrets
from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException, Request, Response, Security, status
from fastapi.security import APIKeyCookie
from sqlalchemy import select
from sqlalchemy.orm import Session

from src.database import get_db

from ._internal.entities import User
from ._internal.session_service import (
    create_session_for_user,
    delete_session,
    get_user_from_session,
)
from .schemas import LoginResponse, SessionInfo, UserPublic

router = APIRouter(prefix="/api/auth", tags=["auth"])


# Cookie extractor using FastAPI's built-in APIKeyCookie security scheme
session_cookie = APIKeyCookie(name="session_id", auto_error=False)


def _user_to_public(user: User, roles: list[str]) -> UserPublic:
    return UserPublic(
        id=user.id or 0,
        email=user.email,
        firstName=user.first_name,
        lastName=user.last_name,
        phoneNumber=user.phone_number,
        roles=roles,
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


@router.post("/login", response_model=LoginResponse)
async def login(
    response: Response,
    request: Request,
    db: Session = Depends(get_db),
):
    """Login with email/password (JSON or form) and set an HttpOnly cookie.

    Accepts either:
    - JSON: {"email": "...", "password": "..."}
    - Form (OAuth2 style): username=<email>&password=...
    """
    content_type = (request.headers.get("content-type") or "").lower()
    email: str | None = None
    password: str | None = None

    try:
        if "application/json" in content_type:
            payload = await request.json()
            if isinstance(payload, dict):
                email = payload.get("email") or payload.get("username")
                password = payload.get("password")
        else:
            form = await request.form()
            email = form.get("email") or form.get("username")  # type: ignore[assignment]
            password = form.get("password")  # type: ignore[assignment]
    except Exception:
        # Fall through to validation error below
        pass

    if not email or not password:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Missing credentials: provide email and password",
        )

    user = _get_user_by_email(db, email)
    if not user or not user.is_active or not _verify_password(password, user.password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username or password",
            headers={"WWW-Authenticate": "Bearer"},
        )

    session_id = create_session_for_user(db, user.id or 0)

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

    # Prepare response matching frontend expectations (LoginResponse)
    role_names = [r.name for r in (user.roles or [])]
    return LoginResponse(user=_user_to_public(user, role_names), sessionId=session_id, roles=role_names)


def get_current_user(
    session_id: Annotated[str | None, Security(session_cookie)],
    db: Session = Depends(get_db),
) -> User:
    user = get_user_from_session(db, session_id)
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


@router.get("/session", response_model=SessionInfo)
def read_me(current_user: User = Depends(get_current_user)):
    # Return SessionInfo shape expected by frontend
    role_names = [r.name for r in (current_user.roles or [])]
    return SessionInfo(authenticated=True, user=_user_to_public(current_user, role_names), roles=role_names)


@router.post("/logout")
def logout(
    response: Response,
    session_id: Annotated[str | None, Security(session_cookie)],
    db: Session = Depends(get_db),
):
    # Remove the DB-backed session if present
    delete_session(db=db, session_id=session_id)

    # Remove cookie by setting it expired
    response.delete_cookie(key="session_id", path="/")
    return {"ok": True}
