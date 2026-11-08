/**
 * User account domain (#1843 / ADR-030).
 */
export interface UserAccount {
  idUser?: number;
  name?: string;
  password?: string;
  type?: string;
  active?: boolean;
  person?: unknown;
  role?: { idRole: number; name: string } | null;
}

export type UserInput = Partial<UserAccount>;
