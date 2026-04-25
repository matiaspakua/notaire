"use client";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { TipoDeDocumento } from "@/types";

export default function DocumentosPage() {
  const { data: tipos = [], isLoading } = useQuery({
    queryKey: ["tipos-documento"],
    queryFn: () => apiGet<TipoDeDocumento[]>("/catalogos/tipos-documento"),
  });
  const columns: Column<TipoDeDocumento>[] = [
    { key: "id", header: "ID", render: (t) => t.idTipoDeDocumento, className: "w-12" },
    { key: "nombre", header: "Nombre", render: (t) => <span className="font-medium">{t.nombre}</span> },
  ];
  return (
    <div>
      <AppHeader title="Tipos de Documento" description="CU27, CU65" />
      <DataTable data={tipos} columns={columns} isLoading={isLoading} keyExtractor={(t) => t.idTipoDeDocumento!} emptyMessage="No hay tipos de documento" />
    </div>
  );
}
