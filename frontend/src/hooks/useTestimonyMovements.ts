import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiPost } from "@/lib/api-client";
import { testimoniesKeys } from "@/hooks/useTestimonies";
import type { TestimonyMovement } from "@/types";

function useMovementAction(action: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (idTestimony: number) =>
      apiPost<TestimonyMovement>(`/testimony-movements/${idTestimony}/${action}`, {}),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}

/** CU11 - Send a verified testimony to the property registry for inscription. */
export function useEnterRegistration() {
  return useMovementAction("enter-registration");
}

/** Record the inscription of the testimony's open movement. */
export function useRegisterInscription() {
  return useMovementAction("register-inscription");
}

/** CU12 - Withdraws a registered testimony and records the card number. */
export function useWithdraw() {
  const qc = useQueryClient();
  return useMutation({
    // Backend route is /withdraw (English) and its body is DtoTestimonyMovement(cardNumber) —
    // not the former Spanish withdraw path plus the card number.
    mutationFn: ({ idTestimony, cardNumber }: { idTestimony: number; cardNumber: number }) =>
      apiPost<TestimonyMovement>(`/testimony-movements/${idTestimony}/withdraw`, {
        cardNumber,
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}

export interface TestimonyReentry {
  idTestimony: number;
  cardNumber: number;
  observedByRegistry: boolean;
  notes: string;
}

/** CU44 - Reenters a previously withdrawn testimony without changing the earlier movement. */
export function useReenter() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ idTestimony, cardNumber, observedByRegistry, notes }: TestimonyReentry) =>
      apiPost<TestimonyMovement>(`/testimony-movements/${idTestimony}/reenter`, {
        cardNumber,
        observedByRegistry,
        notes,
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}
