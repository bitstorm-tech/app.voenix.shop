# Backend: Refactor Mug Categories to Article Categories
Because we unite every article (e.g., Mugs, T-Shirts, Pillows, etc.) under Articles. We need to refactor the package `mugs` to an article package with the name `articles`.

## Requirements
- Refactor the API package @backend/src/main/kotlin/com/jotoai/voenix/shop/api/admin/mugs
- Refactor the domain package @backend/src/main/kotlin/com/jotoai/voenix/shop/domain/mugs
  - Add an intermediate package called `articles`
  - Move the package `mugs` inside the new package `articles`
- Think about all other related code that must be refactored

Think hard to create the best possible architecture and plan.
