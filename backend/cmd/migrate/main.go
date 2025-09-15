package main

import "voenix/backend/internal/database"

func main() {
	database.DoMigrations()
}
