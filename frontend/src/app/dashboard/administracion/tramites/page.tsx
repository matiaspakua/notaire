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
import type { TipoDeTramite } from "@/types";

export default function TramitesPage() {
  const { data = [], isLoading, refetch } = useQuery({
    queryKey: ["tipos-tramite"],
    queryFn: () => apiGet<TipoDeTramite[]>("/catalogos/tipos-tramite"),
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [nombre, setNombre] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [seArchiva, setSeArchiva] = useState(false);
  const [seInscribe, setSeInscribe] = useState(false);
  const [saving, setSaving] = useState(false);

  function openCreate() {
    setNombre("");
    setDescripcion("");
    setSeArchiva(false);
    setSeInscribe(false);
    setModalOpen(true);
  }

  async function handleSave() {
    if (!nombre.trim()) {
      toast.error("El nombre es requerido");
      return;
    }
    setSaving(true);
    try {
      await apiPost("/catalogos/tipos-tramite", { nombre: nombre.trim(), descripcion: descripcion.trim(), seArchiva, seInscribe });
      toast.success("Tipo de trámite creado");
      setModalOpen(false);
      refetch();
    } catch {
      toast.error("Error al crear el tipo de trámite");
    } finally {
      setSaving(false);
    }
  }

  const columns: Column<TipoDeTramite>[] = [
    { key: "id", header: "ID", render: (t) => t.idTipoDeTramite, className: "w-12" },
    { key: "nombre", header: "Nombre", render: (t) => <span className="font-medium">{t.nombre}</span> },
    { key: "desc", header: "Descripción", render: (t) => t.descripcion ?? "—" },
  ];

  return (
    <div>
      <AppHeader
        title="Tipos de Trámite"
        description="CU26, CU57, CU64 — Catálogo de tipos de trámite disponibles"
        actions={
          <Button onClick={openCreate} data-testid="btn-nuevo-tipo-tramite">
            <Plus className="h-4 w-4" />
            Nuevo Tipo
          </Button>
        }
      />
      <DataTable data={data} columns={columns} isLoading={isLoading} keyExtractor={(t) => t.idTipoDeTramite!} emptyMessage="No hay tipos de trámite" />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title="Nuevo Tipo de Trámite">
              <FormField label="Nombre" required>
                <Input
                  value={nombre}
                  onChange={(e) => setNombre(e.target.value)}
                  placeholder="Ej: Compraventa"
                  aria-label="Nombre"
                />
              </FormField>
              <FormField label="Descripción">
                <Input
                  value={descripcion}
                  onChange={(e) => setDescripcion(e.target.value)}
                  placeholder="Descripción del tipo de trámite"
                  aria-label="Descripción"
                />
              </FormField>
              <FormField label="Se archiva">
                <input
                  type="checkbox"
                  id="se-archiva"
                  checked={seArchiva}
                  onChange={(e) => setSeArchiva(e.target.checked)}
                  aria-label="Se archiva"
                />
              </FormField>
              <FormField label="Se inscribe">
                <input
                  type="checkbox"
                  id="se-inscribe"
                  checked={seInscribe}
                  onChange={(e) => setSeInscribe(e.target.checked)}
                  aria-label="Se inscribe"
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
