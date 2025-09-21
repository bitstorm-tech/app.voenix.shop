package main

import (
	"bytes"
	"crypto/tls"
	"errors"
	"fmt"
	"log"
	"net"
	"os"
	"strings"
	"time"

	ftp "github.com/jlaffaye/ftp"
)

const (
	ftpServer                = "ftps://example.com:21"
	ftpUser                  = "username"
	ftpPassword              = "password"
	ftpRemotePath            = "/remote/path/sample.pdf"
	ftpTimeoutSeconds        = 10
	ftpExplicitTLS           = true
	ftpInsecureSkipVerifyTLS = false
	localFilePath            = ""
)

var inlinePayload = []byte("%PDF-1.4\n% Debug FTP client sample PDF\n1 0 obj<<>>endobj\nstartxref\n0\n%%EOF\n")

type clientConfig struct {
	server                string
	user                  string
	password              string
	remotePath            string
	timeout               time.Duration
	explicitTLS           bool
	insecureSkipVerifyTLS bool
	localFile             string
}

func main() {
	log.SetFlags(log.LstdFlags | log.Lmicroseconds | log.Lshortfile)
	cfg := clientConfig{
		server:                ftpServer,
		user:                  ftpUser,
		password:              ftpPassword,
		remotePath:            ftpRemotePath,
		timeout:               time.Duration(ftpTimeoutSeconds) * time.Second,
		explicitTLS:           ftpExplicitTLS,
		insecureSkipVerifyTLS: ftpInsecureSkipVerifyTLS,
		localFile:             localFilePath,
	}
	log.Printf("starting ftp debug client with config: server=%q user=%q password_len=%d remotePath=%q timeout=%s explicitTLS=%t insecureSkipVerifyTLS=%t localFile=%q",
		cfg.server, cfg.user, len(cfg.password), cfg.remotePath, cfg.timeout, cfg.explicitTLS, cfg.insecureSkipVerifyTLS, cfg.localFile)

	if err := run(cfg); err != nil {
		log.Fatalf("ftp debug client failed: %v", err)
	}
	log.Println("ftp debug client finished successfully")
}

func run(cfg clientConfig) error {
	addr, secure, host, err := normalizeFTPServer(cfg.server)
	if err != nil {
		return fmt.Errorf("normalize ftp server: %w", err)
	}
	log.Printf("normalized server: addr=%q secure=%t host=%q", addr, secure, host)

	if cfg.explicitTLS {
		secure = true
	}
	log.Printf("effective secure setting: %t", secure)

	tlsConfig := (*tls.Config)(nil)
	if secure {
		tlsConfig = &tls.Config{MinVersion: tls.VersionTLS12, ServerName: host, InsecureSkipVerify: cfg.insecureSkipVerifyTLS}
		log.Printf("constructed TLS config: minVersion=TLS1.2 serverName=%q insecureSkipVerify=%t", tlsConfig.ServerName, tlsConfig.InsecureSkipVerify)
	}

	timeout := cfg.timeout
	if timeout <= 0 {
		timeout = 10 * time.Second
		log.Printf("timeout not provided or invalid, defaulting to %s", timeout)
	}

	dialOptions := []ftp.DialOption{ftp.DialWithTimeout(timeout)}
	if secure {
		dialOptions = append(dialOptions, ftp.DialWithExplicitTLS(tlsConfig))
	}
	log.Printf("dial options configured: count=%d", len(dialOptions))

	log.Println("dialing ftp server...")
	client, err := ftp.Dial(addr, dialOptions...)
	if err != nil {
		return fmt.Errorf("ftp dial: %w", err)
	}
	log.Println("ftp dial successful")
	defer func() {
		log.Println("closing ftp connection")
		if quitErr := client.Quit(); quitErr != nil {
			log.Printf("ftp quit error: %v", quitErr)
		} else {
			log.Println("ftp connection closed cleanly")
		}
	}()

	log.Println("attempting login")
	if err := client.Login(cfg.user, cfg.password); err != nil {
		return fmt.Errorf("ftp login: %w", err)
	}
	log.Println("login successful")

	if err := dumpServerInfo(client); err != nil {
		log.Printf("failed to gather server info: %v", err)
	}

	payload, description, err := loadPayload(cfg)
	if err != nil {
		return fmt.Errorf("load payload: %w", err)
	}
	log.Printf("payload ready: %s (%d bytes)", description, len(payload))

	reader := bytes.NewReader(payload)
	log.Printf("starting upload to remote path %q", cfg.remotePath)
	if err := client.Stor(cfg.remotePath, reader); err != nil {
		return fmt.Errorf("ftp stor: %w", err)
	}
	log.Println("upload completed successfully")

	if err := client.NoOp(); err != nil {
		log.Printf("noop command failed: %v", err)
	} else {
		log.Println("noop command succeeded, server responsive")
	}

	return nil
}

func loadPayload(cfg clientConfig) ([]byte, string, error) {
	if strings.TrimSpace(cfg.localFile) == "" {
		log.Println("no local file configured, using inline payload")
		return inlinePayload, "inline payload", nil
	}

	path := strings.TrimSpace(cfg.localFile)
	log.Printf("loading local file payload from %q", path)
	data, err := os.ReadFile(path)
	if err != nil {
		return nil, "", fmt.Errorf("read file %q: %w", path, err)
	}
	return data, fmt.Sprintf("file %s", path), nil
}

func dumpServerInfo(client *ftp.ServerConn) error {
	if client == nil {
		return errors.New("nil ftp client")
	}

	log.Println("retrieving current directory")
	dir, err := client.CurrentDir()
	if err != nil {
		return fmt.Errorf("current dir: %w", err)
	}
	log.Printf("current directory: %q", dir)

	log.Println("listing entries in current directory")
	entries, err := client.List(".")
	if err != nil {
		return fmt.Errorf("list current directory: %w", err)
	}
	limit := len(entries)
	if limit > 20 {
		limit = 20
	}
	for index := 0; index < limit; index++ {
		entry := entries[index]
		log.Printf("entry[%d]: name=%q type=%v size=%d time=%s target=%q", index, entry.Name, entry.Type, entry.Size, entry.Time.Format(time.RFC3339), entry.Target)
	}
	log.Printf("total directory entries retrieved: %d", len(entries))

	log.Println("sending NOOP command for responsiveness check")
	if err := client.NoOp(); err != nil {
		return fmt.Errorf("noop: %w", err)
	}
	log.Println("noop succeeded during server info collection")

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
