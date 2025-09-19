package utility

import (
	"crypto/tls"
	"errors"
	"io"
	"strings"
	"testing"
	"time"
)

type stubDialer struct {
	client      *stubClient
	connectErr  error
	lastAddr    string
	lastTimeout time.Duration
	lastSecure  bool
	lastTLS     *tls.Config
}

func (s *stubDialer) Connect(addr string, timeout time.Duration, secure bool, tlsConfig *tls.Config) (ftpClient, error) {
	s.lastAddr = addr
	s.lastTimeout = timeout
	s.lastSecure = secure
	s.lastTLS = tlsConfig
	if s.connectErr != nil {
		return nil, s.connectErr
	}
	if s.client == nil {
		s.client = &stubClient{}
	}
	return s.client, nil
}

type stubClient struct {
	loginErr   error
	storErr    error
	quitErr    error
	quitCount  int
	lastUser   string
	stored     []byte
	storedPath string
}

func (s *stubClient) Login(user, password string) error {
	s.lastUser = user
	return s.loginErr
}

func (s *stubClient) Stor(path string, r io.Reader) error {
	s.storedPath = path
	data, err := io.ReadAll(r)
	if err != nil {
		return err
	}
	s.stored = data
	return s.storErr
}

func (s *stubClient) Quit() error {
	s.quitCount++
	return s.quitErr
}

func withStubDialer(t *testing.T, dialer *stubDialer) {
	prev := ftpDialer
	ftpDialer = dialer
	t.Cleanup(func() { ftpDialer = prev })
}

func TestUploadPDFToFTPSuccess(t *testing.T) {
	client := &stubClient{}
	dialer := &stubDialer{client: client}
	withStubDialer(t, dialer)

	pdfData := []byte("pdf data")
	err := UploadPDFToFTP(pdfData, "ftp.example.com", "user", "pass", &FTPUploadOptions{RemotePath: "order-123.pdf", Timeout: 2 * time.Second})
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}

	if dialer.lastAddr != "ftp.example.com:21" {
		t.Fatalf("expected addr ftp.example.com:21, got %s", dialer.lastAddr)
	}
	if dialer.lastTimeout != 2*time.Second {
		t.Fatalf("expected timeout 2s, got %v", dialer.lastTimeout)
	}
	if dialer.lastSecure {
		t.Fatalf("expected secure false")
	}
	if client.storedPath != "order-123.pdf" {
		t.Fatalf("expected remote path order-123.pdf, got %s", client.storedPath)
	}
	if string(client.stored) != "pdf data" {
		t.Fatalf("expected stored pdf data, got %q", string(client.stored))
	}
	if client.quitCount == 0 {
		t.Fatalf("expected quit to be called")
	}
}

func TestUploadPDFToFTPConnectError(t *testing.T) {
	dialer := &stubDialer{connectErr: errors.New("dial failed")}
	withStubDialer(t, dialer)
	err := UploadPDFToFTP([]byte("data"), "ftp.example.com", "user", "pass", &FTPUploadOptions{RemotePath: "file.pdf"})
	if err == nil {
		t.Fatal("expected error")
	}
	if !strings.Contains(err.Error(), "connect failed") {
		t.Fatalf("expected connect error, got %v", err)
	}
}

func TestUploadPDFToFTPLoginError(t *testing.T) {
	client := &stubClient{loginErr: errors.New("bad creds")}
	dialer := &stubDialer{client: client}
	withStubDialer(t, dialer)
	err := UploadPDFToFTP([]byte("data"), "ftp.example.com", "user", "pass", &FTPUploadOptions{RemotePath: "file.pdf"})
	if err == nil {
		t.Fatal("expected error")
	}
	if !strings.Contains(err.Error(), "login failed") {
		t.Fatalf("expected login error, got %v", err)
	}
	if client.quitCount == 0 {
		t.Fatalf("expected quit even on login failure")
	}
}

func TestUploadPDFToFTPStorError(t *testing.T) {
	client := &stubClient{storErr: errors.New("disk full")}
	dialer := &stubDialer{client: client}
	withStubDialer(t, dialer)
	err := UploadPDFToFTP([]byte("data"), "ftp.example.com", "user", "pass", &FTPUploadOptions{RemotePath: "file.pdf"})
	if err == nil {
		t.Fatal("expected error")
	}
	if !strings.Contains(err.Error(), "upload failed") {
		t.Fatalf("expected upload error, got %v", err)
	}
	if client.quitCount == 0 {
		t.Fatalf("expected quit on stor failure")
	}
}

func TestUploadPDFToFTPSecureFromScheme(t *testing.T) {
	client := &stubClient{}
	dialer := &stubDialer{client: client}
	withStubDialer(t, dialer)
	err := UploadPDFToFTP([]byte("data"), "ftps://secure.example.com", "user", "pass", &FTPUploadOptions{RemotePath: "file.pdf"})
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if !dialer.lastSecure {
		t.Fatalf("expected secure true")
	}
	if dialer.lastTLS == nil {
		t.Fatalf("expected tls config to be set")
	}
	if dialer.lastTLS.ServerName != "secure.example.com" {
		t.Fatalf("expected server name to be secure.example.com, got %s", dialer.lastTLS.ServerName)
	}
}

func TestUploadPDFToFTPExplicitTLSOverride(t *testing.T) {
	client := &stubClient{}
	dialer := &stubDialer{client: client}
	withStubDialer(t, dialer)
	err := UploadPDFToFTP([]byte("data"), "ftp.example.com", "user", "pass", &FTPUploadOptions{RemotePath: "file.pdf", ExplicitTLS: true})
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if !dialer.lastSecure {
		t.Fatalf("expected secure true due to ExplicitTLS option")
	}
}

func TestUploadPDFToFTPRequiresRemotePath(t *testing.T) {
	err := UploadPDFToFTP([]byte("data"), "ftp.example.com", "user", "pass", nil)
	if err == nil {
		t.Fatal("expected error when remote path missing")
	}
}

func TestNormalizeFTPServerDefaultsPort(t *testing.T) {
	addr, secure, host, err := normalizeFTPServer("ftp.example.com")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if addr != "ftp.example.com:21" {
		t.Fatalf("expected addr ftp.example.com:21, got %s", addr)
	}
	if secure {
		t.Fatalf("expected secure false")
	}
	if host != "ftp.example.com" {
		t.Fatalf("expected host ftp.example.com, got %s", host)
	}
}
