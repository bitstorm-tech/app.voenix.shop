from __future__ import annotations

from typing import Annotated

from fastapi import APIRouter, Depends, File, Form, HTTPException, Request, UploadFile, status
from fastapi.responses import Response

from src.auth._internal.entities import User
from src.auth.api import require_admin
from src.image import StorageLocations, convert_image_to_png_bytes, store_image_bytes
from src.image._internal.image_conversion_service import crop_image_bytes
from src.image._internal.image_file_service import load_image_bytes_and_type
from src.image._internal.sanitize_service import safe_filename
from src.image._internal.schemas import UploadRequest
from src.image._internal.upload_request_service import parse_upload_request

router = APIRouter(
    prefix="/api/admin/images",
    tags=["images"],
    dependencies=[Depends(require_admin)],
)


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


@router.post("", status_code=status.HTTP_201_CREATED)
async def admin_upload_image(
    _admin: Annotated[User, Depends(require_admin)],  # noqa: ANN001
    http_request: Request,
    file: UploadFile = File(...),
    # Discrete fields (alternative to JSON 'request' part)
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
    # Support both styles for the 'request' part:
    # - as a regular form field (string)
    # - as a file/blob part with Content-Type application/json
    form = await http_request.form()
    request_json_value = None
    if "request" in form:
        req_part = form.get("request")
        if isinstance(req_part, UploadFile):
            # Frontend may send JSON as a Blob/file named 'request'
            content = await req_part.read()
            try:
                request_json_value = content.decode("utf-8", errors="ignore")
            except Exception:
                request_json_value = None
        else:
            request_json_value = str(req_part) if req_part is not None else None

    req = _parse_mixed_upload_request(
        request_json=request_json_value,
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


@router.get("/prompt-test/{filename}")
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


@router.delete("/prompt-test/{filename}", status_code=status.HTTP_204_NO_CONTENT)
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
