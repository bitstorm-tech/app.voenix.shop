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


__all__ = [
    "StorageLocations",
]
