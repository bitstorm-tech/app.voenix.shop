package ai

import (
	"context"
	"encoding/json"
	"errors"
	"io"
	"net/http"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"gorm.io/gorm"

	"voenix/backend/internal/auth"
	imgsvc "voenix/backend/internal/image"
	"voenix/backend/internal/prompt"
)

type promptReader interface {
	GetPrompt(ctx context.Context, id int) (*prompt.PromptRead, error)
}

// Accepts: OPENAI|GOOGLE|FLUX (case-insensitive). Defaults to GOOGLE=>Gemini for broader coverage.
func providerFromParam(p string) (Provider, bool) {
	// In test mode, always use the mock provider regardless of input.
	if IsTestMode() {
		return ProviderMock, true
	}
	switch strings.ToUpper(strings.TrimSpace(p)) {
	case "GOOGLE", "GEMINI", string(ProviderGemini):
		return ProviderGemini, true
	case "FLUX", string(ProviderFlux):
		return ProviderFlux, false // not implemented
	case "OPENAI", "GPT", string(ProviderGPT):
		return ProviderGPT, true
	case "MOCK", "TEST", string(ProviderMock):
		return ProviderMock, true
	default:
		// default to Gemini
		return ProviderGemini, true
	}
}

type testPromptResponse struct {
	ImageURL      string      `json:"imageUrl"`
	Filename      string      `json:"filename"`
	FinalPrompt   string      `json:"finalPrompt"`
	RequestParams interface{} `json:"requestParams,omitempty"`
}

type imageEditResponse struct {
	ImageFilenames []string `json:"imageFilenames"`
}

// Only fields we may consume are included; others are ignored for now.
type createImageEditRequest struct {
	Prompt     string `json:"prompt"`
	PromptID   int64  `json:"promptId"`
	Background string `json:"background"`
	Quality    string `json:"quality"`
	Size       string `json:"size"`
	N          int    `json:"n"`
}

