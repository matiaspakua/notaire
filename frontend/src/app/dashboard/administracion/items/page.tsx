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
  useDescuentosYRecargos,
} from "@/hooks/useItems";
import { formatCurrency } from "@/lib/utils";
import type { Item, TipoItem } from "@/types";

const EMPTY: Partial<Item> = {
  name: "",
  value: undefined,
  type: "NORMAL",
};

const TIPO_LABELS: Record<TipoItem, string> = {
  NORMAL: "Normal",
  DESCUENTO: "Descuento",
  RECARGO: "Recargo",
};

export default function ItemsPage() {
  const { data: items = [], isLoading } = useItems();
  const createMutation = useCreateItem();
  const updateMutation = useUpdateItem();
  const deleteMutation = useDeleteItem();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Item>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);
  const [presupuestoId, setPresupuestoId] = useState("");
  const [reporteId, setReporteId] = useState("");
  const [reporteQuery, setReporteQuery] = useState<number | undefined>(undefined);
  const { data: descuentosRecargos = [], isFetching: reporteLoading } =
    useDescuentosYRecargos(reporteQuery);

  function openCreate() {
    setEditing(EMPTY);
    setPresupuestoId("");
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(item: Item) {
    setEditing(item);
    setPresupuestoId(item.fkIdBudget?.idBudget?.toString() ?? "");
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    const type = editing.type ?? "NORMAL";
    if (type !== "NORMAL" && !editing.reason?.trim()) {
      toast.error("El motivo es obligatorio para ítems de descuento o recargo");
      return;
    }
    const payload: Partial<Item> = {
      ...editing,
      type,
      fkIdBudget: presupuestoId ? { idBudget: Number(presupuestoId) } : undefined,
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
      key: "nombre",
      header: "Nombre",
      render: (i) => i.name ?? "—",
    },
    {
      key: "valor",
      header: "Valor",
      render: (i) => <span className="font-medium">{formatCurrency(i.value)}</span>,
    },
    {
      key: "presupuesto",
      header: "Presupuesto",
      render: (i) => i.fkIdBudget?.idBudget ? `#${i.fkIdBudget.idBudget}` : "—",
    },
    {
      key: "tipo",
      header: "Tipo",
      render: (i) => TIPO_LABELS[i.type ?? "NORMAL"],
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

      <FormContainer>
        <FormSection title="Descuentos y recargos por presupuesto">
          <div className="flex items-end gap-3">
            <FormField label="ID Presupuesto">
              <Input
                type="number"
                value={reporteId}
                onChange={(e) => setReporteId(e.target.value)}
                placeholder="ID del presupuesto"
                data-testid="input-reporte-presupuesto-id"
              />
            </FormField>
            <Button
              variant="secondary"
              onClick={() => setReporteQuery(reporteId ? Number(reporteId) : undefined)}
              data-testid="btn-consultar-reporte"
            >
              Consultar
            </Button>
          </div>
          {reporteQuery && (
            <DataTable
              data={descuentosRecargos}
              isLoading={reporteLoading}
              keyExtractor={(i) => i.idItem!}
              emptyMessage="Este presupuesto no tiene descuentos ni recargos"
              columns={[
                { key: "nombre", header: "Nombre", render: (i) => i.name ?? "—" },
                { key: "tipo", header: "Tipo", render: (i) => TIPO_LABELS[i.type ?? "NORMAL"] },
                { key: "motivo", header: "Motivo", render: (i) => i.reason ?? "—" },
                { key: "valor", header: "Valor", render: (i) => formatCurrency(i.value) },
              ]}
            />
          )}
        </FormSection>
      </FormContainer>

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? "Editar ítem" : "Nuevo ítem"}>
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
                <FormField label="Nombre">
                  <Input
                    value={editing.name ?? ""}
                    onChange={(e) => setEditing({ ...editing, name: e.target.value })}
                    data-testid="input-cantidad"
                  />
                </FormField>
                <FormField label="Valor ($)">
                  <Input
                    type="number"
                    step="0.01"
                    value={editing.value ?? ""}
                    onChange={(e) => setEditing({ ...editing, value: parseFloat(e.target.value) })}
                    data-testid="input-precio"
                  />
                </FormField>
              </div>
              <FormField label="Tipo de ítem">
                <Select
                  value={editing.type ?? "NORMAL"}
                  onValueChange={(v) => setEditing({ ...editing, type: v as TipoItem })}
                >
                  <SelectTrigger data-testid="select-tipo-item">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {(Object.keys(TIPO_LABELS) as TipoItem[]).map((tipo) => (
                      <SelectItem key={tipo} value={tipo}>
                        {TIPO_LABELS[tipo]}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              {editing.type && editing.type !== "NORMAL" && (
                <FormField label="Motivo" required>
                  <Input
                    value={editing.reason ?? ""}
                    onChange={(e) => setEditing({ ...editing, reason: e.target.value })}
                    placeholder="Ej: Descuento por pronto pago"
                    data-testid="input-motivo"
                  />
                </FormField>
              )}
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
