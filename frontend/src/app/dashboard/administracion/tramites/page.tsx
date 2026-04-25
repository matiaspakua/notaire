"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, CheckCircle, XCircle } from "lucide-react";
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
  useTiposTramite,
  useCreateTipoTramite,
  useUpdateTipoTramite,
  useDeleteTipoTramite,
} from "@/hooks/useTiposTramite";
import type { TipoDeTramite } from "@/types";

/** CU26, CU57, CU64 — Gestionar tipos de trámite */
const EMPTY: Partial<TipoDeTramite> = {
  nombre: "",
  descripcion: "",
  seArchiva: false,
  seInscribe: false,
};

export default function TramitesPage() {
  const { data: tipos = [], isLoading } = useTiposTramite();
  const createMutation = useCreateTipoTramite();
  const updateMutation = useUpdateTipoTramite();
  const deleteMutation = useDeleteTipoTramite();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<TipoDeTramite>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  function openCreate() {
    setEditing(EMPTY);
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(t: TipoDeTramite) {
    setEditing(t);
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    if (!editing.nombre?.trim()) {
      toast.error("El nombre es obligatorio");
      return;
    }
    try {
      if (isEditMode && editing.idTipoTramite) {
        await updateMutation.mutateAsync({
          id: editing.idTipoTramite,
          data: editing,
        });
        toast.success("Tipo de trámite actualizado");
      } else {
        await createMutation.mutateAsync(editing);
        toast.success("Tipo de trámite creado");
      }
      setModalOpen(false);
    } catch {
      toast.error("Error al guardar el tipo de trámite");
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Tipo de trámite eliminado");
    } catch {
      toast.error("Error al eliminar — puede tener trámites asociados");
    } finally {
      setDeleteId(null);
    }
  }

  const BoolIcon = ({ value }: { value?: boolean }) =>
    value ? (
      <CheckCircle className="h-4 w-4 text-green-600" />
    ) : (
      <XCircle className="h-4 w-4 text-muted-foreground" />
    );

  const columns: Column<TipoDeTramite>[] = [
    {
      key: "id",
      header: "ID",
      render: (t) => (
        <span className="text-xs text-muted-foreground">{t.idTipoTramite}</span>
      ),
      className: "w-16",
    },
    {
      key: "nombre",
      header: "Nombre",
      render: (t) => <span className="font-medium">{t.nombre ?? "—"}</span>,
    },
    {
      key: "descripcion",
      header: "Descripción",
      render: (t) => (
        <span className="text-sm text-muted-foreground">
          {t.descripcion ?? "—"}
        </span>
      ),
    },
    {
      key: "archiva",
      header: "Se archiva",
      className: "w-28 text-center",
      render: (t) => <BoolIcon value={t.seArchiva} />,
    },
    {
      key: "inscribe",
      header: "Se inscribe",
      className: "w-28 text-center",
      render: (t) => <BoolIcon value={t.seInscribe} />,
    },
    {
      key: "actions",
      header: "",
      className: "w-24",
      render: (t) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(t)}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(t.idTipoTramite!)}
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
        title="Tipos de Trámite"
        description="CU26, CU57, CU64 — Administrar el catálogo de tipos de trámite"
        actions={
          <Button onClick={openCreate}>
            <Plus className="h-4 w-4" />
            Nuevo tipo
          </Button>
        }
      />

      <DataTable
        data={tipos}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(t) => t.idTipoTramite!}
        emptyMessage="No hay tipos de trámite registrados"
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {isEditMode ? "Editar tipo de trámite" : "Nuevo tipo de trámite"}
            </DialogTitle>
          </DialogHeader>
          <div className="space-y-3 pt-2">
            <div className="space-y-1">
              <Label>Nombre *</Label>
              <Input
                value={editing.nombre ?? ""}
                onChange={(e) =>
                  setEditing({ ...editing, nombre: e.target.value })
                }
                placeholder="Ej: Compraventa, Hipoteca, Donación…"
              />
            </div>
            <div className="space-y-1">
              <Label>Descripción</Label>
              <Input
                value={editing.descripcion ?? ""}
                onChange={(e) =>
                  setEditing({ ...editing, descripcion: e.target.value })
                }
                placeholder="Descripción opcional"
              />
            </div>
            <div className="flex gap-6 pt-1">
              <label className="flex items-center gap-2 cursor-pointer text-sm">
                <input
                  type="checkbox"
                  checked={editing.seArchiva ?? false}
                  onChange={(e) =>
                    setEditing({ ...editing, seArchiva: e.target.checked })
                  }
                  className="rounded"
                />
                Se archiva
              </label>
              <label className="flex items-center gap-2 cursor-pointer text-sm">
                <input
                  type="checkbox"
                  checked={editing.seInscribe ?? false}
                  onChange={(e) =>
                    setEditing({ ...editing, seInscribe: e.target.checked })
                  }
                  className="rounded"
                />
                Se inscribe
              </label>
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
