from __future__ import annotations

import re

from fastapi import HTTPException, status


def safe_filename(name: str) -> str:
    """Whitelist-only filename sanitizer. Raises HTTP 400 on invalid input."""
    if not re.fullmatch(r"[A-Za-z0-9._\-]+", name):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid filename")
    return name


__all__ = ["safe_filename"]
