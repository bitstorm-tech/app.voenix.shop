package article

import (
	"errors"

	"gorm.io/gorm"
)

func errorsIsNotFound(err error) bool { return errors.Is(err, gorm.ErrRecordNotFound) }

// Generic exists by id helper
func existsByID[T any](db *gorm.DB, id int) bool {
	var cnt int64
	db.Model(new(T)).Where("id = ?", id).Count(&cnt)
	return cnt > 0
}

// Counting helpers
func countArticlesByCategory(db *gorm.DB, categoryID int) int {
	var cnt int64
	db.Model(&Article{}).Where("category_id = ?", categoryID).Count(&cnt)
	return int(cnt)
}

func countArticlesBySubcategory(db *gorm.DB, subcategoryID int) int {
	var cnt int64
	db.Model(&Article{}).Where("subcategory_id = ?", subcategoryID).Count(&cnt)
	return int(cnt)
}

// timePtr and strPtrOrNil are defined in dtos.go
