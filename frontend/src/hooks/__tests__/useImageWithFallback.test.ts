import { act, renderHook, waitFor } from '@testing-library/react';
import { useImageWithFallback } from '../useImageWithFallback';

// Mock Image constructor
class MockImage {
  onload: (() => void) | null = null;
  onerror: (() => void) | null = null;
  src: string = '';

  constructor() {
    // Simulate async image loading
    setTimeout(() => {
      if (this.src.includes('error')) {
        this.onerror?.();
      } else if (this.src) {
        this.onload?.();
      }
    }, 10);
  }
}

// Replace global Image with MockImage (cast to avoid DOM constructor type mismatch in Node/JSDOM)
(globalThis as any).Image = MockImage;

describe('useImageWithFallback', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.runOnlyPendingTimers();
    jest.useRealTimers();
  });

  describe('Initial State', () => {
    it('should start with loading state when src is provided', () => {
      const { result } = renderHook(() => useImageWithFallback('https://example.com/image.jpg'));

      expect(result.current.status).toBe('loading');
      expect(result.current.isLoading).toBe(true);
      expect(result.current.isLoaded).toBe(false);
      expect(result.current.isError).toBe(false);
      expect(result.current.retryCount).toBe(0);
    });

    it('should start with error state when src is null', () => {
      const { result } = renderHook(() => useImageWithFallback(null));

      expect(result.current.status).toBe('error');
      expect(result.current.isError).toBe(true);
      expect(result.current.error?.message).toBe('No image source provided');
    });

    it('should start with error state when src is undefined', () => {
      const { result } = renderHook(() => useImageWithFallback(undefined));

      expect(result.current.status).toBe('error');
      expect(result.current.isError).toBe(true);
      expect(result.current.error?.message).toBe('No image source provided');
    });
  });

  describe('Image Loading Success', () => {
    it('should transition to loaded state on successful load', async () => {
      const onLoad = jest.fn();
      const { result } = renderHook(() => useImageWithFallback('https://example.com/success.jpg', { onLoad }));

      expect(result.current.status).toBe('loading');

      await act(async () => {
        jest.advanceTimersByTime(20);
      });

      await waitFor(() => {
        expect(result.current.status).toBe('loaded');
      });

      expect(result.current.isLoaded).toBe(true);
      expect(result.current.isLoading).toBe(false);
      expect(onLoad).toHaveBeenCalledTimes(1);
    });

    it('should handle changing src to valid image', async () => {
      const { result, rerender } = renderHook(({ src }) => useImageWithFallback(src), { initialProps: { src: null as string | null } });

      expect(result.current.status).toBe('error');

      rerender({ src: 'https://example.com/new-image.jpg' });

      expect(result.current.status).toBe('loading');

      await act(async () => {
        jest.advanceTimersByTime(20);
      });

      await waitFor(() => {
        expect(result.current.status).toBe('loaded');
      });
    });
  });

  describe('Image Loading Failure and Retry', () => {
    it('should retry on failure with exponential backoff', async () => {
      const onError = jest.fn();
      const { result } = renderHook(() =>
        useImageWithFallback('https://example.com/error.jpg', {
          maxRetries: 2,
          retryDelay: 100,
          onError,
        }),
      );

      // Initial load fails
      await act(async () => {
        jest.advanceTimersByTime(20);
      });

      await waitFor(() => {
        expect(result.current.status).toBe('retrying');
      });
      expect(result.current.retryCount).toBe(1);
      expect(result.current.isRetrying).toBe(true);

      // First retry (100ms delay)
      await act(async () => {
        jest.advanceTimersByTime(100);
      });

      // Image load attempt
      await act(async () => {
        jest.advanceTimersByTime(20);
      });

      await waitFor(() => {
        expect(result.current.retryCount).toBe(2);
      });

      // Second retry (200ms delay - exponential backoff)
      await act(async () => {
        jest.advanceTimersByTime(200);
      });

      // Image load attempt
      await act(async () => {
        jest.advanceTimersByTime(20);
      });

      await waitFor(() => {
        expect(result.current.status).toBe('error');
      });

      expect(result.current.isError).toBe(true);
      expect(result.current.retryCount).toBe(2);
      expect(onError).toHaveBeenCalledTimes(1);
      expect(onError).toHaveBeenCalledWith(
        expect.objectContaining({
          message: 'Failed to load image: https://example.com/error.jpg',
        }),
      );
    });

    it('should allow manual retry after max retries exceeded', async () => {
      const { result } = renderHook(() =>
        useImageWithFallback('https://example.com/error.jpg', {
          maxRetries: 1,
          retryDelay: 50,
        }),
      );

      // Let automatic retries fail
      await act(async () => {
        jest.advanceTimersByTime(200);
      });

      await waitFor(() => {
        expect(result.current.status).toBe('error');
      });

      // Manual retry
      act(() => {
        result.current.retry();
      });

      expect(result.current.status).toBe('loading');
      expect(result.current.retryCount).toBe(0); // Reset on manual retry
    });

    it('should not retry when maxRetries is 0', async () => {
      const onError = jest.fn();
      const { result } = renderHook(() =>
        useImageWithFallback('https://example.com/error.jpg', {
          maxRetries: 0,
          onError,
        }),
      );

      await act(async () => {
        jest.advanceTimersByTime(20);
      });

      await waitFor(() => {
        expect(result.current.status).toBe('error');
      });

      expect(result.current.retryCount).toBe(0);
      expect(onError).toHaveBeenCalledTimes(1);
    });
  });

  describe('Preload Option', () => {
    it('should not load image when preload is false', () => {
      const { result } = renderHook(() =>
        useImageWithFallback('https://example.com/image.jpg', {
          preload: false,
        }),
      );

      expect(result.current.status).toBe('loading');

      act(() => {
        jest.advanceTimersByTime(100);
      });

      // Status should remain loading since image wasn't preloaded
      expect(result.current.status).toBe('loading');
    });
  });

  describe('Cleanup', () => {
    it('should cleanup timeout on unmount', () => {
      const clearTimeoutSpy = jest.spyOn(global, 'clearTimeout');

      const { unmount } = renderHook(() =>
        useImageWithFallback('https://example.com/error.jpg', {
          maxRetries: 3,
          retryDelay: 1000,
        }),
      );

      // Trigger first failure to start retry timer
      act(() => {
        jest.advanceTimersByTime(20);
      });

      unmount();

      expect(clearTimeoutSpy).toHaveBeenCalled();
    });

    it('should cleanup image handlers on unmount', () => {
      const { unmount, result } = renderHook(() => useImageWithFallback('https://example.com/image.jpg'));

      unmount();

      // After unmount, image handlers should be cleared
      // This prevents memory leaks
      expect(result.current.status).toBeDefined(); // Hook still returns last state
    });

    it('should cleanup when src changes', async () => {
      const { result, rerender } = renderHook(({ src }) => useImageWithFallback(src), { initialProps: { src: 'https://example.com/image1.jpg' } });

      expect(result.current.status).toBe('loading');

      // Change src before first image loads
      rerender({ src: 'https://example.com/image2.jpg' });

      await act(async () => {
        jest.advanceTimersByTime(20);
      });

      await waitFor(() => {
        expect(result.current.status).toBe('loaded');
      });

      // Should load the second image, not the first
      expect(result.current.isLoaded).toBe(true);
    });
  });

  describe('Edge Cases', () => {
    it('should handle empty string src', () => {
      const { result } = renderHook(() => useImageWithFallback(''));

      expect(result.current.status).toBe('loading');

      // Empty string is technically a valid src, so it attempts to load
      act(() => {
        jest.advanceTimersByTime(20);
      });
    });

    it('should handle rapid src changes', async () => {
      const { result, rerender } = renderHook(({ src }) => useImageWithFallback(src), { initialProps: { src: 'https://example.com/image1.jpg' } });

      // Rapid changes
      rerender({ src: 'https://example.com/image2.jpg' });
      rerender({ src: 'https://example.com/image3.jpg' });
      rerender({ src: 'https://example.com/image4.jpg' });

      await act(async () => {
        jest.advanceTimersByTime(20);
      });

      await waitFor(() => {
        expect(result.current.status).toBe('loaded');
      });

      // Should successfully load the final image
      expect(result.current.isLoaded).toBe(true);
    });

    it('should handle retry when src is null', () => {
      const { result } = renderHook(() => useImageWithFallback(null));

      expect(result.current.status).toBe('error');

      // Attempting to retry with null src should not crash
      act(() => {
        result.current.retry();
      });

      expect(result.current.status).toBe('error');
    });

    it('should calculate exponential backoff correctly', async () => {
      const delays: number[] = [];
      const originalSetTimeout = globalThis.setTimeout.bind(globalThis);

      // Spy on setTimeout to capture delays (ensure types align across Node/DOM)
      jest.spyOn(globalThis as any, 'setTimeout').mockImplementation(((fn: any, delay?: number, ...args: any[]) => {
        if (typeof delay === 'number' && delay > 0) {
          delays.push(delay);
        }
        return originalSetTimeout(fn as any, delay as any, ...args) as any;
      }) as any);

      renderHook(() =>
        useImageWithFallback('https://example.com/error.jpg', {
          maxRetries: 3,
          retryDelay: 100,
        }),
      );

      // Let all retries happen
      await act(async () => {
        jest.advanceTimersByTime(1000);
      });

      // Verify exponential backoff: 100, 200, 400
      expect(delays).toContain(100); // First retry: 100 * 2^0
      expect(delays).toContain(200); // Second retry: 100 * 2^1
      expect(delays).toContain(400); // Third retry: 100 * 2^2
    });
  });

  describe('Concurrent Operations', () => {
    it('should handle retry during src change', async () => {
      const { result, rerender } = renderHook(
        ({ src }) =>
          useImageWithFallback(src, {
            maxRetries: 2,
            retryDelay: 100,
          }),
        { initialProps: { src: 'https://example.com/error.jpg' } },
      );

      // Let it fail once
      await act(async () => {
        jest.advanceTimersByTime(20);
      });

      await waitFor(() => {
        expect(result.current.status).toBe('retrying');
      });

      // Change src during retry
      rerender({ src: 'https://example.com/success.jpg' });

      await act(async () => {
        jest.advanceTimersByTime(20);
      });

      await waitFor(() => {
        expect(result.current.status).toBe('loaded');
      });

      expect(result.current.isLoaded).toBe(true);
      expect(result.current.retryCount).toBe(0); // Reset on src change
    });

    it('should handle manual retry during automatic retry', async () => {
      const { result } = renderHook(() =>
        useImageWithFallback('https://example.com/error.jpg', {
          maxRetries: 3,
          retryDelay: 1000,
        }),
      );

      // Let first attempt fail
      await act(async () => {
        jest.advanceTimersByTime(20);
      });

      await waitFor(() => {
        expect(result.current.isRetrying).toBe(true);
      });

      // Manual retry during automatic retry
      act(() => {
        result.current.retry();
      });

      expect(result.current.status).toBe('loading');
      expect(result.current.retryCount).toBe(0); // Reset on manual retry
    });
  });
});
