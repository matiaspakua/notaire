"use client";

import { Suspense, useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, Receipt, ListChecks } from "lucide-react";
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
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { FormContainer, FormSection, FormField, FormActions, FormHeader } from "@/theme/form-patterns";
import { useQuery } from "@tanstack/react-query";
import { apiGet, ApiError } from "@/lib/api-client";
import { presentMutationError } from "@/lib/mutation-error";
import {
  useBudgetsPage,
  useBudgetSummary,
  useCreateBudget,
  useUpdateBudget,
  useDeleteBudget,
  useLoadItemsFromTemplate,
  useAddItemsFromCatalog,
} from "@/hooks/useBudgets";
import { PersonPicker } from "@/components/shared/PersonPicker";
import { useClampPage, useUrlPagination } from "@/hooks/useUrlPagination";
import { useItems, useItemsByBudget } from "@/hooks/useItems";
import { useProcedureTypes } from "@/hooks/useProcedureTypes";
import { formatDate, formatCurrency, fullName } from "@/lib/utils";
import { BUDGET_STATUSES, DEFAULT_BUDGET_STATUS, budgetStatusLabelKey, normalizeBudgetStatus } from "@/lib/budget-status";
import type { Budget } from "@/types";
import { useDeleteError } from "@/hooks/useDeleteError";
import { toDateInputValue } from "@/lib/dates";

const NO_TEMPLATE = "none";

const EMPTY: Partial<Budget> = { date: "", propertyAmount: undefined, status: DEFAULT_BUDGET_STATUS };

export default function BudgetsPage() {
  return (
    <Suspense>
      <BudgetsList />
    </Suspense>
  );
}

