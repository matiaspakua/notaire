"use client";

import { useState } from "react";
import { Loader2 } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { Button } from "@/components/ui/button";
import { FormActions, FormContainer, FormSection } from "@/theme/form-patterns";
import { MfaCodeInput } from "@/components/mfa/mfa-code-input";
import { MfaEnrollmentPanel } from "@/components/mfa/mfa-enrollment-panel";
import { mfaUseCases } from "@/composition/mfa";
import { InvalidCodeFormatError, type SecondFactorMode } from "@/core/mfa/domain/mfa";
import { httpStatus } from "@/lib/http-error";

function disableErrorKey(err: unknown): string {
  if (err instanceof InvalidCodeFormatError) return "codeInvalid";
  const status = httpStatus(err);
  if (status === 400) return "disableWrong";
  if (status === 404) return "notEnrolled";
  if (status === 429) return "locked";
  return "disableError";
}

export default function SecurityPage() {
  const t = useTranslations("mfa");
  const [enrolling, setEnrolling] = useState(false);
  const [mode, setMode] = useState<SecondFactorMode>("totp");
  const [value, setValue] = useState("");
  const [busy, setBusy] = useState(false);
  const [disabled, setDisabled] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleDisable(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setDisabled(false);
    try {
      await mfaUseCases.disable({ mode, value });
      setValue("");
      setDisabled(true);
    } catch (err) {
      setError(disableErrorKey(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <AppHeader title={t("pageTitle")} description={t("pageDescription")} />
      <div className="mx-auto w-full max-w-[600px] space-y-8 px-6 py-8">
        <FormSection title={t("startTitle")} subtitle={t("startDescription")}>
          {enrolling ? (
            <MfaEnrollmentPanel onDone={() => setEnrolling(false)} />
          ) : (
            <Button type="button" variant="apple" data-testid="btn-mfa-start" onClick={() => setEnrolling(true)}>
              {t("start")}
            </Button>
          )}
        </FormSection>

        <form onSubmit={handleDisable}>
          <FormContainer>
            <FormSection title={t("disableTitle")} subtitle={t("disableDescription")}>
              <MfaCodeInput
                mode={mode}
                value={value}
                onChange={setValue}
                onModeChange={setMode}
                inputTestId="input-mfa-disable-code"
                toggleTestId="toggle-mfa-disable-recovery"
                disabled={busy}
              />
              {error && (
                <p data-testid="mfa-disable-error" role="alert" className="text-sm font-medium text-destructive">
                  {t(error)}
                </p>
              )}
              {disabled && (
                <p data-testid="mfa-disabled-notice" role="status" className="text-sm font-medium text-success">
                  {t("disabledNotice")}
                </p>
              )}
            </FormSection>
            <FormActions align="left">
              <Button type="submit" variant="destructive" data-testid="btn-mfa-disable" disabled={busy}>
                {busy && <Loader2 className="mr-2 h-4 w-4 animate-spin" aria-hidden="true" />}
                {t("disableSubmit")}
              </Button>
            </FormActions>
          </FormContainer>
        </form>
      </div>
    </>
  );
}
