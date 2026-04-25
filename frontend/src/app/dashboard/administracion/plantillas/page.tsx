"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, Trash2 } from "lucide-react";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { usePlantillas, useCreatePlantilla, useDeletePlantilla } from "@/hooks/usePlantillas";
import { useTiposTramite } from "@/hooks/useTiposTramite";
import { useConceptos } from "@/hooks/useConceptos";
import type { PlantillaPresupuesto } from "@/types";

/**
 * CU39, CU49, CU55 — Gestionar plantillas de presupuesto.
 * Each PlantillaPresupuesto links a TipoDeTramite with a Concepto (composite PK).
 */
const EMPTY = { idTipoTramite: "", idConcepto: "", observaciones: "" };

export default function PlantillasPage() {
  const { data: plantillas = [], isLoading } = usePlantillas();
  const { data: tiposTramite = [] } = useTiposTramite();
  const { data: conceptos = [] } = useConceptos();
  const createMutation = useCreatePlantilla();
  const deleteMutation = useDeletePlantilla();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<{
    idTipoTramite: number;
    idConcepto: number;
  } | null>(null);
  const [form, setForm] = useState(EMPTY);

  function openCreate() {
    setForm(EMPTY);
    setModalOpen(true);
  }

  async function handleSave() {
    if (!form.idTipoTramite || !form.idConcepto) {
      toast.error("Debe seleccionar tipo de trámite y concepto");
      return;
    }
    const payload: Partial<PlantillaPresupuesto> = {
      tipoDeTramite: { idTipoTramite: parseInt(form.idTipoTramite) },
      concepto: { idConcepto: parseInt(form.idConcepto) },
      observaciones: form.observaciones || undefined,
    };
    try {
      await createMutation.mutateAsync(payload);
      toast.success("Plantilla creada");
      setModalOpen(false);
    } catch {
      toast.error("Error al crear la plantilla — puede que ya exista");
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      await deleteMutation.mutateAsync(deleteTarget);
      toast.success("Plantilla eliminada");
    } catch {
      toast.error("Error al eliminar la plantilla");
    } finally {
      setDeleteTarget(null);
    }
  }

  const columns: Column<PlantillaPresupuesto>[] = [
    {
      key: "tipoTramite",
      header: "Tipo de Trámite",
      render: (p) => (
        <span className="font-medium">{p.tipoDeTramite?.nombre ?? "—"}</span>
      ),
    },
    {
      key: "concepto",
      header: "Concepto",
      render: (p) => p.concepto?.nombre ?? "—",
    },
    {
      key: "valor",
      header: "Valor base",
      render: (p) =>
        p.concepto?.valor != null
          ? `$${p.concepto.valor.toLocaleString("es-AR")}`
          : "—",
    },
    {
      key: "observaciones",
      header: "Observaciones",
      render: (p) => (
        <span className="text-sm text-muted-foreground">
          {p.observaciones ?? "—"}
        </span>
      ),
    },
    {
      key: "actions",
      header: "",
      className: "w-16",
      render: (p) => (
        <Button
          size="sm"
          variant="ghost"
          className="text-destructive hover:text-destructive"
          onClick={() =>
            setDeleteTarget({
              idTipoTramite: p.plantillaPresupuestoPK!.fkIdTipoTramite,
              idConcepto: p.plantillaPresupuestoPK!.fkIdConcepto,
            })
          }
        >
          <Trash2 className="h-4 w-4" />
        </Button>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title="Plantillas de Presupuesto"
        description="CU39, CU49, CU55 — Asociar conceptos de cobro a tipos de trámite"
        actions={
          <Button onClick={openCreate}>
            <Plus className="h-4 w-4" />
            Nueva plantilla
          </Button>
        }
      />

      <DataTable
        data={plantillas}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(p) =>
          `${p.plantillaPresupuestoPK?.fkIdTipoTramite}-${p.plantillaPresupuestoPK?.fkIdConcepto}`
        }
        emptyMessage="No hay plantillas de presupuesto registradas"
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Nueva plantilla de presupuesto</DialogTitle>
          </DialogHeader>
          <div className="space-y-3 pt-2">
            <div className="space-y-1">
              <Label>Tipo de trámite *</Label>
              <select
                className="w-full border border-input rounded-md px-3 py-2 text-sm bg-background"
                value={form.idTipoTramite}
                onChange={(e) =>
                  setForm({ ...form, idTipoTramite: e.target.value })
                }
              >
                <option value="">Seleccionar…</option>
                {tiposTramite.map((t) => (
                  <option key={t.idTipoTramite} value={t.idTipoTramite}>
                    {t.nombre}
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-1">
              <Label>Concepto *</Label>
              <select
                className="w-full border border-input rounded-md px-3 py-2 text-sm bg-background"
                value={form.idConcepto}
                onChange={(e) =>
                  setForm({ ...form, idConcepto: e.target.value })
                }
              >
                <option value="">Seleccionar…</option>
                {conceptos.map((c) => (
                  <option key={c.idConcepto} value={c.idConcepto}>
                    {c.nombre}{c.valor != null ? ` — $${c.valor}` : ""}
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-1">
              <Label>Observaciones</Label>
              <Input
                value={form.observaciones}
                onChange={(e) =>
                  setForm({ ...form, observaciones: e.target.value })
                }
                placeholder="Notas opcionales sobre esta plantilla"
              />
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <Button variant="outline" onClick={() => setModalOpen(false)}>
                Cancelar
              </Button>
              <Button
                onClick={handleSave}
                disabled={createMutation.isPending}
              >
                Crear plantilla
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={!!deleteTarget}
        onOpenChange={(v) => !v && setDeleteTarget(null)}
        onConfirm={handleDelete}
        loading={deleteMutation.isPending}
        description="Esto eliminará la asociación entre el tipo de trámite y el concepto."
      />
    </div>
  );
}
