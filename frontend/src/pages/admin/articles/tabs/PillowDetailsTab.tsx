import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { FieldLabel } from '@/components/ui/FieldLabel';
import { Input } from '@/components/ui/Input';
import { Switch } from '@/components/ui/Switch';
import type { CreatePillowDetailsRequest } from '@/types/article';

interface PillowDetailsTabProps {
  pillowDetails?: Partial<CreatePillowDetailsRequest>;
  onChange: (details: CreatePillowDetailsRequest) => void;
}

export default function PillowDetailsTab({ pillowDetails, onChange }: PillowDetailsTabProps) {
  const details: CreatePillowDetailsRequest = {
    widthCm: pillowDetails?.widthCm || 0,
    heightCm: pillowDetails?.heightCm || 0,
    depthCm: pillowDetails?.depthCm || 0,
    material: pillowDetails?.material || '',
    fillingType: pillowDetails?.fillingType || '',
    coverRemovable: pillowDetails?.coverRemovable ?? true,
    washable: pillowDetails?.washable ?? true,
  };

  const handleChange = (field: keyof CreatePillowDetailsRequest, value: any) => {
    onChange({ ...details, [field]: value });
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Pillow Specifications</CardTitle>
        <CardDescription>Dimensions and material properties</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-3 gap-4">
          <div className="space-y-2">
            <FieldLabel htmlFor="widthCm" required>
              Width (cm)
            </FieldLabel>
            <Input
              id="widthCm"
              type="number"
              value={details.widthCm}
              onChange={(e) => handleChange('widthCm', Number(e.target.value))}
              placeholder="40"
              min="0"
            />
          </div>

          <div className="space-y-2">
            <FieldLabel htmlFor="heightCm" required>
              Height (cm)
            </FieldLabel>
            <Input
              id="heightCm"
              type="number"
              value={details.heightCm}
              onChange={(e) => handleChange('heightCm', Number(e.target.value))}
              placeholder="40"
              min="0"
            />
          </div>

          <div className="space-y-2">
            <FieldLabel htmlFor="depthCm" required>
              Depth (cm)
            </FieldLabel>
            <Input
              id="depthCm"
              type="number"
              value={details.depthCm}
              onChange={(e) => handleChange('depthCm', Number(e.target.value))}
              placeholder="15"
              min="0"
            />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <FieldLabel htmlFor="material" required>
              Material
            </FieldLabel>
            <Input id="material" value={details.material} onChange={(e) => handleChange('material', e.target.value)} placeholder="Cotton blend" />
          </div>

          <div className="space-y-2">
            <FieldLabel htmlFor="fillingType" required>
              Filling Type
            </FieldLabel>
            <Input
              id="fillingType"
              value={details.fillingType}
              onChange={(e) => handleChange('fillingType', e.target.value)}
              placeholder="Polyester fiber"
            />
          </div>
        </div>

        <div className="space-y-4">
          <div className="flex items-center space-x-2">
            <Switch id="coverRemovable" checked={details.coverRemovable} onCheckedChange={(checked) => handleChange('coverRemovable', checked)} />
            <FieldLabel htmlFor="coverRemovable">Removable Cover</FieldLabel>
          </div>

          <div className="flex items-center space-x-2">
            <Switch id="washable" checked={details.washable} onCheckedChange={(checked) => handleChange('washable', checked)} />
            <FieldLabel htmlFor="washable">Machine Washable</FieldLabel>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
