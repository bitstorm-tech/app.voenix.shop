import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import type { CreateMugRequest } from '@/lib/api';

interface CostsTabProps {
  formData: CreateMugRequest;
  setFormData: (data: CreateMugRequest) => void;
}

export default function CostsTab({ formData, setFormData }: CostsTabProps) {
  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <Label htmlFor="price">Price ($)</Label>
        <Input
          id="price"
          type="number"
          step="0.01"
          value={formData.price}
          onChange={(e) => setFormData({ ...formData, price: parseFloat(e.target.value) || 0 })}
          placeholder="0.00"
          required
        />
        <p className="text-sm text-gray-500">Enter the selling price for this mug in USD</p>
      </div>
    </div>
  );
}
