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

export function useCreateRol() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Role>) => apiPost<Role>("/roles", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: rolesKeys.all }),
  });
}

export function useUpdateRol() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Role> }) =>
      apiPut<Role>(`/roles/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: rolesKeys.all }),
  });
}

export function useDeleteRol() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/roles/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: rolesKeys.all }),
  });
}

export function useAssignRolToUsuario() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ idRol, idUsuario }: { idRol: number; idUsuario: number }) =>
      apiPut<void>(`/roles/${idRol}/users/${idUsuario}`, {}),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: rolesKeys.all });
      qc.invalidateQueries({ queryKey: ["usuarios"] });
    },
  });
}

export function useUnassignRolFromUsuario() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (idUsuario: number) => apiDelete(`/roles/users/${idUsuario}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["usuarios"] }),
  });
}
