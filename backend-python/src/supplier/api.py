from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.orm import Session

from src.auth.api import require_admin
from src.database import get_db

from ._internal.entities import Supplier
from .schemas import SupplierCreate, SupplierRead, SupplierUpdate

router = APIRouter(
    prefix="/api/admin/suppliers",
    tags=["suppliers"],
    dependencies=[Depends(require_admin)],
)


@router.get("/", response_model=list[SupplierRead], response_model_by_alias=True)
def get_suppliers(db: Session = Depends(get_db)):
    """List all suppliers with full entity fields.

    Returns: list[SupplierRead] – Complete supplier rows ordered by database default.
    Status: 200 OK
    """
    result = db.execute(select(Supplier))
    return result.scalars().all()


@router.get("/{id}", response_model=SupplierRead, response_model_by_alias=True)
def get_supplier(id: int, db: Session = Depends(get_db)):
    """Fetch a supplier by ID.

    Path params:
    - id: int – Supplier identifier

    Returns: Supplier – Full supplier entity
    Errors: 404 if not found
    Status: 200 OK | 404 Not Found
    """
    result = db.execute(select(Supplier).where(Supplier.id == id))
    supplier = result.scalar_one_or_none()
    if not supplier:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Supplier not found")
    return supplier


@router.post("/", response_model=SupplierRead, status_code=status.HTTP_201_CREATED)
def create_supplier(payload: SupplierCreate, db: Session = Depends(get_db)):
    """Create a new supplier.

    Body: SupplierCreate – All fields optional; validated per length/format rules
    Returns: SupplierRead – Newly created row
    Status: 201 Created
    """
    supplier = Supplier(**payload.model_dump(mode="json"))
    db.add(supplier)
    db.commit()
    db.refresh(supplier)
    return supplier


@router.put("/{id}", response_model=SupplierRead)
def update_supplier(id: int, payload: SupplierUpdate, db: Session = Depends(get_db)):
    """Update an existing supplier by ID.

    Path params:
    - id: int – Supplier identifier

    Body: SupplierUpdate – Validated fields to overwrite existing values
    Returns: SupplierRead – Updated row
    Errors: 404 if not found
    Status: 200 OK | 404 Not Found
    """
    result = db.execute(select(Supplier).where(Supplier.id == id))
    supplier = result.scalar_one_or_none()
    if not supplier:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Supplier not found")

    # Apply updates via schema helper to keep entity thin and logic centralized
    payload.apply(supplier)

    db.add(supplier)
    db.commit()
    db.refresh(supplier)
    return supplier


@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_supplier(id: int, db: Session = Depends(get_db)):
    """Delete a supplier by ID.

    Path params:
    - id: int – Supplier identifier

    Returns: None
    Errors: 404 if not found
    Status: 204 No Content | 404 Not Found
    """
    result = db.execute(select(Supplier).where(Supplier.id == id))
    supplier = result.scalar_one_or_none()
    if not supplier:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Supplier not found")
    db.delete(supplier)
    db.commit()
