"""
Gemini 2.5 Flash Image client (REST).

This module provides a small, dependency‑light helper to send an input image
to Google Gemini 2.5 Flash Image and get edited/manipulated images back.

Request/response structure follows the official Gemini API (v1beta) generateContent
endpoint for the image model. The model and API key are configurable via env vars.

Environment variables:
- GOOGLE_API_KEY: API key for Google AI Studio / Gemini API
- GEMINI_IMAGE_MODEL: Optional. Defaults to "gemini-2.5-flash-image-preview"

Typical usage:

    from aigen.gemini_generator import edit_image_with_gemini

    with open("/path/to/image.png", "rb") as f:
        img_bytes = f.read()

    outputs = edit_image_with_gemini(
        image=img_bytes,
        prompt="Replace the sky with a vibrant sunset.",
        candidate_count=1,
    )

    # outputs is a list[bytes] of image data (PNG/JPEG depending on the model)
    with open("edited.png", "wb") as out:
        out.write(outputs[0])

Notes:
- This function sends the provided image inline (base64) in the request body.
- For larger inputs, consider the Gemini File API and use fileData parts.
- Returned images are extracted from candidates[].content.parts[].inlineData.
"""

from __future__ import annotations

import base64
import mimetypes
import os
from pathlib import Path

import httpx
from dotenv import load_dotenv

from src.ai.interfaces import AIImageGenerator

# Load environment variables from a .env if present
load_dotenv()


_DEFAULT_MODEL = os.getenv("GEMINI_IMAGE_MODEL", "gemini-2.5-flash-image-preview")
_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"


def _guess_mime_type(filename: str | None) -> str:
    """Guess a MIME type from a filename; default to image/png if unknown."""
    if not filename:
        return "image/png"
    mime, _ = mimetypes.guess_type(filename)
    if not mime:
        # Fallback to a sensible default for image editing
        return "image/png"
    return mime


def _ensure_bytes_and_mime(
    image: bytes | str | Path,
    explicit_mime: str | None = None,
) -> tuple[bytes, str]:
    """Normalize input image to (bytes, mime_type).

    - If `image` is bytes, keep as-is and use `explicit_mime` or default.
    - If `image` is a path, read bytes and infer MIME from extension.
    """
    if isinstance(image, str | Path):
        path = Path(image)
        data = path.read_bytes()
        mime = explicit_mime or _guess_mime_type(path.name)
        return data, mime
    elif isinstance(image, bytes | bytearray):
        data = bytes(image)
        mime = explicit_mime or "image/png"
        return data, mime
    raise TypeError("image must be bytes, str, or Path")


def _build_request_body(
    prompt: str,
    image_bytes: bytes,
    mime_type: str,
    candidate_count: int,
    max_output_tokens: int | None = 8192,
    temperature: float | None = 0.7,
) -> dict:
    """Construct the JSON payload matching Gemini generateContent schema."""
    encoded = base64.b64encode(image_bytes).decode("ascii")
    body: dict = {
        "contents": [
            {
                # role optional; omitting matches Kotlin implementation
                "parts": [
                    {"text": prompt},
                    {"inlineData": {"mimeType": mime_type, "data": encoded}},
                ]
            }
        ],
        "generationConfig": {
            "candidateCount": int(candidate_count),
        },
    }

    # Only include optional params if provided
    if max_output_tokens is not None:
        body["generationConfig"]["maxOutputTokens"] = int(max_output_tokens)
    if temperature is not None:
        body["generationConfig"]["temperature"] = float(temperature)

    return body


