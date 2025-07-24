import { countryApi } from '@/lib/api';
import { useQuery } from '@tanstack/react-query';

const QUERY_KEYS = {
  all: ['countries'] as const,
};

export function useCountries() {
  return useQuery({
    queryKey: QUERY_KEYS.all,
    queryFn: countryApi.getAll,
    staleTime: 1000 * 60 * 60, // 1 hour - countries don't change often
  });
}
