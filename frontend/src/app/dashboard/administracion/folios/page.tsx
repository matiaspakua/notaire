"use client";
import { useState } from "react";
import { toast } from "sonner";
import { useTranslations } from "next-intl";
import { NotaireIcon } from "@/components/ui/notaire-icon";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useQuery } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import { extractApiError } from "@/lib/utils";
import type { Folio, Persona } from "@/types";

const ESTADOS_FOLIO = ["Nuevo", "Utilizado", "Errose"] as const;
const ESTADO_UTILIZADO = "Utilizado";

interface TipoDeFolioRow {
  idTipoFolio: number;
  nombre: string;
}

interface FolioFormState {
  idFolio?: number;
  numero?: number;
  anio?: number;
  estado: string;
  observaciones: string;
  tipoFolioId?: number;
  escribanoId?: number;
}

const EMPTY: FolioFormState = {
  numero: undefined,
  anio: new Date().getFullYear(),
  estado: "Nuevo",
  observaciones: "",
  tipoFolioId: undefined,
  escribanoId: undefined,
};

export default function FoliosAdminPage() {
  const t = useTranslations("administracion.folios");
  const tc = useTranslations("common");

  const [estadoFilter, setEstadoFilter] = useState<string>("");

  const { data = [], isLoading, refetch } = useQuery({
    queryKey: ["folios", estadoFilter],
    queryFn: () =>
      estadoFilter
        ? apiGet<Folio[]>(`/folio/search?estado=${encodeURIComponent(estadoFilter)}`)
        : apiGet<Folio[]>("/folio"),
  });

  const { data: tiposFolio = [], refetch: refetchTiposFolio } = useQuery({
    queryKey: ["tipos-folio"],
    queryFn: () => apiGet<TipoDeFolioRow[]>("/tipo-folio"),
  });

  const [tipoSearch, setTipoSearch] = useState("");
  const [tipoModalOpen, setTipoModalOpen] = useState(false);
  const [tipoEditing, setTipoEditing] = useState<TipoDeFolioRow | null>(null);
  const [tipoNombre, setTipoNombre] = useState("");
  const [tipoDeleteId, setTipoDeleteId] = useState<number | null>(null);
  const [tipoSaving, setTipoSaving] = useState(false);
  const [tipoDeleting, setTipoDeleting] = useState(false);

  const { data: filteredTiposFolio = [] } = useQuery({
    queryKey: ["tipos-folio", "search", tipoSearch, tiposFolio],
    queryFn: () =>
      tipoSearch
        ? apiGet<TipoDeFolioRow[]>(`/tipo-folio/search?nombre=${encodeURIComponent(tipoSearch)}`)
        : Promise.resolve(tiposFolio),
  });

  function openCreateTipo() {
    setTipoEditing(null);
    setTipoNombre("");
    setTipoModalOpen(true);
  }

  function openEditTipo(tf: TipoDeFolioRow) {
    setTipoEditing(tf);
    setTipoNombre(tf.nombre);
    setTipoModalOpen(true);
  }

  async function handleSaveTipo() {
    if (!tipoNombre.trim()) {
      toast.error(t("tiposDeFolio.nameRequired"));
      return;
    }
    setTipoSaving(true);
    try {
      if (tipoEditing) {
        await apiPut(`/tipo-folio/${tipoEditing.idTipoFolio}`, { nombre: tipoNombre });
        toast.success(t("tiposDeFolio.updated"));
      } else {
        await apiPost("/tipo-folio", { nombre: tipoNombre });
        toast.success(t("tiposDeFolio.created"));
      }
      setTipoModalOpen(false);
      refetchTiposFolio();
    } catch (err) {
      toast.error(extractApiError(err) ?? t("tiposDeFolio.errorSave"));
    } finally {
      setTipoSaving(false);
    }
  }

  async function handleDeleteTipoClick(tf: TipoDeFolioRow) {
    try {
      const { inUse } = await apiGet<{ inUse: boolean }>(`/tipo-folio/${tf.idTipoFolio}/in-use`);
      if (inUse) {
        toast.error(t("tiposDeFolio.inUseCannotDelete"));
        return;
      }
      setTipoDeleteId(tf.idTipoFolio);
    } catch {
      toast.error(t("tiposDeFolio.errorDelete"));
    }
  }

  async function handleConfirmDeleteTipo() {
    if (!tipoDeleteId) return;
    setTipoDeleting(true);
    try {
      await apiDelete(`/tipo-folio/${tipoDeleteId}`);
      toast.success(t("tiposDeFolio.deleted"));
      refetchTiposFolio();
    } catch (err) {
      toast.error(extractApiError(err) ?? t("tiposDeFolio.errorDelete"));
    } finally {
      setTipoDeleting(false);
      setTipoDeleteId(null);
    }
  }

  const { data: escribanos = [] } = useQuery({
    queryKey: ["escribanos-disponibles"],
    queryFn: () => apiGet<Persona[]>("/escrituras/escribanos-disponibles"),
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState<FolioFormState>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);

  function openCreate() {
    setForm(EMPTY);
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(folio: Folio) {
    setForm({
      idFolio: folio.idFolio,
      numero: folio.numero,
      anio: folio.anio,
      estado: folio.estado ?? "Nuevo",
      observaciones: folio.observaciones ?? "",
      tipoFolioId: folio.tiposDeFolio?.idTipoFolio,
      escribanoId: folio.personaEscribano?.idPersona,
    });
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    if (form.numero === undefined || form.numero === null || Number.isNaN(form.numero)) {
      toast.error(t("numberRequired"));
      return;
    }
    if (!form.tipoFolioId) {
      toast.error(t("fields.tipo") + " " + tc("required"));
      return;
    }
    if (!form.escribanoId) {
      toast.error(t("fields.escribano") + " " + tc("required"));
      return;
    }
    setSaving(true);
    try {
      const body = {
        numero: form.numero,
        anio: form.anio,
        estado: form.estado,
        observaciones: form.observaciones,
        tipoFolioId: form.tipoFolioId,
        escribanoId: form.escribanoId,
      };
      if (isEditMode && form.idFolio) {
        await apiPut(`/folio/${form.idFolio}`, body);
        toast.success(t("updated"));
      } else {
        await apiPost("/folio", body);
        toast.success(t("created"));
      }
      setModalOpen(false);
      refetch();
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorCreate"));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    setDeleting(true);
    try {
      await apiDelete(`/folio/${deleteId}`);
      toast.success(t("deleted"));
      refetch();
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorDelete"));
    } finally {
      setDeleting(false);
      setDeleteId(null);
    }
  }

  const columns: Column<Folio>[] = [
    { key: "id", header: tc("id"), render: (f) => <span className="text-xs text-muted-foreground">{f.idFolio}</span>, className: "w-12" },
    { key: "numero", header: t("fields.numero"), render: (f) => <span className="font-medium">{f.numero}</span> },
    { key: "anio", header: tc("year"), render: (f) => f.anio ?? "—" },
    { key: "tipo", header: t("fields.tipo"), render: (f) => f.tiposDeFolio?.nombre ?? f.tipoDeFolio?.nombre ?? "—" },
    {
      key: "estado",
      header: t("fields.estado"),
      render: (f) => (f.estado ? <Badge variant="secondary">{f.estado}</Badge> : "—"),
    },
    {
      key: "actions",
      header: "",
      className: "w-24",
      render: (f) => {
        const inUse = f.estado === ESTADO_UTILIZADO;
        return (
          <div className="flex gap-2 justify-end">
            <Button
              size="sm"
              variant="ghost"
              onClick={() => openEdit(f)}
              disabled={inUse}
              title={inUse ? t("inUseCannotModify") : undefined}
              data-testid="btn-edit-folio"
              aria-label={tc("edit")}
            >
              <NotaireIcon src="/icons/actions/generar.png" alt={tc("edit")} size={16} />
            </Button>
            <Button
              size="sm"
              variant="ghost"
              className="text-destructive hover:text-destructive"
              onClick={() => setDeleteId(f.idFolio!)}
              disabled={inUse}
              title={inUse ? t("inUseCannotModify") : undefined}
              data-testid="btn-delete-folio"
              aria-label={tc("delete")}
            >
              <NotaireIcon src="/icons/actions/borrar.png" alt={tc("delete")} size={16} />
            </Button>
          </div>
        );
      },
    },
  ];

  return (
    <div>
      <AppHeader
        title={t("title")}
        description={t("description")}
        actions={
          <Button onClick={openCreate} data-testid="btn-nuevo-folio">
            <NotaireIcon src="/icons/actions/agregar.png" alt={tc("add")} size={16} className="mr-1" />
            {t("newFolio")}
          </Button>
        }
      />
      <div className="px-4 pb-4 flex items-center gap-2">
        <Select value={estadoFilter || "all"} onValueChange={(v) => setEstadoFilter(v === "all" ? "" : v)}>
          <SelectTrigger className="w-48" data-testid="select-filter-estado-folio">
            <SelectValue placeholder={t("fields.estadoPlaceholder")} />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">{tc("all")}</SelectItem>
            {ESTADOS_FOLIO.map((e) => (
              <SelectItem key={e} value={e}>{e}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <DataTable data={data} columns={columns} isLoading={isLoading} keyExtractor={(f) => f.idFolio!} emptyMessage={t("noData")} />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? t("editFolio") : t("newFolio")}>
              <FormField label={t("fields.numero")} required>
                <Input
                  type="number"
                  value={form.numero ?? ""}
                  onChange={(e) => setForm({ ...form, numero: e.target.value === "" ? undefined : Number(e.target.value) })}
                  placeholder="1001"
                  data-testid="input-numero-folio"
                />
              </FormField>
              <FormField label={tc("year")}>
                <Input
                  type="number"
                  value={form.anio ?? ""}
                  onChange={(e) => setForm({ ...form, anio: e.target.value === "" ? undefined : Number(e.target.value) })}
                  placeholder={String(new Date().getFullYear())}
                />
              </FormField>
              <FormField label={t("fields.estado")} required>
                <Select value={form.estado} onValueChange={(v) => setForm({ ...form, estado: v })}>
                  <SelectTrigger data-testid="select-estado-folio">
                    <SelectValue placeholder={t("fields.estadoPlaceholder")} />
                  </SelectTrigger>
                  <SelectContent>
                    {ESTADOS_FOLIO.map((e) => (
                      <SelectItem key={e} value={e}>{e}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label={t("fields.tipo")} required>
                <Select
                  value={form.tipoFolioId !== undefined ? String(form.tipoFolioId) : ""}
                  onValueChange={(v) => setForm({ ...form, tipoFolioId: Number(v) })}
                >
                  <SelectTrigger data-testid="select-tipo-folio">
                    <SelectValue
                      placeholder={t("fields.tipoPlaceholder")}
                      label={form.tipoFolioId ? tiposFolio.find((tf) => tf.idTipoFolio === form.tipoFolioId)?.nombre : undefined}
                    />
                  </SelectTrigger>
                  <SelectContent>
                    {tiposFolio.map((tf) => (
                      <SelectItem key={tf.idTipoFolio} value={String(tf.idTipoFolio)}>{tf.nombre}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label={t("fields.escribano")} required>
                <Select
                  value={form.escribanoId !== undefined ? String(form.escribanoId) : ""}
                  onValueChange={(v) => setForm({ ...form, escribanoId: Number(v) })}
                >
                  <SelectTrigger data-testid="select-escribano-folio">
                    <SelectValue
                      placeholder={t("fields.escribanoPlaceholder")}
                      label={form.escribanoId ? (() => {
                        const e = escribanos.find((w) => w.idPersona === form.escribanoId);
                        return e ? [e.apellido, e.nombre].filter(Boolean).join(" ") : undefined;
                      })() : undefined}
                    />
                  </SelectTrigger>
                  <SelectContent>
                    {escribanos.map((e) => (
                      <SelectItem key={e.idPersona} value={String(e.idPersona)}>
                        {e.apellido} {e.nombre}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label={tc("observations")}>
                <Input
                  value={form.observaciones}
                  onChange={(e) => setForm({ ...form, observaciones: e.target.value })}
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                <NotaireIcon src="/icons/actions/cerrar.png" alt={tc("cancel")} size={16} className="mr-1" />
                {tc("cancel")}
              </Button>
              <Button onClick={handleSave} disabled={saving}>
                <NotaireIcon src="/icons/actions/guardar.png" alt={tc("save")} size={16} className="mr-1 brightness-0 invert" />
                {isEditMode ? tc("update") : tc("save")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={!!deleteId} onOpenChange={(v) => !v && setDeleteId(null)} onConfirm={handleDelete} loading={deleting} />

      <div className="mt-8">
        <AppHeader
          title={t("tiposDeFolio.title")}
          actions={
            <Button onClick={openCreateTipo} data-testid="btn-nuevo-tipo-folio">
              <NotaireIcon src="/icons/actions/agregar.png" alt={tc("add")} size={16} className="mr-1" />
              {t("tiposDeFolio.newTipo")}
            </Button>
          }
        />
        <div className="px-4 pb-4">
          <Input
            placeholder={t("tiposDeFolio.searchPlaceholder")}
            value={tipoSearch}
            onChange={(e) => setTipoSearch(e.target.value)}
            className="w-64"
            data-testid="input-search-tipo-folio"
          />
        </div>
        <DataTable
          data={filteredTiposFolio}
          keyExtractor={(tf) => tf.idTipoFolio}
          emptyMessage={t("tiposDeFolio.noData")}
          columns={[
            { key: "id", header: tc("id"), render: (tf) => <span className="text-xs text-muted-foreground">{tf.idTipoFolio}</span>, className: "w-12" },
            { key: "nombre", header: tc("name"), render: (tf) => <span className="font-medium">{tf.nombre}</span> },
            {
              key: "actions",
              header: "",
              className: "w-24",
              render: (tf) => (
                <div className="flex gap-2 justify-end">
                  <Button size="sm" variant="ghost" onClick={() => openEditTipo(tf)} data-testid="btn-edit-tipo-folio" aria-label={tc("edit")}>
                    <NotaireIcon src="/icons/actions/generar.png" alt={tc("edit")} size={16} />
                  </Button>
                  <Button
                    size="sm"
                    variant="ghost"
                    className="text-destructive hover:text-destructive"
                    onClick={() => handleDeleteTipoClick(tf)}
                    data-testid="btn-delete-tipo-folio"
                    aria-label={tc("delete")}
                  >
                    <NotaireIcon src="/icons/actions/borrar.png" alt={tc("delete")} size={16} />
                  </Button>
                </div>
              ),
            },
          ]}
        />
      </div>

      <Dialog open={tipoModalOpen} onOpenChange={setTipoModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={tipoEditing ? t("tiposDeFolio.editTipo") : t("tiposDeFolio.newTipo")}>
              <FormField label={tc("name")} required>
                <Input
                  value={tipoNombre}
                  onChange={(e) => setTipoNombre(e.target.value)}
                  data-testid="input-nombre-tipo-folio"
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setTipoModalOpen(false)}>
                <NotaireIcon src="/icons/actions/cerrar.png" alt={tc("cancel")} size={16} className="mr-1" />
                {tc("cancel")}
              </Button>
              <Button onClick={handleSaveTipo} disabled={tipoSaving} data-testid="btn-save-tipo-folio">
                <NotaireIcon src="/icons/actions/guardar.png" alt={tc("save")} size={16} className="mr-1 brightness-0 invert" />
                {tipoEditing ? tc("update") : tc("save")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={!!tipoDeleteId}
        onOpenChange={(v) => !v && setTipoDeleteId(null)}
        onConfirm={handleConfirmDeleteTipo}
        loading={tipoDeleting}
      />
    </div>
  );
}
