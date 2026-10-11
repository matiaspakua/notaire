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
  const verificados = testimonies.filter((te) => te.verified);

  const ingresarMutation = useEnterRegistration();
  const registrarMutation = useRegisterInscription();
  const retirarMutation = useWithdraw();
  const reingresarMutation = useReenter();

  const [retirarId, setRetirarId] = useState<number | null>(null);
  const [numberCard, setNumberCard] = useState("");
  const [reingresarId, setReingresarId] = useState<number | null>(null);

  async function handleIngresar(idTestimony: number) {
    try {
      await ingresarMutation.mutateAsync(idTestimony);
      toast.success(t("ingresado"));
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorIngresar"));
    }
  }

  async function handleRegistrar(idTestimony: number) {
    try {
      await registrarMutation.mutateAsync(idTestimony);
      toast.success(t("inscripto"));
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorRegistrar"));
    }
  }

  function openRetirar(idTestimony: number) {
    setRetirarId(idTestimony);
    setNumberCard("");
  }

  async function handleRetirar() {
    if (!retirarId) return;
    try {
      await retirarMutation.mutateAsync({ idTestimony: retirarId, numberCard: Number(numberCard) });
      toast.success(t("retirado"));
      setRetirarId(null);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorRetirar"));
    }
  }

  async function handleReenter(details: ReentryDetails) {
    if (!reingresarId) return;
    try {
      await reingresarMutation.mutateAsync({ idTestimony: reingresarId, ...details });
      toast.success(t("reingresado"));
      setReingresarId(null);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorReingresar"));
    }
  }

  const columns: Column<Testimony>[] = [
    { key: "number", header: t("fields.numero"), render: (te) => <span className="font-medium">{te.number}</span> },
    { key: "escritura", header: t("fields.escritura"), render: (te) => te.deed?.number ?? "—" },
    { key: "estado", header: t("fields.estado"), render: (te) => t(`estados.${statusMovement(te)}`) },
    { key: "fechaIngreso", header: t("fields.fechaIngreso"), render: (te) => formatDate(latestMovement(te)?.dateEntry) },
    { key: "fechaInscripcion", header: t("fields.fechaInscripcion"), render: (te) => formatDate(latestMovement(te)?.dateRegistration) },
    { key: "fechaSalida", header: t("fields.fechaSalida"), render: (te) => formatDate(latestMovement(te)?.dateExit) },
    {
      key: "actions",
      header: "",
      className: "w-40",
      render: (te) => {
        const estado = statusMovement(te);
        const id = te.idTestimony!;
        return (
          <div className="flex gap-2 justify-end">
            {estado === "sinIngresar" && (
              <Button size="sm" variant="ghost" onClick={() => handleIngresar(id)} aria-label={t("ingresar")} data-testid={`btn-sign-in-testimony-${id}`}>
                <LogIn className="h-4 w-4" />
              </Button>
            )}
            {estado === "ingresado" && (
              <Button size="sm" variant="ghost" onClick={() => handleRegistrar(id)} aria-label={t("registrarInscripcion")} data-testid={`btn-record-registration-${id}`}>
                <Stamp className="h-4 w-4" />
              </Button>
            )}
            {estado === "inscripto" && (
              <Button size="sm" variant="ghost" onClick={() => openRetirar(id)} aria-label={t("retirar")} data-testid={`btn-withdraw-testimony-${id}`}>
                <LogOut className="h-4 w-4" />
              </Button>
            )}
            {estado === "retirado" && (
              <Button size="sm" variant="ghost" onClick={() => setReingresarId(id)} aria-label={t("reingresar")} data-testid={`btn-reenter-testimony-${id}`}>
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
      <DataTable data={verificados} columns={columns} isLoading={isLoading} keyExtractor={(te) => te.idTestimony!} emptyMessage={t("noData")} />

      <Dialog open={!!retirarId} onOpenChange={(v) => !v && setRetirarId(null)}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={t("retirar")}>
              <FormField label={t("fields.numeroCarton")} required>
                <Input type="number" value={numberCard} onChange={(e) => setNumberCard(e.target.value)} data-testid="input-card-number" />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setRetirarId(null)}>
                {tc("cancel")}
              </Button>
              <Button onClick={handleRetirar} disabled={!numberCard || retirarMutation.isPending} data-testid="btn-confirm-retiro">
                {t("retirar")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      {reingresarId !== null && (
        <ReenterDialog open isPending={reingresarMutation.isPending} onCancel={() => setReingresarId(null)} onConfirm={handleReenter} />
      )}
    </div>
  );
}
