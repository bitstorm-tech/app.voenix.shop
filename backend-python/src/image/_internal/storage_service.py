from __future__ import annotations

import contextlib
import os
import uuid
from pathlib import Path

PathLike = str | Path


def _ensure_ext(ext: str | None) -> str:
    if not ext:
        return ".png"
    ext = ext.strip()
    if not ext:
        return ".png"
    if not ext.startswith("."):
        ext = "." + ext
    return ext.lower()


def store_image_bytes(
    data: bytes,
    directory: PathLike,
    filename: str | None = None,
    *,
    ext: str | None = None,
    overwrite: bool = False,
    mode: int | None = None,
) -> Path:
    """Persist image bytes to the filesystem and return the path.

    - Creates `directory` if it does not exist.
    - If `filename` is provided without an extension, `ext` (default .png) is appended.
    - If `filename` is not provided, a random name is generated with `ext`.
    - If `overwrite` is False and the destination exists, raises FileExistsError.
    - If `mode` is provided, applies it via os.chmod on the final file.
    """
    dir_path = Path(directory)
    dir_path.mkdir(parents=True, exist_ok=True)

    if filename:
        name_path = Path(filename)
        final_name = name_path.name if name_path.suffix else name_path.name + _ensure_ext(ext)
    else:
        final_name = str(uuid.uuid4()) + _ensure_ext(ext)

    dest = dir_path / final_name

    if dest.exists() and not overwrite:
        raise FileExistsError(f"File already exists: {dest}")

    tmp = dest.with_suffix(dest.suffix + ".tmp")
    tmp.write_bytes(data)
    os.replace(tmp, dest)

    if mode is not None:
        with contextlib.suppress(Exception):
            os.chmod(dest, mode)

    return dest


__all__ = [
    "store_image_bytes",
]
