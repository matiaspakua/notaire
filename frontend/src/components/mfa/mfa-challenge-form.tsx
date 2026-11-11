"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Loader2 } from "lucide-react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import { FormActions, FormContainer } from "@/theme/form-patterns";
import { MfaCodeInput } from "@/components/mfa/mfa-code-input";
import { mfaUseCases } from "@/composition/mfa";
import { resolveLoginOutcome } from "@/core/auth/domain/auth";
import type { LoginResult } from "@/core/auth/domain/auth";
import { InvalidCodeFormatError, type SecondFactorMode } from "@/core/mfa/domain/mfa";
import { errorBody, httpStatus, lockoutMessage } from "@/lib/http-error";

interface MfaChallengeFormProps {
  mfaToken: string;
  onBack: () => void;
  onSession: (user: LoginResult) => void;
}

export function MfaChallengeForm({ mfaToken, onBack, onSession }: MfaChallengeFormProps) {
  const t = useTranslations("login");
  const [mode, setMode] = useState<SecondFactorMode>("totp");
  const [value, setValue] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      const outcome = resolveLoginOutcome(await mfaUseCases.completeLogin({ mfaToken, mode, value }));
      if (outcome.kind === "session") {
        onSession(outcome.user);
        return;
      }
      toast.error(t("mfaError"));
      setValue("");
    } catch (error) {
      if (error instanceof InvalidCodeFormatError) {
        toast.error(t("mfaCodeInvalid"));
      } else if (httpStatus(error) === 429) {
        toast.error(lockoutMessage(errorBody(error), t("lockoutError")));
      } else {
        toast.error(t("connectionError"));
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <FormContainer>
        <h2 className="text-center text-lg font-semibold tracking-tight text-foreground">{t("mfaTitle")}</h2>
        <p className="text-center text-sm text-muted-foreground">{t("mfaDescription")}</p>
        <MfaCodeInput
          mode={mode}
          value={value}
          onChange={setValue}
          onModeChange={setMode}
          inputTestId="input-mfa-code"
          toggleTestId="toggle-mfa-recovery"
          disabled={loading}
          autoFocus
        />
        <FormActions align="center">
          <div className="flex w-full flex-col gap-3">
            <Button
              type="submit"
              size="lg"
              variant="apple"
              className="h-12 w-full text-lg font-semibold"
              disabled={loading}
              data-testid="btn-mfa-verify"
            >
              {loading && <Loader2 className="mr-2 h-5 w-5 animate-spin" aria-hidden="true" />}
              {t("mfaVerify")}
            </Button>
            <Button type="button" variant="ghost" data-testid="btn-mfa-back" disabled={loading} onClick={onBack}>
              {t("mfaBack")}
            </Button>
          </div>
        </FormActions>
      </FormContainer>
    </form>
  );
}
