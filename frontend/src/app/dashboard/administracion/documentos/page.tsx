"use client";
import { useState } from "react";
import { toast } from "sonner";
import { useTranslations } from "next-intl";
import { NotaireIcon } from "@/components/ui/notaire-icon";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import { extractApiError } from "@/lib/utils";
import type { TipoDeDocumento } from "@/types";

const EMPTY: Partial<TipoDeDocumento> = { nombre: "" };

export default function DocumentosPage() {
  const t = useTranslations("administracion.documentos");
  const tc = useTranslations("common");

  const qc = useQueryClient();
  const { data: tipos = [], isLoading } = useQuery({
    queryKey: ["tiposDocumento"],
    queryFn: () => apiGet<TipoDeDocumento[]>("/tipo-de-documento"),
  });

  const createMutation = useMutation({
    mutationFn: (data: Partial<TipoDeDocumento>) => apiPost<void>("/tipo-de-documento", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["tiposDocumento"] }),
  });
  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<TipoDeDocumento> }) =>
      apiPut<void>(`/tipo-de-documento/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["tiposDocumento"] }),
  });
  const deleteMutation = useMutation({
    mutationFn: (id: number) => apiDelete(`/tipo-de-documento/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["tiposDocumento"] }),
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<TipoDeDocumento>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);
  const [search, setSearch] = useState("");

  const { data: filtered = tipos } = useQuery({
    queryKey: ["tiposDocumento", "search", search, tipos],
    queryFn: () =>
      search.trim()
        ? apiGet<TipoDeDocumento[]>(`/tipo-de-documento/search?nombre=${encodeURIComponent(search.trim())}`)
        : Promise.resolve(tipos),
  });

  function openCreate() { setEditing(EMPTY); setIsEditMode(false); setModalOpen(true); }
  function openEdit(tipo: TipoDeDocumento) { setEditing(tipo); setIsEditMode(true); setModalOpen(true); }

  async function handleSave() {
    if (!editing.nombre?.trim()) { toast.error(t("nameRequired")); return; }
    try {
      if (isEditMode && editing.idTipoDocumento) {
        await updateMutation.mutateAsync({ id: editing.idTipoDocumento, data: editing });
        toast.success("Tipo de documento actualizado");
      } else {
        await createMutation.mutateAsync(editing);
        toast.success("Tipo de documento creado");
      }
      setModalOpen(false);
    } catch (err) {
      const apiError = extractApiError(err);
      toast.error(apiError ?? t("errorSave"));
    }
  }

  async function handleDeleteClick(tipo: TipoDeDocumento) {
    try {
      const { inUse } = await apiGet<{ inUse: boolean }>(`/tipo-de-documento/${tipo.idTipoDocumento}/in-use`);
      if (inUse) {
        toast.error(t("inUseCannotDelete"));
        return;
      }
      setDeleteId(tipo.idTipoDocumento!);
    } catch {
      toast.error(t("errorDelete"));
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Tipo de documento eliminado");
    } catch (err) {
      const apiError = extractApiError(err);
      toast.error(apiError ?? t("errorDelete"));
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<TipoDeDocumento>[] = [
    { key: "id", header: tc("id"), render: (tipo) => <span className="text-xs text-muted-foreground">{tipo.idTipoDocumento}</span>, className: "w-12" },
    { key: "nombre", header: tc("name"), render: (tipo) => <span className="font-medium">{tipo.nombre}</span> },
    {
      key: "actions", header: "", className: "w-24",
      render: (tipo) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(tipo)}>
            <NotaireIcon src="/icons/actions/generar.png" alt={tc("edit")} size={16} />
          </Button>
          <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive" onClick={() => handleDeleteClick(tipo)} data-testid="btn-delete-documento">
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
          <Button onClick={openCreate} data-testid="btn-nuevo-tipo-documento">
            <NotaireIcon src="/icons/actions/agregar.png" alt={tc("add")} size={16} className="mr-1" />
            {t("newDoc")}
          </Button>
        }
      />
      <div className="mb-4">
        <Input
          placeholder={t("searchPlaceholder")}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="max-w-sm"
          data-testid="input-search-documento"
        />
      </div>
      <DataTable
        data={filtered}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(tipo) => tipo.idTipoDocumento!}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? t("editDoc") : t("newDoc")}>
              <FormField label={tc("name")} required>
                <Input
                  value={editing.nombre ?? ""}
                  onChange={(e) => setEditing({ ...editing, nombre: e.target.value })}
                  placeholder={t("namePlaceholder")}
                  data-testid="input-nombre-documento"
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

      <ConfirmDialog
        open={!!deleteId}
        onOpenChange={(v) => !v && setDeleteId(null)}
        onConfirm={handleDelete}
        loading={deleteMutation.isPending}
      />
    </div>
  );
}
