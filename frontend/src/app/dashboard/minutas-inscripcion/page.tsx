"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Download, FileCheck2, Search, Send, ShieldAlert, Stamp } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { theme } from "@/theme/tokens";
import {
  useMinutaInscripcion,
  useGenerarMinutaInscripcion,
  usePresentarMinutaInscripcion,
  useObservarMinutaInscripcion,
  useInscribirMinutaInscripcion,
  descargarReporteMinutaInscripcion,
} from "@/hooks/useMinutasInscripcion";
import { useEscrituras } from "@/hooks/useEscrituras";
import { extractApiError } from "@/lib/utils";

const ESTADO_FIRMADA = "Firmada";
const ESTADO_GENERADA = "Generada";
const ESTADO_PRESENTADA = "Presentado para inscripción";
const ESTADO_OBSERVADA = "Observado";
const ESTADO_INSCRIPTA = "Inscripto";

export default function MinutasInscripcionPage() {
  const t = useTranslations("minutasInscripcion");
  const tc = useTranslations("common");

  const { data: escrituras = [] } = useEscrituras();
  const generarMutation = useGenerarMinutaInscripcion();
  const presentarMutation = usePresentarMinutaInscripcion();
  const observarMutation = useObservarMinutaInscripcion();
  const inscribirMutation = useInscribirMinutaInscripcion();

  const [minutaId, setMinutaId] = useState<number | null>(null);
  const [buscarId, setBuscarId] = useState("");
  const [idEscritura, setIdEscritura] = useState("");
  const [downloading, setDownloading] = useState(false);

  const [presentarOpen, setPresentarOpen] = useState(false);
  const [fechaPresentacion, setFechaPresentacion] = useState("");
  const [numeroEntradaRegistral, setNumeroEntradaRegistral] = useState("");

  const [observarOpen, setObservarOpen] = useState(false);
  const [observacionesRegistro, setObservacionesRegistro] = useState("");
  const [fechaSubsanacion, setFechaSubsanacion] = useState("");

  const [inscribirOpen, setInscribirOpen] = useState(false);
  const [fechaRecepcion, setFechaRecepcion] = useState("");
  const [numeroInscripcionDefinitivo, setNumeroInscripcionDefinitivo] = useState("");

  const { data: minuta, isLoading } = useMinutaInscripcion(minutaId);
  const escriturasFirmadas = escrituras.filter((e) => e.estado === ESTADO_FIRMADA);

  function estadoLabel(estado?: string): string {
    switch (estado) {
      case ESTADO_GENERADA:
        return t("estadoGenerada");
      case ESTADO_PRESENTADA:
        return t("estadoPresentada");
      case ESTADO_OBSERVADA:
        return t("estadoObservada");
      case ESTADO_INSCRIPTA:
        return t("estadoInscripta");
      default:
        return estado ?? "—";
    }
  }

  async function handleGenerar() {
    try {
      const nueva = await generarMutation.mutateAsync(Number(idEscritura));
      setMinutaId(nueva.idMinutaInscripcion ?? null);
      toast.success(t("generated"));
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorGenerate"));
    }
  }

  function handleBuscar() {
    if (!buscarId) return;
    setMinutaId(Number(buscarId));
  }

  async function handlePresentar() {
    if (!minutaId) return;
    try {
      await presentarMutation.mutateAsync({ id: minutaId, fechaPresentacion, numeroEntradaRegistral });
      toast.success(t("presented"));
      setPresentarOpen(false);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorPresentar"));
    }
  }

  async function handleObservar() {
    if (!minutaId) return;
    try {
      await observarMutation.mutateAsync({ id: minutaId, observacionesRegistro, fechaSubsanacion });
      toast.success(t("observed"));
      setObservarOpen(false);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorObservar"));
    }
  }

  async function handleInscribir() {
    if (!minutaId) return;
    try {
      await inscribirMutation.mutateAsync({ id: minutaId, fechaRecepcion, numeroInscripcionDefinitivo });
      toast.success(t("inscribed"));
      setInscribirOpen(false);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorInscribir"));
    }
  }

  async function handleDescargar() {
    if (!minutaId) return;
    try {
      setDownloading(true);
      await descargarReporteMinutaInscripcion(minutaId);
    } catch {
      toast.error(t("errorReporte"));
    } finally {
      setDownloading(false);
    }
  }

  return (
    <div>
      <AppHeader title={t("title")} />

      <FormContainer>
        <FormSection title={t("generarSection")}>
          <FormField label={t("fields.escritura")} required>
            <Select value={idEscritura} onValueChange={setIdEscritura}>
              <SelectTrigger data-testid="select-escritura-minuta">
                <SelectValue placeholder={t("selectEscritura")} />
              </SelectTrigger>
              <SelectContent>
                {escriturasFirmadas.map((e) => (
                  <SelectItem key={e.idEscritura} value={String(e.idEscritura)}>
                    {t("fields.numero")} {e.numero}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </FormField>
          <FormActions align="left">
            <Button
              onClick={handleGenerar}
              disabled={!idEscritura || generarMutation.isPending}
              data-testid="btn-generar-minuta"
            >
              <FileCheck2 className="h-4 w-4" />
              {t("generarMinuta")}
            </Button>
          </FormActions>
        </FormSection>

        <FormSection title={t("buscarSection")}>
          <FormField label={t("fields.idMinuta")}>
            <div className="flex gap-2">
              <Input
                type="number"
                data-testid="input-buscar-minuta"
                value={buscarId}
                onChange={(e) => setBuscarId(e.target.value)}
              />
              <Button variant="secondary" onClick={handleBuscar} data-testid="btn-buscar-minuta">
                <Search className="h-4 w-4" />
                {tc("search")}
              </Button>
            </div>
          </FormField>
        </FormSection>

        {minutaId && isLoading && <p style={{ color: theme.colors.neutral[600] }}>{tc("loading")}</p>}

        {minuta && (
          <FormSection title={`${t("fields.numero")} ${minuta.numero}`}>
            <FormField label={t("fields.estado")}>
              <span data-testid="minuta-estado" className="font-medium">
                {estadoLabel(minuta.estado)}
              </span>
            </FormField>

            <FormActions align="left">
              <Button
                variant="secondary"
                onClick={handleDescargar}
                disabled={downloading}
                data-testid="btn-descargar-minuta"
              >
                <Download className="h-4 w-4" />
                {t("descargarReporte")}
              </Button>

              {minuta.estado === ESTADO_GENERADA && (
                <Button onClick={() => setPresentarOpen(true)} data-testid="btn-presentar-minuta">
                  <Send className="h-4 w-4" />
                  {t("presentar")}
                </Button>
              )}

              {minuta.estado === ESTADO_PRESENTADA && (
                <>
                  <Button
                    variant="secondary"
                    onClick={() => setObservarOpen(true)}
                    data-testid="btn-observar-minuta"
                  >
                    <ShieldAlert className="h-4 w-4" />
                    {t("observar")}
                  </Button>
                  <Button onClick={() => setInscribirOpen(true)} data-testid="btn-inscribir-minuta">
                    <Stamp className="h-4 w-4" />
                    {t("inscribir")}
                  </Button>
                </>
              )}
            </FormActions>
          </FormSection>
        )}
      </FormContainer>

      <Dialog open={presentarOpen} onOpenChange={setPresentarOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={t("presentar")}>
              <FormField label={t("fields.fechaPresentacion")} required>
                <Input
                  type="date"
                  data-testid="input-fecha-presentacion"
                  value={fechaPresentacion}
                  onChange={(e) => setFechaPresentacion(e.target.value)}
                />
              </FormField>
              <FormField label={t("fields.numeroEntradaRegistral")} required>
                <Input
                  data-testid="input-numero-entrada-registral"
                  value={numeroEntradaRegistral}
                  onChange={(e) => setNumeroEntradaRegistral(e.target.value)}
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setPresentarOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button
                onClick={handlePresentar}
                disabled={!fechaPresentacion || !numeroEntradaRegistral || presentarMutation.isPending}
                data-testid="btn-confirmar-presentar"
              >
                {t("presentar")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <Dialog open={observarOpen} onOpenChange={setObservarOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={t("observar")}>
              <FormField label={t("fields.observacionesRegistro")} required>
                <Input
                  data-testid="input-observaciones-registro"
                  value={observacionesRegistro}
                  onChange={(e) => setObservacionesRegistro(e.target.value)}
                />
              </FormField>
              <FormField label={t("fields.fechaSubsanacion")} required>
                <Input
                  type="date"
                  data-testid="input-fecha-subsanacion"
                  value={fechaSubsanacion}
                  onChange={(e) => setFechaSubsanacion(e.target.value)}
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setObservarOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button
                onClick={handleObservar}
                disabled={!observacionesRegistro || !fechaSubsanacion || observarMutation.isPending}
                data-testid="btn-confirmar-observar"
              >
                {t("observar")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <Dialog open={inscribirOpen} onOpenChange={setInscribirOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={t("inscribir")}>
              <FormField label={t("fields.fechaRecepcion")} required>
                <Input
                  type="date"
                  data-testid="input-fecha-recepcion"
                  value={fechaRecepcion}
                  onChange={(e) => setFechaRecepcion(e.target.value)}
                />
              </FormField>
              <FormField label={t("fields.numeroInscripcionDefinitivo")} required>
                <Input
                  data-testid="input-numero-inscripcion-definitivo"
                  value={numeroInscripcionDefinitivo}
                  onChange={(e) => setNumeroInscripcionDefinitivo(e.target.value)}
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setInscribirOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button
                onClick={handleInscribir}
                disabled={
                  !fechaRecepcion || !numeroInscripcionDefinitivo || inscribirMutation.isPending
                }
                data-testid="btn-confirmar-inscribir"
              >
                {t("inscribir")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>
    </div>
  );
}
