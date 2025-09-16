# Agent Task Ticket — Order PDF Upload to FTP/FTPS

## Title
feat: Upload order PDFs to FTP/FTPS storage

## Goal
- Ensure every generated customer order PDF is uploaded to the configured FTP server immediately after generation while preserving the existing download experience.
- Centralize and harden the FTP transfer logic so future touchpoints (e.g., email receipts) can reuse the same abstraction.

## Context
- Trigger point: `backend/internal/order/handlers.go:downloadOrderPDFHandler` (invoked via `GET /api/user/orders/:orderId/pdf`).
- PDF generation: `backend/internal/pdf` package, currently returning bytes streamed to the client.
- New abstraction: add `internal/utility` helper for FTP uploads; today the package contains shared helpers used across services.
- Configuration surface: `.env` and `.env.example` (root) currently lack any FTP-related variables.
- Deployment: runtime managed through environment variables; no existing secret management for FTP endpoints.

## Constraints & Non-Goals
- Upload must complete (or fail with surfaced error) before responding `200 OK`; do not silently ignore upload errors.
- Keep handler latency minimal (<2s typical); make FTP logic resilient with timeouts and clear errors.
- Avoid leaking FTP credentials in logs; rely on structured errors without secrets.
- Keep the implementation simple and lean; no over-engineering
- Non-goals: asynchronous retry queues, background sync jobs, or storing PDFs in application storage beyond the FTP transfer.

## Acceptance Criteria (Gherkin)
- Scenario: Successful FTP upload
  - Given a valid order and the FTP server is reachable,
  - When the client downloads `/api/user/orders/:orderId/pdf`,
  - Then the PDF is uploaded to the configured FTP server using the utility helper before the response is written,
  - And the client receives the PDF attachment as today.
- Scenario: Missing FTP configuration
  - Given the FTP env variables are unset or empty,
  - When the handler executes,
  - Then the request fails with `500` and a JSON error indicating FTP configuration is missing, and nothing is uploaded.
- Scenario: FTP upload failure
  - Given the FTP server rejects credentials or the network call times out,
  - When the handler attempts to upload the PDF,
  - Then the request fails with `502` (or similar) and the error is logged without leaking credentials.
- Scenario: Utility unit coverage
  - Given the FTP helper is invoked with mocked dependencies,
  - When tests run,
  - Then success and failure branches are exercised, ensuring reconnection and error propagation paths are covered.

## Inputs & Interfaces
- **Runtime configuration:**
  - `.env` keys (mirrored in `.env.example`):
    - `ORDER_PDF_FTP_SERVER` — hostname (optionally `host:port`).
    - `ORDER_PDF_FTP_USER` — username.
    - `ORDER_PDF_FTP_PASSWORD` — password/secret; load via secret manager in production.
    - Optional: `ORDER_PDF_FTP_TIMEOUT` (seconds) with sensible default (e.g., `10`).
  - Assumption: single active FTP target per environment; extendable via namespaced variables if multiple tenants emerge.
- **Utility function signature:** `utility.UploadPDFToFTP(pdf []byte, server, user, password string) error` (add options struct if timeout required).
- **External deps:** consider using `github.com/jlaffaye/ftp` latest stable version; wrap to allow interface-based mocking in tests.
- **Error handling:** return wrapped errors with context for handler logging.

## Env & Tooling
- Go backend commands: from `backend/`, run `go test ./...`, `go test ./internal/order -run TestDownloadOrderPDFHandler*`, and `make check`.
- Environment variable management: ensure `.env` loading (likely via `github.com/joho/godotenv` in dev) picks up new keys.
- Feature flags: none planned; behaviour enabled when env vars present.
- External access: FTP server reachable over standard FTP/FTPS port; tests must mock network (no live server hits).

## Deliverables
- New utility in `backend/internal/utility/ftp.go` (or similar) implementing the upload helper with tests using a mock FTP client.
- Updated `downloadOrderPDFHandler` to load env config (prefer injecting via config struct) and invoke the utility; ensure error propagation.
- Configuration loader updates (where `.env` vars are parsed) to include new FTP settings, plus `.env.example` entries and docs.
- Unit tests:
  - Utility tests covering success, connection/auth failure, and write failure using fakes.
  - Handler tests in `backend/internal/order/handlers_test.go` verifying upload invocation, error handling, and response writing.
- Requirement doc (`requirements/REQ-order-pdf-ftp-upload.md`) linked in future PR.

## Run Commands
```bash
cd backend
go test ./internal/utility ./internal/order
make check
```

## Notes & Assumptions
- Treat "FTP" as supporting both plain FTP and FTPS (explicit TLS); prefer FTPS in production but confirm if any legacy endpoints require plain FTP.
- Handler currently streams PDF bytes synchronously; adding the upload must not disrupt the download stream order (upload before writing response headers/body).

## Out of Scope Follow-ups (optional)
- Add background retry job for failed uploads.
- Centralize outbound file transfer configuration (connection pools, secrets rotation).
- Store upload audit metadata (timestamps, remote paths) in the database for compliance reporting.
