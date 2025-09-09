from __future__ import annotations

from typing import Annotated

from fastapi import APIRouter, Depends, File, Form, HTTPException, Query, UploadFile, status
from fastapi.responses import Response

from src.auth._internal.entities import User
from src.auth.api import require_admin, require_roles
from src.image import StorageLocations, convert_image_to_png_bytes, store_image_bytes
from src.image._internal.image_conversion_service import crop_image_bytes
from src.image._internal.image_file_service import load_image_bytes_and_type
from src.image._internal.sanitize_service import safe_filename
from src.image._internal.schemas import UploadRequest
from src.image._internal.upload_request_service import parse_upload_request
from src.image._internal.user_images_service import scan_user_images, sort_filter_paginate, user_images_dir

router = APIRouter(tags=["images"])


# -----------------------------
# Admin: generic upload (multipart)
# -----------------------------


def _parse_mixed_upload_request(
    *,
    request_json: str | None,
    image_type_field: str | None,
    crop_x: float | None,
    crop_y: float | None,
    crop_w: float | None,
    crop_h: float | None,
) -> UploadRequest:
    try:
        return parse_upload_request(
            request_json=request_json,
            image_type_field=image_type_field,
            crop_x=crop_x,
            crop_y=crop_y,
            crop_w=crop_w,
            crop_h=crop_h,
        )
    except ValueError as exc:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(exc)) from exc


@router.post("/api/admin/images", status_code=status.HTTP_201_CREATED)
async def admin_upload_image(
    _admin: Annotated[User, Depends(require_admin)],  # noqa: ANN001
    file: UploadFile = File(...),
    # Either JSON 'request' part (from frontend) or discrete fields
    request: str | None = Form(None, description="JSON with imageType and cropArea"),
    imageType: str | None = Form(None),
    cropX: float | None = Form(None),
    cropY: float | None = Form(None),
    cropWidth: float | None = Form(None),
    cropHeight: float | None = Form(None),
):
    """Upload an image for admin-managed locations.

    Supports two payload styles:
    - `file` + `request` (JSON: {imageType, cropArea:{x,y,width,height}})
    - `file` + form fields (imageType, cropX, cropY, cropWidth, cropHeight)
    """
    req = _parse_mixed_upload_request(
        request_json=request,
        image_type_field=imageType,
        crop_x=cropX,
        crop_y=cropY,
        crop_w=cropWidth,
        crop_h=cropHeight,
    )

    if not (file.content_type or "").startswith("image/"):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Uploaded file must be an image")

    data = await file.read()
    # Optional crop via nested model
    crop = req.cropArea
    if crop is not None:
        data = crop_image_bytes(
            data,
            x=float(crop.x),
            y=float(crop.y),
            width=float(crop.width),
            height=float(crop.height),
        )

    # Convert to PNG
    png_bytes = convert_image_to_png_bytes(data)

    # Store under resolved directory
    try:
        directory = StorageLocations().resolve_admin_dir(req.imageType)
    except ValueError as exc:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(exc)) from exc
    path = store_image_bytes(png_bytes, directory, ext="png")

    return {"filename": path.name, "imageType": req.imageType}


# -----------------------------
# Admin: prompt-test serve/delete
# -----------------------------


@router.get("/api/admin/images/prompt-test/{filename}")
def admin_get_prompt_test_image(
    filename: str,
    _admin: Annotated[User, Depends(require_admin)],  # noqa: ANN001
):
    fname = safe_filename(filename)
    path = StorageLocations().PROMPT_TEST / fname
    if not path.exists():
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Not found")

    try:
        data, content_type = load_image_bytes_and_type(path)
    except FileNotFoundError:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Not found")
    headers = {"Content-Disposition": f'inline; filename="{path.name}"'}
    return Response(content=data, media_type=content_type, headers=headers)


@router.delete("/api/admin/images/prompt-test/{filename}", status_code=status.HTTP_204_NO_CONTENT)
def admin_delete_prompt_test_image(
    filename: str,
    _admin: Annotated[User, Depends(require_admin)],  # noqa: ANN001
):
    fname = safe_filename(filename)
    path = StorageLocations().PROMPT_TEST / fname
    if path.exists():
        try:
            path.unlink()
        except Exception:
            # Surface as 500 if filesystem fails
            raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Delete failed")
    return Response(status_code=status.HTTP_204_NO_CONTENT)


# -----------------------------
# User: serve image by filename (per-user folder)
# -----------------------------


@router.get("/api/user/images/{filename}")
def user_get_image(
    filename: str,
    current_user: Annotated[User, Depends(require_roles("USER", "ADMIN"))],
):
    # Files are stored under private/images/{userId}/
    fname = safe_filename(filename)
    base = user_images_dir(current_user.id or 0)
    path = base / fname
    if not path.exists():
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Not found")

    try:
        data, content_type = load_image_bytes_and_type(path)
    except FileNotFoundError:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Not found")
    headers = {"Content-Disposition": f'inline; filename="{path.name}"'}
    return Response(content=data, media_type=content_type, headers=headers)


# -----------------------------
# User: list images (filesystem-based, simple pagination)
# -----------------------------


@router.get("/api/user/images")
def list_user_images(
    current_user: Annotated[User, Depends(require_roles("USER", "ADMIN"))],
    page: int = Query(0, ge=0),
    size: int = Query(20, ge=1, le=200),
    type: str = Query("all"),  # all|uploaded|generated
    sortBy: str = Query("createdAt"),  # createdAt|type
    sortDirection: str = Query("DESC"),  # ASC|DESC
):
    items = scan_user_images(current_user.id or 0)
    return sort_filter_paginate(
        items,
        type_=type,
        sort_by=sortBy,
        sort_dir=sortDirection,
        page=page,
        size=size,
    )
