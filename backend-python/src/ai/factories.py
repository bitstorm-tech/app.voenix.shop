from __future__ import annotations

from enum import Enum
from typing import Any

from ._internal.flux_generator import FluxImageGenerator
from ._internal.gemini_generator import GeminiImageGenerator
from ._internal.openai_generator import OpenAIImageGenerator
from .interfaces import AIImageGenerator

"""
Factory for creating AI image generators.

Supports:
- Gemini (implemented)
- Flux (stub)
- OpenAI (stub)
"""


class AIImageProvider(str, Enum):
    GEMINI = "gemini"
    FLUX = "flux"
    OPENAI = "openai"


class AIImageGeneratorFactory:
    """Factory to construct concrete `AiImageGenerator` implementations."""

    @staticmethod
    def create(provider: str | AIImageProvider, **kwargs: Any) -> AIImageGenerator:
        key = (provider.value if isinstance(provider, AIImageProvider) else str(provider)).lower()
        if key == AIImageProvider.GEMINI.value:
            return GeminiImageGenerator(**kwargs)
        if key == AIImageProvider.FLUX.value:
            return FluxImageGenerator(**kwargs)
        if key == AIImageProvider.OPENAI.value:
            return OpenAIImageGenerator(**kwargs)
        raise ValueError(f"Unknown AI image provider: {provider}")


__all__ = [
    "AIImageProvider",
    "AIImageGeneratorFactory",
]
