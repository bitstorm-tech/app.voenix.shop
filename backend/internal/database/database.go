package database

import (
	"errors"
	"fmt"
	"log"
	"os"
	"path/filepath"
	"strings"

	"gorm.io/driver/postgres"
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	"github.com/golang-migrate/migrate/v4"
	_ "github.com/golang-migrate/migrate/v4/database/postgres"
	_ "github.com/golang-migrate/migrate/v4/source/file"
	_ "github.com/joho/godotenv/autoload"
)

// ResolveDSN interprets a DATABASE_URL.
// - sqlite:///./app.db -> sqlite file ./app.db
// - sqlite:///<abs>    -> sqlite file <abs>
// - postgres://...     -> pass-through to the Postgres driver
func ResolveDSN(raw string) (driver string, dsn string) {
	if raw == "" {
		// default to sqlite file in repo root
		return "sqlite", filepath.Clean("./app.db")
	}

	low := strings.ToLower(raw)
	if strings.HasPrefix(low, "sqlite:///") {
		// Trim scheme: sqlite:///path
		path := strings.TrimPrefix(raw, "sqlite:///")
		if path == "" {
			path = "app.db"
		}
		return "sqlite", filepath.Clean(path)
	}
	if strings.HasPrefix(low, "sqlite:") {
		// e.g. sqlite://app.db or sqlite:app.db -> fallback to file after last colon/slashes
		idx := strings.LastIndex(raw, ":")
		if idx >= 0 && idx+1 < len(raw) {
			return "sqlite", filepath.Clean(raw[idx+1:])
		}
		return "sqlite", filepath.Clean("./app.db")
	}
	if strings.HasPrefix(low, "postgres://") || strings.HasPrefix(low, "postgresql://") {
		return "postgres", raw
	}
	// Fallback: treat as sqlite file path
	return "sqlite", filepath.Clean(raw)
}

// Open returns a configured *gorm.DB.
func Open() (*gorm.DB, error) {
	dbURL := os.Getenv("DATABASE_URL")
	driver, dsn := ResolveDSN(dbURL)

	gormConfig := &gorm.Config{
		Logger: logger.Default.LogMode(logger.Warn),
	}

	switch driver {
	case "postgres":
		return gorm.Open(postgres.Open(dsn), gormConfig)
	case "sqlite":
		return gorm.Open(sqlite.Open(dsn), gormConfig)
	default:
		return nil, fmt.Errorf("unsupported driver: %s", driver)
	}
}

func DoMigrations() {
	dbUrl := os.Getenv("DATABASE_URL")
	// Allow overriding the migrations' location; probe common defaults.
	srcURL := os.Getenv("MIGRATIONS_URL")
	if srcURL == "" {
		if _, err := os.Stat("internal/database/migrations"); err == nil {
			srcURL = "file://internal/database/migrations"
		} else if _, err := os.Stat("backend/internal/database/migrations"); err == nil {
			srcURL = "file://backend/internal/database/migrations"
		} else if _, err := os.Stat("/app/internal/database/migrations"); err == nil {
			srcURL = "file:///app/internal/database/migrations"
		} else if _, err := os.Stat("db/migrations"); err == nil {
			srcURL = "file://db/migrations"
		} else {
			log.Fatal("no migrations directory found; set MIGRATIONS_URL to file://...")
		}
	}

	log.Printf("running migrations from %s", srcURL)
	m, err := migrate.New(srcURL, dbUrl)
	if err != nil {
		log.Fatal(err)
	}
	if err := m.Up(); err != nil && !errors.Is(err, migrate.ErrNoChange) {
		log.Fatal(err)
	}
}
