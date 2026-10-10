"use client";

import { Suspense, useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, Receipt, ListChecks } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { FormContainer, FormSection, FormField, FormActions, FormHeader } from "@/theme/form-patterns";
import { useQuery } from "@tanstack/react-query";
import { apiGet, ApiError } from "@/lib/api-client";
import { presentMutationError } from "@/lib/mutation-error";
import {
  usePresupuestosPage,
  usePresupuestoResumen,
  useCreatePresupuesto,
  useUpdatePresupuesto,
  useDeletePresupuesto,
  useCargarItemsDesdePlantilla,
  useAgregarItemsDesdeCatalogo,
} from "@/hooks/usePresupuestos";
import { PersonPicker } from "@/components/shared/PersonPicker";
import { useClampPage, useUrlPagination } from "@/hooks/useUrlPagination";
import { useItems, useItemsByPresupuesto } from "@/hooks/useItems";
import { useTiposTramite } from "@/hooks/useTiposTramite";
import { formatDate, formatCurrency, fullName } from "@/lib/utils";
import type { Presupuesto } from "@/types";
import { useDeleteError } from "@/hooks/useDeleteError";
import { toDateInputValue } from "@/lib/dates";

const NO_TEMPLATE = "none";

const EMPTY: Partial<Presupuesto> = { date: "", propertyAmount: undefined, status: "BORRADOR" };

export default function PresupuestosPage() {
  return (
    <Suspense>
      <PresupuestosList />
    </Suspense>
  );
}

