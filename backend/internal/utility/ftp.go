package utility

import (
	"bytes"
	"crypto/tls"
	"errors"
	"fmt"
	"io"
	"net"
	"strings"
	"time"

	ftp "github.com/jlaffaye/ftp"
)

const defaultFTPTimeout = 10 * time.Second

// FTPUploadOptions controls optional behaviours for UploadPDFToFTP.
type FTPUploadOptions struct {
	// Timeout overrides the default dial and command timeout. Zero or negative uses default.
	Timeout time.Duration
	// RemotePath is the destination path (including filename) on the FTP server.
	RemotePath string
	// ExplicitTLS forces an explicit TLS upgrade even if the server string is not ftps://.
	ExplicitTLS bool
	// TLSConfig optionally provides a custom TLS configuration for explicit TLS connections.
	TLSConfig *tls.Config
}

type ftpClient interface {
	Login(user, password string) error
	Stor(path string, r io.Reader) error
	Quit() error
}

type ftpConnector interface {
	Connect(addr string, timeout time.Duration, secure bool, tlsConfig *tls.Config) (ftpClient, error)
}

type realFTPDialer struct{}

func (realFTPDialer) Connect(addr string, timeout time.Duration, secure bool, tlsConfig *tls.Config) (ftpClient, error) {
	options := []ftp.DialOption{ftp.DialWithTimeout(timeout)}
	if secure {
		cfg := tlsConfig
		if cfg == nil {
			cfg = &tls.Config{MinVersion: tls.VersionTLS12}
		}
		options = append(options, ftp.DialWithExplicitTLS(cfg))
	}
	return ftp.Dial(addr, options...)
}

var ftpDialer ftpConnector = realFTPDialer{}

// UploadPDFToFTP uploads the provided PDF bytes to the configured FTP/FTPS server.
// The upload occurs synchronously and returns an error when the transfer fails.
func UploadPDFToFTP(pdf []byte, server, user, password string, opts *FTPUploadOptions) error {
	trimmed := strings.TrimSpace(server)
	if trimmed == "" {
		return errors.New("ftp server is required")
	}

	cfg := FTPUploadOptions{}
	if opts != nil {
		cfg = *opts
	}

	path := strings.TrimSpace(cfg.RemotePath)
	if path == "" {
		return errors.New("ftp remote path is required")
	}

	timeout := cfg.Timeout
	if timeout <= 0 {
		timeout = defaultFTPTimeout
	}

	addr, secure, host, err := normalizeFTPServer(trimmed)
	if err != nil {
		return err
	}
	if cfg.ExplicitTLS {
		secure = true
	}

	tlsConfig := cfg.TLSConfig
	if secure {
		if tlsConfig == nil {
			tlsConfig = &tls.Config{MinVersion: tls.VersionTLS12, ServerName: host}
		} else if tlsConfig.ServerName == "" {
			// Provide ServerName when missing to avoid handshake failures on SNI servers.
			dup := tlsConfig.Clone()
			dup.ServerName = host
			tlsConfig = dup
		}
	}

	client, err := ftpDialer.Connect(addr, timeout, secure, tlsConfig)
	if err != nil {
		return fmt.Errorf("ftp connect failed: %w", err)
	}
	defer func() {
		_ = client.Quit()
	}()

	if err := client.Login(user, password); err != nil {
		return fmt.Errorf("ftp login failed: %w", err)
	}

	reader := bytes.NewReader(pdf)
	if err := client.Stor(path, reader); err != nil {
		return fmt.Errorf("ftp upload failed: %w", err)
	}

	return nil
}

func normalizeFTPServer(server string) (addr string, secure bool, host string, err error) {
	trimmed := strings.TrimSpace(server)
	lower := strings.ToLower(trimmed)
	secure = false
	switch {
	case strings.HasPrefix(lower, "ftps://"):
		secure = true
		trimmed = trimmed[7:]
	case strings.HasPrefix(lower, "ftp://"):
		trimmed = trimmed[6:]
	}

	if trimmed == "" {
		return "", false, "", errors.New("ftp server host is empty")
	}

	if !strings.Contains(trimmed, ":") {
		trimmed += ":21"
	}

	host, _, splitErr := net.SplitHostPort(trimmed)
	if splitErr != nil {
		return "", false, "", fmt.Errorf("invalid ftp server: %w", splitErr)
	}

	return trimmed, secure, host, nil
}
