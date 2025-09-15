package image

import (
	"errors"
	"os"
	"path/filepath"
	"strings"
)

// StorageLocations centralizes filesystem locations for storing images.
//
// Reads STORAGE_ROOT from the environment. Paths are constructed under this root
// for public and private images
type StorageLocations struct {
	Root string
}

// NewStorageLocations reads STORAGE_ROOT from env and constructs a StorageLocations.
func NewStorageLocations() (*StorageLocations, error) {
	root := os.Getenv("STORAGE_ROOT")
	if strings.TrimSpace(root) == "" {
		return nil, errors.New("STORAGE_ROOT is not configured. Set it in the environment or .env file")
	}
	return &StorageLocations{Root: root}, nil
}

// PublicImages returns {root}/public/images
func (s *StorageLocations) PublicImages() string {
	return filepath.Join(s.Root, "public", "images")
}

// PrivateImages returns {root}/private/images
func (s *StorageLocations) PrivateImages() string {
	return filepath.Join(s.Root, "private", "images")
}

// PromptTest returns {root}/private/images/0_prompt-test
func (s *StorageLocations) PromptTest() string {
	return filepath.Join(s.PrivateImages(), "0_prompt-test")
}

// PromptExample returns {root}/public/images/prompt-example-images
func (s *StorageLocations) PromptExample() string {
	return filepath.Join(s.PublicImages(), "prompt-example-images")
}

// PromptSlotVariantExample returns {root}/public/images/prompt-slot-variant-example-images
func (s *StorageLocations) PromptSlotVariantExample() string {
	return filepath.Join(s.PublicImages(), "prompt-slot-variant-example-images")
}

// MugVariantExample returns {root}/public/images/articles/mugs/variant-example-images
func (s *StorageLocations) MugVariantExample() string {
	return filepath.Join(s.PublicImages(), "articles", "mugs", "variant-example-images")
}

// ShirtVariantExample returns {root}/public/images/articles/shirts/variant-example-images
func (s *StorageLocations) ShirtVariantExample() string {
	return filepath.Join(s.PublicImages(), "articles", "shirts", "variant-example-images")
}

// ResolveAdminDir maps an imageType to a directory.
// Supported:
// - PROMPT_EXAMPLE
// - PROMPT_SLOT_VARIANT_EXAMPLE
// - MUG_VARIANT_EXAMPLE
// - SHIRT_VARIANT_EXAMPLE
// - PROMPT_TEST
// - PUBLIC
// - PRIVATE
func (s *StorageLocations) ResolveAdminDir(imageType string) (string, error) {
	key := strings.ToUpper(strings.TrimSpace(imageType))
	switch key {
	case "PROMPT_EXAMPLE":
		return s.PromptExample(), nil
	case "PROMPT_SLOT_VARIANT_EXAMPLE":
		return s.PromptSlotVariantExample(), nil
	case "MUG_VARIANT_EXAMPLE":
		return s.MugVariantExample(), nil
	case "SHIRT_VARIANT_EXAMPLE":
		return s.ShirtVariantExample(), nil
	case "PROMPT_TEST":
		return s.PromptTest(), nil
	case "PUBLIC":
		return s.PublicImages(), nil
	case "PRIVATE":
		return s.PrivateImages(), nil
	default:
		return "", errors.New("Unsupported imageType: " + imageType)
	}
}
