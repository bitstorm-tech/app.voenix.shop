from __future__ import annotations

import base64

from fastapi import APIRouter, Depends, File, Form, HTTPException, UploadFile, status

from src.ai.factories import AIImageGeneratorFactory, AIImageProvider
from src.auth._internal.entities import User
from src.auth.api import require_admin
from src.image import StorageLocations, convert_image_to_png_bytes, store_image_bytes

from .schemas import GeminiEditResponse

router = APIRouter(prefix="/api/ai/images", tags=["ai"])


@router.post("/", response_model=GeminiEditResponse)
async def post_gemini_edit(
    image: UploadFile = File(..., description="Image to edit/manipulate"),
    prompt: str = Form(..., description="Instruction describing the edit"),
    n: int = Form(1, ge=1, le=8, description="Number of images to return"),
    provider: AIImageProvider = Form(AIImageProvider.GEMINI, description="GTP, Gemini or Flux"),
    _: User = Depends(require_admin),
):
    """Upload an image and a prompt, forward to Gemini, and return edited images.

    Accepts multipart/form-data with fields:
    - image: file
    - prompt: text
    - n: optional integer, number of images to generate (default 1)
    """
    try:
        # Basic validation of content type
        content_type = image.content_type or ""
        if not content_type.startswith("image/"):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Uploaded file must be an image",
            )

        data = await image.read()
        image_generator = AIImageGeneratorFactory.create(provider)
        outputs = image_generator.edit(
            image=data,
            prompt=prompt,
            candidate_count=n,
        )

        # Store each returned image under configured storage location
        storage_dir = StorageLocations().PROMPT_TEST

        # Try to convert to PNG if Pillow is available; otherwise store raw bytes
        for img_bytes in outputs:
            png_bytes = convert_image_to_png_bytes(img_bytes)
            store_image_bytes(png_bytes, storage_dir, ext="png")

        encoded = [base64.b64encode(b).decode("ascii") for b in outputs]
        return GeminiEditResponse(images=encoded)
    except HTTPException:
        raise
    except Exception as e:  # pragma: no cover - surface clean error to client
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"Gemini edit failed: {e}",
        )
