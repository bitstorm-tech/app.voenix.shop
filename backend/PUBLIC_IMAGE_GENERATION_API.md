# Public Image Generation API

## Overview
A public API endpoint has been created to allow users to generate AI images.

## Endpoint
`POST /api/public/images/generate`

### Request
- **Method**: POST
- **Content-Type**: multipart/form-data
- **Parameters**:
  - `image` (required): The image file to edit (MultipartFile)
  - `promptId` (required): The ID of the prompt to use (Long)
  - `background` (optional): Background setting - AUTO, CUTOUT_SUBJECT, PRODUCT (default: AUTO)
  - `quality` (optional): Image quality - LOW, STANDARD, HIGH (default: LOW)  
  - `size` (optional): Image size - various options (default: LANDSCAPE_1536X1024)

### Response
```json
{
  "imageUrls": ["http://localhost:8080/api/public/images/uuid-filename.png"]
}
```

### Error Responses
- `400 Bad Request`: Invalid request parameters or inactive prompt
- `404 Not Found`: Prompt not found
- `500 Internal Server Error`: Server-side error during image generation

## Additional Endpoint
`GET /api/public/images/{filename}`
- Retrieve generated images by filename
- Returns the image with appropriate content type

## Implementation Details

### Files Created:
1. **DTOs**:
   - `PublicImageGenerationRequest.kt` - Request DTO
   - `PublicImageGenerationResponse.kt` - Response DTO

2. **Services**:
   - `PublicImageGenerationService.kt` - Public image generation logic

3. **Controller**:
   - `PublicImageController.kt` - REST endpoints

4. **Exception**:
   - `BadRequestException.kt` - For 400 errors

5. **Tests**:

### Configuration Changes:
- Updated `SecurityConfig.kt` to allow `/api/public/**` endpoints
- Added `app.base-url` property to `application.properties`
- Updated `GlobalExceptionHandler.kt` to handle `BadRequestException`

### Security Considerations:
- File validation: Max 10MB, only image formats (JPEG, PNG, WebP)
- Prompt validation: Only active prompts can be used
- Images generated are stored as PRIVATE type
