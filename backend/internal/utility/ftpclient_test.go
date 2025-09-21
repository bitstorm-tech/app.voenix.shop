package utility

import (
	"bytes"
	"errors"
	"io"
	"os"
	"path"
	"strings"
	"testing"
	"time"

	"golang.org/x/crypto/ssh"
)

type fakeSFTPClient struct {
	statResults         map[string]statResult
	mkdirAllInvocations []string
	mkdirAllErr         error
	openFileErr         error
	openFilePath        string
	writes              bytes.Buffer
	writerCloseErr      error
	closeErr            error
	closed              bool
}

type statResult struct {
	info os.FileInfo
	err  error
}

func (f *fakeSFTPClient) Stat(path string) (os.FileInfo, error) {
	if f.statResults != nil {
		if res, ok := f.statResults[path]; ok {
			return res.info, res.err
		}
	}
	return nil, os.ErrNotExist
}

func (f *fakeSFTPClient) MkdirAll(path string) error {
	f.mkdirAllInvocations = append(f.mkdirAllInvocations, path)
	return f.mkdirAllErr
}

func (f *fakeSFTPClient) OpenFile(path string, _ int) (io.WriteCloser, error) {
	f.openFilePath = path
	if f.openFileErr != nil {
		return nil, f.openFileErr
	}
	return &bufferWriter{buffer: &f.writes, closeErr: f.writerCloseErr}, nil
}

func (f *fakeSFTPClient) Close() error {
	f.closed = true
	return f.closeErr
}

type bufferWriter struct {
	buffer   *bytes.Buffer
	closeErr error
}

func (b *bufferWriter) Write(p []byte) (int, error) {
	return b.buffer.Write(p)
}

func (b *bufferWriter) Close() error {
	return b.closeErr
}

func withStubSFTPClient(t *testing.T, client *fakeSFTPClient, factoryErr error, captureConfig **ssh.ClientConfig, captureAddress *string) {
	previousFactory := newSFTPClient
	newSFTPClient = func(address string, config *ssh.ClientConfig) (sftpClient, error) {
		if captureConfig != nil {
			*captureConfig = config
		}
		if captureAddress != nil {
			*captureAddress = address
		}
		if factoryErr != nil {
			return nil, factoryErr
		}
		return client, nil
	}
	t.Cleanup(func() { newSFTPClient = previousFactory })
}

func TestUploadPDFToSFTPSuccess(t *testing.T) {
	client := &fakeSFTPClient{}
	var gotConfig *ssh.ClientConfig
	var gotAddress string
	withStubSFTPClient(t, client, nil, &gotConfig, &gotAddress)

	payload := []byte("pdf data")
	opts := &FTPUploadOptions{RemotePath: "orders/2025/receipt.pdf", Timeout: 5 * time.Second}
	err := UploadPDFToFTP(payload, "sftp.example.com", "user", "pass", opts)
	if err != nil {
		t.Fatalf("expected success, got error: %v", err)
	}

	if gotAddress != "sftp.example.com:22" {
		t.Fatalf("expected address sftp.example.com:22, got %s", gotAddress)
	}
	if gotConfig == nil {
		t.Fatalf("expected ssh config to be built")
	}
	if gotConfig.Timeout != 5*time.Second {
		t.Fatalf("expected timeout 5s, got %v", gotConfig.Timeout)
	}
	if client.openFilePath != "orders/2025/receipt.pdf" {
		t.Fatalf("expected open file orders/2025/receipt.pdf, got %s", client.openFilePath)
	}
	if client.writes.String() != "pdf data" {
		t.Fatalf("expected uploaded payload to match, got %q", client.writes.String())
	}
	if len(client.mkdirAllInvocations) != 1 || client.mkdirAllInvocations[0] != "orders/2025" {
		t.Fatalf("expected mkdirAll for orders/2025, got %v", client.mkdirAllInvocations)
	}
	if !client.closed {
		t.Fatal("expected client to be closed")
	}
}

func TestUploadPDFToSFTPFactoryError(t *testing.T) {
	factoryErr := errors.New("dial failed")
	withStubSFTPClient(t, nil, factoryErr, nil, nil)

	err := UploadPDFToFTP([]byte("data"), "sftp.example.com", "user", "pass", &FTPUploadOptions{RemotePath: "file.pdf"})
	if err == nil {
		t.Fatal("expected error from factory")
	}
	if !strings.Contains(err.Error(), "dial failed") {
		t.Fatalf("expected dial failed error, got %v", err)
	}
}

func TestUploadPDFToSFTPRemoteDirectoryWithoutFallback(t *testing.T) {
	client := &fakeSFTPClient{}
	withStubSFTPClient(t, client, nil, nil, nil)

	opts := &FTPUploadOptions{RemotePath: "reports/", FallbackFileName: "report.pdf"}
	err := UploadPDFToFTP([]byte("data"), "sftp.example.com", "user", "pass", opts)
	if err != nil {
		t.Fatalf("expected success, got error: %v", err)
	}
	expectedPath := path.Join("reports", "report.pdf")
	if client.openFilePath != expectedPath {
		t.Fatalf("expected open file %s, got %s", expectedPath, client.openFilePath)
	}
}

func TestNormalizeSFTPServerDefaultsPort(t *testing.T) {
	addr, host, err := normalizeSFTPServer("sftp.example.com")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if addr != "sftp.example.com:22" {
		t.Fatalf("expected address sftp.example.com:22, got %s", addr)
	}
	if host != "sftp.example.com" {
		t.Fatalf("expected host sftp.example.com, got %s", host)
	}
}

func TestSelectHostKeyCallbackInsecure(t *testing.T) {
	callback, err := selectHostKeyCallback(FTPUploadOptions{InsecureSkipHostKeyVerification: true})
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if callback == nil {
		t.Fatal("expected callback")
	}
}
