"use client";

import { Suspense, useState } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
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
  useSubstitutions,
  useCreateSubstitution,
  useUpdateSubstitution,
  useDeleteSubstitution,
} from "@/hooks/useSubstitutions";
import { presentMutationError } from "@/lib/mutation-error";
import type { Substitution } from "@/types";
import { useDeleteError } from "@/hooks/useDeleteError";
import { toDateInputValue } from "@/lib/dates";
import { useFormat } from "@/hooks/useFormat";
import { filterSubstitutions, substitutionYears } from "@/lib/substitution-filter";

const EMPTY: Partial<Substitution> = {
  dateStart: "",
  dateEnd: "",
};

function personName(p: Substitution["fkIdSubstituted"]): string {
  if (!p) return "—";
  return [p.name, p.lastName].filter(Boolean).join(" ") || `#${p.personId}`;
}

// useSearchParams (?year=&notary=, #1546) needs a Suspense boundary for static prerendering.
export default function SubstitutionsPage() {
  return (
    <Suspense>
      <SubstitutionsList />
    </Suspense>
  );
}

function SubstitutionsList() {
  const fmt = useFormat();
  const t = useTranslations("suplencias");
  const showDeleteError = useDeleteError();
  const tc = useTranslations("common");
  const { data: allSubstitutions = [], isLoading } = useSubstitutions();
  // CU59 (#1546): year (overlap) and notary filters, kept in the URL.
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const yearParam = Number(searchParams.get("year"));
  const year = Number.isInteger(yearParam) && yearParam > 0 ? yearParam : undefined;
  // Local state so fast typing isn't lost while the URL catches up.
  const [notary, setNotary] = useState(searchParams.get("notary") ?? "");
  const years = substitutionYears(allSubstitutions, new Date().getFullYear());
  const substitutions = filterSubstitutions(allSubstitutions, { year, notary });
  function updateQuery(key: string, value: string) {
    const next = new URLSearchParams(searchParams.toString());
    if (value) next.set(key, value);
    else next.delete(key);
    const qs = next.toString();
    router.replace(qs ? `${pathname}?${qs}` : pathname, { scroll: false });
  }
  const createMutation = useCreateSubstitution();
  const updateMutation = useUpdateSubstitution();
  const deleteMutation = useDeleteSubstitution();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Substitution>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  const [notaryId, setNotaryId] = useState("");
  const [substituteId, setSubstituteId] = useState("");

  function openCreate() {
    setEditing(EMPTY);
    setNotaryId("");
    setSubstituteId("");
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(s: Substitution) {
    setEditing(s);
    setNotaryId(s.fkIdSubstituted?.personId?.toString() ?? "");
    setSubstituteId(s.fkIdSubstitute?.personId?.toString() ?? "");
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    const payload: Partial<Substitution> = {
      dateStart: editing.dateStart,
      dateEnd: editing.dateEnd,
      fkIdSubstituted: notaryId ? { personId: Number(notaryId) } : undefined,
      fkIdSubstitute: substituteId ? { personId: Number(substituteId) } : undefined,
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
      showDeleteError(err, t("errorDelete"));
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<Substitution>[] = [
    {
      key: "id",
      header: tc("id"),
      render: (s) => <span className="text-xs text-muted-foreground">{s.idSubstitution}</span>,
      className: "w-12",
    },
    {
      key: "notary",
      header: t("fields.escribano"),
      render: (s) => (
        <div className="flex items-center gap-2">
          <UserCheck className="h-4 w-4 text-muted-foreground" />
          <span className="font-medium">{personName(s.fkIdSubstituted)}</span>
        </div>
      ),
    },
    {
      key: "substitute",
      header: t("fields.suplente"),
      render: (s) => personName(s.fkIdSubstitute),
    },
    {
      key: "startDate",
      header: t("fields.desde"),
      render: (s) => fmt.date(s.dateStart),
    },
    {
      key: "endDate",
      header: t("fields.hasta"),
      render: (s) => fmt.date(s.dateEnd),
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
          <Button onClick={openCreate} data-testid="btn-new-substitution">
            <Plus className="h-4 w-4" />
            {t("newSuplencia")}
          </Button>
        }
      />

      <div className="mb-4 flex flex-wrap items-end gap-3">
        <div className="w-40">
          <FormField label={t("filtros.anio")}>
            <select
              data-testid="select-substitutions-year"
              aria-label={t("filtros.anio")}
              className="h-12 rounded-lg border border-input bg-background px-4 text-sm text-foreground font-sans outline-none cursor-pointer focus:ring-2 focus:ring-ring"
              value={year ?? ""}
              onChange={(e) => updateQuery("year", e.target.value)}
            >
              <option value="">{t("filtros.todosLosAnios")}</option>
              {years.map((y) => (
                <option key={y} value={y}>
                  {y}
                </option>
              ))}
            </select>
          </FormField>
        </div>
        <div className="w-full sm:w-72">
          <FormField label={t("filtros.escribano")}>
            <Input
              data-testid="input-substitutions-notary"
              aria-label={t("filtros.escribano")}
              placeholder={t("filtros.escribanoPlaceholder")}
              value={notary}
              onChange={(e) => {
                setNotary(e.target.value);
                updateQuery("notary", e.target.value);
              }}
            />
          </FormField>
        </div>
        <p className="pb-3 text-sm text-muted-foreground" data-testid="substitutions-count" aria-live="polite">
          {t("filtros.resultados", { count: substitutions.length })}
        </p>
      </div>

      <DataTable
        data={substitutions}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(s) => s.idSubstitution!}
        emptyMessage={year ? t("noDataYear", { year }) : t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={isEditMode ? t("editSuplencia") : t("newSuplencia")}>
              <div className="grid grid-cols-2 gap-3">
                <FormField label={t("fields.escribanoId")} helperText={t("fields.escribanoHelper")}>
                  <Input
                    type="number"
                    value={notaryId}
                    onChange={(e) => setNotaryId(e.target.value)}
                    placeholder={t("fields.escribanoPlaceholder")}
                    data-testid="input-notary-id"
                  />
                </FormField>
                <FormField label={t("fields.substituteId")} helperText={t("fields.escribanoHelper")}>
                  <Input
                    type="number"
                    value={substituteId}
                    onChange={(e) => setSubstituteId(e.target.value)}
                    placeholder={t("fields.suplentePlaceholder")}
                    data-testid="input-substitute-id"
                  />
                </FormField>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <FormField label={t("fields.desde")}>
                  <Input
                    type="date"
                    value={toDateInputValue(editing.dateStart)}
                    onChange={(e) => setEditing({ ...editing, dateStart: e.target.value })}
                    data-testid="input-start-date"
                  />
                </FormField>
                <FormField label={t("fields.hasta")}>
                  <Input
                    type="date"
                    value={toDateInputValue(editing.dateEnd)}
                    onChange={(e) => setEditing({ ...editing, dateEnd: e.target.value })}
                    data-testid="input-end-date"
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
                data-testid="btn-save-substitution"
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
