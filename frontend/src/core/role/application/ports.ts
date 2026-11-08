import type { Role, RoleInput } from "../domain/role";

export interface RoleGateway {
  list(): Promise<Role[]>;
  create(input: RoleInput): Promise<Role>;
  update(id: number, input: RoleInput): Promise<Role>;
  delete(id: number): Promise<void>;
  assignToUser(roleId: number, userId: number): Promise<void>;
  unassignFromUser(userId: number): Promise<void>;
}
