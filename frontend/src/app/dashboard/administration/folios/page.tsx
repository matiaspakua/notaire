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
import { FormContainer, FormSection, FormField, FormActions, CheckboxField } from "@/theme/form-patterns";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import { extractApiError } from "@/lib/utils";
import { useDeeds } from "@/hooks/useDeeds";
import type { Folio, Person } from "@/types";
import { useDeleteError } from "@/hooks/useDeleteError";

const ESTADOS_FOLIO = ["Nuevo", "Utilizado", "Errose"] as const;
const ESTADO_UTILIZADO = "Utilizado";
const STATUS_DEED_SIGNED = "Firmada";

interface TipoDeFolioRow {
  idFolioType: number;
  name: string;
  isAuxiliary?: boolean;
  notes?: string;
  enabled?: boolean;
  /** Optimistic-lock version read from the list; the full update sends it back (#655). */
  version?: number;
}

interface FolioFormState {
  idFolio?: number;
  number?: number;
  year?: number;
  status: string;
  notes: string;
  folioTypeId?: number;
  notaryId?: number;
  deedId?: number;
}

const EMPTY: FolioFormState = {
  number: undefined,
  year: new Date().getFullYear(),
  status: "Nuevo",
  notes: "",
  folioTypeId: undefined,
  notaryId: undefined,
  deedId: undefined,
};

