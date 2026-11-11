"use client";

import { useEffect, useState } from "react";
import { Loader2 } from "lucide-react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { CheckboxField, FormField } from "@/theme/form-patterns";
import { mfaUseCases } from "@/composition/mfa";
import { InvalidCodeFormatError, type EnrollmentStart } from "@/core/mfa/domain/mfa";
import { httpStatus } from "@/lib/http-error";

type Phase =
  | { kind: "loading" }
  | { kind: "error"; messageKey: string }
  | { kind: "confirm"; start: EnrollmentStart }
  | { kind: "codes"; recoveryCodes: string[] };

interface MfaEnrollmentPanelProps {
  /** Enrolment-scoped login token; omitted when the user already has a session. */
  bearerToken?: string;
  onDone: () => void;
}

function beginErrorKey(err: unknown): string {
  const status = httpStatus(err);
  if (status === 409) return "alreadyEnrolled";
  if (status === 429) return "locked";
  return "beginError";
}

function confirmErrorKey(err: unknown): string {
  if (err instanceof InvalidCodeFormatError) return "codeInvalid";
  const status = httpStatus(err);
  if (status === 400) return "confirmWrong";
  if (status === 429) return "locked";
  return "confirmError";
}

export function MfaEnrollmentPanel({ bearerToken, onDone }: MfaEnrollmentPanelProps) {
  const t = useTranslations("mfa");
  const [phase, setPhase] = useState<Phase>({ kind: "loading" });
  const [code, setCode] = useState("");
  const [confirmError, setConfirmError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [saved, setSaved] = useState(false);

  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    let active = true;
    mfaUseCases
      .beginEnrollment(bearerToken)
      .then((start) => active && setPhase({ kind: "confirm", start }))
      .catch((err) => active && setPhase({ kind: "error", messageKey: beginErrorKey(err) }));
    return () => {
      active = false;
    };
  }, [bearerToken, attempt]);

  function retry() {
    setPhase({ kind: "loading" });
    setAttempt((n) => n + 1);
  }

  async function handleConfirm(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setConfirmError(null);
    try {
      const recoveryCodes = await mfaUseCases.confirmEnrollment(code, bearerToken);
      setCode("");
      setPhase({ kind: "codes", recoveryCodes });
    } catch (err) {
      setConfirmError(confirmErrorKey(err));
    } finally {
      setBusy(false);
    }
  }

  if (phase.kind === "loading") {
    return (
      <p data-testid="mfa-loading" role="status" className="flex items-center gap-2 text-muted-foreground">
        <Loader2 className="h-5 w-5 animate-spin" aria-hidden="true" />
        {t("loading")}
      </p>
    );
  }

  if (phase.kind === "error") {
    return (
      <div className="space-y-4">
        <p data-testid="mfa-error" role="alert" className="text-sm font-medium text-destructive">
          {t(phase.messageKey)}
        </p>
        <Button type="button" variant="outline" data-testid="btn-mfa-retry" onClick={retry}>
          {t("retry")}
        </Button>
      </div>
    );
  }

  if (phase.kind === "codes") {
    return (
      <div className="space-y-4">
        <h3 className="text-base font-semibold text-foreground">{t("recoveryTitle")}</h3>
        <p className="text-sm text-muted-foreground">{t("recoveryWarning")}</p>
        <ul
          data-testid="mfa-recovery-codes"
          className="grid grid-cols-2 gap-2 rounded-[12px] bg-secondary/60 p-4 font-mono text-sm"
        >
          {phase.recoveryCodes.map((recovery) => (
            <li key={recovery}>{recovery}</li>
          ))}
        </ul>
        <CheckboxField data-testid="check-mfa-saved" label={t("saved")} checked={saved} onChange={setSaved} />
        <Button
          type="button"
          variant="apple"
          className="w-full"
          data-testid="btn-mfa-done"
          disabled={!saved}
          onClick={onDone}
        >
          {t("done")}
        </Button>
      </div>
    );
  }

  return (
    <form onSubmit={handleConfirm} className="space-y-4">
      <p className="text-sm text-muted-foreground">{t("scanHint")}</p>
      <div className="space-y-2 rounded-[12px] bg-secondary/60 p-4">
        <p className="text-xs font-semibold text-muted-foreground">{t("secretLabel")}</p>
        <p data-testid="mfa-secret" className="break-all font-mono text-base">
          {phase.start.secret}
        </p>
        <a
          data-testid="mfa-otpauth-link"
          href={phase.start.otpauthUri}
          className="text-sm font-medium text-primary-text underline"
        >
          {t("openInApp")}
        </a>
      </div>
      <FormField label={t("confirmLabel")} required>
        <Input
          data-testid="input-mfa-confirm-code"
          className="h-12 text-lg tracking-widest"
          value={code}
          onChange={(e) => setCode(e.target.value)}
          inputMode="numeric"
          autoComplete="one-time-code"
          disabled={busy}
        />
      </FormField>
      {confirmError && (
        <p data-testid="mfa-error" role="alert" className="text-sm font-medium text-destructive">
          {t(confirmError)}
        </p>
      )}
      <Button type="submit" variant="apple" className="w-full" data-testid="btn-mfa-confirm" disabled={busy}>
        {busy && <Loader2 className="mr-2 h-4 w-4 animate-spin" aria-hidden="true" />}
        {t("confirmSubmit")}
      </Button>
    </form>
  );
}
