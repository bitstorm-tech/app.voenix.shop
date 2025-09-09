from __future__ import annotations

from json import JSONDecodeError, loads

from .schemas import CropArea, UploadRequest


def parse_upload_request(
    *,
    request_json: str | None,
    image_type_field: str | None,
    crop_x: float | None,
    crop_y: float | None,
    crop_w: float | None,
    crop_h: float | None,
) -> UploadRequest:
    """Parse mixed multipart inputs into an UploadRequest.

    Accepts either a JSON 'request' part or discrete form fields for imageType/crop.*.
    Raises ValueError for invalid inputs (caller translates to HTTP errors).
    """
    if request_json:
        try:
            data = loads(request_json)
        except JSONDecodeError as exc:
            raise ValueError("Invalid JSON in 'request' part") from exc

        image_type = str(data.get("imageType") or data.get("type") or "").strip()
        crop_dict = data.get("cropArea")
        crop_area = None
        if isinstance(crop_dict, dict) and all(k in crop_dict for k in ("x", "y", "width", "height")):
            crop_area = CropArea(**crop_dict)
        return UploadRequest(imageType=image_type, cropArea=crop_area)

    if not image_type_field:
        raise ValueError("Missing imageType")

    crop_area = None
    if crop_x is not None and crop_y is not None and crop_w is not None and crop_h is not None:
        crop_area = CropArea(x=float(crop_x), y=float(crop_y), width=float(crop_w), height=float(crop_h))
    return UploadRequest(imageType=image_type_field, cropArea=crop_area)


__all__ = ["parse_upload_request"]
