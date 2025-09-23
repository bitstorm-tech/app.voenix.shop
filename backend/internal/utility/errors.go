package utility

// SafeError returns err.Error() when err is non-nil, otherwise an empty string.
// It helps simplify logging or response payloads that should omit absent errors.
func SafeError(err error) string {
	if err == nil {
		return ""
	}
	return err.Error()
}
