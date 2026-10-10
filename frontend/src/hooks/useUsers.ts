import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { UserAccount } from "@/types";

export const usuariosKeys = { all: ["usuarios"] as const };

export function useUsers() {
  return useQuery({
    queryKey: usuariosKeys.all,
    queryFn: () => apiGet<UserAccount[]>("/users"),
  });
}

export function useCreateUsuario() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<UserAccount>) => apiPost<UserAccount>("/users", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: usuariosKeys.all }),
  });
}

export function useUpdateUsuario() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<UserAccount> }) =>
      apiPut<void>(`/users/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: usuariosKeys.all }),
  });
}

export function useDeleteUsuario() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/users/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: usuariosKeys.all }),
  });
}
