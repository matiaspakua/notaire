"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, CheckCircle, XCircle } from "lucide-react";
import { useTranslations } from "next-intl";
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
  const t = useTranslations("documentos");
  const tc = useTranslations("common");

  const { data: documentos = [], isLoading } = useDocumentosPresentados();
  const { data: tiposDoc = [] } = useTiposDocumento();
  const createMutation = useCreateDocumentoPresentado();
  const updateMutation = useUpdateDocumentoPresentado();
  const deleteMutation = useDeleteDocumentoPresentado();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<DocumentoPresentado | null>(null);
  const [form, setForm] = useState({ tipoId: "", fecha: "", entregado: false });

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
    const data = {
      tipoId: form.tipoId ? Number(form.tipoId) : null,
      fecha: form.fecha || null,
      entregado: form.entregado,
    };
    try {
      if (editing?.idDocumentoPresentado) {
        await updateMutation.mutateAsync({ id: editing.idDocumentoPresentado, data });
        toast.success(t("updated"));
      } else {
        await createMutation.mutateAsync(data);
        toast.success(t("registered"));
      }
      setModalOpen(false);
    } catch {
      toast.error(t("errorSave"));
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success(t("deleted"));
    } catch {
      toast.error(t("errorDelete"));
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<DocumentoPresentado>[] = [
    {
      key: "id",
      header: tc("id"),
      render: (d) => <span className="text-muted-foreground text-xs">{d.idDocumentoPresentado}</span>,
      className: "w-16",
    },
    {
      key: "tipo",
      header: tc("type"),
      render: (d) => <span className="font-medium">{d.tipo?.nombre ?? "—"}</span>,
    },
    {
      key: "fecha",
      header: tc("date"),
      render: (d) => d.fecha ? new Date(d.fecha).toLocaleDateString("es-AR") : "—",
    },
    {
      key: "entregado",
      header: t("delivered"),
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
          <Button size="icon" variant="ghost" onClick={() => openEdit(d)} aria-label={tc("edit")}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="icon"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(d.idDocumentoPresentado!)}
            aria-label={tc("delete")}
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
        title={t("title")}
        actions={
          <Button onClick={openCreate}>
            <Plus className="h-4 w-4" />
            {t("newDocumento")}
          </Button>
        }
      />

      <DataTable
        data={documentos}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(d) => d.idDocumentoPresentado!}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={editing ? t("editDocumento") : t("newDocumento")}>
              <FormField label={tc("type")}>
                <Select value={form.tipoId} onValueChange={(v) => setForm({ ...form, tipoId: v })}>
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccionar tipo" />
                  </SelectTrigger>
                  <SelectContent>
                    {tiposDoc.map((tipo) => (
                      <SelectItem key={tipo.idTipoDocumento} value={tipo.idTipoDocumento!.toString()}>
                        {tipo.nombre}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label={tc("date")}>
                <Input
                  type="date"
                  value={form.fecha}
                  onChange={(e) => setForm({ ...form, fecha: e.target.value })}
                />
              </FormField>
              <CheckboxField
                label={t("delivered")}
                checked={form.entregado}
                onChange={(v) => setForm({ ...form, entregado: v })}
              />
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>
                {editing ? tc("update") : tc("create")}
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
