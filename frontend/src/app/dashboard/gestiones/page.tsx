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
  useGestiones,
  useCreateGestion,
  useUpdateGestion,
  useDeleteGestion,
} from "@/hooks/useGestiones";
import type { GestionDeEscritura } from "@/types";

export default function GestionesPage() {
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
        toast.success("Gestión actualizada");
      } else {
        await createMutation.mutateAsync(data);
        toast.success("Gestión creada");
      }
      setModalOpen(false);
    } catch {
      toast.error("Error al guardar la gestión");
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Gestión eliminada");
    } catch {
      toast.error("Error al eliminar la gestión");
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<GestionDeEscritura>[] = [
    {
      key: "id",
      header: "ID",
      render: (g) => <span className="text-muted-foreground text-xs">{g.idGestion}</span>,
      className: "w-16",
    },
    {
      key: "numero",
      header: "Número",
      render: (g) => <span className="font-medium">{g.numero ?? "—"}</span>,
    },
    {
      key: "tramites",
      header: "Trámites",
      render: (g) => g.tramiteList?.length ?? 0,
    },
    {
      key: "actions",
      header: "",
      render: (g) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(g)} aria-label="Editar">
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(g.idGestion!)}
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
        title="Gestiones"
        description="Gestión de trámites y escrituras (CU02, CU13–CU16, CU19, CU53)"
        actions={
          <Button onClick={openCreate} data-testid="btn-nueva-gestion">
            <Plus className="h-4 w-4" />
            Nueva gestión
          </Button>
        }
      />

      <DataTable
        data={gestiones}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(g) => g.idGestion!}
        emptyMessage="No hay gestiones registradas"
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={editing ? "Editar gestión" : "Nueva gestión"}>
              <FormField label="Número de gestión" required>
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
                Cancelar
              </Button>
              <Button
                onClick={handleSave}
                disabled={createMutation.isPending || updateMutation.isPending}
                data-testid="btn-guardar-gestion"
              >
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
