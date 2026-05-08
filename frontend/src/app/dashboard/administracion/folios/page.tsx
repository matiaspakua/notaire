"use client";
import { useState } from "react";
import { toast } from "sonner";
import { NotaireIcon } from "@/components/ui/notaire-icon";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useQuery } from "@tanstack/react-query";
import { apiGet, apiPost } from "@/lib/api-client";
import type { Folio } from "@/types";

export default function FoliosAdminPage() {
  const { data = [], isLoading, refetch } = useQuery({
    queryKey: ["folios"],
    queryFn: () => apiGet<Folio[]>("/folios"),
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [numero, setNumero] = useState("");
  const [anio, setAnio] = useState(new Date().getFullYear().toString());
  const [saving, setSaving] = useState(false);

  function openCreate() {
    setNumero("");
    setAnio(new Date().getFullYear().toString());
    setModalOpen(true);
  }

  async function handleSave() {
    if (!numero.trim()) {
      toast.error("El número es requerido");
      return;
    }
    setSaving(true);
    try {
      await apiPost("/folios", { numero: Number(numero), anio: Number(anio), disponible: true });
      toast.success("Folio creado");
      setModalOpen(false);
      refetch();
    } catch {
      toast.error("Error al crear el folio");
    } finally {
      setSaving(false);
    }
  }

  const columns: Column<Folio>[] = [
    { key: "id", header: "ID", render: (f) => f.idFolio, className: "w-12" },
    { key: "numero", header: "Número", render: (f) => <span className="font-medium">{f.numero}</span> },
    { key: "tipo", header: "Tipo", render: (f) => f.tipoDeFolio?.nombre ?? "—" },
    { key: "estado", header: "Estado", render: (f) => f.disponible ? <Badge variant="success">Disponible</Badge> : <Badge variant="secondary">En uso</Badge> },
  ];

  return (
    <div>
      <AppHeader
        title="Folios"
        description="CU28, CU40, CU58, CU63, CU68 — Gestión de folios notariales y su disponibilidad"
        actions={
          <Button onClick={openCreate} data-testid="btn-nuevo-folio">
            <NotaireIcon src="/icons/actions/agregar.png" alt="Agregar" size={16} className="mr-1" />
            Nuevo Folio
          </Button>
        }
      />
      <DataTable data={data} columns={columns} isLoading={isLoading} keyExtractor={(f) => f.idFolio!} emptyMessage="No hay folios" />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title="Nuevo Folio">
              <FormField label="Número" required>
                <Input
                  type="number"
                  value={numero}
                  onChange={(e) => setNumero(e.target.value)}
                  placeholder="Ej: 1001"
                  aria-label="Número"
                />
              </FormField>
              <FormField label="Año" required>
                <Input
                  type="number"
                  value={anio}
                  onChange={(e) => setAnio(e.target.value)}
                  placeholder={new Date().getFullYear().toString()}
                  aria-label="Año"
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                <NotaireIcon src="/icons/actions/cerrar.png" alt="Cancelar" size={16} className="mr-1" />
                Cancelar
              </Button>
              <Button onClick={handleSave} disabled={saving}>
                <NotaireIcon src="/icons/actions/guardar.png" alt="Guardar" size={16} className="mr-1 brightness-0 invert" />
                Guardar
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>
    </div>
  );
}