func RegisterRoutes(r *gin.Engine, db *gorm.DB, imageService *imgsvc.Service, promptService promptReader) {
	// Admin AI routes
	admin := r.Group("/api/admin/ai")
	admin.Use(auth.RequireAdmin(db))

	admin.GET("/llms", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"llms": ProviderLLMs()})
	})

	// POST /api/admin/ai/test-prompt
	// Multipart form:
	// - image: file (required)
	// - masterPrompt: string (required)
	// - specificPrompt: string (optional)
	// - background, quality, size: strings (optional)
	// - provider: query or form value (OPENAI|GOOGLE|FLUX), defaults to GOOGLE(Gemini)
	admin.POST("/test-prompt", func(c *gin.Context) {
		fileHeader, err := c.FormFile("image")
		if err != nil || fileHeader == nil {
			c.JSON(http.StatusBadRequest, gin.H{"message": "Missing image"})
			return
		}
		// quick best-effort content type check
		if ct := fileHeader.Header.Get("Content-Type"); ct != "" && !strings.HasPrefix(ct, "image/") {
			c.JSON(http.StatusBadRequest, gin.H{"message": "Uploaded file must be an image"})
			return
		}

		master := strings.TrimSpace(c.PostForm("masterPrompt"))
		if master == "" {
			c.JSON(http.StatusUnprocessableEntity, gin.H{"message": "Validation failed", "errors": gin.H{"masterPrompt": "Master prompt is required"}})
			return
		}
		specific := strings.TrimSpace(c.PostForm("specificPrompt"))
		background := strings.TrimSpace(c.PostForm("background"))
		quality := strings.TrimSpace(c.PostForm("quality"))
		size := strings.TrimSpace(c.PostForm("size"))

		finalPrompt := strings.TrimSpace(strings.Join([]string{master, specific}, " "))
		provStr := c.DefaultQuery("provider", c.PostForm("provider"))
		if provStr == "" {
			provStr = "GOOGLE"
		}
		prov, ok := providerFromParam(provStr)
		if !ok {
			c.JSON(http.StatusBadRequest, gin.H{"message": "Provider not implemented"})
			return
		}

		f, err := fileHeader.Open()
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"message": "Failed to read upload"})
			return
		}
		defer func() { _ = f.Close() }()
		data, err := io.ReadAll(f)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"message": "Failed to read upload"})
			return
		}

		gen, err := Create(prov)
		if err != nil {
			c.JSON(http.StatusBadGateway, gin.H{"message": err.Error()})
			return
		}

		ctx, cancel := context.WithTimeout(c.Request.Context(), 120*time.Second)
		defer cancel()
		images, err := gen.Edit(ctx, data, finalPrompt, 1)
		if err != nil || len(images) == 0 {
			var sb *SafetyBlockedError
			if errors.As(err, &sb) {
				c.JSON(http.StatusUnprocessableEntity, gin.H{"message": "Request blocked by AI safety filters", "detail": sb.Reason})
				return
			}
			c.JSON(http.StatusBadGateway, gin.H{"message": "Image generation failed", "detail": safeErr(err)})
			return
		}

		// Store as PNG under prompt-test directory
		loc, err := imgsvc.NewStorageLocations()
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"message": err.Error()})
			return
		}
		pngBytes, err := imgsvc.ConvertImageToPNGBytes(images[0])
		if err != nil {
			pngBytes = images[0]
		}
		fullPath, err := imgsvc.StoreImageBytes(pngBytes, loc.PromptTest(), "", "png", false)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"message": "Failed to store image"})
			return
		}
		filename := filepath.Base(fullPath)
		imageURL := "/api/admin/images/prompt-test/" + filename

		// Include request params for UI insight
		modelName := "gemini-2.5-flash-image-preview"
		if prov == ProviderGPT {
			modelName = "gpt-image-1"
		}
		reqParams := map[string]any{
			"model":          modelName,
			"size":           size,
			"n":              1,
			"responseFormat": "b64_json",
			"masterPrompt":   master,
			"specificPrompt": specific,
			"combinedPrompt": finalPrompt,
			"quality":        quality,
			"background":     background,
			"provider":       strings.ToUpper(provStr),
		}
		c.JSON(http.StatusOK, testPromptResponse{ImageURL: imageURL, Filename: filename, FinalPrompt: finalPrompt, RequestParams: reqParams})
	})

	admin.POST("/image-edit", func(c *gin.Context) {
		fileHeader, err := c.FormFile("image")
		if err != nil || fileHeader == nil {
			c.JSON(http.StatusBadRequest, gin.H{"message": "Missing image"})
			return
		}
		// request can be either a file part or a string field
		var reqJSON string
		if reqFH, err := c.FormFile("request"); err == nil && reqFH != nil {
			f, err := reqFH.Open()
			if err == nil {
				defer func() { _ = f.Close() }()
				b, _ := io.ReadAll(f)
				reqJSON = string(b)
			}
		} else {
			reqJSON = c.PostForm("request")
		}
		var req createImageEditRequest
		if strings.TrimSpace(reqJSON) != "" {
			_ = json.Unmarshal([]byte(reqJSON), &req) // best-effort; validation below
		}
		// Fallbacks for prompt and n if provided as plain fields
		if req.Prompt == "" {
			req.Prompt = strings.TrimSpace(c.PostForm("prompt"))
		}
		if req.N == 0 {
			if v := strings.TrimSpace(c.PostForm("n")); v != "" {
				if vv, err := strconv.Atoi(v); err == nil {
					req.N = vv
				}
			}
		}
		if req.N <= 0 || req.N > 10 {
			req.N = 1
		}
		if strings.TrimSpace(req.Prompt) == "" {
			// We don't have promptId-backed prompts on Go backend
			c.JSON(http.StatusBadRequest, gin.H{"message": "Missing prompt"})
			return
		}

		// quick best-effort content type check
		if ct := fileHeader.Header.Get("Content-Type"); ct != "" && !strings.HasPrefix(ct, "image/") {
			c.JSON(http.StatusBadRequest, gin.H{"message": "Uploaded file must be an image"})
			return
		}

		f, err := fileHeader.Open()
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"message": "Failed to read upload"})
			return
		}
		defer func() { _ = f.Close() }()
		data, err := io.ReadAll(f)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"message": "Failed to read upload"})
			return
		}

		provStr := c.DefaultQuery("provider", c.PostForm("provider"))
		if provStr == "" {
			provStr = "GOOGLE"
		}
		prov, ok := providerFromParam(provStr)
		if !ok {
			c.JSON(http.StatusBadRequest, gin.H{"message": "Provider not implemented"})
			return
		}

		gen, err := Create(prov)
		if err != nil {
			c.JSON(http.StatusBadGateway, gin.H{"message": err.Error()})
			return
		}
		ctx, cancel := context.WithTimeout(c.Request.Context(), 120*time.Second)
		defer cancel()
		images, err := gen.Edit(ctx, data, req.Prompt, req.N)
		if err != nil || len(images) == 0 {
			var sb *SafetyBlockedError
			if errors.As(err, &sb) {
				c.JSON(http.StatusUnprocessableEntity, gin.H{"message": "Request blocked by AI safety filters", "detail": sb.Reason})
				return
			}
			c.JSON(http.StatusBadGateway, gin.H{"message": "Image edit failed", "detail": safeErr(err)})
			return
		}

		// Store as PNG under prompt-test directory (admin accessible)
		loc, err := imgsvc.NewStorageLocations()
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"message": err.Error()})
			return
		}
		out := make([]string, 0, len(images))
		for _, b := range images {
			pngBytes, err := imgsvc.ConvertImageToPNGBytes(b)
			if err != nil {
				pngBytes = b
			}
			fullPath, err := imgsvc.StoreImageBytes(pngBytes, loc.PromptTest(), "", "png", false)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"message": "Failed to store image"})
				return
			}
			out = append(out, filepath.Base(fullPath))
		}
		c.JSON(http.StatusOK, imageEditResponse{ImageFilenames: out})
	})

	// User AI routes (stub)
	user := r.Group("/api/user/ai/images")
	user.Use(auth.RequireRoles(db, "USER", "ADMIN"))
	user.POST("/generate", func(c *gin.Context) {
		// Authenticated user
		uVal, _ := c.Get("currentUser")
		u, _ := uVal.(*auth.User)
		if u == nil {
			c.JSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
			return
		}

		// Parse multipart: image (required)
		fileHeader, err := c.FormFile("image")
		if err != nil || fileHeader == nil {
			c.JSON(http.StatusBadRequest, gin.H{"message": "Missing image"})
			return
		}
		if ct := fileHeader.Header.Get("Content-Type"); ct != "" && !strings.HasPrefix(ct, "image/") {
			c.JSON(http.StatusBadRequest, gin.H{"message": "Uploaded file must be an image"})
			return
		}

		// promptId (required)
		pidStr := strings.TrimSpace(c.PostForm("promptId"))
		if pidStr == "" {
			c.JSON(http.StatusBadRequest, gin.H{"message": "Missing promptId"})
			return
		}
		pid64, err := strconv.ParseInt(pidStr, 10, 64)
		if err != nil || pid64 <= 0 {
			c.JSON(http.StatusBadRequest, gin.H{"message": "Invalid promptId"})
			return
		}
		if promptService == nil {
			c.JSON(http.StatusInternalServerError, gin.H{"message": "Prompt service unavailable"})
			return
		}
		promptRead, promptLookupError := promptService.GetPrompt(c.Request.Context(), int(pid64))
		if promptLookupError != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"message": "Failed to load prompt"})
			return
		}
		if promptRead == nil {
			c.JSON(http.StatusNotFound, gin.H{"message": "Prompt not found"})
			return
		}
		promptText := strings.TrimSpace(derefPtr(promptRead.PromptText))
		if promptText == "" {
			promptText = strings.TrimSpace(promptRead.Title)
		}
		if promptText == "" {
			c.JSON(http.StatusUnprocessableEntity, gin.H{"message": "Prompt content is empty", "detail": "The requested prompt has no text configured"})
			return
		}

		// Optional crop params
		cropX, _ := strconv.ParseFloat(strings.TrimSpace(c.PostForm("cropX")), 64)
		cropY, _ := strconv.ParseFloat(strings.TrimSpace(c.PostForm("cropY")), 64)
		cropW, _ := strconv.ParseFloat(strings.TrimSpace(c.PostForm("cropWidth")), 64)
		cropH, _ := strconv.ParseFloat(strings.TrimSpace(c.PostForm("cropHeight")), 64)
		hasCrop := cropW != 0 && cropH != 0

		// Provider
		provStr := c.DefaultQuery("provider", c.PostForm("provider"))
		if provStr == "" {
			provStr = "GOOGLE"
		}
		prov, ok := providerFromParam(provStr)
		if !ok {
			c.JSON(http.StatusBadRequest, gin.H{"message": "Provider not implemented"})
			return
		}

		// Read file bytes
		f, err := fileHeader.Open()
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"message": "Failed to read upload"})
			return
		}
		defer func() { _ = f.Close() }()
		data, err := io.ReadAll(f)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"message": "Failed to read upload"})
			return
		}

		// Apply crop if provided
		if hasCrop {
			data = imgsvc.CropImageBytes(data, cropX, cropY, cropW, cropH)
		}
		// Normalize uploaded image to PNG for consistency
		pngBytes, err := imgsvc.ConvertImageToPNGBytes(data)
		if err != nil {
			pngBytes = data // fall back to original
		}

		// Prepare storage locations and user directory
		loc, err := imgsvc.NewStorageLocations()
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"message": err.Error()})
			return
		}
		userDir, err := imgsvc.UserImagesDir(u.ID)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"message": err.Error()})
			return
		}
		_ = loc // keep loc referenced for potential future use

		uploadedUUID := uuid.NewString()
		// Store original (cropped) image with UUID-based name and persist to DB (uploaded_images)
		origNameBase := uploadedUUID + "_original"
		origFullPath, err := imgsvc.StoreImageBytes(pngBytes, userDir, origNameBase, "png", false)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"message": "Failed to store uploaded image"})
			return
		}
		origStoredName := filepath.Base(origFullPath)
		// Prefer normalized content type
		uploadedContentType := "image/png"
		uploadedSize := int64(len(pngBytes))
		uploaded := imgsvc.UploadedImage{
			UUID:             uploadedUUID,
			OriginalFilename: fileHeader.Filename,
			StoredFilename:   origStoredName,
			ContentType:      uploadedContentType,
			FileSize:         uploadedSize,
			UserID:           u.ID,
			CreatedAt:        time.Now().UTC(),
		}
		if err := imageService.CreateUploadedImage(c.Request.Context(), &uploaded); err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"message": "Failed to persist uploaded image"})
			return
		}

		// Build generator and request N variants (default 4)
		gen, err := Create(prov)
		if err != nil {
			c.JSON(http.StatusBadGateway, gin.H{"message": err.Error()})
			return
		}
		// Use the same (cropped) data as source for edits
		n := 4
		ctx, cancel := context.WithTimeout(c.Request.Context(), 120*time.Second)
		defer cancel()
		images, err := gen.Edit(ctx, data, promptText, n)
		if err != nil || len(images) == 0 {
			var sb *SafetyBlockedError
			if errors.As(err, &sb) {
				c.JSON(http.StatusUnprocessableEntity, gin.H{"message": "Request blocked by AI safety filters", "detail": sb.Reason})
				return
			}
			c.JSON(http.StatusBadGateway, gin.H{"message": "Image generation failed", "detail": safeErr(err)})
			return
		}

		// Store generated images under the user directory and build URLs/IDs
		urls := make([]string, 0, len(images))
		ids := make([]int, 0, len(images))
		for i, b := range images {
			outBytes, err := imgsvc.ConvertImageToPNGBytes(b)
			if err != nil {
				outBytes = b
			}
			fname := uploadedUUID + "_generated_" + strconv.Itoa(i+1)
			fullPath, err := imgsvc.StoreImageBytes(outBytes, userDir, fname, "png", false)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"message": "Failed to store image"})
				return
			}
			justName := filepath.Base(fullPath)
			// Persist to DB (generated_images)
			genUUID := uuid.NewString()
			ip := c.ClientIP()
			gi := imgsvc.GeneratedImage{
				UUID:            genUUID,
				Filename:        justName,
				PromptID:        int(pid64),
				UserID:          &u.ID,
				UploadedImageID: &uploaded.ID,
				CreatedAt:       time.Now().UTC(),
				IPAddress:       stringPtrNonEmpty(ip),
			}
			if err := imageService.CreateGeneratedImage(c.Request.Context(), &gi); err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"message": "Failed to persist generated image"})
				return
			}
			urls = append(urls, "/api/user/images/"+justName)
			ids = append(ids, gi.ID)
		}

		c.JSON(http.StatusOK, gin.H{"imageUrls": urls, "generatedImageIds": ids})
	})

	// Public AI routes (stub)
	pub := r.Group("/api/public/ai/images")
	pub.POST("/generate", func(c *gin.Context) {
		c.JSON(http.StatusNotImplemented, gin.H{"message": "Public image generation not implemented in Go backend"})
	})
}

// ---- helpers ----

func derefPtr(p *string) string {
	if p == nil {
		return ""
	}
	return *p
}

func safeErr(err error) string {
	if err == nil {
		return ""
	}
	return err.Error()
}

func stringPtrNonEmpty(s string) *string {
	if s == "" {
		return nil
	}
	return &s
}
