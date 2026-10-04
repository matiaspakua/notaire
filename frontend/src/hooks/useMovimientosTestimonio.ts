import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiPost } from "@/lib/api-client";
import { testimoniosKeys } from "@/hooks/useTestimonios";
import type { MovimientoTestimonio } from "@/types";

function useMovimientoAction(action: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (idTestimonio: number) =>
      apiPost<MovimientoTestimonio>(`/movimiento-testimonio/${idTestimonio}/${action}`, {}),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniosKeys.all }),
  });
}

/** CU11 - Ingresa un testimonio verificado al Registro de la Propiedad para su inscripción. */
export function useIngresarInscripcion() {
  return useMovimientoAction("ingresar-inscripcion");
}

/** Registra la inscripción del movimiento abierto de un testimonio. */
export function useRegistrarInscripcion() {
  return useMovimientoAction("registrar-inscripcion");
}

/** CU12 - Retira un testimonio inscripto, registrando el número de cartón. */
export function useRetirar() {
  const qc = useQueryClient();
  return useMutation({
    // Backend route is /withdraw (English) and its body is DtoTestimonyMovement(cardNumber) —
    // not /retirar+numeroCarton.
    mutationFn: ({ idTestimonio, numeroCarton }: { idTestimonio: number; numeroCarton: number }) =>
      apiPost<MovimientoTestimonio>(`/movimiento-testimonio/${idTestimonio}/withdraw`, {
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
      apiPost<MovimientoTestimonio>(`/movimiento-testimonio/${idTestimonio}/reenter`, {
        cardNumber: numeroCarton,
        observedByRegistry: observadoPorRegistro,
        notes: observaciones,
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniosKeys.all }),
  });
}
