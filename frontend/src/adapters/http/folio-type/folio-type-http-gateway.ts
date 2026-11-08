/**
 * HTTP adapter for the folio-type catalog (#1785 / ADR-030).
 * Only place in this catalog that may import api-client.
 */
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import type { FolioType } from "@/core/folio-type/domain/folio-type";
import type { FolioTypeGateway, FolioTypeInput } from "@/core/folio-type/application/ports";

/** Wire shape from FolioType / DtoFolioType. */
interface FolioTypeDto {
  idFolioType?: number;
  name?: string;
  isAuxiliary?: boolean;
  notes?: string;
  enabled?: boolean;
  version?: number;
}

function toDomain(dto: FolioTypeDto): FolioType {
  return {
    id: dto.idFolioType ?? 0,
    name: dto.name ?? "",
    isAuxiliary: dto.isAuxiliary,
    notes: dto.notes,
    enabled: dto.enabled,
    version: dto.version,
  };
}

function toWire(input: FolioTypeInput): FolioTypeDto {
  const dto: FolioTypeDto = { name: input.name };
  if (input.isAuxiliary !== undefined) dto.isAuxiliary = input.isAuxiliary;
  if (input.notes !== undefined) dto.notes = input.notes;
  if (input.enabled !== undefined) dto.enabled = input.enabled;
  if (input.version !== undefined) dto.version = input.version;
  return dto;
}

export const folioTypeHttpGateway: FolioTypeGateway = {
  list: async () => {
    const rows = await apiGet<FolioTypeDto[]>("/folio-types");
    return rows.map(toDomain);
  },
  searchByName: async (name: string) => {
    const rows = await apiGet<FolioTypeDto[]>(
      `/folio-types/search?name=${encodeURIComponent(name)}`,
    );
    return rows.map(toDomain);
  },
  create: async (input: FolioTypeInput) =>
    toDomain(await apiPost<FolioTypeDto>("/folio-types", toWire(input))),
  update: async (id: number, input: FolioTypeInput) =>
    toDomain(await apiPut<FolioTypeDto>(`/folio-types/${id}`, toWire(input))),
  delete: async (id: number) => {
    await apiDelete(`/folio-types/${id}`);
  },
  isInUse: async (id: number) => {
    const { inUse } = await apiGet<{ inUse: boolean }>(`/folio-types/${id}/in-use`);
    return inUse;
  },
};
