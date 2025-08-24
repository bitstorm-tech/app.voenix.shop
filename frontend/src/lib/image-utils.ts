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
 * Loads an image from a URL
 * @param src - The image URL to load
 * @returns Promise resolving to the loaded HTMLImageElement
 */
const loadImage = (src: string): Promise<HTMLImageElement> => {
  return new Promise((resolve, reject) => {
    const image = new Image();
    image.crossOrigin = 'anonymous';
    image.onload = () => resolve(image);
    image.onerror = () => reject(new Error('Failed to load image'));
    image.src = src;
  });
};

/**
 * Converts a canvas to a blob
 * @param canvas - The canvas to convert
 * @returns Promise resolving to the blob
 */
const canvasToBlob = (canvas: HTMLCanvasElement): Promise<Blob> => {
  return new Promise((resolve, reject) => {
    canvas.toBlob(
      (blob) => blob ? resolve(blob) : reject(new Error('Failed to create blob from canvas')),
      'image/png',
      0.9
    );
  });
};

/**
 * Creates a cropped image from a source image URL and crop area
 * @param imageUrl - The source image URL (can be blob URL or regular URL)
 * @param cropArea - The crop area in pixels
 * @returns Promise resolving to a blob URL of the cropped image
 */
export const createCroppedImage = async (imageUrl: string, cropArea: CropArea): Promise<string> => {
  const image = await loadImage(imageUrl);
  
  const canvas = document.createElement('canvas');
  const ctx = canvas.getContext('2d');
  
  if (!ctx) {
    throw new Error('Failed to get canvas context');
  }

  canvas.width = cropArea.width;
  canvas.height = cropArea.height;

  ctx.drawImage(
    image, 
    cropArea.x, cropArea.y, cropArea.width, cropArea.height,
    0, 0, cropArea.width, cropArea.height
  );

  const blob = await canvasToBlob(canvas);
  return URL.createObjectURL(blob);
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
 * Converts a blob URL to a File object
 * @param blobUrl - The blob URL to convert
 * @param fileName - The name for the resulting file
 * @param mimeType - The MIME type for the file (default: 'image/png')
 * @returns Promise resolving to a File object
 */
export const blobUrlToFile = async (blobUrl: string, fileName: string, mimeType: string = 'image/png'): Promise<File> => {
  const response = await fetch(blobUrl);
  const blob = await response.blob();
  return new File([blob], fileName, { type: mimeType });
};

/**
 * Cleanup function for blob URLs
 * @param urls - Array of blob URLs to revoke
 */
export const cleanupBlobUrls = (urls: string[]): void => {
  urls.forEach((url) => {
    if (url.startsWith('blob:')) {
      URL.revokeObjectURL(url);
    }
  });
};
