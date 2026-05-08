"use client";
import { useState } from "react";
import { Plus } from "lucide-react";
import { toast } from "sonner";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useQuery } from "@tanstack/react-query";
import { apiGet, apiPost } from "@/lib/api-client";
import type { EstadoDeGestion } from "@/types";

export default function EstadosGestionPage() {
  const { data = [], isLoading, refetch } = useQuery({
    queryKey: ["estados-gestion"],
    queryFn: () => apiGet<EstadoDeGestion[]>("/catalogos/estados-gestion"),
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [nombre, setNombre] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [saving, setSaving] = useState(false);

  function openCreate() {
    setNombre("");
    setDescripcion("");
    setModalOpen(true);
  }

  async function handleSave() {
    if (!nombre.trim()) {
      toast.error("El nombre es requerido");
      return;
    }
    setSaving(true);
    try {
      await apiPost("/catalogos/estados-gestion", { nombre: nombre.trim(), descripcion: descripcion.trim() });
      toast.success("Estado creado");
      setModalOpen(false);
      refetch();
    } catch {
      toast.error("Error al crear el estado");
    } finally {
      setSaving(false);
    }
  }

  const columns: Column<EstadoDeGestion>[] = [
    { key: "id", header: "ID", render: (e) => e.idEstadoDeGestion, className: "w-12" },
    { key: "nombre", header: "Nombre", render: (e) => <span className="font-medium">{e.nombre}</span> },
    { key: "desc", header: "Descripción", render: (e) => e.descripcion ?? "—" },
  ];

  return (
    <div>
      <AppHeader
        title="Estados de Gestión"
        description="CU67 — Estados del ciclo de vida de las gestiones notariales"
        actions={
          <Button onClick={openCreate} data-testid="btn-nuevo-estado">
            <Plus className="h-4 w-4" />
            Nuevo Estado
          </Button>
        }
      />
      <DataTable data={data} columns={columns} isLoading={isLoading} keyExtractor={(e) => e.idEstadoDeGestion!} emptyMessage="No hay estados de gestión" />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title="Nuevo Estado de Gestión">
              <FormField label="Nombre" required>
                <Input
                  value={nombre}
                  onChange={(e) => setNombre(e.target.value)}
                  placeholder="Ej: En trámite"
                  aria-label="Nombre"
                />
              </FormField>
              <FormField label="Descripción">
                <Input
                  value={descripcion}
                  onChange={(e) => setDescripcion(e.target.value)}
                  placeholder="Descripción opcional"
                  aria-label="Descripción"
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                Cancelar
              </Button>
              <Button onClick={handleSave} disabled={saving}>
                Crear
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>
    </div>
  );
}
