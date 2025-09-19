package main

import (
	"errors"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"strings"
	"time"

	"voenix/backend/internal/ai"
	"voenix/backend/internal/article"
	articlePg "voenix/backend/internal/article/postgres"
	"voenix/backend/internal/auth"
	authPg "voenix/backend/internal/auth/postgres"
	"voenix/backend/internal/cart"
	"voenix/backend/internal/country"
	"voenix/backend/internal/database"
	"voenix/backend/internal/image"
	"voenix/backend/internal/order"
	"voenix/backend/internal/prompt"
	"voenix/backend/internal/supplier"
	"voenix/backend/internal/vat"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	_ "github.com/joho/godotenv/autoload"
)

func main() {
	db, err := database.Open()
	if err != nil {
		log.Fatalf("failed to open DB: %v", err)
	}

	database.DoMigrations()

	r := gin.Default()

	// Configure CORS via gin-contrib/cors
	allowed := []string{
		"*",
		"http://localhost",
		"http://localhost:3000",
		"http://localhost:5173",
		"http://127.0.0.1",
	}
	if v := os.Getenv("CORS_ALLOWED_ORIGINS"); v != "" {
		allowed = strings.Split(v, ",")
	}

	cfg := cors.Config{
		AllowMethods:     []string{"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"},
		AllowHeaders:     []string{"Authorization", "Content-Type", "X-Requested-With"},
		AllowCredentials: true,
		MaxAge:           12 * time.Hour,
	}
	// If wildcard present, allow all origins by echoing the request origin.
	if contains(allowed, "*") {
		cfg.AllowOriginFunc = func(origin string) bool { return true }
	} else {
		cfg.AllowOrigins = allowed
	}
	r.Use(cors.New(cfg))

	// Health
	r.GET("/health", func(c *gin.Context) { c.JSON(http.StatusOK, gin.H{"ok": true}) })

	// Static public files: serve ${STORAGE_ROOT}/public at /public
	if loc, err := image.NewStorageLocations(); err != nil {
		log.Printf("warning: STORAGE_ROOT not set; static /public disabled: %v", err)
	} else {
		publicDir := filepath.Join(loc.Root, "public")
		// Disable directory listing (last arg = false)
		r.StaticFS("/public", gin.Dir(publicDir, false))
		log.Printf("Serving static files at /public from %s", publicDir)
	}

	serveFrontend(r)

	// Middlewares
	requireAdminMiddleware := auth.RequireAdmin(db)

	// Repositories
	authRepo := authPg.NewRepository(db)
	articleRepo := articlePg.NewRepository(db)

	// Services
	authSvc := auth.NewService(authRepo)
	articleSvc := article.NewService(articleRepo)

	// Routes
	auth.RegisterRoutes(r, authSvc)
	vat.RegisterRoutes(r, db)
	country.RegisterRoutes(r, db)
	supplier.RegisterRoutes(r, db)
	image.RegisterRoutes(r, db)
	ai.RegisterRoutes(r, db)
	prompt.RegisterRoutes(r, db, ai.ProviderLLMIDs())
	article.RegisterRoutes(r, requireAdminMiddleware, articleSvc)
	cart.RegisterRoutes(r, db, articleSvc)
	order.RegisterRoutes(r, db, articleSvc)

	addr := os.Getenv("ADDR")
	if addr == "" {
		addr = ":8080"
	}
	s := &http.Server{
		Addr:              addr,
		Handler:           r,
		ReadHeaderTimeout: 5 * time.Second,
	}
	log.Printf("Go backend listening on %s", addr)
	checkIfTestMode()
	if err := s.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
		log.Fatalf("server error: %v", err)
	}

}

func serveFrontend(r *gin.Engine) {
	// Optional: Serve built frontend (Vite) when FRONTEND_DIST is set
	// - Expects a directory containing index.html and an assets/ subfolder
	// - Serves /assets/* statics and SPA fallback for non-/api,/public routes
	if dist := os.Getenv("FRONTEND_DIST"); strings.TrimSpace(dist) != "" {
		// Validate directory and presence of index.html
		if fi, err := os.Stat(dist); err != nil || !fi.IsDir() {
			log.Printf("warning: FRONTEND_DIST not a directory or unreadable: %s", dist)
		} else if _, err := os.Stat(filepath.Join(dist, "index.html")); err != nil {
			log.Printf("warning: FRONTEND_DIST missing index.html: %s", dist)
		} else {
			// Serve hashed asset files (JS/CSS/images)
			assetsDir := filepath.Join(dist, "assets")
			if st, err := os.Stat(assetsDir); err == nil && st.IsDir() {
				r.StaticFS("/assets", gin.Dir(assetsDir, false))
			}
			// Serve common root static files if present
			for _, name := range []string{
				"favicon.ico",
				"favicon.svg",
				"robots.txt",
				"manifest.webmanifest",
				"icon.svg",
				"apple-touch-icon.png",
			} {
				p := filepath.Join(dist, name)
				if st, err := os.Stat(p); err == nil && !st.IsDir() {
					r.StaticFile("/"+name, p)
				}
			}
			// Index at root
			r.GET("/", func(c *gin.Context) {
				c.File(filepath.Join(dist, "index.html"))
			})
			// SPA fallback for client-routed paths
			r.NoRoute(func(c *gin.Context) {
				p := c.Request.URL.Path
				// Preserve API and storage namespaces
				if strings.HasPrefix(p, "/api/") || strings.HasPrefix(p, "/public/") {
					c.JSON(http.StatusNotFound, gin.H{"detail": "not found"})
					return
				}
				// Only serve index for HTML navigations
				if strings.Contains(c.GetHeader("Accept"), "text/html") {
					c.File(filepath.Join(dist, "index.html"))
					return
				}
				c.Status(http.StatusNotFound)
			})
			log.Printf("Serving frontend from %s (index + /assets, SPA fallback)", dist)
		}
	}
}

func contains(arr []string, v string) bool {
	for _, a := range arr {
		if strings.TrimSpace(a) == v {
			return true
		}
	}
	return false
}

func checkIfTestMode() {
	if ai.IsTestMode() {
		log.Printf("️===========================")
		log.Printf("⚠️ RUNNING IN TEST MODE ⚠️")
		log.Printf("️===========================")
	}
}
