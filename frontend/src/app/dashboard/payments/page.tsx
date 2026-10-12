"use client";

import { Suspense, useState } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, FileText } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { usePayments, usePaymentsByBudget, usePaymentsByDate, useBudgetBalance, useCreatePayment, useUpdatePayment, useDeletePayment, usePaymentStatus } from "@/hooks/usePayments";
import { useBudgetSummary } from "@/hooks/useBudgets";
import { BudgetPicker } from "@/components/shared/BudgetPicker";
import { usePaymentReceipt } from "@/hooks/useReports";
import { ApiError } from "@/lib/api-client";
import { presentMutationError } from "@/lib/mutation-error";
import type { Payment } from "@/types";
import { useDeleteError } from "@/hooks/useDeleteError";
import { toDateInputValue } from "@/lib/dates";
import { useFormat } from "@/hooks/useFormat";

const PAYMENT_FIELD_NAMES = ["amount", "date", "paymentMethod", "notes", "idBudget"];

const EMPTY: Partial<Payment> = { idBudget: undefined, amount: undefined, date: "", paymentMethod: "", notes: "" };

// useSearchParams (?budget=, #1544) needs a Suspense boundary for static prerendering.
export default function PaymentsPage() {
  return (
    <Suspense>
      <PaymentsPageContent />
    </Suspense>
  );
}

