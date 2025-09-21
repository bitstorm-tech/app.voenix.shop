package main

import (
	"bytes"
	"errors"
	"fmt"
	"io"
	"log"
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

const (
	sftpServer                          = ""
	sftpUser                            = ""
	sftpPassword                        = ""
	sftpRemotePath                      = ""
	sftpTimeoutSeconds                  = 10
	sftpInsecureSkipHostKeyVerification = true
	localFilePath                       = ""
)

var inlinePayload = []byte("%PDF-1.4\n% Debug SFTP client sample PDF\n1 0 obj<<>>endobj\nstartxref\n0\n%%EOF\n")

type clientConfig struct {
	server                          string
	user                            string
	password                        string
	remotePath                      string
	timeout                         time.Duration
	insecureSkipHostKeyVerification bool
	localFile                       string
}

func main() {
	log.SetFlags(log.LstdFlags | log.Lmicroseconds | log.Lshortfile)
	configuration := clientConfig{
		server:                          sftpServer,
		user:                            sftpUser,
		password:                        sftpPassword,
		remotePath:                      sftpRemotePath,
		timeout:                         time.Duration(sftpTimeoutSeconds) * time.Second,
		insecureSkipHostKeyVerification: sftpInsecureSkipHostKeyVerification,
		localFile:                       localFilePath,
	}

	log.Printf("starting sftp debug client with config: server=%q user=%q password_len=%d remotePath=%q timeout=%s insecureSkipHostKeyVerification=%t localFile=%q",
		configuration.server, configuration.user, len(configuration.password), configuration.remotePath, configuration.timeout, configuration.insecureSkipHostKeyVerification, configuration.localFile)

	if err := run(configuration); err != nil {
		log.Fatalf("sftp debug client failed: %v", err)
	}
	log.Println("sftp debug client finished successfully")
}

func run(configuration clientConfig) error {
	address, host, err := normalizeSFTPServer(configuration.server)
	if err != nil {
		return fmt.Errorf("normalize sftp server: %w", err)
	}
	log.Printf("normalized server: addr=%q host=%q", address, host)

	sshConfiguration, err := buildSSHClientConfig(configuration)
	if err != nil {
		return fmt.Errorf("build ssh config: %w", err)
	}

	log.Println("dialing sftp server...")
	sshClient, err := ssh.Dial("tcp", address, sshConfiguration)
	if err != nil {
		return fmt.Errorf("ssh dial: %w", err)
	}
	log.Println("ssh dial successful")
	defer func() {
		log.Println("closing ssh client")
		if closeError := sshClient.Close(); closeError != nil {
			log.Printf("ssh close error: %v", closeError)
		} else {
			log.Println("ssh client closed cleanly")
		}
	}()

	sftpClient, err := sftp.NewClient(sshClient)
	if err != nil {
		return fmt.Errorf("create sftp client: %w", err)
	}
	log.Println("sftp client created successfully")
	defer func() {
		log.Println("closing sftp client")
		if closeError := sftpClient.Close(); closeError != nil {
			log.Printf("sftp close error: %v", closeError)
		} else {
			log.Println("sftp client closed cleanly")
		}
	}()

	if err := dumpServerInfo(sftpClient); err != nil {
		log.Printf("failed to gather server info: %v", err)
	}

	payload, description, err := loadPayload(configuration)
	if err != nil {
		return fmt.Errorf("load payload: %w", err)
	}
	log.Printf("payload ready: %s (%d bytes)", description, len(payload))

	reader := bytes.NewReader(payload)
	remoteFilePath, err := resolveRemoteFilePath(sftpClient, configuration)
	if err != nil {
		return fmt.Errorf("resolve remote file path: %w", err)
	}
	log.Printf("resolved remote file path: %q", remoteFilePath)

	remoteFile, err := sftpClient.OpenFile(remoteFilePath, os.O_WRONLY|os.O_CREATE|os.O_TRUNC)
	if err != nil {
		return fmt.Errorf("sftp open remote file %q: %w", remoteFilePath, err)
	}

	_, copyError := io.Copy(remoteFile, reader)
	if closeError := remoteFile.Close(); closeError != nil {
		log.Printf("remote file close error: %v", closeError)
	}
	if copyError != nil {
		return fmt.Errorf("sftp copy payload: %w", copyError)
	}
	log.Println("upload completed successfully")

	uploadedInfo, err := sftpClient.Stat(remoteFilePath)
	if err != nil {
		log.Printf("stat uploaded file failed: %v", err)
	} else {
		log.Printf("uploaded file size: %d bytes modified: %s", uploadedInfo.Size(), uploadedInfo.ModTime().Format(time.RFC3339))
	}

	return nil
}

func loadPayload(configuration clientConfig) ([]byte, string, error) {
	if strings.TrimSpace(configuration.localFile) == "" {
		log.Println("no local file configured, using inline payload")
		return inlinePayload, "inline payload", nil
	}

	localFilePath := strings.TrimSpace(configuration.localFile)
	log.Printf("loading local file payload from %q", localFilePath)
	data, err := os.ReadFile(localFilePath)
	if err != nil {
		return nil, "", fmt.Errorf("read file %q: %w", localFilePath, err)
	}
	return data, fmt.Sprintf("file %s", localFilePath), nil
}

func dumpServerInfo(client *sftp.Client) error {
	if client == nil {
		return errors.New("nil sftp client")
	}

	log.Println("retrieving current directory")
	directory, err := client.Getwd()
	if err != nil {
		return fmt.Errorf("get working directory: %w", err)
	}
	log.Printf("current directory: %q", directory)

	log.Println("listing entries in current directory")
	entries, err := client.ReadDir(directory)
	if err != nil {
		return fmt.Errorf("read directory: %w", err)
	}

	limit := len(entries)
	if limit > 20 {
		limit = 20
	}
	for index := 0; index < limit; index++ {
		entry := entries[index]
		entryType := "file"
		if entry.IsDir() {
			entryType = "directory"
		} else if entry.Mode()&os.ModeSymlink != 0 {
			entryType = "symlink"
		}
		log.Printf("entry[%d]: name=%q type=%s size=%d mode=%v modTime=%s", index, entry.Name(), entryType, entry.Size(), entry.Mode(), entry.ModTime().Format(time.RFC3339))
	}
	log.Printf("total directory entries retrieved: %d", len(entries))

	log.Println("statting working directory for responsiveness check")
	if _, err := client.Stat(directory); err != nil {
		return fmt.Errorf("stat working directory: %w", err)
	}
	log.Println("stat succeeded during server info collection")

	return nil
}

func normalizeSFTPServer(server string) (string, string, error) {
	trimmed := strings.TrimSpace(server)
	lower := strings.ToLower(trimmed)

	switch {
	case strings.HasPrefix(lower, "sftp://"):
		trimmed = trimmed[7:]
	case strings.HasPrefix(lower, "ssh://"):
		trimmed = trimmed[6:]
	}

	if trimmed == "" {
		return "", "", errors.New("sftp server host is empty")
	}

	if !strings.Contains(trimmed, ":") {
		trimmed += ":22"
	}

	host, _, splitError := net.SplitHostPort(trimmed)
	if splitError != nil {
		return "", "", fmt.Errorf("invalid sftp server: %w", splitError)
	}

	return trimmed, host, nil
}

func buildSSHClientConfig(configuration clientConfig) (*ssh.ClientConfig, error) {
	hostKeyCallback, err := selectHostKeyCallback(configuration.insecureSkipHostKeyVerification)
	if err != nil {
		return nil, err
	}

	sshConfiguration := &ssh.ClientConfig{
		User:            configuration.user,
		Auth:            []ssh.AuthMethod{ssh.Password(configuration.password)},
		HostKeyCallback: hostKeyCallback,
		Timeout:         configuration.timeout,
		BannerCallback: func(message string) error {
			log.Printf("ssh banner: %s", message)
			return nil
		},
	}

	return sshConfiguration, nil
}

func resolveRemoteFilePath(client *sftp.Client, configuration clientConfig) (string, error) {
	if client == nil {
		return "", errors.New("nil sftp client when resolving remote path")
	}

	trimmedRemote := strings.TrimSpace(configuration.remotePath)
	remoteIndicatesDirectory := strings.HasSuffix(trimmedRemote, "/")

	defaultFileName := deriveRemoteFileName(configuration)
	if trimmedRemote == "" {
		return defaultFileName, nil
	}

	cleanRemote := path.Clean(trimmedRemote)
	if cleanRemote == "." {
		cleanRemote = defaultFileName
	}

	info, statError := client.Stat(cleanRemote)
	switch {
	case statError == nil:
		if info.IsDir() {
			remoteIndicatesDirectory = true
		}
	case isNotExistError(statError):
		// The path does not exist; ensure parent directories exist before returning the path.
		directory := path.Dir(cleanRemote)
		if directory != "." && directory != "/" && directory != "" {
			if err := client.MkdirAll(directory); err != nil {
				return "", fmt.Errorf("ensure remote directory %q: %w", directory, err)
			}
		}
	default:
		return "", fmt.Errorf("stat remote path %q: %w", cleanRemote, statError)
	}

	if remoteIndicatesDirectory {
		directory := strings.TrimSuffix(cleanRemote, "/")
		if directory == "" || directory == "." {
			return defaultFileName, nil
		}
		if err := client.MkdirAll(directory); err != nil {
			return "", fmt.Errorf("ensure remote directory %q: %w", directory, err)
		}
		return path.Join(directory, defaultFileName), nil
	}

	// Ensure the directory hierarchy for the file path exists.
	containingDirectory := path.Dir(cleanRemote)
	if containingDirectory != "." && containingDirectory != "/" && containingDirectory != "" {
		if err := client.MkdirAll(containingDirectory); err != nil {
			return "", fmt.Errorf("ensure remote directory %q: %w", containingDirectory, err)
		}
	}

	return cleanRemote, nil
}

func deriveRemoteFileName(configuration clientConfig) string {
	trimmedLocal := strings.TrimSpace(configuration.localFile)
	if trimmedLocal != "" {
		baseName := filepath.Base(trimmedLocal)
		if baseName != "." && baseName != string(os.PathSeparator) && baseName != "" {
			return baseName
		}
	}
	return fmt.Sprintf("payload-%d.bin", time.Now().Unix())
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
		return statusError.FxCode() == sftp.ErrSSHFxNoSuchFile
	}
	return false
}

func selectHostKeyCallback(skipVerification bool) (ssh.HostKeyCallback, error) {
	if skipVerification {
		log.Println("host key verification disabled; using insecure ignore host key callback")
		return ssh.InsecureIgnoreHostKey(), nil
	}

	userHomeDirectory, err := os.UserHomeDir()
	if err != nil {
		return nil, fmt.Errorf("determine user home directory: %w", err)
	}

	knownHostsPath := filepath.Join(userHomeDirectory, ".ssh", "known_hosts")
	callback, err := knownhosts.New(knownHostsPath)
	if err != nil {
		return nil, fmt.Errorf("load known hosts from %q: %w", knownHostsPath, err)
	}

	log.Printf("using known_hosts file %q for host key verification", knownHostsPath)
	return callback, nil
}
