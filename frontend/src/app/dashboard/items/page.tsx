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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import {
  useItems,
  useCreateItem,
  useUpdateItem,
  useDeleteItem,
  useDescuentosYRecargos,
} from "@/hooks/useItems";
import { presentMutationError } from "@/lib/mutation-error";
import { formatCurrency } from "@/lib/utils";
import type { Item, TipoItem } from "@/types";

const EMPTY: Partial<Item> = {
  name: "",
  value: undefined,
  type: "NORMAL",
};

const TIPO_VALUES: TipoItem[] = ["NORMAL", "DESCUENTO", "RECARGO"];

export default function ItemsPage() {
  const t = useTranslations("items");
  const tc = useTranslations("common");
  const { data: items = [], isLoading } = useItems();
  const createMutation = useCreateItem();
  const updateMutation = useUpdateItem();
  const deleteMutation = useDeleteItem();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Item>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);
  const [presupuestoId, setPresupuestoId] = useState("");
  const [reporteId, setReporteId] = useState("");
  const [reporteQuery, setReporteQuery] = useState<number | undefined>(undefined);
  const { data: descuentosRecargos = [], isFetching: reporteLoading } =
    useDescuentosYRecargos(reporteQuery);

  function tipoLabel(tipo: TipoItem): string {
    return t(`types.${tipo}`);
  }

  function openCreate() {
    setEditing(EMPTY);
    setPresupuestoId("");
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(item: Item) {
    setEditing(item);
    setPresupuestoId(item.fkIdBudget?.idBudget?.toString() ?? "");
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    const type = editing.type ?? "NORMAL";
    if (type !== "NORMAL" && !editing.reason?.trim()) {
      toast.error(t("reasonRequired"));
      return;
    }
    const payload: Partial<Item> = {
      ...editing,
      type,
      fkIdBudget: presupuestoId ? { idBudget: Number(presupuestoId) } : undefined,
    };
    try {
      if (isEditMode && editing.idItem) {
        await updateMutation.mutateAsync({ id: editing.idItem, data: payload });
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

  const columns: Column<Item>[] = [
    {
      key: "id",
      header: tc("id"),
      render: (i) => <span className="text-xs text-muted-foreground">{i.idItem}</span>,
      className: "w-12",
    },
    {
      key: "nombre",
      header: t("fields.nombre"),
      render: (i) => i.name ?? "—",
    },
    {
      key: "valor",
      header: t("fields.valor"),
      render: (i) => <span className="font-medium">{formatCurrency(i.value)}</span>,
    },
    {
      key: "presupuesto",
      header: t("fields.presupuesto"),
      render: (i) => i.fkIdBudget?.idBudget ? `#${i.fkIdBudget.idBudget}` : "—",
    },
    {
      key: "tipo",
      header: t("fields.tipo"),
      render: (i) => tipoLabel(i.type ?? "NORMAL"),
    },
    {
      key: "actions",
      header: "",
      className: "w-24",
      render: (i) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(i)} aria-label={tc("edit")}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(i.idItem!)}
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
          <Button onClick={openCreate} data-testid="btn-nuevo-item">
            <Plus className="h-4 w-4" />
            {t("newItem")}
          </Button>
        }
      />

      <DataTable
        data={items}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(i) => i.idItem!}
        emptyMessage={t("noData")}
      />

      <FormContainer>
        <FormSection title={t("report.title")}>
          <div className="flex items-end gap-3">
            <FormField label={t("fields.presupuestoId")}>
              <Input
                type="number"
                value={reporteId}
                onChange={(e) => setReporteId(e.target.value)}
                placeholder={t("fields.presupuestoPlaceholder")}
                data-testid="input-reporte-presupuesto-id"
              />
            </FormField>
            <Button
              variant="secondary"
              onClick={() => setReporteQuery(reporteId ? Number(reporteId) : undefined)}
              data-testid="btn-consultar-reporte"
            >
              {t("report.consult")}
            </Button>
          </div>
          {reporteQuery && (
            <DataTable
              data={descuentosRecargos}
              isLoading={reporteLoading}
              keyExtractor={(i) => i.idItem!}
              emptyMessage={t("report.empty")}
              columns={[
                { key: "nombre", header: t("fields.nombre"), render: (i) => i.name ?? "—" },
                { key: "tipo", header: t("fields.tipo"), render: (i) => tipoLabel(i.type ?? "NORMAL") },
                { key: "motivo", header: t("fields.motivo"), render: (i) => i.reason ?? "—" },
                { key: "valor", header: t("fields.valor"), render: (i) => formatCurrency(i.value) },
              ]}
            />
          )}
        </FormSection>
      </FormContainer>

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={isEditMode ? t("editItem") : t("newItem")}>
              <FormField label={t("fields.presupuestoId")}>
                <Input
                  type="number"
                  value={presupuestoId}
                  onChange={(e) => setPresupuestoId(e.target.value)}
                  placeholder={t("fields.presupuestoPlaceholder")}
                  data-testid="input-presupuesto-id"
                />
              </FormField>
              <div className="grid grid-cols-2 gap-3">
                <FormField label={t("fields.nombre")}>
                  <Input
                    value={editing.name ?? ""}
                    onChange={(e) => setEditing({ ...editing, name: e.target.value })}
                    data-testid="input-cantidad"
                  />
                </FormField>
                <FormField label={t("fields.valorLabel")}>
                  <Input
                    type="number"
                    step="0.01"
                    value={editing.value ?? ""}
                    onChange={(e) => setEditing({ ...editing, value: parseFloat(e.target.value) })}
                    data-testid="input-precio"
                  />
                </FormField>
              </div>
              <FormField label={t("fields.tipoItem")}>
                <Select
                  value={editing.type ?? "NORMAL"}
                  onValueChange={(v) => setEditing({ ...editing, type: v as TipoItem })}
                >
                  <SelectTrigger data-testid="select-tipo-item">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {TIPO_VALUES.map((tipo) => (
                      <SelectItem key={tipo} value={tipo}>
                        {tipoLabel(tipo)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              {editing.type && editing.type !== "NORMAL" && (
                <FormField label={t("fields.motivo")} required>
                  <Input
                    value={editing.reason ?? ""}
                    onChange={(e) => setEditing({ ...editing, reason: e.target.value })}
                    placeholder={t("fields.motivoPlaceholder")}
                    data-testid="input-motivo"
                  />
                </FormField>
              )}
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button
                onClick={handleSave}
                disabled={createMutation.isPending || updateMutation.isPending}
                data-testid="btn-guardar-item"
              >
                {isEditMode ? tc("update") : tc("create")}
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
