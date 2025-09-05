"""
Stub implementation for a Flux-based image generator.

This class satisfies the `AiImageGenerator` protocol but is not yet
implemented. The `edit` method raises NotImplementedError.
"""

from __future__ import annotations

from pathlib import Path

from src.ai.interfaces import AIImageGenerator


class FluxImageGenerator(AIImageGenerator):
    def __init__(
        self,
        *,
        api_key: str | None = None,
        model: str | None = None,
        default_candidate_count: int = 1,
        default_max_output_tokens: int | None = 8192,
        default_temperature: float | None = 0.7,
        default_timeout: float = 60.0,
    ) -> None:
        self._api_key = api_key
        self._model = model
        self._default_candidate_count = int(default_candidate_count)
        self._default_max_output_tokens = default_max_output_tokens
        self._default_temperature = default_temperature
        self._default_timeout = float(default_timeout)

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
        raise NotImplementedError("FluxImageGenerator is not implemented yet")


__all__ = ["FluxImageGenerator"]
