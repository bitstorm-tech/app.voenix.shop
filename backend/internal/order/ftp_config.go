package order

import (
	"errors"
	"fmt"
	"path"
	"strconv"
	"strings"
	"time"
	"unicode"

	"voenix/backend/internal/utility"
)

var (
	errOrderPDFFTPConfigMissing = errors.New("order PDF FTP configuration missing")
	uploadPDFToFTP              = utility.UploadPDFToFTP
)

type orderPDFFTPConfig struct {
	Name                            string
	Server                          string
	Folder                          string
	User                            string
	Password                        string
	Timeout                         time.Duration
	InsecureSkipHostKeyVerification bool
	KnownHostsPath                  string
	FallbackFileName                string
}

func loadOrderPDFFTPConfigs(getenv func(string) string) ([]orderPDFFTPConfig, error) {
	configNamesValue := strings.TrimSpace(getenv("ORDER_PDF_FTP_CONFIGS"))
	if configNamesValue == "" {
		return nil, errOrderPDFFTPConfigMissing
	}

	configNames := splitAndClean(configNamesValue)
	if len(configNames) == 0 {
		return nil, errOrderPDFFTPConfigMissing
	}

	uniqueNames := make(map[string]struct{}, len(configNames))
	configs := make([]orderPDFFTPConfig, 0, len(configNames))
	for _, configName := range configNames {
		if _, exists := uniqueNames[configName]; exists {
			return nil, fmt.Errorf("duplicate FTP config name %q", configName)
		}
		uniqueNames[configName] = struct{}{}
		normalized := normalizeConfigEnvName(configName)
		config, err := buildOrderPDFFTPConfig(getenv, configName, normalized)
		if err != nil {
			return nil, fmt.Errorf("load FTP config %q: %w", configName, err)
		}
		configs = append(configs, config)
	}

	return configs, nil
}

func (config orderPDFFTPConfig) options(remotePath string) *utility.FTPUploadOptions {
	return &utility.FTPUploadOptions{
		RemotePath:                      remotePath,
		Timeout:                         config.Timeout,
		InsecureSkipHostKeyVerification: config.InsecureSkipHostKeyVerification,
		KnownHostsPath:                  config.KnownHostsPath,
		FallbackFileName:                config.FallbackFileName,
	}
}

func remoteOrderPDFPath(folder string, filename string) string {
	base := strings.TrimSpace(filename)
	if base == "" {
		base = "order.pdf"
	}
	// Ensure forward slash separators for FTP regardless of platform.
	return path.Join(folder, base)
}

func buildOrderPDFFTPConfig(getenv func(string) string, originalName string, normalizedName string) (orderPDFFTPConfig, error) {
	serverKey := keyWithSuffix("ORDER_PDF_FTP_SERVER", normalizedName)
	userKey := keyWithSuffix("ORDER_PDF_FTP_USER", normalizedName)
	passwordKey := keyWithSuffix("ORDER_PDF_FTP_PASSWORD", normalizedName)
	server := strings.TrimSpace(getenv(serverKey))
	user := strings.TrimSpace(getenv(userKey))
	password := strings.TrimSpace(getenv(passwordKey))

	if server == "" || user == "" || password == "" {
		return orderPDFFTPConfig{}, fmt.Errorf("%s, %s, and %s must be set: %w", serverKey, userKey, passwordKey, errOrderPDFFTPConfigMissing)
	}

	config := orderPDFFTPConfig{
		Name:     originalName,
		Server:   server,
		Folder:   strings.TrimSpace(getenv(keyWithSuffix("ORDER_PDF_FTP_FOLDER", normalizedName))),
		User:     user,
		Password: password,
		KnownHostsPath: strings.TrimSpace(
			getenv(keyWithSuffix("ORDER_PDF_FTP_KNOWN_HOSTS_PATH", normalizedName)),
		),
		FallbackFileName: strings.TrimSpace(
			getenv(keyWithSuffix("ORDER_PDF_FTP_FALLBACK_FILENAME", normalizedName)),
		),
	}

	if timeoutValue := strings.TrimSpace(getenv(keyWithSuffix("ORDER_PDF_FTP_TIMEOUT", normalizedName))); timeoutValue != "" {
		seconds, err := strconv.Atoi(timeoutValue)
		if err != nil {
			return orderPDFFTPConfig{}, fmt.Errorf("invalid %s for FTP config %q: %w", keyWithSuffix("ORDER_PDF_FTP_TIMEOUT", normalizedName), originalName, err)
		}
		if seconds < 0 {
			return orderPDFFTPConfig{}, fmt.Errorf("%s must be >= 0 for FTP config %q", keyWithSuffix("ORDER_PDF_FTP_TIMEOUT", normalizedName), originalName)
		}
		config.Timeout = time.Duration(seconds) * time.Second
	}

	if skipValue := strings.TrimSpace(getenv(keyWithSuffix("ORDER_PDF_FTP_SKIP_HOST_KEY_VERIFICATION", normalizedName))); skipValue != "" {
		value, err := strconv.ParseBool(skipValue)
		if err != nil {
			return orderPDFFTPConfig{}, fmt.Errorf("invalid %s for FTP config %q: %w", keyWithSuffix("ORDER_PDF_FTP_SKIP_HOST_KEY_VERIFICATION", normalizedName), originalName, err)
		}
		config.InsecureSkipHostKeyVerification = value
	}

	return config, nil
}

func normalizeConfigEnvName(name string) string {
	trimmed := strings.TrimSpace(name)
	if trimmed == "" {
		return "DEFAULT"
	}

	var builder strings.Builder
	for _, r := range trimmed {
		switch {
		case unicode.IsLetter(r):
			builder.WriteRune(unicode.ToUpper(r))
		case unicode.IsDigit(r):
			builder.WriteRune(r)
		case r == '_':
			builder.WriteRune('_')
		default:
			builder.WriteRune('_')
		}
	}

	normalized := builder.String()
	normalized = strings.Trim(normalized, "_")
	if normalized == "" {
		return "DEFAULT"
	}
	return normalized
}

func keyWithSuffix(base string, suffix string) string {
	if suffix == "" {
		return base
	}
	return fmt.Sprintf("%s_%s", base, suffix)
}

func splitAndClean(value string) []string {
	parts := strings.Split(value, ",")
	configs := make([]string, 0, len(parts))
	for _, part := range parts {
		trimmed := strings.TrimSpace(part)
		if trimmed != "" {
			configs = append(configs, trimmed)
		}
	}
	return configs
}
