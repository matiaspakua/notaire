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
import { Badge } from "@/components/ui/badge";
import { FormContainer, FormSection, FormField, FormActions, CheckboxField } from "@/theme/form-patterns";
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import {
  usePersonas,
  useCreatePersona,
  useUpdatePersona,
  useDeletePersona,
} from "@/hooks/usePersonas";
import { fullName } from "@/lib/utils";
import { presentPersonaSaveError } from "@/lib/persona-save-error";
import type { Persona } from "@/types";
import { useDeleteError } from "@/hooks/useDeleteError";

const EMPTY: Partial<Persona> = {
  firstName: "",
  lastName: "",
  identificationNumber: "",
  email: "",
  phone: "",
  address: "",
  isClient: false,
};

export default function PersonasPage() {
  const t = useTranslations("personas");
  const showDeleteError = useDeleteError();
  const tc = useTranslations("common");

  const { data: personas = [], isLoading } = usePersonas();
  const createMutation = useCreatePersona();
  const updateMutation = useUpdatePersona();
  const deleteMutation = useDeletePersona();

  const [modalOpen, setModalOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Partial<Persona>>(EMPTY);
  const [isEditMode, setIsEditMode] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const [searchNombre, setSearchNombre] = useState("");
  const [searchApellido, setSearchApellido] = useState("");
  const [searchDni, setSearchDni] = useState("");
  const [filterClientes, setFilterClientes] = useState(false);

  const hasSearchCriteria = !!(searchNombre || searchApellido || searchDni || filterClientes);

  const { data: filteredPersonas = personas } = useQuery({
    queryKey: ["personas", "buscar", searchNombre, searchApellido, searchDni, filterClientes, personas],
    queryFn: () => {
      if (!hasSearchCriteria) {
        return Promise.resolve(personas);
      }
      // Backend route is /people/search (PersonController#searchPeople) — not /personas/buscar.
      const params = new URLSearchParams();
      if (searchNombre) params.set("firstName", searchNombre);
      if (searchApellido) params.set("lastName", searchApellido);
      if (searchDni) params.set("identificationNumber", searchDni);
      if (filterClientes) params.set("isClient", "true");
      return apiGet<Persona[]>(`/people/search?${params.toString()}`);
    },
  });

  function openCreate() {
    setEditing(EMPTY);
    setIsEditMode(false);
    setFieldErrors({});
    setModalOpen(true);
  }

  function openEdit(p: Persona) {
    setEditing(p);
    setIsEditMode(true);
    setFieldErrors({});
    setModalOpen(true);
  }

  async function handleSave() {
    setFieldErrors({});
    try {
      if (isEditMode && editing.personId) {
        await updateMutation.mutateAsync({ id: editing.personId, data: editing });
        toast.success(t("updated"));
      } else {
        await createMutation.mutateAsync(editing);
        toast.success(t("created"));
      }
      setModalOpen(false);
    } catch (err) {
      presentPersonaSaveError(err, {
        fallback: t("errorSave"),
        duplicateDocument: t("duplicateDocument"),
        viewExistingLabel: t("viewExisting"),
        personas,
        onViewExisting: openEdit,
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
      showDeleteError(err, t("errorDelete"));
    } finally {
      setDeleteId(null);
    }
  }

  const columns: Column<Persona>[] = [
    {
      key: "id",
      header: tc("id"),
      render: (p) => <span className="text-xs text-muted-foreground">{p.personId}</span>,
      className: "w-12",
    },
    {
      key: "firstName",
      header: t("fields.nombre"),
      render: (p) => <span className="font-medium">{fullName(p)}</span>,
    },
    {
      key: "dni",
      header: `${t("fields.dni")} / ${t("fields.cuil")}`,
      render: (p) => p.identificationNumber ?? p.taxId ?? "—",
    },
    {
      key: "email",
      header: t("fields.email"),
      render: (p) => p.email ?? "—",
    },
    {
      key: "cliente",
      header: tc("type"),
      render: (p) =>
        p.isClient ? (
          <Badge variant="success">{t("badges.client")}</Badge>
        ) : (
          <Badge variant="secondary">{t("badges.person")}</Badge>
        ),
    },
    {
      key: "actions",
      header: "",
      render: (p) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(p)} aria-label={tc("edit")}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setDeleteId(p.personId!)}
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
          <Button onClick={openCreate} data-testid="btn-nueva-persona">
            <Plus className="h-4 w-4" />
            {t("newPersona")}
          </Button>
        }
      />

      <div data-testid="search-bar" className="flex flex-wrap gap-3 px-4 pb-4">
        <Input
          placeholder={t("searchPlaceholders.nombre")}
          aria-label={t("searchPlaceholders.nombre")}
          value={searchNombre}
          onChange={(e) => setSearchNombre(e.target.value)}
          data-testid="input-search-firstName"
          className="w-40"
        />
        <Input
          placeholder={t("searchPlaceholders.apellido")}
          aria-label={t("searchPlaceholders.apellido")}
          value={searchApellido}
          onChange={(e) => setSearchApellido(e.target.value)}
          data-testid="input-search-lastName"
          className="w-40"
        />
        <Input
          placeholder={t("searchPlaceholders.dni")}
          aria-label={t("searchPlaceholders.dni")}
          value={searchDni}
          onChange={(e) => setSearchDni(e.target.value)}
          data-testid="input-search-dni"
          className="w-36"
        />
        <Button
          variant={filterClientes ? "default" : "secondary"}
          onClick={() => setFilterClientes((v) => !v)}
          data-testid="btn-filter-clientes"
          size="sm"
        >
          {t("filterClients")}
        </Button>
      </div>

      <DataTable
        data={filteredPersonas}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(p) => p.personId!}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent className="max-w-md">
          <FormContainer>
            <FormSection title={isEditMode ? t("editPersona") : t("newPersona")}>
              <div className="grid grid-cols-2 gap-3">
                <FormField label={t("fields.nombre")} required error={fieldErrors.firstName}>
                  <Input
                    value={editing.firstName ?? ""}
                    onChange={(e) => setEditing({ ...editing, firstName: e.target.value })}
                    data-testid="input-firstName"
                    aria-invalid={!!fieldErrors.firstName}
                  />
                </FormField>
                <FormField label={t("fields.apellido")} required error={fieldErrors.lastName}>
                  <Input
                    value={editing.lastName ?? ""}
                    onChange={(e) => setEditing({ ...editing, lastName: e.target.value })}
                    data-testid="input-lastName"
                    aria-invalid={!!fieldErrors.lastName}
                  />
                </FormField>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <FormField label={t("fields.dni")} error={fieldErrors.identificationNumber}>
                  <Input
                    value={editing.identificationNumber ?? ""}
                    onChange={(e) => setEditing({ ...editing, identificationNumber: e.target.value })}
                    data-testid="input-dni"
                    aria-invalid={!!fieldErrors.identificationNumber}
                  />
                </FormField>
                <FormField label={t("fields.cuil")} error={fieldErrors.taxId}>
                  <Input
                    value={editing.taxId ?? ""}
                    onChange={(e) => setEditing({ ...editing, taxId: e.target.value })}
                    aria-invalid={!!fieldErrors.taxId}
                  />
                </FormField>
              </div>
              <FormField label={t("fields.email")} error={fieldErrors.email}>
                <Input
                  type="email"
                  value={editing.email ?? ""}
                  onChange={(e) => setEditing({ ...editing, email: e.target.value })}
                  aria-invalid={!!fieldErrors.email}
                />
              </FormField>
              <FormField label={t("fields.telefono")}>
                <Input
                  value={editing.phone ?? ""}
                  onChange={(e) => setEditing({ ...editing, phone: e.target.value })}
                />
              </FormField>
              <FormField label={t("fields.domicilio")}>
                <Input
                  value={editing.address ?? ""}
                  onChange={(e) => setEditing({ ...editing, address: e.target.value })}
                />
              </FormField>
              <FormField label={t("fields.registroEscribano")} helperText={t("helperText.registroEscribano")}>
                <Input
                  type="number"
                  value={editing.notaryRegistrationNumber ?? ""}
                  onChange={(e) =>
                    setEditing({
                      ...editing,
                      notaryRegistrationNumber: e.target.value ? Number(e.target.value) : undefined,
                    })
                  }
                  data-testid="input-registro-escribano"
                />
              </FormField>
              <CheckboxField
                label={t("fields.esCliente")}
                checked={editing.isClient ?? false}
                onChange={(checked) => setEditing({ ...editing, isClient: checked })}
                data-testid="check-es-cliente"
              />
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
