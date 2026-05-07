"use client";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { PlantillaPresupuesto } from "@/types";

export default function PlantillasPage() {
  const { data = [], isLoading } = useQuery({
    queryKey: ["plantillas-presupuesto"],
    queryFn: () => apiGet<PlantillaPresupuesto[]>("/presupuestos/plantillas"),
  });
  const columns: Column<PlantillaPresupuesto>[] = [
    { key: "id", header: "ID", render: (p) => p.idPlantillaPresupuesto, className: "w-12" },
    { key: "nombre", header: "Nombre", render: (p) => <span className="font-medium">{p.nombre}</span> },
    { key: "items", header: "Items", render: (p) => p.itemList?.length ?? 0 },
  ];
  return (
    <div>
      <AppHeader title="Plantillas de Presupuesto" description="CU39, CU49, CU55 — Plantillas reutilizables para armar presupuestos" />
      <DataTable data={data} columns={columns} isLoading={isLoading} keyExtractor={(p) => p.idPlantillaPresupuesto!} emptyMessage="No hay plantillas" />
    </div>
  );
}
