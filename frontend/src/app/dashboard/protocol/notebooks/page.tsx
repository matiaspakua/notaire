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
import type { Notebook, Folio } from "@/types";
import { Download, FolderPlus } from "lucide-react";

const DAMAGED_STATUSES = ["Errose", "no pasó"];

export default function NotebooksPage() {
  const t = useTranslations("protocolo.cuadernos");
  const tc = useTranslations("common");

  const { data: cuadernos = [], isLoading, refetch } = useQuery({
    queryKey: ["cuadernos"],
    queryFn: () => apiGet<Notebook[]>("/notebooks"),
  });

  const { data: folios = [] } = useQuery({
    queryKey: ["folios"],
    queryFn: () => apiGet<Folio[]>("/folios"),
  });

  const foliosDisponibles = useMemo(
    () => folios.filter((f) => f.status === "Nuevo" || DAMAGED_STATUSES.includes(f.status ?? "")),
    [folios],
  );

  const [modalOpen, setModalOpen] = useState(false);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [notes, setNotes] = useState("");
  const [saving, setSaving] = useState(false);
  const [downloadingId, setDownloadingId] = useState<number | null>(null);

  function toggleFolio(idFolio: number) {
    setSelectedIds((prev) =>
      prev.includes(idFolio) ? prev.filter((id) => id !== idFolio) : [...prev, idFolio],
    );
  }

  function openModal() {
    setSelectedIds([]);
    setNotes("");
    setModalOpen(true);
  }

  async function handleCreateNotebook() {
    const notaryId = folios.find((f) => selectedIds.includes(f.idFolio!))?.fkIdNotaryPerson?.personId;
    if (!notaryId) {
      toast.error(t("selectFoliosFirst"));
      return;
    }
    setSaving(true);
    try {
      await apiPost("/notebooks", {
        idsFolio: selectedIds,
        idNotary: notaryId,
        year: new Date().getFullYear(),
        notes: notes || null,
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

  async function handleDownloadCover(idNotebook: number) {
    setDownloadingId(idNotebook);
    try {
      const blob = await apiGetBytes(`/notebooks/${idNotebook}/cover`);
      const link = document.createElement("a");
      link.href = URL.createObjectURL(blob);
      link.download = `notebook_${idNotebook}_cover.pdf`;
      link.click();
      URL.revokeObjectURL(link.href);
    } catch {
      toast.error(t("errorCaratula"));
    } finally {
      setDownloadingId(null);
    }
  }

  const columns: Column<Notebook>[] = [
    { key: "id", header: tc("id"), render: (c) => <span className="text-xs text-muted-foreground">{c.idNotebook}</span>, className: "w-12" },
    { key: "number", header: tc("number"), render: (c) => <span className="font-medium">{c.number}</span> },
    { key: "year", header: t("fields.year"), render: (c) => c.year },
    {
      key: "registry",
      header: t("fields.registro"),
      render: (c) => c.fkIdNotaryPerson?.notaryRegistrationNumber ?? "—",
    },
    {
      key: "cover",
      header: t("fields.caratula"),
      render: (c) => (
        <Button
          variant="outline"
          size="sm"
          disabled={downloadingId === c.idNotebook}
          onClick={() => handleDownloadCover(c.idNotebook!)}
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
        keyExtractor={(c) => c.idNotebook!}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent className="max-w-2xl">
          <FormContainer>
            <FormSection dialogTitle title={t("newCuaderno")}>
              <FormField label={t("fields.selectFolios")} helperText={t("selectTenHelper")}>
                <div className="max-h-64 overflow-y-auto rounded-lg border border-border/40 p-3 space-y-2">
                  {foliosDisponibles.length === 0 && (
                    <p className="text-sm text-muted-foreground">{t("noAvailableFolios")}</p>
                  )}
                  {foliosDisponibles.map((f) => (
                    <div key={f.idFolio} className="flex items-center gap-2">
                      <CheckboxField
                        label={`N° ${f.number} — ${f.status}`}
                        checked={selectedIds.includes(f.idFolio!)}
                        onChange={() => toggleFolio(f.idFolio!)}
                        data-testid={`checkbox-folio-${f.idFolio}`}
                      />
                      {DAMAGED_STATUSES.includes(f.status ?? "") && (
                        <Badge variant="secondary">{t("damaged")}</Badge>
                      )}
                    </div>
                  ))}
                </div>
              </FormField>
              <FormField label={t("fields.observaciones")} helperText={t("observacionesHelper")}>
                <Input value={notes} onChange={(e) => setNotes(e.target.value)} />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button onClick={handleCreateNotebook} disabled={saving || selectedIds.length === 0}>
                {saving ? tc("saving") : tc("create")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>
    </div>
  );
}
