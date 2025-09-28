package image

import (
	"encoding/json"
	"errors"
	"strconv"
)

// ParseUploadRequest parses either a JSON string request or discrete form fields
// into an UploadRequest. Returns error on invalid input.
func ParseUploadRequest(requestJSON string, imageTypeField string, cropX, cropY, cropW, cropH *string) (UploadRequest, error) {
	if requestJSON != "" {
		var m map[string]any
		if err := json.Unmarshal([]byte(requestJSON), &m); err != nil {
			return UploadRequest{}, errors.New("invalid JSON in 'request' part")
		}
		var imageType string
		if v, ok := m["imageType"].(string); ok && v != "" {
			imageType = v
		} else if v, ok := m["type"].(string); ok && v != "" {
			imageType = v
		}
		var crop *CropArea
		if v, ok := m["cropArea"].(map[string]any); ok {
			cx, cxok := toFloat(v["x"])
			cy, cyok := toFloat(v["y"])
			cw, cwok := toFloat(v["width"])
			ch, chok := toFloat(v["height"])
			if cxok && cyok && cwok && chok {
				crop = &CropArea{X: cx, Y: cy, Width: cw, Height: ch}
			}
		}
		return UploadRequest{ImageType: imageType, CropArea: crop}, nil
	}

	if imageTypeField == "" {
		return UploadRequest{}, errors.New("missing imageType")
	}

	var crop *CropArea
	if cropX != nil && cropY != nil && cropW != nil && cropH != nil {
		if *cropX != "" && *cropY != "" && *cropW != "" && *cropH != "" {
			if x, err1 := strconv.ParseFloat(*cropX, 64); err1 == nil {
				if y, err2 := strconv.ParseFloat(*cropY, 64); err2 == nil {
					if w, err3 := strconv.ParseFloat(*cropW, 64); err3 == nil {
						if h, err4 := strconv.ParseFloat(*cropH, 64); err4 == nil {
							crop = &CropArea{X: x, Y: y, Width: w, Height: h}
						}
					}
				}
			}
		}
	}
	return UploadRequest{ImageType: imageTypeField, CropArea: crop}, nil
}

func toFloat(v any) (float64, bool) {
	switch t := v.(type) {
	case float64:
		return t, true
	case float32:
		return float64(t), true
	case int:
		return float64(t), true
	case int64:
		return float64(t), true
	case json.Number:
		x, err := t.Float64()
		return x, err == nil
	default:
		return 0, false
	}
}
