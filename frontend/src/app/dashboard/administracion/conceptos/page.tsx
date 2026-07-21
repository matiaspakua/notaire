"use client";

import { useState } from "react";
import { toast } from "sonner";
import { useTranslations } from "next-intl";
import { NotaireIcon } from "@/components/ui/notaire-icon";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import { useConceptos, useCreateConcepto, useUpdateConcepto, useDeleteConcepto } from "@/hooks/useConceptos";
import { formatCurrency, extractApiError } from "@/lib/utils";
import type { Concepto } from "@/types";

const EMPTY: Partial<Concepto> = { nombre: "", descripcion: "", valor: undefined };

export default function ConceptosPage() {
  const t = useTranslations("administracion.conceptos");
  const tc = useTranslations("common");

  const { data: conceptos = [], isLoading } = useConceptos();
  const createMutation = useCreateConcepto();
  const updateMutation = useUpdateConcepto();
  const deleteMutation = useDeleteConcepto();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Concepto>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);
  const [search, setSearch] = useState("");

  const { data: filtered = conceptos } = useQuery({
    queryKey: ["conceptos", "search", search, conceptos],
    queryFn: () =>
      search
        ? apiGet<Concepto[]>(`/conceptos/search?nombre=${encodeURIComponent(search)}`)
        : Promise.resolve(conceptos),
  });

  function openCreate() { setEditing(EMPTY); setIsEditMode(false); setModalOpen(true); }
  function openEdit(c: Concepto) { setEditing(c); setIsEditMode(true); setModalOpen(true); }

  async function handleSave() {
    if (!editing.nombre?.trim()) { toast.error(t("nameRequired")); return; }
    try {
      if (isEditMode && editing.idConcepto) {
        await updateMutation.mutateAsync({ id: editing.idConcepto, data: editing });
        toast.success(t("updated"));
      } else {
        await createMutation.mutateAsync(editing);
        toast.success(t("created"));
      }
      setModalOpen(false);
    } catch (err) {
      const conflict = extractApiError(err);
      if (conflict) {
        toast.error(conflict);
      } else {
        toast.error(t("errorSave"));
      }
    }
  }

  async function handleDeleteClick(c: Concepto) {
    try {
      const { inUse } = await apiGet<{ inUse: boolean }>(`/conceptos/${c.idConcepto}/in-use`);
      if (inUse) {
        toast.error(t("inUseCannotDelete"));
        return;
      }
      setDeleteId(c.idConcepto!);
    } catch {
      toast.error(t("errorDelete"));
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success(t("deleted"));
    } catch (err) {
      const conflict = extractApiError(err);
      if (conflict) {
        toast.error(conflict);
      } else {
        toast.error(t("errorDelete"));
      }
    } finally { setDeleteId(null); }
  }

  const columns: Column<Concepto>[] = [
    { key: "id", header: tc("id"), render: (c) => <span className="text-xs text-muted-foreground">{c.idConcepto}</span>, className: "w-12" },
    { key: "nombre", header: t("fields.nombre"), render: (c) => <span className="font-medium">{c.nombre}</span> },
    { key: "descripcion", header: t("fields.descripcion"), render: (c) => c.descripcion ?? "—" },
    { key: "valor", header: "Valor base", render: (c) => formatCurrency(c.valor) },
    {
      key: "actions", header: "", className: "w-24",
      render: (c) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(c)}><NotaireIcon src="/icons/actions/generar.png" alt={tc("edit")} size={16} /></Button>
          <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive" onClick={() => handleDeleteClick(c)} data-testid="btn-delete-concepto"><NotaireIcon src="/icons/actions/borrar.png" alt={tc("delete")} size={16} /></Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title={t("title")}
        description={t("description")}
        actions={<Button onClick={openCreate}><NotaireIcon src="/icons/actions/agregar.png" alt={tc("add")} size={16} className="mr-1" />{t("newConcepto")}</Button>}
      />

      <div className="px-4 pb-4">
        <Input
          placeholder={t("searchPlaceholder")}
          aria-label={t("searchPlaceholder")}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-64"
          data-testid="input-search-concepto"
        />
      </div>

      <DataTable data={filtered} columns={columns} isLoading={isLoading} keyExtractor={(c) => c.idConcepto!} emptyMessage={t("noData")} />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? t("editConcepto") : t("newConcepto")}>
              <FormField label={t("fields.nombre")} required>
                <Input
                  value={editing.nombre ?? ""}
                  onChange={(e) => setEditing({ ...editing, nombre: e.target.value })}
                  data-testid="input-nombre-concepto"
                />
              </FormField>
              <FormField label={t("fields.descripcion")}>
                <Input
                  value={editing.descripcion ?? ""}
                  onChange={(e) => setEditing({ ...editing, descripcion: e.target.value })}
                />
              </FormField>
              <FormField label="Valor base ($)">
                <Input
                  type="number"
                  step="0.01"
                  value={editing.valor ?? ""}
                  onChange={(e) => setEditing({ ...editing, valor: parseFloat(e.target.value) })}
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                <NotaireIcon src="/icons/actions/cerrar.png" alt={tc("cancel")} size={16} className="mr-1" />
                {tc("cancel")}
              </Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>
                <NotaireIcon src="/icons/actions/guardar.png" alt={tc("save")} size={16} className="mr-1 brightness-0 invert" />
                {isEditMode ? tc("update") : tc("save")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={!!deleteId} onOpenChange={(v) => !v && setDeleteId(null)} onConfirm={handleDelete} loading={deleteMutation.isPending} />
    </div>
  );
}
