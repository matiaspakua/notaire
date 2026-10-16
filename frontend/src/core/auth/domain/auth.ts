/**
 * Auth domain (#1843 / ADR-030).
 */
export interface LoginResult {
  idUser?: number;
  name: string;
  type?: string;
  valid?: boolean;
  status?: boolean;
  version?: number;
  person?: { idPerson?: number; firstName?: string; lastName?: string };
  token?: string;
}

export interface LoginInput {
  name: string;
  password: string;
}
