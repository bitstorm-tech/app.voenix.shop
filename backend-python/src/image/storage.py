from __future__ import annotations

import os
import re
import secrets
from pathlib import Path
from typing import Optional, Union


PathLike = Union[str, Path]


def _ensure_ext(ext: Optional[str]) -> str:
    if not ext:
        return ".png"
    ext = ext.strip()
    if not ext:
        return ".png"
    if not ext.startswith("."):
        ext = "." + ext
    return ext.lower()


def _safe_stem(stem: Optional[str]) -> str:
    if stem:
        cleaned = re.sub(r"[^A-Za-z0-9._-]+", "-", stem).strip("-_.")
        if cleaned:
            return cleaned
    return secrets.token_hex(8)


def store_image_bytes(
    data: bytes,
    directory: PathLike,
    filename: Optional[str] = None,
    *,
    ext: Optional[str] = None,
    overwrite: bool = False,
    mode: Optional[int] = None,
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
        if name_path.suffix:
            final_name = name_path.name
        else:
            final_name = name_path.name + _ensure_ext(ext)
    else:
        final_name = _safe_stem(None) + _ensure_ext(ext)

    dest = dir_path / final_name

    if dest.exists() and not overwrite:
        raise FileExistsError(f"File already exists: {dest}")

    tmp = dest.with_suffix(dest.suffix + ".tmp")
    tmp.write_bytes(data)
    os.replace(tmp, dest)

    if mode is not None:
        try:
            os.chmod(dest, mode)
        except Exception:
            pass

    return dest


__all__ = [
    "store_image_bytes",
]

