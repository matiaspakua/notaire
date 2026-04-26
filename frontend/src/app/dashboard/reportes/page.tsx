"use client";

import { useState } from "react";
import { toast } from "sonner";
import { FileDown, FileText, Book, ClipboardList, AlertCircle } from "lucide-react";
import { AppHeader } from "@/components/layout/AppHeader";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import {
  useReportePresupuesto,
  useReportePresupuestoInmuebles,
  useReporteHistorialGestion,
  useReporteDeclaracionJuradaMensual,
  useReporteDeclaracionJuradaRentas,
  useReporteLibroIndice,
  useReporteDeudaDocumentos,
} from "@/hooks/useReportes";

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

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3 p-4">

        {/* Presupuesto */}
        <Card data-testid="card-reporte-presupuesto">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <FileText className="h-5 w-5" />
              Reporte Presupuesto
            </CardTitle>
            <CardDescription>CU01, CU39 — PDF del presupuesto</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-1">
              <Label>ID Presupuesto</Label>
              <Input
                type="number"
                value={idPresupuesto}
                onChange={(e) => setIdPresupuesto(e.target.value)}
                placeholder="Ej: 1"
                data-testid="input-id-presupuesto"
              />
            </div>
            <Button
              className="w-full"
              variant="outline"
              disabled={!idPresupuesto}
              onClick={() =>
                handle(() => rPresupuesto.download(Number(idPresupuesto)))
              }
              data-testid="btn-descargar-presupuesto"
            >
              <FileDown className="h-4 w-4 mr-2" />
              Descargar PDF
            </Button>
          </CardContent>
        </Card>

        {/* Presupuesto con inmuebles */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <FileText className="h-5 w-5" />
              Presupuesto + Inmuebles
            </CardTitle>
            <CardDescription>CU08 — PDF con información de inmuebles</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-1">
              <Label>ID Presupuesto</Label>
              <Input
                type="number"
                value={idPresupuestoInm}
                onChange={(e) => setIdPresupuestoInm(e.target.value)}
                placeholder="Ej: 1"
              />
            </div>
            <Button
              className="w-full"
              variant="outline"
              disabled={!idPresupuestoInm}
              onClick={() =>
                handle(() => rPresupuestoInmuebles.download(Number(idPresupuestoInm)))
              }
            >
              <FileDown className="h-4 w-4 mr-2" />
              Descargar PDF
            </Button>
          </CardContent>
        </Card>

        {/* Historial gestión */}
        <Card data-testid="card-reporte-historial">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <ClipboardList className="h-5 w-5" />
              Historial de Gestión
            </CardTitle>
            <CardDescription>CU13 — PDF del historial de una gestión</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-1">
              <Label>ID Gestión</Label>
              <Input
                type="number"
                value={idGestion}
                onChange={(e) => setIdGestion(e.target.value)}
                placeholder="Ej: 1"
                data-testid="input-id-gestion-historial"
              />
            </div>
            <Button
              className="w-full"
              variant="outline"
              disabled={!idGestion}
              onClick={() =>
                handle(() => rHistorial.download(Number(idGestion)))
              }
            >
              <FileDown className="h-4 w-4 mr-2" />
              Descargar PDF
            </Button>
          </CardContent>
        </Card>

        {/* DDJJ Mensual */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <Book className="h-5 w-5" />
              DDJJ Mensual
            </CardTitle>
            <CardDescription>CU25 — Declaración jurada mensual</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="grid grid-cols-2 gap-2">
              <div className="space-y-1">
                <Label>Año</Label>
                <Input
                  type="number"
                  value={anioMensual}
                  onChange={(e) => setAnioMensual(e.target.value)}
                  min="2000"
                  max="2099"
                />
              </div>
              <div className="space-y-1">
                <Label>Mes</Label>
                <Input
                  type="number"
                  value={mesMensual}
                  onChange={(e) => setMesMensual(e.target.value)}
                  min="1"
                  max="12"
                />
              </div>
            </div>
            <Button
              className="w-full"
              variant="outline"
              onClick={() =>
                handle(() => rDdjjMensual.download(Number(anioMensual), Number(mesMensual)))
              }
            >
              <FileDown className="h-4 w-4 mr-2" />
              Descargar PDF
            </Button>
          </CardContent>
        </Card>

        {/* DDJJ Rentas */}
        <Card data-testid="card-reporte-ddjj-rentas">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <Book className="h-5 w-5" />
              DDJJ de Rentas
            </CardTitle>
            <CardDescription>CU50 — Declaración jurada de rentas</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="grid grid-cols-2 gap-2">
              <div className="space-y-1">
                <Label>Año</Label>
                <Input
                  type="number"
                  value={anioRentas}
                  onChange={(e) => setAnioRentas(e.target.value)}
                  min="2000"
                  max="2099"
                />
              </div>
              <div className="space-y-1">
                <Label>Mes</Label>
                <Input
                  type="number"
                  value={mesRentas}
                  onChange={(e) => setMesRentas(e.target.value)}
                  min="1"
                  max="12"
                />
              </div>
            </div>
            <Button
              className="w-full"
              variant="outline"
              onClick={() =>
                handle(() => rDdjjRentas.download(Number(anioRentas), Number(mesRentas)))
              }
              data-testid="btn-descargar-ddjj-rentas"
            >
              <FileDown className="h-4 w-4 mr-2" />
              Descargar PDF
            </Button>
          </CardContent>
        </Card>

        {/* Libro Índice */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <Book className="h-5 w-5" />
              Libro Índice
            </CardTitle>
            <CardDescription>CU24 — Libro índice anual</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-1">
              <Label>Año</Label>
              <Input
                type="number"
                value={anioIndice}
                onChange={(e) => setAnioIndice(e.target.value)}
                min="2000"
                max="2099"
              />
            </div>
            <Button
              className="w-full"
              variant="outline"
              onClick={() =>
                handle(() => rLibroIndice.download(Number(anioIndice)))
              }
            >
              <FileDown className="h-4 w-4 mr-2" />
              Descargar PDF
            </Button>
          </CardContent>
        </Card>

        {/* Deuda documentos */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <AlertCircle className="h-5 w-5" />
              Deuda de Documentos
            </CardTitle>
            <CardDescription>CU16 — Documentos pendientes por gestión</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-1">
              <Label>Número de gestión</Label>
              <Input
                type="number"
                value={numGestionDeuda}
                onChange={(e) => setNumGestionDeuda(e.target.value)}
                placeholder="Ej: 1001"
              />
            </div>
            <Button
              className="w-full"
              variant="outline"
              disabled={!numGestionDeuda}
              onClick={() =>
                handle(() => rDeudaDocs.download(Number(numGestionDeuda)))
              }
            >
              <FileDown className="h-4 w-4 mr-2" />
              Descargar PDF
            </Button>
          </CardContent>
        </Card>

      </div>
    </div>
  );
}
