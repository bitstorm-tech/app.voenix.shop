package auth

import (
    "net/http"

    "github.com/gin-gonic/gin"
    "gorm.io/gorm"
)

// RequireRoles ensures the current session user has any of the allowed roles.
func RequireRoles(db *gorm.DB, allowed ...string) gin.HandlerFunc {
    allowedSet := map[string]struct{}{}
    for _, r := range allowed {
        allowedSet[r] = struct{}{}
    }
    return func(c *gin.Context) {
        sid, err := c.Cookie("session_id")
        if err != nil || sid == "" {
            c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
            return
        }
        u, err := GetUserFromSession(db, sid)
        if err != nil || u == nil || !u.IsActive() {
            c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
            return
        }
        names := RoleNames(u)
        ok := false
        for _, n := range names {
            if _, exists := allowedSet[n]; exists {
                ok = true
                break
            }
        }
        if !ok {
            c.AbortWithStatusJSON(http.StatusForbidden, gin.H{"detail": "Forbidden: insufficient role"})
            return
        }
        // Attach user to context for downstream handlers if needed
        c.Set("currentUser", u)
        c.Next()
    }
}

// RequireAdmin convenience wrapper.
func RequireAdmin(db *gorm.DB) gin.HandlerFunc {
    return RequireRoles(db, "ADMIN")
}

