"use client";

import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Badge } from "@/components/ui/badge";
import { useFolios } from "@/hooks/useFolios";
import type { Folio } from "@/types";

/** CU24, CU28, CU63 — Protocolo / Gestión de folios */
export default function ProtocoloPage() {
  const { data: folios = [], isLoading } = useFolios();

  const estadoVariant = (estado?: string): "default" | "secondary" | "outline" => {
    if (estado === "DISPONIBLE") return "default";
    if (estado === "USADO") return "secondary";
    return "outline";
  };

  const columns: Column<Folio>[] = [
    {
      key: "id",
      header: "ID",
      render: (f) => (
        <span className="text-xs text-muted-foreground">{f.idFolio}</span>
      ),
      className: "w-12",
    },
    {
      key: "numero",
      header: "Número de folio",
      render: (f) => (
        <span className="font-mono font-medium">{f.numero ?? "—"}</span>
      ),
    },
    {
      key: "anio",
      header: "Año",
      render: (f) => f.anio ?? "—",
    },
    {
      key: "tipo",
      header: "Tipo",
      render: (f) => f.tipoDeFolio?.nombre ?? "—",
    },
    {
      key: "estado",
      header: "Estado",
      render: (f) => (
        <Badge variant={estadoVariant(f.estado)}>
          {f.estado ?? "—"}
        </Badge>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title="Protocolo / Folios"
        description="CU24, CU28, CU63 — Gestionar folios e índices del protocolo notarial"
      />
      <DataTable
        data={folios}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(f) => f.idFolio!}
        emptyMessage="No hay folios registrados"
      />
    </div>
  );
}
