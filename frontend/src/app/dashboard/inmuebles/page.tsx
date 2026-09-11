"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2 } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import {
  useInmuebles,
  useCreateInmueble,
  useUpdateInmueble,
  useDeleteInmueble,
} from "@/hooks/useInmuebles";
import type { Inmueble } from "@/types";

export default function InmueblesPage() {
  const t = useTranslations("inmuebles");
  const tc = useTranslations("common");

  const { data: inmuebles = [], isLoading } = useInmuebles();
  const createMutation = useCreateInmueble();
  const updateMutation = useUpdateInmueble();
  const deleteMutation = useDeleteInmueble();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Inmueble | null>(null);
  const [form, setForm] = useState({
    nomenclaturaCatastral: "",
    domicilio: "",
    valuacionFiscal: "",
    observaciones: "",
    matricula: "",
    tomoFolioFinca: "",
    linderos: "",
  });

  function openCreate() {
    setEditing(null);
    setForm({
      nomenclaturaCatastral: "",
      domicilio: "",
      valuacionFiscal: "",
      observaciones: "",
      matricula: "",
      tomoFolioFinca: "",
      linderos: "",
    });
    setModalOpen(true);
  }

  function openEdit(i: Inmueble) {
    setEditing(i);
    setForm({
      nomenclaturaCatastral: i.cadastralDesignation ?? "",
      domicilio: i.address ?? "",
      valuacionFiscal: i.fiscalAppraisal !== undefined ? String(i.fiscalAppraisal) : "",
      observaciones: i.notes ?? "",
      matricula: i.registrationNumber ?? "",
      tomoFolioFinca: i.volumeFolioLandRecord ?? "",
      linderos: i.boundaries ?? "",
    });
    setModalOpen(true);
  }

  async function handleSave() {
    try {
      const payload = {
        cadastralDesignation: form.nomenclaturaCatastral,
        address: form.domicilio,
        notes: form.observaciones,
        fiscalAppraisal: form.valuacionFiscal === "" ? undefined : Number(form.valuacionFiscal),
        registrationNumber: form.matricula || undefined,
        volumeFolioLandRecord: form.tomoFolioFinca || undefined,
        boundaries: form.linderos || undefined,
      };
      if (editing?.idProperty) {
        await updateMutation.mutateAsync({ id: editing.idProperty, data: payload });
        toast.success(t("updated"));
      } else {
        await createMutation.mutateAsync(payload);
        toast.success(t("created"));
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

  const columns: Column<Inmueble>[] = [
    {
      key: "id",
      header: tc("id"),
      render: (i) => <span className="text-muted-foreground text-xs">{i.idProperty}</span>,
      className: "w-16",
    },
    {
      key: "nomenclatura",
      header: "Nomenclatura Catastral",
      render: (i) => <span className="font-medium">{i.cadastralDesignation ?? "—"}</span>,
    },
    {
      key: "domicilio",
      header: t("fields.domicilio"),
      render: (i) => i.address ?? "—",
    },
    {
      key: "valuacion",
      header: t("fields.valuacionFiscal"),
      render: (i) => i.fiscalAppraisal ?? "—",
      className: "w-40",
    },
    {
      key: "actions",
      header: "",
      render: (i) => (
        <div className="flex gap-1 justify-end">
          <Button size="icon" variant="ghost" onClick={() => openEdit(i)} aria-label={tc("edit")}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="icon"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(i.idProperty!)}
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
          <Button onClick={openCreate} data-testid="btn-nuevo-inmueble">
            <Plus className="h-4 w-4" />
            {t("newInmueble")}
          </Button>
        }
      />

      <DataTable
        data={inmuebles}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(i) => i.idProperty!}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={editing ? t("editInmueble") : t("newInmueble")}>
              <FormField label="Nomenclatura Catastral" required>
                <Input
                  data-testid="input-nomenclatura"
                  value={form.nomenclaturaCatastral}
                  onChange={(e) => setForm({ ...form, nomenclaturaCatastral: e.target.value })}
                  placeholder="Ej: 01-02-03-04-05"
                />
              </FormField>
              <FormField label={t("fields.domicilio")}>
                <Input
                  data-testid="input-domicilio"
                  value={form.domicilio}
                  onChange={(e) => setForm({ ...form, domicilio: e.target.value })}
                  placeholder={t("fields.domicilioPlaceholder")}
                />
              </FormField>
              <FormField label={t("fields.valuacionFiscal")}>
                <Input
                  type="number"
                  data-testid="input-valuacion-fiscal"
                  value={form.valuacionFiscal}
                  onChange={(e) => setForm({ ...form, valuacionFiscal: e.target.value })}
                />
              </FormField>
              <FormField label={tc("observations")}>
                <Input
                  value={form.observaciones}
                  onChange={(e) => setForm({ ...form, observaciones: e.target.value })}
                />
              </FormField>
            </FormSection>
            <FormSection title={t("registralData")}>
              <FormField label={t("fields.matricula")}>
                <Input
                  data-testid="input-matricula"
                  value={form.matricula}
                  onChange={(e) => setForm({ ...form, matricula: e.target.value })}
                />
              </FormField>
              <FormField label={t("fields.tomoFolioFinca")}>
                <Input
                  data-testid="input-tomo-folio-finca"
                  value={form.tomoFolioFinca}
                  onChange={(e) => setForm({ ...form, tomoFolioFinca: e.target.value })}
                  placeholder="T1-F2-FN3"
                />
              </FormField>
              <FormField label={t("fields.linderos")}>
                <Input
                  data-testid="input-linderos"
                  value={form.linderos}
                  onChange={(e) => setForm({ ...form, linderos: e.target.value })}
                  placeholder="Norte, Sur, Este, Oeste"
                />
              </FormField>
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