function PaymentsPageContent() {
  const fmt = useFormat();
  const t = useTranslations("pagos");
  const showDeleteError = useDeleteError();
  const tc = useTranslations("common");

  // CU47 (#1544): consult one budget, kept in the URL (?budget=).
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const budgetParam = Number(searchParams.get("budget"));
  const consultBudget = Number.isInteger(budgetParam) && budgetParam > 0 ? budgetParam : null;
  function updateQuery(key: string, value: string | undefined) {
    const params = new URLSearchParams(searchParams.toString());
    if (value) params.set(key, value);
    else params.delete(key);
    const qs = params.toString();
    router.replace(qs ? `${pathname}?${qs}` : pathname, { scroll: false });
  }
  function setConsultBudget(id: number | undefined) {
    updateQuery("budget", id ? String(id) : undefined);
  }
  // CU47 (#1557): inclusive date range, kept in the URL (?from=&to=). Local state so
  // typing isn't lost while the URL catches up.
  const ISO_DAY = /^\d{4}-\d{2}-\d{2}$/;
  const [from, setFrom] = useState(() => {
    const v = searchParams.get("from") ?? "";
    return ISO_DAY.test(v) ? v : "";
  });
  const [to, setTo] = useState(() => {
    const v = searchParams.get("to") ?? "";
    return ISO_DAY.test(v) ? v : "";
  });
  const rangeInvalid = !!from && !!to && from > to;
  const rangeActive = (!!from || !!to) && !rangeInvalid;
  // Server-side only with both days and no budget; otherwise narrow on the client.
  const serverRange = rangeActive && !consultBudget && !!from && !!to;
  const allPayments = usePayments();
  const byBudget = usePaymentsByBudget(consultBudget);
  const byDate = usePaymentsByDate(serverRange ? from : "", serverRange ? to : "");
  const { data: consultSummary } = useBudgetSummary(consultBudget);
  const { data: consultBalance } = useBudgetBalance(consultBudget);
  const { data: consultStatus } = usePaymentStatus(consultBudget);
  const inRange = (p: Payment) => {
    // Same calendar-day parsing as the Fecha column (#1557), not a raw slice.
    const day = toDateInputValue(p.date);
    return (!from || day >= from) && (!to || day <= to);
  };
  const source = consultBudget ? byBudget : serverRange ? byDate : allPayments;
  const rows = source.data ?? [];
  const filteredRows = rangeInvalid ? [] : rangeActive && !serverRange ? rows.filter(inRange) : rows;
  const payments = consultBudget
    ? [...filteredRows].sort((a, b) => String(b.date ?? "").localeCompare(String(a.date ?? "")))
    : filteredRows;
  const isLoading = source.isLoading;
  const createMutation = useCreatePayment();
  const updateMutation = useUpdatePayment();
  const deleteMutation = useDeletePayment();
  const paymentReceipt = usePaymentReceipt();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Payment>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  // Fetch the outstanding balance for the selected budget (Issue #796)
  const { data: summary, isLoading: summaryLoading } = useBudgetSummary(
    editing.idBudget || null
  );
  // Fetch the payment status for the selected budget (Issue #821)
  const { data: paymentStatus } = usePaymentStatus(editing.idBudget || null);

  const paymentStatusLabel: Record<string, string> = {
    NoPayments: t("estadoSinPagos"),
    PARTIAL: t("estadoParcial"),
    PAID: t("estadoSaldado"),
  };

  function openCreate() {
    setEditing(EMPTY);
    setIsEditMode(false);
    setFieldErrors({});
    setModalOpen(true);
  }
  function openEdit(p: Payment) {
    setEditing(p);
    setIsEditMode(true);
    setFieldErrors({});
    setModalOpen(true);
  }

  async function handleSave() {
    setFieldErrors({});
    try {
      if (isEditMode && editing.idPayment) {
        await updateMutation.mutateAsync({ id: editing.idPayment, data: editing });
        toast.success(t("updated"));
      } else {
        await createMutation.mutateAsync(editing);
        toast.success(t("created"));
      }
      setModalOpen(false);
    } catch (err) {
      presentMutationError(err, {
        fallback:
          err instanceof ApiError && err.status === 409 ? t("saldoExcedido") : t("errorSave"),
        fieldNames: PAYMENT_FIELD_NAMES,
        setFieldErrors,
      });
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

  async function handleIssueReceipt(idPayment: number) {
    try {
      await paymentReceipt.download(idPayment);
    } catch (err) {
      presentMutationError(err, { fallback: t("errorRecibo") });
    }
  }

  const columns: Column<Payment>[] = [
    { key: "id", header: tc("id"), render: (p) => <span className="text-xs text-muted-foreground">{p.idPayment}</span>, className: "w-12" },
    { key: "budget", header: t("fields.presupuesto"), render: (p) => <span className="text-xs text-muted-foreground">#{p.idBudget ?? p.fkIdBudget?.idBudget ?? "—"}</span>, className: "w-20" },
    { key: "date", header: tc("date"), render: (p) => fmt.date(p.date) },
    { key: "amount", header: tc("amount"), render: (p) => <span className="font-medium">{fmt.currency(p.amount)}</span> },
    { key: "method", header: t("fields.metodoPago"), render: (p) => p.paymentMethod ?? "—" },
    {
      key: "actions", header: "", className: "w-32",
      render: (p) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" title={t("emitirRecibo")} onClick={() => handleIssueReceipt(p.idPayment!)}><FileText className="h-4 w-4" /></Button>
          <Button size="sm" variant="ghost" onClick={() => openEdit(p)} aria-label={tc("edit")}><Pencil className="h-4 w-4" /></Button>
          <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive" onClick={() => setDeleteId(p.idPayment!)} aria-label={tc("delete")}><Trash2 className="h-4 w-4" /></Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title={t("title")}
        actions={<Button onClick={openCreate} data-testid="btn-new-payment"><Plus className="h-4 w-4" />{t("newPago")}</Button>}
      />
      <div className="mb-4 grid gap-3 md:grid-cols-[minmax(0,24rem)_1fr] md:items-start">
        <FormField label={t("consulta.label")}>
          <BudgetPicker
            value={consultBudget ?? undefined}
            onChange={(budget) => setConsultBudget(budget?.idBudget)}
            allowClear
            aria-label={t("consulta.label")}
            data-testid="filter-budget-payments"
          />
        </FormField>
        {consultBudget && (
          <dl
            data-testid="payment-budget-summary"
            className="grid grid-cols-2 gap-3 rounded-lg border border-info/30 bg-info/10 p-3 text-sm sm:grid-cols-4"
          >
            <div>
              <dt className="text-xs text-muted-foreground">{t("consulta.total")}</dt>
              <dd className="font-semibold" data-testid="payment-budget-total">{fmt.currency(consultSummary?.total ?? 0)}</dd>
            </div>
            <div>
              <dt className="text-xs text-muted-foreground">{t("consulta.paid")}</dt>
              <dd className="font-semibold" data-testid="payment-budget-paid">
                {fmt.currency((consultSummary?.total ?? 0) - (consultBalance ?? consultSummary?.pendingBalance ?? 0))}
              </dd>
            </div>
            <div>
              <dt className="text-xs text-muted-foreground">{t("consulta.balance")}</dt>
              <dd className="font-semibold" data-testid="payment-budget-balance">{fmt.currency(consultBalance ?? 0)}</dd>
            </div>
            <div>
              <dt className="text-xs text-muted-foreground">{t("consulta.status")}</dt>
              <dd>
                <span data-testid="payment-budget-status" className="rounded-full bg-info/10 px-2 py-0.5 text-xs font-medium text-info">
                  {consultStatus ? paymentStatusLabel[consultStatus] : "—"}
                </span>
              </dd>
            </div>
          </dl>
        )}
      </div>
      <div className="mb-4 flex flex-wrap items-center gap-3">
        <label className="flex items-center gap-2 text-sm text-muted-foreground">
          {t("consulta.desde")}
          <Input
            type="date"
            className="w-auto"
            data-testid="input-payments-from"
            aria-invalid={rangeInvalid}
            value={from}
            onChange={(e) => {
              setFrom(e.target.value);
              updateQuery("from", e.target.value || undefined);
            }}
          />
        </label>
        <label className="flex items-center gap-2 text-sm text-muted-foreground">
          {t("consulta.hasta")}
          <Input
            type="date"
            className="w-auto"
            data-testid="input-payments-to"
            aria-invalid={rangeInvalid}
            value={to}
            onChange={(e) => {
              setTo(e.target.value);
              updateQuery("to", e.target.value || undefined);
            }}
          />
        </label>
        {rangeInvalid && (
          <p role="alert" data-testid="payments-range-error" className="w-full text-sm text-destructive">
            {t("consulta.rangoInvalido")}
          </p>
        )}
      </div>
      <DataTable data={payments} columns={columns} isLoading={isLoading} keyExtractor={(p) => p.idPayment!} emptyMessage={consultBudget ? t("consulta.sinPagos") : t("noData")} />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={isEditMode ? t("editPago") : t("newPago")}>
              {/* Issue #796: Replace numeric ID input with budget picker */}
              <FormField label={t("fields.presupuesto")} required>
                {/* Server search instead of the first 1000 budgets (#1340). */}
                <BudgetPicker
                  value={editing.idBudget ?? undefined}
                  onChange={(budget) => setEditing({ ...editing, idBudget: budget?.idBudget })}
                  aria-label={t("fields.presupuesto")}
                  data-testid="select-budget-payment"
                />
              </FormField>

              {/* Issue #796: Show the outstanding balance after selection */}
              {editing.idBudget && (
                <div className="rounded-lg bg-info/10 p-3 border border-info/30">
                  {summaryLoading ? (
                    <div className="text-sm text-muted-foreground">{t("saldo.loading")}</div>
                  ) : summary ? (
                    <div className="space-y-1">
                      <div className="flex items-center justify-between">
                        <div className="text-sm text-muted-foreground">{t("saldo.pending")}</div>
                        {paymentStatus && (
                          <span
                            data-testid="status-payment-badge"
                            className="rounded-full bg-info/10 px-2 py-0.5 text-xs font-medium text-info"
                          >
                            {paymentStatusLabel[paymentStatus]}
                          </span>
                        )}
                      </div>
                      <div className="text-lg font-semibold text-foreground" data-testid="pending-balance-amount">
                        {fmt.currency(summary.pendingBalance || 0)}
                      </div>
                      <div className="text-xs text-muted-foreground pt-1">
                        {t("saldo.summary", {
                          budgeted: fmt.currency(summary.total || 0),
                          paid: fmt.currency((summary.total || 0) - (summary.pendingBalance || 0)),
                        })}
                      </div>
                    </div>
                  ) : (
                    <div className="text-sm text-destructive">{t("saldo.error")}</div>
                  )}
                </div>
              )}

              <FormField label={tc("date")} required error={fieldErrors.date}>
                <Input
                  type="date"
                  value={toDateInputValue(editing.date)}
                  onChange={(e) => setEditing({ ...editing, date: e.target.value })}
                  aria-invalid={!!fieldErrors.date}
                />
              </FormField>
              <FormField label={`${tc("amount")} ($)`} required error={fieldErrors.amount}>
                <Input
                  type="number"
                  step="0.01"
                  value={editing.amount ?? ""}
                  onChange={(e) => setEditing({ ...editing, amount: parseFloat(e.target.value) })}
                  aria-invalid={!!fieldErrors.amount}
                  data-testid="input-amount-payment"
                />
              </FormField>
              <FormField
                label={t("fields.metodoPago")}
                helperText={t("fields.metodoPlaceholder")}
                error={fieldErrors.paymentMethod}
              >
                <Input
                  value={editing.paymentMethod ?? ""}
                  onChange={(e) => setEditing({ ...editing, paymentMethod: e.target.value })}
                  placeholder={t("methods.efectivo")}
                  aria-invalid={!!fieldErrors.paymentMethod}
                />
              </FormField>
              <FormField label={tc("observations")} error={fieldErrors.notes}>
                <Input
                  value={editing.notes ?? ""}
                  onChange={(e) => setEditing({ ...editing, notes: e.target.value })}
                  aria-invalid={!!fieldErrors.notes}
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
    </div>
  );
}
