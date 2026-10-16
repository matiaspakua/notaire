/**
 * HTTP adapter for roles (#1843 / ADR-030).
 */
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import type { Role, RoleInput } from "@/core/role/domain/role";
import type { RoleGateway } from "@/core/role/application/ports";

export const roleHttpGateway: RoleGateway = {
  list: () => apiGet<Role[]>("/roles"),
  create: (input: RoleInput) => apiPost<Role>("/roles", input),
  update: (id, input) => apiPut<Role>(`/roles/${id}`, input),
  delete: async (id) => {
    await apiDelete(`/roles/${id}`);
  },
  assignToUser: async (roleId, userId) => {
    await apiPut<void>(`/roles/${roleId}/users/${userId}`, {});
  },
  unassignFromUser: async (userId) => {
    await apiDelete(`/roles/users/${userId}`);
  },
};
