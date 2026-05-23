"use client";
import { useState } from "react";
import { toast } from "sonner";
import { useTranslations } from "next-intl";
import { NotaireIcon } from "@/components/ui/notaire-icon";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useQuery } from "@tanstack/react-query";
import { apiGet, apiPost } from "@/lib/api-client";
import type { EstadoDeGestion } from "@/types";

export default function EstadosGestionPage() {
  const t = useTranslations("administracion.estadosGestion");
  const tc = useTranslations("common");

  const { data = [], isLoading, refetch } = useQuery({
    queryKey: ["estados-gestion"],
    queryFn: () => apiGet<EstadoDeGestion[]>("/catalogos/estados-gestion"),
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [nombre, setNombre] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [saving, setSaving] = useState(false);

  function openCreate() {
    setNombre("");
    setDescripcion("");
    setModalOpen(true);
  }

  async function handleSave() {
    if (!nombre.trim()) {
      toast.error(t("nameRequired"));
      return;
    }
    setSaving(true);
    try {
      await apiPost("/catalogos/estados-gestion", { nombre: nombre.trim(), descripcion: descripcion.trim() });
      toast.success(t("created"));
      setModalOpen(false);
      refetch();
    } catch {
      toast.error(t("errorCreate"));
    } finally {
      setSaving(false);
    }
  }

  const columns: Column<EstadoDeGestion>[] = [
    { key: "id", header: tc("id"), render: (e) => <span className="text-xs text-muted-foreground">{e.idEstadoDeGestion}</span>, className: "w-12" },
    { key: "nombre", header: t("fields.nombre"), render: (e) => <span className="font-medium">{e.nombre}</span> },
    { key: "desc", header: tc("description"), render: (e) => e.descripcion ?? "—" },
  ];

  return (
    <div>
      <AppHeader
        title={t("title")}
        description={t("description")}
        actions={
          <Button onClick={openCreate} data-testid="btn-nuevo-estado">
            <NotaireIcon src="/icons/actions/agregar.png" alt={tc("add")} size={16} className="mr-1" />
            {t("newEstado")}
          </Button>
        }
      />
      <DataTable data={data} columns={columns} isLoading={isLoading} keyExtractor={(e) => e.idEstadoDeGestion!} emptyMessage={t("noData")} />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={t("newEstado")}>
              <FormField label={t("fields.nombre")} required>
                <Input
                  value={nombre}
                  onChange={(e) => setNombre(e.target.value)}
                  placeholder={t("fields.namePlaceholder")}
                  data-testid="input-nombre-estado"
                />
              </FormField>
              <FormField label={t("fields.descripcion")}>
                <Input
                  value={descripcion}
                  onChange={(e) => setDescripcion(e.target.value)}
                  placeholder={t("fields.descripcionPlaceholder")}
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                <NotaireIcon src="/icons/actions/cerrar.png" alt={tc("cancel")} size={16} className="mr-1" />
                {tc("cancel")}
              </Button>
              <Button onClick={handleSave} disabled={saving}>
                <NotaireIcon src="/icons/actions/guardar.png" alt={tc("save")} size={16} className="mr-1 brightness-0 invert" />
                {tc("save")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>
    </div>
  );
}
