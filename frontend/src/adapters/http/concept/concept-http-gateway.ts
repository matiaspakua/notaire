/**
 * HTTP adapter for the concept catalog (#1781 / ADR-030).
 * Only place in this catalog that may import api-client.
 */
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import type { Concept } from "@/core/concept/domain/concept";
import type { ConceptGateway, ConceptInput } from "@/core/concept/application/ports";

/** Wire shape from Concept / DtoConcept. */
interface ConceptDto {
  idConcept?: number;
  name?: string;
  value?: number;
  percentage?: number;
  fixed?: boolean;
  enabled?: boolean;
  version?: number;
}

function toDomain(dto: ConceptDto): Concept {
  return {
    id: dto.idConcept ?? 0,
    name: dto.name ?? "",
    value: dto.value,
    percentage: dto.percentage,
    fixed: dto.fixed,
    enabled: dto.enabled,
    version: dto.version,
  };
}

function toWire(input: ConceptInput): ConceptDto {
  return {
    name: input.name,
    value: input.value,
    percentage: input.percentage,
    fixed: input.fixed,
    enabled: input.enabled,
    version: input.version,
  };
}

export const conceptHttpGateway: ConceptGateway = {
  list: async () => {
    const rows = await apiGet<ConceptDto[]>("/concepts");
    return rows.map(toDomain);
  },
  searchByName: async (name: string) => {
    const rows = await apiGet<ConceptDto[]>(`/concepts/search?name=${encodeURIComponent(name)}`);
    return rows.map(toDomain);
  },
  create: async (input: ConceptInput) =>
    toDomain(await apiPost<ConceptDto>("/concepts", toWire(input))),
  update: async (id: number, input: ConceptInput) =>
    toDomain(await apiPut<ConceptDto>(`/concepts/${id}`, toWire(input))),
  delete: async (id: number) => {
    await apiDelete(`/concepts/${id}`);
  },
  isInUse: async (id: number) => {
    const { inUse } = await apiGet<{ inUse: boolean }>(`/concepts/${id}/in-use`);
    return inUse;
  },
};
