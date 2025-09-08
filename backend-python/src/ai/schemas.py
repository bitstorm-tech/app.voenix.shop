from __future__ import annotations

from pydantic import BaseModel


class GeminiEditResponse(BaseModel):
    images: list[str]
