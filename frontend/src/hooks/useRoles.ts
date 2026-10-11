import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Role } from "@/types";

export const rolesKeys = { all: ["roles"] as const };

export function useRoles() {
  return useQuery({
    queryKey: rolesKeys.all,
    queryFn: () => apiGet<Role[]>("/roles"),
  });
}

export function useCreateRole() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Role>) => apiPost<Role>("/roles", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: rolesKeys.all }),
  });
}

export function useUpdateRole() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Role> }) =>
      apiPut<Role>(`/roles/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: rolesKeys.all }),
  });
}

export function useDeleteRole() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/roles/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: rolesKeys.all }),
  });
}

export function useAssignRoleToUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ roleId, userId }: { roleId: number; userId: number }) =>
      apiPut<void>(`/roles/${roleId}/users/${userId}`, {}),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: rolesKeys.all });
      qc.invalidateQueries({ queryKey: ["users"] });
    },
  });
}

export function useUnassignRoleFromUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (userId: number) => apiDelete(`/roles/users/${userId}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["users"] }),
  });
}
