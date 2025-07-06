import { Alert, AlertDescription } from '@/components/ui/Alert';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Info, Lock } from 'lucide-react';
import { useWizardContext } from '../../contexts/WizardContext';
import { useUserDataForm } from '../../hooks/useUserDataForm';

export default function UserDataStep() {
  const { userData, setUserData } = useWizardContext();
  const { formData, errors, handleChange, validateForm } = useUserDataForm(userData);

  const handleEmailChange = (value: string) => {
    handleChange('email', value);
    // Check if email is valid and update parent state immediately
    const isValidEmail = value && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
    if (isValidEmail) {
      setUserData({ ...formData, email: value });
    }
  };

  const handleBlur = () => {
    if (validateForm()) {
      setUserData(formData);
    }
  };

  return (
    <div className="mx-auto max-w-md space-y-6">
      <div>
        <h3 className="mb-2 text-lg font-semibold">Personal Information</h3>
        <p className="text-sm text-gray-600">We need some basic information to personalize your product</p>
      </div>

      <Alert>
        <Lock className="h-4 w-4" />
        <AlertDescription>
          Your information is secure and will only be used to process your personalized mug order. We need this data to ensure your custom design is
          properly saved and associated with your order.
        </AlertDescription>
      </Alert>

      <div className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="email">
            Email Address <span className="text-red-500">*</span>
          </Label>
          <Input
            id="email"
            type="email"
            placeholder="your.email@example.com"
            value={formData.email}
            onChange={(e) => handleEmailChange(e.target.value)}
            onBlur={handleBlur}
            className={errors.email ? 'border-red-500' : ''}
          />
          {errors.email && <p className="text-sm text-red-500">{errors.email}</p>}
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="firstName">First Name</Label>
            <Input
              id="firstName"
              type="text"
              placeholder="John"
              value={formData.firstName || ''}
              onChange={(e) => handleChange('firstName', e.target.value)}
              onBlur={handleBlur}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="lastName">Last Name</Label>
            <Input
              id="lastName"
              type="text"
              placeholder="Doe"
              value={formData.lastName || ''}
              onChange={(e) => handleChange('lastName', e.target.value)}
              onBlur={handleBlur}
            />
          </div>
        </div>

        <div className="space-y-2">
          <Label htmlFor="phoneNumber">Phone Number</Label>
          <Input
            id="phoneNumber"
            type="tel"
            placeholder="+1 (555) 123-4567"
            value={formData.phoneNumber || ''}
            onChange={(e) => handleChange('phoneNumber', e.target.value)}
            onBlur={handleBlur}
            className={errors.phoneNumber ? 'border-red-500' : ''}
          />
          {errors.phoneNumber && <p className="text-sm text-red-500">{errors.phoneNumber}</p>}
        </div>
      </div>

      <div className="rounded-lg bg-blue-50 p-4">
        <div className="flex gap-3">
          <Info className="h-5 w-5 flex-shrink-0 text-blue-600" />
          <div className="text-sm text-blue-800">
            <p className="font-medium">Why we need this information:</p>
            <ul className="mt-1 list-inside list-disc space-y-1">
              <li>To save your personalized design</li>
              <li>To send you order updates</li>
              <li>To contact you if there are any issues with your design</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}
