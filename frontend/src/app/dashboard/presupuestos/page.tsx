"use client";

import { useState, useMemo } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, Search, X } from "lucide-react";
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
import { Badge } from "@/components/ui/badge";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  usePresupuestos,
  useCreatePresupuesto,
  useUpdatePresupuesto,
  useDeletePresupuesto,
} from "@/hooks/usePresupuestos";
import { formatDate, formatCurrency } from "@/lib/utils";
import type { Presupuesto } from "@/types";

const ESTADOS = ["BORRADOR", "APROBADO", "RECHAZADO", "VENCIDO"] as const;
type Estado = (typeof ESTADOS)[number];

function estadoBadge(estado?: string) {
  switch (estado?.toUpperCase()) {
    case "APROBADO":
      return (
        <Badge className="bg-green-100 text-green-800 hover:bg-green-100">
          Aprobado
        </Badge>
      );
    case "RECHAZADO":
      return <Badge variant="destructive">Rechazado</Badge>;
    case "VENCIDO":
      return (
        <Badge className="bg-orange-100 text-orange-800 hover:bg-orange-100">
          Vencido
        </Badge>
      );
    default:
      return <Badge variant="outline">{estado ?? "Borrador"}</Badge>;
  }
}

const EMPTY: Partial<Presupuesto> = {
  fecha: "",
  monto: undefined,
  estado: "BORRADOR",
};

export default function PresupuestosPage() {
  const { data: presupuestos = [], isLoading } = usePresupuestos();
  const createMutation = useCreatePresupuesto();
  const updateMutation = useUpdatePresupuesto();
  const deleteMutation = useDeletePresupuesto();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Presupuesto>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  // Filter state (CU60 — Buscar Presupuesto)
  const [filterEstado, setFilterEstado] = useState<string>("TODOS");
  const [searchId, setSearchId] = useState("");

  const filtered = useMemo(() => {
    return presupuestos.filter((p) => {
      const matchEstado =
        filterEstado === "TODOS" ||
        p.estado?.toUpperCase() === filterEstado;
      const matchId =
        !searchId || p.idPresupuesto?.toString().includes(searchId);
      return matchEstado && matchId;
    });
  }, [presupuestos, filterEstado, searchId]);

  const hasFilter = filterEstado !== "TODOS" || searchId;

  function clearFilters() {
    setFilterEstado("TODOS");
    setSearchId("");
  }

  function openCreate() {
    setEditing(EMPTY);
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(p: Presupuesto) {
    setEditing(p);
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    try {
      if (isEditMode && editing.idPresupuesto) {
        await updateMutation.mutateAsync({
          id: editing.idPresupuesto,
          data: editing,
        });
        toast.success("Presupuesto actualizado");
      } else {
        await createMutation.mutateAsync(editing);
        toast.success("Presupuesto creado");
      }
      setModalOpen(false);
    } catch {
      toast.error("Error al guardar el presupuesto");
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Presupuesto eliminado");
    } catch {
      toast.error("Error al eliminar");
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<Presupuesto>[] = [
    {
      key: "id",
      header: "ID",
      render: (p) => (
        <span className="text-xs text-muted-foreground">{p.idPresupuesto}</span>
      ),
      className: "w-12",
    },
    {
      key: "fecha",
      header: "Fecha",
      render: (p) => formatDate(p.fecha),
    },
    {
      key: "monto",
      header: "Monto",
      render: (p) => (
        <span className="font-medium">{formatCurrency(p.monto)}</span>
      ),
    },
    {
      key: "estado",
      header: "Estado",
      render: (p) => estadoBadge(p.estado),
    },
    {
      key: "persona",
      header: "Cliente",
      render: (p) =>
        p.persona
          ? [p.persona.nombre, p.persona.apellido].filter(Boolean).join(" ")
          : "—",
    },
    {
      key: "items",
      header: "Ítems",
      render: (p) => (
        <span className="text-muted-foreground">
          {p.itemList?.length ?? 0}
        </span>
      ),
    },
    {
      key: "actions",
      header: "",
      className: "w-24",
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
    },
  ];

  return (
    <div>
      <AppHeader
        title="Presupuestos"
        description="CU01, CU39, CU45, CU49, CU55, CU60 — Preparar y gestionar presupuestos"
        actions={
          <Button onClick={openCreate} data-testid="btn-nuevo-presupuesto">
            <Plus className="h-4 w-4" />
            Nuevo presupuesto
          </Button>
        }
      />

      {/* Filter bar — CU60 */}
      <div className="flex flex-wrap items-center gap-3 px-4 py-3 border-b bg-muted/30">
        <div className="flex items-center gap-1 text-sm text-muted-foreground">
          <Search className="h-4 w-4" />
          <span>Filtrar:</span>
        </div>
        <Input
          placeholder="ID presupuesto"
          value={searchId}
          onChange={(e) => setSearchId(e.target.value)}
          className="h-8 w-36"
          data-testid="input-search-presupuesto"
        />
        <Select
          value={filterEstado}
          onValueChange={setFilterEstado}
        >
          <SelectTrigger className="h-8 w-40" data-testid="select-estado">
            <SelectValue placeholder="Estado" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TODOS">Todos los estados</SelectItem>
            {ESTADOS.map((e) => (
              <SelectItem key={e} value={e}>
                {e.charAt(0) + e.slice(1).toLowerCase()}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        {hasFilter && (
          <Button
            size="sm"
            variant="ghost"
            onClick={clearFilters}
            className="h-8 text-muted-foreground"
          >
            <X className="h-3 w-3 mr-1" />
            Limpiar
          </Button>
        )}
        <span className="text-xs text-muted-foreground ml-auto">
          {filtered.length} resultado{filtered.length !== 1 ? "s" : ""}
        </span>
      </div>

      <DataTable
        data={filtered}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(p) => p.idPresupuesto!}
        emptyMessage="No hay presupuestos"
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {isEditMode ? "Editar presupuesto" : "Nuevo presupuesto"}
            </DialogTitle>
          </DialogHeader>
          <div className="space-y-3 pt-2">
            <div className="space-y-1">
              <Label>Fecha</Label>
              <Input
                type="date"
                value={editing.fecha ?? ""}
                onChange={(e) =>
                  setEditing({ ...editing, fecha: e.target.value })
                }
              />
            </div>
            <div className="space-y-1">
              <Label>Monto ($)</Label>
              <Input
                type="number"
                step="0.01"
                value={editing.monto ?? ""}
                onChange={(e) =>
                  setEditing({ ...editing, monto: parseFloat(e.target.value) })
                }
                data-testid="input-monto"
              />
            </div>
            <div className="space-y-1">
              <Label>Estado</Label>
              <Select
                value={editing.estado ?? "BORRADOR"}
                onValueChange={(v) => setEditing({ ...editing, estado: v })}
              >
                <SelectTrigger data-testid="select-estado-form">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {ESTADOS.map((e) => (
                    <SelectItem key={e} value={e}>
                      {e.charAt(0) + e.slice(1).toLowerCase()}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <Button variant="outline" onClick={() => setModalOpen(false)}>
                Cancelar
              </Button>
              <Button
                onClick={handleSave}
                disabled={
                  createMutation.isPending || updateMutation.isPending
                }
              >
                {isEditMode ? "Actualizar" : "Crear"}
              </Button>
            </div>
          </div>
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
