from collections.abc import AsyncIterator
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from sqlmodel import SQLModel

from .ai import api as ai_api
from .auth import api as auth_api
from .country import api as country_api
from .database import engine
from .image import StorageLocations
from .image import api as image_api
from .prompt import api as prompt_router
from .supplier import api as supplier_api
from .vat import api as vat_api


@asynccontextmanager
async def lifespan(_: FastAPI) -> AsyncIterator[None]:
    # Ensure all SQLModel tables exist (including auth sessions)
    # Models are imported via routers above, so metadata is populated.
    SQLModel.metadata.create_all(engine)
    yield


app = FastAPI(title="Cockaigne Backend", version="0.1.0", lifespan=lifespan)

# Adjust origins as needed for your RN app/dev host
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "*",
        "http://localhost",
        "http://localhost:3000",
        "http://localhost:5173",
        "http://127.0.0.1",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


app.include_router(vat_api.router)
app.include_router(supplier_api.router)
app.include_router(country_api.router)
app.include_router(ai_api.router)
app.include_router(auth_api.router)
app.include_router(prompt_router.router)
app.include_router(image_api.router)

# Serve public assets directly from STORAGE_ROOT/public under /public/*
public_dir = StorageLocations().root / "public"
app.mount("/public", StaticFiles(directory=str(public_dir)), name="public")
