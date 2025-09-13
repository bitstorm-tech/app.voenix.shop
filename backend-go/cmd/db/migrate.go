package main

import "voenix/backend-go/internal/database"

func main() {
	database.DoMigrations()
}
