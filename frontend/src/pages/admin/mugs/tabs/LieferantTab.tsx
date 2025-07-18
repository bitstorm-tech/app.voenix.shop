import type { CreateMugRequest } from '@/lib/api';

interface LieferantTabProps {
  formData: CreateMugRequest;
  setFormData: (data: CreateMugRequest) => void;
}

export default function LieferantTab({}: LieferantTabProps) {
  return (
    <div className="space-y-6">
      <div className="text-center text-gray-500">
        <p>Lieferant tab content coming soon...</p>
      </div>
    </div>
  );
}
