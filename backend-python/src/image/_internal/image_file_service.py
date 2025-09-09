from __future__ import annotations

import mimetypes
from pathlib import Path


def load_image_bytes_and_type(path: Path) -> tuple[bytes, str]:
    """Read a file's bytes and return a best-effort content type.

    Raises FileNotFoundError if the path does not exist.
    Defaults to "image/png" when type cannot be guessed.
    """
    if not path.exists():
        raise FileNotFoundError(path)
    data = path.read_bytes()
    content_type, _ = mimetypes.guess_type(path.name)
    return data, (content_type or "image/png")


__all__ = ["load_image_bytes_and_type"]
