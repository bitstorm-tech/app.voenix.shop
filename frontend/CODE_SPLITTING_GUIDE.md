# Code Splitting Implementation Guide

## Overview

This document describes the code splitting implementation in the Voenix Shop frontend application. The implementation creates separate JavaScript bundles for the admin section and editor section to improve initial load time and performance.

## Implementation Details

### 1. Route-Based Code Splitting

The application uses React's `lazy()` function and dynamic imports to split code at the route level:

- **Main Routes**: Editor, Cart, Checkout, and Login pages are lazy-loaded
- **Admin Routes**: All admin pages are bundled together in a separate chunk

### 2. Vite Configuration

The `vite.config.ts` file defines manual chunks for optimal bundle sizes:

```typescript
manualChunks: {
  // Core vendor libraries
  'vendor-react': ['react', 'react-dom', 'react-router-dom'],
  
  // UI component libraries
  'vendor-ui': [/* Radix UI components, lucide-react, etc. */],
  
  // 3D visualization libraries (heavy)
  'vendor-3d': ['three', '@react-three/fiber', '@react-three/drei'],
  
  // Data fetching libraries
  'vendor-query': ['@tanstack/react-query', '@tanstack/react-query-devtools'],
  
  // Drag and drop functionality
  'vendor-dnd': ['@dnd-kit/core', '@dnd-kit/sortable', '@dnd-kit/utilities'],
  
  // Feature-specific libraries
  'feature-editor': ['react-image-crop', 'immer'],
  'feature-animation': ['framer-motion'],
}
```

### 3. Bundle Structure

After building, the application creates these main bundles:

- `index-[hash].js`: Main entry point (small)
- `vendor-react-[hash].js`: React core libraries
- `vendor-ui-[hash].js`: UI component libraries
- `vendor-3d-[hash].js`: Three.js libraries (only loaded when needed)
- `admin-[hash].js`: Admin section code (lazy-loaded)
- `editor-[hash].js`: Editor page code (lazy-loaded)

### 4. Route Prefetching

The `useRoutePrefetch` hook provides intelligent prefetching:

- When on login page → prefetch admin routes
- When on editor page → prefetch cart/checkout routes

### 5. Loading States

A dedicated `LazyLoadingFallback` component provides visual feedback during chunk loading.

## Verification

### Build Analysis

Run the bundle analyzer to visualize chunk sizes:

```bash
npm run build:analyze
```

This generates a visual report at `dist/bundle-stats.html`.

### Network Tab Verification

1. Build the production version:
   ```bash
   npm run build
   npm run preview
   ```

2. Open the browser DevTools Network tab

3. Navigate to different sections and observe:
   - Initial page load should NOT load admin chunks
   - Navigating to `/admin` should trigger loading of admin chunks
   - The vendor chunks should be loaded once and cached

### Expected Results

- **Initial Load**: ~200-300KB (excluding images)
- **Admin Section**: Additional ~150-200KB when first accessed
- **3D Preview**: Three.js chunks (~500KB) loaded only when needed

## Best Practices

1. **Lazy Load Heavy Components**: Components using Three.js or other heavy libraries should be lazy-loaded
2. **Group Related Code**: Keep related features in the same chunk to reduce network requests
3. **Monitor Bundle Sizes**: Regularly check bundle sizes with `npm run build:analyze`
4. **Use Prefetching Wisely**: Only prefetch routes that users are likely to visit next

## Performance Tips

1. **CSS Code Splitting**: CSS is automatically split per-chunk in Vite
2. **Image Optimization**: Use appropriate image formats and lazy load images
3. **Caching**: Leverage browser caching with content-hash filenames
4. **Compression**: Enable gzip/brotli compression on the server

## Troubleshooting

### Issue: Chunks Too Large
- Review the `manualChunks` configuration
- Consider splitting large components into smaller ones
- Check for unnecessary dependencies

### Issue: Too Many Small Chunks
- Consolidate related code into fewer chunks
- Adjust the chunking strategy in `vite.config.ts`

### Issue: Slow Initial Load
- Check if vendor chunks are too large
- Consider using CDN for large libraries
- Enable HTTP/2 push for critical resources