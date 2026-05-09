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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import {
  useItems,
  useCreateItem,
  useUpdateItem,
  useDeleteItem,
} from "@/hooks/useItems";
import { useConceptos } from "@/hooks/useConceptos";
import type { Item, Concepto } from "@/types";

export default function ItemsPage() {
  const { data: items = [], isLoading } = useItems();
  const { data: conceptos = [] } = useConceptos();
  const createMutation = useCreateItem();
  const updateMutation = useUpdateItem();
  const deleteMutation = useDeleteItem();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Item | null>(null);
  const [form, setForm] = useState({
    conceptoId: "",
    cantidad: "",
    precio: "",
  });

  function openCreate() {
    setEditing(null);
    setForm({ conceptoId: "", cantidad: "", precio: "" });
    setModalOpen(true);
  }

  function openEdit(i: Item) {
    setEditing(i);
    setForm({
      conceptoId: i.concepto?.idConcepto?.toString() ?? "",
      cantidad: i.cantidad?.toString() ?? "",
      precio: i.precio?.toString() ?? "",
    });
    setModalOpen(true);
  }

  async function handleSave() {
    const data: Partial<Item> = {
      concepto: form.conceptoId ? { idConcepto: Number(form.conceptoId) } as Concepto : undefined,
      cantidad: form.cantidad ? Number(form.cantidad) : undefined,
      precio: form.precio ? Number(form.precio) : undefined,
    };
    try {
      if (editing?.idItem) {
        await updateMutation.mutateAsync({ id: editing.idItem, data });
        toast.success("Item actualizado");
      } else {
        await createMutation.mutateAsync(data);
        toast.success("Item creado");
      }
      setModalOpen(false);
    } catch {
      toast.error("Error al guardar el item");
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Item eliminado");
    } catch {
      toast.error("Error al eliminar el item");
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<Item>[] = [
    {
      key: "id",
      header: "ID",
      render: (i) => <span className="text-muted-foreground text-xs">{i.idItem}</span>,
      className: "w-16",
    },
    {
      key: "concepto",
      header: "Concepto",
      render: (i) => <span className="font-medium">{i.concepto?.nombre ?? "—"}</span>,
    },
    {
      key: "cantidad",
      header: "Cantidad",
      render: (i) => i.cantidad ?? "—",
      className: "w-24",
    },
    {
      key: "precio",
      header: "Precio",
      render: (i) => i.precio ? `$${i.precio.toLocaleString("es-AR")}` : "—",
      className: "w-32",
    },
    {
      key: "subtotal",
      header: "Subtotal",
      render: (i) => {
        const subtotal = (i.cantidad ?? 0) * (i.precio ?? 0);
        return subtotal > 0 ? `$${subtotal.toLocaleString("es-AR")}` : "—";
      },
      className: "w-32",
    },
    {
      key: "actions",
      header: "",
      render: (i) => (
        <div className="flex gap-1 justify-end">
          <Button size="icon" variant="ghost" onClick={() => openEdit(i)} aria-label="Editar">
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="icon"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(i.idItem!)}
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
        title="Items"
        description="Gestión de ítems de presupuestos"
        actions={
          <Button onClick={openCreate}>
            <Plus className="h-4 w-4" />
            Nuevo item
          </Button>
        }
      />

      <DataTable
        data={items}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(i) => i.idItem!}
        emptyMessage="No hay items registrados"
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={editing ? "Editar item" : "Nuevo item"}>
              <FormField label="Concepto">
                <Select value={form.conceptoId} onValueChange={(v) => setForm({ ...form, conceptoId: v })}>
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccionar concepto" />
                  </SelectTrigger>
                  <SelectContent>
                    {conceptos.map((c) => (
                      <SelectItem key={c.idConcepto} value={c.idConcepto!.toString()}>
                        {c.nombre}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <div className="grid grid-cols-2 gap-3">
                <FormField label="Cantidad">
                  <Input
                    type="number"
                    value={form.cantidad}
                    onChange={(e) => setForm({ ...form, cantidad: e.target.value })}
                    placeholder="1"
                  />
                </FormField>
                <FormField label="Precio ($)">
                  <Input
                    type="number"
                    value={form.precio}
                    onChange={(e) => setForm({ ...form, precio: e.target.value })}
                    placeholder="0.00"
                  />
                </FormField>
              </div>
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
