"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, UserCheck } from "lucide-react";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import {
  useSuplencias,
  useCreateSuplencia,
  useUpdateSuplencia,
  useDeleteSuplencia,
} from "@/hooks/useSuplencias";
import { formatDate } from "@/lib/utils";
import type { Suplencia } from "@/types";

const EMPTY: Partial<Suplencia> = {
  fechaInicio: "",
  fechaFin: "",
};

function personaName(p: Suplencia["fkIdSuplantado"]): string {
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
    setEscribanoId(s.fkIdSuplantado?.idPersona?.toString() ?? "");
    setSuplenteId(s.fkIdSuplente?.idPersona?.toString() ?? "");
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    const payload: Partial<Suplencia> = {
      fechaInicio: editing.fechaInicio,
      fechaFin: editing.fechaFin,
      fkIdSuplantado: escribanoId ? { idPersona: Number(escribanoId) } : undefined,
      fkIdSuplente: suplenteId ? { idPersona: Number(suplenteId) } : undefined,
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
      render: (s) => <span className="text-xs text-muted-foreground">{s.idSuplencia}</span>,
      className: "w-12",
    },
    {
      key: "escribano",
      header: "Escribano",
      render: (s) => (
        <div className="flex items-center gap-2">
          <UserCheck className="h-4 w-4 text-muted-foreground" />
          <span className="font-medium">{personaName(s.fkIdSuplantado)}</span>
        </div>
      ),
    },
    {
      key: "suplente",
      header: "Suplente",
      render: (s) => personaName(s.fkIdSuplente),
    },
    {
      key: "desde",
      header: "Desde",
      render: (s) => formatDate(s.fechaInicio),
    },
    {
      key: "hasta",
      header: "Hasta",
      render: (s) => formatDate(s.fechaFin),
    },
    {
      key: "actions",
      header: "",
      className: "w-24",
      render: (s) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(s)} aria-label="Editar">
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
        description="Registrar y consultar suplencias de escribano"
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
          <FormContainer>
            <FormSection title={isEditMode ? "Editar suplencia" : "Nueva suplencia"}>
              <div className="grid grid-cols-2 gap-3">
                <FormField label="ID Escribano" helperText="ID de la persona">
                  <Input
                    type="number"
                    value={escribanoId}
                    onChange={(e) => setEscribanoId(e.target.value)}
                    placeholder="ID del escribano"
                    data-testid="input-escribano-id"
                  />
                </FormField>
                <FormField label="ID Suplente" helperText="ID de la persona">
                  <Input
                    type="number"
                    value={suplenteId}
                    onChange={(e) => setSuplenteId(e.target.value)}
                    placeholder="ID del suplente"
                    data-testid="input-suplente-id"
                  />
                </FormField>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <FormField label="Desde">
                  <Input
                    type="date"
                    value={editing.fechaInicio ?? ""}
                    onChange={(e) => setEditing({ ...editing, fechaInicio: e.target.value })}
                    data-testid="input-desde"
                  />
                </FormField>
                <FormField label="Hasta">
                  <Input
                    type="date"
                    value={editing.fechaFin ?? ""}
                    onChange={(e) => setEditing({ ...editing, fechaFin: e.target.value })}
                    data-testid="input-hasta"
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
                data-testid="btn-guardar-suplencia"
              >
                {isEditMode ? "Actualizar" : "Registrar"}
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
