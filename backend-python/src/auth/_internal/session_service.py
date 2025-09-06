from __future__ import annotations

import secrets
from datetime import UTC, datetime, timedelta

from sqlalchemy import select
from sqlalchemy.orm import Session

from .entities import Session as DBSession
from .entities import User

DEFAULT_TTL_SECONDS = 60 * 60 * 24 * 7  # 7 days


def create_session_for_user(db: Session, user_id: int, ttl_seconds: int | None = DEFAULT_TTL_SECONDS) -> str:
    """Create a new DB-backed session for a user and return the session ID."""
    session_id = secrets.token_urlsafe(32)
    expires_at = datetime.now(UTC) + timedelta(seconds=ttl_seconds) if ttl_seconds and ttl_seconds > 0 else None

    db_session = DBSession(id=session_id, user_id=user_id, expires_at=expires_at)
    db.add(db_session)
    db.commit()
    return session_id


def delete_session(db: Session, session_id: str | None) -> None:
    if not session_id:
        return
    obj = db.get(DBSession, session_id)
    if obj is not None:
        db.delete(obj)
        db.commit()


def get_user_from_session(db: Session, session_id: str | None) -> User | None:
    if not session_id:
        return None

    # Load session and join user
    result = db.execute(
        select(User, DBSession).join(DBSession, DBSession.user_id == User.id).where(DBSession.id == session_id)
    )
    row = result.first()
    if not row:
        return None

    user, db_sess = row[0], row[1]

    # Check expiration if set
    if db_sess.expires_at is not None and datetime.now(UTC) > db_sess.expires_at:
        # Expired: remove and treat as missing
        db.delete(db_sess)
        db.commit()
        return None

    return user
