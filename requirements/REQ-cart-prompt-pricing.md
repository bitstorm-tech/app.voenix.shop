# Agent Task Ticket — Cart Prompt Pricing and Correct Totals (Go backend)

## Title
feat: Show prompt prices and correct totals on Cart page

## Goal
- Cart page clearly displays per-item prompt prices when a prompt is attached to a cart item and reflects accurate totals across line items, subtotal, and the checkout button amount.
- Prevent price surprises by ensuring the UI totals match backend-calculated totals in cents, rendered in USD with two decimals.

## Context
- User flow: Editor → prompt-selection → add to cart → Cart (`/cart`) → Checkout.
- Target UI: `frontend/src/pages/Cart.tsx` (current implementation shows item price from `priceAtTime`, subtotal per item, and order summary from `cartData.totalPrice`).
- Types (current):
  - FE: `frontend/src/types/cart.ts` (`CartDto`, `CartItemDto`) — contains `promptId` but no explicit prompt price fields.
  - FE: `frontend/src/types/prompt.ts` — `Prompt.price?: number` (cents) for public display.
  - Go backend DTOs: `backend-go/internal/cart/api_types.go` (`CartDto`, `CartItemDto`).
  - Go backend models: `backend-go/internal/cart/models.go` (`Cart`, `CartItem`).
  - Go prompt models: `backend-go/internal/prompt/entities.go` (`Prompt` with `PriceID` -> `prices` row).
- Go backend pricing today:
  - `CartItem.PriceAtTime` is set from article cost calculation: `currentGrossPrice(db, articleID)` in `backend-go/internal/cart/store.go`.
  - Totals are computed in `backend-go/internal/cart/mapper.go` as `sum(item.PriceAtTime * item.Quantity)` and ignore any prompt price.
  - Prompts can have a price via `prompts.price_id` -> `prices` (`SalesTotalGross`) per migration `000002_prompts_price_fk.up.sql`, but cart logic does not capture or total it.

## Constraints & Non-Goals
- Currency: USD only; format with `$` and two decimals; compute in cents to avoid rounding errors.
- Performance: No N+1 calls per item from the browser; avoid fetching prompt details per line item.
- Accessibility: Price breakdown must be readable by screen readers; use clear labels.
- Backwards compatibility: Existing carts without prompt pricing data must still calculate correctly (prompt price treated as 0).
- Non-goals:
  - Shipping, taxes, coupons/discounts, and multi-currency are out of scope.
  - Changing checkout flow or payment providers is out of scope.
  - Styling overhaul beyond what’s necessary to display prices is out of scope.

## Acceptance Criteria (Gherkin)
- Scenario: Item with prompt price displayed
  - Given a cart item with `promptId` and a non-zero prompt price at time of adding,
  - When I open the Cart page,
  - Then the item row shows the base article price and a separate “Prompt price” line,
  - And the item-level subtotal equals `(article price + prompt price) × quantity`,
  - And the order summary subtotal and the checkout button amount equal the sum of all item subtotals.

- Scenario: Item without prompt
  - Given a cart item with no `promptId`,
  - When I open the Cart page,
  - Then only the base article price is shown,
  - And subtotals and totals exclude any prompt price.

- Scenario: Multiple quantities
  - Given an item with quantity `Q > 1`,
  - When I open the Cart page,
  - Then the displayed subtotal equals `Q × (article price + prompt price)` with correct 2-decimal USD formatting.

- Scenario: Mixed cart
  - Given a cart with items with and without prompts,
  - When I open the Cart page,
  - Then each item shows the correct breakdown, and the order summary equals the sum of item subtotals.

- Scenario: Price changed indicator
  - Given `hasPriceChanged = true` for an item (article and/or prompt component),
  - When I open the Cart page,
  - Then the UI shows a “was $X.XX” indicator for any component whose recorded price differs from its current price (if provided),
  - And the totals still use the recorded “at time” prices in cents from the cart.

- Edge cases
  - If a prompt is inactive or missing, its line still renders using recorded prices; if no recorded prompt price exists, render “Prompt price: $0.00” and do not error.
  - If a prompt’s display data fails to load, totals remain correct and the UI still shows a generic “Prompt” label without breaking layout.

## Inputs & Interfaces
- Request/Response shapes (Go)
  - Extend `backend-go/internal/cart/api_types.go` `CartItemDto` to include a breakdown:
    - `ArticlePriceAtTime int    json:"articlePriceAtTime"` (cents; maps to existing `PriceAtTime` for backward compatibility if needed)
    - `PromptPriceAtTime  int    json:"promptPriceAtTime"` (cents; default 0)
    - `ArticleOriginalPrice int  json:"articleOriginalPrice"` (cents; maps to existing `OriginalPrice`)
    - `PromptOriginalPrice  int  json:"promptOriginalPrice"` (cents; default 0)
    - Keep `HasPriceChanged bool` for aggregate flag; optionally add `HasPromptPriceChanged bool json:"hasPromptPriceChanged"`.
    - Optionally include `PromptTitle *string json:"promptTitle"` to display a label without FE lookups.
  - Define `CartItemDto.TotalPrice = (ArticlePriceAtTime + PromptPriceAtTime) * Quantity`.
  - Keep `CartDto.TotalPrice = sum(item.TotalPrice)`.

