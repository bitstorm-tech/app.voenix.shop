SHELL := /bin/bash

PACKAGES := ./cmd/... ./internal/...

.PHONY: dev

dev:
	air -c .air.toml

run:
	go run ./cmd/server

build:
	@mkdir -p bin
	go build -o ./bin/server ./cmd/server

check: lint vet build

fmt:
	go fmt $(PACKAGES)

vet:
	go vet $(PACKAGES)

lint:
	golangci-lint run $(PACKAGES)

test:
	go test $(PACKAGES)
