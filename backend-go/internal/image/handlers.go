package image

import (
	"io"
	"net/http"
	"os"
	"path/filepath"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend-go/internal/auth"
)

// RegisterRoutes mounts user and admin image routes.
func RegisterRoutes(r *gin.Engine, db *gorm.DB) {
	// Admin routes
	admin := r.Group("/api/admin/images")
	admin.Use(auth.RequireAdmin(db))

	admin.POST("/", func(c *gin.Context) {
		// Parse multipart form
		fileHeader, err := c.FormFile("file")
		if err != nil || fileHeader == nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Missing file"})
			return
		}

		// request can be either a file part or a string field
		var requestJSON string
		if reqFH, err := c.FormFile("request"); err == nil && reqFH != nil {
			f, err := reqFH.Open()
			if err == nil {
				defer func() { _ = f.Close() }()
				b, _ := io.ReadAll(f)
				requestJSON = string(b)
			}
		} else {
			requestJSON = c.PostForm("request")
		}

		imageType := c.PostForm("imageType")
		cropX := c.PostForm("cropX")
		cropY := c.PostForm("cropY")
		cropW := c.PostForm("cropWidth")
		cropH := c.PostForm("cropHeight")

		req, perr := ParseUploadRequest(requestJSON, imageType, &cropX, &cropY, &cropW, &cropH)
		if perr != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": perr.Error()})
			return
		}

		// quick content type check – best effort
		if ct := fileHeader.Header.Get("Content-Type"); ct != "" && !strings.HasPrefix(ct, "image/") {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Uploaded file must be an image"})
			return
		}

		f, err := fileHeader.Open()
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Failed to read upload"})
			return
		}
		defer func() { _ = f.Close() }()
		data, err := io.ReadAll(f)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Failed to read upload"})
			return
		}

		if req.CropArea != nil {
			data = CropImageBytes(data, req.CropArea.X, req.CropArea.Y, req.CropArea.Width, req.CropArea.Height)
		}

		// Convert to PNG
		pngBytes, err := ConvertImageToPNGBytes(data)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Failed to process image"})
			return
		}

		// Store under resolved directory
		loc, err := NewStorageLocations()
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": err.Error()})
			return
		}
		dir, err := loc.ResolveAdminDir(req.ImageType)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
			return
		}
		path, err := StoreImageBytes(pngBytes, dir, "", "png", false)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to store image"})
			return
		}

		c.JSON(http.StatusCreated, gin.H{"filename": filepath.Base(path), "imageType": req.ImageType})
	})

	admin.GET("/prompt-test/:filename", func(c *gin.Context) {
		fname, err := SafeFilename(c.Param("filename"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
			return
		}
		loc, err := NewStorageLocations()
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": err.Error()})
			return
		}
		path := filepath.Join(loc.PromptTest(), fname)
		if _, err := os.Stat(path); err != nil {
			c.JSON(http.StatusNotFound, gin.H{"detail": "Not found"})
			return
		}
		data, ctype, err := LoadImageBytesAndType(path)
		if err != nil {
			c.JSON(http.StatusNotFound, gin.H{"detail": "Not found"})
			return
		}
		c.Header("Content-Disposition", "inline; filename=\""+filepath.Base(path)+"\"")
		c.Data(http.StatusOK, ctype, data)
	})

	admin.DELETE("/prompt-test/:filename", func(c *gin.Context) {
		fname, err := SafeFilename(c.Param("filename"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
			return
		}
		loc, err := NewStorageLocations()
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": err.Error()})
			return
		}
		path := filepath.Join(loc.PromptTest(), fname)
		// If exists, delete; otherwise 204
		if _, err := os.Stat(path); err == nil {
			if err := os.Remove(path); err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Delete failed"})
				return
			}
		}
		c.Status(http.StatusNoContent)
	})

	// User routes
	user := r.Group("/api/user/images")
	user.Use(auth.RequireRoles(db, "USER", "ADMIN"))

	user.GET("/:filename", func(c *gin.Context) {
		uVal, _ := c.Get("currentUser")
		u, _ := uVal.(*auth.User)
		if u == nil {
			c.JSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
			return
		}
		fname, err := SafeFilename(c.Param("filename"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
			return
		}
		base, err := UserImagesDir(u.ID)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": err.Error()})
			return
		}
		path := filepath.Join(base, fname)
		if _, err := os.Stat(path); err != nil {
			c.JSON(http.StatusNotFound, gin.H{"detail": "Not found"})
			return
		}
		data, ctype, err := LoadImageBytesAndType(path)
		if err != nil {
			c.JSON(http.StatusNotFound, gin.H{"detail": "Not found"})
			return
		}
		c.Header("Content-Disposition", "inline; filename=\""+filepath.Base(path)+"\"")
		c.Data(http.StatusOK, ctype, data)
	})

	user.GET("/", func(c *gin.Context) {
		uVal, _ := c.Get("currentUser")
		u, _ := uVal.(*auth.User)
		if u == nil {
			c.JSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
			return
		}
		page, _ := strconv.Atoi(c.DefaultQuery("page", "0"))
		size, _ := strconv.Atoi(c.DefaultQuery("size", "20"))
		typeFilter := c.DefaultQuery("type", "all")
		sortBy := c.DefaultQuery("sortBy", "createdAt")
		sortDir := c.DefaultQuery("sortDirection", "DESC")

		items, err := ScanUserImages(u.ID)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to scan images"})
			return
		}
		resp := SortFilterPaginate(items, typeFilter, sortBy, sortDir, page, size)
		c.JSON(http.StatusOK, resp)
	})
}
