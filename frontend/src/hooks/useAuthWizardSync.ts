import { useSession } from '@/hooks/queries/useAuth';
import { useCartMigration } from '@/hooks/queries/useCartMigration';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import { useEffect } from 'react';

export function useAuthWizardSync() {
  const { data: session, isLoading } = useSession();
  const setAuthenticated = useWizardStore((state) => state.setAuthenticated);
  const { isMigrating, migrationError } = useCartMigration(session?.authenticated || false);

  useEffect(() => {
    if (!isLoading && session) {
      setAuthenticated(session.authenticated, session.user || null);
    }
  }, [session, isLoading, setAuthenticated]);

  return {
    isLoading: isLoading || isMigrating,
    isAuthenticated: session?.authenticated || false,
    migrationError,
  };
}
