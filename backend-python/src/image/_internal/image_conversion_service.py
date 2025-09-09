"""
Image conversion utilities (service).

This module provides helper functions to convert an input image of many
popular formats (JPEG/JPG, PNG, WEBP, GIF, BMP, TIFF, etc.) into a PNG.

Dependencies:
- Requires Pillow (PIL). If it's not installed, install with:
    uv add pillow
  or add "pillow" to your project dependencies.

Typical usage:

    from image.image_converter import convert_image_to_png_bytes, convert_image_to_png_file

    # Convert from bytes → PNG bytes
    png_bytes = convert_image_to_png_bytes(jpeg_bytes)

    # Convert from file path → write .png alongside the original
    output_path = convert_image_to_png_file("/path/to/photo.jpg")
"""

from __future__ import annotations

import contextlib
import io
from io import BytesIO
from pathlib import Path
from typing import BinaryIO

from PIL import Image, ImageOps, UnidentifiedImageError  # type: ignore

PathLike = str | Path
BytesLike = bytes | bytearray
ImageInput = BytesLike | BinaryIO | PathLike


def _open_image(source: ImageInput) -> Image.Image:
    """Open an image from bytes, a binary stream, or a file path.

    Applies EXIF orientation (transpose) so output PNGs match user expectations.
    If the image is animated (e.g., GIF/WEBP), the first frame is used.
    """
    if isinstance(source, bytes | bytearray):
        buf = io.BytesIO(source)
        img = Image.open(buf)
    elif hasattr(source, "read"):
        # BinaryIO
        img = Image.open(source)  # type: ignore[arg-type]
    else:
        # Path-like
        path = Path(str(source))
        img = Image.open(path)

    # Normalize orientation based on EXIF
    with contextlib.suppress(Exception):
        img = ImageOps.exif_transpose(img)

    # For animated images, use the first frame as a representative PNG
    try:
        if getattr(img, "is_animated", False):
            img.seek(0)
    except Exception:
        pass

    return img


def _convert_mode_for_png(img: Image.Image) -> Image.Image:
    """Convert image mode to a PNG-friendly mode while preserving alpha if present."""
    # If the image has alpha channel, prefer RGBA
    bands = img.getbands()
    has_alpha = "A" in bands or (img.info.get("transparency") is not None)

    if img.mode in ("RGBA", "LA"):
        return img
    if img.mode == "P":
        # Palettized; convert with alpha preservation if present
        return img.convert("RGBA" if has_alpha else "RGB")
    if img.mode in ("CMYK", "YCbCr"):
        return img.convert("RGB")
    if img.mode in ("I", "F"):
        # 32-bit integer/float to 8-bit per channel RGB
        return img.convert("RGB")
    if has_alpha:
        return img.convert("RGBA")
    if img.mode not in ("RGB", "L", "1"):
        # Fallback to RGB for any other exotic modes
        return img.convert("RGB")
    return img


def convert_image_to_png_bytes(source: ImageInput) -> bytes:
    """Convert an input image (bytes, stream, or file path) to PNG bytes.

    - Supports common formats: JPEG/JPG, PNG, WEBP, GIF, BMP, TIFF, etc.
    - Corrects EXIF orientation.
    - For animated formats, uses the first frame.

    Raises:
        UnidentifiedImageError: if the input is not a recognizable image
        OSError: on underlying I/O errors
    """
    try:
        img = _open_image(source)
    except UnidentifiedImageError:
        raise
    except Exception as e:
        # Re-raise I/O errors (e.g., file not found)
        raise e

    img = _convert_mode_for_png(img)

    out = io.BytesIO()
    # Use optimize=True for a reasonable size reduction without being slow
    img.save(out, format="PNG", optimize=True)
    return out.getvalue()


def convert_image_to_png_file(
    input_path: PathLike,
    output_path: PathLike | None = None,
    *,
    overwrite: bool = False,
) -> Path:
    """Convert an image at `input_path` into a PNG file.

    - If `output_path` is not provided, writes alongside the input with a `.png` extension.
    - If `overwrite` is False and the output exists, raises FileExistsError.

    Returns the `Path` to the written PNG file.
    """
    input_path = Path(str(input_path))
    if output_path is None:
        output_path = input_path.with_suffix(".png")
    output_path = Path(str(output_path))

    if output_path.exists() and not overwrite:
        raise FileExistsError(f"Output file already exists: {output_path}")

    png_bytes = convert_image_to_png_bytes(input_path)
    output_path.write_bytes(png_bytes)
    return output_path


__all__ = [
    "convert_image_to_png_bytes",
    "convert_image_to_png_file",
]


def crop_image_bytes(image_bytes: bytes, *, x: float, y: float, width: float, height: float) -> bytes:
    """Crop an image from bytes and return PNG bytes.

    - Clamps coordinates to image bounds.
    - Always returns PNG bytes for consistency.
    - On any processing error, returns original bytes (fail-open behavior).
    """
    try:
        with Image.open(BytesIO(image_bytes)) as img:
            ix, iy, iw, ih = int(x), int(y), int(width), int(height)
            ix = max(0, min(ix, img.width - 1))
            iy = max(0, min(iy, img.height - 1))
            iw = max(1, min(iw, img.width - ix))
            ih = max(1, min(ih, img.height - iy))
            cropped = img.crop((ix, iy, ix + iw, iy + ih))
            out = io.BytesIO()
            cropped.save(out, format="PNG", optimize=True)
            return out.getvalue()
    except Exception:
        return image_bytes
