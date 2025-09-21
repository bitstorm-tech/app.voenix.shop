package utility

import (
	"bytes"
	"errors"
	"fmt"
	"io"
	"net"
	"os"
	"path"
	"path/filepath"
	"strings"
	"time"

	"github.com/pkg/sftp"
	"golang.org/x/crypto/ssh"
	"golang.org/x/crypto/ssh/knownhosts"
)

const defaultSFTPTimeout = 10 * time.Second

// FTPUploadOptions controls behaviours for UploadPDFToFTP.
type FTPUploadOptions struct {
	Timeout                         time.Duration
	RemotePath                      string
	InsecureSkipHostKeyVerification bool
	KnownHostsPath                  string
	FallbackFileName                string
}

type sftpClient interface {
	Stat(string) (os.FileInfo, error)
	MkdirAll(string) error
	OpenFile(string, int) (io.WriteCloser, error)
	Close() error
}

var newSFTPClient = func(address string, config *ssh.ClientConfig) (sftpClient, error) {
	sshClient, err := ssh.Dial("tcp", address, config)
	if err != nil {
		return nil, fmt.Errorf("ssh dial failed: %w", err)
	}

	client, err := sftp.NewClient(sshClient)
	if err != nil {
		closeErr := sshClient.Close()
		if closeErr != nil {
			err = fmt.Errorf("create sftp client failed: %w (closing ssh: %v)", err, closeErr)
		} else {
			err = fmt.Errorf("create sftp client failed: %w", err)
		}
		return nil, err
	}

	return &realSFTPClient{client: client, sshClient: sshClient}, nil
}

type realSFTPClient struct {
	client    *sftp.Client
	sshClient *ssh.Client
}

func (c *realSFTPClient) Stat(path string) (os.FileInfo, error) {
	return c.client.Stat(path)
}

func (c *realSFTPClient) MkdirAll(path string) error {
	return c.client.MkdirAll(path)
}

func (c *realSFTPClient) OpenFile(path string, flag int) (io.WriteCloser, error) {
	return c.client.OpenFile(path, flag)
}

func (c *realSFTPClient) Close() error {
	sftpErr := c.client.Close()
	sshErr := c.sshClient.Close()
	if sftpErr != nil {
		return sftpErr
	}
	return sshErr
}

// UploadPDFToFTP uploads the provided PDF bytes to an SFTP server.
func UploadPDFToFTP(pdf []byte, server, user, password string, opts *FTPUploadOptions) error {
	trimmedServer := strings.TrimSpace(server)
	if trimmedServer == "" {
		return errors.New("sftp server is required")
	}

	if user = strings.TrimSpace(user); user == "" {
		return errors.New("sftp user is required")
	}
	if password = strings.TrimSpace(password); password == "" {
		return errors.New("sftp password is required")
	}

	configuration := FTPUploadOptions{}
	if opts != nil {
		configuration = *opts
	}

	remotePath := strings.TrimSpace(configuration.RemotePath)
	if remotePath == "" {
		return errors.New("sftp remote path is required")
	}

	timeout := configuration.Timeout
	if timeout <= 0 {
		timeout = defaultSFTPTimeout
	}

	address, err := normalizeSFTPServer(trimmedServer)
	if err != nil {
		return err
	}

	sshConfig, err := buildSSHClientConfig(user, password, timeout, configuration)
	if err != nil {
		return err
	}

	client, err := newSFTPClient(address, sshConfig)
	if err != nil {
		return err
	}
	defer func() {
		_ = client.Close()
	}()

	defaultFileName := strings.TrimSpace(configuration.FallbackFileName)
	if defaultFileName == "" {
		defaultFileName = fmt.Sprintf("upload-%d.pdf", time.Now().Unix())
	}

	remoteFilePath, err := resolveRemoteFilePath(client, remotePath, defaultFileName)
	if err != nil {
		return err
	}

	remoteFile, err := client.OpenFile(remoteFilePath, os.O_WRONLY|os.O_CREATE|os.O_TRUNC)
	if err != nil {
		return fmt.Errorf("open remote file %q: %w", remoteFilePath, err)
	}

	_, copyErr := io.Copy(remoteFile, bytes.NewReader(pdf))
	closeErr := remoteFile.Close()
	if copyErr != nil {
		return fmt.Errorf("write remote file %q: %w", remoteFilePath, copyErr)
	}
	if closeErr != nil {
		return fmt.Errorf("close remote file %q: %w", remoteFilePath, closeErr)
	}

	return nil
}

