"use client";
import { useState } from "react";
import { toast } from "sonner";
import { useTranslations } from "next-intl";
import { NotaireIcon } from "@/components/ui/notaire-icon";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useQuery } from "@tanstack/react-query";
import { apiGet, apiPost } from "@/lib/api-client";
import type { Folio } from "@/types";

export default function FoliosAdminPage() {
  const t = useTranslations("administracion.folios");
  const tc = useTranslations("common");

  const { data = [], isLoading, refetch } = useQuery({
    queryKey: ["folios"],
    queryFn: () => apiGet<Folio[]>("/folios"),
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [numero, setNumero] = useState("");
  const [anio, setAnio] = useState(new Date().getFullYear().toString());
  const [saving, setSaving] = useState(false);

  function openCreate() {
    setNumero("");
    setAnio(new Date().getFullYear().toString());
    setModalOpen(true);
  }

  async function handleSave() {
    if (!numero.trim()) {
      toast.error(t("numberRequired"));
      return;
    }
    setSaving(true);
    try {
      await apiPost("/folios", { numero: Number(numero), anio: Number(anio), disponible: true });
      toast.success(t("created"));
      setModalOpen(false);
      refetch();
    } catch {
      toast.error(t("errorCreate"));
    } finally {
      setSaving(false);
    }
  }

  const columns: Column<Folio>[] = [
    { key: "id", header: tc("id"), render: (f) => <span className="text-xs text-muted-foreground">{f.idFolio}</span>, className: "w-12" },
    { key: "numero", header: t("fields.numero"), render: (f) => <span className="font-medium">{f.numero}</span> },
    { key: "tipo", header: t("fields.tipo"), render: (f) => f.tipoDeFolio?.nombre ?? "—" },
    { key: "estado", header: t("fields.estado"), render: (f) => f.disponible ? <Badge variant="success">Disponible</Badge> : <Badge variant="secondary">En uso</Badge> },
  ];

  return (
    <div>
      <AppHeader
        title={t("title")}
        description={t("description")}
        actions={
          <Button onClick={openCreate} data-testid="btn-nuevo-folio">
            <NotaireIcon src="/icons/actions/agregar.png" alt={tc("add")} size={16} className="mr-1" />
            {t("newFolio")}
          </Button>
        }
      />
      <DataTable data={data} columns={columns} isLoading={isLoading} keyExtractor={(f) => f.idFolio!} emptyMessage={t("noData")} />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={t("newFolio")}>
              <FormField label={t("fields.numero")} required>
                <Input
                  type="number"
                  value={numero}
                  onChange={(e) => setNumero(e.target.value)}
                  placeholder="Ej: 1001"
                  data-testid="input-numero-folio"
                />
              </FormField>
              <FormField label={t("fields.year")} required>
                <Input
                  type="number"
                  value={anio}
                  onChange={(e) => setAnio(e.target.value)}
                  placeholder={new Date().getFullYear().toString()}
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
