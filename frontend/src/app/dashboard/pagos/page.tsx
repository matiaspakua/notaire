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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { usePagos, useCreatePago, useUpdatePago, useDeletePago, usePagoEstado } from "@/hooks/usePagos";
import { usePresupuestos, usePresupuestoResumen } from "@/hooks/usePresupuestos";
import { formatDate, formatCurrency } from "@/lib/utils";
import type { Pago } from "@/types";

const EMPTY: Partial<Pago> = { idPresupuesto: undefined, monto: undefined, fecha: "", metodoPago: "", observaciones: "" };

export default function PagosPage() {
  const t = useTranslations("pagos");
  const tc = useTranslations("common");

  const { data: pagos = [], isLoading } = usePagos();
  const { data: presupuestos = [] } = usePresupuestos();
  const createMutation = useCreatePago();
  const updateMutation = useUpdatePago();
  const deleteMutation = useDeletePago();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Pago>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  // Fetch saldo for selected presupuesto (Issue #796)
  const { data: resumen, isLoading: resumenLoading } = usePresupuestoResumen(
    editing.idPresupuesto || null
  );
  // Fetch estado de pago for selected presupuesto (Issue #821)
  const { data: estadoPago } = usePagoEstado(editing.idPresupuesto || null);

  const estadoPagoLabel: Record<string, string> = {
    SIN_PAGOS: t("estadoSinPagos"),
    PARCIAL: t("estadoParcial"),
    SALDADO: t("estadoSaldado"),
  };

  function openCreate() { setEditing(EMPTY); setIsEditMode(false); setModalOpen(true); }
  function openEdit(p: Pago) { setEditing(p); setIsEditMode(true); setModalOpen(true); }

  async function handleSave() {
    try {
      if (isEditMode && editing.idPago) {
        await updateMutation.mutateAsync({ id: editing.idPago, data: editing });
        toast.success(t("updated"));
      } else {
        await createMutation.mutateAsync(editing);
        toast.success(t("created"));
      }
      setModalOpen(false);
    } catch { toast.error(t("errorSave")); }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success(t("deleted"));
    } catch { toast.error(t("errorDelete")); }
    finally { setDeleteId(null); }
  }

  const columns: Column<Pago>[] = [
    { key: "id", header: tc("id"), render: (p) => <span className="text-xs text-muted-foreground">{p.idPago}</span>, className: "w-12" },
    { key: "presupuesto", header: "Presupuesto", render: (p) => <span className="text-xs text-muted-foreground">#{p.idPresupuesto ?? p.presupuesto?.idPresupuesto ?? "—"}</span>, className: "w-20" },
    { key: "fecha", header: tc("date"), render: (p) => formatDate(p.fecha) },
    { key: "monto", header: tc("amount"), render: (p) => <span className="font-medium">{formatCurrency(p.monto)}</span> },
    { key: "metodo", header: t("fields.metodoPago"), render: (p) => p.metodoPago ?? "—" },
    {
      key: "actions", header: "", className: "w-24",
      render: (p) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(p)}><Pencil className="h-4 w-4" /></Button>
          <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive" onClick={() => setDeleteId(p.idPago!)}><Trash2 className="h-4 w-4" /></Button>
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
      <DataTable data={pagos} columns={columns} isLoading={isLoading} keyExtractor={(p) => p.idPago!} emptyMessage={t("noData")} />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? t("editPago") : t("newPago")}>
              {/* Issue #796: Replace numeric ID input with presupuesto picker */}
              <FormField label="Presupuesto" required>
                <Select
                  value={editing.idPresupuesto?.toString() || ""}
                  onValueChange={(value) => setEditing({ ...editing, idPresupuesto: parseInt(value) })}
                >
                  <SelectTrigger data-testid="select-presupuesto-pago">
                    <SelectValue placeholder="Seleccionar presupuesto..." />
                  </SelectTrigger>
                  <SelectContent>
                    {presupuestos.length === 0 ? (
                      <div className="px-2 py-1.5 text-sm text-muted-foreground">No hay presupuestos disponibles</div>
                    ) : (
                      presupuestos.map((p) => (
                        <SelectItem key={p.idPresupuesto} value={p.idPresupuesto!.toString()}>
                          {p.persona ? `${p.persona.apellido}, ${p.persona.nombre} - $${p.monto}` : `Presupuesto #${p.idPresupuesto}`}
                        </SelectItem>
                      ))
                    )}
                  </SelectContent>
                </Select>
              </FormField>

              {/* Issue #796: Show saldo pendiente after selection */}
              {editing.idPresupuesto && (
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
                      <div className="text-lg font-semibold text-blue-900">
                        {formatCurrency(resumen.saldoPendiente || 0)}
                      </div>
                      <div className="text-xs text-muted-foreground pt-1">
                        Presupuestado: {formatCurrency(resumen.total || 0)} | Pagado: {formatCurrency((resumen.total || 0) - (resumen.saldoPendiente || 0))}
                      </div>
                    </div>
                  ) : (
                    <div className="text-sm text-red-600">No se pudo cargar el saldo. Intenta nuevamente.</div>
                  )}
                </div>
              )}

              <FormField label={tc("date")} required>
                <Input type="date" value={editing.fecha ?? ""} onChange={(e) => setEditing({ ...editing, fecha: e.target.value })} />
              </FormField>
              <FormField label={`${tc("amount")} ($)`} required>
                <Input type="number" step="0.01" value={editing.monto ?? ""} onChange={(e) => setEditing({ ...editing, monto: parseFloat(e.target.value) })} />
              </FormField>
              <FormField label={t("fields.metodoPago")} helperText={t("fields.metodoPlaceholder")}>
                <Input value={editing.metodoPago ?? ""} onChange={(e) => setEditing({ ...editing, metodoPago: e.target.value })} placeholder={t("methods.efectivo")} />
              </FormField>
              <FormField label={tc("observations")}>
                <Input value={editing.observaciones ?? ""} onChange={(e) => setEditing({ ...editing, observaciones: e.target.value })} />
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