export default function FoliosAdminPage() {
  const t = useTranslations("administracion.folios");
  const showDeleteError = useDeleteError();
  const tc = useTranslations("common");

  const queryClient = useQueryClient();
  const [estadoFilter, setEstadoFilter] = useState<string>("");

  const { data = [], isLoading, refetch } = useQuery({
    queryKey: ["folios", estadoFilter],
    queryFn: () =>
      estadoFilter
        ? apiGet<Folio[]>(`/folios/search?status=${encodeURIComponent(estadoFilter)}`)
        : apiGet<Folio[]>("/folios"),
  });

  const { data: tiposFolio = [], refetch: refetchTiposFolio } = useQuery({
    queryKey: ["tipos-folio"],
    queryFn: () => apiGet<TipoDeFolioRow[]>("/folio-types"),
  });

  const [tipoSearch, setTipoSearch] = useState("");
  const [tipoModalOpen, setTipoModalOpen] = useState(false);
  const [tipoEditing, setTipoEditing] = useState<TipoDeFolioRow | null>(null);
  const [typeName, setTypeName] = useState("");
  const [tipoEsAuxiliar, setTipoEsAuxiliar] = useState(false);
  const [tipoDeleteId, setTipoDeleteId] = useState<number | null>(null);
  const [tipoSaving, setTipoSaving] = useState(false);
  const [tipoDeleting, setTipoDeleting] = useState(false);

  const { data: filteredTiposFolio = [] } = useQuery({
    queryKey: ["tipos-folio", "search", tipoSearch, tiposFolio],
    queryFn: () =>
      tipoSearch
        ? apiGet<TipoDeFolioRow[]>(`/folio-types/search?name=${encodeURIComponent(tipoSearch)}`)
        : Promise.resolve(tiposFolio),
  });

  function openCreateTipo() {
    setTipoEditing(null);
    setTypeName("");
    setTipoEsAuxiliar(false);
    setTipoModalOpen(true);
  }

  function openEditTipo(tf: TipoDeFolioRow) {
    setTipoEditing(tf);
    setTypeName(tf.name);
    setTipoEsAuxiliar(tf.isAuxiliary ?? false);
    setTipoModalOpen(true);
  }

  async function handleSaveTipo() {
    if (!typeName.trim()) {
      toast.error(t("tiposDeFolio.nameRequired"));
      return;
    }
    setTipoSaving(true);
    try {
      if (tipoEditing) {
        // PUT replaces the row: send the loaded fields (notes, enabled, version) with the edited ones,
        // otherwise version reads as 0 and every edit after the first answers 409 (#655).
        await apiPut(`/folio-types/${tipoEditing.idFolioType}`, {
          ...tipoEditing,
          name: typeName,
          isAuxiliary: tipoEsAuxiliar,
        });
        toast.success(t("tiposDeFolio.updated"));
      } else {
        await apiPost("/folio-types", { name: typeName, isAuxiliary: tipoEsAuxiliar });
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
      const { inUse } = await apiGet<{ inUse: boolean }>(`/folio-types/${tf.idFolioType}/in-use`);
      if (inUse) {
        toast.warning(t("tiposDeFolio.inUseCannotDelete"));
        return;
      }
      setTipoDeleteId(tf.idFolioType);
    } catch (err) {
      showDeleteError(err, t("tiposDeFolio.errorDelete"));
    }
  }

  async function handleConfirmDeleteTipo() {
    if (!tipoDeleteId) return;
    setTipoDeleting(true);
    try {
      await apiDelete(`/folio-types/${tipoDeleteId}`);
      toast.success(t("tiposDeFolio.deleted"));
      refetchTiposFolio();
    } catch (err) {
      showDeleteError(err, t("tiposDeFolio.errorDelete"));
    } finally {
      setTipoDeleting(false);
      setTipoDeleteId(null);
    }
  }

  const { data: notaries = [] } = useQuery({
    queryKey: ["notaries-disponibles"],
    queryFn: () => apiGet<Person[]>("/deeds/available-notaries"),
  });

  const { data: deeds = [] } = useDeeds();
  const { data: allFolios = [] } = useQuery({
    queryKey: ["folios"],
    queryFn: () => apiGet<Folio[]>("/folios"),
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState<FolioFormState>(EMPTY);
  const deedsLinkable = deeds.filter((e) => {
    if (e.status !== STATUS_DEED_SIGNED) return false;
    const yaVinculada = allFolios.some(
      (f) => f.fkIdDeed?.idDeed === e.idDeed && f.idFolio !== form.idFolio
    );
    return !yaVinculada;
  });
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
      number: folio.number,
      year: folio.year,
      status: folio.status ?? "Nuevo",
      notes: folio.notes ?? "",
      folioTypeId: folio.fkIdFolioType?.idFolioType,
      notaryId: folio.fkIdNotaryPerson?.personId,
      deedId: folio.fkIdDeed?.idDeed,
    });
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    if (form.number === undefined || form.number === null || Number.isNaN(form.number)) {
      toast.error(t("numberRequired"));
      return;
    }
    if (!form.folioTypeId) {
      toast.error(t("fields.tipo") + " " + tc("required"));
      return;
    }
    if (!form.notaryId) {
      toast.error(t("fields.escribano") + " " + tc("required"));
      return;
    }
    setSaving(true);
    try {
      const body = {
        number: form.number,
        year: form.year,
        status: form.status,
        notes: form.notes,
        typeFolioId: form.folioTypeId,
        notaryId: form.notaryId,
        deedId: form.deedId,
      };
      if (isEditMode && form.idFolio) {
        await apiPut(`/folios/${form.idFolio}`, body);
        toast.success(t("updated"));
      } else {
        await apiPost("/folios", body);
        toast.success(t("created"));
      }
      setModalOpen(false);
      refetch();
      queryClient.invalidateQueries({ queryKey: ["folios"] });
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
      await apiDelete(`/folios/${deleteId}`);
      toast.success(t("deleted"));
      refetch();
    } catch (err) {
      showDeleteError(err, t("errorDelete"));
    } finally {
      setDeleting(false);
      setDeleteId(null);
    }
  }

  const columns: Column<Folio>[] = [
    { key: "id", header: tc("id"), render: (f) => <span className="text-xs text-muted-foreground">{f.idFolio}</span>, className: "w-12" },
    { key: "number", header: t("fields.numero"), render: (f) => <span className="font-medium">{f.number}</span> },
    { key: "year", header: tc("year"), render: (f) => f.year ?? "—" },
    { key: "type", header: t("fields.tipo"), render: (f) => f.fkIdFolioType?.name ?? "—" },
    {
      key: "status",
      header: t("fields.estado"),
      render: (f) => (f.status ? <Badge variant="secondary">{f.status}</Badge> : "—"),
    },
    {
      key: "deed",
      header: t("fields.escritura"),
      render: (f) => (f.fkIdDeed?.number != null ? `Nº ${f.fkIdDeed.number}` : "—"),
    },
    {
      key: "actions",
      header: "",
      className: "w-24",
      render: (f) => {
        const inUse = f.status === ESTADO_UTILIZADO;
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
              <NotaireIcon src="/icons/actions/generate.png" alt={tc("edit")} size={16} />
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
          <Button onClick={openCreate} data-testid="btn-new-folio">
            <NotaireIcon src="/icons/actions/agregar.png" alt={tc("add")} size={16} className="mr-1" />
            {t("newFolio")}
          </Button>
        }
      />
      <div className="px-4 pb-4 flex items-center gap-2">
        <Select value={estadoFilter || "all"} onValueChange={(v) => setEstadoFilter(v === "all" ? "" : v)}>
          <SelectTrigger className="w-48" data-testid="select-filter-status-folio" aria-label={t("estadoFilter")}>
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
            <FormSection dialogTitle title={isEditMode ? t("editFolio") : t("newFolio")}>
              <FormField label={t("fields.numero")} required>
                <Input
                  type="number"
                  value={form.number ?? ""}
                  onChange={(e) => setForm({ ...form, number: e.target.value === "" ? undefined : Number(e.target.value) })}
                  placeholder="1001"
                  data-testid="input-folio-number"
                />
              </FormField>
              <FormField label={tc("year")}>
                <Input
                  type="number"
                  value={form.year ?? ""}
                  onChange={(e) => setForm({ ...form, year: e.target.value === "" ? undefined : Number(e.target.value) })}
                  placeholder={String(new Date().getFullYear())}
                />
              </FormField>
              <FormField label={t("fields.estado")} required>
                <Select value={form.status} onValueChange={(v) => setForm({ ...form, status: v })}>
                  <SelectTrigger data-testid="select-status-folio">
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
                  value={form.folioTypeId !== undefined ? String(form.folioTypeId) : ""}
                  onValueChange={(v) => setForm({ ...form, folioTypeId: Number(v) })}
                >
                  <SelectTrigger data-testid="select-folio-type">
                    <SelectValue placeholder={t("fields.tipoPlaceholder")} />
                  </SelectTrigger>
                  <SelectContent>
                    {tiposFolio.map((tf) => (
                      <SelectItem key={tf.idFolioType} value={String(tf.idFolioType)}>{tf.name}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label={t("fields.escribano")} required>
                <Select
                  value={form.notaryId !== undefined ? String(form.notaryId) : ""}
                  onValueChange={(v) => setForm({ ...form, notaryId: Number(v) })}
                >
                  <SelectTrigger data-testid="select-notary-folio">
                    <SelectValue placeholder={t("fields.escribanoPlaceholder")} />
                  </SelectTrigger>
                  <SelectContent>
                    {notaries.map((e) => (
                      <SelectItem key={e.personId} value={String(e.personId)}>
                        {e.lastName} {e.firstName}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label={t("fields.escritura")}>
                <Select
                  value={form.deedId !== undefined ? String(form.deedId) : "none"}
                  onValueChange={(v) => setForm({ ...form, deedId: v === "none" ? undefined : Number(v) })}
                >
                  <SelectTrigger data-testid="select-deed-folio">
                    <SelectValue placeholder={t("fields.escrituraPlaceholder")} />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="none">{t("fields.escrituraPlaceholder")}</SelectItem>
                    {deedsLinkable.map((e) => (
                      <SelectItem key={e.idDeed} value={String(e.idDeed)}>
                        {t("escrituraOption", { number: e.number ?? "—" })}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label={tc("observations")}>
                <Input
                  value={form.notes}
                  onChange={(e) => setForm({ ...form, notes: e.target.value })}
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
            <Button onClick={openCreateTipo} data-testid="btn-new-folio-type">
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
            data-testid="input-search-folio-type"
          />
        </div>
        <DataTable
          data={filteredTiposFolio}
          keyExtractor={(tf) => tf.idFolioType}
          emptyMessage={t("tiposDeFolio.noData")}
          columns={[
            { key: "id", header: tc("id"), render: (tf) => <span className="text-xs text-muted-foreground">{tf.idFolioType}</span>, className: "w-12" },
            { key: "name", header: tc("name"), render: (tf) => <span className="font-medium">{tf.name}</span> },
            {
              key: "auxiliary",
              header: t("tiposDeFolio.esAuxiliar"),
              render: (tf) => (tf.isAuxiliary ? <Badge variant="secondary">{t("tiposDeFolio.esAuxiliar")}</Badge> : "—"),
            },
            {
              key: "actions",
              header: "",
              className: "w-24",
              render: (tf) => (
                <div className="flex gap-2 justify-end">
                  <Button size="sm" variant="ghost" onClick={() => openEditTipo(tf)} data-testid="btn-edit-folio-type" aria-label={tc("edit")}>
                    <NotaireIcon src="/icons/actions/generate.png" alt={tc("edit")} size={16} />
                  </Button>
                  <Button
                    size="sm"
                    variant="ghost"
                    className="text-destructive hover:text-destructive"
                    onClick={() => handleDeleteTipoClick(tf)}
                    data-testid="btn-delete-folio-type"
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
            <FormSection dialogTitle title={tipoEditing ? t("tiposDeFolio.editTipo") : t("tiposDeFolio.newTipo")}>
              <FormField label={tc("name")} required>
                <Input
                  value={typeName}
                  onChange={(e) => setTypeName(e.target.value)}
                  data-testid="input-name-folio-type"
                />
              </FormField>
              <CheckboxField
                label={t("tiposDeFolio.esAuxiliar")}
                checked={tipoEsAuxiliar}
                onChange={setTipoEsAuxiliar}
                data-testid="checkbox-is-auxiliary-folio-type"
              />
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setTipoModalOpen(false)}>
                <NotaireIcon src="/icons/actions/cerrar.png" alt={tc("cancel")} size={16} className="mr-1" />
                {tc("cancel")}
              </Button>
              <Button onClick={handleSaveTipo} disabled={tipoSaving} data-testid="btn-save-folio-type">
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
