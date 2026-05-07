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
  useEscrituras,
  useCreateEscritura,
  useUpdateEscritura,
  useDeleteEscritura,
} from "@/hooks/useEscrituras";
import { formatDate } from "@/lib/utils";
import type { Escritura } from "@/types";

const EMPTY: Partial<Escritura> = { numero: undefined, fecha: "" };

export default function EscriturasPage() {
  const { data: escrituras = [], isLoading } = useEscrituras();
  const createMutation = useCreateEscritura();
  const updateMutation = useUpdateEscritura();
  const deleteMutation = useDeleteEscritura();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Escritura>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  function openCreate() { setEditing(EMPTY); setIsEditMode(false); setModalOpen(true); }
  function openEdit(e: Escritura) { setEditing(e); setIsEditMode(true); setModalOpen(true); }

  async function handleSave() {
    try {
      if (isEditMode && editing.idEscritura) {
        await updateMutation.mutateAsync({ id: editing.idEscritura, data: editing });
        toast.success("Escritura actualizada");
      } else {
        await createMutation.mutateAsync(editing);
        toast.success("Escritura creada");
      }
      setModalOpen(false);
    } catch { toast.error("Error al guardar"); }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Escritura eliminada");
    } catch { toast.error("Error al eliminar"); }
    finally { setDeleteId(null); }
  }

  const columns: Column<Escritura>[] = [
    { key: "id", header: "ID", render: (e) => <span className="text-xs text-muted-foreground">{e.idEscritura}</span>, className: "w-12" },
    { key: "numero", header: "Número", render: (e) => <span className="font-medium">{e.numero ?? "—"}</span> },
    { key: "fecha", header: "Fecha", render: (e) => formatDate(e.fecha) },
    { key: "folio", header: "Folio", render: (e) => e.folio?.numero ?? "—" },
    {
      key: "actions", header: "", className: "w-24",
      render: (e) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(e)}><Pencil className="h-4 w-4" /></Button>
          <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive" onClick={() => setDeleteId(e.idEscritura!)}><Trash2 className="h-4 w-4" /></Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title="Escrituras"
        description="CU05, CU06, CU07, CU08, CU52, CU62 — Protocolo de escrituras"
        actions={<Button onClick={openCreate}><Plus className="h-4 w-4" />Nueva escritura</Button>}
      />
      <DataTable data={escrituras} columns={columns} isLoading={isLoading} keyExtractor={(e) => e.idEscritura!} emptyMessage="No hay escrituras" />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? "Editar escritura" : "Nueva escritura"}>
              <FormField label="Número" required>
                <Input type="number" value={editing.numero ?? ""} onChange={(e) => setEditing({ ...editing, numero: Number(e.target.value) })} />
              </FormField>
              <FormField label="Fecha" required>
                <Input type="date" value={editing.fecha ?? ""} onChange={(e) => setEditing({ ...editing, fecha: e.target.value })} />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>{isEditMode ? "Actualizar" : "Crear"}</Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={!!deleteId} onOpenChange={(v) => !v && setDeleteId(null)} onConfirm={handleDelete} loading={deleteMutation.isPending} />
    </div>
  );
}
