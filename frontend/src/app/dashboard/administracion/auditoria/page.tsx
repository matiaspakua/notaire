"use client";

import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { useAuditoria } from "@/hooks/useAuditoria";
import { formatDate } from "@/lib/utils";
import type { RegistroAuditoria } from "@/types";

/**
 * Read-only audit log viewer.
 * Shows all system actions recorded in RegistroAuditoria.
 */
export default function AuditoriaPage() {
  const { data: registros = [], isLoading } = useAuditoria();

  const columns: Column<RegistroAuditoria>[] = [
    {
      key: "id",
      header: "ID",
      render: (r) => (
        <span className="text-xs text-muted-foreground font-mono">
          {r.idRegistroAuditoria}
        </span>
      ),
      className: "w-16",
    },
    {
      key: "fecha",
      header: "Fecha",
      render: (r) => formatDate(r.fecha),
      className: "w-36",
    },
    {
      key: "usuario",
      header: "Usuario",
      render: (r) => (
        <span className="font-medium">{r.usuarios?.nombre ?? "—"}</span>
      ),
      className: "w-32",
    },
    {
      key: "modulo",
      header: "Módulo",
      render: (r) => (
        <span className="text-sm text-muted-foreground">{r.modulo ?? "—"}</span>
      ),
      className: "w-36",
    },
    {
      key: "detalle",
      header: "Detalle",
      render: (r) => (
        <span className="text-sm">{r.detalleOperacion ?? "—"}</span>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title="Auditoría del Sistema"
        description="Registro completo de operaciones realizadas por los usuarios"
      />
      <DataTable
        data={registros}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(r) => r.idRegistroAuditoria!}
        emptyMessage="No hay registros de auditoría"
      />
    </div>
  );
}
