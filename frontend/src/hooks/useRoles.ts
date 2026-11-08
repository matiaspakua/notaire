/**
 * React Query hooks for roles.
 * Hexagon wiring: #1843 / ADR-030.
 */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { roleUseCases } from "@/composition/role";
import type { Role as DomainRole, RoleInput } from "@/core/role/domain/role";
import type { Role } from "@/types";

export const rolesKeys = { all: ["roles"] as const };

function toWire(r: DomainRole): Role {
  return r as Role;
}

export function useRoles() {
  return useQuery({
    queryKey: rolesKeys.all,
    queryFn: async () => (await roleUseCases.list()).map(toWire),
  });
}

export function useCreateRole() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (data: Partial<Role>) => toWire(await roleUseCases.create(data as RoleInput)),
    onSuccess: () => qc.invalidateQueries({ queryKey: rolesKeys.all }),
  });
}

export function useUpdateRole() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, data }: { id: number; data: Partial<Role> }) =>
      toWire(await roleUseCases.update(id, data as RoleInput)),
    onSuccess: () => qc.invalidateQueries({ queryKey: rolesKeys.all }),
  });
}

export function useDeleteRole() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => roleUseCases.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: rolesKeys.all }),
  });
}

export function useAssignRoleToUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ roleId, userId }: { roleId: number; userId: number }) =>
      roleUseCases.assignToUser(roleId, userId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: rolesKeys.all });
      qc.invalidateQueries({ queryKey: ["users"] });
    },
  });
}

export function useUnassignRoleFromUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (userId: number) => roleUseCases.unassignFromUser(userId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["users"] }),
  });
}
