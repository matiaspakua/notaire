import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Rol } from "@/types";

export const rolesKeys = { all: ["roles"] as const };

export function useRoles() {
  return useQuery({
    queryKey: rolesKeys.all,
    queryFn: () => apiGet<Rol[]>("/roles"),
  });
}

export function useCreateRol() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Rol>) => apiPost<Rol>("/roles", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: rolesKeys.all }),
  });
}

export function useUpdateRol() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Rol> }) =>
      apiPut<Rol>(`/roles/${id}`, data),
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
      apiPut<void>(`/roles/${idRol}/usuarios/${idUsuario}`, {}),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: rolesKeys.all });
      qc.invalidateQueries({ queryKey: ["usuarios"] });
    },
  });
}

export function useUnassignRolFromUsuario() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (idUsuario: number) => apiDelete(`/roles/usuarios/${idUsuario}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["usuarios"] }),
  });
}
