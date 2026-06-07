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
import { useItems, useCreateItem, useUpdateItem, useDeleteItem } from "@/hooks/useItems";
import { useConceptos } from "@/hooks/useConceptos";
import { formatCurrency } from "@/lib/utils";
import type { Item } from "@/types";

const EMPTY: Partial<Item> = {
  cantidad: 1,
  precio: undefined,
};

export default function ItemsPage() {
  const { data: items = [], isLoading } = useItems();
  const { data: conceptos = [] } = useConceptos();
  const createMutation = useCreateItem();
  const updateMutation = useUpdateItem();
  const deleteMutation = useDeleteItem();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Item>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);
  const [conceptoId, setConceptoId] = useState("");
  const [presupuestoId, setPresupuestoId] = useState("");

  function openCreate() {
    setEditing(EMPTY);
    setConceptoId("");
    setPresupuestoId("");
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(item: Item) {
    setEditing(item);
    setConceptoId(item.concepto?.idConcepto?.toString() ?? "");
    setPresupuestoId(item.presupuesto?.idPresupuesto?.toString() ?? "");
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    const payload: Partial<Item> = {
      ...editing,
      concepto: conceptoId ? { idConcepto: Number(conceptoId) } : undefined,
      presupuesto: presupuestoId ? { idPresupuesto: Number(presupuestoId) } : undefined,
    };
    try {
      if (isEditMode && editing.idItem) {
        await updateMutation.mutateAsync({ id: editing.idItem, data: payload });
        toast.success("Ítem actualizado");
      } else {
        await createMutation.mutateAsync(payload);
        toast.success("Ítem creado");
      }
      setModalOpen(false);
    } catch {
      toast.error("Error al guardar el ítem");
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Ítem eliminado");
    } catch {
      toast.error("Error al eliminar");
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<Item>[] = [
    {
      key: "id",
      header: "ID",
      render: (i) => <span className="text-xs text-muted-foreground">{i.idItem}</span>,
      className: "w-12",
    },
    {
      key: "concepto",
      header: "Concepto",
      render: (i) => i.concepto?.nombre ?? `#${i.concepto?.idConcepto ?? "—"}`,
    },
    {
      key: "cantidad",
      header: "Cantidad",
      render: (i) => i.cantidad ?? 1,
    },
    {
      key: "precio",
      header: "Precio",
      render: (i) => <span className="font-medium">{formatCurrency(i.precio)}</span>,
    },
    {
      key: "subtotal",
      header: "Subtotal",
      render: (i) => <span className="font-semibold">{formatCurrency((i.precio ?? 0) * (i.cantidad ?? 1))}</span>,
    },
    {
      key: "presupuesto",
      header: "Presupuesto",
      render: (i) => i.presupuesto?.idPresupuesto ? `#${i.presupuesto.idPresupuesto}` : "—",
    },
    {
      key: "actions",
      header: "",
      className: "w-24",
      render: (i) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(i)} aria-label="Editar">
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(i.idItem!)}
            aria-label="Eliminar"
          >
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title="Ítems de Presupuesto"
        description="Gestionar ítems y conceptos en presupuestos"
        actions={
          <Button onClick={openCreate} data-testid="btn-nuevo-item">
            <Plus className="h-4 w-4" />
            Nuevo ítem
          </Button>
        }
      />

      <DataTable
        data={items}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(i) => i.idItem!}
        emptyMessage="No hay ítems registrados"
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? "Editar ítem" : "Nuevo ítem"}>
              <FormField label="Concepto">
                <Select value={conceptoId} onValueChange={setConceptoId}>
                  <SelectTrigger data-testid="select-concepto">
                    <SelectValue
                      placeholder="Seleccionar concepto..."
                      label={conceptoId ? conceptos.find((c) => c.idConcepto?.toString() === conceptoId)?.nombre : undefined}
                    />
                  </SelectTrigger>
                  <SelectContent>
                    {conceptos.map((c) => (
                      <SelectItem key={c.idConcepto} value={c.idConcepto!.toString()}>
                        {c.nombre} {c.valor ? `($${c.valor})` : ""}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label="ID Presupuesto">
                <Input
                  type="number"
                  value={presupuestoId}
                  onChange={(e) => setPresupuestoId(e.target.value)}
                  placeholder="ID del presupuesto"
                  data-testid="input-presupuesto-id"
                />
              </FormField>
              <div className="grid grid-cols-2 gap-3">
                <FormField label="Cantidad">
                  <Input
                    type="number"
                    min="1"
                    value={editing.cantidad ?? 1}
                    onChange={(e) => setEditing({ ...editing, cantidad: Number(e.target.value) })}
                    data-testid="input-cantidad"
                  />
                </FormField>
                <FormField label="Precio ($)">
                  <Input
                    type="number"
                    step="0.01"
                    value={editing.precio ?? ""}
                    onChange={(e) => setEditing({ ...editing, precio: parseFloat(e.target.value) })}
                    data-testid="input-precio"
                  />
                </FormField>
              </div>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                Cancelar
              </Button>
              <Button
                onClick={handleSave}
                disabled={createMutation.isPending || updateMutation.isPending}
                data-testid="btn-guardar-item"
              >
                {isEditMode ? "Actualizar" : "Crear"}
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
