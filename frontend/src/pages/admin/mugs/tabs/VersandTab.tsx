import type { CreateMugRequest } from '@/lib/api';

interface VersandTabProps {
  formData: CreateMugRequest;
  setFormData: (data: CreateMugRequest) => void;
}

export default function VersandTab({}: VersandTabProps) {
  return (
    <div className="space-y-6">
      <div className="text-center text-gray-500">
        <p>Versand tab content coming soon...</p>
      </div>
    </div>
  );
}
