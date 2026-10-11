import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiPost } from "@/lib/api-client";
import { testimoniosKeys } from "@/hooks/useTestimonies";
import type { TestimonyMovement } from "@/types";

function useMovimientoAction(action: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (idTestimonio: number) =>
      apiPost<TestimonyMovement>(`/testimony-movements/${idTestimonio}/${action}`, {}),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniosKeys.all }),
  });
}

/** CU11 - Send a verified testimony to the property registry for inscription. */
export function useIngresarInscripcion() {
  return useMovimientoAction("enter-registration");
}

/** Record the inscription of the testimony's open movement. */
export function useRegistrarInscripcion() {
  return useMovimientoAction("register-inscription");
}

/** CU12 - Retira un testimonio inscripto, registrando el número de cartón. */
export function useRetirar() {
  const qc = useQueryClient();
  return useMutation({
    // Backend route is /withdraw (English) and its body is DtoTestimonyMovement(cardNumber) —
    // not /retirar+numeroCarton.
    mutationFn: ({ idTestimonio, numeroCarton }: { idTestimonio: number; numeroCarton: number }) =>
      apiPost<TestimonyMovement>(`/testimony-movements/${idTestimonio}/withdraw`, {
        cardNumber: numeroCarton,
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniosKeys.all }),
  });
}

export interface ReingresoTestimonio {
  idTestimonio: number;
  numeroCarton: number;
  observadoPorRegistro: boolean;
  observaciones: string;
}

/** CU44 - Reingresa un testimonio previamente retirado, sin alterar el movimiento anterior. */
export function useReingresar() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ idTestimonio, numeroCarton, observadoPorRegistro, observaciones }: ReingresoTestimonio) =>
      apiPost<TestimonyMovement>(`/testimony-movements/${idTestimonio}/reenter`, {
        cardNumber: numeroCarton,
        observedByRegistry: observadoPorRegistro,
        notes: observaciones,
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniosKeys.all }),
  });
}
