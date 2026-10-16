/**
 * Outbound port for people list/search (#1823 / ADR-030).
 */
import type { Person } from "../domain/person";

/** Write payload for create/update. */
export interface PersonInput {
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
  identificationTypeId?: number | null;
  version?: number;
}

export interface PersonSearchCriteria {
  firstName?: string;
  lastName?: string;
  identificationNumber?: string;
  isClient?: boolean;
}

export interface PersonPage {
  content: Person[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface PersonPageRequest {
  page: number;
  size: number;
  sort?: string;
}

export interface PersonGateway {
  getById(id: number): Promise<Person>;
  listPage(request: PersonPageRequest): Promise<PersonPage>;
  search(criteria: PersonSearchCriteria): Promise<Person[]>;
  create(input: PersonInput): Promise<void>;
  update(id: number, input: PersonInput): Promise<void>;
  delete(id: number): Promise<void>;
}
