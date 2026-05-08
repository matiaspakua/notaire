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
import type { PlantillaPresupuesto, TipoDeTramite } from "@/types";

export default function PlantillasPage() {
  const { data = [], isLoading, refetch } = useQuery({
    queryKey: ["plantillas-presupuesto"],
    queryFn: () => apiGet<PlantillaPresupuesto[]>("/presupuestos/plantillas"),
  });

  const { data: tiposTramite = [] } = useQuery({
    queryKey: ["tipos-tramite"],
    queryFn: () => apiGet<TipoDeTramite[]>("/catalogos/tipos-tramite"),
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [nombre, setNombre] = useState("");
  const [tipoTramiteId, setTipoTramiteId] = useState("");
  const [saving, setSaving] = useState(false);

  function openCreate() {
    setNombre("");
    setTipoTramiteId("");
    setModalOpen(true);
  }

  async function handleSave() {
    if (!nombre.trim()) {
      toast.error("El nombre es requerido");
      return;
    }
    setSaving(true);
    try {
      await apiPost("/presupuestos/plantillas", { nombre: nombre.trim(), tipoTramiteId: tipoTramiteId ? Number(tipoTramiteId) : undefined });
      toast.success("Plantilla creada");
      setModalOpen(false);
      refetch();
    } catch {
      toast.error("Error al crear la plantilla");
    } finally {
      setSaving(false);
    }
  }

  const columns: Column<PlantillaPresupuesto>[] = [
    { key: "id", header: "ID", render: (p) => p.idPlantillaPresupuesto, className: "w-12" },
    { key: "nombre", header: "Nombre", render: (p) => <span className="font-medium">{p.nombre}</span> },
    { key: "items", header: "Items", render: (p) => p.itemList?.length ?? 0 },
  ];

  return (
    <div>
      <AppHeader
        title="Plantillas de Presupuesto"
        description="CU39, CU49, CU55 — Plantillas reutilizables para armar presupuestos"
        actions={
          <Button onClick={openCreate} data-testid="btn-nueva-plantilla">
            <Plus className="h-4 w-4" />
            Nueva Plantilla
          </Button>
        }
      />
      <DataTable data={data} columns={columns} isLoading={isLoading} keyExtractor={(p) => p.idPlantillaPresupuesto!} emptyMessage="No hay plantillas" />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title="Nueva Plantilla de Presupuesto">
              <FormField label="Nombre" required>
                <Input
                  value={nombre}
                  onChange={(e) => setNombre(e.target.value)}
                  placeholder="Ej: Compraventa estándar"
                  aria-label="Nombre"
                />
              </FormField>
              <FormField label="Tipo de Trámite">
                <select
                  value={tipoTramiteId}
                  onChange={(e) => setTipoTramiteId(e.target.value)}
                  aria-label="Tipo de Trámite"
                  className="flex h-12 w-full rounded-[12px] border border-border bg-white px-4 py-2.5 text-base shadow-sm focus:outline-none focus:ring-2 focus:ring-ring"
                >
                  <option value="">Seleccionar tipo de trámite</option>
                  {tiposTramite.map((t) => (
                    <option key={t.idTipoDeTramite} value={String(t.idTipoDeTramite)}>
                      {t.nombre}
                    </option>
                  ))}
                </select>
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
