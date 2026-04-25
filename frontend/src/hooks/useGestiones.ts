import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { GestionDeEscritura } from "@/types";

export const gestionesKeys = {
  all: ["gestiones"] as const,
  detail: (id: number) => ["gestiones", id] as const,
  byCliente: (id: number) => ["gestiones", "cliente", id] as const,
};

export function useGestiones() {
  return useQuery({
    queryKey: gestionesKeys.all,
    queryFn: () => apiGet<GestionDeEscritura[]>("/gestiones"),
  });
}

export function useGestion(id: number) {
  return useQuery({
    queryKey: gestionesKeys.detail(id),
    queryFn: () => apiGet<GestionDeEscritura>(`/gestiones/${id}`),
    enabled: id > 0,
  });
}

export function useGestionesByCliente(idPersona: number) {
  return useQuery({
    queryKey: gestionesKeys.byCliente(idPersona),
    queryFn: () => apiGet<GestionDeEscritura[]>(`/gestiones/cliente/${idPersona}`),
    enabled: idPersona > 0,
  });
}

export function useCreateGestion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<GestionDeEscritura>) =>
      apiPost<void>("/gestiones", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: gestionesKeys.all }),
  });
}

export function useUpdateGestion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<GestionDeEscritura> }) =>
      apiPut<void>(`/gestiones/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: gestionesKeys.all }),
  });
}

export function useDeleteGestion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/gestiones/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: gestionesKeys.all }),
  });
}
