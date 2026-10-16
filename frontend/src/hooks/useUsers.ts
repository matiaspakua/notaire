/**
 * React Query hooks for users.
 * Hexagon wiring: #1843 / ADR-030.
 */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { userUseCases } from "@/composition/user";
import type { UserAccount as DomainUser, UserInput } from "@/core/user/domain/user";
import type { UserAccount } from "@/types";

export const usersKeys = { all: ["users"] as const };

function toWire(u: DomainUser): UserAccount {
  return u as UserAccount;
}

export function useUsers() {
  return useQuery({
    queryKey: usersKeys.all,
    queryFn: async () => (await userUseCases.list()).map(toWire),
  });
}

export function useCreateUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (data: Partial<UserAccount>) =>
      toWire(await userUseCases.create(data as UserInput)),
    onSuccess: () => qc.invalidateQueries({ queryKey: usersKeys.all }),
  });
}

export function useUpdateUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<UserAccount> }) =>
      userUseCases.update(id, data as UserInput),
    onSuccess: () => qc.invalidateQueries({ queryKey: usersKeys.all }),
  });
}

export function useDeleteUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => userUseCases.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: usersKeys.all }),
  });
}
