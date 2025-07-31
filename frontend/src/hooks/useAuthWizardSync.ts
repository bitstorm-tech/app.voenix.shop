import { useSession } from '@/hooks/queries/useAuth';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import { useEffect } from 'react';

export function useAuthWizardSync() {
  const { data: session, isLoading } = useSession();
  const setAuthenticated = useWizardStore((state) => state.setAuthenticated);

  useEffect(() => {
    if (!isLoading && session) {
      setAuthenticated(session.authenticated, session.user || null);
    }
  }, [session, isLoading, setAuthenticated]);

  return { isLoading, isAuthenticated: session?.authenticated || false };
}
