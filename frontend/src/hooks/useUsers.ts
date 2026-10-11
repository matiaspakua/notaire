import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { UserAccount } from "@/types";

export const usersKeys = { all: ["users"] as const };

export function useUsers() {
  return useQuery({
    queryKey: usersKeys.all,
    queryFn: () => apiGet<UserAccount[]>("/users"),
  });
}

export function useCreateUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<UserAccount>) => apiPost<UserAccount>("/users", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: usersKeys.all }),
  });
}

export function useUpdateUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<UserAccount> }) =>
      apiPut<void>(`/users/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: usersKeys.all }),
  });
}

export function useDeleteUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/users/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: usersKeys.all }),
  });
}
