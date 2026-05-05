"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, Building2 } from "lucide-react";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  useInmuebles,
  useCreateInmueble,
  useUpdateInmueble,
  useDeleteInmueble,
} from "@/hooks/useInmuebles";
import type { Inmueble } from "@/types";

export default function InmueblesPage() {
  const { data: inmuebles = [], isLoading } = useInmuebles();
  const createMutation = useCreateInmueble();
  const updateMutation = useUpdateInmueble();
  const deleteMutation = useDeleteInmueble();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Inmueble | null>(null);
  const [form, setForm] = useState({
    nomenclaturaCatastral: "",
    domicilio: "",
    valuacionFiscal: "",
    observaciones: "",
  });

  function openCreate() {
    setEditing(null);
    setForm({ nomenclaturaCatastral: "", domicilio: "", valuacionFiscal: "", observaciones: "" });
    setModalOpen(true);
  }

  function openEdit(i: Inmueble) {
    setEditing(i);
    setForm({
      nomenclaturaCatastral: i.nomenclaturaCatastral ?? "",
      domicilio: i.domicilio ?? "",
      valuacionFiscal: i.valuacionFiscal ?? "",
      observaciones: i.observaciones ?? "",
    });
    setModalOpen(true);
  }

  async function handleSave() {
    try {
      if (editing?.idInmueble) {
        await updateMutation.mutateAsync({ id: editing.idInmueble, data: form });
        toast.success("Inmueble actualizado");
      } else {
        await createMutation.mutateAsync(form);
        toast.success("Inmueble creado");
      }
      setModalOpen(false);
    } catch {
      toast.error("Error al guardar el inmueble");
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    try {
      await deleteMutation.mutateAsync(deleteId);
      toast.success("Inmueble eliminado");
    } catch {
      toast.error("Error al eliminar el inmueble");
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<Inmueble>[] = [
    {
      key: "id",
      header: "ID",
      render: (i) => <span className="text-muted-foreground text-xs">{i.idInmueble}</span>,
      className: "w-16",
    },
    {
      key: "nomenclatura",
      header: "Nomenclatura Catastral",
      render: (i) => <span className="font-medium">{i.nomenclaturaCatastral ?? "—"}</span>,
    },
    {
      key: "domicilio",
      header: "Domicilio",
      render: (i) => i.domicilio ?? "—",
    },
    {
      key: "valuacion",
      header: "Valuación Fiscal",
      render: (i) => i.valuacionFiscal ?? "—",
      className: "w-40",
    },
    {
      key: "actions",
      header: "",
      render: (i) => (
        <div className="flex gap-1 justify-end">
          <Button size="icon" variant="ghost" onClick={() => openEdit(i)} aria-label="Editar">
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="icon"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(i.idInmueble!)}
            aria-label="Eliminar"
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
        title="Inmuebles"
        description="Gestión de propiedades (CU69)"
        actions={
          <Button onClick={openCreate}>
            <Plus className="h-4 w-4" />
            Nuevo inmueble
          </Button>
        }
      />

      <DataTable
        data={inmuebles}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(i) => i.idInmueble!}
        emptyMessage="No hay inmuebles registrados"
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Building2 className="h-5 w-5 text-primary" />
              {editing ? "Editar inmueble" : "Nuevo inmueble"}
            </DialogTitle>
          </DialogHeader>
          <div className="space-y-4 pt-2">
            <div className="space-y-1.5">
              <Label>Nomenclatura Catastral</Label>
              <Input
                value={form.nomenclaturaCatastral}
                onChange={(e) => setForm({ ...form, nomenclaturaCatastral: e.target.value })}
                placeholder="Ej: 01-02-03-04-05"
              />
            </div>
            <div className="space-y-1.5">
              <Label>Domicilio</Label>
              <Input
                value={form.domicilio}
                onChange={(e) => setForm({ ...form, domicilio: e.target.value })}
                placeholder="Calle, número, localidad"
              />
            </div>
            <div className="space-y-1.5">
              <Label>Valuación Fiscal</Label>
              <Input
                value={form.valuacionFiscal}
                onChange={(e) => setForm({ ...form, valuacionFiscal: e.target.value })}
                placeholder="Valor fiscal del inmueble"
              />
            </div>
            <div className="space-y-1.5">
              <Label>Observaciones</Label>
              <Input
                value={form.observaciones}
                onChange={(e) => setForm({ ...form, observaciones: e.target.value })}
                placeholder="Notas adicionales"
              />
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <Button variant="outline" onClick={() => setModalOpen(false)}>
                Cancelar
              </Button>
              <Button
                onClick={handleSave}
                disabled={createMutation.isPending || updateMutation.isPending}
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
