"use client";
/**
 * #1555 / CU07: register a testimony manually (bare POST /testimonies) with a chosen deed,
 * number, notes and verified flag. 400 field errors show inline; 404/409 as a toast.
 */
import { useState } from "react";
import { toast } from "sonner";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormSection, FormField, FormActions, CheckboxField } from "@/theme/form-patterns";
import { DeedPicker } from "@/components/shared/DeedPicker";
import { useCreateTestimony } from "@/hooks/useTestimonies";
import { ApiError } from "@/lib/api-client";
import { extractApiError } from "@/lib/utils";
import { parseFieldErrors } from "@/lib/mutation-error";

const FIELDS = ["deed", "deed.idDeed", "number", "notes"];

export function RegisterTestimonyDialog({ open, onClose }: { open: boolean; onClose: () => void }) {
  const t = useTranslations("testimonios.registrar");
  const tc = useTranslations("common");
  const create = useCreateTestimony();
  const [idDeed, setIdDeed] = useState<number | undefined>();
  const [number, setNumber] = useState("");
  const [notes, setNotes] = useState("");
  const [verified, setVerified] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  function reset() {
    setIdDeed(undefined);
    setNumber("");
    setNotes("");
    setVerified(false);
    setErrors({});
  }

  function close() {
    reset();
    onClose();
  }

  async function handleSave() {
    const next: Record<string, string> = {};
    if (idDeed === undefined) next.deed = t("deedRequired");
    if (!/^[1-9]\d*$/.test(number.trim())) next.number = t("numberRequired");
    setErrors(next);
    if (Object.keys(next).length > 0) return;
    try {
      await create.mutateAsync({ deed: { idDeed: idDeed! }, number: Number(number.trim()), notes: notes.trim(), flagged: false, verified });
      toast.success(t("created"));
      close();
    } catch (err) {
      if (err instanceof ApiError && err.status === 404) {
        toast.error(t("deedNotFound"));
        return;
      }
      if (err instanceof ApiError && err.status === 409) {
        toast.error(t("conflict"));
        return;
      }
      const message = extractApiError(err) ?? t("error");
      const fieldErrors = parseFieldErrors(message, FIELDS);
      if (fieldErrors["deed.idDeed"]) fieldErrors.deed = fieldErrors["deed.idDeed"];
      if (Object.keys(fieldErrors).length > 0) setErrors(fieldErrors);
      else toast.error(message);
    }
  }

  const fieldError = (key: string) =>
    errors[key] ? (
      <p className="mt-1 text-sm text-destructive" data-testid={`register-testimony-error-${key}`}>
        {errors[key]}
      </p>
    ) : null;

  return (
    <Dialog open={open} onOpenChange={(v) => !v && close()}>
      <DialogContent>
        <FormContainer>
          <FormSection dialogTitle title={t("title")}>
            <FormField label={t("deed")} required>
              <DeedPicker
                value={idDeed}
                onChange={(deed) => setIdDeed(deed?.idDeed ?? undefined)}
                aria-label={t("deed")}
                data-testid="select-deed-register-testimony"
              />
              {fieldError("deed")}
            </FormField>
            <FormField label={t("number")} required>
              <Input
                type="number"
                min={1}
                value={number}
                onChange={(e) => setNumber(e.target.value)}
                aria-label={t("number")}
                aria-invalid={!!errors.number}
                data-testid="input-number-register-testimony"
              />
              {fieldError("number")}
            </FormField>
            <FormField label={t("notes")}>
              <Input
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                aria-label={t("notes")}
                data-testid="input-notes-register-testimony"
              />
              {fieldError("notes")}
            </FormField>
            <CheckboxField label={t("verified")} checked={verified} onChange={setVerified} />
          </FormSection>
          <FormActions align="right">
            <Button variant="secondary" onClick={close}>
              {tc("cancel")}
            </Button>
            <Button onClick={handleSave} disabled={create.isPending} data-testid="btn-save-register-testimony">
              {tc("save")}
            </Button>
          </FormActions>
        </FormContainer>
      </DialogContent>
    </Dialog>
  );
}
