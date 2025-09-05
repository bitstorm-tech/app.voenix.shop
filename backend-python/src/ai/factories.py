from __future__ import annotations

from enum import Enum
from typing import Any

from ._internal.flux_generator import FluxImageGenerator
from ._internal.gemini_generator import GeminiImageGenerator
from ._internal.gpt_generator import GptImageGenerator
from .interfaces import AIImageGenerator

"""
Factory for creating AI image generators.

Supports:
- Gemini (implemented)
- Flux (stub)
- GPT (stub)
"""


class AIImageProvider(str, Enum):
    GEMINI = "gemini"
    FLUX = "flux"
    GPT = "gpt"


class AIImageGeneratorFactory:
    """Factory to construct concrete `AiImageGenerator` implementations."""

    @staticmethod
    def create(provider: str | AIImageProvider, **kwargs: Any) -> AIImageGenerator:
        key = (provider.value if isinstance(provider, AIImageProvider) else str(provider)).lower()
        if key == AIImageProvider.GEMINI.value:
            return GeminiImageGenerator(**kwargs)
        if key == AIImageProvider.FLUX.value:
            return FluxImageGenerator(**kwargs)
        if key == AIImageProvider.GPT.value:
            return GptImageGenerator(**kwargs)
        raise ValueError(f"Unknown AI image provider: {provider}")


__all__ = [
    "AIImageProvider",
    "AIImageGeneratorFactory",
]
