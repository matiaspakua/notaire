/**
 * Server-side deed lookup for pickers (#1340). Forms never load the whole deeds
 * table (GET /escrituras?size=1000 filtered to signed deeds in the browser left
 * every deed after the 1000th out): they show the newest signed deeds
 * (GET /escrituras?status=Firmada) and search any other by number.
 */
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { apiGet, apiGetPage } from "@/lib/api-client";
import { ESCRITURAS_SORT, escriturasKeys } from "@/hooks/useEscrituras";
import type { Escritura } from "@/types";

/** Newest signed deeds listed before the user types. */
export const DEED_PICKER_RECENT_SIZE = 20;
/** The only deeds a testimony, registration draft or folio can use (BusinessConstants.DeedFIRMADA). */
export const DEED_STATUS_SIGNED = "Firmada";

const isSigned = (d: Escritura) => d.status === DEED_STATUS_SIGNED;

/** Signed deeds whose number is `raw` (GET /escrituras/buscar); text that is not a number matches none. */
export async function searchSignedDeeds(raw: string): Promise<Escritura[]> {
  const q = raw.trim();
  if (!/^\d+$/.test(q)) return [];
  const found = await apiGet<Escritura[]>(`/escrituras/buscar?number=${encodeURIComponent(q)}`);
  return found.filter(isSigned).sort((a, b) => (b.idDeed ?? 0) - (a.idDeed ?? 0));
}

export function useDeedSearch(query: string, enabled: boolean) {
  const q = query.trim();
  return useQuery({
    queryKey: ["escrituras", "picker-search", q],
    queryFn: () => searchSignedDeeds(q),
    enabled: enabled && q.length > 0,
    placeholderData: keepPreviousData,
  });
}

/** The newest signed deeds, for an empty picker query. */
export function useRecentSignedDeeds(enabled: boolean) {
  return useQuery({
    queryKey: ["escrituras", "picker-recent", DEED_STATUS_SIGNED],
    queryFn: () =>
      apiGetPage<Escritura>("/escrituras", {
        page: 0,
        size: DEED_PICKER_RECENT_SIZE,
        sort: ESCRITURAS_SORT,
        params: { status: DEED_STATUS_SIGNED },
      }),
    enabled,
  });
}

/** One deed by id, so a form shows its current deed whatever page it is on. */
export function useDeed(id: number | undefined, enabled = true) {
  return useQuery({
    queryKey: escriturasKeys.detail(id ?? -1),
    queryFn: () => apiGet<Escritura>(`/escrituras/${id}`),
    enabled: enabled && id != null,
    staleTime: 60_000,
  });
}