def _extract_images_from_response(resp_json: dict) -> list[bytes]:
    """Parse generateContent response and extract all inline image bytes.

    Expected structure (simplified):
      {
        "candidates": [
          {
            "content": {
              "parts": [
                {"text": ...} | {"inlineData": {"mimeType": "image/...", "data": "..."}} | ...
              ]
            }
          }, ...
        ],
        "error": { ... }?
      }
    """
    # Surface API error if present
    if "error" in resp_json and resp_json["error"]:
        err = resp_json["error"]
        message = err.get("message") or str(err)
        code = err.get("code")
        status = err.get("status")
        raise RuntimeError(f"Gemini API error: code={code} status={status} message={message}")

    candidates = resp_json.get("candidates") or []
    images: list[bytes] = []

    for cand in candidates:
        content = cand.get("content") or {}
        parts = content.get("parts") or []
        for part in parts:
            inline = part.get("inlineData")
            if not inline:
                continue
            mime = inline.get("mimeType") or ""
            data_b64 = inline.get("data")
            if not data_b64:
                continue
            # Only collect images
            if not mime.startswith("image/"):
                continue
            try:
                images.append(base64.b64decode(data_b64))
            except Exception as e:  # pragma: no cover - defensive
                raise ValueError(f"Failed to decode image data from response: {e}")

    if not images:
        raise ValueError("Gemini response contained no image inlineData")

    return images


def _post_json(url: str, params: dict, json_body: dict, timeout: float = 60.0) -> dict:
    """POST JSON using httpx if available, otherwise urllib.request.

    Returns parsed JSON dict or raises an exception.
    """

    with httpx.Client(timeout=timeout) as client:
        resp = client.post(
            url,
            params=params,
            json=json_body,
            headers={"Content-Type": "application/json"},
        )
        # Raise for HTTP errors
        resp.raise_for_status()
        return resp.json()


def _edit_image_with_gemini(
    image: bytes | str | Path,
    prompt: str,
    *,
    api_key: str | None = None,
    model: str | None = None,
    candidate_count: int = 1,
    mime_type: str | None = None,
    max_output_tokens: int | None = 8192,
    temperature: float | None = 0.7,
    timeout: float = 60.0,
) -> list[bytes]:
    """Send an image + instruction prompt to Gemini 2.5 Flash Image and return edited images.

    Parameters:
    - image: bytes or path to the source image to edit/manipulate
    - prompt: instruction describing the edit (e.g., "remove background")
    - api_key: Google API key; defaults to env GOOGLE_API_KEY
    - model: model ID; defaults to env GEMINI_IMAGE_MODEL or gemini-2.5-flash-image-preview
    - candidate_count: number of images to return (1..N)
    - mime_type: optional MIME for the input image (auto-guessed for file paths)
    - max_output_tokens: optional; forwarded to generationConfig
    - temperature: optional; forwarded to generationConfig
    - timeout: HTTP request timeout in seconds

    Returns:
    - List of image bytes (each an edited image)
    """
    key = api_key or os.getenv("GOOGLE_API_KEY", "").strip()
    if not key:
        raise RuntimeError("GOOGLE_API_KEY is not configured")

    model_id = (model or _DEFAULT_MODEL).strip()
    if not model_id:
        raise RuntimeError("Model is not configured")

    # Prepare image bytes and mime
    img_bytes, inferred_mime = _ensure_bytes_and_mime(image, explicit_mime=mime_type)

    # Construct request
    url = f"{_BASE_URL}/{model_id}:generateContent"
    params = {"key": key}
    body = _build_request_body(
        prompt=prompt,
        image_bytes=img_bytes,
        mime_type=inferred_mime,
        candidate_count=candidate_count,
        max_output_tokens=max_output_tokens,
        temperature=temperature,
    )

    # Send request and parse response
    resp_json = _post_json(url, params=params, json_body=body, timeout=timeout)
    return _extract_images_from_response(resp_json)


class GeminiImageGenerator(AIImageGenerator):
    """AiImageGenerator implementation backed by Google Gemini Image model.

    Wraps `edit_image_with_gemini` and allows configuring API key, model, and
    default generation parameters during construction.
    """

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
        self._api_key = (api_key or os.getenv("GOOGLE_API_KEY", "")).strip() or None
        self._model = (model or _DEFAULT_MODEL).strip() or None
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
        # Use instance defaults when caller leaves parameters as default values
        return _edit_image_with_gemini(
            image=image,
            prompt=prompt,
            api_key=self._api_key,
            model=self._model,
            candidate_count=candidate_count or self._default_candidate_count,
            mime_type=mime_type,
            max_output_tokens=(max_output_tokens if max_output_tokens is not None else self._default_max_output_tokens),
            temperature=temperature if temperature is not None else self._default_temperature,
            timeout=timeout if timeout is not None else self._default_timeout,
        )


__all__ = [
    "GeminiImageGenerator",
]
