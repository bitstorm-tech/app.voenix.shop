from contextlib import asynccontextmanager
from typing import AsyncIterator

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from .database import Base, engine
from .vat import vat_api


@asynccontextmanager
async def lifespan(_: FastAPI) -> AsyncIterator[None]:
    # Create tables on startup for simple setups. For production, use migrations.
    Base.metadata.create_all(bind=engine)
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


@app.get("/health")
def health():
    return {"status": "ok!!!"}


app.include_router(vat_api.router)
