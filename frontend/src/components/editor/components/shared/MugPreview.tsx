import { cn } from '@/lib/utils';
import { GeneratedImageCropData, MugOption } from '../../types';
import MugPreview3D from './MugPreview3D';

interface MugPreviewProps {
  mug: MugOption;
  imageUrl: string;
  cropData?: GeneratedImageCropData | null;
  className?: string;
}

export default function MugPreview({ mug, imageUrl, cropData, className }: MugPreviewProps) {
  return (
    <div className={cn('relative', className)}>
      <MugPreview3D mug={mug} imageUrl={imageUrl} cropData={cropData || null} />

      <div className="mt-6 text-center">
        <h4 className="text-lg font-semibold">{mug.name}</h4>
        <p className="mt-1 text-sm text-gray-600">{mug.capacity}</p>
        {mug.special && (
          <span className="mt-2 inline-block rounded-full bg-yellow-100 px-3 py-1 text-sm font-medium text-yellow-800">{mug.special}</span>
        )}
      </div>
    </div>
  );
}
