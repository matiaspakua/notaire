"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Download, FileCheck2, Search, Send, ShieldAlert, Stamp } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
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
import { DeedPicker } from "@/components/shared/DeedPicker";
import { extractApiError } from "@/lib/utils";

const STATUS_GENERATED = "Generada";
const STATUS_SUBMITTED = "Presentado para inscripción";
const STATUS_FLAGGED = "Observado";
const STATUS_REGISTERED = "Inscripto";

export default function RegistrationDraftsRegistrationPage() {
  const t = useTranslations("minutasInscripcion");
  const tc = useTranslations("common");

  const generateMutation = useGenerateRegistrationDraft();
  const submitMutation = useSubmitRegistrationDraft();
  const observeMutation = useObserveRegistrationDraft();
  const registerMutation = useRegisterRegistrationDraft();

  const [registrationDraftId, setRegistrationDraftId] = useState<number | null>(null);
  const [searchId, setSearchId] = useState("");
  const [idDeed, setIdDeed] = useState("");
  const [downloading, setDownloading] = useState(false);

  const [submitOpen, setSubmitOpen] = useState(false);
  const [dateSubmission, setDateSubmission] = useState("");
  const [registryEntryNumber, setRegistryEntryNumber] = useState("");

  const [observeOpen, setObserveOpen] = useState(false);
  const [registryNotes, setRegistryNotes] = useState("");
  const [dateCorrection, setDateCorrection] = useState("");

  const [registerOpen, setRegisterOpen] = useState(false);
  const [dateReception, setDateReception] = useState("");
  const [finalRegistrationNumber, setFinalRegistrationNumber] = useState("");

  const { data: registrationDraft, isLoading } = useRegistrationDraft(registrationDraftId);
  function statusLabel(status?: string): string {
    switch (status) {
      case STATUS_GENERATED:
        return t("estadoGenerada");
      case STATUS_SUBMITTED:
        return t("estadoPresentada");
      case STATUS_FLAGGED:
        return t("estadoObservada");
      case STATUS_REGISTERED:
        return t("estadoInscripta");
      default:
        return status ?? "—";
    }
  }

  async function handleGenerate() {
    try {
      const created = await generateMutation.mutateAsync(Number(idDeed));
      setRegistrationDraftId(created.idRegistrationDraft ?? null);
      toast.success(t("generated"));
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorGenerate"));
    }
  }

  function handleSearch() {
    if (!searchId) return;
    setRegistrationDraftId(Number(searchId));
  }

  async function handleSubmit() {
    if (!registrationDraftId) return;
    try {
      await submitMutation.mutateAsync({ id: registrationDraftId, dateSubmission, registryEntryNumber });
      toast.success(t("presented"));
      setSubmitOpen(false);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorPresentar"));
    }
  }

  async function handleObserve() {
    if (!registrationDraftId) return;
    try {
      await observeMutation.mutateAsync({ id: registrationDraftId, registryNotes, dateCorrection });
      toast.success(t("observed"));
      setObserveOpen(false);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorObservar"));
    }
  }

  async function handleRegister() {
    if (!registrationDraftId) return;
    try {
      await registerMutation.mutateAsync({ id: registrationDraftId, dateReception, finalRegistrationNumber });
      toast.success(t("inscribed"));
      setRegisterOpen(false);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorInscribir"));
    }
  }

  async function handleDownload() {
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
            <DeedPicker
              value={idDeed ? Number(idDeed) : undefined}
              onChange={(deed) => setIdDeed(deed?.idDeed != null ? String(deed.idDeed) : "")}
              aria-label={t("fields.escritura")}
              data-testid="select-deed-registration-draft"
            />
          </FormField>
          <FormActions align="left">
            <Button
              onClick={handleGenerate}
              disabled={!idDeed || generateMutation.isPending}
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
                value={searchId}
                onChange={(e) => setSearchId(e.target.value)}
              />
              <Button variant="secondary" onClick={handleSearch} data-testid="btn-search-registration-draft">
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
                {statusLabel(registrationDraft.status)}
              </span>
            </FormField>

            <FormActions align="left">
              <Button
                variant="secondary"
                onClick={handleDownload}
                disabled={downloading}
                data-testid="btn-download-registration-draft"
              >
                <Download className="h-4 w-4" />
                {t("descargarReporte")}
              </Button>

              {registrationDraft.status === STATUS_GENERATED && (
                <Button onClick={() => setSubmitOpen(true)} data-testid="btn-submit-registration-draft">
                  <Send className="h-4 w-4" />
                  {t("presentar")}
                </Button>
              )}

              {registrationDraft.status === STATUS_SUBMITTED && (
                <>
                  <Button
                    variant="secondary"
                    onClick={() => setObserveOpen(true)}
                    data-testid="btn-observe-registration-draft"
                  >
                    <ShieldAlert className="h-4 w-4" />
                    {t("observar")}
                  </Button>
                  <Button onClick={() => setRegisterOpen(true)} data-testid="btn-register-registration-draft">
                    <Stamp className="h-4 w-4" />
                    {t("inscribir")}
                  </Button>
                </>
              )}
            </FormActions>
          </FormSection>
        )}
      </FormContainer>

      <Dialog open={submitOpen} onOpenChange={setSubmitOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={t("presentar")}>
              <FormField label={t("fields.fechaPresentacion")} required>
                <Input
                  type="date"
                  data-testid="input-submission-date"
                  value={dateSubmission}
                  onChange={(e) => setDateSubmission(e.target.value)}
                />
              </FormField>
              <FormField label={t("fields.numeroEntradaRegistral")} required>
                <Input
                  data-testid="input-registry-entry-number"
                  value={registryEntryNumber}
                  onChange={(e) => setRegistryEntryNumber(e.target.value)}
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setSubmitOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button
                onClick={handleSubmit}
                disabled={!dateSubmission || !registryEntryNumber || submitMutation.isPending}
                data-testid="btn-confirm-submit"
              >
                {t("presentar")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <Dialog open={observeOpen} onOpenChange={setObserveOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={t("observar")}>
              <FormField label={t("fields.observacionesRegistro")} required>
                <Input
                  data-testid="input-notes-registry"
                  value={registryNotes}
                  onChange={(e) => setRegistryNotes(e.target.value)}
                />
              </FormField>
              <FormField label={t("fields.fechaSubsanacion")} required>
                <Input
                  type="date"
                  data-testid="input-correction-date"
                  value={dateCorrection}
                  onChange={(e) => setDateCorrection(e.target.value)}
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setObserveOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button
                onClick={handleObserve}
                disabled={!registryNotes || !dateCorrection || observeMutation.isPending}
                data-testid="btn-confirm-observe"
              >
                {t("observar")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <Dialog open={registerOpen} onOpenChange={setRegisterOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={t("inscribir")}>
              <FormField label={t("fields.fechaRecepcion")} required>
                <Input
                  type="date"
                  data-testid="input-receipt-date"
                  value={dateReception}
                  onChange={(e) => setDateReception(e.target.value)}
                />
              </FormField>
              <FormField label={t("fields.numeroInscripcionDefinitivo")} required>
                <Input
                  data-testid="input-final-registration-number"
                  value={finalRegistrationNumber}
                  onChange={(e) => setFinalRegistrationNumber(e.target.value)}
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setRegisterOpen(false)}>
                {tc("cancel")}
              </Button>
              <Button
                onClick={handleRegister}
                disabled={
                  !dateReception || !finalRegistrationNumber || registerMutation.isPending
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
