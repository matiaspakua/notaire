"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, Archive } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import {
  useGestiones,
  useGestionesByCliente,
  useCreateCompleteGestion,
  useUpdateGestion,
  useDeleteGestion,
  useSaldoPendiente,
  useArchivarGestion,
} from "@/hooks/useGestiones";
import { usePersonas } from "@/hooks/usePersonas";
import { usePresupuestos } from "@/hooks/usePresupuestos";
import { useEstadosGestion } from "@/hooks/useEstadosGestion";
import { useTiposTramite } from "@/hooks/useTiposTramite";
import { useInmuebles } from "@/hooks/useInmuebles";
import { fullName, formatCurrency } from "@/lib/utils";
import type { GestionDeEscritura } from "@/types";

const ESTADO_ARCHIVADA = "Archivada";

export default function GestionesPage() {
  const t = useTranslations("gestiones");
  const tc = useTranslations("common");
  const { data: gestiones = [], isLoading } = useGestiones();
  const { data: personas = [] } = usePersonas();
  const { data: presupuestos = [] } = usePresupuestos();
  const { data: estados = [] } = useEstadosGestion();
  const { data: tiposTramite = [] } = useTiposTramite();
  const { data: inmuebles = [] } = useInmuebles();
  const createCompleteMutation = useCreateCompleteGestion();
  const updateMutation = useUpdateGestion();
  const deleteMutation = useDeleteGestion();
  const archivarMutation = useArchivarGestion();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [archiveId, setArchiveId] = useState<number | null>(null);
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

  const visibleGestiones = clienteFilter ? gestionesByCliente : gestiones;
  const isLoadingVisible = clienteFilter ? isLoadingByCliente : isLoading;

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
    setNumero(g.numero?.toString() ?? "");
    setModalOpen(true);
  }

  async function handleSave() {
    try {
      if (editing?.idGestion) {
        await updateMutation.mutateAsync({
          id: editing.idGestion,
          data: { numero: numero ? Number(numero) : undefined },
        });
        toast.success(t("updated"));
      } else {
        await createCompleteMutation.mutateAsync({
          numero: Number(numero),
          presupuestoId: Number(presupuestoId),
          escribanoId: Number(escribanoId),
          estadoGestionId: Number(estadoId),
          tipoTramiteId: Number(tipoTramiteId),
          inmuebleId: inmuebleId ? Number(inmuebleId) : undefined,
        });
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

  async function handleArchive() {
    if (!archiveId) return;
    try {
      await archivarMutation.mutateAsync(archiveId);
      toast.success(t("archived"));
    } catch {
      toast.error(t("errorArchive"));
    } finally {
      setArchiveId(null);
    }
  }

  const columns: Column<GestionDeEscritura>[] = [
    {
      key: "id",
      header: tc("id"),
      render: (g) => <span className="text-muted-foreground text-xs">{g.idGestion}</span>,
      className: "w-16",
    },
    {
      key: "numero",
      header: t("fields.numero"),
      render: (g) => <span className="font-medium">{g.numero ?? "—"}</span>,
    },
    {
      key: "tramites",
      header: t("fields.tipo"),
      render: (g) => g.tramiteCount ?? 0,
    },
    {
      key: "actions",
      header: "",
      render: (g) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(g)} aria-label={tc("edit")}>
            <Pencil className="h-4 w-4" />
          </Button>
          {g.estadoActual !== ESTADO_ARCHIVADA && (
            <Button
              size="sm"
              variant="ghost"
              onClick={() => setArchiveId(g.idGestion!)}
              aria-label={t("archiveGestion")}
              data-testid={`btn-archivar-gestion-${g.idGestion}`}
            >
              <Archive className="h-4 w-4" />
            </Button>
          )}
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(g.idGestion!)}
            aria-label={tc("delete")}
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
          <Button onClick={openCreate} data-testid="btn-nueva-gestion">
            <Plus className="h-4 w-4" />
            {t("newGestion")}
          </Button>
        }
      />

      <div className="px-4 pb-4 flex items-center gap-2">
        <Select value={clienteFilter || "all"} onValueChange={(v) => setClienteFilter(v === "all" ? "" : v)}>
          <SelectTrigger className="w-56" data-testid="select-filter-cliente-gestion">
            <SelectValue placeholder={t("filterByCliente")} />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">{tc("all")}</SelectItem>
            {personas.filter((p) => p.esCliente).map((p) => (
              <SelectItem key={p.idPersona} value={String(p.idPersona)}>{fullName(p)}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <DataTable
        data={visibleGestiones}
        columns={columns}
        isLoading={isLoadingVisible}
        keyExtractor={(g) => g.idGestion!}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={editing ? t("editGestion") : t("newGestion")}>
              <FormField label={t("fields.numero")} required>
                <Input
                  type="number"
                  value={numero}
                  onChange={(e) => setNumero(e.target.value)}
                  placeholder="Ej: 1001"
                  data-testid="input-numero-gestion"
                />
              </FormField>
              {!editing && (
                <>
                  <FormField label={t("fields.presupuesto")} required>
                    <Select value={presupuestoId} onValueChange={setPresupuestoId}>
                      <SelectTrigger data-testid="select-presupuesto-gestion"><SelectValue placeholder="Seleccionar presupuesto..." /></SelectTrigger>
                      <SelectContent>{presupuestos.map((p) => <SelectItem key={p.idPresupuesto} value={String(p.idPresupuesto)}>Presupuesto #{p.idPresupuesto}</SelectItem>)}</SelectContent>
                    </Select>
                  </FormField>
                  <FormField label={t("fields.escribano")} required>
                    <Select value={escribanoId} onValueChange={setEscribanoId}>
                      <SelectTrigger data-testid="select-escribano-gestion"><SelectValue placeholder="Seleccionar escribano..." /></SelectTrigger>
                      <SelectContent>{personas.map((p) => <SelectItem key={p.idPersona} value={String(p.idPersona)}>{fullName(p)}</SelectItem>)}</SelectContent>
                    </Select>
                  </FormField>
                  <FormField label={t("fields.estado")} required>
                    <Select value={estadoId} onValueChange={setEstadoId}>
                      <SelectTrigger data-testid="select-estado-gestion"><SelectValue placeholder="Seleccionar estado..." /></SelectTrigger>
                      <SelectContent>{estados.map((e) => <SelectItem key={e.idEstadoGestion} value={String(e.idEstadoGestion)}>{e.nombre}</SelectItem>)}</SelectContent>
                    </Select>
                  </FormField>
                  <FormField label={t("fields.tipo")} required>
                    <Select value={tipoTramiteId} onValueChange={setTipoTramiteId}>
                      <SelectTrigger data-testid="select-tipo-tramite-gestion"><SelectValue placeholder="Seleccionar trámite..." /></SelectTrigger>
                      <SelectContent>{tiposTramite.map((tt) => <SelectItem key={tt.idTipoDeTramite} value={String(tt.idTipoDeTramite)}>{tt.nombre}</SelectItem>)}</SelectContent>
                    </Select>
                  </FormField>
                  <FormField label={t("fields.inmueble")}>
                    <Select value={inmuebleId} onValueChange={setInmuebleId}>
                      <SelectTrigger data-testid="select-inmueble-gestion"><SelectValue placeholder="Seleccionar inmueble..." /></SelectTrigger>
                      <SelectContent>{inmuebles.map((i) => <SelectItem key={i.idInmueble} value={String(i.idInmueble)}>{i.domicilio ?? `Inmueble #${i.idInmueble}`}</SelectItem>)}</SelectContent>
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
        onOpenChange={(v) => !v && setArchiveId(null)}
        onConfirm={handleArchive}
        loading={archivarMutation.isPending}
        title={t("archiveConfirmTitle")}
        description={
          saldoPendiente && saldoPendiente.saldoPendiente > 0
            ? t("archiveConfirmDescriptionWithDebt", { monto: formatCurrency(saldoPendiente.saldoPendiente) })
            : t("archiveConfirmDescriptionNoDebt")
        }
        confirmLabel={t("archiveGestion")}
      />
    </div>
  );
}
