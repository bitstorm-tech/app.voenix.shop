package auth

import (
	"context"
	"crypto/rand"
	"encoding/base64"
	"errors"
	"strings"
	"sync/atomic"
	"time"
)

const defaultTokenBytes = 32

var activeService atomic.Pointer[Service]

// Service coordinates auth workflows on top of the repository abstraction.
type Service struct {
	repo Repository
	now  func() time.Time
}

// NewService constructs a Service with the provided repository.
func NewService(repo Repository) *Service {
	return &Service{repo: repo, now: time.Now}
}

// UseService registers the provided service as the default for package helpers.
func UseService(svc *Service) {
	activeService.Store(svc)
}

// CurrentService retrieves the active auth service if configured.
func CurrentService() *Service {
	return activeService.Load()
}

// GetUserByEmail looks up a user along with their roles.
func (s *Service) GetUserByEmail(ctx context.Context, email string) (*User, error) {
	if strings.TrimSpace(email) == "" {
		return nil, errors.New("email required")
	}
	user, err := s.repo.UserByEmail(ctx, email)
	if err != nil {
		return nil, err
	}
	return user, nil
}

// CreateSessionForUser issues a new session token for the given user.
func (s *Service) CreateSessionForUser(ctx context.Context, userID int, ttl time.Duration) (string, error) {
	token, err := randomTokenURLSafe(defaultTokenBytes)
	if err != nil {
		return "", err
	}
	var expiresAt *time.Time
	if ttl > 0 {
		expiry := s.now().UTC().Add(ttl)
		expiresAt = &expiry
	}
	session := &Session{
		ID:        token,
		UserID:    userID,
		CreatedAt: s.now().UTC(),
		ExpiresAt: expiresAt,
	}
	if err := s.repo.CreateSession(ctx, session); err != nil {
		return "", err
	}
	return token, nil
}

// DeleteSession removes the stored session token when it exists.
func (s *Service) DeleteSession(ctx context.Context, sessionID string) error {
	if strings.TrimSpace(sessionID) == "" {
		return nil
	}
	return s.repo.DeleteSession(ctx, sessionID)
}

// GetUserFromSession resolves the user for a session, clearing expired sessions.
func (s *Service) GetUserFromSession(ctx context.Context, sessionID string) (*User, error) {
	if strings.TrimSpace(sessionID) == "" {
		return nil, nil
	}
	session, err := s.repo.SessionByID(ctx, sessionID)
	if err != nil {
		return nil, err
	}
	if session == nil {
		return nil, nil
	}
	now := s.now().UTC()
	if session.ExpiresAt != nil && now.After(*session.ExpiresAt) {
		_ = s.repo.DeleteSession(ctx, session.ID)
		return nil, nil
	}
	user, err := s.repo.UserByID(ctx, session.UserID)
	if err != nil {
		return nil, err
	}
	return user, nil
}

func randomTokenURLSafe(n int) (string, error) {
	b := make([]byte, n)
	if _, err := rand.Read(b); err != nil {
		return "", err
	}
	return base64.RawURLEncoding.EncodeToString(b), nil
}
