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
import type { Folio, Persona } from "@/types";

const ESTADOS_FOLIO = ["Nuevo", "Utilizado", "Errose"] as const;

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

  const { data = [], isLoading, refetch } = useQuery({
    queryKey: ["folios"],
    queryFn: () => apiGet<Folio[]>("/folio"),
  });

  const { data: tiposFolio = [] } = useQuery({
    queryKey: ["tipos-folio"],
    queryFn: () => apiGet<{ idTipoFolio: number; nombre: string }[]>("/tipo-folio"),
  });

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
    } catch {
      toast.error(t("errorCreate"));
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
    } catch {
      toast.error(t("errorDelete"));
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
      render: (f) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(f)} data-testid="btn-edit-folio" aria-label={tc("edit")}>
            <NotaireIcon src="/icons/actions/generar.png" alt={tc("edit")} size={16} />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(f.idFolio!)}
            data-testid="btn-delete-folio"
            aria-label={tc("delete")}
          >
            <NotaireIcon src="/icons/actions/borrar.png" alt={tc("delete")} size={16} />
          </Button>
        </div>
      ),
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
    </div>
  );
}
