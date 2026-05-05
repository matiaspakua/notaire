"use client";

import { useState } from "react";
import { Shield, Search, Filter } from "lucide-react";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { useAuditoria } from "@/hooks/useAuditoria";
import type { RegistroAuditoria } from "@/types";

export default function AuditoriaPage() {
  const { data: registros = [], isLoading } = useAuditoria();
  const [search, setSearch] = useState("");
  const [moduloFilter, setModuloFilter] = useState<string>("all");

  const modulos = Array.from(new Set(registros.map((r) => r.modulo).filter(Boolean)));

  const filtered = registros.filter((r) => {
    const matchesSearch =
      !search ||
      r.detalleOperacion?.toLowerCase().includes(search.toLowerCase()) ||
      r.usuarios?.nombre?.toLowerCase().includes(search.toLowerCase());
    const matchesModulo = moduloFilter === "all" || r.modulo === moduloFilter;
    return matchesSearch && matchesModulo;
  });

  const columns: Column<RegistroAuditoria>[] = [
    {
      key: "id",
      header: "ID",
      render: (r) => <span className="text-muted-foreground text-xs">{r.idRegistroAuditoria}</span>,
      className: "w-16",
    },
    {
      key: "fecha",
      header: "Fecha",
      render: (r) => (
        <span className="text-sm">
          {r.fecha ? new Date(r.fecha).toLocaleString("es-AR") : "—"}
        </span>
      ),
      className: "w-44",
    },
    {
      key: "usuario",
      header: "Usuario",
      render: (r) => <span className="font-medium">{r.usuarios?.nombre ?? "—"}</span>,
    },
    {
      key: "modulo",
      header: "Módulo",
      render: (r) =>
        r.modulo ? (
          <Badge variant="secondary" className="text-xs font-medium">
            {r.modulo}
          </Badge>
        ) : (
          "—"
        ),
      className: "w-32",
    },
    {
      key: "detalle",
      header: "Operación",
      render: (r) => (
        <span className="text-sm text-muted-foreground max-w-md truncate block" title={r.detalleOperacion}>
          {r.detalleOperacion ?? "—"}
        </span>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title="Auditoría"
        description="Registro de actividades del sistema (CU73)"
      />

      {/* Filters */}
      <div className="flex items-center gap-3 mb-4">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            className="pl-9"
            placeholder="Buscar por usuario u operación..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        {modulos.length > 0 && (
          <div className="flex items-center gap-2">
            <Filter className="h-4 w-4 text-muted-foreground" />
            <select
              className="h-11 rounded-xl border border-input bg-background px-4 py-2 text-sm apple-focus"
              value={moduloFilter}
              onChange={(e) => setModuloFilter(e.target.value)}
            >
              <option value="all">Todos los módulos</option>
              {modulos.map((m) => (
                <option key={m} value={m}>
                  {m}
                </option>
              ))}
            </select>
          </div>
        )}
      </div>

      <DataTable
        data={filtered}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(r) => r.idRegistroAuditoria!}
        emptyMessage="No hay registros de auditoría"
      />

      {/* Info card */}
      <div className="mt-6 rounded-2xl border border-border/50 bg-card apple-shadow p-6">
        <div className="flex items-start gap-3">
          <div className="flex items-center justify-center w-10 h-10 rounded-xl bg-primary/10">
            <Shield className="h-5 w-5 text-primary" />
          </div>
          <div>
            <h3 className="font-semibold text-sm">Registro de Auditoría</h3>
            <p className="text-sm text-muted-foreground mt-1">
              Este módulo muestra todas las operaciones realizadas en el sistema. Los registros son
              de solo lectura y se generan automáticamente al crear, modificar o eliminar datos.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
