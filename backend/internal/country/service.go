package country

import "context"

type Service struct {
	repo Repository
}

func NewService(repo Repository) *Service {
	return &Service{repo: repo}
}

func (s *Service) All(ctx context.Context) ([]Country, error) {
	return s.repo.All(ctx)
}
