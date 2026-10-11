"use client";

import { Suspense, useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, Signature } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import {
  useDeedsPage,
  useCreateDeed,
  useUpdateDeed,
  useDeleteDeed,
  useSignDeed,
} from "@/hooks/useDeeds";
import { extractApiError } from "@/lib/utils";
import { useClampPage, useUrlPagination } from "@/hooks/useUrlPagination";
import type { Deed, Folio } from "@/types";
import { useDeleteError } from "@/hooks/useDeleteError";
import { toDateInputValue } from "@/lib/dates";
import { useFormat } from "@/hooks/useFormat";

const EMPTY: Partial<Deed> = { number: undefined, dateDeedrecording: "", body: "" };
const ESTADO_SIN_FIRMAR = "Sin Firmar";

export default function DeedsPage() {
  return (
    <Suspense>
      <DeedsList />
    </Suspense>
  );
}

function DeedsList() {
  const fmt = useFormat();
  const t = useTranslations("escrituras");
  const showDeleteError = useDeleteError();
  const tc = useTranslations("common");

  const { data: folios = [] } = useQuery({
    queryKey: ["folios"],
    queryFn: () => apiGet<Folio[]>("/folios"),
  });
  const createMutation = useCreateDeed();
  const updateMutation = useUpdateDeed();
  const deleteMutation = useDeleteDeed();
  const firmarMutation = useSignDeed();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [firmarId, setFirmarId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Deed>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);
  const [searchNumber, setSearchNumber] = useState("");
  const number = searchNumber.trim();

  // One server page at a time, page and size in the URL (#1340): the list used
  // to load size=1000.
  const paging = useUrlPagination();
  const { data: deedsPage, isLoading: isLoadingPage, isFetching } = useDeedsPage(
    { page: paging.page, size: paging.size },
    { enabled: !number },
  );
  useClampPage(paging, number ? undefined : deedsPage?.totalPages);

  // The backend reads `number`. Any other query name is ignored and returns every deed.
  const { data: found = [], isLoading: isLoadingSearch } = useQuery({
    queryKey: ["deeds", "search", number],
    queryFn: () => apiGet<Deed[]>(`/deeds/search?number=${encodeURIComponent(number)}`),
    enabled: /^\d+$/.test(number),
  });
  const filteredDeeds = number ? (/^\d+$/.test(number) ? found : []) : deedsPage?.content ?? [];
  const isLoading = number ? isLoadingSearch && /^\d+$/.test(number) : isLoadingPage;

  function openCreate() { setEditing(EMPTY); setIsEditMode(false); setModalOpen(true); }
  function openEdit(e: Deed) { setEditing(e); setIsEditMode(true); setModalOpen(true); }

  async function handleSave() {
    try {
      if (isEditMode && editing.idDeed) {
        await updateMutation.mutateAsync({ id: editing.idDeed, data: editing });
        toast.success(t("updated"));
      } else {
        await createMutation.mutateAsync(editing);
        toast.success(t("created"));
      }
      setModalOpen(false);
    } catch (err) { toast.error(extractApiError(err) ?? t("errorSave")); }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success(t("deleted"));
    } catch (err) { showDeleteError(err, t("errorDelete")); }
    finally { setDeleteId(null); }
  }

  async function handleSign() {
    if (!firmarId) return;
    try {
      await firmarMutation.mutateAsync(firmarId);
      toast.success(t("firmada"));
    } catch (err) { toast.error(extractApiError(err) ?? t("errorFirmar")); }
    finally { setFirmarId(null); }
  }

  const columns: Column<Deed>[] = [
    { key: "id", header: tc("id"), render: (e) => <span className="text-xs text-muted-foreground">{e.idDeed}</span>, className: "w-12" },
    { key: "number", header: t("fields.numero"), render: (e) => <span className="font-medium">{e.number ?? "—"}</span> },
    { key: "date", header: tc("date"), render: (e) => fmt.date(e.dateDeedrecording) },
    { key: "status", header: t("fields.estado"), render: (e) => e.status ?? "—" },
    {
      key: "folio",
      header: t("fields.folio"),
      render: (e) => {
        const linked = folios.find((f) => f.fkIdDeed?.idDeed === e.idDeed);
        return linked ? `Folio #${linked.idFolio}` : "—";
      },
    },
    {
      key: "actions", header: "", className: "w-32",
      render: (e) => (
        <div className="flex gap-2 justify-end">
          {e.status === ESTADO_SIN_FIRMAR && (
            <Button
              size="sm"
              variant="ghost"
              onClick={() => setFirmarId(e.idDeed!)}
              aria-label={t("firmarEscritura")}
              data-testid={`btn-sign-deed-${e.idDeed}`}
            >
              <Signature className="h-4 w-4" />
            </Button>
          )}
          <Button size="sm" variant="ghost" onClick={() => openEdit(e)} aria-label={tc("edit")}><Pencil className="h-4 w-4" /></Button>
          <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive" onClick={() => setDeleteId(e.idDeed!)} aria-label={tc("delete")}><Trash2 className="h-4 w-4" /></Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title={t("title")}
        actions={<Button onClick={openCreate} data-testid="btn-new-deed"><Plus className="h-4 w-4" />{t("newEscritura")}</Button>}
      />
      <div className="px-4 pb-4">
        <Input
          placeholder={t("searchPlaceholder")}
          aria-label={t("searchPlaceholder")}
          value={searchNumber}
          onChange={(e) => setSearchNumber(e.target.value)}
          className="w-full sm:w-48"
          type="number"
          data-testid="input-search-deed"
        />
      </div>
      <DataTable
        data={filteredDeeds}
        columns={columns}
        isLoading={isLoading}
        isFetching={!number && isFetching}
        keyExtractor={(e) => e.idDeed!}
        emptyMessage={t("noData")}
        pagination={
          number
            ? undefined
            : {
                page: deedsPage?.number ?? paging.page,
                size: paging.size,
                totalElements: deedsPage?.totalElements ?? 0,
                onPageChange: paging.setPage,
                onSizeChange: paging.setSize,
              }
        }
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={isEditMode ? t("editEscritura") : t("newEscritura")}>
              <FormField label={t("fields.numero")} required>
                <Input type="number" value={editing.number ?? ""} onChange={(e) => setEditing({ ...editing, number: Number(e.target.value) })} />
              </FormField>
              <FormField label={tc("date")} required>
                <Input type="date" value={toDateInputValue(editing.dateDeedrecording)} onChange={(e) => setEditing({ ...editing, dateDeedrecording: e.target.value })} />
              </FormField>
              <FormField label={t("fields.folio")} required={!isEditMode}>
                <Select value={editing.idFolio?.toString() ?? ""} onValueChange={(v) => setEditing({ ...editing, idFolio: parseInt(v) })}>
                  <SelectTrigger data-testid="select-folio-deed">
                    <SelectValue placeholder={t("fields.folio")} />
                  </SelectTrigger>
                  <SelectContent>
                    {folios.filter(f => f.status === "Nuevo").map(f => (
                      <SelectItem key={f.idFolio} value={String(f.idFolio)}>
                        {t("folioOption", { id: f.idFolio ?? "—", type: f.fkIdFolioType?.name ?? "—" })}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label={t("fields.observaciones")}>
                <Input
                  value={editing.notes ?? ""}
                  onChange={(e) => setEditing({ ...editing, notes: e.target.value })}
                  data-testid="input-notes-deed"
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>{tc("cancel")}</Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>{isEditMode ? tc("update") : tc("create")}</Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={!!deleteId} onOpenChange={(v) => !v && setDeleteId(null)} onConfirm={handleDelete} loading={deleteMutation.isPending} />

      <ConfirmDialog
        open={!!firmarId}
        onOpenChange={(v) => !v && setFirmarId(null)}
        onConfirm={handleSign}
        loading={firmarMutation.isPending}
        title={t("firmarConfirmTitle")}
        description={t("firmarConfirmDescription")}
        confirmLabel={t("firmarEscritura")}
      />
    </div>
  );
}
