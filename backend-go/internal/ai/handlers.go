package ai

import (
    "encoding/json"
    "io"
    "net/http"
    "path/filepath"
    "strconv"
    "strings"
    "time"

    "github.com/gin-gonic/gin"
    "gorm.io/gorm"

    "voenix/backend-go/internal/auth"
    imgsvc "voenix/backend-go/internal/image"
)

// providerFromParam maps high-level provider names used by Kotlin/FE to internal providers.
// Accepts: OPENAI|GOOGLE|FLUX (case-insensitive). Defaults to GOOGLE=>Gemini for broader coverage.
func providerFromParam(p string) (Provider, bool) {
    switch strings.ToUpper(strings.TrimSpace(p)) {
    case "GOOGLE", "GEMINI", string(ProviderGemini):
        return ProviderGemini, true
    case "FLUX", string(ProviderFlux):
        return ProviderFlux, false // not implemented
    case "OPENAI", "GPT", string(ProviderGPT):
        return ProviderGPT, false // not implemented
    default:
        // default to Gemini
        return ProviderGemini, true
    }
}

type testPromptResponse struct {
    ImageURL     string      `json:"imageUrl"`
    Filename     string      `json:"filename"`
    FinalPrompt  string      `json:"finalPrompt"`
    RequestParams interface{} `json:"requestParams,omitempty"`
}

type imageEditResponse struct {
    ImageFilenames []string `json:"imageFilenames"`
}

// createImageEditRequest is a minimal mirror of Kotlin CreateImageEditRequest for parsing.
// Only fields we may consume are included; others are ignored for now.
type createImageEditRequest struct {
    Prompt         string `json:"prompt"`   // non-Kotlin, but allow FE overrides
    PromptID       int64  `json:"promptId"`
    Background     string `json:"background"`
    Quality        string `json:"quality"`
    Size           string `json:"size"`
    N              int    `json:"n"`
}

// RegisterRoutes mounts AI routes aligned with Kotlin controllers:
// - Admin:   POST /api/admin/ai/image-edit
//            POST /api/admin/ai/test-prompt
// - User:    POST /api/user/ai/images/generate (stub)
// - Public:  POST /api/public/ai/images/generate (stub)
func RegisterRoutes(r *gin.Engine, db *gorm.DB) {
    // Admin AI routes
    admin := r.Group("/api/admin/ai")
    admin.Use(auth.RequireAdmin(db))

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

        mimeType := fileHeader.Header.Get("Content-Type")
        images, err := gen.Edit(c.Request.Context(), data, finalPrompt, Options{CandidateCount: 1, MimeType: mimeType, Timeout: 60 * time.Second})
        if err != nil || len(images) == 0 {
            c.JSON(http.StatusBadGateway, gin.H{"message": "Image generation failed", "detail": err.Error()})
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
        reqParams := map[string]any{
            "model":          "gemini-2.5-flash-image-preview",
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

    // POST /api/admin/ai/image-edit
    // Multipart form with parts:
    // - image: file (required)
    // - request: JSON (Kotlin CreateImageEditRequest); we accept also {prompt, n}
    // - provider: OPENAI|GOOGLE|FLUX (query or form), default GOOGLE(Gemini)
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
        mimeType := fileHeader.Header.Get("Content-Type")
        images, err := gen.Edit(c.Request.Context(), data, req.Prompt, Options{CandidateCount: req.N, MimeType: mimeType, Timeout: 60 * time.Second})
        if err != nil || len(images) == 0 {
            c.JSON(http.StatusBadGateway, gin.H{"message": "Image edit failed", "detail": err.Error()})
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
        c.JSON(http.StatusNotImplemented, gin.H{"message": "User image generation not implemented in Go backend"})
    })

    // Public AI routes (stub)
    pub := r.Group("/api/public/ai/images")
    pub.POST("/generate", func(c *gin.Context) {
        c.JSON(http.StatusNotImplemented, gin.H{"message": "Public image generation not implemented in Go backend"})
    })
}
