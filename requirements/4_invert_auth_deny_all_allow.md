# Invert authentication logic (deny all, allow explicitly)

## Requirements
- The security configuration must be improved in @backend/src/main/kotlin/com/jotoai/voenix/shop/auth/config/SecurityConfig.kt
- All endpoints must be secured
- Explicitly allow endpoints that don't need authentication

Think hard to create the best possible architecture and plan.