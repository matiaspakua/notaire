"use client";

import { useState, useMemo } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, Search, X, History, FileText } from "lucide-react";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  useGestiones,
  useCreateGestion,
  useUpdateGestion,
  useDeleteGestion,
} from "@/hooks/useGestiones";
import { useEstadosGestion } from "@/hooks/useEstadosGestion";
import { useReporteHistorialGestion } from "@/hooks/useReportes";
import { formatDate } from "@/lib/utils";
import type { GestionDeEscritura } from "@/types";

/** CU13 — Get latest estado from historialList */
function getEstadoActual(g: GestionDeEscritura): string | undefined {
  if (!g.historialList || g.historialList.length === 0) return undefined;
  const sorted = [...g.historialList].sort((a, b) => {
    const da = a.fecha ? new Date(a.fecha).getTime() : 0;
    const db = b.fecha ? new Date(b.fecha).getTime() : 0;
    return db - da;
  });
  return sorted[0]?.estado?.nombre;
}

function estadoBadge(estado?: string) {
  if (!estado) return <Badge variant="outline">Sin estado</Badge>;
  const lower = estado.toLowerCase();
  if (lower.includes("activ") || lower.includes("abiert"))
    return <Badge className="bg-green-100 text-green-800 hover:bg-green-100">{estado}</Badge>;
  if (lower.includes("cerr") || lower.includes("finaliz"))
    return <Badge variant="secondary">{estado}</Badge>;
  if (lower.includes("cancel") || lower.includes("archiv"))
    return <Badge variant="destructive">{estado}</Badge>;
  return <Badge variant="outline">{estado}</Badge>;
}

