import { Alert, AlertDescription } from '@/components/ui/Alert';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { authKeys } from '@/hooks/queries/useAuth';
import { useRegister } from '@/hooks/queries/useRegister';
import { authApi } from '@/lib/api';
import { queryClient } from '@/lib/queryClient';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import { LoginRequest, SessionInfo } from '@/types/auth';
import { useMutation } from '@tanstack/react-query';
import { Info, Loader2, Lock } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useUserDataForm } from '../../hooks/useUserDataForm';

export default function UserDataStep() {
  const userData = useWizardStore((state) => state.userData);
  const isAuthenticated = useWizardStore((state) => state.isAuthenticated);
  const goNext = useWizardStore((state) => state.goNext);
  const setUserData = useWizardStore((state) => state.setUserData);
  const setAuthenticated = useWizardStore((state) => state.setAuthenticated);
  const preserveState = useWizardStore((state) => state.preserveState);
  const { formData, errors, handleChange, validateForm } = useUserDataForm(userData);

  const [authMode, setAuthMode] = useState<'register' | 'login' | null>(null);
  const [password, setPassword] = useState('');
  const [authError, setAuthError] = useState<string | null>(null);
  const [isCheckingEmail, setIsCheckingEmail] = useState(false);

  const registerMutation = useRegister();

  // Custom login mutation that doesn't navigate away
  const loginMutation = useMutation({
    mutationFn: (data: LoginRequest) => authApi.login(data),
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
    },
  });

  // If already authenticated, skip to next step
  useEffect(() => {
    if (isAuthenticated) {
      goNext();
    }
  }, [isAuthenticated, goNext]);

  const handleEmailChange = (value: string) => {
    handleChange('email', value);
    setAuthError(null);
    setAuthMode(null);
  };

  const checkEmailExists = async () => {
    if (!formData.email || errors.email) return;

    setIsCheckingEmail(true);
    setAuthError(null);

    try {
      // Try to login with a dummy password to check if email exists
      await authApi.login({ email: formData.email, password: 'dummy-check' });
    } catch (error: any) {
      if (error.status === 401) {
        // Email exists, switch to login mode
        setAuthMode('login');
      } else if (error.status === 404 || error.message.includes('not found')) {
        // Email doesn't exist, switch to register mode
        setAuthMode('register');
      } else {
        setAuthError('An error occurred. Please try again.');
      }
    } finally {
      setIsCheckingEmail(false);
    }
  };

  const handleAuth = async () => {
    if (!formData.email || !password || !authMode) return;

    setAuthError(null);

    // Preserve wizard state before authentication
    preserveState();

    try {
      if (authMode === 'register') {
        await registerMutation.mutateAsync({ email: formData.email, password });
      } else {
        await loginMutation.mutateAsync({ email: formData.email, password });
      }

      // Save user data to wizard store
      setUserData(formData);
    } catch (error: any) {
      setAuthError(error.message || 'Authentication failed. Please try again.');
    }
  };

  const handleBlur = (field: string) => {
    if (field === 'email' && formData.email && !errors.email && !authMode) {
      checkEmailExists();
    }

    // Always save form data if valid
    if (validateForm()) {
      setUserData(formData);
    }
  };

  // Show loading state while checking authentication
  if (isAuthenticated) {
    return (
      <div className="flex flex-col items-center justify-center py-12">
        <Loader2 className="text-primary h-8 w-8 animate-spin" />
        <p className="mt-2 text-sm text-gray-600">Loading your profile...</p>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-md space-y-6">
      <div>
        <h3 className="mb-2 text-lg font-semibold">Personal Information</h3>
        <p className="text-sm text-gray-600">
          {authMode ? 'Create an account or sign in to save your design' : 'We need some basic information to personalize your product'}
        </p>
      </div>

      <Alert>
        <Lock className="h-4 w-4" />
        <AlertDescription>
          {authMode
            ? 'Your account keeps your designs safe and makes reordering easy. Your information is always secure.'
            : 'Your information is secure and will only be used to process your personalized mug order.'}
        </AlertDescription>
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
            onBlur={() => handleBlur('email')}
            className={errors.email ? 'border-red-500' : ''}
            disabled={isCheckingEmail || registerMutation.isPending || loginMutation.isPending}
          />
          {errors.email && <p className="text-sm text-red-500">{errors.email}</p>}
        </div>

        {authMode && (
          <div className="space-y-2">
            <Label htmlFor="password">
              Password <span className="text-red-500">*</span>
            </Label>
            <Input
              id="password"
              type="password"
              placeholder={authMode === 'register' ? 'Create a password' : 'Enter your password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className={authError ? 'border-red-500' : ''}
              disabled={registerMutation.isPending || loginMutation.isPending}
            />
            {authMode === 'register' && (
              <p className="text-xs text-gray-500">Password must be at least 8 characters with uppercase, lowercase, number, and special character</p>
            )}
          </div>
        )}

        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="firstName">First Name</Label>
            <Input
              id="firstName"
              type="text"
              placeholder="John"
              value={formData.firstName || ''}
              onChange={(e) => handleChange('firstName', e.target.value)}
              onBlur={() => handleBlur('firstName')}
              disabled={registerMutation.isPending || loginMutation.isPending}
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
              onBlur={() => handleBlur('lastName')}
              disabled={registerMutation.isPending || loginMutation.isPending}
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
            onBlur={() => handleBlur('phoneNumber')}
            className={errors.phoneNumber ? 'border-red-500' : ''}
            disabled={registerMutation.isPending || loginMutation.isPending}
          />
          {errors.phoneNumber && <p className="text-sm text-red-500">{errors.phoneNumber}</p>}
        </div>
      </div>

      {authMode && (
        <div className="space-y-3">
          <Button
            onClick={handleAuth}
            disabled={!formData.email || !password || registerMutation.isPending || loginMutation.isPending}
            className="w-full"
          >
            {registerMutation.isPending || loginMutation.isPending ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                {authMode === 'register' ? 'Creating Account...' : 'Signing In...'}
              </>
            ) : authMode === 'register' ? (
              'Create Account & Continue'
            ) : (
              'Sign In & Continue'
            )}
          </Button>

          <p className="text-center text-sm text-gray-600">
            {authMode === 'register' ? (
              <>
                Already have an account?{' '}
                <button
                  type="button"
                  onClick={() => {
                    setAuthMode('login');
                    setAuthError(null);
                  }}
                  className="text-primary font-medium hover:underline"
                >
                  Sign in
                </button>
              </>
            ) : (
              <>
                New to our shop?{' '}
                <button
                  type="button"
                  onClick={() => {
                    setAuthMode('register');
                    setAuthError(null);
                  }}
                  className="text-primary font-medium hover:underline"
                >
                  Create account
                </button>
              </>
            )}
          </p>
        </div>
      )}

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
