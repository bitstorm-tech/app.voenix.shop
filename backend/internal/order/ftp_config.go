package order

import (
	"errors"
	"fmt"
	"path"
	"strconv"
	"strings"
	"time"

	"voenix/backend/internal/utility"
)

const orderPDFRemoteDir = "orders"

var (
	errOrderPDFFTPConfigMissing = errors.New("order PDF FTP configuration missing")
	uploadPDFToFTP              = utility.UploadPDFToFTP
)

type orderPDFFTPConfig struct {
	Server   string
	User     string
	Password string
	Timeout  time.Duration
}

func loadOrderPDFFTPConfig(getenv func(string) string) (orderPDFFTPConfig, error) {
	server := strings.TrimSpace(getenv("ORDER_PDF_FTP_SERVER"))
	user := strings.TrimSpace(getenv("ORDER_PDF_FTP_USER"))
	password := strings.TrimSpace(getenv("ORDER_PDF_FTP_PASSWORD"))

	if server == "" || user == "" || password == "" {
		return orderPDFFTPConfig{}, errOrderPDFFTPConfigMissing
	}

	cfg := orderPDFFTPConfig{
		Server:   server,
		User:     user,
		Password: password,
	}

	if timeoutString := strings.TrimSpace(getenv("ORDER_PDF_FTP_TIMEOUT")); timeoutString != "" {
		seconds, err := strconv.Atoi(timeoutString)
		if err != nil {
			return orderPDFFTPConfig{}, fmt.Errorf("invalid ORDER_PDF_FTP_TIMEOUT: %w", err)
		}
		if seconds < 0 {
			return orderPDFFTPConfig{}, fmt.Errorf("ORDER_PDF_FTP_TIMEOUT must be >= 0")
		}
		cfg.Timeout = time.Duration(seconds) * time.Second
	}

	return cfg, nil
}

func (cfg orderPDFFTPConfig) options(remotePath string) *utility.FTPUploadOptions {
	return &utility.FTPUploadOptions{
		RemotePath: remotePath,
		Timeout:    cfg.Timeout,
	}
}

func remoteOrderPDFPath(filename string) string {
	base := strings.TrimSpace(filename)
	if base == "" {
		base = "order.pdf"
	}
	// Ensure forward slash separators for FTP regardless of platform.
	return path.Join(orderPDFRemoteDir, base)
}
