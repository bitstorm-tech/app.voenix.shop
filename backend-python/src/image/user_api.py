from __future__ import annotations

from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException, Query
from fastapi.responses import Response

from src.auth._internal.entities import User
from src.auth.api import require_roles
from src.image._internal.image_file_service import load_image_bytes_and_type
from src.image._internal.sanitize_service import safe_filename
from src.image._internal.user_images_service import (
    scan_user_images,
    sort_filter_paginate,
    user_images_dir,
)

router = APIRouter(prefix="/api/user/images", tags=["images"])


@router.get("/{filename}")
def user_get_image(
    filename: str,
    current_user: Annotated[User, Depends(require_roles("USER", "ADMIN"))],
):
    # Files are stored under private/images/{userId}/
    fname = safe_filename(filename)
    base = user_images_dir(current_user.id or 0)
    path = base / fname
    if not path.exists():
        raise HTTPException(status_code=404, detail="Not found")

    try:
        data, content_type = load_image_bytes_and_type(path)
    except FileNotFoundError:
        raise HTTPException(status_code=404, detail="Not found")
    headers = {"Content-Disposition": f'inline; filename="{path.name}"'}
    return Response(content=data, media_type=content_type, headers=headers)


@router.get("")
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
