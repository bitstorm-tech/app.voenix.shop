/**
 * Image preloader service for better cart performance
 */

class ImagePreloader {
  private cache = new Map<string, Promise<void>>();

  /**
   * Preload a single image
   */
  preloadImage(src: string): Promise<void> {
    if (this.cache.has(src)) {
      return this.cache.get(src)!;
    }

    const promise = new Promise<void>((resolve, reject) => {
      const img = new Image();
      img.onload = () => resolve();
      img.onerror = () => reject(new Error(`Failed to preload image: ${src}`));
      img.src = src;
    });

    this.cache.set(src, promise);
    return promise;
  }

  /**
   * Preload multiple images with priority-based ordering
   */
  async preloadImages(sources: Array<{ src: string; priority?: 'high' | 'medium' | 'low' }>): Promise<void> {
    // Sort by priority (high first, then medium, then low)
    const priorityOrder = { high: 0, medium: 1, low: 2 };
    const sortedSources = sources
      .filter((item) => item.src)
      .sort((a, b) => {
        const aPriority = priorityOrder[a.priority || 'medium'];
        const bPriority = priorityOrder[b.priority || 'medium'];
        return aPriority - bPriority;
      });

    // Preload high priority images first
    const highPriority = sortedSources.filter((item) => (item.priority || 'medium') === 'high');
    const mediumPriority = sortedSources.filter((item) => (item.priority || 'medium') === 'medium');
    const lowPriority = sortedSources.filter((item) => (item.priority || 'medium') === 'low');

    // Load high priority images immediately
    if (highPriority.length > 0) {
      await Promise.allSettled(highPriority.map((item) => this.preloadImage(item.src)));
    }

    // Load medium and low priority images with slight delays
    setTimeout(() => {
      Promise.allSettled(mediumPriority.map((item) => this.preloadImage(item.src)));
    }, 100);

    setTimeout(() => {
      Promise.allSettled(lowPriority.map((item) => this.preloadImage(item.src)));
    }, 300);
  }

  /**
   * Clear the preload cache
   */
  clearCache(): void {
    this.cache.clear();
  }

  /**
   * Get cache stats
   */
  getCacheStats(): { size: number; keys: string[] } {
    return {
      size: this.cache.size,
      keys: Array.from(this.cache.keys()),
    };
  }
}

// Export singleton instance
export const imagePreloader = new ImagePreloader();
