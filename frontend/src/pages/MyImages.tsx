import { ImageSkeleton } from '@/components/ImageSkeleton';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import { AppHeader } from '@/components/layout/AppHeader';
import { Button } from '@/components/ui/Button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { useSession } from '@/hooks/queries/useAuth';
import { useUserImages } from '@/hooks/queries/useUserImages';
import { useImageWithFallback } from '@/hooks/useImageWithFallback';
import type { UserImage, UserImagesParams } from '@/types/userImage';
import { AlertTriangle, ChevronLeft, ChevronRight, Download, ImageIcon, Palette, Upload } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

const ImageCard = ({ image }: { image: UserImage }) => {
  const imageUrl = image.imageUrl || `/api/user/images/${image.filename}`;
  const { isLoaded, isError, retry } = useImageWithFallback(imageUrl, {
    maxRetries: 3,
    preload: true,
  });

  const navigate = useNavigate();

  const handleDownload = () => {
    const link = document.createElement('a');
    link.href = imageUrl;
    link.download = image.originalFilename || image.filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleDesignMug = () => {
    navigate(`/editor?image=${image.filename}`);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const formatFileSize = (bytes?: number) => {
    if (!bytes) return 'Unknown size';
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    if (bytes === 0) return '0 Bytes';
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return Math.round((bytes / Math.pow(1024, i)) * 100) / 100 + ' ' + sizes[i];
  };

  return (
    <div className="group relative overflow-hidden rounded-lg bg-white shadow-sm transition-shadow hover:shadow-md">
      <div className="aspect-square overflow-hidden bg-gray-100">
        {!isLoaded && !isError && <ImageSkeleton className="h-full w-full" showIcon={false} />}
        {isError && (
          <div className="flex h-full flex-col items-center justify-center p-4">
            <ImageIcon className="mb-2 h-8 w-8 text-gray-400" />
            <p className="text-xs text-gray-500">Failed to load image</p>
            <Button onClick={retry} size="sm" variant="ghost" className="mt-2">
              Retry
            </Button>
          </div>
        )}
        {isLoaded && (
          <img
            src={imageUrl}
            alt={image.originalFilename || 'User image'}
            className="h-full w-full object-cover transition-transform group-hover:scale-105"
          />
        )}
      </div>

      <div className="p-4">
        <div className="mb-2 flex items-center justify-between">
          <span
            className={`inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ${
              image.type === 'generated' ? 'bg-purple-100 text-purple-800' : 'bg-blue-100 text-blue-800'
            }`}
          >
            {image.type === 'generated' ? 'Generated' : 'Uploaded'}
          </span>
          <span className="text-xs text-gray-500">{formatDate(image.createdAt)}</span>
        </div>

        {image.originalFilename && (
          <p className="mb-1 truncate text-sm font-medium text-gray-900" title={image.originalFilename}>
            {image.originalFilename}
          </p>
        )}

        {image.promptTitle && (
          <p className="mb-1 truncate text-xs text-gray-600" title={image.promptTitle}>
            Prompt: {image.promptTitle}
          </p>
        )}

        <p className="text-xs text-gray-500">{formatFileSize(image.fileSize)}</p>

        <div className="mt-3 flex gap-2">
          <Button onClick={handleDownload} size="sm" variant="outline" className="flex-1">
            <Download className="mr-1 h-3 w-3" />
            Download
          </Button>
          <Button onClick={handleDesignMug} size="sm" variant="default" className="flex-1">
            <Palette className="mr-1 h-3 w-3" />
            Design Mug
          </Button>
        </div>
      </div>
    </div>
  );
};

export default function MyImagesPage() {
  const navigate = useNavigate();
  const { data: session, isLoading: sessionLoading } = useSession();
  const [filters, setFilters] = useState<UserImagesParams>({
    page: 0,
    size: 20,
    type: 'all',
    sortBy: 'createdAt',
    sortDirection: 'DESC',
  });

  const { data: imagesData, isLoading: imagesLoading, error: imagesError } = useUserImages(filters);

  // Redirect to login if not authenticated
  useEffect(() => {
    if (!sessionLoading && !session?.authenticated) {
      navigate('/login?redirect=' + encodeURIComponent('/my-images'));
    }
  }, [session, sessionLoading, navigate]);

  // Don't render anything while checking authentication
  if (sessionLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <LoadingSpinner />
      </div>
    );
  }

  // Don't render if not authenticated (redirect will happen)
  if (!session?.authenticated) {
    return null;
  }

  const handleFilterChange = (key: keyof UserImagesParams, value: any) => {
    setFilters((prev) => ({
      ...prev,
      [key]: value,
      page: key === 'page' ? value : 0, // Reset page when changing other filters
    }));
  };

  const handlePageChange = (newPage: number) => {
    setFilters((prev) => ({ ...prev, page: newPage }));
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  // Loading state
  if (imagesLoading && !imagesData) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <LoadingSpinner />
          <p className="mt-2 text-sm text-gray-600">Loading your images...</p>
        </div>
      </div>
    );
  }

  // Error state
  if (imagesError) {
    return (
      <div className="flex min-h-screen items-center justify-center px-4">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 text-red-500">
            <AlertTriangle className="h-12 w-12" />
          </div>
          <h2 className="mt-4 text-lg font-medium text-gray-900">Error loading images</h2>
          <p className="mt-2 text-sm text-gray-600">{imagesError instanceof Error ? imagesError.message : 'Unable to load your images'}</p>
          <Button onClick={() => window.location.reload()} className="mt-6">
            Try Again
          </Button>
        </div>
      </div>
    );
  }

  const images = imagesData?.content || [];
  const totalPages = imagesData?.totalPages || 0;
  const currentPage = filters.page || 0;
  const totalElements = imagesData?.totalElements || 0;

  return (
    <div className="min-h-screen bg-gray-50">
      <AppHeader />
      <div className="mx-auto max-w-7xl px-4 pt-8 pb-8 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-gray-900 sm:text-3xl">My Images</h1>
          <p className="mt-2 text-gray-600">View and manage your uploaded and generated images</p>
        </div>

        {/* Filters */}
        <div className="mb-6 flex flex-wrap gap-4 rounded-lg bg-white p-4 shadow-sm">
          <div className="flex items-center gap-2">
            <label htmlFor="type-filter" className="text-sm font-medium text-gray-700">
              Type:
            </label>
            <Select value={filters.type} onValueChange={(value) => handleFilterChange('type', value)}>
              <SelectTrigger id="type-filter" className="w-[180px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Images</SelectItem>
                <SelectItem value="uploaded">Uploaded Only</SelectItem>
                <SelectItem value="generated">Generated Only</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="flex items-center gap-2">
            <label htmlFor="sort-filter" className="text-sm font-medium text-gray-700">
              Sort by:
            </label>
            <Select value={filters.sortBy} onValueChange={(value) => handleFilterChange('sortBy', value)}>
              <SelectTrigger id="sort-filter" className="w-[180px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="createdAt">Date</SelectItem>
                <SelectItem value="type">Type</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="flex items-center gap-2">
            <label htmlFor="order-filter" className="text-sm font-medium text-gray-700">
              Order:
            </label>
            <Select value={filters.sortDirection} onValueChange={(value) => handleFilterChange('sortDirection', value)}>
              <SelectTrigger id="order-filter" className="w-[180px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="DESC">Newest First</SelectItem>
                <SelectItem value="ASC">Oldest First</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        {/* Results count */}
        {totalElements > 0 && (
          <div className="mb-4 text-sm text-gray-600">
            Showing {images.length} of {totalElements} image{totalElements !== 1 ? 's' : ''}
          </div>
        )}

        {/* Images Grid or Empty State */}
        {images.length === 0 ? (
          <div className="py-12 text-center">
            <Upload className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-4 text-lg font-medium text-gray-900">No images yet</h3>
            <p className="mt-2 text-gray-600">
              {filters.type === 'uploaded'
                ? "You haven't uploaded any images yet."
                : filters.type === 'generated'
                  ? "You haven't generated any images yet."
                  : 'Upload or generate images to see them here.'}
            </p>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
              {images.map((image) => (
                <ImageCard key={image.id} image={image} />
              ))}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="mt-8 flex items-center justify-center gap-2">
                <Button onClick={() => handlePageChange(currentPage - 1)} disabled={currentPage === 0} variant="outline" size="sm">
                  <ChevronLeft className="h-4 w-4" />
                  Previous
                </Button>

                <div className="flex gap-1">
                  {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
                    let pageNumber = i;
                    if (totalPages > 5) {
                      if (currentPage < 3) {
                        pageNumber = i;
                      } else if (currentPage > totalPages - 3) {
                        pageNumber = totalPages - 5 + i;
                      } else {
                        pageNumber = currentPage - 2 + i;
                      }
                    }

                    if (pageNumber >= 0 && pageNumber < totalPages) {
                      return (
                        <Button
                          key={pageNumber}
                          onClick={() => handlePageChange(pageNumber)}
                          variant={currentPage === pageNumber ? 'default' : 'outline'}
                          size="sm"
                          className="min-w-[40px]"
                        >
                          {pageNumber + 1}
                        </Button>
                      );
                    }
                    return null;
                  })}
                </div>

                <Button onClick={() => handlePageChange(currentPage + 1)} disabled={currentPage === totalPages - 1} variant="outline" size="sm">
                  Next
                  <ChevronRight className="h-4 w-4" />
                </Button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
