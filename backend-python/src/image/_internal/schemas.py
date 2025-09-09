from __future__ import annotations

from pydantic import BaseModel, ConfigDict, Field


class CropArea(BaseModel):
    x: float
    y: float
    width: float
    height: float


class UploadRequest(BaseModel):
    model_config = ConfigDict(extra="ignore")

    imageType: str = Field(..., min_length=1)
    cropArea: CropArea | None = None


__all__ = [
    "CropArea",
    "UploadRequest",
]
