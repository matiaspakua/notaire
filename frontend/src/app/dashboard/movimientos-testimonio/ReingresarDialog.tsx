"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { FormContainer, FormSection, FormField, FormActions, CheckboxField } from "@/theme/form-patterns";
import { useThemeClasses } from "@/theme";

export interface DatosReingreso {
  numeroCarton: number;
  observadoPorRegistro: boolean;
  observaciones: string;
}

interface ReingresarDialogProps {
  open: boolean;
  isPending: boolean;
  onCancel: () => void;
  onConfirm: (datos: DatosReingreso) => void;
}

/** CU44 paso 5 - solicita número de cartón, si fue observado por el registro y observaciones. */
export function ReingresarDialog({ open, isPending, onCancel, onConfirm }: ReingresarDialogProps) {
  const t = useTranslations("movimientosTestimonio");
  const tc = useTranslations("common");
  const themeClass = useThemeClasses();

  const [numeroCarton, setNumeroCarton] = useState("");
  const [observado, setObservado] = useState(false);
  const [observaciones, setObservaciones] = useState("");

  const faltanObservaciones = observado && observaciones.trim() === "";

  return (
    <Dialog open={open} onOpenChange={(v) => !v && onCancel()}>
      <DialogContent>
        <FormContainer>
          <FormSection title={t("reingresar")}>
            <FormField label={t("fields.numeroCarton")}>
              <Input type="number" value={numeroCarton} onChange={(e) => setNumeroCarton(e.target.value)} data-testid="input-reingreso-carton" />
            </FormField>
            <CheckboxField label={t("fields.observadoPorRegistro")} checked={observado} onChange={setObservado} data-testid="checkbox-reingreso-observado" />
            <FormField label={t("fields.observaciones")} required={observado}>
              <textarea
                className={themeClass("textarea")}
                value={observaciones}
                onChange={(e) => setObservaciones(e.target.value)}
                data-testid="input-reingreso-observaciones"
              />
            </FormField>
          </FormSection>
          <FormActions align="right">
            <Button variant="secondary" onClick={onCancel}>
              {tc("cancel")}
            </Button>
            <Button
              onClick={() => onConfirm({ numeroCarton: Number(numeroCarton) || 0, observadoPorRegistro: observado, observaciones })}
              disabled={faltanObservaciones || isPending}
              data-testid="btn-confirmar-reingreso"
            >
              {t("reingresar")}
            </Button>
          </FormActions>
        </FormContainer>
      </DialogContent>
    </Dialog>
  );
}
