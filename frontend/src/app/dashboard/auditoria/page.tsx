"use client";

import { useState } from "react";
import { Shield, Search, Filter } from "lucide-react";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { useAuditoria } from "@/hooks/useAuditoria";
import { theme } from "@/theme/tokens";
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
      <div style={{ display: "flex", alignItems: "center", gap: theme.spacing[3], marginBottom: theme.spacing[4] }}>
        <div style={{ position: "relative", flex: 1, maxWidth: "360px" }}>
          <Search
            style={{
              position: "absolute",
              left: theme.spacing[3],
              top: "50%",
              transform: "translateY(-50%)",
              width: "16px",
              height: "16px",
              color: theme.colors.neutral[500],
              pointerEvents: "none",
            }}
          />
          <Input
            style={{ paddingLeft: "2.5rem" }}
            placeholder="Buscar por usuario u operación..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        {modulos.length > 0 && (
          <div style={{ display: "flex", alignItems: "center", gap: theme.spacing[2] }}>
            <Filter style={{ width: "16px", height: "16px", color: theme.colors.neutral[500], flexShrink: 0 }} />
            <select
              style={{
                height: theme.sizes.input.height,
                borderRadius: theme.borderRadius.lg,
                border: `1px solid ${theme.semantic.form.inputBorder}`,
                backgroundColor: theme.semantic.form.inputBg,
                padding: `0 ${theme.spacing[4]}`,
                fontSize: theme.typography.fontSize.sm,
                color: theme.semantic.form.inputText,
                fontFamily: theme.typography.fontFamily.body,
                outline: "none",
                cursor: "pointer",
              }}
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
      <div
        style={{
          marginTop: theme.spacing[6],
          borderRadius: theme.borderRadius["2xl"],
          border: `1px solid ${theme.colors.neutral[200]}`,
          backgroundColor: theme.semantic.card.bg,
          boxShadow: theme.shadows.sm,
          padding: theme.spacing[6],
        }}
      >
        <div style={{ display: "flex", alignItems: "flex-start", gap: theme.spacing[3] }}>
          <div
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              width: "40px",
              height: "40px",
              borderRadius: theme.borderRadius.lg,
              backgroundColor: `${theme.colors.primary[600]}1a`,
              flexShrink: 0,
            }}
          >
            <Shield style={{ width: "20px", height: "20px", color: theme.colors.primary[600] }} />
          </div>
          <div>
            <h3
              style={{
                fontSize: theme.typography.fontSize.sm,
                fontWeight: theme.typography.fontWeight.semibold,
                color: theme.colors.neutral[900],
                fontFamily: theme.typography.fontFamily.body,
                margin: 0,
                marginBottom: theme.spacing[1],
              }}
            >
              Registro de Auditoría
            </h3>
            <p
              style={{
                fontSize: theme.typography.fontSize.sm,
                color: theme.colors.neutral[600],
                fontFamily: theme.typography.fontFamily.body,
                margin: 0,
                lineHeight: theme.typography.lineHeight.relaxed,
              }}
            >
              Este módulo muestra todas las operaciones realizadas en el sistema. Los registros son
              de solo lectura y se generan automáticamente al crear, modificar o eliminar datos.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
