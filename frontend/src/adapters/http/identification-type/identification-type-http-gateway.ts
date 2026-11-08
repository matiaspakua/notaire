/**
 * HTTP adapter for the identification-type catalog (#1732 / ADR-030).
 * Only place in this catalog that may import api-client.
 */
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import type { IdentificationType } from "@/core/identification-type/domain/identification-type";
import type {
  IdentificationTypeGateway,
  IdentificationTypeInput,
} from "@/core/identification-type/application/ports";

/** Wire shape from IdentificationTypeResponse. */
interface IdentificationTypeDto {
  idIdentificationType: number;
  name: string;
  characters?: string | null;
  version?: number;
}

function toDomain(dto: IdentificationTypeDto): IdentificationType {
  return {
    id: dto.idIdentificationType,
    name: dto.name,
    characters: dto.characters,
    version: dto.version,
  };
}

export const identificationTypeHttpGateway: IdentificationTypeGateway = {
  list: async () => {
    const rows = await apiGet<IdentificationTypeDto[]>("/identification-types");
    return rows.map(toDomain);
  },
  create: async (input: IdentificationTypeInput) =>
    toDomain(await apiPost<IdentificationTypeDto>("/identification-types", input)),
  update: async (id: number, input: IdentificationTypeInput) =>
    toDomain(await apiPut<IdentificationTypeDto>(`/identification-types/${id}`, input)),
  delete: async (id: number) => {
    await apiDelete(`/identification-types/${id}`);
  },
};
