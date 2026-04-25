"use client";

import { useState, useMemo } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, Search, X, Users, User } from "lucide-react";
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
  usePersonas,
  useCreatePersona,
  useUpdatePersona,
  useDeletePersona,
} from "@/hooks/usePersonas";
import type { Persona } from "@/types";

const EMPTY: Partial<Persona> = {
  nombre: "",
  apellido: "",
  dni: "",
  cuil: "",
  email: "",
  telefono: "",
  domicilio: "",
  esCliente: false,
};

export default function PersonasPage() {
  const { data: personas = [], isLoading } = usePersonas();
  const createMutation = useCreatePersona();
  const updateMutation = useUpdatePersona();
  const deleteMutation = useDeletePersona();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Persona>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);

  // Search state (CU61 — client-side filter on loaded data)
  const [searchNombre, setSearchNombre] = useState("");
  const [searchApellido, setSearchApellido] = useState("");
  const [searchDni, setSearchDni] = useState("");
  const [filterClientes, setFilterClientes] = useState<boolean | null>(null);

  const filteredPersonas = useMemo(() => {
    return personas.filter((p) => {
      const matchNombre =
        !searchNombre ||
        p.nombre?.toLowerCase().includes(searchNombre.toLowerCase());
      const matchApellido =
        !searchApellido ||
        p.apellido?.toLowerCase().includes(searchApellido.toLowerCase());
      const matchDni =
        !searchDni || p.dni?.includes(searchDni) || p.cuil?.includes(searchDni);
      const matchCliente =
        filterClientes === null || p.esCliente === filterClientes;
      return matchNombre && matchApellido && matchDni && matchCliente;
    });
  }, [personas, searchNombre, searchApellido, searchDni, filterClientes]);

  const hasActiveFilter =
    searchNombre || searchApellido || searchDni || filterClientes !== null;

  function clearFilters() {
    setSearchNombre("");
    setSearchApellido("");
    setSearchDni("");
    setFilterClientes(null);
  }

  function openCreate() {
    setEditing(EMPTY);
    setIsEditMode(false);
    setModalOpen(true);
  }

  function openEdit(p: Persona) {
    setEditing(p);
    setIsEditMode(true);
    setModalOpen(true);
  }

  async function handleSave() {
    try {
      if (isEditMode && editing.idPersona) {
        await updateMutation.mutateAsync({
          id: editing.idPersona,
          data: editing,
        });
        toast.success("Persona actualizada");
      } else {
        await createMutation.mutateAsync(editing);
        toast.success("Persona registrada");
      }
      setModalOpen(false);
    } catch {
      toast.error("Error al guardar la persona");
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Persona eliminada");
    } catch {
      toast.error("Error al eliminar");
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<Persona>[] = [
    {
      key: "id",
      header: "ID",
      render: (p) => (
        <span className="text-xs text-muted-foreground">{p.idPersona}</span>
      ),
      className: "w-12",
    },
    {
      key: "nombre",
      header: "Nombre",
      render: (p) => (
        <span className="font-medium">
          {[p.nombre, p.apellido].filter(Boolean).join(" ") || "—"}
        </span>
      ),
    },
    {
      key: "dni",
      header: "DNI / CUIL",
      render: (p) => (
        <div className="text-sm">
          <div>{p.dni ?? "—"}</div>
          {p.cuil && (
            <div className="text-xs text-muted-foreground">{p.cuil}</div>
          )}
        </div>
      ),
    },
    {
      key: "contacto",
      header: "Contacto",
      render: (p) => (
        <div className="text-sm">
          <div>{p.email ?? "—"}</div>
          {p.telefono && (
            <div className="text-xs text-muted-foreground">{p.telefono}</div>
          )}
        </div>
      ),
    },
    {
      key: "tipo",
      header: "Tipo",
      render: (p) =>
        p.esCliente ? (
          <Badge variant="secondary" className="gap-1">
            <User className="h-3 w-3" />
            Cliente
          </Badge>
        ) : (
          <Badge variant="outline">
            <Users className="h-3 w-3 mr-1" />
            Persona
          </Badge>
        ),
    },
    {
      key: "actions",
      header: "",
      className: "w-24",
      render: (p) => (
        <div className="flex gap-2 justify-end">
          <Button
            size="sm"
            variant="ghost"
            onClick={() => openEdit(p)}
            aria-label="Editar"
          >
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(p.idPersona!)}
            aria-label="Eliminar"
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
        title="Personas"
        description="CU17, CU18, CU21, CU41, CU46, CU48, CU51, CU54, CU61 — Gestión de personas y clientes"
        actions={
          <Button onClick={openCreate} data-testid="btn-nueva-persona">
            <Plus className="h-4 w-4" />
            Nueva persona
          </Button>
        }
      />

      {/* Search bar — CU61 */}
      <div
        className="flex flex-wrap items-end gap-3 px-4 py-3 border-b bg-muted/30"
        data-testid="search-bar"
      >
        <div className="flex items-center gap-1 text-sm text-muted-foreground">
          <Search className="h-4 w-4" />
          <span>Buscar:</span>
        </div>
        <Input
          placeholder="Nombre"
          value={searchNombre}
          onChange={(e) => setSearchNombre(e.target.value)}
          className="h-8 w-36"
          data-testid="input-search-nombre"
        />
        <Input
          placeholder="Apellido"
          value={searchApellido}
          onChange={(e) => setSearchApellido(e.target.value)}
          className="h-8 w-36"
          data-testid="input-search-apellido"
        />
        <Input
          placeholder="DNI / CUIL"
          value={searchDni}
          onChange={(e) => setSearchDni(e.target.value)}
          className="h-8 w-40"
          data-testid="input-search-dni"
        />
        <div className="flex gap-2">
          <Button
            size="sm"
            variant={filterClientes === null ? "outline" : "secondary"}
            onClick={() =>
              setFilterClientes(filterClientes === null ? null : null)
            }
            className="h-8"
          >
            Todos
          </Button>
          <Button
            size="sm"
            variant={filterClientes === true ? "default" : "outline"}
            onClick={() =>
              setFilterClientes(filterClientes === true ? null : true)
            }
            className="h-8"
            data-testid="btn-filter-clientes"
          >
            Solo clientes
          </Button>
        </div>
        {hasActiveFilter && (
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
          {filteredPersonas.length} resultado
          {filteredPersonas.length !== 1 ? "s" : ""}
        </span>
      </div>

      <DataTable
        data={filteredPersonas}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(p) => p.idPersona!}
        emptyMessage="No se encontraron personas"
      />

      {/* Create / Edit Modal */}
      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>
              {isEditMode ? "Editar persona" : "Nueva persona"}
            </DialogTitle>
          </DialogHeader>
          <div className="grid grid-cols-2 gap-3 pt-2">
            <div className="space-y-1">
              <Label>Nombre</Label>
              <Input
                value={editing.nombre ?? ""}
                onChange={(e) =>
                  setEditing({ ...editing, nombre: e.target.value })
                }
                placeholder="Juan"
                data-testid="input-nombre"
              />
            </div>
            <div className="space-y-1">
              <Label>Apellido</Label>
              <Input
                value={editing.apellido ?? ""}
                onChange={(e) =>
                  setEditing({ ...editing, apellido: e.target.value })
                }
                placeholder="García"
                data-testid="input-apellido"
              />
            </div>
            <div className="space-y-1">
              <Label>DNI</Label>
              <Input
                value={editing.dni ?? ""}
                onChange={(e) =>
                  setEditing({ ...editing, dni: e.target.value })
                }
                placeholder="12345678"
              />
            </div>
            <div className="space-y-1">
              <Label>CUIL</Label>
              <Input
                value={editing.cuil ?? ""}
                onChange={(e) =>
                  setEditing({ ...editing, cuil: e.target.value })
                }
                placeholder="20-12345678-5"
              />
            </div>
            <div className="col-span-2 space-y-1">
              <Label>Email</Label>
              <Input
                type="email"
                value={editing.email ?? ""}
                onChange={(e) =>
                  setEditing({ ...editing, email: e.target.value })
                }
                placeholder="juan@ejemplo.com"
              />
            </div>
            <div className="space-y-1">
              <Label>Teléfono</Label>
              <Input
                value={editing.telefono ?? ""}
                onChange={(e) =>
                  setEditing({ ...editing, telefono: e.target.value })
                }
                placeholder="+54 9 261 000 0000"
              />
            </div>
            <div className="space-y-1">
              <Label>Domicilio</Label>
              <Input
                value={editing.domicilio ?? ""}
                onChange={(e) =>
                  setEditing({ ...editing, domicilio: e.target.value })
                }
                placeholder="Av. San Martín 123"
              />
            </div>
            <div className="col-span-2 flex items-center gap-2">
              <input
                type="checkbox"
                id="esCliente"
                checked={editing.esCliente ?? false}
                onChange={(e) =>
                  setEditing({ ...editing, esCliente: e.target.checked })
                }
                className="h-4 w-4"
                data-testid="check-es-cliente"
              />
              <Label htmlFor="esCliente">Es cliente</Label>
            </div>
            <div className="col-span-2 flex justify-end gap-2 pt-2">
              <Button variant="outline" onClick={() => setModalOpen(false)}>
                Cancelar
              </Button>
              <Button
                onClick={handleSave}
                disabled={
                  createMutation.isPending || updateMutation.isPending
                }
                data-testid="btn-guardar-persona"
              >
                {isEditMode ? "Actualizar" : "Guardar"}
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
