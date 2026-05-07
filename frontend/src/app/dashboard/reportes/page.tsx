"use client";

import { useState } from "react";
import { toast } from "sonner";
import { FileDown, FileText, Book, ClipboardList, AlertCircle } from "lucide-react";
import { AppHeader } from "@/components/layout/AppHeader";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { FormField } from "@/theme/form-patterns";
import { theme } from "@/theme/tokens";
import {
  useReportePresupuesto,
  useReportePresupuestoInmuebles,
  useReporteHistorialGestion,
  useReporteDeclaracionJuradaMensual,
  useReporteDeclaracionJuradaRentas,
  useReporteLibroIndice,
  useReporteDeudaDocumentos,
} from "@/hooks/useReportes";

const cardStyle: React.CSSProperties = {
  backgroundColor: theme.semantic.card.bg,
  borderRadius: theme.borderRadius["2xl"],
  border: `1px solid ${theme.semantic.card.border}`,
  boxShadow: theme.shadows.sm,
  padding: theme.spacing[6],
  display: "flex",
  flexDirection: "column",
  gap: theme.spacing[4],
};

const cardTitleStyle: React.CSSProperties = {
  fontSize: theme.typography.fontSize.base,
  fontWeight: theme.typography.fontWeight.semibold,
  color: theme.colors.neutral[900],
  fontFamily: theme.typography.fontFamily.display,
  margin: 0,
};

const cardDescStyle: React.CSSProperties = {
  fontSize: theme.typography.fontSize.sm,
  color: theme.colors.neutral[600],
  fontFamily: theme.typography.fontFamily.body,
  margin: 0,
};

const iconWrapStyle: React.CSSProperties = {
  color: theme.colors.primary[600],
  width: "20px",
  height: "20px",
  flexShrink: 0,
};

