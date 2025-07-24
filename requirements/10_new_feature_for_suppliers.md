# New feature for ValueAddedTax (VAT)
Implement a new feature `Supplier` for managing suppliers. Implement the necessary files in the backend and the frontend.

## Requirements Backend
- New Entity with all relevant code (Entity, Controller, DTOs, etc.)
- The API should live in the admin package
- Fields for the entity:
  - name (string, nullable)
  - title (string, nullable)
  - firstName (string, nullable)
  - lastName (string, nullable)
  - street (string, nullable)
  - houseNumber (string, nullable)
  - city (string, nullable)
  - postalCode (int, nullable)
  - country (string, nullable)
  - phoneNumber1 (string, nullable)
  - phoneNumber2 (string, nullable)
  - phoneNumber3 (string, nullable)
  - email (string, nullable)
  - website (string, nullable)

## Requirements Frontend
- The page to list all suppliers already exists: @frontend/src/pages/admin/Suppliers.tsx
  - Add missing logic to the page and remove unnecessary code
- Add a page to edit existing or create new supplier entries
- Follow the design principles of existing NewOrEdit* pages

## Remarks
Think hard to create the best possible architecture and plan.