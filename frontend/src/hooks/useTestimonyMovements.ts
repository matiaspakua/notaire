/**
 * React Query hooks for testimony movements.
 * Hexagon wiring: #1834 / ADR-030.
 */
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { testimonyMovementUseCases } from "@/composition/testimony-movement";
import { testimoniesKeys } from "@/hooks/useTestimonies";
import type { ReenterInput } from "@/core/testimony-movement/domain/testimony-movement";

function useMovementAction(action: "enterRegistration" | "registerInscription") {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (idTestimony: number) => testimonyMovementUseCases[action](idTestimony),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}

/** CU11 - Send a verified testimony to the property registry for inscription. */
export function useEnterRegistration() {
  return useMovementAction("enterRegistration");
}

/** Record the inscription of the testimony's open movement. */
export function useRegisterInscription() {
  return useMovementAction("registerInscription");
}

/** CU12 - Withdraws a registered testimony and records the card number. */
export function useWithdraw() {
  const qc = useQueryClient();
  return useMutation({
    // Backend route is /withdraw (English) and its body is DtoTestimonyMovement(cardNumber) —
    // not the retired `/retirar` path plus `numeroCarton`.
    mutationFn: ({ idTestimony, cardNumber }: { idTestimony: number; cardNumber: number }) =>
      testimonyMovementUseCases.withdraw({ idTestimony, cardNumber }),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}

export type TestimonyReentry = ReenterInput;

/** CU44 - Reenters a previously withdrawn testimony without changing the earlier movement. */
export function useReenter() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: TestimonyReentry) => testimonyMovementUseCases.reenter(input),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}