func normalizeSFTPServer(server string) (string, error) {
	trimmed := strings.TrimSpace(server)
	lower := strings.ToLower(trimmed)

	switch {
	case strings.HasPrefix(lower, "sftp://"):
		trimmed = trimmed[7:]
	case strings.HasPrefix(lower, "ssh://"):
		trimmed = trimmed[6:]
	}

	if trimmed == "" {
		return "", errors.New("sftp server host is empty")
	}

	if !strings.Contains(trimmed, ":") {
		trimmed += ":22"
	}

	_, _, splitErr := net.SplitHostPort(trimmed)
	if splitErr != nil {
		return "", fmt.Errorf("invalid sftp server: %w", splitErr)
	}

	return trimmed, nil
}

func buildSSHClientConfig(user, password string, timeout time.Duration, opts FTPUploadOptions) (*ssh.ClientConfig, error) {
	hostKeyCallback, err := selectHostKeyCallback(opts)
	if err != nil {
		return nil, err
	}

	return &ssh.ClientConfig{
		User:            user,
		Auth:            []ssh.AuthMethod{ssh.Password(password)},
		HostKeyCallback: hostKeyCallback,
		Timeout:         timeout,
	}, nil
}

func selectHostKeyCallback(opts FTPUploadOptions) (ssh.HostKeyCallback, error) {
	if opts.InsecureSkipHostKeyVerification {
		return ssh.InsecureIgnoreHostKey(), nil
	}

	knownHostsPath := strings.TrimSpace(opts.KnownHostsPath)
	if knownHostsPath == "" {
		home, err := os.UserHomeDir()
		if err != nil {
			return nil, fmt.Errorf("determine user home directory: %w", err)
		}
		knownHostsPath = filepath.Join(home, ".ssh", "known_hosts")
	}

	callback, err := knownhosts.New(knownHostsPath)
	if err != nil {
		return nil, fmt.Errorf("load known hosts from %q: %w", knownHostsPath, err)
	}

	return callback, nil
}

func resolveRemoteFilePath(client sftpClient, remotePath string, fallbackFileName string) (string, error) {
	if client == nil {
		return "", errors.New("nil sftp client when resolving remote path")
	}

	trimmedRemote := strings.TrimSpace(remotePath)
	pathLooksLikeDirectory := strings.HasSuffix(trimmedRemote, "/")

	cleanRemote := path.Clean(trimmedRemote)
	if cleanRemote == "." {
		cleanRemote = ""
	}

	remoteIndicatesDirectory := pathLooksLikeDirectory || cleanRemote == ""
	if cleanRemote == "/" {
		remoteIndicatesDirectory = true
	}

	if !remoteIndicatesDirectory {
		info, err := client.Stat(cleanRemote)
		switch {
		case err == nil:
			if info.IsDir() {
				remoteIndicatesDirectory = true
			}
		case isNotExistError(err):
			// Directory will be created later.
		default:
			return "", fmt.Errorf("stat remote path %q: %w", cleanRemote, err)
		}
	}

	if remoteIndicatesDirectory {
		if fallbackFileName == "" {
			return "", fmt.Errorf("remote path %q refers to a directory and no fallback file name provided", remotePath)
		}
		directory := strings.TrimSuffix(cleanRemote, "/")
		if directory == "" || directory == "." {
			return fallbackFileName, nil
		}
		if err := client.MkdirAll(directory); err != nil {
			return "", fmt.Errorf("ensure remote directory %q: %w", directory, err)
		}
		return path.Join(directory, fallbackFileName), nil
	}

	directory := path.Dir(cleanRemote)
	if directory != "." && directory != "/" && directory != "" {
		if err := client.MkdirAll(directory); err != nil {
			return "", fmt.Errorf("ensure remote directory %q: %w", directory, err)
		}
	}

	return cleanRemote, nil
}

func isNotExistError(err error) bool {
	if err == nil {
		return false
	}
	if errors.Is(err, os.ErrNotExist) {
		return true
	}
	var statusError *sftp.StatusError
	if errors.As(err, &statusError) {
		return errors.Is(statusError.FxCode(), sftp.ErrSSHFxNoSuchFile)
	}
	return false
}

var _ sftpClient = (*realSFTPClient)(nil)
