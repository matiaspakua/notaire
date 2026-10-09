"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, UserCheck } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import {
  useSuplencias,
  useCreateSuplencia,
  useUpdateSuplencia,
  useDeleteSuplencia,
} from "@/hooks/useSuplencias";
import { presentMutationError } from "@/lib/mutation-error";
import { formatDate } from "@/lib/utils";
import type { Suplencia } from "@/types";

const EMPTY: Partial<Suplencia> = {
  dateStart: "",
  dateEnd: "",
};

function personaName(p: Suplencia["fkIdSubstituted"]): string {
  if (!p) return "—";
  return [p.name, p.lastName].filter(Boolean).join(" ") || `#${p.personId}`;
}

export default function SuplenciasPage() {
  const t = useTranslations("suplencias");
  const tc = useTranslations("common");
  const { data: suplencias = [], isLoading } = useSuplencias();
  const createMutation = useCreateSuplencia();
  const updateMutation = useUpdateSuplencia();
  const deleteMutation = useDeleteSuplencia();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Suplencia>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  const [escribanoId, setEscribanoId] = useState("");
  const [suplenteId, setSuplenteId] = useState("");

  function openCreate() {
    setEditing(EMPTY);
    setEscribanoId("");
    setSuplenteId("");
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(s: Suplencia) {
    setEditing(s);
    setEscribanoId(s.fkIdSubstituted?.personId?.toString() ?? "");
    setSuplenteId(s.fkIdSubstitute?.personId?.toString() ?? "");
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    const payload: Partial<Suplencia> = {
      dateStart: editing.dateStart,
      dateEnd: editing.dateEnd,
      fkIdSubstituted: escribanoId ? { personId: Number(escribanoId) } : undefined,
      fkIdSubstitute: suplenteId ? { personId: Number(suplenteId) } : undefined,
    };
    try {
      if (isEditMode && editing.idSubstitution) {
        await updateMutation.mutateAsync({ id: editing.idSubstitution, data: payload });
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
      presentMutationError(err, { fallback: t("errorDelete") });
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<Suplencia>[] = [
    {
      key: "id",
      header: tc("id"),
      render: (s) => <span className="text-xs text-muted-foreground">{s.idSubstitution}</span>,
      className: "w-12",
    },
    {
      key: "escribano",
      header: t("fields.escribano"),
      render: (s) => (
        <div className="flex items-center gap-2">
          <UserCheck className="h-4 w-4 text-muted-foreground" />
          <span className="font-medium">{personaName(s.fkIdSubstituted)}</span>
        </div>
      ),
    },
    {
      key: "suplente",
      header: t("fields.suplente"),
      render: (s) => personaName(s.fkIdSubstitute),
    },
    {
      key: "desde",
      header: t("fields.desde"),
      render: (s) => formatDate(s.dateStart),
    },
    {
      key: "hasta",
      header: t("fields.hasta"),
      render: (s) => formatDate(s.dateEnd),
    },
    {
      key: "actions",
      header: "",
      className: "w-24",
      render: (s) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(s)} aria-label={tc("edit")}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(s.idSubstitution!)}
            aria-label={tc("delete")}
          >
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title={t("title")}
        description={t("description")}
        actions={
          <Button onClick={openCreate} data-testid="btn-nueva-suplencia">
            <Plus className="h-4 w-4" />
            {t("newSuplencia")}
          </Button>
        }
      />

      <DataTable
        data={suplencias}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(s) => s.idSubstitution!}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={isEditMode ? t("editSuplencia") : t("newSuplencia")}>
              <div className="grid grid-cols-2 gap-3">
                <FormField label={t("fields.escribanoId")} helperText={t("fields.escribanoHelper")}>
                  <Input
                    type="number"
                    value={escribanoId}
                    onChange={(e) => setEscribanoId(e.target.value)}
                    placeholder={t("fields.escribanoPlaceholder")}
                    data-testid="input-escribano-id"
                  />
                </FormField>
                <FormField label={t("fields.suplenteId")} helperText={t("fields.escribanoHelper")}>
                  <Input
                    type="number"
                    value={suplenteId}
                    onChange={(e) => setSuplenteId(e.target.value)}
                    placeholder={t("fields.suplentePlaceholder")}
                    data-testid="input-suplente-id"
                  />
                </FormField>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <FormField label={t("fields.desde")}>
                  <Input
                    type="date"
                    value={editing.dateStart ?? ""}
                    onChange={(e) => setEditing({ ...editing, dateStart: e.target.value })}
                    data-testid="input-desde"
                  />
                </FormField>
                <FormField label={t("fields.hasta")}>
                  <Input
                    type="date"
                    value={editing.dateEnd ?? ""}
                    onChange={(e) => setEditing({ ...editing, dateEnd: e.target.value })}
                    data-testid="input-hasta"
                  />
                </FormField>
              </div>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button
                onClick={handleSave}
                disabled={createMutation.isPending || updateMutation.isPending}
                data-testid="btn-guardar-suplencia"
              >
                {isEditMode ? tc("update") : t("register")}
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
