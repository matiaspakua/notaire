import { keepPreviousData, useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiGetPage, apiGetPaged, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import { itemsKeys } from "@/hooks/useItems";
import type { Item, Presupuesto, PresupuestoResumen } from "@/types";

export const presupuestosKeys = {
  all: ["presupuestos"] as const,
  detail: (id: number) => ["presupuestos", id] as const,
  byPersona: (id: number) => ["presupuestos", "persona", id] as const,
  resumen: (id: number) => ["presupuestos", id, "resumen"] as const,
  page: (params: { page: number; size: number }) => ["presupuestos", "page", params] as const,
};

/** Sort for the budgets list: newest first (#1340). */
export const PRESUPUESTOS_SORT = "idBudget,desc";

/** One server page of GET /presupuestos (#1340); the list is never loaded whole. */
export function usePresupuestosPage(params: { page: number; size: number }, options: { enabled?: boolean } = {}) {
  return useQuery({
    queryKey: presupuestosKeys.page(params),
    queryFn: () =>
      apiGetPage<Presupuesto>("/presupuestos", { page: params.page, size: params.size, sort: PRESUPUESTOS_SORT }),
    placeholderData: keepPreviousData,
    enabled: options.enabled ?? true,
  });
}

/** CU47 - Consultar Pago: financial summary (gestión, total, saldo, pagos) for a presupuesto. */
export function usePresupuestoResumen(id: number | null) {
  return useQuery({
    queryKey: presupuestosKeys.resumen(id ?? 0),
    queryFn: () => apiGet<PresupuestoResumen>(`/presupuestos/${id}/resumen`),
    enabled: id !== null,
  });
}

export function usePresupuestos() {
  return useQuery({
    queryKey: presupuestosKeys.all,
    queryFn: () => apiGetPaged<Presupuesto>("/presupuestos"),
  });
}

interface CreatePresupuestoInput {
  data: Partial<Presupuesto>;
  tipoTramiteId?: number;
}

interface CreatePresupuestoResult {
  presupuesto: Presupuesto;
  itemsLoaded: boolean;
}

/**
 * CU01/CU39 - Creates the presupuesto and, when a tipo de trámite is given, loads that type's template items into it.
 * A missing template must not undo the creation, so the second step only reports `itemsLoaded`.
 */
export function useCreatePresupuesto() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ data, tipoTramiteId }: CreatePresupuestoInput): Promise<CreatePresupuestoResult> => {
      const presupuesto = await apiPost<Presupuesto>("/presupuestos", data);
      if (tipoTramiteId === undefined) return { presupuesto, itemsLoaded: false };
      try {
        await apiPost<Item[]>(
          `/presupuestos/${presupuesto.idBudget}/items-desde-plantilla?tipoTramiteId=${tipoTramiteId}`,
          undefined,
        );
        return { presupuesto, itemsLoaded: true };
      } catch {
        return { presupuesto, itemsLoaded: false };
      }
    },
    onSuccess: (result) => {
      qc.invalidateQueries({ queryKey: presupuestosKeys.all });
      if (result.itemsLoaded) qc.invalidateQueries({ queryKey: itemsKeys.byPresupuesto(result.presupuesto.idBudget!) });
    },
  });
}

export function useUpdatePresupuesto() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Presupuesto> }) =>
      apiPut<void>(`/presupuestos/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: presupuestosKeys.all }),
  });
}

export function useDeletePresupuesto() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/presupuestos/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: presupuestosKeys.all }),
  });
}

/** CU39 - Cargar los ítems del presupuesto desde la plantilla del tipo de trámite. */
export function useCargarItemsDesdePlantilla() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ idPresupuesto, tipoTramiteId }: { idPresupuesto: number; tipoTramiteId: number }) =>
      apiPost<Item[]>(`/presupuestos/${idPresupuesto}/items-desde-plantilla?tipoTramiteId=${tipoTramiteId}`, undefined),
    onSuccess: (_, variables) =>
      qc.invalidateQueries({ queryKey: itemsKeys.byPresupuesto(variables.idPresupuesto) }),
  });
}

/** CU71 - Agregar al presupuesto copias de ítems existentes del catálogo. */
export function useAgregarItemsDesdeCatalogo() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ idPresupuesto, idItems }: { idPresupuesto: number; idItems: number[] }) =>
      apiPost<Item[]>(`/presupuestos/${idPresupuesto}/items-desde-catalogo`, idItems),
    onSuccess: (_, variables) =>
      qc.invalidateQueries({ queryKey: itemsKeys.byPresupuesto(variables.idPresupuesto) }),
  });
}
