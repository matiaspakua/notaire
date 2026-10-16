/**
 * HTTP adapter for the procedure-type catalog (#1796 / ADR-030).
 * Only place in this catalog that may import api-client.
 */
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import type { ProcedureType } from "@/core/procedure-type/domain/procedure-type";
import type {
  ProcedureTypeGateway,
  ProcedureTypeInput,
} from "@/core/procedure-type/application/ports";

/** Wire shape from ProcedureType / DtoProcedureType. */
interface ProcedureTypeDto {
  idProcedureType?: number;
  name?: string;
  notes?: string | null;
  isArchived?: boolean;
  isRegistered?: boolean;
  associatesProperties?: boolean;
  enabled?: boolean;
  workflowDefinitionId?: number | null;
  workflowDefinitionName?: string | null;
}

function toDomain(dto: ProcedureTypeDto): ProcedureType {
  return {
    id: dto.idProcedureType ?? 0,
    name: dto.name ?? "",
    notes: dto.notes,
    isArchived: dto.isArchived,
    isRegistered: dto.isRegistered,
    associatesProperties: dto.associatesProperties,
    enabled: dto.enabled,
    workflowDefinitionId: dto.workflowDefinitionId,
    workflowDefinitionName: dto.workflowDefinitionName,
  };
}

function toWire(input: ProcedureTypeInput): ProcedureTypeDto {
  return {
    name: input.name,
    notes: input.notes,
    isArchived: input.isArchived,
    isRegistered: input.isRegistered,
    associatesProperties: input.associatesProperties,
    enabled: input.enabled,
  };
}

export const procedureTypeHttpGateway: ProcedureTypeGateway = {
  list: async () => (await apiGet<ProcedureTypeDto[]>("/procedure-types")).map(toDomain),
  searchByName: async (name: string) =>
    (
      await apiGet<ProcedureTypeDto[]>(
        `/procedure-types/search?name=${encodeURIComponent(name)}`,
      )
    ).map(toDomain),
  create: async (input: ProcedureTypeInput) =>
    toDomain(await apiPost<ProcedureTypeDto>("/procedure-types", toWire(input))),
  update: async (id: number, input: ProcedureTypeInput) =>
    toDomain(await apiPut<ProcedureTypeDto>(`/procedure-types/${id}`, toWire(input))),
  delete: async (id: number) => {
    await apiDelete(`/procedure-types/${id}`);
  },
  isInUse: async (id: number) => {
    const { inUse } = await apiGet<{ inUse: boolean }>(`/procedure-types/${id}/in-use`);
    return inUse;
  },
  assignWorkflow: async (id: number, workflowDefinitionId: number | null) =>
    toDomain(
      await apiPut<ProcedureTypeDto>(`/procedure-types/${id}/workflow`, { workflowDefinitionId }),
    ),
};
