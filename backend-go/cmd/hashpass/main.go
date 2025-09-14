package main

import (
    "bufio"
    "crypto/rand"
    "crypto/sha256"
    "encoding/base64"
    "errors"
    "flag"
    "fmt"
    "io"
    "os"
    "strings"

    "golang.org/x/crypto/pbkdf2"
)

// run is the entry point for the hashpass CLI.
// It generates a password hash compatible with the Go backend's
// VerifyPassword implementation: "pbkdf2_sha256$<iterations>$<salt_b64>$<hash_b64>".
func run() int {
    var iterations int
    var saltLen int
    var password string

    flag.IntVar(&iterations, "iterations", 200000, "PBKDF2 iteration count")
    flag.IntVar(&saltLen, "salt-bytes", 16, "salt length in bytes")
    flag.StringVar(&password, "password", "", "password to hash (omit to read from stdin)")
    flag.Parse()

    if iterations <= 0 {
        fmt.Fprintln(os.Stderr, "iterations must be > 0")
        return 2
    }
    if saltLen <= 0 {
        fmt.Fprintln(os.Stderr, "salt-bytes must be > 0")
        return 2
    }

    if password == "" {
        // Read the first line from stdin if no --password provided.
        info, err := os.Stdin.Stat()
        if err != nil {
            fmt.Fprintf(os.Stderr, "failed to stat stdin: %v\n", err)
            return 2
        }
        if (info.Mode() & os.ModeCharDevice) != 0 {
            fmt.Fprintln(os.Stderr, "no --password provided; pass via --password or pipe via stdin")
            return 2
        }
        reader := bufio.NewReader(os.Stdin)
        line, err := reader.ReadString('\n')
        if err != nil && !errors.Is(err, io.EOF) {
            fmt.Fprintf(os.Stderr, "failed to read password from stdin: %v\n", err)
            return 2
        }
        password = strings.TrimRight(line, "\r\n")
    }

    salt := make([]byte, saltLen)
    if _, err := rand.Read(salt); err != nil {
        fmt.Fprintf(os.Stderr, "failed to read random salt: %v\n", err)
        return 2
    }

    derived := pbkdf2.Key([]byte(password), salt, iterations, 32, sha256.New)

    saltB64 := base64.StdEncoding.EncodeToString(salt)
    hashB64 := base64.StdEncoding.EncodeToString(derived)
    fmt.Printf("pbkdf2_sha256$%d$%s$%s\n", iterations, saltB64, hashB64)
    return 0
}

func main() {
    code := run()
    if code != 0 {
        os.Exit(code)
    }
}
