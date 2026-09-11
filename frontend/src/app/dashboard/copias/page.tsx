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
  useCopias,
  useCreateCopia,
  useUpdateCopia,
  useDeleteCopia,
} from "@/hooks/useCopias";
import type { Copia } from "@/types";

export default function CopiasPage() {
  const t = useTranslations("copias");
  const tc = useTranslations("common");

  const { data: copias = [], isLoading } = useCopias();
  const createMutation = useCreateCopia();
  const updateMutation = useUpdateCopia();
  const deleteMutation = useDeleteCopia();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Copia | null>(null);
  const [form, setForm] = useState({
    numero: "",
    fechaImpresion: "",
    fechaRetiro: "",
    observaciones: "",
  });

  function openCreate() {
    setEditing(null);
    setForm({ numero: "", fechaImpresion: new Date().toISOString().split("T")[0], fechaRetiro: "", observaciones: "" });
    setModalOpen(true);
  }

  function openEdit(c: Copia) {
    setEditing(c);
    setForm({
      numero: c.number?.toString() ?? "",
      fechaImpresion: c.datePrinting?.split("T")[0] ?? "",
      fechaRetiro: c.dateWithdrawal?.split("T")[0] ?? "",
      observaciones: c.notes ?? "",
    });
    setModalOpen(true);
  }

  async function handleSave() {
    const data: Partial<Copia> = {
      number: form.numero ? Number(form.numero) : undefined,
      datePrinting: form.fechaImpresion || undefined,
      dateWithdrawal: form.fechaRetiro || undefined,
      notes: form.observaciones || undefined,
    };
    try {
      if (editing?.idCopy) {
        await updateMutation.mutateAsync({ id: editing.idCopy, data });
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

  const columns: Column<Copia>[] = [
    {
      key: "id",
      header: tc("id"),
      render: (c) => <span className="text-muted-foreground text-xs">{c.idCopy}</span>,
      className: "w-16",
    },
    {
      key: "numero",
      header: tc("number"),
      render: (c) => <span className="font-medium">{c.number ?? "—"}</span>,
    },
    {
      key: "fechaImpresion",
      header: "Fecha Impresión",
      render: (c) => c.datePrinting ? new Date(c.datePrinting).toLocaleDateString("es-AR") : "—",
    },
    {
      key: "fechaRetiro",
      header: "Fecha Retiro",
      render: (c) => c.dateWithdrawal ? new Date(c.dateWithdrawal).toLocaleDateString("es-AR") : "—",
    },
    {
      key: "testimonio",
      header: "Testimonio",
      render: (c) => c.fkIdTestimony?.number ? `#${c.fkIdTestimony.number}` : "—",
      className: "w-32",
    },
    {
      key: "actions",
      header: "",
      render: (c) => (
        <div className="flex gap-1 justify-end">
          <Button size="icon" variant="ghost" onClick={() => openEdit(c)} aria-label={tc("edit")}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="icon"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(c.idCopy!)}
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
          <Button onClick={openCreate}>
            <Plus className="h-4 w-4" />
            {t("newCopia")}
          </Button>
        }
      />

      <DataTable
        data={copias}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(c) => c.idCopy!}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={editing ? t("editCopia") : t("newCopia")}>
              <FormField label={tc("number")} required>
                <Input
                  type="number"
                  value={form.numero}
                  onChange={(e) => setForm({ ...form, numero: e.target.value })}
                />
              </FormField>
              <div className="grid grid-cols-2 gap-3">
                <FormField label="Fecha de Impresión">
                  <Input
                    type="date"
                    value={form.fechaImpresion}
                    onChange={(e) => setForm({ ...form, fechaImpresion: e.target.value })}
                  />
                </FormField>
                <FormField label="Fecha de Retiro">
                  <Input
                    type="date"
                    value={form.fechaRetiro}
                    onChange={(e) => setForm({ ...form, fechaRetiro: e.target.value })}
                  />
                </FormField>
              </div>
              <FormField label={tc("observations")}>
                <Input
                  value={form.observaciones}
                  onChange={(e) => setForm({ ...form, observaciones: e.target.value })}
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>
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
