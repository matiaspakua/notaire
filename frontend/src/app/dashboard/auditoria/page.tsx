"use client";

import { useState } from "react";
import { Shield, Search, Filter } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { useAuditoria } from "@/hooks/useAuditoria";
import type { RegistroAuditoria } from "@/types";

export default function AuditoriaPage() {
  const t = useTranslations("auditoria");
  const tc = useTranslations("common");

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
      header: tc("id"),
      render: (r) => <span className="text-muted-foreground text-xs">{r.idRegistroAuditoria}</span>,
      className: "w-16",
    },
    {
      key: "fecha",
      header: t("fields.fecha"),
      render: (r) => (
        <span className="text-sm">
          {r.fecha ? new Date(r.fecha).toLocaleString("es-AR") : "—"}
        </span>
      ),
      className: "w-44",
    },
    {
      key: "usuario",
      header: t("fields.usuario"),
      render: (r) => <span className="font-medium">{r.usuarios?.nombre ?? "—"}</span>,
    },
    {
      key: "modulo",
      header: t("fields.modulo"),
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
      header: t("fields.operacion"),
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
        title={t("title")}
        description={t("description")}
      />

      <div className="flex items-center gap-3 mb-4">
        <div className="relative flex-1 max-w-[360px]">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground pointer-events-none" />
          <Input
            className="pl-10"
            placeholder={t("searchPlaceholder")}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        {modulos.length > 0 && (
          <div className="flex items-center gap-2">
            <Filter className="w-4 h-4 text-muted-foreground shrink-0" />
            <select
              className="h-12 rounded-lg border border-input bg-background px-4 text-sm text-foreground font-sans outline-none cursor-pointer focus:ring-2 focus:ring-ring"
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
        emptyMessage={t("noData")}
      />

      <div className="mt-6 rounded-2xl border border-border bg-card shadow-sm p-6">
        <div className="flex items-start gap-3">
          <div className="flex items-center justify-center w-10 h-10 rounded-lg bg-primary/10 shrink-0">
            <Shield className="w-5 h-5 text-primary" />
          </div>
          <div>
            <h3 className="text-sm font-semibold text-foreground mb-1">
              {t("title")}
            </h3>
            <p className="text-sm text-muted-foreground leading-relaxed">
              {t("description")}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
