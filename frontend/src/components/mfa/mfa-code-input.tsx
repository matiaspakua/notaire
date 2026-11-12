"use client";

import { useTranslations } from "next-intl";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { FormField } from "@/theme/form-patterns";
import type { SecondFactorMode } from "@/core/mfa/domain/mfa";

interface MfaCodeInputProps {
  mode: SecondFactorMode;
  value: string;
  onChange: (value: string) => void;
  onModeChange: (mode: SecondFactorMode) => void;
  inputTestId: string;
  toggleTestId: string;
  disabled?: boolean;
  autoFocus?: boolean;
  error?: string;
}

/** One field for a 6-digit TOTP or a recovery code, with an explicit mode toggle. */
export function MfaCodeInput({
  mode,
  value,
  onChange,
  onModeChange,
  inputTestId,
  toggleTestId,
  disabled,
  autoFocus,
  error,
}: MfaCodeInputProps) {
  const t = useTranslations("mfa");
  const isTotp = mode === "totp";
  return (
    <>
      <FormField label={isTotp ? t("codeLabel") : t("recoveryLabel")} required error={error}>
        <Input
          data-testid={inputTestId}
          className="h-12 text-lg tracking-widest"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          inputMode={isTotp ? "numeric" : "text"}
          autoComplete={isTotp ? "one-time-code" : "off"}
          autoFocus={autoFocus}
          disabled={disabled}
        />
      </FormField>
      <Button
        type="button"
        variant="link"
        className="self-start px-0"
        data-testid={toggleTestId}
        disabled={disabled}
        onClick={() => {
          onChange("");
          onModeChange(isTotp ? "recovery" : "totp");
        }}
      >
        {isTotp ? t("useRecovery") : t("useTotp")}
      </Button>
    </>
  );
}
