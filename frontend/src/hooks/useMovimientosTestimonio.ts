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
    mutationFn: ({ idTestimonio, numeroCarton }: { idTestimonio: number; numeroCarton: number }) =>
      apiPost<MovimientoTestimonio>(`/movimiento-testimonio/${idTestimonio}/retirar`, { numeroCarton }),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniosKeys.all }),
  });
}

/** CU44 - Reingresa un testimonio previamente retirado, sin alterar el movimiento anterior. */
export function useReingresar() {
  return useMovimientoAction("reingresar");
}