export default function ReportesPage() {
  const rPresupuesto = useReportePresupuesto();
  const rPresupuestoInmuebles = useReportePresupuestoInmuebles();
  const rHistorial = useReporteHistorialGestion();
  const rDdjjMensual = useReporteDeclaracionJuradaMensual();
  const rDdjjRentas = useReporteDeclaracionJuradaRentas();
  const rLibroIndice = useReporteLibroIndice();
  const rDeudaDocs = useReporteDeudaDocumentos();

  const [idPresupuesto, setIdPresupuesto] = useState("");
  const [idPresupuestoInm, setIdPresupuestoInm] = useState("");
  const [idGestion, setIdGestion] = useState("");
  const [anioMensual, setAnioMensual] = useState(new Date().getFullYear().toString());
  const [mesMensual, setMesMensual] = useState((new Date().getMonth() + 1).toString());
  const [anioRentas, setAnioRentas] = useState(new Date().getFullYear().toString());
  const [mesRentas, setMesRentas] = useState((new Date().getMonth() + 1).toString());
  const [anioIndice, setAnioIndice] = useState(new Date().getFullYear().toString());
  const [numGestionDeuda, setNumGestionDeuda] = useState("");

  async function handle(fn: () => Promise<void>) {
    try {
      await fn();
      toast.success("PDF generado y descargado");
    } catch {
      toast.error("Error al generar el reporte (backend necesario)");
    }
  }

  return (
    <div>
      <AppHeader
        title="Reportes"
        description="CU03, CU13, CU24, CU25, CU50 — Generación de reportes PDF"
      />

      <div
        style={{
          display: "grid",
          gap: theme.spacing[4],
          gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))",
          padding: theme.spacing[4],
        }}
      >
        {/* Presupuesto */}
        <div style={cardStyle} data-testid="card-reporte-presupuesto">
          <div style={{ display: "flex", flexDirection: "column", gap: theme.spacing[1] }}>
            <div style={{ display: "flex", alignItems: "center", gap: theme.spacing[2] }}>
              <FileText style={iconWrapStyle} />
              <h3 style={cardTitleStyle}>Reporte Presupuesto</h3>
            </div>
            <p style={cardDescStyle}>CU01, CU39 — PDF del presupuesto</p>
          </div>
          <FormField label="ID Presupuesto" required>
            <Input
              type="number"
              value={idPresupuesto}
              onChange={(e) => setIdPresupuesto(e.target.value)}
              placeholder="Ej: 1"
              data-testid="input-id-presupuesto"
            />
          </FormField>
          <Button
            className="w-full"
            variant="secondary"
            disabled={!idPresupuesto}
            onClick={() => handle(() => rPresupuesto.download(Number(idPresupuesto)))}
            data-testid="btn-descargar-presupuesto"
          >
            <FileDown className="h-4 w-4 mr-2" />
            Descargar PDF
          </Button>
        </div>

        {/* Presupuesto + Inmuebles */}
        <div style={cardStyle}>
          <div style={{ display: "flex", flexDirection: "column", gap: theme.spacing[1] }}>
            <div style={{ display: "flex", alignItems: "center", gap: theme.spacing[2] }}>
              <FileText style={iconWrapStyle} />
              <h3 style={cardTitleStyle}>Presupuesto + Inmuebles</h3>
            </div>
            <p style={cardDescStyle}>CU08 — PDF con información de inmuebles</p>
          </div>
          <FormField label="ID Presupuesto" required>
            <Input
              type="number"
              value={idPresupuestoInm}
              onChange={(e) => setIdPresupuestoInm(e.target.value)}
              placeholder="Ej: 1"
            />
          </FormField>
          <Button
            className="w-full"
            variant="secondary"
            disabled={!idPresupuestoInm}
            onClick={() => handle(() => rPresupuestoInmuebles.download(Number(idPresupuestoInm)))}
          >
            <FileDown className="h-4 w-4 mr-2" />
            Descargar PDF
          </Button>
        </div>

        {/* Historial gestión */}
        <div style={cardStyle} data-testid="card-reporte-historial">
          <div style={{ display: "flex", flexDirection: "column", gap: theme.spacing[1] }}>
            <div style={{ display: "flex", alignItems: "center", gap: theme.spacing[2] }}>
              <ClipboardList style={iconWrapStyle} />
              <h3 style={cardTitleStyle}>Historial de Gestión</h3>
            </div>
            <p style={cardDescStyle}>CU13 — PDF del historial de una gestión</p>
          </div>
          <FormField label="ID Gestión" required>
            <Input
              type="number"
              value={idGestion}
              onChange={(e) => setIdGestion(e.target.value)}
              placeholder="Ej: 1"
              data-testid="input-id-gestion-historial"
            />
          </FormField>
          <Button
            className="w-full"
            variant="secondary"
            disabled={!idGestion}
            onClick={() => handle(() => rHistorial.download(Number(idGestion)))}
          >
            <FileDown className="h-4 w-4 mr-2" />
            Descargar PDF
          </Button>
        </div>

        {/* DDJJ Mensual */}
        <div style={cardStyle}>
          <div style={{ display: "flex", flexDirection: "column", gap: theme.spacing[1] }}>
            <div style={{ display: "flex", alignItems: "center", gap: theme.spacing[2] }}>
              <Book style={iconWrapStyle} />
              <h3 style={cardTitleStyle}>DDJJ Mensual</h3>
            </div>
            <p style={cardDescStyle}>CU25 — Declaración jurada mensual</p>
          </div>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: theme.spacing[3] }}>
            <FormField label="Año">
              <Input
                type="number"
                value={anioMensual}
                onChange={(e) => setAnioMensual(e.target.value)}
                min="2000"
                max="2099"
              />
            </FormField>
            <FormField label="Mes">
              <Input
                type="number"
                value={mesMensual}
                onChange={(e) => setMesMensual(e.target.value)}
                min="1"
                max="12"
              />
            </FormField>
          </div>
          <Button
            className="w-full"
            variant="secondary"
            onClick={() => handle(() => rDdjjMensual.download(Number(anioMensual), Number(mesMensual)))}
          >
            <FileDown className="h-4 w-4 mr-2" />
            Descargar PDF
          </Button>
        </div>

        {/* DDJJ Rentas */}
        <div style={cardStyle} data-testid="card-reporte-ddjj-rentas">
          <div style={{ display: "flex", flexDirection: "column", gap: theme.spacing[1] }}>
            <div style={{ display: "flex", alignItems: "center", gap: theme.spacing[2] }}>
              <Book style={iconWrapStyle} />
              <h3 style={cardTitleStyle}>DDJJ de Rentas</h3>
            </div>
            <p style={cardDescStyle}>CU50 — Declaración jurada de rentas</p>
          </div>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: theme.spacing[3] }}>
            <FormField label="Año">
              <Input
                type="number"
                value={anioRentas}
                onChange={(e) => setAnioRentas(e.target.value)}
                min="2000"
                max="2099"
              />
            </FormField>
            <FormField label="Mes">
              <Input
                type="number"
                value={mesRentas}
                onChange={(e) => setMesRentas(e.target.value)}
                min="1"
                max="12"
              />
            </FormField>
          </div>
          <Button
            className="w-full"
            variant="secondary"
            onClick={() => handle(() => rDdjjRentas.download(Number(anioRentas), Number(mesRentas)))}
            data-testid="btn-descargar-ddjj-rentas"
          >
            <FileDown className="h-4 w-4 mr-2" />
            Descargar PDF
          </Button>
        </div>

        {/* Libro Índice */}
        <div style={cardStyle}>
          <div style={{ display: "flex", flexDirection: "column", gap: theme.spacing[1] }}>
            <div style={{ display: "flex", alignItems: "center", gap: theme.spacing[2] }}>
              <Book style={iconWrapStyle} />
              <h3 style={cardTitleStyle}>Libro Índice</h3>
            </div>
            <p style={cardDescStyle}>CU24 — Libro índice anual</p>
          </div>
          <FormField label="Año">
            <Input
              type="number"
              value={anioIndice}
              onChange={(e) => setAnioIndice(e.target.value)}
              min="2000"
              max="2099"
            />
          </FormField>
          <Button
            className="w-full"
            variant="secondary"
            onClick={() => handle(() => rLibroIndice.download(Number(anioIndice)))}
          >
            <FileDown className="h-4 w-4 mr-2" />
            Descargar PDF
          </Button>
        </div>

        {/* Deuda documentos */}
        <div style={cardStyle}>
          <div style={{ display: "flex", flexDirection: "column", gap: theme.spacing[1] }}>
            <div style={{ display: "flex", alignItems: "center", gap: theme.spacing[2] }}>
              <AlertCircle style={iconWrapStyle} />
              <h3 style={cardTitleStyle}>Deuda de Documentos</h3>
            </div>
            <p style={cardDescStyle}>CU16 — Documentos pendientes por gestión</p>
          </div>
          <FormField label="Número de gestión" required>
            <Input
              type="number"
              value={numGestionDeuda}
              onChange={(e) => setNumGestionDeuda(e.target.value)}
              placeholder="Ej: 1001"
            />
          </FormField>
          <Button
            className="w-full"
            variant="secondary"
            disabled={!numGestionDeuda}
            onClick={() => handle(() => rDeudaDocs.download(Number(numGestionDeuda)))}
          >
            <FileDown className="h-4 w-4 mr-2" />
            Descargar PDF
          </Button>
        </div>
      </div>
    </div>
  );
}
