"use client";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { TipoDeTramite } from "@/types";

export default function TramitesPage() {
  const { data = [], isLoading } = useQuery({
    queryKey: ["tipos-tramite"],
    queryFn: () => apiGet<TipoDeTramite[]>("/catalogos/tipos-tramite"),
  });
  const columns: Column<TipoDeTramite>[] = [
    { key: "id", header: "ID", render: (t) => t.idTipoDeTramite, className: "w-12" },
    { key: "nombre", header: "Nombre", render: (t) => <span className="font-medium">{t.nombre}</span> },
    { key: "desc", header: "Descripción", render: (t) => t.descripcion ?? "—" },
  ];
  return (
    <div>
      <AppHeader title="Tipos de Trámite" description="CU26, CU57, CU64" />
      <DataTable data={data} columns={columns} isLoading={isLoading} keyExtractor={(t) => t.idTipoDeTramite!} emptyMessage="No hay tipos de trámite" />
    </div>
  );
}
