/**
 * Server-side deed lookup for pickers (#1340). Forms never load the whole deeds
 * table (GET /deeds?size=1000 filtered to signed deeds in the browser left
 * every deed after the 1000th out): they show the newest signed deeds
 * (GET /deeds?status=Firmada) and search any other by number.
 */
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { apiGet, apiGetPage } from "@/lib/api-client";
import { DEEDS_SORT, deedsKeys } from "@/hooks/useDeeds";
import type { Deed } from "@/types";

/** Newest signed deeds listed before the user types. */
export const DEED_PICKER_RECENT_SIZE = 20;
/** The only deeds a testimony, registration draft or folio can use (BusinessConstants.DeedFIRMADA). */
export const DEED_STATUS_SIGNED = "Firmada";

const isSigned = (deed: Deed) => deed.status === DEED_STATUS_SIGNED;

/** Signed deeds whose number is `raw` (GET /deeds/search); text that is not a number matches none. */
export async function searchSignedDeeds(raw: string): Promise<Deed[]> {
  const q = raw.trim();
  if (!/^\d+$/.test(q)) return [];
  const found = await apiGet<Deed[]>(`/deeds/search?number=${encodeURIComponent(q)}`);
  return found.filter(isSigned).sort((a, b) => (b.idDeed ?? 0) - (a.idDeed ?? 0));
}

export function useDeedSearch(query: string, enabled: boolean) {
  const q = query.trim();
  return useQuery({
    queryKey: ["deeds", "picker-search", q],
    queryFn: () => searchSignedDeeds(q),
    enabled: enabled && q.length > 0,
    placeholderData: keepPreviousData,
  });
}

/** The newest signed deeds, for an empty picker query. */
export function useRecentSignedDeeds(enabled: boolean) {
  return useQuery({
    queryKey: ["deeds", "picker-recent", DEED_STATUS_SIGNED],
    queryFn: () =>
      apiGetPage<Deed>("/deeds", {
        page: 0,
        size: DEED_PICKER_RECENT_SIZE,
        sort: DEEDS_SORT,
        params: { status: DEED_STATUS_SIGNED },
      }),
    enabled,
  });
}

/** One deed by id, so a form shows its current deed whatever page it is on. */
export function useDeed(id: number | undefined, enabled = true) {
  return useQuery({
    queryKey: deedsKeys.detail(id ?? -1),
    queryFn: () => apiGet<Deed>(`/deeds/${id}`),
    enabled: enabled && id != null,
    staleTime: 60_000,
  });
}
