"use client";
/**
 * Identification type catalog (#1361 slice 2, RF-53, CU17): ADMIN lists, creates,
 * edits and deletes the types the person form offers. The API answers 409 for a
 * name another type has (any letter case) and for a type people use.
 */
import { useState } from "react";
import { toast } from "sonner";
import { useTranslations } from "next-intl";
import { NotaireIcon } from "@/components/ui/notaire-icon";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { ApiError } from "@/lib/api-client";
import { presentMutationError } from "@/lib/mutation-error";
import { useDeleteError } from "@/hooks/useDeleteError";
import {
  useCreateIdentificationType,
  useDeleteIdentificationType,
  useIdentificationTypes,
  useUpdateIdentificationType,
} from "@/hooks/useIdentificationTypes";
import type { IdentificationType } from "@/types";

interface Draft {
  id?: number;
  name: string;
  characters: string;
}

const EMPTY: Draft = { name: "", characters: "" };

export default function IdentificationTypesPage() {
  const t = useTranslations("administracion.tiposIdentificacion");
  const tc = useTranslations("common");
  const showDeleteError = useDeleteError();

  const { data = [], isLoading } = useIdentificationTypes();
  const createType = useCreateIdentificationType();
  const updateType = useUpdateIdentificationType();
  const deleteType = useDeleteIdentificationType();

  const [modalOpen, setModalOpen] = useState(false);
  const [draft, setDraft] = useState<Draft>(EMPTY);
  const [deleteTarget, setDeleteTarget] = useState<IdentificationType | null>(null);

  const saving = createType.isPending || updateType.isPending;

  function openCreate() {
    setDraft(EMPTY);
    setModalOpen(true);
  }

  function openEdit(type: IdentificationType) {
    setDraft({ id: type.idIdentificationType, name: type.name ?? "", characters: type.characters ?? "" });
    setModalOpen(true);
  }

  async function handleSave() {
    const name = draft.name.trim();
    const characters = draft.characters.trim();
    if (!name) {
      toast.error(t("nameRequired"));
      return;
    }
    if (!/^([1-9]|[1-4][0-9]|50)$/.test(characters)) { // same rule as the API (#1526)
      toast.error(t("charactersRequired"));
      return;
    }
    try {
      if (draft.id) {
        await updateType.mutateAsync({ id: draft.id, name, characters });
        toast.success(t("updated"));
      } else {
        await createType.mutateAsync({ name, characters });
        toast.success(t("created"));
      }
      setModalOpen(false);
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        toast.error(t("duplicateName"));
        return;
      }
      presentMutationError(err, { fallback: t("errorSave") });
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      await deleteType.mutateAsync(deleteTarget.idIdentificationType);
      toast.success(t("deleted"));
    } catch (err) {
      showDeleteError(err, t("errorDelete"));
    } finally {
      setDeleteTarget(null);
    }
  }

  const columns: Column<IdentificationType>[] = [
    {
      key: "id",
      header: tc("id"),
      render: (e) => <span className="text-xs text-muted-foreground">{e.idIdentificationType}</span>,
      className: "w-12",
    },
    { key: "name", header: t("fields.name"), render: (e) => <span className="font-medium">{e.name}</span>, mobile: "primary" },
    { key: "characters", header: t("fields.characters"), render: (e) => e.characters ?? "—" },
    {
      key: "actions",
      header: "",
      className: "w-24",
      mobile: "actions",
      render: (e) => (
        <div className="flex gap-2 justify-end">
          <Button
            size="icon-sm"
            variant="ghost"
            className="shrink-0"
            onClick={() => openEdit(e)}
            data-testid="btn-edit-identification-type"
            aria-label={`${tc("edit")} ${e.name}`}
          >
            <NotaireIcon src="/icons/actions/generate.png" alt="" size={16} />
          </Button>
          <Button
            size="icon-sm"
            variant="ghost"
            className="shrink-0 text-destructive hover:text-destructive"
            onClick={() => setDeleteTarget(e)}
            data-testid="btn-delete-identification-type"
            aria-label={`${tc("delete")} ${e.name}`}
          >
            <NotaireIcon src="/icons/actions/delete.png" alt="" size={16} />
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
          <Button onClick={openCreate} data-testid="btn-new-identification-type">
            <NotaireIcon src="/icons/actions/add.png" alt="" size={16} className="mr-1" />
            {t("new")}
          </Button>
        }
      />
      <DataTable
        data={data}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(e) => e.idIdentificationType}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={draft.id ? t("edit") : t("new")}>
              <FormField label={t("fields.name")} required>
                <Input
                  value={draft.name}
                  onChange={(e) => setDraft({ ...draft, name: e.target.value })}
                  placeholder={t("fields.namePlaceholder")}
                  maxLength={60}
                  data-testid="input-identification-type-name"
                />
              </FormField>
              <FormField label={t("fields.characters")} helperText={t("fields.charactersHelp")} required>
                <Input
                  value={draft.characters}
                  onChange={(e) => setDraft({ ...draft, characters: e.target.value })}
                  inputMode="numeric"
                  maxLength={2}
                  placeholder="8"
                  data-testid="input-identification-type-characters"
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                <NotaireIcon src="/icons/actions/cancel.png" alt="" size={16} className="mr-1" />
                {tc("cancel")}
              </Button>
              <Button onClick={handleSave} disabled={saving} data-testid="btn-save-identification-type">
                <NotaireIcon src="/icons/actions/save.png" alt="" size={16} className="mr-1 brightness-0 invert" />
                {draft.id ? tc("update") : tc("save")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={!!deleteTarget}
        onOpenChange={(v) => !v && setDeleteTarget(null)}
        onConfirm={handleDelete}
        loading={deleteType.isPending}
      />
    </div>
  );
}
