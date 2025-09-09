package util

import (
    "crypto/sha256"
    "crypto/subtle"
    "encoding/base64"
    "errors"
    "fmt"
    "strings"

    "golang.org/x/crypto/pbkdf2"
)

// VerifyPassword verifies a plaintext password against a stored representation.
// Supports:
// - pbkdf2_sha256$<iterations>$<salt_b64>$<hash_b64>
// - legacy plain text (direct compare)
func VerifyPassword(plain string, stored *string) (bool, error) {
    if stored == nil || *stored == "" {
        return false, nil
    }
    s := *stored
    if strings.HasPrefix(s, "pbkdf2_sha256$") {
        parts := strings.SplitN(s, "$", 4)
        if len(parts) != 4 {
            return false, errors.New("invalid pbkdf2 format")
        }
        iterStr, saltB64, hashB64 := parts[1], parts[2], parts[3]
        var iterations int
        if _, err := fmt.Sscanf(iterStr, "%d", &iterations); err != nil || iterations <= 0 {
            return false, errors.New("invalid pbkdf2 iterations")
        }
        salt, err := base64.StdEncoding.DecodeString(saltB64)
        if err != nil {
            return false, errors.New("invalid salt b64")
        }
        expected, err := base64.StdEncoding.DecodeString(hashB64)
        if err != nil {
            return false, errors.New("invalid hash b64")
        }
        derived := pbkdf2.Key([]byte(plain), salt, iterations, len(expected), sha256.New)
        ok := subtle.ConstantTimeCompare(derived, expected) == 1
        return ok, nil
    }
    // Legacy fallback: direct constant-time compare
    ok := subtle.ConstantTimeCompare([]byte(plain), []byte(s)) == 1
    return ok, nil
}

