"use client";

import { Suspense, useState } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { Shield, Search, Filter } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { useAuditoria } from "@/hooks/useAuditoria";
import type { RegistroAuditoria } from "@/types";
import { formatInstant } from "@/lib/dates";
import { AUDIT_MODULES } from "@/lib/audit-modules";
import { PAGE_SIZE_OPTIONS } from "@/components/shared/Pagination";

const DEFAULT_SIZE = PAGE_SIZE_OPTIONS[0];

function readInt(value: string | null, fallback: number): number {
  const n = Number(value);
  return Number.isInteger(n) && n >= 0 ? n : fallback;
}

export default function AuditoriaPage() {
  return (
    <Suspense>
      <AuditoriaList />
    </Suspense>
  );
}

function AuditoriaList() {
  const t = useTranslations("auditoria");
  const tc = useTranslations("common");
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  // Page, size and module live in the URL so back/forward and reload keep them (#1340).
  const page = readInt(searchParams.get("page"), 0);
  const sizeParam = readInt(searchParams.get("size"), DEFAULT_SIZE);
  const size = (PAGE_SIZE_OPTIONS as readonly number[]).includes(sizeParam) ? sizeParam : DEFAULT_SIZE;
  const moduloFilter = searchParams.get("module") ?? "all";

  function updateQuery(changes: Record<string, string | number | null>) {
    const next = new URLSearchParams(searchParams.toString());
    for (const [key, value] of Object.entries(changes)) {
      if (value === null || value === "") next.delete(key);
      else next.set(key, String(value));
    }
    const qs = next.toString();
    router.replace(qs ? `${pathname}?${qs}` : pathname, { scroll: false });
  }

  const { data, isLoading, isFetching } = useAuditoria({
    page,
    size,
    module: moduloFilter === "all" ? undefined : moduloFilter,
  });
  const registros = data?.content ?? [];
  const [search, setSearch] = useState("");

  // Search narrows the rows of the current page only; the backend has no text filter.
  const filtered = registros.filter((r) => {
    return (
      !search ||
      r.operationDetail?.toLowerCase().includes(search.toLowerCase()) ||
      r.users?.name?.toLowerCase().includes(search.toLowerCase())
    );
  });

  const columns: Column<RegistroAuditoria>[] = [
    {
      key: "id",
      header: tc("id"),
      render: (r) => <span className="text-muted-foreground text-xs">{r.idAuditRecord}</span>,
      className: "w-16",
    },
    {
      key: "fecha",
      header: t("fields.fecha"),
      render: (r) => (
        <span className="text-sm">
          {formatInstant(r.date)}
        </span>
      ),
      className: "w-44",
    },
    {
      key: "usuario",
      header: t("fields.usuario"),
      render: (r) => <span className="font-medium">{r.users?.name ?? "—"}</span>,
    },
    {
      key: "modulo",
      header: t("fields.modulo"),
      render: (r) =>
        r.module ? (
          <Badge variant="secondary" className="text-xs font-medium">
            {r.module}
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
        <span className="text-sm text-muted-foreground max-w-md truncate block" title={r.operationDetail}>
          {r.operationDetail ?? "—"}
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
            aria-label={t("searchPlaceholder")}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <div className="flex items-center gap-2">
          <Filter className="w-4 h-4 text-muted-foreground shrink-0" aria-hidden="true" />
          <select
            aria-label={t("moduleFilter")}
            data-testid="select-modulo-auditoria"
            className="h-12 rounded-lg border border-input bg-background px-4 text-sm text-foreground font-sans outline-none cursor-pointer focus:ring-2 focus:ring-ring"
            value={moduloFilter}
            onChange={(e) => updateQuery({ module: e.target.value === "all" ? null : e.target.value, page: null })}
          >
            <option value="all">{t("allModules")}</option>
            {AUDIT_MODULES.map((m) => (
              <option key={m} value={m}>
                {m}
              </option>
            ))}
          </select>
        </div>
      </div>

      <DataTable
        data={filtered}
        columns={columns}
        isLoading={isLoading}
        isFetching={isFetching}
        keyExtractor={(r) => r.idAuditRecord!}
        emptyMessage={t("noData")}
        pagination={{
          page: data?.number ?? page,
          size,
          totalElements: data?.totalElements ?? 0,
          onPageChange: (next) => updateQuery({ page: next === 0 ? null : next }),
          onSizeChange: (next) => updateQuery({ size: next === DEFAULT_SIZE ? null : next, page: null }),
        }}
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
