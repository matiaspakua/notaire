"use client";

import { useMemo, useState } from "react";
import { toast } from "sonner";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { FormContainer, FormSection, FormField, FormActions, CheckboxField } from "@/theme/form-patterns";
import { useQuery } from "@tanstack/react-query";
import { apiGet, apiGetBytes, apiPost } from "@/lib/api-client";
import { extractApiError } from "@/lib/utils";
import type { Cuaderno, Folio } from "@/types";
import { Download, FolderPlus } from "lucide-react";

const ESTADOS_DANADOS = ["Errose", "no pasó"];

export default function CuadernosPage() {
  const t = useTranslations("protocolo.cuadernos");
  const tc = useTranslations("common");

  const { data: cuadernos = [], isLoading, refetch } = useQuery({
    queryKey: ["cuadernos"],
    queryFn: () => apiGet<Cuaderno[]>("/cuadernos"),
  });

  const { data: folios = [] } = useQuery({
    queryKey: ["folios"],
    queryFn: () => apiGet<Folio[]>("/folio"),
  });

  const foliosDisponibles = useMemo(
    () => folios.filter((f) => f.estado === "Nuevo" || ESTADOS_DANADOS.includes(f.estado ?? "")),
    [folios],
  );

  const [modalOpen, setModalOpen] = useState(false);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [observaciones, setObservaciones] = useState("");
  const [saving, setSaving] = useState(false);
  const [downloadingId, setDownloadingId] = useState<number | null>(null);

  function toggleFolio(idFolio: number) {
    setSelectedIds((prev) =>
      prev.includes(idFolio) ? prev.filter((id) => id !== idFolio) : [...prev, idFolio],
    );
  }

  function openModal() {
    setSelectedIds([]);
    setObservaciones("");
    setModalOpen(true);
  }

  async function handleCrearCuaderno() {
    const escribanoId = folios.find((f) => selectedIds.includes(f.idFolio!))?.personaEscribano?.idPersona;
    if (!escribanoId) {
      toast.error(t("selectFoliosFirst"));
      return;
    }
    setSaving(true);
    try {
      await apiPost("/cuadernos", {
        idsFolio: selectedIds,
        idEscribano: escribanoId,
        anio: new Date().getFullYear(),
        observaciones: observaciones || null,
      });
      toast.success(t("created"));
      setModalOpen(false);
      refetch();
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorCreate"));
    } finally {
      setSaving(false);
    }
  }

  async function handleDownloadCaratula(idCuaderno: number) {
    setDownloadingId(idCuaderno);
    try {
      const blob = await apiGetBytes(`/cuadernos/${idCuaderno}/caratula`);
      const link = document.createElement("a");
      link.href = URL.createObjectURL(blob);
      link.download = `cuaderno_${idCuaderno}_caratula.pdf`;
      link.click();
      URL.revokeObjectURL(link.href);
    } catch {
      toast.error(t("errorCaratula"));
    } finally {
      setDownloadingId(null);
    }
  }

  const columns: Column<Cuaderno>[] = [
    { key: "id", header: tc("id"), render: (c) => <span className="text-xs text-muted-foreground">{c.idCuaderno}</span>, className: "w-12" },
    { key: "numero", header: tc("number"), render: (c) => <span className="font-medium">{c.numero}</span> },
    { key: "anio", header: t("fields.year"), render: (c) => c.anio },
    {
      key: "registro",
      header: t("fields.registro"),
      render: (c) => c.fkIdPersonaEscribano?.registroEscribano ?? "—",
    },
    {
      key: "caratula",
      header: t("fields.caratula"),
      render: (c) => (
        <Button
          variant="outline"
          size="sm"
          disabled={downloadingId === c.idCuaderno}
          onClick={() => handleDownloadCaratula(c.idCuaderno!)}
        >
          <Download className="h-4 w-4" />
          {tc("downloadPdf")}
        </Button>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title={t("title")}
        description={t("description")}
        actions={
          <Button onClick={openModal}>
            <FolderPlus className="h-4 w-4" />
            {t("newCuaderno")}
          </Button>
        }
      />
      <DataTable
        data={cuadernos}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(c) => c.idCuaderno!}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent className="max-w-2xl">
          <FormContainer>
            <FormSection title={t("newCuaderno")}>
              <FormField label={t("fields.selectFolios")} helperText={t("selectTenHelper")}>
                <div className="max-h-64 overflow-y-auto rounded-lg border border-border/40 p-3 space-y-2">
                  {foliosDisponibles.length === 0 && (
                    <p className="text-sm text-muted-foreground">{t("noAvailableFolios")}</p>
                  )}
                  {foliosDisponibles.map((f) => (
                    <div key={f.idFolio} className="flex items-center gap-2">
                      <CheckboxField
                        label={`N° ${f.numero} — ${f.estado}`}
                        checked={selectedIds.includes(f.idFolio!)}
                        onChange={() => toggleFolio(f.idFolio!)}
                        data-testid={`checkbox-folio-${f.idFolio}`}
                      />
                      {ESTADOS_DANADOS.includes(f.estado ?? "") && (
                        <Badge variant="secondary">{t("damaged")}</Badge>
                      )}
                    </div>
                  ))}
                </div>
              </FormField>
              <FormField label={t("fields.observaciones")} helperText={t("observacionesHelper")}>
                <Input value={observaciones} onChange={(e) => setObservaciones(e.target.value)} />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button onClick={handleCrearCuaderno} disabled={saving || selectedIds.length === 0}>
                {saving ? tc("saving") : tc("create")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>
    </div>
  );
}
