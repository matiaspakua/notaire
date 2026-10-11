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

/** CU12 - Retira un testimony inscripto, registrando el número de cartón. */
export function useWithdraw() {
  const qc = useQueryClient();
  return useMutation({
    // Backend route is /withdraw (English) and its body is DtoTestimonyMovement(cardNumber) —
    // not /retirar+numberCard.
    mutationFn: ({ idTestimony, numberCard }: { idTestimony: number; numberCard: number }) =>
      apiPost<TestimonyMovement>(`/testimony-movements/${idTestimony}/withdraw`, {
        cardNumber: numberCard,
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}

export interface TestimonyReentry {
  idTestimony: number;
  numberCard: number;
  observadoPorRegistro: boolean;
  notes: string;
}

/** CU44 - Reingresa un testimony previamente retirado, sin alterar el movement anterior. */
export function useReenter() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ idTestimony, numberCard, observadoPorRegistro, notes }: TestimonyReentry) =>
      apiPost<TestimonyMovement>(`/testimony-movements/${idTestimony}/reenter`, {
        cardNumber: numberCard,
        observedByRegistry: observadoPorRegistro,
        notes: notes,
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}
