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
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useQuery } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Folio } from "@/types";

const EMPTY: Partial<Folio> = { numero: undefined, anio: new Date().getFullYear(), estado: "", observaciones: "" };

export default function FoliosAdminPage() {
  const t = useTranslations("administracion.folios");
  const tc = useTranslations("common");

  const { data = [], isLoading, refetch } = useQuery({
    queryKey: ["folios"],
    queryFn: () => apiGet<Folio[]>("/folio"),
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Partial<Folio>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);

  function openCreate() {
    setEditing(EMPTY);
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(folio: Folio) {
    setEditing(folio);
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    if (editing.numero === undefined || editing.numero === null || Number.isNaN(editing.numero)) {
      toast.error(t("numberRequired"));
      return;
    }
    setSaving(true);
    try {
      const body = {
        numero: editing.numero,
        anio: editing.anio,
        estado: editing.estado,
        observaciones: editing.observaciones,
      };
      if (isEditMode && editing.idFolio) {
        await apiPut(`/folio/${editing.idFolio}`, body);
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
    { key: "tipo", header: t("fields.tipo"), render: (f) => f.tipoDeFolio?.nombre ?? "—" },
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
                  value={editing.numero ?? ""}
                  onChange={(e) => setEditing({ ...editing, numero: e.target.value === "" ? undefined : Number(e.target.value) })}
                  placeholder="1001"
                  data-testid="input-numero-folio"
                />
              </FormField>
              <FormField label={tc("year")}>
                <Input
                  type="number"
                  value={editing.anio ?? ""}
                  onChange={(e) => setEditing({ ...editing, anio: e.target.value === "" ? undefined : Number(e.target.value) })}
                  placeholder={String(new Date().getFullYear())}
                />
              </FormField>
              <FormField label={t("fields.estado")}>
                <Input value={editing.estado ?? ""} onChange={(e) => setEditing({ ...editing, estado: e.target.value })} />
              </FormField>
              <FormField label={tc("observations")}>
                <Input value={editing.observaciones ?? ""} onChange={(e) => setEditing({ ...editing, observaciones: e.target.value })} />
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
