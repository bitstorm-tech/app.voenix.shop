'use client';

import React from 'react';
import { Button } from './Button';
import { AlertTriangle } from 'lucide-react';

interface ErrorBoundaryState {
  hasError: boolean;
  error?: Error;
}

interface ErrorBoundaryProps {
  children: React.ReactNode;
  fallback?: React.ComponentType<{ error: Error; reset: () => void }>;
  onError?: (error: Error, errorInfo: React.ErrorInfo) => void;
}

class ErrorBoundaryClass extends React.Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    // Log error to console in development
    console.error('ErrorBoundary caught an error:', error, errorInfo);
    
    // Call optional error handler
    this.props.onError?.(error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      const reset = () => {
        this.setState({ hasError: false, error: undefined });
      };

      if (this.props.fallback) {
        const FallbackComponent = this.props.fallback;
        return <FallbackComponent error={this.state.error!} reset={reset} />;
      }

      return <DefaultErrorFallback error={this.state.error!} reset={reset} />;
    }

    return this.props.children;
  }
}

function DefaultErrorFallback({ error, reset }: { error: Error; reset: () => void }) {
  return (
    <div className="flex min-h-[200px] flex-col items-center justify-center rounded-lg border border-destructive/20 bg-destructive/5 p-6 text-center">
      <div className="flex items-center gap-2 text-destructive mb-4">
        <AlertTriangle className="h-5 w-5" />
        <h3 className="text-lg font-semibold">Something went wrong</h3>
      </div>
      
      <p className="text-muted-foreground mb-4 max-w-md">
        An error occurred while rendering this component. This might be due to malformed data or a temporary issue.
      </p>
      
      {process.env.NODE_ENV === 'development' && (
        <details className="mb-4 max-w-md text-left">
          <summary className="cursor-pointer text-sm font-medium text-muted-foreground hover:text-foreground">
            Error details (development only)
          </summary>
          <pre className="mt-2 whitespace-pre-wrap text-xs text-muted-foreground bg-muted p-2 rounded">
            {error.message}
          </pre>
        </details>
      )}
      
      <Button onClick={reset} variant="outline" size="sm">
        Try again
      </Button>
    </div>
  );
}

export function ErrorBoundary(props: ErrorBoundaryProps) {
  return <ErrorBoundaryClass {...props} />;
}

/**
 * Higher-order component to wrap components with error boundary
 */
export function withErrorBoundary<P extends object>(
  Component: React.ComponentType<P>,
  fallback?: React.ComponentType<{ error: Error; reset: () => void }>
) {
  const WrappedComponent = (props: P) => (
    <ErrorBoundary fallback={fallback}>
      <Component {...props} />
    </ErrorBoundary>
  );
  
  WrappedComponent.displayName = `withErrorBoundary(${Component.displayName || Component.name})`;
  
  return WrappedComponent;
}