function BudgetsList() {
  const t = useTranslations("presupuestos");
  const showDeleteError = useDeleteError();
  const tc = useTranslations("common");

  const createMutation = useCreateBudget();
  const updateMutation = useUpdateBudget();
  const deleteMutation = useDeleteBudget();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Budget>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  const [summaryId, setSummaryId] = useState<number | null>(null);
  const { data: summary, isLoading: isSummaryLoading, error: summaryError } =
    useBudgetSummary(summaryId);

  const [newProcedureTypeId, setNewProcedureTypeId] = useState(NO_TEMPLATE);
  const [itemsBudgetId, setItemsBudgetId] = useState<number | null>(null);
  const { data: budgetItems = [], isLoading: isItemsLoading } =
    useItemsByBudget(itemsBudgetId ?? undefined);
  const { data: procedureTypes = [] } = useProcedureTypes();
  const { data: catalogItems = [] } = useItems();
  const [selectedProcedureTypeId, setSelectedTypeProcedureId] = useState<string>("");
  const [selectedCatalogItemId, setSelectedCatalogItemId] = useState<string>("");
  const loadTemplateMutation = useLoadItemsFromTemplate();
  const addFromCatalogMutation = useAddItemsFromCatalog();

  function openItems(p: Budget) {
    setItemsBudgetId(p.idBudget!);
    setSelectedTypeProcedureId("");
    setSelectedCatalogItemId("");
  }

  async function handleLoadTemplate() {
    if (!itemsBudgetId || !selectedProcedureTypeId) return;
    try {
      await loadTemplateMutation.mutateAsync({
        idBudget: itemsBudgetId,
        procedureTypeId: Number(selectedProcedureTypeId),
      });
      toast.success(t("items.loadedFromPlantilla"));
    } catch (e) {
      // CU39: keep curated Spanish for "no template" (400). Server body is
      // terse and would otherwise replace items.errorNoPlantilla via extractApiError.
      const noTemplate = e instanceof ApiError && e.status === 400;
      presentMutationError(e, {
        fallback: noTemplate ? t("items.errorNoPlantilla") : t("items.errorCargar"),
        preferFallback: noTemplate,
      });
    }
  }

  async function handleAddFromCatalog() {
    if (!itemsBudgetId || !selectedCatalogItemId) return;
    try {
      await addFromCatalogMutation.mutateAsync({
        idBudget: itemsBudgetId,
        idItems: [Number(selectedCatalogItemId)],
      });
      toast.success(t("items.addedFromCatalogo"));
      setSelectedCatalogItemId("");
    } catch (err) {
      presentMutationError(err, { fallback: t("items.errorAgregar") });
    }
  }

  const itemsSubtotal = budgetItems.reduce((sum, item) => sum + (item.value ?? 0), 0);

  const [searchBudget, setSearchBudget] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("TODOS");
  const search = searchBudget.trim();
  const searchId = /^\d+$/.test(search) ? Number(search) : null;
  const byStatus = statusFilter !== "TODOS";

  // One server page at a time, page and size in the URL (#1340): the list used
  // to load size=1000, so budgets after the 1000th were unreachable.
  const paging = useUrlPagination();
  const { data: budgetsPage, isLoading: isLoadingPage, isFetching } = useBudgetsPage(
    { page: paging.page, size: paging.size },
    { enabled: !byStatus && searchId === null },
  );
  useClampPage(paging, byStatus || searchId !== null ? undefined : budgetsPage?.totalPages);

  // CU60: the backend reads `status`; `estado` was ignored and returned every budget.
  const { data: budgetsByStatus = [], isLoading: isLoadingByStatus } = useQuery({
    queryKey: ["budgets", "search", statusFilter],
    queryFn: () => apiGet<Budget[]>(`/budgets/search?status=${encodeURIComponent(statusFilter)}`),
    enabled: byStatus,
  });

  // A budget number is looked up on the server, so it is found on any page.
  const { data: byId = null, isLoading: isLoadingId } = useQuery({
    queryKey: ["budgets", searchId ?? 0, "search"],
    queryFn: () =>
      apiGet<Budget>(`/budgets/${searchId}`).catch((e) => {
        if (e instanceof ApiError && e.status === 404) return null;
        throw e;
      }),
    enabled: searchId !== null,
  });

  const shown = byStatus ? budgetsByStatus : budgetsPage?.content ?? [];
  const isLoading = searchId !== null ? isLoadingId : byStatus ? isLoadingByStatus : isLoadingPage;

  // A name filters the rows shown (the page or the status list): the backend has
  // no budget search by client name.
  const filteredBudgets =
    searchId !== null
      ? byId && (!byStatus || normalizeBudgetStatus(byId.status) === statusFilter)
        ? [byId]
        : []
      : shown.filter((p) => {
          if (!search) return true;
          const q = search.toLowerCase();
          return p.person ? fullName(p.person).toLowerCase().includes(q) : false;
        });
  const showPagination = !byStatus && searchId === null && !search;

  function openCreate() {
    setEditing(EMPTY);
    setNewProcedureTypeId(NO_TEMPLATE);
    setIsEditMode(false);
    setModalOpen(true);
  }
  function openEdit(p: Budget) {
    setEditing({ ...p, status: normalizeBudgetStatus(p.status) ?? p.status });
    setIsEditMode(true);
    setModalOpen(true);
  }

  /** Translated label of a stored status; a value outside the vocabulary shows as stored (#1346). */
  function statusLabel(status: string | undefined) {
    const code = normalizeBudgetStatus(status);
    return code ? t(budgetStatusLabelKey(code)) : status || "—";
  }

  async function handleSave() {
    try {
      if (isEditMode && editing.idBudget) {
        await updateMutation.mutateAsync({ id: editing.idBudget, data: editing });
        toast.success(t("updated"));
      } else {
        const procedureTypeId = newProcedureTypeId === NO_TEMPLATE ? undefined : Number(newProcedureTypeId);
        const { itemsLoaded } = await createMutation.mutateAsync({ data: editing, procedureTypeId });
        toast.success(t("created"));
        if (procedureTypeId !== undefined && !itemsLoaded) toast.warning(t("itemsNotLoaded"));
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

  const columns: Column<Budget>[] = [
    {
      key: "id",
      header: tc("id"),
      render: (p) => <span className="text-xs text-muted-foreground">{p.idBudget}</span>,
      className: "w-12",
    },
    {
      key: "date",
      header: tc("date"),
      render: (p) => formatDate(p.date),
    },
    {
      key: "person",
      header: t("fields.cliente"),
      render: (p) => p.person ? fullName(p.person) : <span className="text-muted-foreground">—</span>,
    },
    {
      key: "amount",
      header: tc("amount"),
      render: (p) => <span className="font-medium">{formatCurrency(p.propertyAmount)}</span>,
    },
    {
      key: "status",
      header: tc("status"),
      render: (p) => statusLabel(p.status),
    },
    {
      key: "actions",
      header: "",
      render: (p) => (
        <div className="flex gap-2 justify-end">
          <Button
            size="sm"
            variant="ghost"
            data-testid={`btn-summary-budget-${p.idBudget}`}
            aria-label={t("resumen.title")}
            onClick={() => setSummaryId(p.idBudget!)}
          >
            <Receipt className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            aria-label={t("items.title")}
            data-testid={`btn-items-budget-${p.idBudget}`}
            onClick={() => openItems(p)}
          >
            <ListChecks className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            data-testid={`btn-edit-budget-${p.idBudget}`}
            aria-label={tc("edit")}
            onClick={() => openEdit(p)}
          >
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            data-testid={`btn-delete-budget-${p.idBudget}`}
            aria-label={tc("delete")}
            onClick={() => setDeleteId(p.idBudget!)}
          >
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      ),
      className: "w-32",
    },
  ];

  return (
    <div>
      <AppHeader
        title={t("title")}
        actions={
          <Button onClick={openCreate} data-testid="btn-new-budget">
            <Plus className="h-4 w-4" />
            {t("newPresupuesto")}
          </Button>
        }
      />

      <div className="flex flex-wrap gap-3 px-4 pb-4">
        <Input
          placeholder={t("searchPlaceholder")}
          aria-label={t("searchPlaceholder")}
          value={searchBudget}
          onChange={(e) => setSearchBudget(e.target.value)}
          data-testid="input-search-budget"
          className="w-full sm:w-52"
        />
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger data-testid="select-status" className="w-44" aria-label={t("estadoFilter")}>
            <SelectValue placeholder={`${tc("status")}...`} />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TODOS">{tc("all")}</SelectItem>
            {BUDGET_STATUSES.map((code) => (
              <SelectItem key={code} value={code}>{t(budgetStatusLabelKey(code))}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <DataTable
        data={filteredBudgets}
        columns={columns}
        isLoading={isLoading}
        isFetching={showPagination && isFetching}
        keyExtractor={(p) => p.idBudget!}
        emptyMessage={t("noData")}
        pagination={
          showPagination
            ? {
                page: budgetsPage?.number ?? paging.page,
                size: paging.size,
                totalElements: budgetsPage?.totalElements ?? 0,
                onPageChange: paging.setPage,
                onSizeChange: paging.setSize,
              }
            : undefined
        }
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={isEditMode ? t("editPresupuesto") : t("newPresupuesto")}>
              <FormField label={t("fields.cliente")} required>
                {/* Server search instead of the first 1000 people (#1340). */}
                <PersonPicker
                  value={editing.person?.personId}
                  selected={editing.person}
                  onChange={(person) => setEditing({ ...editing, person })}
                  aria-label={t("fields.cliente")}
                  data-testid="select-person"
                />
              </FormField>
              <FormField label={tc("date")} required>
                <Input
                  type="date"
                  value={toDateInputValue(editing.date)}
                  onChange={(e) => setEditing({ ...editing, date: e.target.value })}
                />
              </FormField>
              <FormField label={`${tc("amount")} ($)`} required helperText={t("fields.montoAyuda")}>
                <Input
                  type="number"
                  step="0.01"
                  value={editing.propertyAmount ?? ""}
                  onChange={(e) => setEditing({ ...editing, propertyAmount: parseFloat(e.target.value) })}
                  data-testid="input-amount"
                />
              </FormField>
              {!isEditMode && (
                <FormField label={t("fields.tipoTramite")} helperText={t("fields.tipoTramiteAyuda")}>
                  <Select value={newProcedureTypeId} onValueChange={setNewProcedureTypeId}>
                    <SelectTrigger data-testid="select-procedure-type-new">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value={NO_TEMPLATE}>{t("fields.sinPlantilla")}</SelectItem>
                      {procedureTypes.map((tt) => (
                        <SelectItem key={tt.idProcedureType} value={tt.idProcedureType!.toString()}>
                          {tt.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </FormField>
              )}
              <FormField label={tc("status")}>
                <Select
                  value={editing.status ?? DEFAULT_BUDGET_STATUS}
                  onValueChange={(v) => setEditing({ ...editing, status: v })}
                >
                  <SelectTrigger data-testid="select-status-budget" aria-label={tc("status")}>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {/* A legacy value outside the vocabulary stays visible (never a blank select) but must be changed to save (#1346). */}
                    {editing.status && !normalizeBudgetStatus(editing.status) && (
                      <SelectItem value={editing.status} disabled>{editing.status}</SelectItem>
                    )}
                    {BUDGET_STATUSES.map((code) => (
                      <SelectItem key={code} value={code}>{t(budgetStatusLabelKey(code))}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending} data-testid="btn-save-budget">
                {isEditMode ? tc("update") : tc("create")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <Dialog open={summaryId !== null} onOpenChange={(v) => !v && setSummaryId(null)}>
        <DialogContent className="max-w-2xl" data-testid="dialog-summary-budget">
          <FormContainer>
            <FormHeader dialogTitle title={t("resumen.title")} />
            {isSummaryLoading && (
              <p className="text-sm text-muted-foreground">{tc("loading")}</p>
            )}
            {summaryError && (
              <p className="text-sm text-destructive" data-testid="summary-not-found">
                {summaryError instanceof ApiError && summaryError.status === 404
                  ? t("resumen.notFound")
                  : t("errorSave")}
              </p>
            )}
            {summary && (
              <>
                <FormSection title={t("resumen.gestionSection")}>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <p className="text-muted-foreground">{t("resumen.numeroGestion")}</p>
                      <p className="font-medium">{summary.numberManagement ?? "—"}</p>
                    </div>
                    <div>
                      <p className="text-muted-foreground">{t("resumen.encabezado")}</p>
                      <p className="font-medium">{summary.managementHeading ?? "—"}</p>
                    </div>
                    <div>
                      <p className="text-muted-foreground">{t("resumen.numeroPresupuesto")}</p>
                      <p className="font-medium">{summary.numberBudget}</p>
                    </div>
                    <div>
                      <p className="text-muted-foreground">{tc("amount")} {t("resumen.total").toLowerCase()}</p>
                      <p className="font-medium">{formatCurrency(summary.total)}</p>
                    </div>
                    <div>
                      <p className="text-muted-foreground">{t("resumen.saldo")}</p>
                      <p className="font-medium">{formatCurrency(summary.pendingBalance)}</p>
                    </div>
                  </div>
                </FormSection>
                <FormSection title={t("resumen.pagosSection")}>
                  {(summary.payments ?? []).length === 0 ? (
                    <p className="text-sm text-muted-foreground" data-testid="summary-empty-payments">
                      {t("resumen.noPagos")}
                    </p>
                  ) : (
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>{tc("id")}</TableHead>
                          <TableHead>{tc("amount")}</TableHead>
                          <TableHead>{tc("date")}</TableHead>
                          <TableHead>{tc("observations")}</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
{(summary.payments ?? []).map((payment) => (
                <TableRow key={payment.idPayment}>
                  <TableCell>{payment.idPayment}</TableCell>
                  <TableCell>{formatCurrency(payment.amount)}</TableCell>
                  <TableCell>{formatDate(payment.date)}</TableCell>
                  <TableCell>{payment.notes ?? "—"}</TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  )}
                </FormSection>
              </>
            )}
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setSummaryId(null)}>
                {tc("cancel")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <Dialog open={itemsBudgetId !== null} onOpenChange={(v) => !v && setItemsBudgetId(null)}>
        <DialogContent className="max-w-2xl" data-testid="dialog-items-budget">
          <FormContainer>
            <FormHeader dialogTitle title={t("items.title")} />

            <FormSection title={t("items.plantillaSection")}>
              <FormField
                label={t("items.selectTipoTramite")}
                helperText={procedureTypes.length === 0 ? t("items.noTiposTramite") : undefined}
              >
                <div className="flex gap-2">
                  <Select value={selectedProcedureTypeId} onValueChange={setSelectedTypeProcedureId}>
                    <SelectTrigger data-testid="select-procedure-type-items" disabled={procedureTypes.length === 0}>
                      <SelectValue placeholder={t("items.selectTipoTramite")} />
                    </SelectTrigger>
                    <SelectContent>
                      {procedureTypes.map((tt) => (
                        <SelectItem key={tt.idProcedureType} value={tt.idProcedureType!.toString()}>
                          {tt.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <Button
                    data-testid="btn-load-template"
                    disabled={!selectedProcedureTypeId || loadTemplateMutation.isPending}
                    onClick={handleLoadTemplate}
                  >
                    {t("items.cargarPlantilla")}
                  </Button>
                </div>
              </FormField>
            </FormSection>

            <FormSection title={t("items.catalogoSection")}>
              <FormField
                label={t("items.selectCatalogItem")}
                helperText={catalogItems.length === 0 ? t("items.noCatalogItems") : undefined}
              >
                <div className="flex gap-2">
                  <Select value={selectedCatalogItemId} onValueChange={setSelectedCatalogItemId}>
                    <SelectTrigger data-testid="select-catalog-item" disabled={catalogItems.length === 0}>
                      <SelectValue placeholder={t("items.selectCatalogItem")} />
                    </SelectTrigger>
                    <SelectContent>
                      {catalogItems.map((item) => (
                        <SelectItem key={item.idItem} value={item.idItem!.toString()}>
                          {item.name} — {formatCurrency(item.value)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <Button
                    data-testid="btn-add-from-catalog"
                    disabled={!selectedCatalogItemId || addFromCatalogMutation.isPending}
                    onClick={handleAddFromCatalog}
                  >
                    {t("items.agregarCatalogo")}
                  </Button>
                </div>
              </FormField>
            </FormSection>

            <FormSection title={t("items.itemsSection")}>
              {isItemsLoading ? (
                <p className="text-sm text-muted-foreground">{tc("loading")}</p>
              ) : budgetItems.length === 0 ? (
                <p className="text-sm text-muted-foreground" data-testid="items-empty-data">
                  {t("items.noData")}
                </p>
              ) : (
                <>
                  <Table data-testid="table-items-budget">
                    <TableHeader>
                      <TableRow>
                        <TableHead>{t("items.columns.nombre")}</TableHead>
                        <TableHead>{t("items.columns.valor")}</TableHead>
                        <TableHead>{t("items.columns.porcentaje")}</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {budgetItems.map((item) => (
                        <TableRow key={item.idItem}>
                          <TableCell>{item.name}</TableCell>
                          <TableCell>{formatCurrency(item.value)}</TableCell>
                          <TableCell>{item.percentage ?? 0}%</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                  <div className="flex justify-end pt-2 text-sm font-medium" data-testid="items-subtotal">
                    {t("items.subtotal")}: {formatCurrency(itemsSubtotal)}
                  </div>
                </>
              )}
            </FormSection>

            <FormActions align="right">
              <Button variant="secondary" onClick={() => setItemsBudgetId(null)}>
                {tc("cancel")}
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
