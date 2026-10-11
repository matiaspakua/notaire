"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { FormContainer, FormSection, FormField, FormActions, CheckboxField } from "@/theme/form-patterns";
import { useThemeClasses } from "@/theme";

export interface ReentryDetails {
  numberCard: number;
  observedByRegistry: boolean;
  notes: string;
}

interface ReenterDialogProps {
  open: boolean;
  isPending: boolean;
  onCancel: () => void;
  onConfirm: (details: ReentryDetails) => void;
}

/** CU44 step 5 - asks for the card number, whether the registry flagged it, and notes. */
export function ReenterDialog({ open, isPending, onCancel, onConfirm }: ReenterDialogProps) {
  const t = useTranslations("movimientosTestimonio");
  const tc = useTranslations("common");
  const themeClass = useThemeClasses();

  const [numberCard, setNumberCard] = useState("");
  const [observedByRegistry, setObservedByRegistry] = useState(false);
  const [notes, setNotes] = useState("");

  const missingNotes = observedByRegistry && notes.trim() === "";

  return (
    <Dialog open={open} onOpenChange={(v) => !v && onCancel()}>
      <DialogContent>
        <FormContainer>
          <FormSection dialogTitle title={t("reingresar")}>
            <FormField label={t("fields.numeroCarton")}>
              <Input type="number" value={numberCard} onChange={(e) => setNumberCard(e.target.value)} data-testid="input-reentry-carton" />
            </FormField>
            <CheckboxField label={t("fields.observadoPorRegistro")} checked={observedByRegistry} onChange={setObservedByRegistry} data-testid="checkbox-reentry-observed" />
            <FormField label={t("fields.observaciones")} required={observedByRegistry}>
              <textarea
                className={themeClass("textarea")}
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                data-testid="input-reentry-notes"
              />
            </FormField>
          </FormSection>
          <FormActions align="right">
            <Button variant="secondary" onClick={onCancel}>
              {tc("cancel")}
            </Button>
            <Button
              onClick={() => onConfirm({ numberCard: Number(numberCard) || 0, observedByRegistry, notes })}
              disabled={missingNotes || isPending}
              data-testid="btn-confirm-reentry"
            >
              {t("reingresar")}
            </Button>
          </FormActions>
        </FormContainer>
      </DialogContent>
    </Dialog>
  );
}
