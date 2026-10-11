/**
 * Identification type catalog (DNI, CUIT, Pasaporte, …) for the person form (#1361, RF-37)
 * and its admin page (#1361 slice 2, RF-53).
 * Endpoint: GET/POST/PUT/DELETE /api/v1/identification-types
 */
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import type { IdentificationType } from "@/types";

export const identificationTypeKeys = {
  all: ["identificationTypes"] as const,
};

/** Name of the type a new person starts with (Owner decision on #1361). */
export const DEFAULT_IDENTIFICATION_TYPE = "DNI";

export function useIdentificationTypes() {
  return useQuery({
    queryKey: identificationTypeKeys.all,
    queryFn: () => apiGet<IdentificationType[]>("/identification-types"),
    staleTime: 5 * 60 * 1000,
  });
}

/** The id of the default type (DNI, any letter case) in the catalog, if present. */
export function defaultIdentificationTypeId(types: IdentificationType[] | undefined): number | undefined {
  return types?.find((t) => t.name?.trim().toUpperCase() === DEFAULT_IDENTIFICATION_TYPE)?.idIdentificationType;
}

/** IdentificationTypeRequest: `characters` is the number length the person form uses. */
export interface IdentificationTypeInput {
  name: string;
  characters: string;
}

function useInvalidateTypes() {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: identificationTypeKeys.all });
}

export function useCreateIdentificationType() {
  const invalidate = useInvalidateTypes();
  return useMutation({
    mutationFn: (input: IdentificationTypeInput) => apiPost<IdentificationType>("/identification-types", input),
    onSuccess: invalidate,
  });
}

export function useUpdateIdentificationType() {
  const invalidate = useInvalidateTypes();
  return useMutation({
    mutationFn: ({ id, ...input }: IdentificationTypeInput & { id: number }) =>
      apiPut(`/identification-types/${id}`, input),
    onSuccess: invalidate,
  });
}

export function useDeleteIdentificationType() {
  const invalidate = useInvalidateTypes();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/identification-types/${id}`),
    onSuccess: invalidate,
  });
}
