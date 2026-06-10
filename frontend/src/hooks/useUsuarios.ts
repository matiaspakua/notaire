import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Usuario } from "@/types";

export const usuariosKeys = { all: ["usuarios"] as const };

export function useUsuarios() {
  return useQuery({
    queryKey: usuariosKeys.all,
    queryFn: () => apiGet<Usuario[]>("/usuarios"),
  });
}

export function useCreateUsuario() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Usuario>) => apiPost<Usuario>("/usuarios", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: usuariosKeys.all }),
  });
}

export function useUpdateUsuario() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Usuario> }) =>
      apiPut<void>(`/usuarios/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: usuariosKeys.all }),
  });
}

export function useDeleteUsuario() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/usuarios/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: usuariosKeys.all }),
  });
}
