"use client";

import { useTranslations } from "next-intl";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormHeader, FormSection } from "@/theme/form-patterns";
import { useManagementCaseSummary, useManagementFinancialSummary } from "@/hooks/useManagementSummary";
import { formatCurrency, formatDate } from "@/lib/utils";
import type { CaseDocument } from "@/types";

function documentFlags(document: CaseDocument, t: (key: string) => string): string[] {
  const flags: [boolean, string][] = [
    [document.prepared, "preparado"],
    [document.released, "liberado"],
    [document.observed, "observado"],
    [document.delivered, "entregado"],
    [document.reentered, "reingresado"],
  ];
  return flags.filter(([active]) => active).map(([, key]) => t(key));
}

interface ManagementSummaryDialogProps {
  managementId: number | null;
  onClose: () => void;
}

/** #774 - Escrituras, testimonies (with registry state), copias and pagos of a gestión. */
export function ManagementSummaryDialog({ managementId, onClose }: ManagementSummaryDialogProps) {
  const t = useTranslations("gestiones.resumen");
  const { data: caso, isLoading } = useManagementCaseSummary(managementId ?? undefined);
  const { data: financiero } = useManagementFinancialSummary(managementId ?? undefined);

  const deeds = caso?.deeds ?? [];
  const documents = caso?.documents ?? [];

  return (
    <Dialog open={!!managementId} onOpenChange={(open) => !open && onClose()}>
      <DialogContent data-testid="dialog-summary-case">
        <FormContainer>
          <FormHeader dialogTitle title={t("title")} description={caso ? `${caso.managementNumber} — ${caso.heading ?? ""}` : undefined} />
          {isLoading ? (
            <p className="text-sm text-muted-foreground">{t("loading")}</p>
          ) : (
            <>
              <FormSection title={t("escrituras")}>
                {deeds.length === 0 ? (
                  <p className="text-sm text-muted-foreground" data-testid="summary-empty-deeds">{t("sinEscrituras")}</p>
                ) : (
                  deeds.map((deed) => (
                    <div key={deed.idDeed} className="border-b pb-2" data-testid="summary-deed">
                      <div className="font-medium">{t("escritura", { numero: deed.number })}</div>
                      {deed.testimonies.map((testimony) => (
                        <div key={testimony.idTestimony} className="text-sm" data-testid="summary-testimony">
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
              <FormSection title={t("documentos")}>
                {documents.length === 0 ? (
                  <p className="text-sm text-muted-foreground" data-testid="summary-empty-documents">{t("sinDocumentos")}</p>
                ) : (
                  documents.map((document) => (
                    <div key={document.idSubmittedDocument} className="text-sm border-b pb-2" data-testid="summary-document">
                      <div className="font-medium">{document.typeName ?? document.name ?? "—"}</div>
                      <div className="text-muted-foreground">
                        {t("tramiteDocumento", { id: document.idProcedure })}
                        {document.dateDue ? ` · ${t("vence", { fecha: formatDate(document.dateDue) })}` : ""}
                        {documentFlags(document, t).map((flag) => ` · ${flag}`).join("")}
                      </div>
                    </div>
                  ))
                )}
              </FormSection>
              <FormSection title={t("pagos")}>
                {financiero ? (
                  <div className="text-sm space-y-1" data-testid="summary-payments">
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
