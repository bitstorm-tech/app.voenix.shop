package main

import (
	"log"
	"net/http"
	"os"
	"strings"
	"time"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"

	"voenix/backend-go/internal/auth"
	"voenix/backend-go/internal/ai"
	"voenix/backend-go/internal/country"
	"voenix/backend-go/internal/database"
	"voenix/backend-go/internal/image"
	"voenix/backend-go/internal/supplier"
	"voenix/backend-go/internal/vat"
)

func main() {
	// Load environment variables from .env files if present
	// Tries current working directory and repo-local path when invoked from root.
	if fp := os.Getenv("ENV_FILE"); fp != "" {
		_ = godotenv.Load(fp)
	}
	_ = godotenv.Load(".env")
	_ = godotenv.Load("backend-go/.env")

	db, err := database.Open()
	if err != nil {
		log.Fatalf("failed to open DB: %v", err)
	}

	// Optional migration for auth tables only
	if err := database.AutoMigrateIfEnabled(db, &auth.User{}, &auth.Role{}, &auth.Session{}, &vat.ValueAddedTax{}, &country.Country{}, &supplier.Supplier{}); err != nil {
		log.Fatalf("auto-migrate failed: %v", err)
	}

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
	// If wildcard present, allow all origins by echoing request origin.
	if contains(allowed, "*") {
		cfg.AllowOriginFunc = func(origin string) bool { return true }
	} else {
		cfg.AllowOrigins = allowed
	}
	r.Use(cors.New(cfg))

	// Health
	r.GET("/health", func(c *gin.Context) { c.JSON(http.StatusOK, gin.H{"ok": true}) })

	// Auth routes
	auth.RegisterRoutes(r, db)
	// VAT admin routes
	vat.RegisterRoutes(r, db)
	// Country public routes (mirrors Kotlin public endpoint)
	country.RegisterRoutes(r, db)
	// Supplier admin routes
	supplier.RegisterRoutes(r, db)
	// Image admin/user routes
	image.RegisterRoutes(r, db)
	// AI image routes (admin)
	ai.RegisterRoutes(r, db)

	addr := os.Getenv("ADDR")
	if addr == "" {
		addr = ":8081" // keep separate from Kotlin :8080
	}
	s := &http.Server{
		Addr:              addr,
		Handler:           r,
		ReadHeaderTimeout: 5 * time.Second,
	}
	log.Printf("Go backend listening on %s", addr)
	if err := s.ListenAndServe(); err != nil && err != http.ErrServerClosed {
		log.Fatalf("server error: %v", err)
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
