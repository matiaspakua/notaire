/**
 * Person domain model (#1823 / ADR-030).
 * Pure TypeScript — no React, Next, or HTTP.
 */

export interface Person {
  id: number;
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
  identificationTypeName?: string;
  identificationTypeCharacters?: string;
  version?: number;
}
