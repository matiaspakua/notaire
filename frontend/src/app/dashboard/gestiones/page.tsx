"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2 } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import {
  useGestiones,
  useCreateGestion,
  useUpdateGestion,
  useDeleteGestion,
} from "@/hooks/useGestiones";
import type { GestionDeEscritura } from "@/types";

export default function GestionesPage() {
  const t = useTranslations("gestiones");
  const tc = useTranslations("common");
  const { data: gestiones = [], isLoading } = useGestiones();
  const createMutation = useCreateGestion();
  const updateMutation = useUpdateGestion();
  const deleteMutation = useDeleteGestion();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<GestionDeEscritura | null>(null);
  const [numero, setNumero] = useState("");

  function openCreate() {
    setEditing(null);
    setNumero("");
    setModalOpen(true);
  }

  function openEdit(g: GestionDeEscritura) {
    setEditing(g);
    setNumero(g.numero?.toString() ?? "");
    setModalOpen(true);
  }

  async function handleSave() {
    const data: Partial<GestionDeEscritura> = {
      numero: numero ? Number(numero) : undefined,
    };
    try {
      if (editing?.idGestion) {
        await updateMutation.mutateAsync({ id: editing.idGestion, data });
        toast.success(t("updated"));
      } else {
        await createMutation.mutateAsync(data);
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
      render: (g) => g.tramiteList?.length ?? 0,
    },
    {
      key: "actions",
      header: "",
      render: (g) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(g)} aria-label={tc("edit")}>
            <Pencil className="h-4 w-4" />
          </Button>
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
      className: "w-24",
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

      <DataTable
        data={gestiones}
        columns={columns}
        isLoading={isLoading}
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
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button
                onClick={handleSave}
                disabled={createMutation.isPending || updateMutation.isPending}
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
    </div>
  );
}
