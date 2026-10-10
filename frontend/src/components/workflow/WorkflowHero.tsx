"use client";

/**
 * Dashboard hero (#1347, RF-23/CU14): the workflow of the newest management that
 * has one, or of the management whose number the user searches. Says which case
 * it shows (number and header) and has an empty state when no recent case has a
 * workflow.
 */
import { useState } from "react";
import Link from "next/link";
import { useTranslations } from "next-intl";
import { Search } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import WorkflowTracker from "@/components/motion/WorkflowTracker";
import { useGestionByNumero } from "@/hooks/useGestiones";
import { useGestionWorkflowTrace, useLatestTracedGestion } from "@/hooks/useGestionWorkflow";
import { theme } from "@/theme/tokens";
import type { GestionWorkflowTrace } from "@/types";

function caseLabel(trace: GestionWorkflowTrace, fallback: (number: number | undefined) => string): string {
  return trace.encabezado ? `#${trace.number} — ${trace.encabezado}` : fallback(trace.number);
}

export function WorkflowHero() {
  const td = useTranslations("dashboard");
  const tw = useTranslations("dashboard.workflow");

  const [refInput, setRefInput] = useState("");
  const [searchedNumero, setSearchedNumero] = useState<number | undefined>();
  const searching = searchedNumero != null;
  const byNumero = useGestionByNumero(searchedNumero);
  const searchedTrace = useGestionWorkflowTrace(searching ? byNumero.data?.idManagement : undefined);
  const latest = useLatestTracedGestion(!searching);

  const notFound = searching && byNumero.isError;
  const trace = searching ? searchedTrace.data : latest.data?.trace;
  const loading = searching ? searchedTrace.isLoading && byNumero.data != null : latest.isLoading;
  const noRecentWorkflow = !searching && latest.isSuccess && latest.data === null;

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const numero = Number.parseInt(refInput.trim(), 10);
    setSearchedNumero(Number.isNaN(numero) ? undefined : numero);
  }

  return (
    <section className="space-y-5 px-2" data-testid="workflow-hero" data-management-id={trace?.managementId}>
      <div className="flex flex-col lg:flex-row lg:items-end justify-between gap-4">
        <div>
          <h2 className="text-2xl font-semibold tracking-tight text-foreground">{td("workflowProgress")}</h2>
          {trace && (
            <p className="text-sm text-muted-foreground mt-1" data-testid="workflow-subtitle">
              {caseLabel(trace, (number) => tw("managementFallback", { number: number ?? "—" }))}
              {trace.statusActual && (
                <span className="ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-primary/10 text-primary-text">
                  {trace.statusActual}
                </span>
              )}
            </p>
          )}
        </div>
        <form onSubmit={handleSubmit} className="flex items-end gap-3" data-testid="workflow-search-form">
          <div className="space-y-1.5">
            <label htmlFor="workflow-ref" className="block text-sm font-semibold" style={{ color: theme.colors.neutral[800] }}>
              {tw("searchLabel")}
            </label>
            <Input
              id="workflow-ref"
              type="number"
              inputMode="numeric"
              value={refInput}
              onChange={(e) => setRefInput(e.target.value)}
              placeholder={tw("searchPlaceholder")}
              className="w-44"
            />
          </div>
          <Button type="submit" variant="default" className="min-w-[120px]">
            <Search className="h-4 w-4 mr-2" />
            {tw("searchButton")}
          </Button>
        </form>
      </div>

      {notFound && (
        <p role="alert" className="text-sm font-medium text-destructive" data-testid="workflow-not-found">
          {tw("notFound")}
        </p>
      )}

      {noRecentWorkflow && (
        <div
          data-testid="workflow-empty"
          className="flex flex-col items-start gap-3 rounded-[28px] border border-dashed border-[hsl(var(--border))] bg-white/60 p-8 text-sm text-muted-foreground"
        >
          <p>{tw("noRecentWorkflow")}</p>
          <Link href="/dashboard/gestiones" className="font-semibold text-primary-text underline-offset-4 hover:underline apple-focus rounded">
            {tw("openGestiones")}
          </Link>
        </div>
      )}

      {trace && !notFound && <WorkflowTracker trace={trace} />}

      {loading && !trace && (
        <div className="h-[320px] bg-muted rounded-[28px] animate-pulse" data-testid="workflow-skeleton" />
      )}
    </section>
  );
}
