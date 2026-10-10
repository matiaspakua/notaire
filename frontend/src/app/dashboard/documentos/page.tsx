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
import { presentMutationError } from "@/lib/mutation-error";
import { formatDate } from "@/lib/utils";
import {
  useDocumentosPresentados,
  useCreateDocumentoPresentado,
  useUpdateDocumentoPresentado,
  useDeleteDocumentoPresentado,
} from "@/hooks/useDocumentosPresentados";
import { useTiposDocumento } from "@/hooks/useDocumentos";
import { useGestiones } from "@/hooks/useGestiones";
import {
  EMPTY_DOCUMENTO_FORM,
  missingDocumentoFields,
  showsTramiteLink,
  toDocumentoRequest,
  type DocumentoForm,
} from "@/lib/documento-presentado-form";
import { useReingresoDocumentacion } from "@/hooks/useReingresoDocumentacion";
import type { DocumentoPresentado } from "@/types";
import { useDeleteError } from "@/hooks/useDeleteError";
import { toDateInputValue, todayInputValue } from "@/lib/dates";

export default function DocumentosPage() {
  const t = useTranslations("documentos");
  const showDeleteError = useDeleteError();
  const tc = useTranslations("common");

  const { data: documentos = [], isLoading } = useDocumentosPresentados();
  const { data: tiposDoc = [] } = useTiposDocumento();
  const createMutation = useCreateDocumentoPresentado();
  const updateMutation = useUpdateDocumentoPresentado();
  const deleteMutation = useDeleteDocumentoPresentado();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<DocumentoPresentado | null>(null);
  const [form, setForm] = useState<DocumentoForm>(EMPTY_DOCUMENTO_FORM);
  const { data: gestiones = [] } = useGestiones();
  const { data: gestionConTramites } = useReingresoDocumentacion(form.gestionId ? Number(form.gestionId) : undefined);
  const tramites = gestionConTramites?.procedures ?? [];

  function openCreate() {
    setEditing(null);
    setForm({ ...EMPTY_DOCUMENTO_FORM, fecha: todayInputValue() });
    setModalOpen(true);
  }

  function openEdit(d: DocumentoPresentado) {
    setEditing(d);
    setForm({
      tipoId: d.type?.idDocumentType?.toString() ?? "",
      fecha: toDateInputValue(d.date),
      entregado: d.delivered ?? false,
      gestionId: "",
      tramiteId: d.procedureId?.toString() ?? "",
    });
    setModalOpen(true);
  }

  async function handleSave() {
    const data = toDocumentoRequest(form);
    try {
      if (editing?.idSubmittedDocument) {
        await updateMutation.mutateAsync({ id: editing.idSubmittedDocument, data });
        toast.success(t("updated"));
      } else {
        await createMutation.mutateAsync(data);
        toast.success(t("registered"));
      }
      setModalOpen(false);
    } catch (err) {
      presentMutationError(err, { fallback: t("errorSave") });
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success(t("deleted"));
    } catch (err) {
      showDeleteError(err, t("errorDelete"));
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<DocumentoPresentado>[] = [
    {
      key: "id",
      header: tc("id"),
      render: (d) => <span className="text-muted-foreground text-xs">{d.idSubmittedDocument}</span>,
      className: "w-16",
    },
    {
      key: "tipo",
      header: tc("type"),
      render: (d) => <span className="font-medium">{d.type?.name ?? "—"}</span>,
    },
    {
      key: "tramite",
      header: t("tramite"),
      render: (d) => (d.procedureId ? `#${d.procedureId}` : "—"),
      className: "w-24",
    },
    {
      key: "fecha",
      header: tc("date"),
      render: (d) => formatDate(d.date),
    },
    {
      key: "entregado",
      header: t("delivered"),
      render: (d) => (
        <span className="flex items-center gap-1.5">
          {d.delivered ? (
            <CheckCircle className="h-4 w-4 text-emerald-500" />
          ) : (
            <XCircle className="h-4 w-4 text-muted-foreground/40" />
          )}
          <span className="text-sm">{d.delivered ? "Sí" : "No"}</span>
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
            onClick={() => setDeleteId(d.idSubmittedDocument!)}
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
          <Button onClick={openCreate} data-testid="btn-nuevo-documento">
            <Plus className="h-4 w-4" />
            {t("newDocumento")}
          </Button>
        }
      />

      <DataTable
        data={documentos}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(d) => d.idSubmittedDocument!}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={editing ? t("editDocumento") : t("newDocumento")}>
              <FormField label={tc("type")} required={!editing}>
                <Select value={form.tipoId} onValueChange={(v) => setForm({ ...form, tipoId: v })}>
                  <SelectTrigger data-testid="select-tipo-documento">
                    <SelectValue placeholder="Seleccionar tipo" />
                  </SelectTrigger>
                  <SelectContent>
                    {tiposDoc.map((tipo) => (
                      <SelectItem key={tipo.idDocumentType} value={tipo.idDocumentType!.toString()}>
                        {tipo.name}
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
              {showsTramiteLink(editing) && (
                <>
                  <FormField label={t("gestion")} helperText={t("gestionHelper")} required={!editing}>
                    <Select
                      value={form.gestionId}
                      onValueChange={(v) => setForm({ ...form, gestionId: v, tramiteId: "" })}
                    >
                      <SelectTrigger data-testid="select-gestion-documento">
                        <SelectValue placeholder={t("selectGestion")} />
                      </SelectTrigger>
                      <SelectContent>
                        {gestiones.map((g) => (
                          <SelectItem key={g.idManagement} value={g.idManagement!.toString()}>
                            {g.number} — {g.encabezado}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </FormField>
                  {form.gestionId && (
                    <FormField
                      label={t("tramite")}
                      helperText={tramites.length === 0 ? t("sinTramites") : undefined}
                      required={!editing}
                    >
                      <Select value={form.tramiteId} onValueChange={(v) => setForm({ ...form, tramiteId: v })}>
                        <SelectTrigger data-testid="select-tramite-documento" disabled={tramites.length === 0}>
                          <SelectValue placeholder={t("selectTramite")} />
                        </SelectTrigger>
                        <SelectContent>
                          {tramites.map((tr) => (
                            <SelectItem key={tr.idProcedure} value={tr.idProcedure.toString()}>
                              #{tr.idProcedure} — {tr.typeProcedureName}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </FormField>
                  )}
                </>
              )}
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
              <Button
                onClick={handleSave}
                disabled={
                  createMutation.isPending ||
                  updateMutation.isPending ||
                  missingDocumentoFields(form, !!editing).length > 0
                }
                data-testid="btn-guardar-documento"
              >
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
