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

	"voenix/backend/internal/auth"
)

func RegisterRoutes(router *gin.Engine, database *gorm.DB, service *Service) {
	admin := router.Group("/api/admin/images")
	admin.Use(auth.RequireAdmin(database))

	admin.POST("", func(context *gin.Context) {
		fileHeader, err := context.FormFile("file")
		if err != nil || fileHeader == nil {
			context.JSON(http.StatusBadRequest, gin.H{"detail": "Missing file"})
			return
		}

		var requestJSON string
		if requestFileHeader, err := context.FormFile("request"); err == nil && requestFileHeader != nil {
			file, err := requestFileHeader.Open()
			if err == nil {
				defer func() { _ = file.Close() }()
				bytes, _ := io.ReadAll(file)
				requestJSON = string(bytes)
			}
		} else {
			requestJSON = context.PostForm("request")
		}

		imageType := context.PostForm("imageType")
		cropX := context.PostForm("cropX")
		cropY := context.PostForm("cropY")
		cropWidth := context.PostForm("cropWidth")
		cropHeight := context.PostForm("cropHeight")

		request, parseErr := ParseUploadRequest(requestJSON, imageType, &cropX, &cropY, &cropWidth, &cropHeight)
		if parseErr != nil {
			context.JSON(http.StatusBadRequest, gin.H{"detail": parseErr.Error()})
			return
		}

		if contentType := fileHeader.Header.Get("Content-Type"); contentType != "" && !strings.HasPrefix(contentType, "image/") {
			context.JSON(http.StatusBadRequest, gin.H{"detail": "Uploaded file must be an image"})
			return
		}

		file, err := fileHeader.Open()
		if err != nil {
			context.JSON(http.StatusBadRequest, gin.H{"detail": "Failed to read upload"})
			return
		}
		defer func() { _ = file.Close() }()
		data, err := io.ReadAll(file)
		if err != nil {
			context.JSON(http.StatusBadRequest, gin.H{"detail": "Failed to read upload"})
			return
		}

		if request.CropArea != nil {
			data = CropImageBytes(data, request.CropArea.X, request.CropArea.Y, request.CropArea.Width, request.CropArea.Height)
		}

		pngBytes, err := ConvertImageToPNGBytes(data)
		if err != nil {
			context.JSON(http.StatusBadRequest, gin.H{"detail": "Failed to process image"})
			return
		}

		storageLocations, err := NewStorageLocations()
		if err != nil {
			context.JSON(http.StatusInternalServerError, gin.H{"detail": err.Error()})
			return
		}
		directory, err := storageLocations.ResolveAdminDir(request.ImageType)
		if err != nil {
			context.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
			return
		}
		path, err := StoreImageBytes(pngBytes, directory, "", "png", false)
		if err != nil {
			context.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to store image"})
			return
		}

		context.JSON(http.StatusCreated, gin.H{"filename": filepath.Base(path), "imageType": request.ImageType})
	})

	admin.GET("/prompt-test/:filename", func(context *gin.Context) {
		filename, err := SafeFilename(context.Param("filename"))
		if err != nil {
			context.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
			return
		}
		storageLocations, err := NewStorageLocations()
		if err != nil {
			context.JSON(http.StatusInternalServerError, gin.H{"detail": err.Error()})
			return
		}
		path := filepath.Join(storageLocations.PromptTest(), filename)
		if _, err := os.Stat(path); err != nil {
			context.JSON(http.StatusNotFound, gin.H{"detail": "Not found"})
			return
		}
		bytes, contentType, err := LoadImageBytesAndType(path)
		if err != nil {
			context.JSON(http.StatusNotFound, gin.H{"detail": "Not found"})
			return
		}
		context.Header("Content-Disposition", "inline; filename=\""+filepath.Base(path)+"\"")
		context.Data(http.StatusOK, contentType, bytes)
	})

	admin.DELETE("/prompt-test/:filename", func(context *gin.Context) {
		filename, err := SafeFilename(context.Param("filename"))
		if err != nil {
			context.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
			return
		}
		storageLocations, err := NewStorageLocations()
		if err != nil {
			context.JSON(http.StatusInternalServerError, gin.H{"detail": err.Error()})
			return
		}
		path := filepath.Join(storageLocations.PromptTest(), filename)
		if _, err := os.Stat(path); err == nil {
			if err := os.Remove(path); err != nil {
				context.JSON(http.StatusInternalServerError, gin.H{"detail": "Delete failed"})
				return
			}
		}
		context.Status(http.StatusNoContent)
	})

	userGroup := router.Group("/api/user/images")
	userGroup.Use(auth.RequireRoles(database, "USER", "ADMIN"))

	userGroup.GET("/:filename", func(context *gin.Context) {
		userValue, _ := context.Get("currentUser")
		currentUser, _ := userValue.(*auth.User)
		if currentUser == nil {
			context.JSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
			return
		}
		filename, err := SafeFilename(context.Param("filename"))
		if err != nil {
			context.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
			return
		}
		base, err := UserImagesDir(currentUser.ID)
		if err != nil {
			context.JSON(http.StatusInternalServerError, gin.H{"detail": err.Error()})
			return
		}
		path := filepath.Join(base, filename)
		if _, err := os.Stat(path); err != nil {
			context.JSON(http.StatusNotFound, gin.H{"detail": "Not found"})
			return
		}
		bytes, contentType, err := LoadImageBytesAndType(path)
		if err != nil {
			context.JSON(http.StatusNotFound, gin.H{"detail": "Not found"})
			return
		}
		context.Header("Content-Disposition", "inline; filename=\""+filepath.Base(path)+"\"")
		context.Data(http.StatusOK, contentType, bytes)
	})

	userGroup.GET("", func(context *gin.Context) {
		userValue, _ := context.Get("currentUser")
		currentUser, _ := userValue.(*auth.User)
		if currentUser == nil {
			context.JSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
			return
		}
		page, _ := strconv.Atoi(context.DefaultQuery("page", "0"))
		size, _ := strconv.Atoi(context.DefaultQuery("size", "20"))
		typeFilter := context.DefaultQuery("type", "all")
		sortBy := context.DefaultQuery("sortBy", "createdAt")
		sortDirection := context.DefaultQuery("sortDirection", "DESC")

		items, err := service.ListUserImages(currentUser.ID)
		if err != nil {
			context.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to scan images"})
			return
		}
		response := service.BuildUserImagesPage(items, typeFilter, sortBy, sortDirection, page, size)
		context.JSON(http.StatusOK, response)
	})
}
