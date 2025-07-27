# Persist Price Calculation to the Database
All the fields of the Price Calculation tab @frontend/src/pages/admin/articles/components/PriceCalculationTab.tsx have to be stored in the database.

## Requirements Backend
- B1: Create a new table article_price_calculation with that includes all fields from the PriceCalculationTab.
- B2: Article and article_price_calculation have a one-to-one relation.
- B3: All the relevant code (Entity, Controller, Services, etc.) is implemented.

## Requirements Frontend
- F1: When saving an article (new or edited), the price calculation is sent to the database to persist it.
- F2: All the relevant code to save or update price calculations is implemented or updated.

## Remarks
Ultrathink to create the best possible architecture and plan.