- DB schema deltas (Go migrations)
  - Add a migration pair under `backend-go/internal/database/migrations/` (e.g., `000003_cart_prompt_prices.up.sql`/`.down.sql`) that:
    - Alters `cart_items` to add:
      - `prompt_price_at_time integer not null default 0`
      - `prompt_original_price integer not null default 0`
    - Works on both SQLite and Postgres (plain `integer` with defaults is portable; use `if exists` guards as in prior migrations).

- Services/Assemblers (Go)
  - `backend-go/internal/cart/models.go`: add GORM fields on `CartItem` with tags:
    - `PromptPriceAtTime int    gorm:"column:prompt_price_at_time;not null;default:0"`
    - `PromptOriginalPrice int  gorm:"column:prompt_original_price;not null;default:0"`
  - `backend-go/internal/cart/api_types.go`: add the new DTO fields listed above.
  - `backend-go/internal/cart/mapper.go`:
    - Populate DTO fields from model.
    - Compute `TotalPrice` as `(PriceAtTime + PromptPriceAtTime) * Quantity` or switch to using `ArticlePriceAtTime` naming for clarity in the DTO and keep model’s existing fields.
    - Set `HasPriceChanged = (PriceAtTime != OriginalPrice) || (PromptPriceAtTime != PromptOriginalPrice)`.
  - `backend-go/internal/cart/handlers_items.go` (`addItemHandler`):
    - When `req.PromptID != nil`, fetch current prompt gross price via helper `promptCurrentGrossPrice(db, *req.PromptID)`; set both `PromptPriceAtTime` and `PromptOriginalPrice` to that value (else 0).
    - Keep `PriceAtTime`/`OriginalPrice` for the article component as today.
  - `backend-go/internal/cart/handlers_cart.go`:
    - In `getCartSummaryHandler`, compute `total += (it.PriceAtTime + it.PromptPriceAtTime) * it.Quantity`.
    - In `refreshPricesHandler`, update `OriginalPrice` from current article price (as today) and also update `PromptOriginalPrice` from the current prompt price if `PromptID != nil`.
  - `backend-go/internal/cart/store.go` or `service.go`:
    - Add `promptCurrentGrossPrice(db *gorm.DB, promptID int) (int, error)` that returns `prompts.price_id -> prices.sales_total_gross` or 0 if missing; implement with simple join or `Preload("Price")`.

## Env & Tooling
- Frontend: React + TypeScript (Vite). Commands: `npm run type-check`, `npm run build`, `npm run format`, `npm run lint`.
- Go backend: Gin + GORM. Commands:
  - From `backend-go/`: `go build ./...`, `go test ./...`.
  - Run: `go run ./cmd/server` (migrations auto-run via `database.DoMigrations()`).
  - Migrations live in `backend-go/internal/database/migrations` and are applied on startup or via `go run ./cmd/db` if provided.
- Feature flags: none required; change is safe to roll out universally.
- External deps/mocks: none.

## Deliverables
- Go backend
  - Migration adding prompt pricing columns to `cart_items` (up/down SQL).
  - Updated GORM models, handlers, and mapper to record, expose, and total prompt prices.
  - Updated API DTOs with breakdown fields and optional `promptTitle`.
  - Unit tests in Go covering totals and DTO mapping (mixed carts, quantities, missing prompts, price changes).

- Frontend
  - Update `frontend/src/pages/Cart.tsx` to render:
    - Base article price line.
    - Prompt price line when `promptPriceAtTime > 0` or `promptId` present.
    - Per-item subtotal: `(article + prompt) × quantity` using cents → dollars conversion.
    - Order summary totals sourced from `CartDto.totalPrice` (no client recomputation for the summary).
  - Optional: show `promptTitle` if provided.
  - Light Testing Library checks for rendering and arithmetic.

- Quality gates
  - Go backend: `go build ./...` and `go test ./...` pass; no lint errors if `golangci-lint` configured.
  - Frontend: `npm run type-check` and `npm run build` pass; `npm run format` executed; lint clean.

## Run Commands
```bash
# Go backend
cd backend-go
go build ./...
go test ./...
go run ./cmd/server

# Frontend
cd ../frontend
npm run type-check
npm run build
npm run lint
npm run format
```

## Notes & Assumptions
- Prompt prices are independent of article prices and must be captured at the time of adding to cart to ensure pricing stability even if catalog prices change later.
- If BE cannot deliver `promptTitle` in the DTO, FE will display a generic label (“Prompt”) without additional fetches to avoid performance regressions.
- All arithmetic uses integer cents; UI renders via `(cents / 100).toFixed(2)`.

## Out of Scope Follow-ups (optional)
- Tax and shipping estimation on Cart page.
- Multi-currency support and localized currency formatting.
- Coupons/discounts and promotional price messaging.
