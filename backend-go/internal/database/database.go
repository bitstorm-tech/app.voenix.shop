package database

import (
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"time"

	"gorm.io/driver/postgres"
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

// ResolveDSN interprets a DATABASE_URL similar to the Python service.
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

// AutoMigrateIfEnabled runs AutoMigrate when AUTO_MIGRATE=true.
func AutoMigrateIfEnabled(db *gorm.DB, models ...any) error {
	if strings.EqualFold(os.Getenv("AUTO_MIGRATE"), "true") {
		return db.AutoMigrate(models...)
	}
	return nil
}

// SessionTTL returns TTL seconds for sessions; default 7 days.
func SessionTTL() time.Duration {
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
