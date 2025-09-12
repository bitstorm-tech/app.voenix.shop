SHELL := /bin/bash

.PHONY: dev

dev: ## Run server with hot reload via Air
	@if ! command -v air >/dev/null 2>&1; then \
		echo "Air is not installed. Install with: go install github.com/air-verse/air@latest"; \
		exit 1; \
	fi
	air -c .air.toml

run: ## Run server without hot reload
	go run ./cmd/server

build: ## Build server binary to ./bin/server
	@mkdir -p bin
	go build -o ./bin/server ./cmd/server

check: lint vet build

fmt: ## Format all Go files
	go fmt ./...

vet: ## Static analysis with go vet
	go vet ./...

lint: ## Run golangci-lint (aggregated linters)
	@if ! command -v golangci-lint >/dev/null 2>&1; then \
		echo "golangci-lint not found. Install with: go install github.com/golangci/golangci-lint/cmd/golangci-lint@latest"; \
		exit 1; \
	fi
	golangci-lint run

test: ## Run Go tests
	go test ./...
