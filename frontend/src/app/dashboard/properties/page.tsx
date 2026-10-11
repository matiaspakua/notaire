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
  useProperties,
  useCreateProperty,
  useUpdateProperty,
  useDeleteProperty,
} from "@/hooks/useProperties";
import { presentMutationError } from "@/lib/mutation-error";
import type { Property } from "@/types";
import { useDeleteError } from "@/hooks/useDeleteError";

export default function PropertiesPage() {
  const t = useTranslations("inmuebles");
  const showDeleteError = useDeleteError();
  const tc = useTranslations("common");

  const { data: properties = [], isLoading } = useProperties();
  const createMutation = useCreateProperty();
  const updateMutation = useUpdateProperty();
  const deleteMutation = useDeleteProperty();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Property | null>(null);
  const [form, setForm] = useState({
    cadastralDesignationCatastral: "",
    domicilio: "",
    valuacionFiscal: "",
    notes: "",
    matricula: "",
    tomoFolioFinca: "",
    linderos: "",
  });

  function openCreate() {
    setEditing(null);
    setForm({
      cadastralDesignationCatastral: "",
      domicilio: "",
      valuacionFiscal: "",
      notes: "",
      matricula: "",
      tomoFolioFinca: "",
      linderos: "",
    });
    setModalOpen(true);
  }

  function openEdit(i: Property) {
    setEditing(i);
    setForm({
      cadastralDesignationCatastral: i.cadastralDesignation ?? "",
      domicilio: i.address ?? "",
      valuacionFiscal: i.fiscalAppraisal !== undefined ? String(i.fiscalAppraisal) : "",
      notes: i.notes ?? "",
      matricula: i.registrationNumber ?? "",
      tomoFolioFinca: i.volumeFolioLandRecord ?? "",
      linderos: i.boundaries ?? "",
    });
    setModalOpen(true);
  }

  async function handleSave() {
    try {
      const payload = {
        cadastralDesignation: form.cadastralDesignationCatastral,
        address: form.domicilio,
        notes: form.notes,
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

  const columns: Column<Property>[] = [
    {
      key: "id",
      header: tc("id"),
      render: (i) => <span className="text-muted-foreground text-xs">{i.idProperty}</span>,
      className: "w-16",
    },
    {
      key: "cadastralCode",
      header: t("fields.nomenclatura"),
      render: (i) => <span className="font-medium">{i.cadastralDesignation ?? "—"}</span>,
    },
    {
      key: "address",
      header: t("fields.domicilio"),
      render: (i) => i.address ?? "—",
    },
    {
      key: "valuation",
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
          <Button onClick={openCreate} data-testid="btn-new-property">
            <Plus className="h-4 w-4" />
            {t("newInmueble")}
          </Button>
        }
      />

      <DataTable
        data={properties}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(i) => i.idProperty!}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={editing ? t("editInmueble") : t("newInmueble")}>
              <FormField label={t("fields.nomenclatura")} required>
                <Input
                  data-testid="input-nomenclatura"
                  value={form.cadastralDesignationCatastral}
                  onChange={(e) => setForm({ ...form, cadastralDesignationCatastral: e.target.value })}
                  placeholder={t("fields.nomenclaturaPlaceholder")}
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
                  value={form.notes}
                  onChange={(e) => setForm({ ...form, notes: e.target.value })}
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
                  data-testid="input-volume-folio-property"
                  value={form.tomoFolioFinca}
                  onChange={(e) => setForm({ ...form, tomoFolioFinca: e.target.value })}
                  placeholder={t("fields.tomoFolioFincaPlaceholder")}
                />
              </FormField>
              <FormField label={t("fields.linderos")}>
                <Input
                  data-testid="input-linderos"
                  value={form.linderos}
                  onChange={(e) => setForm({ ...form, linderos: e.target.value })}
                  placeholder={t("fields.linderosPlaceholder")}
                />
              </FormField>
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
                  // The cadastralDesignation catastral identifies the property (CU69, #655).
                  !form.cadastralDesignationCatastral.trim()
                }
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
