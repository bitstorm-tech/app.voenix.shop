// Utility to prefetch route chunks for better performance
export const prefetchRoutes = {
  // Prefetch admin routes when user hovers over admin links
  admin: () => {
    import('@/routes/AdminRoutes');
  },

  // Prefetch editor when hovering over editor links
  editor: () => {
    import('@/pages/Editor');
  },

  // Prefetch cart and checkout together as they're often used in sequence
  checkout: () => {
    import('@/pages/Cart');
    import('@/pages/Checkout');
  },

  // Prefetch all major routes (use sparingly, e.g., after initial load)
  all: () => {
    // Delay prefetch to not interfere with initial page load
    setTimeout(() => {
      import('@/routes/AdminRoutes');
      import('@/pages/Editor');
      import('@/pages/Cart');
      import('@/pages/Checkout');
    }, 2000);
  },
};
