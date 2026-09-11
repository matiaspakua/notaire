"use client";

import { useState } from "react";
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
import {
  usePresupuestos,
  usePresupuestoResumen,
  useCreatePresupuesto,
  useUpdatePresupuesto,
  useDeletePresupuesto,
  useCargarItemsDesdePlantilla,
  useAgregarItemsDesdeCatalogo,
} from "@/hooks/usePresupuestos";
import { usePersonas } from "@/hooks/usePersonas";
import { useItems, useItemsByPresupuesto } from "@/hooks/useItems";
import { useTiposTramite } from "@/hooks/useTiposTramite";
import { formatDate, formatCurrency, fullName } from "@/lib/utils";
import type { Presupuesto } from "@/types";

const EMPTY: Partial<Presupuesto> = { date: "", propertyAmount: undefined, status: "BORRADOR" };

export default function PresupuestosPage() {
  const t = useTranslations("presupuestos");
  const tc = useTranslations("common");

  const { data: presupuestos = [], isLoading } = usePresupuestos();
  const { data: personas = [] } = usePersonas();
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
      toast.error(
        e instanceof ApiError && e.status === 400
          ? t("items.errorNoPlantilla")
          : t("items.errorCargar")
      );
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
    } catch {
      toast.error(t("items.errorAgregar"));
    }
  }

  const itemsSubtotal = presupuestoItems.reduce((sum, item) => sum + (item.value ?? 0), 0);

  const [searchPresupuesto, setSearchPresupuesto] = useState("");
  const [filterEstado, setFilterEstado] = useState<string>("TODOS");

  const { data: byEstado = presupuestos } = useQuery({
    queryKey: ["presupuestos", "buscar", filterEstado, presupuestos],
    queryFn: () =>
      filterEstado !== "TODOS"
        ? apiGet<Presupuesto[]>(`/presupuestos/buscar?estado=${encodeURIComponent(filterEstado)}`)
        : Promise.resolve(presupuestos),
  });

  const filteredPresupuestos = byEstado.filter((p) => {
    if (searchPresupuesto) {
      const q = searchPresupuesto.toLowerCase();
      const matchId = p.idBudget?.toString().includes(q);
      const matchPersona = p.person ? fullName(p.person).toLowerCase().includes(q) : false;
      if (!matchId && !matchPersona) return false;
    }
    return true;
  });

  function openCreate() { setEditing(EMPTY); setIsEditMode(false); setModalOpen(true); }
  function openEdit(p: Presupuesto) { setEditing(p); setIsEditMode(true); setModalOpen(true); }

  async function handleSave() {
    try {
      if (isEditMode && editing.idBudget) {
        await updateMutation.mutateAsync({ id: editing.idBudget, data: editing });
        toast.success(t("updated"));
      } else {
        await createMutation.mutateAsync(editing);
        toast.success(t("created"));
      }
      setModalOpen(false);
    } catch {
      toast.error(t("errorSave"));
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success(t("deleted"));
    } catch {
      toast.error(t("errorDelete"));
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
          className="w-52"
        />
        <Select value={filterEstado} onValueChange={setFilterEstado}>
          <SelectTrigger data-testid="select-estado" className="w-44">
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
        keyExtractor={(p) => p.idBudget!}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? t("editPresupuesto") : t("newPresupuesto")}>
              <FormField
                label={t("fields.cliente")}
                required
                helperText={personas.length === 0 ? "No hay personas registradas. Primero registre una persona." : undefined}
              >
                <Select
                  value={editing.person?.idPerson?.toString() ?? ""}
                  onValueChange={(v) => {
                    const persona = personas.find((p) => p.personId?.toString() === v);
                    setEditing({ ...editing, person: persona });
                  }}
                >
                  <SelectTrigger data-testid="select-persona" disabled={personas.length === 0}>
                    <SelectValue placeholder="Seleccionar cliente..." />
                  </SelectTrigger>
                  <SelectContent>
                    {personas.map((p) => (
                      <SelectItem key={p.personId} value={p.personId!.toString()}>
                        {fullName(p)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label={tc("date")} required>
                <Input
                  type="date"
                  value={editing.date ?? ""}
                  onChange={(e) => setEditing({ ...editing, date: e.target.value })}
                />
              </FormField>
              <FormField label={`${tc("amount")} ($)`} required>
                <Input
                  type="number"
                  step="0.01"
                  value={editing.propertyAmount ?? ""}
                  onChange={(e) => setEditing({ ...editing, propertyAmount: parseFloat(e.target.value) })}
                  data-testid="input-monto"
                />
              </FormField>
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
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>
                {isEditMode ? tc("update") : tc("create")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <Dialog open={resumenId !== null} onOpenChange={(v) => !v && setResumenId(null)}>
        <DialogContent className="max-w-2xl" data-testid="dialog-resumen-presupuesto">
          <FormContainer>
            <FormHeader title={t("resumen.title")} />
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
                      <p className="font-medium">{formatCurrency(resumen.saldoPending)}</p>
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
            <FormHeader title={t("items.title")} />

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
