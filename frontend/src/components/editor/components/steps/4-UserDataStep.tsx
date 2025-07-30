import { Alert, AlertDescription } from '@/components/ui/Alert';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { authKeys } from '@/hooks/queries/useAuth';
import { authApi } from '@/lib/api';
import { queryClient } from '@/lib/queryClient';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import { SessionInfo } from '@/types/auth';
import { useMutation } from '@tanstack/react-query';
import { Info, Lock } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { useUserDataForm } from '../../hooks/useUserDataForm';

export default function UserDataStep() {
  const userData = useWizardStore((state) => state.userData);
  const setUserData = useWizardStore((state) => state.setUserData);
  const setAuthenticated = useWizardStore((state) => state.setAuthenticated);
  const goNext = useWizardStore((state) => state.goNext);
  const { formData, errors, handleChange } = useUserDataForm(userData);
  const [authError, setAuthError] = useState<string | null>(null);
  const hasRegisteredRef = useRef(false);

  // Custom register guest mutation
  const registerGuestMutation = useMutation({
    mutationFn: () => authApi.registerGuest(formData),
    onSuccess: async (data) => {
      // Update session in cache
      queryClient.setQueryData<SessionInfo>(authKeys.session(), {
        authenticated: true,
        user: { ...data.user, roles: data.roles },
        roles: data.roles,
      });

      // Invalidate queries to refetch with new auth state
      await queryClient.invalidateQueries();

      // Set authenticated state in wizard store
      setAuthenticated(true, data.user);

      // Mark that we've registered
      hasRegisteredRef.current = true;

      // Navigate to next step after successful registration
      goNext();
    },
    onError: (error: any) => {
      console.error('Registration error:', error);
      setAuthError(error.message || 'Registration failed. You can still continue as a guest.');
      // User can still proceed without registration
    },
  });

  // Update wizard store when form data changes
  useEffect(() => {
    setUserData(formData);
  }, [formData, setUserData]);

  // Intercept navigation to handle registration
  useEffect(() => {
    const handleNextClick = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      const button = target.closest('button');

      // Check if this is the Next button and we're on the user-data step
      const currentStep = useWizardStore.getState().currentStep;
      const canGoNext = useWizardStore.getState().canGoNext;

      if (
        button &&
        button.textContent?.includes('Next') &&
        currentStep === 'user-data' &&
        canGoNext &&
        !hasRegisteredRef.current &&
        !registerGuestMutation.isPending
      ) {
        e.preventDefault();
        e.stopPropagation();

        // Clear any previous errors
        setAuthError(null);

        // Attempt to register the user as a guest
        registerGuestMutation.mutate();
      }
    };

    // Add event listener to capture clicks on the Next button
    document.addEventListener('click', handleNextClick, true);

    return () => {
      document.removeEventListener('click', handleNextClick, true);
    };
  }, [registerGuestMutation]);

  const handleEmailChange = (value: string) => {
    handleChange('email', value);
    setAuthError(null);
    // Reset registration status when email changes
    hasRegisteredRef.current = false;
  };

  return (
    <div className="mx-auto max-w-md space-y-6">
      <div>
        <h3 className="mb-2 text-lg font-semibold">Personal Information</h3>
        <p className="text-sm text-gray-600">We need some basic information to personalize your product</p>
      </div>

      <Alert>
        <Lock className="h-4 w-4" />
        <AlertDescription>Your information is secure and will only be used to process your personalized mug order.</AlertDescription>
      </Alert>

      {authError && (
        <Alert variant="destructive">
          <AlertDescription>{authError}</AlertDescription>
        </Alert>
      )}

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
            className={errors.email ? 'border-red-500' : ''}
            disabled={registerGuestMutation.isPending}
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
              disabled={registerGuestMutation.isPending}
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
              disabled={registerGuestMutation.isPending}
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
            className={errors.phoneNumber ? 'border-red-500' : ''}
            disabled={registerGuestMutation.isPending}
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
