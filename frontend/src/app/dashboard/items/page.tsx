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
} from "@/hooks/useItems";
import type { Item } from "@/types";

export default function ItemsPage() {
  const t = useTranslations("items");
  const tc = useTranslations("common");

  const { data: items = [], isLoading } = useItems();
  const createMutation = useCreateItem();
  const updateMutation = useUpdateItem();
  const deleteMutation = useDeleteItem();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Item | null>(null);
  const [form, setForm] = useState({ nombre: "", cantidad: "", precio: "" });

  function openCreate() {
    setEditing(null);
    setForm({ nombre: "", cantidad: "", precio: "" });
    setModalOpen(true);
  }

  function openEdit(i: Item) {
    setEditing(i);
    setForm({
      nombre: i.name ?? "",
      cantidad: i.value?.toString() ?? "",
      precio: i.value?.toString() ?? "",
    });
    setModalOpen(true);
  }

  async function handleSave() {
    const data: Partial<Item> = {
      name: form.nombre || undefined,
      value: form.precio ? Number(form.precio) : undefined,
    };
    try {
      if (editing?.idItem) {
        await updateMutation.mutateAsync({ id: editing.idItem, data });
        toast.success(t("updated"));
      } else {
        await createMutation.mutateAsync(data);
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

  const columns: Column<Item>[] = [
    {
      key: "id",
      header: tc("id"),
      render: (i) => <span className="text-muted-foreground text-xs">{i.idItem}</span>,
      className: "w-16",
    },
    {
      key: "concepto",
      header: t("fields.concepto"),
      render: (i) => <span className="font-medium">{i.name ?? "—"}</span>,
    },
    {
      key: "precio",
      header: t("fields.precio"),
      render: (i) => i.value ? `$${i.value.toLocaleString("es-AR")}` : "—",
      className: "w-32",
    },
    {
      key: "actions",
      header: "",
      render: (i) => (
        <div className="flex gap-1 justify-end">
          <Button size="icon" variant="ghost" onClick={() => openEdit(i)} aria-label={tc("edit")}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="icon"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(i.idItem!)}
            aria-label={tc("delete")}
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
          <Button onClick={openCreate}>
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

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={editing ? t("editItem") : t("newItem")}>
              <FormField label={t("fields.concepto")}>
                <Input
                  value={form.nombre}
                  onChange={(e) => setForm({ ...form, nombre: e.target.value })}
                  placeholder="Nombre del ítem"
                />
              </FormField>
              <FormField label={`${t("fields.precio")} ($)`}>
                <Input
                  type="number"
                  value={form.precio}
                  onChange={(e) => setForm({ ...form, precio: e.target.value })}
                  placeholder="0.00"
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button onClick={handleSave} disabled={createMutation.isPending || updateMutation.isPending}>
                {editing ? tc("update") : tc("create")}
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
