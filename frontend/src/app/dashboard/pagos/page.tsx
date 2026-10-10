"use client";

import { useState } from "react";
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { usePagos, useCreatePago, useUpdatePago, useDeletePago, usePagoEstado } from "@/hooks/usePagos";
import { usePresupuestos, usePresupuestoResumen } from "@/hooks/usePresupuestos";
import { useReciboPago } from "@/hooks/useReportes";
import { ApiError } from "@/lib/api-client";
import { presentMutationError } from "@/lib/mutation-error";
import { formatDate, formatCurrency } from "@/lib/utils";
import type { Pago } from "@/types";
import { toDateInputValue } from "@/lib/dates";

const PAGO_FIELD_NAMES = ["amount", "date", "paymentMethod", "notes", "idBudget"];

const EMPTY: Partial<Pago> = { idBudget: undefined, amount: undefined, date: "", paymentMethod: "", notes: "" };

export default function PagosPage() {
  const t = useTranslations("pagos");
  const tc = useTranslations("common");

  const { data: pagos = [], isLoading } = usePagos();
  const { data: presupuestos = [] } = usePresupuestos();
  const createMutation = useCreatePago();
  const updateMutation = useUpdatePago();
  const deleteMutation = useDeletePago();
  const reciboPago = useReciboPago();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Pago>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  // Fetch saldo for selected presupuesto (Issue #796)
  const { data: resumen, isLoading: resumenLoading } = usePresupuestoResumen(
    editing.idBudget || null
  );
  // Fetch estado de pago for selected presupuesto (Issue #821)
  const { data: estadoPago } = usePagoEstado(editing.idBudget || null);

  const estadoPagoLabel: Record<string, string> = {
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
  function openEdit(p: Pago) {
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
        fieldNames: PAGO_FIELD_NAMES,
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
      presentMutationError(err, { fallback: t("errorDelete") });
    } finally {
      setDeleteId(null);
    }
  }

  async function handleEmitirRecibo(idPago: number) {
    try {
      await reciboPago.download(idPago);
    } catch (err) {
      presentMutationError(err, { fallback: t("errorRecibo") });
    }
  }

  const columns: Column<Pago>[] = [
    { key: "id", header: tc("id"), render: (p) => <span className="text-xs text-muted-foreground">{p.idPayment}</span>, className: "w-12" },
    { key: "presupuesto", header: "Presupuesto", render: (p) => <span className="text-xs text-muted-foreground">#{p.idBudget ?? p.fkIdBudget?.idBudget ?? "—"}</span>, className: "w-20" },
    { key: "fecha", header: tc("date"), render: (p) => formatDate(p.date) },
    { key: "monto", header: tc("amount"), render: (p) => <span className="font-medium">{formatCurrency(p.amount)}</span> },
    { key: "metodo", header: t("fields.metodoPago"), render: (p) => p.paymentMethod ?? "—" },
    {
      key: "actions", header: "", className: "w-32",
      render: (p) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" title={t("emitirRecibo")} onClick={() => handleEmitirRecibo(p.idPayment!)}><FileText className="h-4 w-4" /></Button>
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
        actions={<Button onClick={openCreate} data-testid="btn-nuevo-pago"><Plus className="h-4 w-4" />{t("newPago")}</Button>}
      />
      <DataTable data={pagos} columns={columns} isLoading={isLoading} keyExtractor={(p) => p.idPayment!} emptyMessage={t("noData")} />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? t("editPago") : t("newPago")}>
              {/* Issue #796: Replace numeric ID input with presupuesto picker */}
              <FormField label="Presupuesto" required>
                <Select
                  value={editing.idBudget?.toString() || ""}
                  onValueChange={(value) => setEditing({ ...editing, idBudget: parseInt(value) })}
                >
                  <SelectTrigger data-testid="select-presupuesto-pago">
                    <SelectValue placeholder="Seleccionar presupuesto..." />
                  </SelectTrigger>
                  <SelectContent>
                    {presupuestos.length === 0 ? (
                      <div className="px-2 py-1.5 text-sm text-muted-foreground">No hay presupuestos disponibles</div>
                    ) : (
                      presupuestos.map((p) => (
                        <SelectItem key={p.idBudget} value={p.idBudget!.toString()}>
                          {p.person ? `${p.person.lastName}, ${p.person.name} - $${p.propertyAmount}` : `Presupuesto #${p.idBudget}`}
                        </SelectItem>
                      ))
                    )}
                  </SelectContent>
                </Select>
              </FormField>

              {/* Issue #796: Show saldo pendiente after selection */}
              {editing.idBudget && (
                <div className="rounded-lg bg-blue-50 p-3 border border-blue-200">
                  {resumenLoading ? (
                    <div className="text-sm text-muted-foreground">Cargando saldo...</div>
                  ) : resumen ? (
                    <div className="space-y-1">
                      <div className="flex items-center justify-between">
                        <div className="text-sm text-muted-foreground">Saldo Pendiente</div>
                        {estadoPago && (
                          <span
                            data-testid="estado-pago-badge"
                            className="rounded-full bg-blue-100 px-2 py-0.5 text-xs font-medium text-blue-800"
                          >
                            {estadoPagoLabel[estadoPago]}
                          </span>
                        )}
                      </div>
                      <div className="text-lg font-semibold text-blue-900" data-testid="saldo-pendiente-amount">
                        {formatCurrency(resumen.pendingBalance || 0)}
                      </div>
                      <div className="text-xs text-muted-foreground pt-1">
                        Presupuestado: {formatCurrency(resumen.total || 0)} | Pagado: {formatCurrency((resumen.total || 0) - (resumen.pendingBalance || 0))}
                      </div>
                    </div>
                  ) : (
                    <div className="text-sm text-red-600">No se pudo cargar el saldo. Intenta nuevamente.</div>
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
                  data-testid="input-monto-pago"
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
