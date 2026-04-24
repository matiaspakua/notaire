"use client";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { EstadoDeGestion } from "@/types";

export default function EstadosGestionPage() {
  const { data = [], isLoading } = useQuery({
    queryKey: ["estados-gestion"],
    queryFn: () => apiGet<EstadoDeGestion[]>("/catalogos/estados-gestion"),
  });
  const columns: Column<EstadoDeGestion>[] = [
    { key: "id", header: "ID", render: (e) => e.idEstadoDeGestion, className: "w-12" },
    { key: "nombre", header: "Nombre", render: (e) => <span className="font-medium">{e.nombre}</span> },
    { key: "desc", header: "Descripción", render: (e) => e.descripcion ?? "—" },
  ];
  return (
    <div>
      <AppHeader title="Estados de Gestión" description="CU67" />
      <DataTable data={data} columns={columns} isLoading={isLoading} keyExtractor={(e) => e.idEstadoDeGestion!} emptyMessage="No hay estados de gestión" />
    </div>
  );
}
