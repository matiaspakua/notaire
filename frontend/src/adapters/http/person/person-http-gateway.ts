/**
 * HTTP adapter for people list/search (#1823 / ADR-030).
 * Only place in this catalog that may import api-client.
 */
import { apiDelete, apiGet, apiGetPage, apiPost, apiPut } from "@/lib/api-client";
import type { Person } from "@/core/person/domain/person";
import type {
  PersonGateway,
  PersonInput,
  PersonPage,
  PersonPageRequest,
  PersonSearchCriteria,
} from "@/core/person/application/ports";

/** Wire shape from Person / PersonResponse. */
interface PersonDto {
  personId?: number;
  firstName?: string;
  lastName?: string;
  nationality?: string;
  identificationNumber?: string;
  taxId?: string;
  sex?: string;
  birthDate?: string;
  maritalStatus?: string;
  marriageCount?: number;
  occupation?: string;
  address?: string;
  phone?: string;
  email?: string;
  notaryRegistrationNumber?: number;
  isClient?: boolean;
  fkIdIdentificationType?: { idIdentificationType?: number; name?: string; characters?: string };
  identificationTypeId?: number | null;
  version?: number;
}

function toDomain(dto: PersonDto): Person {
  return {
    id: dto.personId ?? 0,
    firstName: dto.firstName,
    lastName: dto.lastName,
    nationality: dto.nationality,
    identificationNumber: dto.identificationNumber,
    taxId: dto.taxId,
    sex: dto.sex,
    birthDate: dto.birthDate,
    maritalStatus: dto.maritalStatus,
    marriageCount: dto.marriageCount,
    occupation: dto.occupation,
    address: dto.address,
    phone: dto.phone,
    email: dto.email,
    notaryRegistrationNumber: dto.notaryRegistrationNumber,
    isClient: dto.isClient,
    identificationTypeId:
      dto.identificationTypeId ?? dto.fkIdIdentificationType?.idIdentificationType ?? null,
    identificationTypeName: dto.fkIdIdentificationType?.name,
    identificationTypeCharacters: dto.fkIdIdentificationType?.characters,
    version: dto.version,
  };
}

function toWire(input: PersonInput): PersonDto {
  return {
    firstName: input.firstName,
    lastName: input.lastName,
    nationality: input.nationality,
    identificationNumber: input.identificationNumber,
    taxId: input.taxId,
    sex: input.sex,
    birthDate: input.birthDate,
    maritalStatus: input.maritalStatus,
    marriageCount: input.marriageCount,
    occupation: input.occupation,
    address: input.address,
    phone: input.phone,
    email: input.email,
    notaryRegistrationNumber: input.notaryRegistrationNumber,
    isClient: input.isClient,
    identificationTypeId: input.identificationTypeId,
    version: input.version,
  };
}

export const personHttpGateway: PersonGateway = {
  getById: async (id: number) => toDomain(await apiGet<PersonDto>(`/people/${id}`)),
  listPage: async (request: PersonPageRequest): Promise<PersonPage> => {
    const page = await apiGetPage<PersonDto>("/people", {
      page: request.page,
      size: request.size,
      sort: request.sort,
    });
    return {
      content: page.content.map(toDomain),
      totalElements: page.totalElements,
      totalPages: page.totalPages,
      number: page.number,
      size: page.size,
    };
  },
  search: async (criteria: PersonSearchCriteria) => {
    const qs = new URLSearchParams();
    if (criteria.firstName) qs.set("firstName", criteria.firstName);
    if (criteria.lastName) qs.set("lastName", criteria.lastName);
    if (criteria.identificationNumber) qs.set("identificationNumber", criteria.identificationNumber);
    if (criteria.isClient) qs.set("isClient", "true");
    const rows = await apiGet<PersonDto[]>(`/people/search?${qs.toString()}`);
    return rows.map(toDomain);
  },
  create: async (input: PersonInput) => {
    await apiPost<void>("/people", toWire(input));
  },
  update: async (id: number, input: PersonInput) => {
    await apiPut<void>(`/people/${id}`, toWire(input));
  },
  delete: async (id: number) => {
    await apiDelete(`/people/${id}`);
  },
};
