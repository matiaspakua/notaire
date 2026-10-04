"use client";

import { useTranslations } from "next-intl";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormHeader, FormSection } from "@/theme/form-patterns";
import { useGestionResumenCaso, useGestionResumenFinanciero } from "@/hooks/useGestionResumen";
import { formatCurrency } from "@/lib/utils";

interface GestionResumenDialogProps {
  gestionId: number | null;
  onClose: () => void;
}

/** #774 - Escrituras, testimonios (with registry state), copias and pagos of a gestión. */
export function GestionResumenDialog({ gestionId, onClose }: GestionResumenDialogProps) {
  const t = useTranslations("gestiones.resumen");
  const { data: caso, isLoading } = useGestionResumenCaso(gestionId ?? undefined);
  const { data: financiero } = useGestionResumenFinanciero(gestionId ?? undefined);

  const deeds = caso?.deeds ?? [];

  return (
    <Dialog open={!!gestionId} onOpenChange={(open) => !open && onClose()}>
      <DialogContent data-testid="dialog-resumen-caso">
        <FormContainer>
          <FormHeader title={t("title")} description={caso ? `${caso.managementNumber} — ${caso.heading ?? ""}` : undefined} />
          {isLoading ? (
            <p className="text-sm text-muted-foreground">{t("loading")}</p>
          ) : (
            <>
              <FormSection title={t("escrituras")}>
                {deeds.length === 0 ? (
                  <p className="text-sm text-muted-foreground" data-testid="resumen-sin-escrituras">{t("sinEscrituras")}</p>
                ) : (
                  deeds.map((deed) => (
                    <div key={deed.idDeed} className="border-b pb-2" data-testid="resumen-escritura">
                      <div className="font-medium">{t("escritura", { numero: deed.number })}</div>
                      {deed.testimonies.map((testimony) => (
                        <div key={testimony.idTestimony} className="text-sm" data-testid="resumen-testimonio">
                          {t("testimonio", { numero: testimony.number })}: {t(`estados.${testimony.state}`)}
                          {testimony.verified ? ` · ${t("verificado")}` : ""}
                          {testimony.flagged ? ` · ${t("observado")}` : ""}
                          {` · ${t("copias", { cantidad: testimony.copies })}`}
                        </div>
                      ))}
                    </div>
                  ))
                )}
              </FormSection>
              <FormSection title={t("pagos")}>
                {financiero ? (
                  <div className="text-sm space-y-1" data-testid="resumen-pagos">
                    <div>{t("totalPresupuestado")}: {formatCurrency(financiero.totalPresupuestado)}</div>
                    <div>{t("totalCobrado")}: {formatCurrency(financiero.totalCobrado)}</div>
                    <div>{t("saldoPendiente")}: {formatCurrency(financiero.pendingBalance)}</div>
                  </div>
                ) : (
                  <p className="text-sm text-muted-foreground">{t("sinPagos")}</p>
                )}
              </FormSection>
            </>
          )}
        </FormContainer>
      </DialogContent>
    </Dialog>
  );
}
