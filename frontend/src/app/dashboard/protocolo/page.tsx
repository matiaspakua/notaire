"use client";

import { useState } from "react";
import { toast } from "sonner";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useQuery } from "@tanstack/react-query";
import { apiGet, apiGetBytes } from "@/lib/api-client";
import type { Folio } from "@/types";
import { FileText, Download } from "lucide-react";

export default function ProtocoloPage() {
  const { data: folios = [], isLoading } = useQuery({
    queryKey: ["folios"],
    queryFn: () => apiGet<Folio[]>("/folios"),
  });

  const [reportDialog, setReportDialog] = useState<string | null>(null);
  const [reportYear, setReportYear] = useState(new Date().getFullYear().toString());
  const [reportMonth, setReportMonth] = useState((new Date().getMonth() + 1).toString());
  const [downloading, setDownloading] = useState(false);

  async function handleDownloadReport() {
    if (!reportDialog) return;
    setDownloading(true);
    try {
      let url = "";
      let filename = "";
      if (reportDialog === "libro-indice") {
        url = `/reportes/libro-indice?anio=${reportYear}`;
        filename = `libro_indice_${reportYear}.pdf`;
      } else if (reportDialog === "declaracion-jurada-mensual") {
        url = `/reportes/declaracion-jurada-mensual?anio=${reportYear}&mes=${reportMonth}`;
        filename = `declaracion_jurada_mensual_${reportMonth}_${reportYear}.pdf`;
      } else if (reportDialog === "declaracion-jurada-rentas") {
        url = `/reportes/declaracion-jurada-rentas?anio=${reportYear}&mes=${reportMonth}`;
        filename = `declaracion_jurada_rentas_${reportMonth}_${reportYear}.pdf`;
      }
      const blob = await apiGetBytes(url);
      const link = document.createElement("a");
      link.href = URL.createObjectURL(blob);
      link.download = filename;
      link.click();
      URL.revokeObjectURL(link.href);
      toast.success("Reporte descargado");
      setReportDialog(null);
    } catch {
      toast.error("Error al generar el reporte");
    } finally {
      setDownloading(false);
    }
  }

  const reportTitle = {
    "libro-indice": "Generar Libro de Índices (CU24)",
    "declaracion-jurada-mensual": "Generar Declaración Jurada Mensual (CU25)",
    "declaracion-jurada-rentas": "Generar Declaración Jurada de Rentas (CU50)",
  }[reportDialog ?? ""] ?? "";

  const columns: Column<Folio>[] = [
    { key: "id", header: "ID", render: (f) => <span className="text-xs text-muted-foreground">{f.idFolio}</span>, className: "w-12" },
    { key: "numero", header: "Número de folio", render: (f) => <span className="font-medium">{f.numero ?? "—"}</span> },
    { key: "tipo", header: "Tipo", render: (f) => f.tipoDeFolio?.nombre ?? "—" },
    {
      key: "disponible",
      header: "Estado",
      render: (f) =>
        f.disponible ? <Badge variant="success">Disponible</Badge> : <Badge variant="secondary">En uso</Badge>,
    },
  ];

  return (
    <div>
      <AppHeader
        title="Protocolo"
        description="CU24, CU25, CU28, CU50, CU63 — Folios, índices y declaraciones juradas"
        actions={
          <div className="flex gap-2">
            <Button variant="outline" onClick={() => setReportDialog("libro-indice")}>
              <FileText className="h-4 w-4" />Libro de Índices
            </Button>
            <Button variant="outline" onClick={() => setReportDialog("declaracion-jurada-mensual")}>
              <FileText className="h-4 w-4" />DDJJ Mensual
            </Button>
            <Button variant="outline" onClick={() => setReportDialog("declaracion-jurada-rentas")}>
              <FileText className="h-4 w-4" />DDJJ Rentas
            </Button>
          </div>
        }
      />
      <DataTable
        data={folios}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(f) => f.idFolio!}
        emptyMessage="No hay folios registrados"
      />

      <Dialog open={!!reportDialog} onOpenChange={(v) => !v && setReportDialog(null)}>
        <DialogContent>
          <FormContainer>
            <FormSection title={reportTitle}>
              <FormField label="Año" required>
                <Input type="number" value={reportYear} onChange={(e) => setReportYear(e.target.value)} />
              </FormField>
              {reportDialog !== "libro-indice" && (
                <FormField label="Mes" required helperText="1 – 12">
                  <Input type="number" min="1" max="12" value={reportMonth} onChange={(e) => setReportMonth(e.target.value)} />
                </FormField>
              )}
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setReportDialog(null)}>Cancelar</Button>
              <Button onClick={handleDownloadReport} disabled={downloading}>
                <Download className="h-4 w-4" />{downloading ? "Generando..." : "Descargar PDF"}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>
    </div>
  );
}
