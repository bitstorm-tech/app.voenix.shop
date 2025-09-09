from __future__ import annotations

import os
from pathlib import Path

from dotenv import load_dotenv

# Ensure .env is loaded so STORAGE_ROOT can be read from env
load_dotenv()


class StorageLocations:
    """Centralized storage location resolver.

    Reads `STORAGE_ROOT` from environment (or .env) and exposes well-known
    storage directories used by the application. New locations should be added
    here to keep path logic in one place.
    """

    def __init__(self, storage_root: str | os.PathLike[str] | None = None) -> None:
        root = storage_root or os.getenv("STORAGE_ROOT")
        if not root:
            raise RuntimeError("STORAGE_ROOT is not configured. Set it in the environment or .env file.")
        self.root = Path(str(root)).expanduser()

    @property
    def PROMPT_TEST(self) -> Path:
        """Location for prompt test images.

        {storage.root}/private/images/prompt-test
        """
        return self.root / "private" / "images" / "0_prompt-test"

    @property
    def PROMPT_EXAMPLE(self) -> Path:
        """Location for prompt example images.

        {storage.root}/public/images/prompt-example-images
        """
        return self.root / "public" / "images" / "prompt-example-images"

    @property
    def PROMPT_SLOT_VARIANT_EXAMPLE(self) -> Path:
        """Location for prompt slot variant example images.

        {storage.root}/public/images/prompt-slot-variant-example-images
        """
        return self.root / "public" / "images" / "prompt-slot-variant-example-images"

    @property
    def MUG_VARIANT_EXAMPLE(self) -> Path:
        """Location for mug variant example images.

        {storage.root}/public/images/articles/mugs/variant-example-images
        """
        return self.root / "public" / "images" / "articles" / "mugs" / "variant-example-images"

    @property
    def SHIRT_VARIANT_EXAMPLE(self) -> Path:
        """Location for shirt variant example images.

        {storage.root}/public/images/articles/shirts/variant-example-images
        """
        return self.root / "public" / "images" / "articles" / "shirts" / "variant-example-images"

    @property
    def PUBLIC_IMAGES(self) -> Path:
        """Base directory for public images.

        {storage.root}/public/images
        """
        return self.root / "public" / "images"

    @property
    def PRIVATE_IMAGES(self) -> Path:
        """Base directory for private images.

        {storage.root}/private/images
        """
        return self.root / "private" / "images"

    def resolve_admin_dir(self, image_type: str) -> Path:
        """Resolve admin-managed image type to a directory.

        Raises ValueError if the image_type is unsupported.
        """
        mapping = {
            "PROMPT_EXAMPLE": self.PROMPT_EXAMPLE,
            "PROMPT_SLOT_VARIANT_EXAMPLE": self.PROMPT_SLOT_VARIANT_EXAMPLE,
            "MUG_VARIANT_EXAMPLE": self.MUG_VARIANT_EXAMPLE,
            "SHIRT_VARIANT_EXAMPLE": self.SHIRT_VARIANT_EXAMPLE,
            "PROMPT_TEST": self.PROMPT_TEST,
            "PUBLIC": self.PUBLIC_IMAGES,
            "PRIVATE": self.PRIVATE_IMAGES,
        }
        try:
            return mapping[image_type.upper()]
        except KeyError as exc:
            raise ValueError(f"Unsupported imageType: {image_type}") from exc


__all__ = [
    "StorageLocations",
]
