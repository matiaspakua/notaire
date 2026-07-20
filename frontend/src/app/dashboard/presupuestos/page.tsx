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
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import {
  usePresupuestos,
  useCreatePresupuesto,
  useUpdatePresupuesto,
  useDeletePresupuesto,
} from "@/hooks/usePresupuestos";
import { usePersonas } from "@/hooks/usePersonas";
import { formatDate, formatCurrency, fullName } from "@/lib/utils";
import type { Presupuesto } from "@/types";

const EMPTY: Partial<Presupuesto> = { fecha: "", monto: undefined, estado: "BORRADOR" };

export default function PresupuestosPage() {
  const t = useTranslations("presupuestos");
  const tc = useTranslations("common");

  const { data: presupuestos = [], isLoading } = usePresupuestos();
  const { data: personas = [] } = usePersonas();
  const createMutation = useCreatePresupuesto();
  const updateMutation = useUpdatePresupuesto();
  const deleteMutation = useDeletePresupuesto();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Presupuesto>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  const [searchPresupuesto, setSearchPresupuesto] = useState("");
  const [filterEstado, setFilterEstado] = useState<string>("TODOS");

  const { data: byEstado = presupuestos } = useQuery({
    queryKey: ["presupuestos", "buscar", filterEstado, presupuestos],
    queryFn: () =>
      filterEstado !== "TODOS"
        ? apiGet<Presupuesto[]>(`/presupuestos/buscar?estado=${encodeURIComponent(filterEstado)}`)
        : Promise.resolve(presupuestos),
  });

  const filteredPresupuestos = byEstado.filter((p) => {
    if (searchPresupuesto) {
      const q = searchPresupuesto.toLowerCase();
      const matchId = p.idPresupuesto?.toString().includes(q);
      const matchPersona = p.persona ? (p.persona.nombre + " " + p.persona.apellido).toLowerCase().includes(q) : false;
      if (!matchId && !matchPersona) return false;
    }
    return true;
  });

  function openCreate() { setEditing(EMPTY); setIsEditMode(false); setModalOpen(true); }
  function openEdit(p: Presupuesto) { setEditing(p); setIsEditMode(true); setModalOpen(true); }

  async function handleSave() {
    try {
      if (isEditMode && editing.idPresupuesto) {
        await updateMutation.mutateAsync({ id: editing.idPresupuesto, data: editing });
        toast.success(t("updated"));
      } else {
        await createMutation.mutateAsync(editing);
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

  const columns: Column<Presupuesto>[] = [
    {
      key: "id",
      header: tc("id"),
      render: (p) => <span className="text-xs text-muted-foreground">{p.idPresupuesto}</span>,
      className: "w-12",
    },
    {
      key: "fecha",
      header: tc("date"),
      render: (p) => formatDate(p.fecha),
    },
    {
      key: "persona",
      header: t("fields.cliente"),
      render: (p) => p.persona ? fullName(p.persona) : <span className="text-muted-foreground">—</span>,
    },
    {
      key: "monto",
      header: tc("amount"),
      render: (p) => <span className="font-medium">{formatCurrency(p.monto)}</span>,
    },
    {
      key: "estado",
      header: tc("status"),
      render: (p) => p.estado ?? "—",
    },
    {
      key: "actions",
      header: "",
      render: (p) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(p)}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(p.idPresupuesto!)}
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
          <Button onClick={openCreate} data-testid="btn-nuevo-presupuesto">
            <Plus className="h-4 w-4" />
            {t("newPresupuesto")}
          </Button>
        }
      />

      <div className="flex flex-wrap gap-3 px-4 pb-4">
        <Input
          placeholder={t("searchPlaceholder")}
          value={searchPresupuesto}
          onChange={(e) => setSearchPresupuesto(e.target.value)}
          data-testid="input-search-presupuesto"
          className="w-52"
        />
        <Select value={filterEstado} onValueChange={setFilterEstado}>
          <SelectTrigger data-testid="select-estado" className="w-44">
            <SelectValue placeholder={`${tc("status")}...`} />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TODOS">Todos</SelectItem>
            <SelectItem value="BORRADOR">Borrador</SelectItem>
            <SelectItem value="APROBADO">Aprobado</SelectItem>
            <SelectItem value="RECHAZADO">Rechazado</SelectItem>
            <SelectItem value="FACTURADO">Facturado</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <DataTable
        data={filteredPresupuestos}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(p) => p.idPresupuesto!}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? t("editPresupuesto") : t("newPresupuesto")}>
              <FormField
                label={t("fields.cliente")}
                required
                helperText={personas.length === 0 ? "No hay personas registradas. Primero registre una persona." : undefined}
              >
                <Select
                  value={editing.persona?.idPersona?.toString() ?? ""}
                  onValueChange={(v) => {
                    const persona = personas.find((p) => p.idPersona?.toString() === v);
                    setEditing({ ...editing, persona });
                  }}
                >
                  <SelectTrigger data-testid="select-persona" disabled={personas.length === 0}>
                    <SelectValue placeholder="Seleccionar cliente..." />
                  </SelectTrigger>
                  <SelectContent>
                    {personas.map((p) => (
                      <SelectItem key={p.idPersona} value={p.idPersona!.toString()}>
                        {fullName(p)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label={tc("date")} required>
                <Input
                  type="date"
                  value={editing.fecha ?? ""}
                  onChange={(e) => setEditing({ ...editing, fecha: e.target.value })}
                />
              </FormField>
              <FormField label={`${tc("amount")} ($)`} required>
                <Input
                  type="number"
                  step="0.01"
                  value={editing.monto ?? ""}
                  onChange={(e) => setEditing({ ...editing, monto: parseFloat(e.target.value) })}
                  data-testid="input-monto"
                />
              </FormField>
              <FormField label={tc("status")}>
                <Select
                  value={editing.estado ?? "BORRADOR"}
                  onValueChange={(v) => setEditing({ ...editing, estado: v })}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="BORRADOR">Borrador</SelectItem>
                    <SelectItem value="APROBADO">Aprobado</SelectItem>
                    <SelectItem value="RECHAZADO">Rechazado</SelectItem>
                    <SelectItem value="FACTURADO">Facturado</SelectItem>
                  </SelectContent>
                </Select>
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>
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
