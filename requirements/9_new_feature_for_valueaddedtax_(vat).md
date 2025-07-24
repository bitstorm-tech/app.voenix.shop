# New feature for ValueAddedTax (VAT)
Implement a new feature for Value Added TAX (VAT). Implement the necessary files in the backend and the frontend.

## Requirements Backend
- New Entity with all relevant code (Entity, Controller, DTOs, etc.)
- The API should live in the admin package
- Fields for the entity:
  - name (string, not null)
  - percent (integer, not null)
  - description (text, nullable) 

## Requirements Frontend
- Add a page to list all available VAT entries
- Add a page to edit existing or create new VAT entries
- New AdminSidebar entry under Masterdata
- Follow the design principles of existing pages like @frontend/src/pages/admin/Prompts.tsx

Think hard to create the best possible architecture and plan.