package image

import (
	"io"
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend/internal/auth"
)

func RegisterRoutes(router *gin.Engine, database *gorm.DB, service *Service) {
	admin := router.Group("/api/admin/images")
	admin.Use(auth.RequireAdmin(database))

	admin.POST("", func(ctx *gin.Context) {
		fileHeader, err := ctx.FormFile("file")
		if err != nil || fileHeader == nil {
			ctx.JSON(http.StatusBadRequest, gin.H{"detail": "Missing file"})
			return
		}

		var requestJSON string
		if requestFileHeader, err := ctx.FormFile("request"); err == nil && requestFileHeader != nil {
			requestFile, err := requestFileHeader.Open()
			if err == nil {
				defer func() { _ = requestFile.Close() }()
				requestBytes, _ := io.ReadAll(requestFile)
				requestJSON = string(requestBytes)
			}
		} else {
			requestJSON = ctx.PostForm("request")
		}

		imageType := ctx.PostForm("imageType")
		cropX := ctx.PostForm("cropX")
		cropY := ctx.PostForm("cropY")
		cropWidth := ctx.PostForm("cropWidth")
		cropHeight := ctx.PostForm("cropHeight")

		uploadRequest, parseErr := ParseUploadRequest(requestJSON, imageType, &cropX, &cropY, &cropWidth, &cropHeight)
		if parseErr != nil {
			ctx.JSON(http.StatusBadRequest, gin.H{"detail": parseErr.Error()})
			return
		}

		if contentType := fileHeader.Header.Get("Content-Type"); contentType != "" && !strings.HasPrefix(contentType, "image/") {
			ctx.JSON(http.StatusBadRequest, gin.H{"detail": "Uploaded file must be an image"})
			return
		}

		uploadedFile, err := fileHeader.Open()
		if err != nil {
			ctx.JSON(http.StatusBadRequest, gin.H{"detail": "Failed to read upload image"})
			return
		}
		defer func() { _ = uploadedFile.Close() }()

		filename, imageTypeResult, err := service.UploadAdminImage(ctx.Request.Context(), uploadedFile, uploadRequest.ImageType, uploadRequest.CropArea)
		if err != nil {
			ctx.JSON(http.StatusInternalServerError, gin.H{"detail": err.Error()})
			return
		}

		ctx.JSON(http.StatusCreated, gin.H{"filename": filename, "imageType": imageTypeResult})
	})

	admin.GET("/prompt-test/:filename", func(ctx *gin.Context) {
		filename := ctx.Param("filename")
		imageBytes, contentType, err := service.GetPromptTestImage(ctx.Request.Context(), filename)
		if err != nil {
			ctx.JSON(http.StatusNotFound, gin.H{"detail": "Not found"})
			return
		}
		ctx.Header("Content-Disposition", "inline; filename=\""+filename+"\"")
		ctx.Data(http.StatusOK, contentType, imageBytes)
	})

	admin.DELETE("/prompt-test/:filename", func(ctx *gin.Context) {
		filename := ctx.Param("filename")
		err := service.DeletePromptTestImage(ctx.Request.Context(), filename)
		if err != nil {
			ctx.JSON(http.StatusInternalServerError, gin.H{"detail": "Delete failed"})
			return
		}
		ctx.Status(http.StatusNoContent)
	})

	userGroup := router.Group("/api/user/images")
	userGroup.Use(auth.RequireRoles(database, "USER", "ADMIN"))

	userGroup.GET("/:filename", func(ctx *gin.Context) {
		userValue, _ := ctx.Get("currentUser")
		currentUser, _ := userValue.(*auth.User)
		if currentUser == nil {
			ctx.JSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
			return
		}
		filename := ctx.Param("filename")
		imageBytes, contentType, err := service.GetUserImage(ctx.Request.Context(), currentUser.ID, filename)
		if err != nil {
			ctx.JSON(http.StatusNotFound, gin.H{"detail": "Not found"})
			return
		}
		ctx.Header("Content-Disposition", "inline; filename=\""+filename+"\"")
		ctx.Data(http.StatusOK, contentType, imageBytes)
	})

	userGroup.GET("", func(ctx *gin.Context) {
		userValue, _ := ctx.Get("currentUser")
		currentUser, _ := userValue.(*auth.User)
		if currentUser == nil {
			ctx.JSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
			return
		}
		page, _ := strconv.Atoi(ctx.DefaultQuery("page", "0"))
		pageSize, _ := strconv.Atoi(ctx.DefaultQuery("size", "20"))
		typeFilter := ctx.DefaultQuery("type", "all")
		sortBy := ctx.DefaultQuery("sortBy", "createdAt")
		sortDirection := ctx.DefaultQuery("sortDirection", "DESC")

		userImages, err := service.ListUserImages(currentUser.ID)
		if err != nil {
			ctx.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to scan images"})
			return
		}
		paginatedResponse := service.BuildUserImagesPage(userImages, typeFilter, sortBy, sortDirection, page, pageSize)
		ctx.JSON(http.StatusOK, paginatedResponse)
	})
}