export default function GestionesPage() {
  const { data: gestiones = [], isLoading } = useGestiones();
  const { data: estadosGestion = [] } = useEstadosGestion();
  const createMutation = useCreateGestion();
  const updateMutation = useUpdateGestion();
  const deleteMutation = useDeleteGestion();
  const reporteHistorial = useReporteHistorialGestion();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<GestionDeEscritura | null>(null);
  const [numero, setNumero] = useState("");

  // Search/filter — CU02, CU53
  const [searchNumero, setSearchNumero] = useState("");
  const [searchEstado, setSearchEstado] = useState("");

  const filtered = useMemo(() => {
    return gestiones.filter((g) => {
      const matchNumero =
        !searchNumero || g.numero?.toString().includes(searchNumero);
      const matchEstado =
        !searchEstado ||
        getEstadoActual(g)?.toLowerCase().includes(searchEstado.toLowerCase());
      return matchNumero && matchEstado;
    });
  }, [gestiones, searchNumero, searchEstado]);

  function openCreate() {
    setEditing(null);
    setNumero("");
    setModalOpen(true);
  }

  function openEdit(g: GestionDeEscritura) {
    setEditing(g);
    setNumero(g.numero?.toString() ?? "");
    setModalOpen(true);
  }

  async function handleSave() {
    const data: Partial<GestionDeEscritura> = {
      numero: numero ? Number(numero) : undefined,
    };
    try {
      if (editing?.idGestion) {
        await updateMutation.mutateAsync({ id: editing.idGestion, data });
        toast.success("Gestión actualizada");
      } else {
        await createMutation.mutateAsync(data);
        toast.success("Gestión creada");
      }
      setModalOpen(false);
    } catch {
      toast.error("Error al guardar la gestión");
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Gestión eliminada");
    } catch {
      toast.error("Error al eliminar la gestión");
    } finally {
      setDeleteId(null);
    }
  }

  async function handleDownloadHistorial(g: GestionDeEscritura) {
    try {
      await reporteHistorial.download(g.idGestion!);
      toast.success("Historial descargado");
    } catch {
      toast.error("Error al descargar historial (backend requerido)");
    }
  }

  const columns: Column<GestionDeEscritura>[] = [
    {
      key: "id",
      header: "ID",
      render: (g) => (
        <span className="text-muted-foreground text-xs">{g.idGestion}</span>
      ),
      className: "w-16",
    },
    {
      key: "numero",
      header: "Número",
      render: (g) => (
        <span className="font-medium">{g.numero ?? "—"}</span>
      ),
    },
    {
      key: "estado",
      header: "Estado actual",
      render: (g) => estadoBadge(getEstadoActual(g)),
    },
    {
      key: "tramites",
      header: "Trámites",
      render: (g) => (
        <div className="flex items-center gap-1 text-sm">
          <FileText className="h-3.5 w-3.5 text-muted-foreground" />
          {g.tramiteList?.length ?? 0}
        </div>
      ),
    },
    {
      key: "historial",
      header: "Historial",
      render: (g) => (
        <div className="flex items-center gap-1 text-sm">
          <History className="h-3.5 w-3.5 text-muted-foreground" />
          {g.historialList?.length ?? 0} entradas
        </div>
      ),
    },
    {
      key: "actions",
      header: "",
      render: (g) => (
        <div className="flex gap-1 justify-end">
          <Button
            size="sm"
            variant="ghost"
            title="Descargar historial PDF"
            onClick={() => handleDownloadHistorial(g)}
            aria-label="Historial PDF"
          >
            <History className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            onClick={() => openEdit(g)}
            aria-label="Editar"
          >
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(g.idGestion!)}
            aria-label="Eliminar"
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
        title="Gestiones"
        description="Gestión de trámites y escrituras (CU02, CU13–CU16, CU19, CU53)"
        actions={
          <Button onClick={openCreate} data-testid="btn-nueva-gestion">
            <Plus className="h-4 w-4" />
            Nueva gestión
          </Button>
        }
      />

      {/* Search / filter bar */}
      <div className="flex flex-wrap items-center gap-3 px-4 py-3 border-b bg-muted/30">
        <div className="flex items-center gap-1 text-sm text-muted-foreground">
          <Search className="h-4 w-4" />
          <span>Filtrar:</span>
        </div>
        <Input
          placeholder="Número de gestión"
          value={searchNumero}
          onChange={(e) => setSearchNumero(e.target.value)}
          className="h-8 w-44"
          data-testid="input-search-numero"
        />
        <Input
          placeholder="Estado"
          value={searchEstado}
          onChange={(e) => setSearchEstado(e.target.value)}
          className="h-8 w-36"
          data-testid="input-search-estado"
        />
        {(searchNumero || searchEstado) && (
          <Button
            size="sm"
            variant="ghost"
            onClick={() => { setSearchNumero(""); setSearchEstado(""); }}
            className="h-8 text-muted-foreground"
          >
            <X className="h-3 w-3 mr-1" />
            Limpiar
          </Button>
        )}
        <span className="text-xs text-muted-foreground ml-auto">
          {filtered.length} gestión{filtered.length !== 1 ? "es" : ""}
        </span>
      </div>

      <DataTable
        data={filtered}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(g) => g.idGestion!}
        emptyMessage="No hay gestiones registradas"
      />

      {/* Create / Edit Modal */}
      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {editing ? "Editar gestión" : "Nueva gestión"}
            </DialogTitle>
          </DialogHeader>
          <div className="space-y-4 pt-2">
            <div className="space-y-1.5">
              <Label>Número de gestión</Label>
              <Input
                type="number"
                value={numero}
                onChange={(e) => setNumero(e.target.value)}
                placeholder="Ej: 1001"
                data-testid="input-numero-gestion"
              />
            </div>
            {estadosGestion.length > 0 && (
              <div className="space-y-1.5">
                <Label className="text-xs text-muted-foreground">
                  Estados de gestión disponibles: {estadosGestion.map((e) => e.nombre).join(", ")}
                </Label>
              </div>
            )}
            <div className="flex justify-end gap-2 pt-2">
              <Button variant="outline" onClick={() => setModalOpen(false)}>
                Cancelar
              </Button>
              <Button
                onClick={handleSave}
                disabled={createMutation.isPending || updateMutation.isPending}
                data-testid="btn-guardar-gestion"
              >
                {editing ? "Actualizar" : "Crear"}
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
