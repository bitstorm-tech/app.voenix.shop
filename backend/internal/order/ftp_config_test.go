package order

import (
	"testing"
	"time"
)

func TestLoadOrderPDFFTPConfigsSingleSuccess(t *testing.T) {
	configs, err := loadOrderPDFFTPConfigs(func(key string) string {
		switch key {
		case "ORDER_PDF_FTP_CONFIGS":
			return "primary"
		case "ORDER_PDF_FTP_SERVER_PRIMARY":
			return "ftp.example.com"
		case "ORDER_PDF_FTP_FOLDER_PRIMARY":
			return "orders"
		case "ORDER_PDF_FTP_USER_PRIMARY":
			return "user"
		case "ORDER_PDF_FTP_PASSWORD_PRIMARY":
			return "pass"
		case "ORDER_PDF_FTP_TIMEOUT_PRIMARY":
			return "12"
		case "ORDER_PDF_FTP_SKIP_HOST_KEY_VERIFICATION_PRIMARY":
			return "true"
		case "ORDER_PDF_FTP_KNOWN_HOSTS_PATH_PRIMARY":
			return "/tmp/known_hosts"
		case "ORDER_PDF_FTP_FALLBACK_FILENAME_PRIMARY":
			return "fallback.pdf"
		default:
			return ""
		}
	})
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if len(configs) != 1 {
		t.Fatalf("expected one config, got %d", len(configs))
	}
	config := configs[0]
	if config.Server != "ftp.example.com" || config.User != "user" || config.Password != "pass" || config.Folder != "orders" {
		t.Fatalf("unexpected config: %+v", config)
	}
	if config.Timeout != 12*time.Second {
		t.Fatalf("expected timeout 12s, got %v", config.Timeout)
	}
	if !config.InsecureSkipHostKeyVerification {
		t.Fatalf("expected insecure skip host key verification to be true")
	}
	if config.KnownHostsPath != "/tmp/known_hosts" {
		t.Fatalf("unexpected known hosts path: %s", config.KnownHostsPath)
	}
	if config.FallbackFileName != "fallback.pdf" {
		t.Fatalf("unexpected fallback filename: %s", config.FallbackFileName)
	}
	if config.Name != "primary" {
		t.Fatalf("expected config name primary, got %s", config.Name)
	}
}

func TestLoadOrderPDFFTPConfigsMissingList(t *testing.T) {
	configs, err := loadOrderPDFFTPConfigs(func(string) string { return "" })
	if err != nil {
		t.Fatalf("expected no error when configs missing, got %v", err)
	}
	if len(configs) != 0 {
		t.Fatalf("expected no configs, got %d", len(configs))
	}
}

func TestLoadOrderPDFFTPConfigsInvalidTimeout(t *testing.T) {
	_, err := loadOrderPDFFTPConfigs(func(key string) string {
		switch key {
		case "ORDER_PDF_FTP_CONFIGS":
			return "primary"
		case "ORDER_PDF_FTP_SERVER_PRIMARY":
			return "ftp.example.com"
		case "ORDER_PDF_FTP_USER_PRIMARY":
			return "user"
		case "ORDER_PDF_FTP_PASSWORD_PRIMARY":
			return "pass"
		case "ORDER_PDF_FTP_TIMEOUT_PRIMARY":
			return "abc"
		default:
			return ""
		}
	})
	if err == nil {
		t.Fatal("expected error for invalid timeout")
	}
}

func TestLoadOrderPDFFTPConfigsInvalidSkipHostKey(t *testing.T) {
	_, err := loadOrderPDFFTPConfigs(func(key string) string {
		switch key {
		case "ORDER_PDF_FTP_CONFIGS":
			return "primary"
		case "ORDER_PDF_FTP_SERVER_PRIMARY":
			return "ftp.example.com"
		case "ORDER_PDF_FTP_USER_PRIMARY":
			return "user"
		case "ORDER_PDF_FTP_PASSWORD_PRIMARY":
			return "pass"
		case "ORDER_PDF_FTP_SKIP_HOST_KEY_VERIFICATION_PRIMARY":
			return "notabool"
		default:
			return ""
		}
	})
	if err == nil {
		t.Fatal("expected error for invalid skip host key flag")
	}
}

func TestLoadOrderPDFFTPConfigsMultiple(t *testing.T) {
	configs, err := loadOrderPDFFTPConfigs(func(key string) string {
		switch key {
		case "ORDER_PDF_FTP_CONFIGS":
			return "primary, backup"
		case "ORDER_PDF_FTP_SERVER_PRIMARY":
			return "ftp-primary.example.com"
		case "ORDER_PDF_FTP_USER_PRIMARY":
			return "primary-user"
		case "ORDER_PDF_FTP_PASSWORD_PRIMARY":
			return "primary-pass"
		case "ORDER_PDF_FTP_FOLDER_PRIMARY":
			return "orders"
		case "ORDER_PDF_FTP_TIMEOUT_PRIMARY":
			return "20"
		case "ORDER_PDF_FTP_SERVER_BACKUP":
			return "ftp-backup.example.com"
		case "ORDER_PDF_FTP_USER_BACKUP":
			return "backup-user"
		case "ORDER_PDF_FTP_PASSWORD_BACKUP":
			return "backup-pass"
		case "ORDER_PDF_FTP_SKIP_HOST_KEY_VERIFICATION_BACKUP":
			return "true"
		default:
			return ""
		}
	})
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if len(configs) != 2 {
		t.Fatalf("expected two configs, got %d", len(configs))
	}
	primary := configs[0]
	if primary.Name != "primary" || primary.Server != "ftp-primary.example.com" || primary.Timeout != 20*time.Second {
		t.Fatalf("unexpected primary config: %+v", primary)
	}
	backup := configs[1]
	if backup.Name != "backup" || !backup.InsecureSkipHostKeyVerification {
		t.Fatalf("unexpected backup config: %+v", backup)
	}
}

func TestRemoteOrderPDFPath(t *testing.T) {
	if p := remoteOrderPDFPath("orders", "order.pdf"); p != "orders/order.pdf" {
		t.Fatalf("unexpected path: %s", p)
	}
	if p := remoteOrderPDFPath("orders", " "); p != "orders/order.pdf" {
		t.Fatalf("expected fallback path, got %s", p)
	}
}
