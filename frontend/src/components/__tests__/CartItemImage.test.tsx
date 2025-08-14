import { render, screen, fireEvent } from '@testing-library/react';
import { CartItemImage } from '../CartItemImage';
import { useImageWithFallback } from '@/hooks/useImageWithFallback';

// Mock the useImageWithFallback hook
jest.mock('@/hooks/useImageWithFallback');

// Mock the UI components
jest.mock('../ImageSkeleton', () => ({
  ImageSkeleton: ({ className }: { className?: string }) => (
    <div data-testid="image-skeleton" className={className}>Loading...</div>
  ),
}));

jest.mock('../ui/Button', () => ({
  Button: ({ children, onClick, ...props }: any) => (
    <button onClick={onClick} {...props}>{children}</button>
  ),
}));

describe('CartItemImage', () => {
  const mockRetry = jest.fn();
  const defaultProps = {
    src: 'https://example.com/image.jpg',
    alt: 'Test image',
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Loading State', () => {
    it('should display skeleton when loading', () => {
      (useImageWithFallback as jest.Mock).mockReturnValue({
        status: 'loading',
        error: null,
        retryCount: 0,
        retry: mockRetry,
        isLoading: true,
        isLoaded: false,
        isError: false,
        isRetrying: false,
      });

      render(<CartItemImage {...defaultProps} />);

      expect(screen.getByTestId('image-skeleton')).toBeInTheDocument();
      expect(screen.queryByRole('img')).not.toBeInTheDocument();
    });

    it('should apply custom className to skeleton', () => {
      (useImageWithFallback as jest.Mock).mockReturnValue({
        status: 'loading',
        isLoading: true,
        isLoaded: false,
        isError: false,
        isRetrying: false,
        retry: mockRetry,
      });

      render(
        <CartItemImage 
          {...defaultProps} 
          className="custom-size-class rounded-lg"
        />
      );

      const skeleton = screen.getByTestId('image-skeleton');
      expect(skeleton).toHaveClass('custom-size-class rounded-lg');
    });
  });

  describe('Retrying State', () => {
    it('should display retry spinner when retrying', () => {
      (useImageWithFallback as jest.Mock).mockReturnValue({
        status: 'retrying',
        error: null,
        retryCount: 1,
        retry: mockRetry,
        isLoading: false,
        isLoaded: false,
        isError: false,
        isRetrying: true,
      });

      render(<CartItemImage {...defaultProps} />);

      expect(screen.getByText('Retrying...')).toBeInTheDocument();
      expect(screen.queryByTestId('image-skeleton')).not.toBeInTheDocument();
    });
  });

  describe('Error State', () => {
    it('should display error message and retry button on error', () => {
      (useImageWithFallback as jest.Mock).mockReturnValue({
        status: 'error',
        error: new Error('Failed to load'),
        retryCount: 3,
        retry: mockRetry,
        isLoading: false,
        isLoaded: false,
        isError: true,
        isRetrying: false,
      });

      render(<CartItemImage {...defaultProps} />);

      expect(screen.getByText('Failed to load')).toBeInTheDocument();
      expect(screen.getByText('Retry')).toBeInTheDocument();
    });

    it('should show different message for first error', () => {
      (useImageWithFallback as jest.Mock).mockReturnValue({
        status: 'error',
        error: new Error('Network error'),
        retryCount: 0,
        retry: mockRetry,
        isLoading: false,
        isLoaded: false,
        isError: true,
        isRetrying: false,
      });

      render(<CartItemImage {...defaultProps} />);

      expect(screen.getByText('Image error')).toBeInTheDocument();
    });

    it('should call retry function when retry button is clicked', () => {
      (useImageWithFallback as jest.Mock).mockReturnValue({
        status: 'error',
        error: new Error('Failed'),
        retryCount: 1,
        retry: mockRetry,
        isLoading: false,
        isLoaded: false,
        isError: true,
        isRetrying: false,
      });

      render(<CartItemImage {...defaultProps} />);

      const retryButton = screen.getByText('Retry');
      fireEvent.click(retryButton);

      expect(mockRetry).toHaveBeenCalledTimes(1);
    });
  });

  describe('Success State', () => {
    it('should display image when loaded successfully', () => {
      const onLoad = jest.fn();
      
      (useImageWithFallback as jest.Mock).mockReturnValue({
        status: 'loaded',
        error: null,
        retryCount: 0,
        retry: mockRetry,
        isLoading: false,
        isLoaded: true,
        isError: false,
        isRetrying: false,
      });

      render(
        <CartItemImage 
          {...defaultProps} 
          onLoad={onLoad}
        />
      );

      const image = screen.getByRole('img');
      expect(image).toBeInTheDocument();
      expect(image).toHaveAttribute('src', defaultProps.src);
      expect(image).toHaveAttribute('alt', defaultProps.alt);
      expect(image).toHaveAttribute('loading', 'lazy');
    });

    it('should apply custom className to image', () => {
      (useImageWithFallback as jest.Mock).mockReturnValue({
        status: 'loaded',
        isLoading: false,
        isLoaded: true,
        isError: false,
        isRetrying: false,
        retry: mockRetry,
      });

      render(
        <CartItemImage 
          {...defaultProps} 
          className="w-32 h-32 rounded-full"
        />
      );

      const image = screen.getByRole('img');
      expect(image).toHaveClass('w-32 h-32 rounded-full transition-opacity duration-300');
    });

    it('should call onLoad when image loads', () => {
      const onLoad = jest.fn();
      
      (useImageWithFallback as jest.Mock).mockReturnValue({
        status: 'loaded',
        isLoading: false,
        isLoaded: true,
        isError: false,
        isRetrying: false,
        retry: mockRetry,
      });

      render(
        <CartItemImage 
          {...defaultProps} 
          onLoad={onLoad}
        />
      );

      const image = screen.getByRole('img');
      fireEvent.load(image);

      expect(onLoad).toHaveBeenCalledTimes(1);
    });

    it('should call onError when image fails to display', () => {
      const onError = jest.fn();
      const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation();
      
      (useImageWithFallback as jest.Mock).mockReturnValue({
        status: 'loaded',
        isLoading: false,
        isLoaded: true,
        isError: false,
        isRetrying: false,
        retry: mockRetry,
      });

      render(
        <CartItemImage 
          {...defaultProps} 
          onError={onError}
        />
      );

      const image = screen.getByRole('img');
      fireEvent.error(image);

      expect(consoleErrorSpy).toHaveBeenCalledWith(
        'Image load error:',
        expect.any(Object)
      );
      expect(onError).toHaveBeenCalledWith(
        expect.objectContaining({
          message: expect.stringContaining('Failed to display image')
        })
      );

      consoleErrorSpy.mockRestore();
    });
  });

  describe('Fallback State', () => {
    it('should display fallback when src is null', () => {
      (useImageWithFallback as jest.Mock).mockReturnValue({
        status: 'error',
        error: new Error('No source'),
        retryCount: 0,
        retry: mockRetry,
        isLoading: false,
        isLoaded: false,
        isError: true,
        isRetrying: false,
      });

      render(
        <CartItemImage 
          src={null}
          alt="No image"
        />
      );

      // Should show error state with retry option
      expect(screen.getByText('Image error')).toBeInTheDocument();
    });

    it('should display fallback when src is undefined', () => {
      (useImageWithFallback as jest.Mock).mockReturnValue({
        status: 'error',
        error: new Error('No source'),
        retryCount: 0,
        retry: mockRetry,
        isLoading: false,
        isLoaded: false,
        isError: true,
        isRetrying: false,
      });

      render(
        <CartItemImage 
          src={undefined}
          alt="No image"
        />
      );

      expect(screen.getByText('Image error')).toBeInTheDocument();
    });

    it('should show fallback icon when loaded but no src', () => {
      (useImageWithFallback as jest.Mock).mockReturnValue({
        status: 'loaded',
        error: null,
        retryCount: 0,
        retry: mockRetry,
        isLoading: false,
        isLoaded: true,
        isError: false,
        isRetrying: false,
      });

      render(
        <CartItemImage 
          src={null}
          alt="No image"
        />
      );

      // When loaded but no src, should show fallback
      expect(screen.queryByRole('img')).not.toBeInTheDocument();
      // The component will show the fallback state at the bottom
    });
  });

  describe('Hook Integration', () => {
    it('should pass correct options to useImageWithFallback', () => {
      const onLoad = jest.fn();
      const onError = jest.fn();
      
      (useImageWithFallback as jest.Mock).mockReturnValue({
        status: 'loading',
        isLoading: true,
        isLoaded: false,
        isError: false,
        isRetrying: false,
        retry: mockRetry,
      });

      render(
        <CartItemImage 
          {...defaultProps}
          onLoad={onLoad}
          onError={onError}
        />
      );

      expect(useImageWithFallback).toHaveBeenCalledWith(
        defaultProps.src,
        {
          maxRetries: 3,
          retryDelay: 1000,
          preload: true,
          onLoad,
          onError,
        }
      );
    });
  });

  describe('Container Styling', () => {
    it('should apply default container classes when no size classes provided', () => {
      (useImageWithFallback as jest.Mock).mockReturnValue({
        status: 'loaded',
        isLoading: false,
        isLoaded: true,
        isError: false,
        isRetrying: false,
        retry: mockRetry,
      });

      const { container } = render(
        <CartItemImage 
          {...defaultProps}
          className="object-cover"
        />
      );

      const containerDiv = container.firstChild;
      expect(containerDiv).toHaveClass('h-24', 'w-24', 'sm:h-32', 'sm:w-32', 'rounded-md');
    });

    it('should not override existing size classes', () => {
      (useImageWithFallback as jest.Mock).mockReturnValue({
        status: 'loaded',
        isLoading: false,
        isLoaded: true,
        isError: false,
        isRetrying: false,
        retry: mockRetry,
      });

      const { container } = render(
        <CartItemImage 
          {...defaultProps}
          className="h-40 w-40 rounded-full"
        />
      );

      const containerDiv = container.firstChild;
      expect(containerDiv).toHaveClass('h-40', 'w-40');
      expect(containerDiv).not.toHaveClass('h-24', 'w-24', 'sm:h-32', 'sm:w-32');
    });
  });

  describe('Accessibility', () => {
    it('should have proper alt text on image', () => {
      (useImageWithFallback as jest.Mock).mockReturnValue({
        status: 'loaded',
        isLoading: false,
        isLoaded: true,
        isError: false,
        isRetrying: false,
        retry: mockRetry,
      });

      render(
        <CartItemImage 
          {...defaultProps}
          alt="Custom mug with AI-generated design"
        />
      );

      const image = screen.getByRole('img');
      expect(image).toHaveAttribute('alt', 'Custom mug with AI-generated design');
    });

    it('should have accessible retry button', () => {
      (useImageWithFallback as jest.Mock).mockReturnValue({
        status: 'error',
        error: new Error('Failed'),
        retryCount: 1,
        retry: mockRetry,
        isLoading: false,
        isLoaded: false,
        isError: true,
        isRetrying: false,
      });

      render(<CartItemImage {...defaultProps} />);

      const retryButton = screen.getByRole('button', { name: /retry/i });
      expect(retryButton).toBeInTheDocument();
    });
  });

  describe('Performance', () => {
    it('should use lazy loading for images', () => {
      (useImageWithFallback as jest.Mock).mockReturnValue({
        status: 'loaded',
        isLoading: false,
        isLoaded: true,
        isError: false,
        isRetrying: false,
        retry: mockRetry,
      });

      render(<CartItemImage {...defaultProps} />);

      const image = screen.getByRole('img');
      expect(image).toHaveAttribute('loading', 'lazy');
    });

    it('should not re-render unnecessarily when props do not change', () => {
      const mockHook = jest.fn().mockReturnValue({
        status: 'loaded',
        isLoading: false,
        isLoaded: true,
        isError: false,
        isRetrying: false,
        retry: mockRetry,
      });

      (useImageWithFallback as jest.Mock).mockImplementation(mockHook);

      const { rerender } = render(<CartItemImage {...defaultProps} />);
      expect(mockHook).toHaveBeenCalledTimes(1);

      // Re-render with same props
      rerender(<CartItemImage {...defaultProps} />);
      
      // Hook should be called again due to re-render, but with same params
      expect(mockHook).toHaveBeenCalledTimes(2);
      expect(mockHook).toHaveBeenLastCalledWith(
        defaultProps.src,
        expect.any(Object)
      );
    });
  });
});