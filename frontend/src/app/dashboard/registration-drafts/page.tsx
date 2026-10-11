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
  useRegistrationDraft,
  useGenerateRegistrationDraft,
  useSubmitRegistrationDraft,
  useObserveRegistrationDraft,
  useRegisterRegistrationDraft,
  downloadRegistrationDraftReport,
} from "@/hooks/useRegistrationDrafts";
import { useDeeds } from "@/hooks/useDeeds";
import { extractApiError } from "@/lib/utils";

const STATUS_SIGNED = "Firmada";
const ESTADO_GENERADA = "Generada";
const ESTADO_PRESENTADA = "Presentado para inscripción";
const ESTADO_OBSERVADA = "Observado";
const ESTADO_INSCRIPTA = "Inscripto";

export default function RegistrationDraftsRegistrationPage() {
  const t = useTranslations("minutasInscripcion");
  const tc = useTranslations("common");

  const { data: deeds = [] } = useDeeds();
  const generarMutation = useGenerateRegistrationDraft();
  const presentarMutation = useSubmitRegistrationDraft();
  const observarMutation = useObserveRegistrationDraft();
  const inscribirMutation = useRegisterRegistrationDraft();

  const [registrationDraftId, setRegistrationDraftId] = useState<number | null>(null);
  const [buscarId, setBuscarId] = useState("");
  const [idDeed, setIdDeed] = useState("");
  const [downloading, setDownloading] = useState(false);

  const [presentarOpen, setPresentarOpen] = useState(false);
  const [fechaPresentacion, setFechaPresentacion] = useState("");
  const [numberEntryRegistry, setNumberEntryRegistry] = useState("");

  const [observarOpen, setObservarOpen] = useState(false);
  const [notesRegistration, setNotesRegistration] = useState("");
  const [fechaSubsanacion, setFechaSubsanacion] = useState("");

  const [inscribirOpen, setInscribirOpen] = useState(false);
  const [fechaRecepcion, setFechaRecepcion] = useState("");
  const [numberRegistrationFinal, setNumberRegistrationFinal] = useState("");

  const { data: registrationDraft, isLoading } = useRegistrationDraft(registrationDraftId);
  const deedsSigned = deeds.filter((e) => e.status === STATUS_SIGNED);

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
      const nueva = await generarMutation.mutateAsync(Number(idDeed));
      setRegistrationDraftId(nueva.idRegistrationDraft ?? null);
      toast.success(t("generated"));
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorGenerate"));
    }
  }

  function handleBuscar() {
    if (!buscarId) return;
    setRegistrationDraftId(Number(buscarId));
  }

  async function handlePresentar() {
    if (!registrationDraftId) return;
    try {
      await presentarMutation.mutateAsync({ id: registrationDraftId, fechaPresentacion, numberEntryRegistry });
      toast.success(t("presented"));
      setPresentarOpen(false);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorPresentar"));
    }
  }

  async function handleObservar() {
    if (!registrationDraftId) return;
    try {
      await observarMutation.mutateAsync({ id: registrationDraftId, notesRegistration, fechaSubsanacion });
      toast.success(t("observed"));
      setObservarOpen(false);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorObservar"));
    }
  }

  async function handleInscribir() {
    if (!registrationDraftId) return;
    try {
      await inscribirMutation.mutateAsync({ id: registrationDraftId, fechaRecepcion, numberRegistrationFinal });
      toast.success(t("inscribed"));
      setInscribirOpen(false);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorInscribir"));
    }
  }

  async function handleDescargar() {
    if (!registrationDraftId) return;
    try {
      setDownloading(true);
      await downloadRegistrationDraftReport(registrationDraftId);
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
            <Select value={idDeed} onValueChange={setIdDeed}>
              <SelectTrigger data-testid="select-deed-registration-draft">
                <SelectValue placeholder={t("selectEscritura")} />
              </SelectTrigger>
              <SelectContent>
                {deedsSigned.map((e) => (
                  <SelectItem key={e.idDeed} value={String(e.idDeed)}>
                    {t("fields.numero")} {e.number}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </FormField>
          <FormActions align="left">
            <Button
              onClick={handleGenerar}
              disabled={!idDeed || generarMutation.isPending}
              data-testid="btn-generate-registration-draft"
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
                data-testid="input-search-registration-draft"
                value={buscarId}
                onChange={(e) => setBuscarId(e.target.value)}
              />
              <Button variant="secondary" onClick={handleBuscar} data-testid="btn-search-registration-draft">
                <Search className="h-4 w-4" />
                {tc("search")}
              </Button>
            </div>
          </FormField>
        </FormSection>

        {registrationDraftId && isLoading && <p style={{ color: theme.colors.neutral[600] }}>{tc("loading")}</p>}

        {registrationDraft && (
          <FormSection title={`${t("fields.numero")} ${registrationDraft.number}`}>
            <FormField label={t("fields.estado")}>
              <span data-testid="registration-draft-status" className="font-medium">
                {estadoLabel(registrationDraft.status)}
              </span>
            </FormField>

            <FormActions align="left">
              <Button
                variant="secondary"
                onClick={handleDescargar}
                disabled={downloading}
                data-testid="btn-download-registration-draft"
              >
                <Download className="h-4 w-4" />
                {t("descargarReporte")}
              </Button>

              {registrationDraft.status === ESTADO_GENERADA && (
                <Button onClick={() => setPresentarOpen(true)} data-testid="btn-submit-registration-draft">
                  <Send className="h-4 w-4" />
                  {t("presentar")}
                </Button>
              )}

              {registrationDraft.status === ESTADO_PRESENTADA && (
                <>
                  <Button
                    variant="secondary"
                    onClick={() => setObservarOpen(true)}
                    data-testid="btn-observe-registration-draft"
                  >
                    <ShieldAlert className="h-4 w-4" />
                    {t("observar")}
                  </Button>
                  <Button onClick={() => setInscribirOpen(true)} data-testid="btn-register-registration-draft">
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
            <FormSection dialogTitle title={t("presentar")}>
              <FormField label={t("fields.fechaPresentacion")} required>
                <Input
                  type="date"
                  data-testid="input-submission-date"
                  value={fechaPresentacion}
                  onChange={(e) => setFechaPresentacion(e.target.value)}
                />
              </FormField>
              <FormField label={t("fields.numeroEntradaRegistral")} required>
                <Input
                  data-testid="input-registry-entry-number"
                  value={numberEntryRegistry}
                  onChange={(e) => setNumberEntryRegistry(e.target.value)}
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setPresentarOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button
                onClick={handlePresentar}
                disabled={!fechaPresentacion || !numberEntryRegistry || presentarMutation.isPending}
                data-testid="btn-confirm-submit"
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
            <FormSection dialogTitle title={t("observar")}>
              <FormField label={t("fields.observacionesRegistro")} required>
                <Input
                  data-testid="input-notes-registro"
                  value={notesRegistration}
                  onChange={(e) => setNotesRegistration(e.target.value)}
                />
              </FormField>
              <FormField label={t("fields.fechaSubsanacion")} required>
                <Input
                  type="date"
                  data-testid="input-correction-date"
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
                disabled={!notesRegistration || !fechaSubsanacion || observarMutation.isPending}
                data-testid="btn-confirm-observe"
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
            <FormSection dialogTitle title={t("inscribir")}>
              <FormField label={t("fields.fechaRecepcion")} required>
                <Input
                  type="date"
                  data-testid="input-receipt-date"
                  value={fechaRecepcion}
                  onChange={(e) => setFechaRecepcion(e.target.value)}
                />
              </FormField>
              <FormField label={t("fields.numeroInscripcionDefinitivo")} required>
                <Input
                  data-testid="input-final-registration-number"
                  value={numberRegistrationFinal}
                  onChange={(e) => setNumberRegistrationFinal(e.target.value)}
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
                  !fechaRecepcion || !numberRegistrationFinal || inscribirMutation.isPending
                }
                data-testid="btn-confirm-register"
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
