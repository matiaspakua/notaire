"use client";

import { Suspense, useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, Archive, RefreshCcw, History, FolderClock, ClipboardList } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { FormContainer, FormSection, FormField, FormActions, FormHeader } from "@/theme/form-patterns";
import {
  useGestionesPage,
  useGestionesByCliente,
  useCreateCompleteGestion,
  useUpdateGestion,
  useDeleteGestion,
  useSaldoPendiente,
  useArchivarGestion,
  useTransicionarGestion,
  useHistorial,
  useCarpetasByGestion,
  usePonerCarpetaEnEspera,
} from "@/hooks/useGestiones";
import { useGestionWorkflowTrace } from "@/hooks/useGestionWorkflow";
import { PersonPicker } from "@/components/shared/PersonPicker";
import { useClampPage, useUrlPagination } from "@/hooks/useUrlPagination";
import { BudgetPicker } from "@/components/shared/BudgetPicker";
import { useEstadosGestion } from "@/hooks/useEstadosGestion";
import { useTiposTramite } from "@/hooks/useTiposTramite";
import { useInmuebles } from "@/hooks/useInmuebles";
import { ApiError } from "@/lib/api-client";
import { formatCalendarDate } from "@/lib/dates";
import { formatCurrency, formatDate, extractApiError } from "@/lib/utils";
import type { GestionDeEscritura } from "@/types";
import { GestionResumenDialog } from "./GestionResumenDialog";
import { useDeleteError } from "@/hooks/useDeleteError";

const ESTADO_CARPETA_ACTIVA = "Activa";

const ESTADO_ARCHIVADA = "Archivada";

export default function GestionesPage() {
  return (
    <Suspense>
      <GestionesList />
    </Suspense>
  );
}

