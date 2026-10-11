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

const FOLIO_STATUSES = ["Nuevo", "Utilizado", "Errose"] as const;
const STATUS_IN_USE = "Utilizado";
const STATUS_DEED_SIGNED = "Firmada";

interface FolioTypeRow {
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
  const [statusFilter, setStatusFilter] = useState<string>("");

  const { data = [], isLoading, refetch } = useQuery({
    queryKey: ["folios", statusFilter],
    queryFn: () =>
      statusFilter
        ? apiGet<Folio[]>(`/folios/search?status=${encodeURIComponent(statusFilter)}`)
        : apiGet<Folio[]>("/folios"),
  });

  const { data: folioTypes = [], refetch: refetchFolioTypes } = useQuery({
    queryKey: ["folioTypes"],
    queryFn: () => apiGet<FolioTypeRow[]>("/folio-types"),
  });

  const [typeSearch, setTypeSearch] = useState("");
  const [typeModalOpen, setTypeModalOpen] = useState(false);
  const [typeEditing, setTypeEditing] = useState<FolioTypeRow | null>(null);
  const [typeName, setTypeName] = useState("");
  const [typeIsAuxiliary, setTypeIsAuxiliary] = useState(false);
  const [typeDeleteId, setTypeDeleteId] = useState<number | null>(null);
  const [typeSaving, setTypeSaving] = useState(false);
  const [typeDeleting, setTypeDeleting] = useState(false);

  const { data: filteredFolioTypes = [] } = useQuery({
    queryKey: ["folioTypes", "search", typeSearch, folioTypes],
    queryFn: () =>
      typeSearch
        ? apiGet<FolioTypeRow[]>(`/folio-types/search?name=${encodeURIComponent(typeSearch)}`)
        : Promise.resolve(folioTypes),
  });

  function openCreateType() {
    setTypeEditing(null);
    setTypeName("");
    setTypeIsAuxiliary(false);
    setTypeModalOpen(true);
  }

  function openEditType(tf: FolioTypeRow) {
    setTypeEditing(tf);
    setTypeName(tf.name);
    setTypeIsAuxiliary(tf.isAuxiliary ?? false);
    setTypeModalOpen(true);
  }

  async function handleSaveType() {
    if (!typeName.trim()) {
      toast.error(t("tiposDeFolio.nameRequired"));
      return;
    }
    setTypeSaving(true);
    try {
      if (typeEditing) {
        // PUT replaces the row: send the loaded fields (notes, enabled, version) with the edited ones,
        // otherwise version reads as 0 and every edit after the first answers 409 (#655).
        await apiPut(`/folio-types/${typeEditing.idFolioType}`, {
          ...typeEditing,
          name: typeName,
          isAuxiliary: typeIsAuxiliary,
        });
        toast.success(t("tiposDeFolio.updated"));
      } else {
        await apiPost("/folio-types", { name: typeName, isAuxiliary: typeIsAuxiliary });
        toast.success(t("tiposDeFolio.created"));
      }
      setTypeModalOpen(false);
      refetchFolioTypes();
    } catch (err) {
      toast.error(extractApiError(err) ?? t("tiposDeFolio.errorSave"));
    } finally {
      setTypeSaving(false);
    }
  }

  async function handleDeleteTypeClick(tf: FolioTypeRow) {
    try {
      const { inUse } = await apiGet<{ inUse: boolean }>(`/folio-types/${tf.idFolioType}/in-use`);
      if (inUse) {
        toast.warning(t("tiposDeFolio.inUseCannotDelete"));
        return;
      }
      setTypeDeleteId(tf.idFolioType);
    } catch (err) {
      showDeleteError(err, t("tiposDeFolio.errorDelete"));
    }
  }

  async function handleConfirmDeleteType() {
    if (!typeDeleteId) return;
    setTypeDeleting(true);
    try {
      await apiDelete(`/folio-types/${typeDeleteId}`);
      toast.success(t("tiposDeFolio.deleted"));
      refetchFolioTypes();
    } catch (err) {
      showDeleteError(err, t("tiposDeFolio.errorDelete"));
    } finally {
      setTypeDeleting(false);
      setTypeDeleteId(null);
    }
  }

  const { data: notaries = [] } = useQuery({
    queryKey: ["availableNotaries"],
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
        const inUse = f.status === STATUS_IN_USE;
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
        <Select value={statusFilter || "all"} onValueChange={(v) => setStatusFilter(v === "all" ? "" : v)}>
          <SelectTrigger className="w-48" data-testid="select-filter-status-folio" aria-label={t("estadoFilter")}>
            <SelectValue placeholder={t("fields.estadoPlaceholder")} />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">{tc("all")}</SelectItem>
            {FOLIO_STATUSES.map((e) => (
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
                    {FOLIO_STATUSES.map((e) => (
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
                    {folioTypes.map((tf) => (
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
            <Button onClick={openCreateType} data-testid="btn-new-folio-type">
              <NotaireIcon src="/icons/actions/agregar.png" alt={tc("add")} size={16} className="mr-1" />
              {t("tiposDeFolio.newTipo")}
            </Button>
          }
        />
        <div className="px-4 pb-4">
          <Input
            placeholder={t("tiposDeFolio.searchPlaceholder")}
            value={typeSearch}
            onChange={(e) => setTypeSearch(e.target.value)}
            className="w-64"
            data-testid="input-search-folio-type"
          />
        </div>
        <DataTable
          data={filteredFolioTypes}
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
                  <Button size="sm" variant="ghost" onClick={() => openEditType(tf)} data-testid="btn-edit-folio-type" aria-label={tc("edit")}>
                    <NotaireIcon src="/icons/actions/generate.png" alt={tc("edit")} size={16} />
                  </Button>
                  <Button
                    size="sm"
                    variant="ghost"
                    className="text-destructive hover:text-destructive"
                    onClick={() => handleDeleteTypeClick(tf)}
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

      <Dialog open={typeModalOpen} onOpenChange={setTypeModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={typeEditing ? t("tiposDeFolio.editTipo") : t("tiposDeFolio.newTipo")}>
              <FormField label={tc("name")} required>
                <Input
                  value={typeName}
                  onChange={(e) => setTypeName(e.target.value)}
                  data-testid="input-name-folio-type"
                />
              </FormField>
              <CheckboxField
                label={t("tiposDeFolio.esAuxiliar")}
                checked={typeIsAuxiliary}
                onChange={setTypeIsAuxiliary}
                data-testid="checkbox-is-auxiliary-folio-type"
              />
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setTypeModalOpen(false)}>
                <NotaireIcon src="/icons/actions/cerrar.png" alt={tc("cancel")} size={16} className="mr-1" />
                {tc("cancel")}
              </Button>
              <Button onClick={handleSaveType} disabled={typeSaving} data-testid="btn-save-folio-type">
                <NotaireIcon src="/icons/actions/guardar.png" alt={tc("save")} size={16} className="mr-1 brightness-0 invert" />
                {typeEditing ? tc("update") : tc("save")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={!!typeDeleteId}
        onOpenChange={(v) => !v && setTypeDeleteId(null)}
        onConfirm={handleConfirmDeleteType}
        loading={typeDeleting}
      />
    </div>
  );
}
