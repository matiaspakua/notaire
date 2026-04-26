"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, UserCheck } from "lucide-react";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  useSuplencias,
  useCreateSuplencia,
  useUpdateSuplencia,
  useDeleteSuplencia,
} from "@/hooks/useSuplencias";
import { formatDate } from "@/lib/utils";
import type { Suplencia } from "@/types";

const EMPTY: Partial<Suplencia> = {
  desde: "",
  hasta: "",
};

function personaName(p: Suplencia["escribano"]): string {
  if (!p) return "—";
  return [p.nombre, p.apellido].filter(Boolean).join(" ") || `#${p.idPersona}`;
}

export default function SuplenciasPage() {
  const { data: suplencias = [], isLoading } = useSuplencias();
  const createMutation = useCreateSuplencia();
  const updateMutation = useUpdateSuplencia();
  const deleteMutation = useDeleteSuplencia();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Suplencia>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  // IDs for relationship fields
  const [escribanoId, setEscribanoId] = useState("");
  const [suplenteId, setSuplenteId] = useState("");

  function openCreate() {
    setEditing(EMPTY);
    setEscribanoId("");
    setSuplenteId("");
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(s: Suplencia) {
    setEditing(s);
    setEscribanoId(s.escribano?.idPersona?.toString() ?? "");
    setSuplenteId(s.suplente?.idPersona?.toString() ?? "");
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    const payload: Partial<Suplencia> = {
      ...editing,
      escribano: escribanoId ? { idPersona: Number(escribanoId) } : undefined,
      suplente: suplenteId ? { idPersona: Number(suplenteId) } : undefined,
    };
    try {
      if (isEditMode && editing.idSuplencia) {
        await updateMutation.mutateAsync({ id: editing.idSuplencia, data: payload });
        toast.success("Suplencia actualizada");
      } else {
        await createMutation.mutateAsync(payload);
        toast.success("Suplencia registrada");
      }
      setModalOpen(false);
    } catch {
      toast.error("Error al guardar la suplencia");
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Suplencia eliminada");
    } catch {
      toast.error("Error al eliminar");
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<Suplencia>[] = [
    {
      key: "id",
      header: "ID",
      render: (s) => (
        <span className="text-xs text-muted-foreground">{s.idSuplencia}</span>
      ),
      className: "w-12",
    },
    {
      key: "escribano",
      header: "Escribano",
      render: (s) => (
        <div className="flex items-center gap-2">
          <UserCheck className="h-4 w-4 text-muted-foreground" />
          <span className="font-medium">{personaName(s.escribano)}</span>
        </div>
      ),
    },
    {
      key: "suplente",
      header: "Suplente",
      render: (s) => personaName(s.suplente),
    },
    {
      key: "desde",
      header: "Desde",
      render: (s) => formatDate(s.desde),
    },
    {
      key: "hasta",
      header: "Hasta",
      render: (s) => formatDate(s.hasta),
    },
    {
      key: "actions",
      header: "",
      className: "w-24",
      render: (s) => (
        <div className="flex gap-2 justify-end">
          <Button
            size="sm"
            variant="ghost"
            onClick={() => openEdit(s)}
            aria-label="Editar"
          >
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(s.idSuplencia!)}
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
        title="Suplencias"
        description="CU12, CU42, CU59 — Registrar y consultar suplencias de escribano"
        actions={
          <Button onClick={openCreate} data-testid="btn-nueva-suplencia">
            <Plus className="h-4 w-4" />
            Nueva suplencia
          </Button>
        }
      />

      <DataTable
        data={suplencias}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(s) => s.idSuplencia!}
        emptyMessage="No hay suplencias registradas"
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {isEditMode ? "Editar suplencia" : "Nueva suplencia"}
            </DialogTitle>
          </DialogHeader>
          <div className="space-y-4 pt-2">
            <div className="space-y-1">
              <Label>ID Escribano (Persona)</Label>
              <Input
                type="number"
                value={escribanoId}
                onChange={(e) => setEscribanoId(e.target.value)}
                placeholder="ID del escribano titular"
                data-testid="input-escribano-id"
              />
            </div>
            <div className="space-y-1">
              <Label>ID Suplente (Persona)</Label>
              <Input
                type="number"
                value={suplenteId}
                onChange={(e) => setSuplenteId(e.target.value)}
                placeholder="ID del suplente"
                data-testid="input-suplente-id"
              />
            </div>
            <div className="space-y-1">
              <Label>Desde</Label>
              <Input
                type="date"
                value={editing.desde ?? ""}
                onChange={(e) => setEditing({ ...editing, desde: e.target.value })}
                data-testid="input-desde"
              />
            </div>
            <div className="space-y-1">
              <Label>Hasta</Label>
              <Input
                type="date"
                value={editing.hasta ?? ""}
                onChange={(e) => setEditing({ ...editing, hasta: e.target.value })}
                data-testid="input-hasta"
              />
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <Button variant="outline" onClick={() => setModalOpen(false)}>
                Cancelar
              </Button>
              <Button
                onClick={handleSave}
                disabled={
                  createMutation.isPending || updateMutation.isPending
                }
                data-testid="btn-guardar-suplencia"
              >
                {isEditMode ? "Actualizar" : "Registrar"}
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
