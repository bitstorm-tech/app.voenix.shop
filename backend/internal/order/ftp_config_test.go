package order

import (
	"errors"
	"testing"
	"time"
)

func TestLoadOrderPDFFTPConfigSuccess(t *testing.T) {
	cfg, err := loadOrderPDFFTPConfig(func(key string) string {
		switch key {
		case "ORDER_PDF_FTP_SERVER":
			return "ftp.example.com"
		case "ORDER_PDF_FTP_FOLDER":
			return "orders"
		case "ORDER_PDF_FTP_USER":
			return "user"
		case "ORDER_PDF_FTP_PASSWORD":
			return "pass"
		case "ORDER_PDF_FTP_TIMEOUT":
			return "12"
		case "ORDER_PDF_FTP_SKIP_HOST_KEY_VERIFICATION":
			return "true"
		case "ORDER_PDF_FTP_KNOWN_HOSTS_PATH":
			return "/tmp/known_hosts"
		case "ORDER_PDF_FTP_FALLBACK_FILENAME":
			return "fallback.pdf"
		default:
			return ""
		}
	})
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if cfg.Server != "ftp.example.com" || cfg.User != "user" || cfg.Password != "pass" || cfg.Folder != "orders" {
		t.Fatalf("unexpected config: %+v", cfg)
	}
	if cfg.Timeout != 12*time.Second {
		t.Fatalf("expected timeout 12s, got %v", cfg.Timeout)
	}
	if !cfg.InsecureSkipHostKeyVerification {
		t.Fatalf("expected insecure skip host key verification to be true")
	}
	if cfg.KnownHostsPath != "/tmp/known_hosts" {
		t.Fatalf("unexpected known hosts path: %s", cfg.KnownHostsPath)
	}
	if cfg.FallbackFileName != "fallback.pdf" {
		t.Fatalf("unexpected fallback filename: %s", cfg.FallbackFileName)
	}
}

func TestLoadOrderPDFFTPConfigMissing(t *testing.T) {
	_, err := loadOrderPDFFTPConfig(func(string) string { return "" })
	if !errors.Is(err, errOrderPDFFTPConfigMissing) {
		t.Fatalf("expected missing config error, got %v", err)
	}
}

func TestLoadOrderPDFFTPConfigInvalidTimeout(t *testing.T) {
	_, err := loadOrderPDFFTPConfig(func(key string) string {
		switch key {
		case "ORDER_PDF_FTP_SERVER":
			return "ftp.example.com"
		case "ORDER_PDF_FTP_USER":
			return "user"
		case "ORDER_PDF_FTP_PASSWORD":
			return "pass"
		case "ORDER_PDF_FTP_TIMEOUT":
			return "abc"
		default:
			return ""
		}
	})
	if err == nil {
		t.Fatal("expected error for invalid timeout")
	}
}

func TestLoadOrderPDFFTPConfigInvalidSkipHostKey(t *testing.T) {
	_, err := loadOrderPDFFTPConfig(func(key string) string {
		switch key {
		case "ORDER_PDF_FTP_SERVER":
			return "ftp.example.com"
		case "ORDER_PDF_FTP_USER":
			return "user"
		case "ORDER_PDF_FTP_PASSWORD":
			return "pass"
		case "ORDER_PDF_FTP_SKIP_HOST_KEY_VERIFICATION":
			return "notabool"
		default:
			return ""
		}
	})
	if err == nil {
		t.Fatal("expected error for invalid skip host key flag")
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
