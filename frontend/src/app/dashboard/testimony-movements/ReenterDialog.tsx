"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { FormContainer, FormSection, FormField, FormActions, CheckboxField } from "@/theme/form-patterns";
import { useThemeClasses } from "@/theme";

export interface DatosReingreso {
  numberCard: number;
  observadoPorRegistro: boolean;
  notes: string;
}

interface ReingresarDialogProps {
  open: boolean;
  isPending: boolean;
  onCancel: () => void;
  onConfirm: (datos: DatosReingreso) => void;
}

/** CU44 step 5 - asks for the card number, whether the registry flagged it, and notes. */
export function ReenterDialog({ open, isPending, onCancel, onConfirm }: ReingresarDialogProps) {
  const t = useTranslations("movimientosTestimonio");
  const tc = useTranslations("common");
  const themeClass = useThemeClasses();

  const [numberCard, setNumberCard] = useState("");
  const [observado, setObservado] = useState(false);
  const [notes, setNotes] = useState("");

  const missingNotes = observado && notes.trim() === "";

  return (
    <Dialog open={open} onOpenChange={(v) => !v && onCancel()}>
      <DialogContent>
        <FormContainer>
          <FormSection dialogTitle title={t("reingresar")}>
            <FormField label={t("fields.numeroCarton")}>
              <Input type="number" value={numberCard} onChange={(e) => setNumberCard(e.target.value)} data-testid="input-reentry-carton" />
            </FormField>
            <CheckboxField label={t("fields.observadoPorRegistro")} checked={observado} onChange={setObservado} data-testid="checkbox-reentry-observed" />
            <FormField label={t("fields.observaciones")} required={observado}>
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
              onClick={() => onConfirm({ numberCard: Number(numberCard) || 0, observadoPorRegistro: observado, notes })}
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
