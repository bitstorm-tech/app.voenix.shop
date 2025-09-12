package main

import (
    "log"
    "os"

    "github.com/golang-migrate/migrate/v4"
    _ "github.com/golang-migrate/migrate/v4/database/postgres"
    _ "github.com/golang-migrate/migrate/v4/source/file"
    _ "github.com/joho/godotenv/autoload"
)

func main() {
    dbUrl := os.Getenv("DATABASE_URL")
    // Allow overriding the migrations location; probe common defaults.
    srcURL := os.Getenv("MIGRATIONS_URL")
    if srcURL == "" {
        if _, err := os.Stat("internal/database/migrations"); err == nil {
            srcURL = "file://internal/database/migrations"
        } else if _, err := os.Stat("backend-go/internal/database/migrations"); err == nil {
            srcURL = "file://backend-go/internal/database/migrations"
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
    if err := m.Up(); err != nil {
        log.Fatal(err)
    }
}
