"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, FileCheck, CheckCircle, XCircle } from "lucide-react";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
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
      tipoId: d.tipo?.idTipoDeDocumento?.toString() ?? "",
      fecha: d.fecha?.split("T")[0] ?? "",
      entregado: d.entregado ?? false,
    });
    setModalOpen(true);
  }

  async function handleSave() {
    const data: Partial<DocumentoPresentado> = {
      tipo: form.tipoId ? { idTipoDeDocumento: Number(form.tipoId) } as TipoDeDocumento : undefined,
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
        description="Registro de documentos entregados (CU72)"
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
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <FileCheck className="h-5 w-5 text-primary" />
              {editing ? "Editar documento" : "Nuevo documento"}
            </DialogTitle>
          </DialogHeader>
          <div className="space-y-4 pt-2">
            <div className="space-y-1.5">
              <Label>Tipo de Documento</Label>
              <Select value={form.tipoId} onValueChange={(v) => setForm({ ...form, tipoId: v })}>
                <SelectTrigger>
                  <SelectValue placeholder="Seleccionar tipo" />
                </SelectTrigger>
                <SelectContent>
                  {tiposDoc.map((t) => (
                    <SelectItem key={t.idTipoDeDocumento} value={t.idTipoDeDocumento!.toString()}>
                      {t.nombre}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label>Fecha</Label>
              <Input
                type="date"
                value={form.fecha}
                onChange={(e) => setForm({ ...form, fecha: e.target.value })}
              />
            </div>
            <div className="flex items-center gap-2 pt-2">
              <input
                type="checkbox"
                id="entregado"
                checked={form.entregado}
                onChange={(e) => setForm({ ...form, entregado: e.target.checked })}
                className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
              />
              <Label htmlFor="entregado" className="cursor-pointer">Documento entregado</Label>
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <Button variant="outline" onClick={() => setModalOpen(false)}>
                Cancelar
              </Button>
              <Button
                onClick={handleSave}
                disabled={createMutation.isPending || updateMutation.isPending}
              >
                {editing ? "Actualizar" : "Crear"}
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
