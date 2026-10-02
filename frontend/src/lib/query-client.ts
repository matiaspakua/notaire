import { MutationCache, QueryCache, QueryClient } from "@tanstack/react-query";
import { handleSessionExpiryFromUnknown } from "@/lib/session-expiry";

export const queryClient = new QueryClient({
  queryCache: new QueryCache({
    // Safety net: api-client already handles 401; this covers thrown ApiError from queries.
    onError: (error) => {
      handleSessionExpiryFromUnknown(error);
    },
  }),
  mutationCache: new MutationCache({
    onError: (error) => {
      handleSessionExpiryFromUnknown(error);
    },
  }),
  defaultOptions: {
    queries: {
      staleTime: 1000 * 30, // 30 seconds
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});
