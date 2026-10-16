/**
 * HTTP adapter for folio instance admin (#1785 / ADR-030).
 * Only place in this catalog that may import api-client.
 */
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import type { AvailableNotary, Folio, FolioWriteInput } from "@/core/folio/domain/folio";
import type { FolioGateway } from "@/core/folio/application/ports";

interface PersonDto {
  personId?: number;
  firstName?: string;
  lastName?: string;
  notaryRegistrationNumber?: number;
}

export const folioHttpGateway: FolioGateway = {
  list: async () => apiGet<Folio[]>("/folios"),
  searchByStatus: async (status: string) =>
    apiGet<Folio[]>(`/folios/search?status=${encodeURIComponent(status)}`),
  create: async (input: FolioWriteInput) => {
    await apiPost("/folios", input);
  },
  update: async (id: number, input: FolioWriteInput) => {
    await apiPut(`/folios/${id}`, input);
  },
  delete: async (id: number) => {
    await apiDelete(`/folios/${id}`);
  },
  listAvailableNotaries: async () => {
    const rows = await apiGet<PersonDto[]>("/deeds/available-notaries");
    return rows.map(
      (p): AvailableNotary => ({
        personId: p.personId ?? 0,
        firstName: p.firstName,
        lastName: p.lastName,
        notaryRegistrationNumber: p.notaryRegistrationNumber,
      }),
    );
  },
};
