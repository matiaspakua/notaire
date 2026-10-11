"use client";

import { useState } from "react";
import { toast } from "sonner";
import { LogIn, Stamp, LogOut, RotateCcw } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useTestimonies } from "@/hooks/useTestimonies";
import { useEnterRegistration, useRegisterInscription, useWithdraw, useReenter } from "@/hooks/useTestimonyMovements";
import { extractApiError, formatDate } from "@/lib/utils";
import { ReenterDialog, type ReentryDetails } from "./ReenterDialog";
import type { Testimony, TestimonyMovement } from "@/types";

type StatusMovement = "sinIngresar" | "ingresado" | "inscripto" | "retirado";

function latestMovement(testimony: Testimony): TestimonyMovement | undefined {
  const movements = testimony.testimonyMovements ?? [];
  return movements.reduce<TestimonyMovement | undefined>(
    (latest, m) => (!latest || (m.idTestimonyMovement ?? 0) > (latest.idTestimonyMovement ?? 0) ? m : latest),
    undefined
  );
}

function statusMovement(testimony: Testimony): StatusMovement {
  const movement = latestMovement(testimony);
  if (!movement) return "sinIngresar";
  if (movement.dateExit) return "retirado";
  if (movement.registered) return "inscripto";
  return "ingresado";
}

export default function MovementsTestimonyPage() {
  const t = useTranslations("movimientosTestimonio");
  const tc = useTranslations("common");

  const { data: testimonies = [], isLoading } = useTestimonies();
  const verified = testimonies.filter((te) => te.verified);

  const enterMutation = useEnterRegistration();
  const registerMutation = useRegisterInscription();
  const withdrawMutation = useWithdraw();
  const reenterMutation = useReenter();

  const [withdrawId, setWithdrawId] = useState<number | null>(null);
  const [cardNumber, setCardNumber] = useState("");
  const [reenterId, setReenterId] = useState<number | null>(null);

  async function handleEnter(idTestimony: number) {
    try {
      await enterMutation.mutateAsync(idTestimony);
      toast.success(t("ingresado"));
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorIngresar"));
    }
  }

  async function handleRegister(idTestimony: number) {
    try {
      await registerMutation.mutateAsync(idTestimony);
      toast.success(t("inscripto"));
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorRegistrar"));
    }
  }

  function openWithdraw(idTestimony: number) {
    setWithdrawId(idTestimony);
    setCardNumber("");
  }

  async function handleWithdraw() {
    if (!withdrawId) return;
    try {
      await withdrawMutation.mutateAsync({ idTestimony: withdrawId, cardNumber: Number(cardNumber) });
      toast.success(t("retirado"));
      setWithdrawId(null);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorRetirar"));
    }
  }

  async function handleReenter(details: ReentryDetails) {
    if (!reenterId) return;
    try {
      await reenterMutation.mutateAsync({ idTestimony: reenterId, ...details });
      toast.success(t("reingresado"));
      setReenterId(null);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorReingresar"));
    }
  }

  const columns: Column<Testimony>[] = [
    { key: "number", header: t("fields.numero"), render: (te) => <span className="font-medium">{te.number}</span> },
    { key: "deed", header: t("fields.escritura"), render: (te) => te.deed?.number ?? "—" },
    { key: "status", header: t("fields.estado"), render: (te) => t(`estados.${statusMovement(te)}`) },
    { key: "entryDate", header: t("fields.fechaIngreso"), render: (te) => formatDate(latestMovement(te)?.dateEntry) },
    { key: "registrationDate", header: t("fields.fechaInscripcion"), render: (te) => formatDate(latestMovement(te)?.dateRegistration) },
    { key: "exitDate", header: t("fields.fechaSalida"), render: (te) => formatDate(latestMovement(te)?.dateExit) },
    {
      key: "actions",
      header: "",
      className: "w-40",
      render: (te) => {
        const status = statusMovement(te);
        const id = te.idTestimony!;
        return (
          <div className="flex gap-2 justify-end">
            {status === "sinIngresar" && (
              <Button size="sm" variant="ghost" onClick={() => handleEnter(id)} aria-label={t("ingresar")} data-testid={`btn-sign-in-testimony-${id}`}>
                <LogIn className="h-4 w-4" />
              </Button>
            )}
            {status === "ingresado" && (
              <Button size="sm" variant="ghost" onClick={() => handleRegister(id)} aria-label={t("registrarInscripcion")} data-testid={`btn-record-registration-${id}`}>
                <Stamp className="h-4 w-4" />
              </Button>
            )}
            {status === "inscripto" && (
              <Button size="sm" variant="ghost" onClick={() => openWithdraw(id)} aria-label={t("retirar")} data-testid={`btn-withdraw-testimony-${id}`}>
                <LogOut className="h-4 w-4" />
              </Button>
            )}
            {status === "retirado" && (
              <Button size="sm" variant="ghost" onClick={() => setReenterId(id)} aria-label={t("reingresar")} data-testid={`btn-reenter-testimony-${id}`}>
                <RotateCcw className="h-4 w-4" />
              </Button>
            )}
          </div>
        );
      },
    },
  ];

  return (
    <div>
      <AppHeader title={t("title")} />
      <DataTable data={verified} columns={columns} isLoading={isLoading} keyExtractor={(te) => te.idTestimony!} emptyMessage={t("noData")} />

      <Dialog open={!!withdrawId} onOpenChange={(v) => !v && setWithdrawId(null)}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={t("retirar")}>
              <FormField label={t("fields.numeroCarton")} required>
                <Input type="number" value={cardNumber} onChange={(e) => setCardNumber(e.target.value)} data-testid="input-card-number" />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setWithdrawId(null)}>
                {tc("cancel")}
              </Button>
              <Button onClick={handleWithdraw} disabled={!cardNumber || withdrawMutation.isPending} data-testid="btn-confirm-withdraw">
                {t("retirar")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      {reenterId !== null && (
        <ReenterDialog open isPending={reenterMutation.isPending} onCancel={() => setReenterId(null)} onConfirm={handleReenter} />
      )}
    </div>
  );
}