function GestionesList() {
  const t = useTranslations("gestiones");
  const showDeleteError = useDeleteError();
  const tc = useTranslations("common");
  // One server page at a time, page and size in the URL (#1340): the list used
  // to load size=1000, hiding older managements and rendering 1000 rows.
  const paging = useUrlPagination();
  const { data: gestionesPage, isLoading, isFetching } = useGestionesPage({ page: paging.page, size: paging.size });
  const gestiones = gestionesPage?.content ?? [];
  useClampPage(paging, gestionesPage?.totalPages);
  const { data: estados = [] } = useEstadosGestion();
  const { data: tiposTramite = [] } = useTiposTramite();
  const { data: inmuebles = [] } = useInmuebles();
  const createCompleteMutation = useCreateCompleteGestion();
  const updateMutation = useUpdateGestion();
  const deleteMutation = useDeleteGestion();
  const archivarMutation = useArchivarGestion();
  const transicionarMutation = useTransicionarGestion();
  const ponerEnEsperaMutation = usePonerCarpetaEnEspera();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [archiveId, setArchiveId] = useState<number | null>(null);
  const [archiveConflict, setArchiveConflict] = useState<string | null>(null);
  const [transitionId, setTransitionId] = useState<number | null>(null);
  const [selectedEstado, setSelectedEstado] = useState("");
  const [bitacoraId, setBitacoraId] = useState<number | null>(null);
  const [carpetasGestionId, setCarpetasGestionId] = useState<number | null>(null);
  const [resumenCasoId, setResumenCasoId] = useState<number | null>(null);
  const [esperaCarpetaId, setEsperaCarpetaId] = useState<number | null>(null);
  const [motivoEspera, setMotivoEspera] = useState("");
  const [editing, setEditing] = useState<GestionDeEscritura | null>(null);
  const [numero, setNumero] = useState("");
  const [clienteFilter, setClienteFilter] = useState("");
  const [presupuestoId, setPresupuestoId] = useState("");
  const [escribanoId, setEscribanoId] = useState("");
  const [estadoId, setEstadoId] = useState("");
  const [tipoTramiteId, setTipoTramiteId] = useState("");
  const [inmuebleId, setInmuebleId] = useState("");

  const { data: gestionesByCliente = [], isLoading: isLoadingByCliente } = useGestionesByCliente(
    clienteFilter ? Number(clienteFilter) : 0
  );
  const { data: saldoPendiente } = useSaldoPendiente(archiveId ?? undefined);
  const { data: trace } = useGestionWorkflowTrace(transitionId ?? undefined);
  const { data: historial = [] } = useHistorial(bitacoraId ?? undefined);
  const { data: carpetas = [] } = useCarpetasByGestion(carpetasGestionId ?? undefined);

  const visibleGestiones = clienteFilter ? gestionesByCliente : gestiones;
  const isLoadingVisible = clienteFilter ? isLoadingByCliente : isLoading;

  const currentNode = trace?.nodes.find((n) => n.statusManagementName === trace.statusActual);
  const validDestinations = trace && currentNode
    ? trace.transitions
        .filter((tr) => tr.originNodeId === currentNode.id)
        .map((tr) => trace.nodes.find((n) => n.id === tr.destinationNodeId))
        .filter((n): n is NonNullable<typeof n> => !!n && !!n.statusManagementName)
    : [];

  function openCreate() {
    setEditing(null);
    setNumero("");
    setPresupuestoId("");
    setEscribanoId("");
    setEstadoId("");
    setTipoTramiteId("");
    setInmuebleId("");
    setModalOpen(true);
  }

  function openEdit(g: GestionDeEscritura) {
    setEditing(g);
    setNumero(g.number?.toString() ?? "");
    setModalOpen(true);
  }

  async function handleSave() {
    try {
      if (editing?.idManagement) {
        await updateMutation.mutateAsync({
          id: editing.idManagement,
          data: { number: numero ? Number(numero) : undefined },
        });
        toast.success(t("updated"));
      } else {
        const created = await createCompleteMutation.mutateAsync({
          number: Number(numero),
          presupuestoId: Number(presupuestoId),
          escribanoId: Number(escribanoId),
          estadoGestionId: Number(estadoId),
          tipoTramiteId: Number(tipoTramiteId),
          inmuebleId: inmuebleId ? Number(inmuebleId) : undefined,
        });
        toast.success(t("created"));
        notifySubstitutionRedirect(created.notes);
      }
    } catch {
      toast.error(t("errorSave"));
    } finally {
      setModalOpen(false);
    }
  }

  function notifySubstitutionRedirect(notes?: string) {
    if (notes?.includes("redirected by active substitution")
        || notes?.includes("redirigida por suplencia activa")) {
      toast.info(t("suplenciaRedirected"), { description: notes });
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

  async function handleArchive() {
    if (!archiveId) return;
    try {
      await archivarMutation.mutateAsync({ id: archiveId, confirmado: !!archiveConflict });
      toast.success(t("archived"));
      setArchiveId(null);
      setArchiveConflict(null);
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        setArchiveConflict(extractApiError(err) ?? t("archiveConflictTitle"));
        return;
      }
      toast.error(extractApiError(err) ?? t("errorArchive"));
      setArchiveId(null);
      setArchiveConflict(null);
    }
  }

  function closeArchiveDialog(open: boolean) {
    if (!open) {
      setArchiveId(null);
      setArchiveConflict(null);
    }
  }

  async function handlePonerEnEspera() {
    if (!esperaCarpetaId || !carpetasGestionId) return;
    if (!motivoEspera.trim()) {
      toast.error(t("errorMotivoRequerido"));
      return;
    }
    try {
      await ponerEnEsperaMutation.mutateAsync({
        idCarpeta: esperaCarpetaId,
        motivo: motivoEspera,
        gestionId: carpetasGestionId,
      });
      toast.success(t("carpetaEnEspera"));
      setEsperaCarpetaId(null);
      setMotivoEspera("");
      setCarpetasGestionId(null);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorCarpetaEspera"));
    }
  }

  async function handleTransicionar() {
    if (!transitionId || !selectedEstado) return;
    try {
      await transicionarMutation.mutateAsync({ id: transitionId, estadoDestino: selectedEstado });
      toast.success(t("transitioned"));
      setTransitionId(null);
      setSelectedEstado("");
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorTransition"));
    }
  }

  function closeTransitionDialog(open: boolean) {
    if (!open) {
      setTransitionId(null);
      setSelectedEstado("");
    }
  }

  const columns: Column<GestionDeEscritura>[] = [
    {
      key: "numero",
      header: t("fields.numero"),
      render: (g) => (
        <div className="flex flex-col">
          <span className="font-medium">{g.number ?? "—"}</span>
          {/* The internal id stays available as secondary text (#1348). */}
          <span className="text-muted-foreground text-xs">
            {tc("id")} {g.idManagement}
          </span>
        </div>
      ),
    },
    {
      key: "encabezado",
      header: t("fields.encabezado"),
      render: (g) =>
        g.encabezado ? (
          <span className="block max-w-[18rem] truncate" title={g.encabezado}>
            {g.encabezado}
          </span>
        ) : (
          "—"
        ),
    },
    {
      key: "inicio",
      header: t("fields.inicio"),
      render: (g) => <span className="whitespace-nowrap">{formatCalendarDate(g.dateStart)}</span>,
    },
    {
      key: "tramites",
      header: t("fields.tramites"),
      render: (g) => g.procedureCount ?? 0,
      className: "text-right",
    },
    {
      key: "estado",
      header: t("fields.estado"),
      render: (g) => g.statusActual ?? "—",
    },
    {
      key: "actions",
      header: "",
      render: (g) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(g)} aria-label={tc("edit")}>
            <Pencil className="h-4 w-4" />
          </Button>
          {g.statusActual !== ESTADO_ARCHIVADA && (
            <Button
              size="sm"
              variant="ghost"
              onClick={() => setTransitionId(g.idManagement!)}
              aria-label={t("changeState")}
              data-testid={`btn-cambiar-estado-${g.idManagement}`}
            >
              <RefreshCcw className="h-4 w-4" />
            </Button>
          )}
          <Button
            size="sm"
            variant="ghost"
            onClick={() => setBitacoraId(g.idManagement!)}
            aria-label={t("viewBitacora")}
            data-testid={`btn-ver-bitacora-${g.idManagement}`}
          >
            <History className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            onClick={() => setResumenCasoId(g.idManagement!)}
            aria-label={t("resumen.action")}
            data-testid={`btn-resumen-caso-${g.idManagement}`}
          >
            <ClipboardList className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            onClick={() => setCarpetasGestionId(g.idManagement!)}
            aria-label={t("viewCarpetas")}
            data-testid={`btn-ver-carpetas-${g.idManagement}`}
          >
            <FolderClock className="h-4 w-4" />
          </Button>
          {g.statusActual !== ESTADO_ARCHIVADA && (
            <Button
              size="sm"
              variant="ghost"
              onClick={() => setArchiveId(g.idManagement!)}
              aria-label={t("archiveGestion")}
              data-testid={`btn-archivar-gestion-${g.idManagement}`}
            >
              <Archive className="h-4 w-4" />
            </Button>
          )}
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(g.idManagement!)}
            aria-label={tc("delete")}
          >
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      ),
      className: "w-56",
    },
  ];

  return (
    <div>
      <AppHeader
        title={t("title")}
        actions={
          <Button onClick={openCreate} data-testid="btn-nueva-gestion">
            <Plus className="h-4 w-4" />
            {t("newGestion")}
          </Button>
        }
      />

      <div className="px-4 pb-4 flex items-center gap-2">
        {/* Server search over clients instead of the first 1000 people (#1340). */}
        <PersonPicker
          value={clienteFilter ? Number(clienteFilter) : undefined}
          onChange={(person) => setClienteFilter(person?.personId != null ? String(person.personId) : "")}
          aria-label={t("clienteFilter")}
          placeholder={t("filterByCliente")}
          clientsOnly
          allowClear
          className="w-full sm:w-80"
          data-testid="select-filter-cliente-gestion"
        />
      </div>

      <DataTable
        data={visibleGestiones}
        columns={columns}
        isLoading={isLoadingVisible}
        isFetching={!clienteFilter && isFetching}
        keyExtractor={(g) => g.idManagement!}
        emptyMessage={t("noData")}
        pagination={
          clienteFilter
            ? undefined
            : {
                page: gestionesPage?.number ?? paging.page,
                size: paging.size,
                totalElements: gestionesPage?.totalElements ?? 0,
                onPageChange: paging.setPage,
                onSizeChange: paging.setSize,
              }
        }
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={editing ? t("editGestion") : t("newGestion")}>
              <FormField label={t("fields.numero")} required>
                <Input
                  type="number"
                  value={numero}
                  onChange={(e) => setNumero(e.target.value)}
                  placeholder={t("fields.numeroPlaceholder")}
                  data-testid="input-numero-gestion"
                />
              </FormField>
              {!editing && (
                <>
                  <FormField label={t("fields.presupuesto")} required>
                    {/* Server search instead of the first 1000 budgets (#1340). */}
                    <BudgetPicker
                      value={presupuestoId ? Number(presupuestoId) : undefined}
                      onChange={(budget) => setPresupuestoId(budget?.idBudget != null ? String(budget.idBudget) : "")}
                      aria-label={t("fields.presupuesto")}
                      data-testid="select-presupuesto-gestion"
                    />
                  </FormField>
                  <FormField label={t("fields.escribano")} required>
                    <PersonPicker
                      value={escribanoId ? Number(escribanoId) : undefined}
                      onChange={(person) => setEscribanoId(person?.personId != null ? String(person.personId) : "")}
                      aria-label={t("fields.escribano")}
                      data-testid="select-escribano-gestion"
                    />
                  </FormField>
                  <FormField label={t("fields.estado")} required>
                    <Select value={estadoId} onValueChange={setEstadoId}>
                      <SelectTrigger data-testid="select-estado-gestion"><SelectValue placeholder={t("selectEstado")} /></SelectTrigger>
                      <SelectContent>{estados.map((e) => <SelectItem key={e.idManagementStatus} value={String(e.idManagementStatus)}>{e.name}</SelectItem>)}</SelectContent>
                    </Select>
                  </FormField>
                  <FormField label={t("fields.tipo")} required>
                    <Select value={tipoTramiteId} onValueChange={setTipoTramiteId}>
                      <SelectTrigger data-testid="select-tipo-tramite-gestion"><SelectValue placeholder={t("selectTramite")} /></SelectTrigger>
                      <SelectContent>{tiposTramite.map((tt) => <SelectItem key={tt.idProcedureType} value={String(tt.idProcedureType)}>{tt.name}</SelectItem>)}</SelectContent>
                    </Select>
                  </FormField>
                  <FormField label={t("fields.inmueble")}>
                    <Select value={inmuebleId} onValueChange={setInmuebleId}>
                      <SelectTrigger data-testid="select-inmueble-gestion"><SelectValue placeholder={t("selectInmueble")} /></SelectTrigger>
                      <SelectContent>{inmuebles.map((i) => <SelectItem key={i.idProperty} value={String(i.idProperty)}>{i.address ?? t("inmuebleFallback", { id: i.idProperty ?? "—" })}</SelectItem>)}</SelectContent>
                    </Select>
                  </FormField>
                </>
              )}
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button
                onClick={handleSave}
                disabled={createCompleteMutation.isPending || updateMutation.isPending}
                data-testid="btn-guardar-gestion"
              >
                {editing ? tc("update") : tc("create")}
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

      <ConfirmDialog
        open={!!archiveId}
        onOpenChange={closeArchiveDialog}
        onConfirm={handleArchive}
        loading={archivarMutation.isPending}
        title={archiveConflict ? t("archiveConflictTitle") : t("archiveConfirmTitle")}
        description={
          archiveConflict
            ? archiveConflict
            : saldoPendiente && (saldoPendiente.pendingBalance ?? 0) > 0
              ? t("archiveConfirmDescriptionWithDebt", {
                  monto: formatCurrency(saldoPendiente.pendingBalance ?? 0),
                })
              : t("archiveConfirmDescriptionNoDebt")
        }
        confirmLabel={archiveConflict ? t("archiveConfirmAnyway") : t("archiveGestion")}
      />

      <Dialog open={!!transitionId} onOpenChange={closeTransitionDialog}>
        <DialogContent>
          <FormContainer>
            <FormHeader dialogTitle title={t("changeState")} description={t("selectNewStateDescription")} />
            <FormSection title={t("selectNewState")}>
              <FormField label={t("selectNewState")} required>
                <Select value={selectedEstado} onValueChange={setSelectedEstado}>
                  <SelectTrigger data-testid="select-nuevo-estado">
                    <SelectValue placeholder={t("selectNewState")} />
                  </SelectTrigger>
                  <SelectContent>
                    {validDestinations.map((n) => (
                      <SelectItem key={n.id} value={n.statusManagementName!}>
                        {n.statusManagementName}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => closeTransitionDialog(false)}>
                {tc("cancel")}
              </Button>
              <Button
                onClick={handleTransicionar}
                disabled={!selectedEstado || transicionarMutation.isPending}
                data-testid="btn-confirmar-transicion"
              >
                {t("confirmTransition")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <Dialog open={!!bitacoraId} onOpenChange={(v) => !v && setBitacoraId(null)}>
        <DialogContent>
          <FormContainer>
            <FormHeader dialogTitle title={t("bitacoraTitle")} />
            {historial.length === 0 ? (
              <p className="text-sm text-muted-foreground">{t("bitacoraEmpty")}</p>
            ) : (
              <div className="flex flex-col gap-3">
                {historial.map((h) => (
                  <div key={h.idHistory} data-testid="bitacora-item" className="border-b pb-2">
                    <div className="font-medium">{h.statusManagementName}</div>
                    <div className="text-xs text-muted-foreground">{formatDate(h.date)}</div>
                    {h.notes && <div className="text-sm">{h.notes}</div>}
                  </div>
                ))}
              </div>
            )}
          </FormContainer>
        </DialogContent>
      </Dialog>

      <Dialog
        open={!!carpetasGestionId}
        onOpenChange={(v) => {
          if (!v) {
            setCarpetasGestionId(null);
            setEsperaCarpetaId(null);
            setMotivoEspera("");
          }
        }}
      >
        <DialogContent>
          {esperaCarpetaId ? (
            <FormContainer>
              <FormHeader dialogTitle title={t("ponerEnEsperaTitle")} />
              <FormSection title={t("ponerEnEsperaTitle")}>
                <FormField label={t("motivoEspera")} required helperText={t("motivoEsperaHelper")}>
                  <Input
                    value={motivoEspera}
                    onChange={(e) => setMotivoEspera(e.target.value)}
                    placeholder={t("motivoEsperaPlaceholder")}
                    data-testid="input-motivo-espera"
                  />
                </FormField>
              </FormSection>
              <FormActions align="right">
                <Button
                  variant="secondary"
                  onClick={() => {
                    setEsperaCarpetaId(null);
                    setMotivoEspera("");
                  }}
                >
                  {tc("cancel")}
                </Button>
                <Button
                  onClick={handlePonerEnEspera}
                  disabled={ponerEnEsperaMutation.isPending}
                  data-testid="btn-confirmar-espera"
                >
                  {t("confirmEspera")}
                </Button>
              </FormActions>
            </FormContainer>
          ) : (
            <FormContainer>
              <FormHeader title={t("carpetasTitle")} />
              {carpetas.length === 0 ? (
                <p className="text-sm text-muted-foreground">{t("carpetasEmpty")}</p>
              ) : (
                <div className="flex flex-col gap-3">
                  {carpetas.map((c) => (
                    <div
                      key={c.idFolder}
                      data-testid="carpeta-item"
                      className="border-b pb-2 flex items-center justify-between"
                    >
                      <div>
                        <div className="font-medium">{t("carpetaNumero", { numero: c.number ?? 0 })}</div>
                        <div className="text-xs text-muted-foreground">{c.status}</div>
                        {c.waitReason && <div className="text-sm">{c.waitReason}</div>}
                      </div>
                      {c.status === ESTADO_CARPETA_ACTIVA && (
                        <Button
                          size="sm"
                          variant="secondary"
                          onClick={() => setEsperaCarpetaId(c.idFolder!)}
                          data-testid={`btn-poner-en-espera-${c.idFolder}`}
                        >
                          {t("ponerEnEspera")}
                        </Button>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </FormContainer>
          )}
        </DialogContent>
      </Dialog>

      <GestionResumenDialog gestionId={resumenCasoId} onClose={() => setResumenCasoId(null)} />
    </div>
  );
}