function PresupuestosList() {
  const t = useTranslations("presupuestos");
  const showDeleteError = useDeleteError();
  const tc = useTranslations("common");

  const createMutation = useCreatePresupuesto();
  const updateMutation = useUpdatePresupuesto();
  const deleteMutation = useDeletePresupuesto();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Presupuesto>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  const [resumenId, setResumenId] = useState<number | null>(null);
  const { data: resumen, isLoading: isResumenLoading, error: resumenError } =
    usePresupuestoResumen(resumenId);

  const [tipoTramiteNuevoId, setTipoTramiteNuevoId] = useState(NO_TEMPLATE);
  const [itemsPresupuestoId, setItemsPresupuestoId] = useState<number | null>(null);
  const { data: presupuestoItems = [], isLoading: isItemsLoading } =
    useItemsByPresupuesto(itemsPresupuestoId ?? undefined);
  const { data: tiposTramite = [] } = useTiposTramite();
  const { data: catalogoItems = [] } = useItems();
  const [selectedTipoTramiteId, setSelectedTipoTramiteId] = useState<string>("");
  const [selectedCatalogItemId, setSelectedCatalogItemId] = useState<string>("");
  const cargarPlantillaMutation = useCargarItemsDesdePlantilla();
  const agregarCatalogoMutation = useAgregarItemsDesdeCatalogo();

  function openItems(p: Presupuesto) {
    setItemsPresupuestoId(p.idBudget!);
    setSelectedTipoTramiteId("");
    setSelectedCatalogItemId("");
  }

  async function handleCargarPlantilla() {
    if (!itemsPresupuestoId || !selectedTipoTramiteId) return;
    try {
      await cargarPlantillaMutation.mutateAsync({
        idPresupuesto: itemsPresupuestoId,
        tipoTramiteId: Number(selectedTipoTramiteId),
      });
      toast.success(t("items.loadedFromPlantilla"));
    } catch (e) {
      // CU39: keep curated Spanish for "no plantilla" (400). Server body is
      // terse and would otherwise replace items.errorNoPlantilla via extractApiError.
      const noPlantilla = e instanceof ApiError && e.status === 400;
      presentMutationError(e, {
        fallback: noPlantilla ? t("items.errorNoPlantilla") : t("items.errorCargar"),
        preferFallback: noPlantilla,
      });
    }
  }

  async function handleAgregarCatalogo() {
    if (!itemsPresupuestoId || !selectedCatalogItemId) return;
    try {
      await agregarCatalogoMutation.mutateAsync({
        idPresupuesto: itemsPresupuestoId,
        idItems: [Number(selectedCatalogItemId)],
      });
      toast.success(t("items.addedFromCatalogo"));
      setSelectedCatalogItemId("");
    } catch (err) {
      presentMutationError(err, { fallback: t("items.errorAgregar") });
    }
  }

  const itemsSubtotal = presupuestoItems.reduce((sum, item) => sum + (item.value ?? 0), 0);

  const [searchPresupuesto, setSearchPresupuesto] = useState("");
  const [filterEstado, setFilterEstado] = useState<string>("TODOS");
  const search = searchPresupuesto.trim();
  const searchId = /^\d+$/.test(search) ? Number(search) : null;
  const byStatus = filterEstado !== "TODOS";

  // One server page at a time, page and size in the URL (#1340): the list used
  // to load size=1000, so budgets after the 1000th were unreachable.
  const paging = useUrlPagination();
  const { data: presupuestosPage, isLoading: isLoadingPage, isFetching } = usePresupuestosPage(
    { page: paging.page, size: paging.size },
    { enabled: !byStatus && searchId === null },
  );
  useClampPage(paging, byStatus || searchId !== null ? undefined : presupuestosPage?.totalPages);

  // CU60: the backend reads `status`; `estado` was ignored and returned every budget.
  const { data: byEstado = [], isLoading: isLoadingEstado } = useQuery({
    queryKey: ["presupuestos", "buscar", filterEstado],
    queryFn: () => apiGet<Presupuesto[]>(`/presupuestos/buscar?status=${encodeURIComponent(filterEstado)}`),
    enabled: byStatus,
  });

  // A budget number is looked up on the server, so it is found on any page.
  const { data: byId = null, isLoading: isLoadingId } = useQuery({
    queryKey: ["presupuestos", searchId ?? 0, "search"],
    queryFn: () =>
      apiGet<Presupuesto>(`/presupuestos/${searchId}`).catch((e) => {
        if (e instanceof ApiError && e.status === 404) return null;
        throw e;
      }),
    enabled: searchId !== null,
  });

  const shown = byStatus ? byEstado : presupuestosPage?.content ?? [];
  const isLoading = searchId !== null ? isLoadingId : byStatus ? isLoadingEstado : isLoadingPage;

  // A name filters the rows shown (the page or the status list): the backend has
  // no budget search by client name.
  const filteredPresupuestos =
    searchId !== null
      ? byId && (!byStatus || byId.status === filterEstado)
        ? [byId]
        : []
      : shown.filter((p) => {
          if (!search) return true;
          const q = search.toLowerCase();
          return p.person ? fullName(p.person).toLowerCase().includes(q) : false;
        });
  const showPagination = !byStatus && searchId === null && !search;

  function openCreate() {
    setEditing(EMPTY);
    setTipoTramiteNuevoId(NO_TEMPLATE);
    setIsEditMode(false);
    setModalOpen(true);
  }
  function openEdit(p: Presupuesto) { setEditing(p); setIsEditMode(true); setModalOpen(true); }

  async function handleSave() {
    try {
      if (isEditMode && editing.idBudget) {
        await updateMutation.mutateAsync({ id: editing.idBudget, data: editing });
        toast.success(t("updated"));
      } else {
        const tipoTramiteId = tipoTramiteNuevoId === NO_TEMPLATE ? undefined : Number(tipoTramiteNuevoId);
        const { itemsLoaded } = await createMutation.mutateAsync({ data: editing, tipoTramiteId });
        toast.success(t("created"));
        if (tipoTramiteId !== undefined && !itemsLoaded) toast.warning(t("itemsNotLoaded"));
      }
      setModalOpen(false);
    } catch (err) {
      presentMutationError(err, { fallback: t("errorSave") });
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success(t("deleted"));
    } catch (err) {
      showDeleteError(err, t("errorDelete"));
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<Presupuesto>[] = [
    {
      key: "id",
      header: tc("id"),
      render: (p) => <span className="text-xs text-muted-foreground">{p.idBudget}</span>,
      className: "w-12",
    },
    {
      key: "fecha",
      header: tc("date"),
      render: (p) => formatDate(p.date),
    },
    {
      key: "persona",
      header: t("fields.cliente"),
      render: (p) => p.person ? fullName(p.person) : <span className="text-muted-foreground">—</span>,
    },
    {
      key: "monto",
      header: tc("amount"),
      render: (p) => <span className="font-medium">{formatCurrency(p.propertyAmount)}</span>,
    },
    {
      key: "estado",
      header: tc("status"),
      render: (p) => p.status ?? "—",
    },
    {
      key: "actions",
      header: "",
      render: (p) => (
        <div className="flex gap-2 justify-end">
          <Button
            size="sm"
            variant="ghost"
            data-testid={`btn-resumen-presupuesto-${p.idBudget}`}
            aria-label={t("resumen.title")}
            onClick={() => setResumenId(p.idBudget!)}
          >
            <Receipt className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            aria-label={t("items.title")}
            data-testid={`btn-items-presupuesto-${p.idBudget}`}
            onClick={() => openItems(p)}
          >
            <ListChecks className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            data-testid={`btn-editar-presupuesto-${p.idBudget}`}
            aria-label={tc("edit")}
            onClick={() => openEdit(p)}
          >
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            data-testid={`btn-eliminar-presupuesto-${p.idBudget}`}
            aria-label={tc("delete")}
            onClick={() => setDeleteId(p.idBudget!)}
          >
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      ),
      className: "w-32",
    },
  ];

  return (
    <div>
      <AppHeader
        title={t("title")}
        actions={
          <Button onClick={openCreate} data-testid="btn-nuevo-presupuesto">
            <Plus className="h-4 w-4" />
            {t("newPresupuesto")}
          </Button>
        }
      />

      <div className="flex flex-wrap gap-3 px-4 pb-4">
        <Input
          placeholder={t("searchPlaceholder")}
          aria-label={t("searchPlaceholder")}
          value={searchPresupuesto}
          onChange={(e) => setSearchPresupuesto(e.target.value)}
          data-testid="input-search-presupuesto"
          className="w-full sm:w-52"
        />
        <Select value={filterEstado} onValueChange={setFilterEstado}>
          <SelectTrigger data-testid="select-estado" className="w-44" aria-label={t("estadoFilter")}>
            <SelectValue placeholder={`${tc("status")}...`} />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TODOS">Todos</SelectItem>
            <SelectItem value="BORRADOR">Borrador</SelectItem>
            <SelectItem value="APROBADO">Aprobado</SelectItem>
            <SelectItem value="RECHAZADO">Rechazado</SelectItem>
            <SelectItem value="FACTURADO">Facturado</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <DataTable
        data={filteredPresupuestos}
        columns={columns}
        isLoading={isLoading}
        isFetching={showPagination && isFetching}
        keyExtractor={(p) => p.idBudget!}
        emptyMessage={t("noData")}
        pagination={
          showPagination
            ? {
                page: presupuestosPage?.number ?? paging.page,
                size: paging.size,
                totalElements: presupuestosPage?.totalElements ?? 0,
                onPageChange: paging.setPage,
                onSizeChange: paging.setSize,
              }
            : undefined
        }
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={isEditMode ? t("editPresupuesto") : t("newPresupuesto")}>
              <FormField label={t("fields.cliente")} required>
                {/* Server search instead of the first 1000 people (#1340). */}
                <PersonPicker
                  value={editing.person?.personId}
                  selected={editing.person}
                  onChange={(person) => setEditing({ ...editing, person })}
                  aria-label={t("fields.cliente")}
                  data-testid="select-persona"
                />
              </FormField>
              <FormField label={tc("date")} required>
                <Input
                  type="date"
                  value={toDateInputValue(editing.date)}
                  onChange={(e) => setEditing({ ...editing, date: e.target.value })}
                />
              </FormField>
              <FormField label={`${tc("amount")} ($)`} required helperText={t("fields.montoAyuda")}>
                <Input
                  type="number"
                  step="0.01"
                  value={editing.propertyAmount ?? ""}
                  onChange={(e) => setEditing({ ...editing, propertyAmount: parseFloat(e.target.value) })}
                  data-testid="input-monto"
                />
              </FormField>
              {!isEditMode && (
                <FormField label={t("fields.tipoTramite")} helperText={t("fields.tipoTramiteAyuda")}>
                  <Select value={tipoTramiteNuevoId} onValueChange={setTipoTramiteNuevoId}>
                    <SelectTrigger data-testid="select-tipo-tramite-nuevo">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value={NO_TEMPLATE}>{t("fields.sinPlantilla")}</SelectItem>
                      {tiposTramite.map((tt) => (
                        <SelectItem key={tt.idProcedureType} value={tt.idProcedureType!.toString()}>
                          {tt.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </FormField>
              )}
              <FormField label={tc("status")}>
                <Select
                  value={editing.status ?? "BORRADOR"}
                  onValueChange={(v) => setEditing({ ...editing, status: v })}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="BORRADOR">Borrador</SelectItem>
                    <SelectItem value="APROBADO">Aprobado</SelectItem>
                    <SelectItem value="RECHAZADO">Rechazado</SelectItem>
                    <SelectItem value="FACTURADO">Facturado</SelectItem>
                  </SelectContent>
                </Select>
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending} data-testid="btn-guardar-presupuesto">
                {isEditMode ? tc("update") : tc("create")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <Dialog open={resumenId !== null} onOpenChange={(v) => !v && setResumenId(null)}>
        <DialogContent className="max-w-2xl" data-testid="dialog-resumen-presupuesto">
          <FormContainer>
            <FormHeader dialogTitle title={t("resumen.title")} />
            {isResumenLoading && (
              <p className="text-sm text-muted-foreground">{tc("loading")}</p>
            )}
            {resumenError && (
              <p className="text-sm text-destructive" data-testid="resumen-not-found">
                {resumenError instanceof ApiError && resumenError.status === 404
                  ? t("resumen.notFound")
                  : t("errorSave")}
              </p>
            )}
            {resumen && (
              <>
                <FormSection title={t("resumen.gestionSection")}>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <p className="text-muted-foreground">{t("resumen.numeroGestion")}</p>
                      <p className="font-medium">{resumen.numberManagement ?? "—"}</p>
                    </div>
                    <div>
                      <p className="text-muted-foreground">{t("resumen.encabezado")}</p>
                      <p className="font-medium">{resumen.encabezadoManagement ?? "—"}</p>
                    </div>
                    <div>
                      <p className="text-muted-foreground">{t("resumen.numeroPresupuesto")}</p>
                      <p className="font-medium">{resumen.numberBudget}</p>
                    </div>
                    <div>
                      <p className="text-muted-foreground">{tc("amount")} {t("resumen.total").toLowerCase()}</p>
                      <p className="font-medium">{formatCurrency(resumen.total)}</p>
                    </div>
                    <div>
                      <p className="text-muted-foreground">{t("resumen.saldo")}</p>
                      <p className="font-medium">{formatCurrency(resumen.pendingBalance)}</p>
                    </div>
                  </div>
                </FormSection>
                <FormSection title={t("resumen.pagosSection")}>
                  {resumen.payments.length === 0 ? (
                    <p className="text-sm text-muted-foreground" data-testid="resumen-sin-pagos">
                      {t("resumen.noPagos")}
                    </p>
                  ) : (
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>{tc("id")}</TableHead>
                          <TableHead>{tc("amount")}</TableHead>
                          <TableHead>{tc("date")}</TableHead>
                          <TableHead>{tc("observations")}</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {resumen.payments.map((pago) => (
                          <TableRow key={pago.idPayment}>
                            <TableCell>{pago.idPayment}</TableCell>
                            <TableCell>{formatCurrency(pago.amount)}</TableCell>
                            <TableCell>{formatDate(pago.date)}</TableCell>
                            <TableCell>{pago.notes ?? "—"}</TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  )}
                </FormSection>
              </>
            )}
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setResumenId(null)}>
                {tc("cancel")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <Dialog open={itemsPresupuestoId !== null} onOpenChange={(v) => !v && setItemsPresupuestoId(null)}>
        <DialogContent className="max-w-2xl" data-testid="dialog-items-presupuesto">
          <FormContainer>
            <FormHeader dialogTitle title={t("items.title")} />

            <FormSection title={t("items.plantillaSection")}>
              <FormField
                label={t("items.selectTipoTramite")}
                helperText={tiposTramite.length === 0 ? t("items.noTiposTramite") : undefined}
              >
                <div className="flex gap-2">
                  <Select value={selectedTipoTramiteId} onValueChange={setSelectedTipoTramiteId}>
                    <SelectTrigger data-testid="select-tipo-tramite-items" disabled={tiposTramite.length === 0}>
                      <SelectValue placeholder={t("items.selectTipoTramite")} />
                    </SelectTrigger>
                    <SelectContent>
                      {tiposTramite.map((tt) => (
                        <SelectItem key={tt.idProcedureType} value={tt.idProcedureType!.toString()}>
                          {tt.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <Button
                    data-testid="btn-cargar-plantilla"
                    disabled={!selectedTipoTramiteId || cargarPlantillaMutation.isPending}
                    onClick={handleCargarPlantilla}
                  >
                    {t("items.cargarPlantilla")}
                  </Button>
                </div>
              </FormField>
            </FormSection>

            <FormSection title={t("items.catalogoSection")}>
              <FormField
                label={t("items.selectCatalogItem")}
                helperText={catalogoItems.length === 0 ? t("items.noCatalogItems") : undefined}
              >
                <div className="flex gap-2">
                  <Select value={selectedCatalogItemId} onValueChange={setSelectedCatalogItemId}>
                    <SelectTrigger data-testid="select-catalog-item" disabled={catalogoItems.length === 0}>
                      <SelectValue placeholder={t("items.selectCatalogItem")} />
                    </SelectTrigger>
                    <SelectContent>
                      {catalogoItems.map((item) => (
                        <SelectItem key={item.idItem} value={item.idItem!.toString()}>
                          {item.name} — {formatCurrency(item.value)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <Button
                    data-testid="btn-agregar-catalogo"
                    disabled={!selectedCatalogItemId || agregarCatalogoMutation.isPending}
                    onClick={handleAgregarCatalogo}
                  >
                    {t("items.agregarCatalogo")}
                  </Button>
                </div>
              </FormField>
            </FormSection>

            <FormSection title={t("items.itemsSection")}>
              {isItemsLoading ? (
                <p className="text-sm text-muted-foreground">{tc("loading")}</p>
              ) : presupuestoItems.length === 0 ? (
                <p className="text-sm text-muted-foreground" data-testid="items-sin-datos">
                  {t("items.noData")}
                </p>
              ) : (
                <>
                  <Table data-testid="table-items-presupuesto">
                    <TableHeader>
                      <TableRow>
                        <TableHead>{t("items.columns.nombre")}</TableHead>
                        <TableHead>{t("items.columns.valor")}</TableHead>
                        <TableHead>{t("items.columns.porcentaje")}</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {presupuestoItems.map((item) => (
                        <TableRow key={item.idItem}>
                          <TableCell>{item.name}</TableCell>
                          <TableCell>{formatCurrency(item.value)}</TableCell>
                          <TableCell>{item.percentage ?? 0}%</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                  <div className="flex justify-end pt-2 text-sm font-medium" data-testid="items-subtotal">
                    {t("items.subtotal")}: {formatCurrency(itemsSubtotal)}
                  </div>
                </>
              )}
            </FormSection>

            <FormActions align="right">
              <Button variant="secondary" onClick={() => setItemsPresupuestoId(null)}>
                {tc("cancel")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={!!deleteId}
        onOpenChange={(v) => !v && setDeleteId(null)}
        onConfirm={handleDelete}
        loading={deleteMutation.isPending}
      />
    </div>
  );
}
