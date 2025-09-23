package utility

// DerefPointer returns the value that p points to, or def when p is nil.
// It is a lightweight helper to avoid repetitive nil checks when dealing with optional fields.
func DerefPointer[T any](p *T, def T) T {
	if p == nil {
		return def
	}
	return *p
}

// StringPointerNonEmpty returns a pointer to s unless it is an empty string, in which case it returns nil.
// This is useful when preparing optional string fields that should be omitted when blank.
func StringPointerNonEmpty(s string) *string {
	if s == "" {
		return nil
	}
	return &s
}
