/**
 * HTTP adapter for users (#1843 / ADR-030).
 */
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import type { UserAccount, UserInput } from "@/core/user/domain/user";
import type { UserGateway } from "@/core/user/application/ports";

export const userHttpGateway: UserGateway = {
  list: () => apiGet<UserAccount[]>("/users"),
  create: (input: UserInput) => apiPost<UserAccount>("/users", input),
  update: async (id, input) => {
    await apiPut<void>(`/users/${id}`, input);
  },
  delete: async (id) => {
    await apiDelete(`/users/${id}`);
  },
};
