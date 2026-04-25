"use client";

import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Badge } from "@/components/ui/badge";
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { Folio } from "@/types";

export default function ProtocoloPage() {
  const { data: folios = [], isLoading } = useQuery({
    queryKey: ["folios"],
    queryFn: () => apiGet<Folio[]>("/folios"),
  });

  const columns: Column<Folio>[] = [
    { key: "id", header: "ID", render: (f) => <span className="text-xs text-muted-foreground">{f.idFolio}</span>, className: "w-12" },
    { key: "numero", header: "Número de folio", render: (f) => <span className="font-medium">{f.numero ?? "—"}</span> },
    { key: "tipo", header: "Tipo", render: (f) => f.tipoDeFolio?.nombre ?? "—" },
    {
      key: "disponible",
      header: "Estado",
      render: (f) =>
        f.disponible ? <Badge variant="success">Disponible</Badge> : <Badge variant="secondary">En uso</Badge>,
    },
  ];

  return (
    <div>
      <AppHeader
        title="Protocolo / Folios"
        description="CU24, CU28, CU63 — Gestionar folios e índices"
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
