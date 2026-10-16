/**
 * HTTP adapter for the management-status catalog (#1769 / ADR-030).
 * Only place in this catalog that may import api-client.
 */
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import type { ManagementStatus } from "@/core/management-status/domain/management-status";
import type {
  ManagementStatusGateway,
  ManagementStatusInput,
} from "@/core/management-status/application/ports";

/** Wire shape from ManagementStatus / DtoManagementStatus. */
interface ManagementStatusDto {
  idManagementStatus?: number;
  name: string;
  notes?: string | null;
  version?: number;
}

function toDomain(dto: ManagementStatusDto): ManagementStatus {
  return {
    id: dto.idManagementStatus ?? 0,
    name: dto.name,
    notes: dto.notes,
    version: dto.version,
  };
}

export const managementStatusHttpGateway: ManagementStatusGateway = {
  list: async () => {
    const rows = await apiGet<ManagementStatusDto[]>("/management-statuses");
    return rows.map(toDomain);
  },
  searchByName: async (name: string) => {
    const rows = await apiGet<ManagementStatusDto[]>(
      `/management-statuses/search?name=${encodeURIComponent(name)}`,
    );
    return rows.map(toDomain);
  },
  create: async (input: ManagementStatusInput) =>
    toDomain(await apiPost<ManagementStatusDto>("/management-statuses", {
      name: input.name,
      notes: input.notes,
    })),
  update: async (id: number, input: ManagementStatusInput) =>
    toDomain(await apiPut<ManagementStatusDto>(`/management-statuses/${id}`, {
      name: input.name,
      notes: input.notes,
      version: input.version,
    })),
  delete: async (id: number) => {
    await apiDelete(`/management-statuses/${id}`);
  },
  isInUse: async (id: number) => {
    const { inUse } = await apiGet<{ inUse: boolean }>(`/management-statuses/${id}/in-use`);
    return inUse;
  },
};
