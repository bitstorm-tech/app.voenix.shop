import { Alert, AlertDescription } from '@/components/ui/Alert';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { authKeys } from '@/hooks/queries/useAuth';
import { authApi } from '@/lib/api';
import { queryClient } from '@/lib/queryClient';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import { SessionInfo } from '@/types/auth';
import { useMutation } from '@tanstack/react-query';
import { CheckCircle, Info, Lock } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useUserDataForm } from '../../hooks/useUserDataForm';

export default function UserDataStep() {
  const { t } = useTranslation('editor');
  const userData = useWizardStore((state) => state.userData);
  const setUserData = useWizardStore((state) => state.setUserData);
  const setAuthenticated = useWizardStore((state) => state.setAuthenticated);
  const goNext = useWizardStore((state) => state.goNext);
  const isAuthenticated = useWizardStore((state) => state.isAuthenticated);
  const user = useWizardStore((state) => state.user);
  const { formData, errors, handleChange } = useUserDataForm(userData);
  const [authError, setAuthError] = useState<string | null>(null);
  const hasRegisteredRef = useRef(false);
  const [showForm, setShowForm] = useState(false);
  const accountInfoItems = (t('steps.userData.info.items', { returnObjects: true }) as string[]) ?? [];
  const infoWhyItems = (t('steps.userData.info.whyItems', { returnObjects: true }) as string[]) ?? [];

  // Custom register guest mutation
  const registerGuestMutation = useMutation({
    mutationFn: () => authApi.registerGuest(formData),
    onSuccess: async (data) => {
      queryClient.setQueryData<SessionInfo>(authKeys.session(), {
        authenticated: true,
        user: { ...data.user, roles: data.roles },
        roles: data.roles,
      });

      await queryClient.invalidateQueries();

      setAuthenticated(true, data.user);

      // Mark that we've registered
      hasRegisteredRef.current = true;

      // Navigate to next step after successful registration
      goNext();
    },
    onError: (error: unknown) => {
      console.error('Registration error:', error);
      const message = error instanceof Error ? error.message : t('errors.registration');
      setAuthError(message);
      // User can still proceed without registration
    },
  });

  useEffect(() => {
    setUserData(formData);
  }, [formData, setUserData]);

  // Intercept navigation to handle registration
  useEffect(() => {
    const handleNextClick = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      const button = target.closest('button');

      const currentStep = useWizardStore.getState().currentStep;
      const canGoNext = useWizardStore.getState().canGoNext;

      if (
        button &&
        button instanceof HTMLElement &&
        button.dataset.wizardRole === 'next' &&
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

  // If authenticated, show a different view
  if (isAuthenticated && !showForm) {
    return (
      <div className="mx-auto max-w-md space-y-6">
        <div>
          <h3 className="mb-2 text-lg font-semibold">{t('steps.userData.title')}</h3>
          <p className="text-sm text-gray-600">{t('steps.userData.loggedIn')}</p>
        </div>

        <Alert className="border-green-200 bg-green-50">
          <CheckCircle className="h-4 w-4 text-green-600" />
          <AlertDescription className="text-green-800">
            {t('steps.userData.loggedInDescription')}
            {user?.email && <span className="mt-1 block font-medium">{t('steps.userData.loggedInEmail', { email: user.email })}</span>}
          </AlertDescription>
        </Alert>

        <div className="space-y-3">
          <Button onClick={goNext} className="w-full" size="lg">
            {t('steps.userData.cta.continue')}
          </Button>

          <Button onClick={() => setShowForm(true)} variant="outline" className="w-full">
            {t('steps.userData.cta.useDifferent')}
          </Button>
        </div>

        <div className="rounded-lg bg-blue-50 p-4">
          <div className="flex gap-3">
            <Info className="h-5 w-5 flex-shrink-0 text-blue-600" />
            <div className="text-sm text-blue-800">
              <p>{t('steps.userData.info.heading')}</p>
              <ul className="mt-1 list-inside list-disc space-y-1">
                {accountInfoItems.map((item, index) => (
                  <li key={index}>{item}</li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-md space-y-6">
      <div>
        <h3 className="mb-2 text-lg font-semibold">{t('steps.userData.title')}</h3>
        <p className="text-sm text-gray-600">
          {isAuthenticated && showForm ? t('steps.userData.form.alternateDescription') : t('steps.userData.form.defaultDescription')}
        </p>
      </div>

      {isAuthenticated && showForm && (
        <Alert className="border-blue-200 bg-blue-50">
          <Info className="h-4 w-4 text-blue-600" />
          <AlertDescription className="text-blue-800">{t('steps.userData.form.alternateNotice')}</AlertDescription>
        </Alert>
      )}

      {!isAuthenticated && (
        <Alert>
          <Lock className="h-4 w-4" />
          <AlertDescription>{t('steps.userData.form.guestSecure')}</AlertDescription>
        </Alert>
      )}

      {authError && (
        <Alert variant="destructive">
          <AlertDescription>{authError}</AlertDescription>
        </Alert>
      )}

      <div className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="email">
            {t('steps.userData.fields.email.label')} <span className="text-red-500">*</span>
          </Label>
          <Input
            id="email"
            type="email"
            placeholder={t('steps.userData.fields.email.placeholder')}
            value={formData.email}
            onChange={(e) => handleEmailChange(e.target.value)}
            className={errors.email ? 'border-red-500' : ''}
            disabled={registerGuestMutation.isPending}
          />
          {errors.email && <p className="text-sm text-red-500">{errors.email}</p>}
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="firstName">{t('steps.userData.fields.firstName.label')}</Label>
            <Input
              id="firstName"
              type="text"
              placeholder={t('steps.userData.fields.firstName.placeholder')}
              value={formData.firstName || ''}
              onChange={(e) => handleChange('firstName', e.target.value)}
              disabled={registerGuestMutation.isPending}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="lastName">{t('steps.userData.fields.lastName.label')}</Label>
            <Input
              id="lastName"
              type="text"
              placeholder={t('steps.userData.fields.lastName.placeholder')}
              value={formData.lastName || ''}
              onChange={(e) => handleChange('lastName', e.target.value)}
              disabled={registerGuestMutation.isPending}
            />
          </div>
        </div>

        <div className="space-y-2">
          <Label htmlFor="phoneNumber">{t('steps.userData.fields.phone.label')}</Label>
          <Input
            id="phoneNumber"
            type="tel"
            placeholder={t('steps.userData.fields.phone.placeholder')}
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
            <p className="font-medium">{t('steps.userData.info.whyHeading')}</p>
            <ul className="mt-1 list-inside list-disc space-y-1">
              {infoWhyItems.map((item, index) => (
                <li key={index}>{item}</li>
              ))}
            </ul>
          </div>
        </div>
      </div>

      {isAuthenticated && showForm && (
        <Button onClick={() => setShowForm(false)} variant="outline" className="w-full">
          {t('steps.userData.cta.useAccount')}
        </Button>
      )}
    </div>
  );
}
