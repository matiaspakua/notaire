"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2 } from "lucide-react";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  useFolios,
  useTiposFolio,
  useCreateFolio,
  useUpdateFolio,
  useDeleteFolio,
} from "@/hooks/useFolios";
import type { Folio } from "@/types";

/** CU28, CU40, CU58, CU68 — Gestión de folios */
const EMPTY: Partial<Folio> = { numero: undefined, anio: undefined, estado: "DISPONIBLE" };

export default function FoliosPage() {
  const { data: folios = [], isLoading } = useFolios();
  const { data: tiposFolio = [] } = useTiposFolio();
  const createMutation = useCreateFolio();
  const updateMutation = useUpdateFolio();
  const deleteMutation = useDeleteFolio();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Folio>>(EMPTY);
  const [selectedTipoId, setSelectedTipoId] = useState<string>("");
  const [isEditMode, setIsEditMode] = useState(false);

  function openCreate() {
    setEditing(EMPTY);
    setSelectedTipoId("");
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(f: Folio) {
    setEditing(f);
    setSelectedTipoId(f.tipoDeFolio?.idTipoDeFolio?.toString() ?? "");
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    if (!editing.numero) {
      toast.error("El número de folio es obligatorio");
      return;
    }
    const payload: Partial<Folio> = {
      ...editing,
      tipoDeFolio: selectedTipoId
        ? { idTipoDeFolio: parseInt(selectedTipoId) }
        : undefined,
    };
    try {
      if (isEditMode && editing.idFolio) {
        await updateMutation.mutateAsync({ id: editing.idFolio, data: payload });
        toast.success("Folio actualizado");
      } else {
        await createMutation.mutateAsync(payload);
        toast.success("Folio creado");
      }
      setModalOpen(false);
    } catch {
      toast.error("Error al guardar el folio");
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Folio eliminado");
    } catch {
      toast.error("Error al eliminar — puede estar en uso");
    } finally {
      setDeleteId(null);
    }
  }

  const estadoVariant = (e?: string) => {
    if (e === "DISPONIBLE") return "default";
    if (e === "USADO") return "secondary";
    return "outline";
  };

  const columns: Column<Folio>[] = [
    {
      key: "id",
      header: "ID",
      render: (f) => (
        <span className="text-xs text-muted-foreground">{f.idFolio}</span>
      ),
      className: "w-16",
    },
    {
      key: "numero",
      header: "Número",
      render: (f) => <span className="font-mono font-medium">{f.numero}</span>,
    },
    {
      key: "anio",
      header: "Año",
      render: (f) => f.anio ?? "—",
    },
    {
      key: "tipo",
      header: "Tipo",
      render: (f) => f.tipoDeFolio?.nombre ?? "—",
    },
    {
      key: "estado",
      header: "Estado",
      render: (f) => (
        <Badge variant={estadoVariant(f.estado)}>{f.estado ?? "—"}</Badge>
      ),
    },
    {
      key: "actions",
      header: "",
      className: "w-24",
      render: (f) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(f)}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(f.idFolio!)}
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
        title="Folios"
        description="CU28, CU40, CU58, CU68 — Administrar los folios del protocolo notarial"
        actions={
          <Button onClick={openCreate}>
            <Plus className="h-4 w-4" />
            Nuevo folio
          </Button>
        }
      />

      <DataTable
        data={folios}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(f) => f.idFolio!}
        emptyMessage="No hay folios registrados"
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{isEditMode ? "Editar folio" : "Nuevo folio"}</DialogTitle>
          </DialogHeader>
          <div className="space-y-3 pt-2">
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <Label>Número *</Label>
                <Input
                  type="number"
                  value={editing.numero ?? ""}
                  onChange={(e) =>
                    setEditing({ ...editing, numero: parseInt(e.target.value) || undefined })
                  }
                  placeholder="Ej: 1001"
                />
              </div>
              <div className="space-y-1">
                <Label>Año</Label>
                <Input
                  type="number"
                  value={editing.anio ?? ""}
                  onChange={(e) =>
                    setEditing({ ...editing, anio: parseInt(e.target.value) || undefined })
                  }
                  placeholder={new Date().getFullYear().toString()}
                />
              </div>
            </div>

            <div className="space-y-1">
              <Label>Tipo de folio</Label>
              <select
                className="w-full border border-input rounded-md px-3 py-2 text-sm bg-background"
                value={selectedTipoId}
                onChange={(e) => setSelectedTipoId(e.target.value)}
              >
                <option value="">Sin tipo</option>
                {tiposFolio.map((t) => (
                  <option key={t.idTipoDeFolio} value={t.idTipoDeFolio}>
                    {t.nombre}
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-1">
              <Label>Estado</Label>
              <select
                className="w-full border border-input rounded-md px-3 py-2 text-sm bg-background"
                value={editing.estado ?? "DISPONIBLE"}
                onChange={(e) => setEditing({ ...editing, estado: e.target.value })}
              >
                <option value="DISPONIBLE">Disponible</option>
                <option value="USADO">Usado</option>
                <option value="ANULADO">Anulado</option>
              </select>
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <Button variant="outline" onClick={() => setModalOpen(false)}>
                Cancelar
              </Button>
              <Button
                onClick={handleSave}
                disabled={createMutation.isPending || updateMutation.isPending}
              >
                {isEditMode ? "Actualizar" : "Crear"}
              </Button>
            </div>
          </div>
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
