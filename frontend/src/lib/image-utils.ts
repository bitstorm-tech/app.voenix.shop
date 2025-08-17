/**
 * Utility functions for image processing and cropping
 */

export interface CropArea {
  x: number;
  y: number;
  width: number;
  height: number;
}

/**
 * Creates a cropped image from a source image URL and crop area
 * @param imageUrl - The source image URL (can be blob URL or regular URL)
 * @param cropArea - The crop area in pixels
 * @returns Promise resolving to a blob URL of the cropped image
 */
export const createCroppedImage = async (imageUrl: string, cropArea: CropArea): Promise<string> => {
  return new Promise((resolve, reject) => {
    const image = new Image();
    image.crossOrigin = 'anonymous';

    image.onload = () => {
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d');

      if (!ctx) {
        reject(new Error('Failed to get canvas context'));
        return;
      }

      canvas.width = cropArea.width;
      canvas.height = cropArea.height;

      ctx.drawImage(image, cropArea.x, cropArea.y, cropArea.width, cropArea.height, 0, 0, cropArea.width, cropArea.height);

      canvas.toBlob(
        (blob) => {
          if (blob) {
            const croppedUrl = URL.createObjectURL(blob);
            resolve(croppedUrl);
          } else {
            reject(new Error('Failed to create blob from canvas'));
          }
        },
        'image/jpeg',
        0.9,
      );
    };

    image.onerror = () => {
      reject(new Error('Failed to load image'));
    };

    image.src = imageUrl;
  });
};

/**
 * Validates if a file is a valid image file
 * @param file - The file to validate
 * @returns Boolean indicating if the file is a valid image
 */
export const isValidImageFile = (file: File): boolean => {
  return file.type.startsWith('image/');
};

/**
 * Validates if a file size is within the allowed limit
 * @param file - The file to validate
 * @param maxSizeInMB - Maximum allowed size in megabytes (default: 4MB)
 * @returns Boolean indicating if the file size is valid
 */
export const isValidImageSize = (file: File, maxSizeInMB: number = 4): boolean => {
  const maxSizeInBytes = maxSizeInMB * 1024 * 1024;
  return file.size <= maxSizeInBytes;
};

/**
 * Cleanup function for blob URLs
 * @param urls - Array of blob URLs to revoke
 */
export const cleanupBlobUrls = (urls: (string | null)[]): void => {
  urls.forEach((url) => {
    if (url && url.startsWith('blob:')) {
      URL.revokeObjectURL(url);
    }
  });
};
