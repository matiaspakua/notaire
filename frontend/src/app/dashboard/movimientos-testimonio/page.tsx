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
import { useTestimonios } from "@/hooks/useTestimonios";
import { useIngresarInscripcion, useRegistrarInscripcion, useRetirar, useReingresar } from "@/hooks/useMovimientosTestimonio";
import { extractApiError, formatDate } from "@/lib/utils";
import type { Testimonio, MovimientoTestimonio } from "@/types";

type EstadoMovimiento = "sinIngresar" | "ingresado" | "inscripto" | "retirado";

function latestMovimiento(testimonio: Testimonio): MovimientoTestimonio | undefined {
  const movimientos = testimonio.movimientosTestimonios ?? [];
  return movimientos.reduce<MovimientoTestimonio | undefined>(
    (latest, m) => (!latest || (m.idMovimientoTestimonio ?? 0) > (latest.idMovimientoTestimonio ?? 0) ? m : latest),
    undefined
  );
}

function estadoMovimiento(testimonio: Testimonio): EstadoMovimiento {
  const movimiento = latestMovimiento(testimonio);
  if (!movimiento) return "sinIngresar";
  if (movimiento.fechaSalida) return "retirado";
  if (movimiento.inscripta) return "inscripto";
  return "ingresado";
}

export default function MovimientosTestimonioPage() {
  const t = useTranslations("movimientosTestimonio");
  const tc = useTranslations("common");

  const { data: testimonios = [], isLoading } = useTestimonios();
  const verificados = testimonios.filter((te) => te.verificado);

  const ingresarMutation = useIngresarInscripcion();
  const registrarMutation = useRegistrarInscripcion();
  const retirarMutation = useRetirar();
  const reingresarMutation = useReingresar();

  const [retirarId, setRetirarId] = useState<number | null>(null);
  const [numeroCarton, setNumeroCarton] = useState("");

  async function handleIngresar(idTestimonio: number) {
    try {
      await ingresarMutation.mutateAsync(idTestimonio);
      toast.success(t("ingresado"));
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorIngresar"));
    }
  }

  async function handleRegistrar(idTestimonio: number) {
    try {
      await registrarMutation.mutateAsync(idTestimonio);
      toast.success(t("inscripto"));
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorRegistrar"));
    }
  }

  function openRetirar(idTestimonio: number) {
    setRetirarId(idTestimonio);
    setNumeroCarton("");
  }

  async function handleRetirar() {
    if (!retirarId) return;
    try {
      await retirarMutation.mutateAsync({ idTestimonio: retirarId, numeroCarton: Number(numeroCarton) });
      toast.success(t("retirado"));
      setRetirarId(null);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorRetirar"));
    }
  }

  async function handleReingresar(idTestimonio: number) {
    try {
      await reingresarMutation.mutateAsync(idTestimonio);
      toast.success(t("reingresado"));
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorReingresar"));
    }
  }

  const columns: Column<Testimonio>[] = [
    { key: "numero", header: t("fields.numero"), render: (te) => <span className="font-medium">{te.numero}</span> },
    { key: "escritura", header: t("fields.escritura"), render: (te) => te.escritura?.numero ?? "—" },
    { key: "estado", header: t("fields.estado"), render: (te) => t(`estados.${estadoMovimiento(te)}`) },
    { key: "fechaIngreso", header: t("fields.fechaIngreso"), render: (te) => formatDate(latestMovimiento(te)?.fechaIngreso) },
    { key: "fechaInscripcion", header: t("fields.fechaInscripcion"), render: (te) => formatDate(latestMovimiento(te)?.fechaInscripcion) },
    { key: "fechaSalida", header: t("fields.fechaSalida"), render: (te) => formatDate(latestMovimiento(te)?.fechaSalida) },
    {
      key: "actions",
      header: "",
      className: "w-40",
      render: (te) => {
        const estado = estadoMovimiento(te);
        const id = te.idTestimonio!;
        return (
          <div className="flex gap-2 justify-end">
            {estado === "sinIngresar" && (
              <Button size="sm" variant="ghost" onClick={() => handleIngresar(id)} aria-label={t("ingresar")} data-testid={`btn-ingresar-testimonio-${id}`}>
                <LogIn className="h-4 w-4" />
              </Button>
            )}
            {estado === "ingresado" && (
              <Button size="sm" variant="ghost" onClick={() => handleRegistrar(id)} aria-label={t("registrarInscripcion")} data-testid={`btn-registrar-inscripcion-${id}`}>
                <Stamp className="h-4 w-4" />
              </Button>
            )}
            {estado === "inscripto" && (
              <Button size="sm" variant="ghost" onClick={() => openRetirar(id)} aria-label={t("retirar")} data-testid={`btn-retirar-testimonio-${id}`}>
                <LogOut className="h-4 w-4" />
              </Button>
            )}
            {estado === "retirado" && (
              <Button size="sm" variant="ghost" onClick={() => handleReingresar(id)} aria-label={t("reingresar")} data-testid={`btn-reingresar-testimonio-${id}`}>
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
      <DataTable data={verificados} columns={columns} isLoading={isLoading} keyExtractor={(te) => te.idTestimonio!} emptyMessage={t("noData")} />

      <Dialog open={!!retirarId} onOpenChange={(v) => !v && setRetirarId(null)}>
        <DialogContent>
          <FormContainer>
            <FormSection title={t("retirar")}>
              <FormField label={t("fields.numeroCarton")} required>
                <Input type="number" value={numeroCarton} onChange={(e) => setNumeroCarton(e.target.value)} data-testid="input-numero-carton" />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setRetirarId(null)}>
                {tc("cancel")}
              </Button>
              <Button onClick={handleRetirar} disabled={!numeroCarton || retirarMutation.isPending} data-testid="btn-confirmar-retiro">
                {t("retirar")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>
    </div>
  );
}
