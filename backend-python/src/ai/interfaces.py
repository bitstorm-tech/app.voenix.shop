"""
Interfaces for AI image generation/editing.

Defines a protocol to standardize how an image generation/editing backend
should be invoked from the rest of the application.
"""

from __future__ import annotations

from pathlib import Path
from typing import Protocol, runtime_checkable


@runtime_checkable
class AIImageGenerator(Protocol):
    """Protocol for AI image generators capable of editing/manipulating images.

    Implementations should accept an input image and an instruction prompt and
    return one or more edited images as raw bytes.
    """

    def edit(
        self,
        image: bytes | str | Path,
        prompt: str,
        *,
        candidate_count: int = 1,
        mime_type: str | None = None,
        max_output_tokens: int | None = 8192,
        temperature: float | None = 0.7,
        timeout: float = 60.0,
    ) -> list[bytes]:
        """Edit/manipulate an input image according to `prompt`.

        Returns a list of generated image bytes (e.g., PNG/JPEG).
        Implementations may ignore unsupported parameters.
        """
        ...


__all__ = [
    "AIImageGenerator",
]
