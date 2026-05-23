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
import type { PlantillaPresupuesto, TipoDeTramite } from "@/types";

export default function PlantillasPage() {
  const t = useTranslations("administracion.plantillas");
  const tc = useTranslations("common");

  const { data = [], isLoading, refetch } = useQuery({
    queryKey: ["plantillas-presupuesto"],
    queryFn: () => apiGet<PlantillaPresupuesto[]>("/presupuestos/plantillas"),
  });

  const { data: tiposTramite = [] } = useQuery({
    queryKey: ["tipos-tramite"],
    queryFn: () => apiGet<TipoDeTramite[]>("/catalogos/tipos-tramite"),
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [nombre, setNombre] = useState("");
  const [tipoTramiteId, setTipoTramiteId] = useState("");
  const [saving, setSaving] = useState(false);

  function openCreate() {
    setNombre("");
    setTipoTramiteId("");
    setModalOpen(true);
  }

  async function handleSave() {
    if (!nombre.trim()) {
      toast.error(t("nameRequired"));
      return;
    }
    setSaving(true);
    try {
      await apiPost("/presupuestos/plantillas", { nombre: nombre.trim(), tipoTramiteId: tipoTramiteId ? Number(tipoTramiteId) : undefined });
      toast.success(t("created"));
      setModalOpen(false);
      refetch();
    } catch {
      toast.error(t("errorCreate"));
    } finally {
      setSaving(false);
    }
  }

  const columns: Column<PlantillaPresupuesto>[] = [
    { key: "id", header: tc("id"), render: (p) => <span className="text-xs text-muted-foreground">{p.idPlantillaPresupuesto}</span>, className: "w-12" },
    { key: "nombre", header: t("fields.nombre"), render: (p) => <span className="font-medium">{p.nombre}</span> },
    { key: "items", header: t("fields.items"), render: (p) => p.itemList?.length ?? 0 },
  ];

  return (
    <div>
      <AppHeader
        title={t("title")}
        description={t("description")}
        actions={
          <Button onClick={openCreate} data-testid="btn-nueva-plantilla">
            <NotaireIcon src="/icons/actions/agregar.png" alt={tc("add")} size={16} className="mr-1" />
            {t("newPlantilla")}
          </Button>
        }
      />
      <DataTable data={data} columns={columns} isLoading={isLoading} keyExtractor={(p) => p.idPlantillaPresupuesto!} emptyMessage={t("noData")} />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={t("newPlantilla")}>
              <FormField label={t("fields.nombre")} required>
                <Input
                  value={nombre}
                  onChange={(e) => setNombre(e.target.value)}
                  placeholder={t("fields.namePlaceholder")}
                  data-testid="input-nombre-plantilla"
                />
              </FormField>
              <FormField label={t("fields.tipoTramite")}>
                <select
                  value={tipoTramiteId}
                  onChange={(e) => setTipoTramiteId(e.target.value)}
                  className="flex h-12 w-full rounded-[12px] border border-border bg-white px-4 py-2.5 text-base shadow-sm focus:outline-none focus:ring-2 focus:ring-ring"
                >
                  <option value="">{t("fields.tipoTramite")}</option>
                  {tiposTramite.map((tipo) => (
                    <option key={tipo.idTipoDeTramite} value={String(tipo.idTipoDeTramite)}>
                      {tipo.nombre}
                    </option>
                  ))}
                </select>
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
