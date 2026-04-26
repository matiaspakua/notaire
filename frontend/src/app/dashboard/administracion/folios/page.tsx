"use client";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Badge } from "@/components/ui/badge";
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { Folio } from "@/types";

export default function FoliosAdminPage() {
  const { data = [], isLoading } = useQuery({
    queryKey: ["folios"],
    queryFn: () => apiGet<Folio[]>("/folios"),
  });
  const columns: Column<Folio>[] = [
    { key: "id", header: "ID", render: (f) => f.idFolio, className: "w-12" },
    { key: "numero", header: "Número", render: (f) => <span className="font-medium">{f.numero}</span> },
    { key: "tipo", header: "Tipo", render: (f) => f.tipoDeFolio?.nombre ?? "—" },
    { key: "estado", header: "Estado", render: (f) => f.disponible ? <Badge variant="success">Disponible</Badge> : <Badge variant="secondary">En uso</Badge> },
  ];
  return (
    <div>
      <AppHeader title="Folios" description="CU28, CU40, CU58, CU63, CU68" />
      <DataTable data={data} columns={columns} isLoading={isLoading} keyExtractor={(f) => f.idFolio!} emptyMessage="No hay folios" />
    </div>
  );
}
