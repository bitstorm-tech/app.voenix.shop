export interface PDFDownloadOptions {
  orderId: string;
  orderNumber: string;
  onProgress?: (progress: number) => void;
  onError?: (error: Error) => void;
}

export interface PDFDownloadResult {
  success: boolean;
  error?: string;
}

/**
 * Downloads a PDF file from the server using blob API with authentication
 * Handles popup blockers and provides error recovery
 */
export async function downloadOrderPDF({ orderId, orderNumber, onProgress, onError }: PDFDownloadOptions): Promise<PDFDownloadResult> {
  try {
    // Start progress tracking
    onProgress?.(0);

    // Fetch PDF with authentication via cookies (same as API client)
    const response = await fetch(`/api/user/orders/${orderId}/pdf`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/pdf',
      },
      credentials: 'include', // Use cookies for authentication like the rest of the app
    });

    if (!response.ok) {
      const error = new Error(`Failed to download PDF: ${response.status} ${response.statusText}`);
      onError?.(error);
      return { success: false, error: error.message };
    }

    onProgress?.(50);

    // Convert response to blob
    const blob = await response.blob();

    onProgress?.(75);

    // Verify it's actually a PDF
    if (blob.type !== 'application/pdf' && !blob.type.includes('pdf')) {
      const error = new Error('Downloaded file is not a valid PDF');
      onError?.(error);
      return { success: false, error: error.message };
    }

    // Create download link and trigger download
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `order-${orderNumber}-receipt.pdf`;

    // Append to DOM, click, and clean up
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    // Clean up object URL after a short delay to ensure download starts
    setTimeout(() => {
      window.URL.revokeObjectURL(url);
    }, 100);

    onProgress?.(100);

    return { success: true };
  } catch (error) {
    const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';
    const downloadError = new Error(`PDF download failed: ${errorMessage}`);
    onError?.(downloadError);
    return { success: false, error: downloadError.message };
  }
}

/**
 * Creates a manual download link for fallback scenarios
 * Returns the URL that users can click to download manually
 */
export function createManualDownloadUrl(orderId: string): string {
  const baseUrl = window.location.origin;

  // Since the app uses cookie-based authentication, the manual download
  // should work automatically with the same credentials
  return `${baseUrl}/api/user/orders/${orderId}/pdf`;
}

/**
 * Checks if the browser might block automatic downloads (popup blocker)
 * This is a heuristic check and not 100% accurate
 */
export function isDownloadLikelyBlocked(): boolean {
  // Check if we're in a context where popup blocking is likely
  // This includes situations where the download wasn't initiated by a direct user action
  try {
    // Try to open a popup - if it fails, downloads might be blocked too
    const testWindow = window.open('', '_blank', 'width=1,height=1');
    if (testWindow) {
      testWindow.close();
      return false;
    }
    return true;
  } catch {
    return true;
  }
}
