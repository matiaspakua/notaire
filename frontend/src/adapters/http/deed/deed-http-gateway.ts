/**
 * HTTP adapter for deeds (#1831 / ADR-030).
 * Only place in this catalog that may import api-client.
 */
import { apiDelete, apiGet, apiGetPage, apiPost, apiPut } from "@/lib/api-client";
import type { Deed } from "@/core/deed/domain/deed";
import type { DeedGateway, DeedInput, DeedPage, DeedPageRequest } from "@/core/deed/application/ports";

/** Wire shape from DeedResponse — kept loose for OpenAPI drift. */
type DeedDto = Deed;

function toDomain(dto: DeedDto): Deed {
  return {
    idDeed: dto.idDeed,
    number: dto.number,
    dateDeedrecording: dto.dateDeedrecording,
    dateRegistration: dto.dateRegistration,
    body: dto.body,
    notes: dto.notes,
    registrationEntryNumber: dto.registrationEntryNumber,
    status: dto.status,
    version: dto.version,
  };
}

function toWire(input: DeedInput): DeedDto {
  return {
    idDeed: input.idDeed,
    number: input.number,
    dateDeedrecording: input.dateDeedrecording,
    dateRegistration: input.dateRegistration,
    body: input.body,
    notes: input.notes,
    registrationEntryNumber: input.registrationEntryNumber,
    status: input.status,
    version: input.version,
  };
}

export const deedHttpGateway: DeedGateway = {
  getById: async (id: number) => toDomain(await apiGet<DeedDto>(`/deeds/${id}`)),
  listPage: async (request: DeedPageRequest): Promise<DeedPage> => {
    const page = await apiGetPage<DeedDto>("/deeds", {
      page: request.page,
      size: request.size,
      sort: request.sort,
      params: request.params,
    });
    return {
      content: page.content.map(toDomain),
      totalElements: page.totalElements,
      totalPages: page.totalPages,
      number: page.number,
      size: page.size,
    };
  },
  searchByNumber: async (number: string) =>
    (await apiGet<DeedDto[]>(`/deeds/search?number=${encodeURIComponent(number)}`)).map(toDomain),
  create: async (input: DeedInput) => {
    await apiPost<void>("/deeds", toWire(input));
  },
  update: async (id: number, input: DeedInput) => {
    await apiPut<void>(`/deeds/${id}`, toWire(input));
  },
  delete: async (id: number) => {
    await apiDelete(`/deeds/${id}`);
  },
  sign: async (id: number) => toDomain(await apiPost<DeedDto>(`/deeds/${id}/sign`, {})),
};
