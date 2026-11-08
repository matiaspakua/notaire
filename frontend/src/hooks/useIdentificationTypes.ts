/**
 * Identification type catalog (DNI, CUIT, Pasaporte, …) for the person form (#1361, RF-37)
 * and its admin page (#1361 slice 2, RF-53). Hexagon wiring: #1732 / ADR-030.
 * Endpoint: GET/POST/PUT/DELETE /api/v1/identification-types
 */
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { identificationTypeUseCases } from "@/composition/identification-type";
import {
  DEFAULT_IDENTIFICATION_TYPE,
  defaultIdentificationTypeId as defaultIdFromDomain,
  type IdentificationType as DomainIdentificationType,
} from "@/core/identification-type/domain/identification-type";
import type { IdentificationTypeInput } from "@/core/identification-type/application/ports";
import type { IdentificationType } from "@/types";

export const identificationTypeKeys = {
  all: ["identificationTypes"] as const,
};

export { DEFAULT_IDENTIFICATION_TYPE };

/** Map domain model back to the wire-facing UI type used across the app. */
function toWire(type: DomainIdentificationType): IdentificationType {
  return {
    idIdentificationType: type.id,
    name: type.name,
    characters: type.characters,
    version: type.version,
  };
}

/** The id of the default type (DNI, any letter case) in the catalog, if present. */
export function defaultIdentificationTypeId(
  types: IdentificationType[] | undefined,
): number | undefined {
  return defaultIdFromDomain(
    types?.map((t) => ({
      id: t.idIdentificationType,
      name: t.name,
      characters: t.characters,
      version: t.version,
    })),
  );
}

export type { IdentificationTypeInput };

function useInvalidateTypes() {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: identificationTypeKeys.all });
}

export function useIdentificationTypes() {
  return useQuery({
    queryKey: identificationTypeKeys.all,
    queryFn: async () => (await identificationTypeUseCases.list()).map(toWire),
    staleTime: 5 * 60 * 1000,
  });
}

export function useCreateIdentificationType() {
  const invalidate = useInvalidateTypes();
  return useMutation({
    mutationFn: async (input: IdentificationTypeInput) =>
      toWire(await identificationTypeUseCases.create(input)),
    onSuccess: invalidate,
  });
}

export function useUpdateIdentificationType() {
  const invalidate = useInvalidateTypes();
  return useMutation({
    mutationFn: async ({ id, ...input }: IdentificationTypeInput & { id: number }) =>
      toWire(await identificationTypeUseCases.update(id, input)),
    onSuccess: invalidate,
  });
}

export function useDeleteIdentificationType() {
  const invalidate = useInvalidateTypes();
  return useMutation({
    mutationFn: (id: number) => identificationTypeUseCases.delete(id),
    onSuccess: invalidate,
  });
}
