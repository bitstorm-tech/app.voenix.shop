from __future__ import annotations

import mimetypes
import uuid
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path

from .locations import StorageLocations


@dataclass
class UserImageItem:
    id: int
    uuid: str
    filename: str
    originalFilename: str | None
    type: str  # 'uploaded' | 'generated'
    contentType: str | None
    fileSize: int | None
    promptId: int | None
    uploadedImageId: int | None
    userId: int
    createdAt: str
    imageUrl: str
    thumbnailUrl: str | None


def user_images_dir(user_id: int) -> Path:
    return StorageLocations().PRIVATE_IMAGES / str(user_id)


def scan_user_images(user_id: int) -> list[UserImageItem]:
    directory = user_images_dir(user_id)
    if not directory.exists():
        return []
    items: list[UserImageItem] = []
    i = 1
    for entry in directory.iterdir():
        if not entry.is_file():
            continue
        name = entry.name
        if "_generated_" in name:
            img_type = "generated"
            uuid_part = name.split("_generated_")[0]
        elif "_original" in name:
            img_type = "uploaded"
            uuid_part = name.split("_original")[0]
        else:
            img_type = "uploaded"
            uuid_part = name.split(".")[0]

        try:
            _ = uuid.UUID(uuid_part)
            uuid_str = uuid_part
        except ValueError:
            uuid_str = str(uuid.uuid4())

        stat = entry.stat()
        ctime = datetime.fromtimestamp(stat.st_mtime).isoformat()
        content_type, _ = mimetypes.guess_type(name)
        items.append(
            UserImageItem(
                id=i,
                uuid=uuid_str,
                filename=name,
                originalFilename=None,
                type=img_type,
                contentType=content_type,
                fileSize=stat.st_size,
                promptId=None,
                uploadedImageId=None,
                userId=user_id,
                createdAt=ctime,
                imageUrl=f"/api/user/images/{name}",
                thumbnailUrl=None,
            )
        )
        i += 1
    return items


def sort_filter_paginate(
    items: list[UserImageItem],
    *,
    type_: str,
    sort_by: str,
    sort_dir: str,
    page: int,
    size: int,
):
    t = type_.lower()
    if t in {"uploaded", "generated"}:
        items = [it for it in items if it.type == t]

    if sort_by == "type":
        items.sort(key=lambda it: it.type)
    else:
        items.sort(key=lambda it: it.createdAt)

    if sort_dir.upper() == "DESC":
        items.reverse()

    total_elements = len(items)
    total_pages = (total_elements + size - 1) // size if size > 0 else 1
    start = page * size
    end = start + size
    page_items = items[start:end] if start < total_elements else []

    return {
        "content": [item.__dict__ for item in page_items],
        "currentPage": page,
        "totalPages": total_pages,
        "totalElements": total_elements,
        "size": size,
    }


__all__ = [
    "UserImageItem",
    "user_images_dir",
    "scan_user_images",
    "sort_filter_paginate",
]
