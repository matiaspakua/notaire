"use client";

import { useTranslations } from "next-intl";
import { ApiError } from "@/lib/api-client";
import { useFormat } from "@/hooks/useFormat";
import { useManagementCurrentStatus } from "@/hooks/useManagements";

/** CU14 (#1553): "Estado actual" of a management in the case summary. */
export function ManagementCurrentStatus({ managementId }: { managementId: number }) {
  const t = useTranslations("gestiones.estadoActual");
  const fmt = useFormat();
  const { data, isLoading, error } = useManagementCurrentStatus(managementId);

  if (isLoading) return <p className="text-sm text-muted-foreground">{t("cargando")}</p>;
  if (error instanceof ApiError && error.status === 404) {
    return (
      <p className="text-sm text-muted-foreground" data-testid="management-current-status-empty">
        {t("sinEstado")}
      </p>
    );
  }
  if (error || !data) {
    return <p className="text-sm text-muted-foreground">{t("error")}</p>;
  }
  return (
    <dl data-testid="management-current-status" className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-1 text-sm">
      <dt className="text-muted-foreground">{t("estado")}</dt>
      <dd className="font-medium break-words">{data.statusManagementName ?? "—"}</dd>
      <dt className="text-muted-foreground">{t("fecha")}</dt>
      <dd data-testid="management-current-status-date">{data.date ? fmt.date(data.date) : "—"}</dd>
      {data.notes ? (
        <>
          <dt className="text-muted-foreground">{t("notas")}</dt>
          <dd data-testid="management-current-status-notes" className="break-words">{data.notes}</dd>
        </>
      ) : null}
    </dl>
  );
}
