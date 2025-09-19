package auth

import (
	"fmt"
	"net/http"
	"os"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
)

type userPublic struct {
	ID          int      `json:"id"`
	Email       string   `json:"email"`
	FirstName   *string  `json:"firstName,omitempty"`
	LastName    *string  `json:"lastName,omitempty"`
	PhoneNumber *string  `json:"phoneNumber,omitempty"`
	Roles       []string `json:"roles"`
}

type loginResponse struct {
	User      userPublic `json:"user"`
	SessionID string     `json:"sessionId"`
	Roles     []string   `json:"roles"`
}

type sessionInfo struct {
	Authenticated bool       `json:"authenticated"`
	User          userPublic `json:"user"`
	Roles         []string   `json:"roles"`
}

func toPublic(u *User) userPublic {
	roles := RoleNames(u)
	return userPublic{
		ID:          u.ID,
		Email:       u.Email,
		FirstName:   u.FirstName,
		LastName:    u.LastName,
		PhoneNumber: u.PhoneNumber,
		Roles:       roles,
	}
}

// SessionTTL returns TTL seconds for sessions; default 7 days.
func sessionTTL() time.Duration {
	const def = 7 * 24 * time.Hour
	if v := os.Getenv("SESSION_TTL_SECONDS"); v != "" {
		// simple parse
		var n int64
		_, err := fmt.Sscan(v, &n)
		if err == nil && n > 0 {
			return time.Duration(n) * time.Second
		}
	}
	return def
}

// RegisterRoutes mounts the auth handlers under /api/auth
func RegisterRoutes(r *gin.Engine, svc *Service) {
	UseService(svc)
	grp := r.Group("/api/auth")

	grp.POST("/login", func(c *gin.Context) {
		var email, password string
		ct := strings.ToLower(c.GetHeader("Content-Type"))
		if strings.Contains(ct, "application/json") {
			var payload map[string]any
			if err := c.BindJSON(&payload); err == nil {
				if v, ok := payload["email"].(string); ok && v != "" {
					email = v
				} else if v, ok := payload["username"].(string); ok && v != "" {
					email = v
				}
				if v, ok := payload["password"].(string); ok {
					password = v
				}
			}
		} else {
			// form-urlencoded or multipart
			if v := c.PostForm("email"); v != "" {
				email = v
			} else if v := c.PostForm("username"); v != "" {
				email = v
			}
			password = c.PostForm("password")
		}

		if email == "" || password == "" {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Missing credentials: provide email and password"})
			return
		}

		u, err := svc.GetUserByEmail(c.Request.Context(), email)
		if err != nil || u == nil || !u.IsActive() {
			c.Header("WWW-Authenticate", "Bearer")
			c.JSON(http.StatusUnauthorized, gin.H{"detail": "Incorrect username or password"})
			return
		}
		ok, _ := VerifyPassword(password, u.Password)
		if !ok {
			c.Header("WWW-Authenticate", "Bearer")
			c.JSON(http.StatusUnauthorized, gin.H{"detail": "Incorrect username or password"})
			return
		}

		ttl := sessionTTL()
		sid, err := svc.CreateSessionForUser(c.Request.Context(), u.ID, ttl)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create session"})
			return
		}

		// Set cookie: HttpOnly, SameSite Lax, Secure false, 7 days
		maxAge := int(ttl.Seconds())
		// gin SetCookie(name, value, maxAge, path, domain, secure, httpOnly)
		c.SetSameSite(http.SameSiteLaxMode)
		c.SetCookie("session_id", sid, maxAge, "/", "", false, true)

		roles := RoleNames(u)
		c.JSON(http.StatusOK, loginResponse{User: toPublic(u), SessionID: sid, Roles: roles})
	})

	grp.GET("/session", func(c *gin.Context) {
		sid, err := c.Cookie("session_id")
		if err != nil || sid == "" {
			c.JSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
			return
		}
		u, err := svc.GetUserFromSession(c.Request.Context(), sid)
		if err != nil || u == nil || !u.IsActive() {
			c.JSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
			return
		}
		roles := RoleNames(u)
		c.JSON(http.StatusOK, sessionInfo{Authenticated: true, User: toPublic(u), Roles: roles})
	})

	grp.POST("/logout", func(c *gin.Context) {
		sid, _ := c.Cookie("session_id")
		if sid != "" {
			_ = svc.DeleteSession(c.Request.Context(), sid)
		}
		// Delete cookie by setting Max-Age=0
		c.SetCookie("session_id", "", -1, "/", "", false, true)
		c.JSON(http.StatusOK, gin.H{"ok": true})
	})
}
