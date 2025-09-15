package auth

import (
	"crypto/rand"
	"encoding/base64"
	"errors"
	"strings"
	"time"

	"gorm.io/gorm"
)

const defaultTokenBytes = 32

func randomTokenURLSafe(n int) (string, error) {
	b := make([]byte, n)
	if _, err := rand.Read(b); err != nil {
		return "", err
	}
	s := base64.RawURLEncoding.EncodeToString(b)
	return s, nil
}

func GetUserByEmail(db *gorm.DB, email string) (*User, error) {
	if strings.TrimSpace(email) == "" {
		return nil, errors.New("email required")
	}
	var u User
	if err := db.Preload("Roles").Where("email = ?", email).First(&u).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return &u, nil
}

func CreateSessionForUser(db *gorm.DB, userID int, ttl time.Duration) (string, error) {
	token, err := randomTokenURLSafe(defaultTokenBytes)
	if err != nil {
		return "", err
	}
	var expiresAt *time.Time
	if ttl > 0 {
		t := time.Now().UTC().Add(ttl)
		expiresAt = &t
	}
	s := &Session{ID: token, UserID: userID, ExpiresAt: expiresAt}
	if err := db.Create(s).Error; err != nil {
		return "", err
	}
	return token, nil
}

func DeleteSession(db *gorm.DB, sessionID string) error {
	if strings.TrimSpace(sessionID) == "" {
		return nil
	}
	return db.Delete(&Session{ID: sessionID}).Error
}

func GetUserFromSession(db *gorm.DB, sessionID string) (*User, error) {
	if strings.TrimSpace(sessionID) == "" {
		return nil, nil
	}
	// Load session
	var s Session
	if err := db.First(&s, "id = ?", sessionID).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	// Check expiration
	if s.ExpiresAt != nil && time.Now().UTC().After(*s.ExpiresAt) {
		// remove expired session
		_ = db.Delete(&Session{ID: s.ID}).Error
		return nil, nil
	}
	var u User
	if err := db.Preload("Roles").First(&u, "id = ?", s.UserID).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return &u, nil
}

func RoleNames(u *User) []string {
	if u == nil {
		return nil
	}
	out := make([]string, 0, len(u.Roles))
	for _, r := range u.Roles {
		out = append(out, r.Name)
	}
	return out
}
