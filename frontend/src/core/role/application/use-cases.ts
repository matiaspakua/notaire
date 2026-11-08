import type { RoleGateway } from "./ports";
import type { RoleInput } from "../domain/role";

export function createRoleUseCases(gateway: RoleGateway) {
  return {
    list: () => gateway.list(),
    create: (input: RoleInput) => gateway.create(input),
    update: (id: number, input: RoleInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
    assignToUser: (roleId: number, userId: number) => gateway.assignToUser(roleId, userId),
    unassignFromUser: (userId: number) => gateway.unassignFromUser(userId),
  };
}

export type RoleUseCases = ReturnType<typeof createRoleUseCases>;
