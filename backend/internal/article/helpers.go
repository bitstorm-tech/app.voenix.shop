package article

import (
	"errors"

	"gorm.io/gorm"
)

func errorsIsNotFound(err error) bool { return errors.Is(err, gorm.ErrRecordNotFound) }

// timePtr and strPtrOrNil are defined in dtos.go
