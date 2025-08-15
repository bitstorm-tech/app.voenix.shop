import { useCallback, useEffect, useRef, useState } from 'react';

interface ImageState {
  status: 'loading' | 'loaded' | 'error' | 'retrying';
  error?: Error;
  retryCount: number;
}

interface UseImageWithFallbackOptions {
  maxRetries?: number;
  retryDelay?: number;
  preload?: boolean;
  onLoad?: () => void;
  onError?: (error: Error) => void;
}

export function useImageWithFallback(src: string | null | undefined, options: UseImageWithFallbackOptions = {}) {
  const { maxRetries = 3, retryDelay = 1000, preload = true, onLoad, onError } = options;

  const [state, setState] = useState<ImageState>({
    status: src ? 'loading' : 'error',
    retryCount: 0,
  });

  const retryTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const imgRef = useRef<HTMLImageElement | null>(null);

  const cleanup = useCallback(() => {
    if (retryTimeoutRef.current) {
      clearTimeout(retryTimeoutRef.current);
      retryTimeoutRef.current = null;
    }
    if (imgRef.current) {
      imgRef.current.onload = null;
      imgRef.current.onerror = null;
    }
  }, []);

  const loadImage = useCallback(
    (imageSrc: string, retryCount: number = 0) => {
      cleanup();

      const img = new Image();
      imgRef.current = img;

      img.onload = () => {
        setState({
          status: 'loaded',
          retryCount,
        });
        onLoad?.();
      };

      img.onerror = () => {
        const error = new Error(`Failed to load image: ${imageSrc}`);

        if (retryCount < maxRetries) {
          setState({
            status: 'retrying',
            error,
            retryCount: retryCount + 1,
          });

          // Exponential backoff: delay * (2 ^ retryCount)
          const delay = retryDelay * Math.pow(2, retryCount);

          retryTimeoutRef.current = setTimeout(() => {
            loadImage(imageSrc, retryCount + 1);
          }, delay);
        } else {
          setState({
            status: 'error',
            error,
            retryCount,
          });
          onError?.(error);
        }
      };

      // Start loading the image
      img.src = imageSrc;
    },
    [maxRetries, retryDelay, onLoad, onError, cleanup],
  );

  const retry = useCallback(() => {
    if (src) {
      setState((prev) => ({
        ...prev,
        status: 'loading',
      }));
      loadImage(src, 0);
    }
  }, [src, loadImage]);

  useEffect(() => {
    if (!src) {
      setState({
        status: 'error',
        error: new Error('No image source provided'),
        retryCount: 0,
      });
      return;
    }

    if (preload) {
      setState({
        status: 'loading',
        retryCount: 0,
      });
      loadImage(src, 0);
    }

    return cleanup;
  }, [src, preload, loadImage, cleanup]);

  return {
    ...state,
    retry,
    isLoading: state.status === 'loading' || state.status === 'retrying',
    isLoaded: state.status === 'loaded',
    isError: state.status === 'error',
    isRetrying: state.status === 'retrying',
  };
}
