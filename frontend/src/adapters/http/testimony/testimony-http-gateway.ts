/**
 * HTTP adapter for testimonies (#1834 / ADR-030).
 */
import { apiDelete, apiGet, apiGetBytes, apiPost, apiPut } from "@/lib/api-client";
import type {
  Testimony,
  TestimonyCreateInput,
  TestimonyUpdateInput,
  TestimonyVerifyInput,
} from "@/core/testimony/domain/testimony";
import type { TestimonyGateway } from "@/core/testimony/application/ports";

type TestimonyDto = Testimony;

function toDomain(dto: TestimonyDto): Testimony {
  return {
    idTestimony: dto.idTestimony,
    number: dto.number,
    flagged: dto.flagged,
    verified: dto.verified,
    notes: dto.notes,
    version: dto.version,
    deed: dto.deed
      ? { idDeed: dto.deed.idDeed, number: dto.deed.number, status: dto.deed.status }
      : undefined,
    testimonyMovements: dto.testimonyMovements,
  };
}

export const testimonyHttpGateway: TestimonyGateway = {
  list: async () => (await apiGet<TestimonyDto[]>("/testimonies")).map(toDomain),
  generate: async (idDeed: number) =>
    toDomain(await apiPost<TestimonyDto>(`/testimonies/${idDeed}/generate`, {})),
  create: async (input: TestimonyCreateInput) =>
    toDomain(await apiPost<TestimonyDto>("/testimonies", input)),
  verify: async (id: number, input: TestimonyVerifyInput) =>
    toDomain(await apiPost<TestimonyDto>(`/testimonies/${id}/verify`, input)),
  update: async (id: number, input: TestimonyUpdateInput) => {
    await apiPut(`/testimonies/${id}`, input);
  },
  delete: async (id: number) => {
    await apiDelete(`/testimonies/${id}`);
  },
  downloadCopy: async (idTestimony: number) =>
    apiGetBytes(`/reports/testimonies/${idTestimony}/copies`),
};
