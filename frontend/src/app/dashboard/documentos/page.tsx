"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, CheckCircle, XCircle } from "lucide-react";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { FormContainer, FormSection, FormField, FormActions, CheckboxField } from "@/theme/form-patterns";
import {
  useDocumentosPresentados,
  useCreateDocumentoPresentado,
  useUpdateDocumentoPresentado,
  useDeleteDocumentoPresentado,
} from "@/hooks/useDocumentosPresentados";
import { useTiposDocumento } from "@/hooks/useDocumentos";
import type { DocumentoPresentado, TipoDeDocumento } from "@/types";

export default function DocumentosPage() {
  const { data: documentos = [], isLoading } = useDocumentosPresentados();
  const { data: tiposDoc = [] } = useTiposDocumento();
  const createMutation = useCreateDocumentoPresentado();
  const updateMutation = useUpdateDocumentoPresentado();
  const deleteMutation = useDeleteDocumentoPresentado();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<DocumentoPresentado | null>(null);
  const [form, setForm] = useState({
    tipoId: "",
    fecha: "",
    entregado: false,
  });

  function openCreate() {
    setEditing(null);
    setForm({ tipoId: "", fecha: new Date().toISOString().split("T")[0], entregado: false });
    setModalOpen(true);
  }

  function openEdit(d: DocumentoPresentado) {
    setEditing(d);
    setForm({
      tipoId: d.tipo?.idTipoDocumento?.toString() ?? "",
      fecha: d.fecha?.split("T")[0] ?? "",
      entregado: d.entregado ?? false,
    });
    setModalOpen(true);
  }

  async function handleSave() {
    const data: Partial<DocumentoPresentado> = {
      tipo: form.tipoId ? { idTipoDocumento: Number(form.tipoId) } as TipoDeDocumento : undefined,
      fecha: form.fecha || undefined,
      entregado: form.entregado,
    };
    try {
      if (editing?.idDocumentoPresentado) {
        await updateMutation.mutateAsync({ id: editing.idDocumentoPresentado, data });
        toast.success("Documento actualizado");
      } else {
        await createMutation.mutateAsync(data);
        toast.success("Documento registrado");
      }
      setModalOpen(false);
    } catch {
      toast.error("Error al guardar el documento");
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Documento eliminado");
    } catch {
      toast.error("Error al eliminar el documento");
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<DocumentoPresentado>[] = [
    {
      key: "id",
      header: "ID",
      render: (d) => <span className="text-muted-foreground text-xs">{d.idDocumentoPresentado}</span>,
      className: "w-16",
    },
    {
      key: "tipo",
      header: "Tipo de Documento",
      render: (d) => <span className="font-medium">{d.tipo?.nombre ?? "—"}</span>,
    },
    {
      key: "fecha",
      header: "Fecha",
      render: (d) => d.fecha ? new Date(d.fecha).toLocaleDateString("es-AR") : "—",
    },
    {
      key: "entregado",
      header: "Entregado",
      render: (d) => (
        <span className="flex items-center gap-1.5">
          {d.entregado ? (
            <CheckCircle className="h-4 w-4 text-emerald-500" />
          ) : (
            <XCircle className="h-4 w-4 text-muted-foreground/40" />
          )}
          <span className="text-sm">{d.entregado ? "Sí" : "No"}</span>
        </span>
      ),
      className: "w-28",
    },
    {
      key: "actions",
      header: "",
      render: (d) => (
        <div className="flex gap-1 justify-end">
          <Button size="icon" variant="ghost" onClick={() => openEdit(d)} aria-label="Editar">
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="icon"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(d.idDocumentoPresentado!)}
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
        title="Documentos Presentados"
        description="Registro de documentos presentados en gestiones notariales"
        actions={
          <Button onClick={openCreate}>
            <Plus className="h-4 w-4" />
            Nuevo documento
          </Button>
        }
      />

      <DataTable
        data={documentos}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(d) => d.idDocumentoPresentado!}
        emptyMessage="No hay documentos registrados"
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={editing ? "Editar documento" : "Nuevo documento"}>
              <FormField label="Tipo de Documento">
                <Select value={form.tipoId} onValueChange={(v) => setForm({ ...form, tipoId: v })}>
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccionar tipo" />
                  </SelectTrigger>
                  <SelectContent>
                    {tiposDoc.map((t) => (
                      <SelectItem key={t.idTipoDocumento} value={t.idTipoDocumento!.toString()}>
                        {t.nombre}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label="Fecha">
                <Input
                  type="date"
                  value={form.fecha}
                  onChange={(e) => setForm({ ...form, fecha: e.target.value })}
                />
              </FormField>
              <CheckboxField
                label="Documento entregado"
                checked={form.entregado}
                onChange={(v) => setForm({ ...form, entregado: v })}
              />
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                Cancelar
              </Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>
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
