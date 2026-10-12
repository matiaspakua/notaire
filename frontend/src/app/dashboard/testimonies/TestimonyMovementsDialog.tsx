"use client";
/**
 * Read-only movement history of one testimony (#1364, CU11/CU12/CU44): each registry movement,
 * newest first, with its entry, inscription and withdrawal. Movements are recorded by the
 * actions on the testimony-movements screen; manual correction stays API-only (Owner, 2026-10-09).
 * The data comes with the testimony list (GET /testimonies carries testimonyMovements).
 */
import { useTranslations } from "next-intl";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormSection } from "@/theme/form-patterns";
import { useFormat } from "@/hooks/useFormat";
import type { Testimony, TestimonyMovement } from "@/types";

function newestFirst(a: TestimonyMovement, b: TestimonyMovement): number {
  const da = a.dateEntry ?? "";
  const db = b.dateEntry ?? "";
  if (da !== db) return da < db ? 1 : -1;
  return (b.idTestimonyMovement ?? 0) - (a.idTestimonyMovement ?? 0);
}

export function TestimonyMovementsDialog({ testimony, onClose }: { testimony: Testimony | null; onClose: () => void }) {
  const t = useTranslations("testimonios.movements");
  const fmt = useFormat();
  const movements = [...(testimony?.testimonyMovements ?? [])].sort(newestFirst);

  return (
    <Dialog open={testimony !== null} onOpenChange={(open) => { if (!open) onClose(); }}>
      <DialogContent className="max-w-xl">
        <FormContainer>
          <FormSection dialogTitle title={t("title", { number: testimony?.number ?? "" })}>
            {movements.length === 0 ? (
              <p className="text-sm text-muted-foreground">{t("empty")}</p>
            ) : (
              <ol className="flex flex-col gap-3" aria-label={t("title", { number: testimony?.number ?? "" })}>
                {movements.map((m) => (
                  <li
                    key={m.idTestimonyMovement}
                    className="rounded-xl border border-border/60 p-3 text-sm"
                    data-testid={`testimony-movement-${m.idTestimonyMovement}`}
                  >
                    <dl className="grid grid-cols-[auto_1fr] gap-x-3 gap-y-1">
                      <dt className="text-muted-foreground">{t("entered")}</dt>
                      <dd>{fmt.date(m.dateEntry) || "—"}</dd>
                      <dt className="text-muted-foreground">{m.registered ? t("registered") : t("pending")}</dt>
                      <dd>{m.registered ? fmt.date(m.dateRegistration) || "—" : "—"}</dd>
                      {m.dateExit && (
                        <>
                          <dt className="text-muted-foreground">{t("withdrawn")}</dt>
                          <dd>
                            {fmt.date(m.dateExit)}
                            {m.cardNumber ? ` · ${t("card")} ${m.cardNumber}` : ""}
                          </dd>
                        </>
                      )}
                    </dl>
                    {m.observedByRegistry && <p className="mt-2 font-medium text-destructive">{t("observed")}</p>}
                    {m.notes && <p className="mt-1 text-muted-foreground break-words">{m.notes}</p>}
                  </li>
                ))}
              </ol>
            )}
          </FormSection>
        </FormContainer>
      </DialogContent>
    </Dialog>
  );
}
