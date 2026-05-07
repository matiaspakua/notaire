"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2 } from "lucide-react";
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
      numero: c.numero?.toString() ?? "",
      fechaImpresion: c.fechaImpresion?.split("T")[0] ?? "",
      fechaRetiro: c.fechaRetiro?.split("T")[0] ?? "",
      observaciones: c.observaciones ?? "",
    });
    setModalOpen(true);
  }

  async function handleSave() {
    const data: Partial<Copia> = {
      numero: form.numero ? Number(form.numero) : undefined,
      fechaImpresion: form.fechaImpresion || undefined,
      fechaRetiro: form.fechaRetiro || undefined,
      observaciones: form.observaciones || undefined,
    };
    try {
      if (editing?.idCopia) {
        await updateMutation.mutateAsync({ id: editing.idCopia, data });
        toast.success("Copia actualizada");
      } else {
        await createMutation.mutateAsync(data);
        toast.success("Copia creada");
      }
      setModalOpen(false);
    } catch {
      toast.error("Error al guardar la copia");
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Copia eliminada");
    } catch {
      toast.error("Error al eliminar la copia");
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<Copia>[] = [
    {
      key: "id",
      header: "ID",
      render: (c) => <span className="text-muted-foreground text-xs">{c.idCopia}</span>,
      className: "w-16",
    },
    {
      key: "numero",
      header: "Número",
      render: (c) => <span className="font-medium">{c.numero ?? "—"}</span>,
    },
    {
      key: "fechaImpresion",
      header: "Fecha Impresión",
      render: (c) => c.fechaImpresion ? new Date(c.fechaImpresion).toLocaleDateString("es-AR") : "—",
    },
    {
      key: "fechaRetiro",
      header: "Fecha Retiro",
      render: (c) => c.fechaRetiro ? new Date(c.fechaRetiro).toLocaleDateString("es-AR") : "—",
    },
    {
      key: "testimonio",
      header: "Testimonio",
      render: (c) => c.testimonio?.numero ? `#${c.testimonio.numero}` : "—",
      className: "w-32",
    },
    {
      key: "actions",
      header: "",
      render: (c) => (
        <div className="flex gap-1 justify-end">
          <Button size="icon" variant="ghost" onClick={() => openEdit(c)} aria-label="Editar">
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="icon"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(c.idCopia!)}
            aria-label="Eliminar"
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
        title="Copias"
        description="Gestión de copias de testimonios (CU70)"
        actions={
          <Button onClick={openCreate}>
            <Plus className="h-4 w-4" />
            Nueva copia
          </Button>
        }
      />

      <DataTable
        data={copias}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(c) => c.idCopia!}
        emptyMessage="No hay copias registradas"
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={editing ? "Editar copia" : "Nueva copia"}>
              <FormField label="Número" required>
                <Input
                  type="number"
                  value={form.numero}
                  onChange={(e) => setForm({ ...form, numero: e.target.value })}
                  placeholder="Ej: 1"
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
              <FormField label="Observaciones">
                <Input
                  value={form.observaciones}
                  onChange={(e) => setForm({ ...form, observaciones: e.target.value })}
                  placeholder="Notas adicionales"
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                Cancelar
              </Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>
                {editing ? "Actualizar" : "Crear"}
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
