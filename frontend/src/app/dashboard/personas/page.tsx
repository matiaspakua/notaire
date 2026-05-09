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
import { Badge } from "@/components/ui/badge";
import { FormContainer, FormSection, FormField, FormActions, CheckboxField } from "@/theme/form-patterns";
import {
  usePersonas,
  useCreatePersona,
  useUpdatePersona,
  useDeletePersona,
} from "@/hooks/usePersonas";
import { fullName } from "@/lib/utils";
import type { Persona } from "@/types";

const EMPTY: Partial<Persona> = {
  nombre: "",
  apellido: "",
  dni: "",
  email: "",
  telefono: "",
  domicilio: "",
  esCliente: false,
};

export default function PersonasPage() {
  const { data: personas = [], isLoading } = usePersonas();
  const createMutation = useCreatePersona();
  const updateMutation = useUpdatePersona();
  const deleteMutation = useDeletePersona();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Persona>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  function openCreate() {
    setEditing(EMPTY);
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(p: Persona) {
    setEditing(p);
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    try {
      if (isEditMode && editing.idPersona) {
        await updateMutation.mutateAsync({ id: editing.idPersona, data: editing });
        toast.success("Persona actualizada");
      } else {
        await createMutation.mutateAsync(editing);
        toast.success("Persona creada");
      }
      setModalOpen(false);
    } catch {
      toast.error("Error al guardar la persona");
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Persona eliminada");
    } catch {
      toast.error("Error al eliminar la persona");
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<Persona>[] = [
    {
      key: "id",
      header: "ID",
      render: (p) => <span className="text-xs text-muted-foreground">{p.idPersona}</span>,
      className: "w-12",
    },
    {
      key: "nombre",
      header: "Nombre",
      render: (p) => <span className="font-medium">{fullName(p)}</span>,
    },
    {
      key: "dni",
      header: "DNI / CUIL",
      render: (p) => p.dni ?? p.cuil ?? "—",
    },
    {
      key: "email",
      header: "Email",
      render: (p) => p.email ?? "—",
    },
    {
      key: "cliente",
      header: "Tipo",
      render: (p) =>
        p.esCliente ? (
          <Badge variant="success">Cliente</Badge>
        ) : (
          <Badge variant="secondary">Persona</Badge>
        ),
    },
    {
      key: "actions",
      header: "",
      render: (p) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(p)}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(p.idPersona!)}
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
        title="Personas y Clientes"
        description="Registrar y gestionar personas, clientes y escribanos del sistema"
        actions={
          <Button onClick={openCreate} data-testid="btn-nueva-persona">
            <Plus className="h-4 w-4" />
            Nueva persona
          </Button>
        }
      />

      <DataTable
        data={personas}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(p) => p.idPersona!}
        emptyMessage="No hay personas registradas"
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent className="max-w-md">
          <FormContainer>
            <FormSection title={isEditMode ? "Editar persona" : "Nueva persona"}>
              <div className="grid grid-cols-2 gap-3">
                <FormField label="Nombre" required>
                  <Input
                    value={editing.nombre ?? ""}
                    onChange={(e) => setEditing({ ...editing, nombre: e.target.value })}
                    data-testid="input-nombre"
                  />
                </FormField>
                <FormField label="Apellido" required>
                  <Input
                    value={editing.apellido ?? ""}
                    onChange={(e) => setEditing({ ...editing, apellido: e.target.value })}
                    data-testid="input-apellido"
                  />
                </FormField>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <FormField label="DNI">
                  <Input
                    value={editing.dni ?? ""}
                    onChange={(e) => setEditing({ ...editing, dni: e.target.value })}
                  />
                </FormField>
                <FormField label="CUIL">
                  <Input
                    value={editing.cuil ?? ""}
                    onChange={(e) => setEditing({ ...editing, cuil: e.target.value })}
                  />
                </FormField>
              </div>
              <FormField label="Email">
                <Input
                  type="email"
                  value={editing.email ?? ""}
                  onChange={(e) => setEditing({ ...editing, email: e.target.value })}
                />
              </FormField>
              <FormField label="Teléfono">
                <Input
                  value={editing.telefono ?? ""}
                  onChange={(e) => setEditing({ ...editing, telefono: e.target.value })}
                />
              </FormField>
              <FormField label="Domicilio">
                <Input
                  value={editing.domicilio ?? ""}
                  onChange={(e) => setEditing({ ...editing, domicilio: e.target.value })}
                />
              </FormField>
              <CheckboxField
                label="Es cliente"
                checked={editing.esCliente ?? false}
                onChange={(checked) => setEditing({ ...editing, esCliente: checked })}
              />
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                Cancelar
              </Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>
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
