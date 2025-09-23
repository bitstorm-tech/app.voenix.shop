SHELL := /bin/bash

PACKAGES := ./cmd/... ./internal/...

.PHONY: dev
dev:
	air -c .air.toml

.PHONY: run
run:
	go run ./cmd/server

.PHONY: build
build:
	@mkdir -p bin
	go build -o ./bin/server ./cmd/server

.PHONY: check
check: lint vet test build fmt

.PHONY: fmt
fmt:
	go fmt $(PACKAGES)

.PHONY: vet
vet:
	go vet $(PACKAGES)

.PHONY: lint
lint:
	golangci-lint run $(PACKAGES)

.PHONY: test
test:
	go test $(PACKAGES)
