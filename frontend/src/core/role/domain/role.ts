/**
 * Role domain (#1843 / ADR-030).
 */
export interface Role {
  idRole?: number;
  name?: string;
  description?: string;
  active?: boolean;
  modulos?: string[];
}

export type RoleInput = Partial<Role>;
