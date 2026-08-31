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
  });

  function openCreate() {
    setEditing(null);
    setForm({ nomenclaturaCatastral: "", domicilio: "", valuacionFiscal: "", observaciones: "" });
    setModalOpen(true);
  }

  function openEdit(i: Inmueble) {
    setEditing(i);
    setForm({
      nomenclaturaCatastral: i.nomenclaturaCatastral ?? "",
      domicilio: i.domicilio ?? "",
      valuacionFiscal: i.valuacionFiscal !== undefined ? String(i.valuacionFiscal) : "",
      observaciones: i.observaciones ?? "",
    });
    setModalOpen(true);
  }

  async function handleSave() {
    try {
      const payload = {
        nomenclaturaCatastral: form.nomenclaturaCatastral,
        domicilio: form.domicilio,
        observaciones: form.observaciones,
        valuacionFiscal: form.valuacionFiscal === "" ? undefined : Number(form.valuacionFiscal),
      };
      if (editing?.idInmueble) {
        await updateMutation.mutateAsync({ id: editing.idInmueble, data: payload });
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
      render: (i) => <span className="text-muted-foreground text-xs">{i.idInmueble}</span>,
      className: "w-16",
    },
    {
      key: "nomenclatura",
      header: "Nomenclatura Catastral",
      render: (i) => <span className="font-medium">{i.nomenclaturaCatastral ?? "—"}</span>,
    },
    {
      key: "domicilio",
      header: t("fields.domicilio"),
      render: (i) => i.domicilio ?? "—",
    },
    {
      key: "valuacion",
      header: t("fields.valuacionFiscal"),
      render: (i) => i.valuacionFiscal ?? "—",
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
            onClick={() => setDeleteId(i.idInmueble!)}
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
            {t("newInmueble")}
          </Button>
        }
      />

      <DataTable
        data={inmuebles}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(i) => i.idInmueble!}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={editing ? t("editInmueble") : t("newInmueble")}>
              <FormField label="Nomenclatura Catastral" required>
                <Input
                  value={form.nomenclaturaCatastral}
                  onChange={(e) => setForm({ ...form, nomenclaturaCatastral: e.target.value })}
                  placeholder="Ej: 01-02-03-04-05"
                />
              </FormField>
              <FormField label={t("fields.domicilio")}>
                <Input
                  value={form.domicilio}
                  onChange={(e) => setForm({ ...form, domicilio: e.target.value })}
                  placeholder={t("fields.domicilioPlaceholder")}
                />
              </FormField>
              <FormField label={t("fields.valuacionFiscal")}>
                <Input
                  type="number"
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
