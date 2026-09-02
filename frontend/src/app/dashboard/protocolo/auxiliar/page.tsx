"use client";

import { useState } from "react";
import { toast } from "sonner";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useQuery } from "@tanstack/react-query";
import { apiGet, apiPost } from "@/lib/api-client";
import { extractApiError } from "@/lib/utils";
import type { Folio, Escritura } from "@/types";
import { FilePlus } from "lucide-react";

export default function ProtocoloAuxiliarPage() {
  const t = useTranslations("protocolo.auxiliar");
  const tc = useTranslations("common");

  const {
    data: foliosDisponibles = [],
    isLoading,
    refetch,
  } = useQuery({
    queryKey: ["protocolo-auxiliar", "folios-disponibles"],
    queryFn: () => apiGet<Folio[]>("/protocolo-auxiliar/folios-disponibles"),
  });

  const [selectedFolio, setSelectedFolio] = useState<Folio | null>(null);
  const [cuerpo, setCuerpo] = useState("");
  const [saving, setSaving] = useState(false);

  function openModal(folio: Folio) {
    setSelectedFolio(folio);
    setCuerpo("");
  }

  async function handleIniciarEscritura() {
    if (!selectedFolio) return;
    setSaving(true);
    try {
      await apiPost<Escritura>("/protocolo-auxiliar/escrituras", {
        idFolio: selectedFolio.idFolio,
        cuerpo,
      });
      toast.success(t("created"));
      setSelectedFolio(null);
      refetch();
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorCreate"));
    } finally {
      setSaving(false);
    }
  }

  const columns: Column<Folio>[] = [
    {
      key: "id",
      header: tc("id"),
      render: (f) => <span className="text-xs text-muted-foreground">{f.idFolio}</span>,
      className: "w-12",
    },
    { key: "numero", header: tc("number"), render: (f) => <span className="font-medium">{f.numero ?? "—"}</span> },
    { key: "tipo", header: tc("type"), render: (f) => f.fkIdTipoFolio?.nombre ?? "—" },
    {
      key: "action",
      header: "",
      render: (f) => (
        <Button size="sm" onClick={() => openModal(f)}>
          <FilePlus className="h-4 w-4" />
          {t("iniciarEscritura")}
        </Button>
      ),
      className: "w-48",
    },
  ];

  return (
    <div>
      <AppHeader title={t("title")} description={t("description")} />
      <DataTable
        data={foliosDisponibles}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(f) => f.idFolio!}
        emptyMessage={t("noData")}
      />

      <Dialog open={!!selectedFolio} onOpenChange={(v) => !v && setSelectedFolio(null)}>
        <DialogContent>
          <FormContainer>
            <FormSection title={t("iniciarEscritura")}>
              <FormField label={t("fields.folio")}>
                <span className="text-sm font-medium">
                  N° {selectedFolio?.numero} — {selectedFolio?.fkIdTipoFolio?.nombre}
                </span>
              </FormField>
              <FormField label={t("fields.cuerpo")} required helperText={t("cuerpoHelper")}>
                <textarea
                  className="w-full rounded-lg border border-input bg-transparent p-3 text-sm"
                  rows={6}
                  value={cuerpo}
                  onChange={(e) => setCuerpo(e.target.value)}
                  data-testid="textarea-cuerpo-escritura-auxiliar"
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setSelectedFolio(null)}>
                {tc("cancel")}
              </Button>
              <Button onClick={handleIniciarEscritura} disabled={saving || !cuerpo.trim()}>
                {saving ? tc("saving") : tc("create")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>
    </div>
  );
}
