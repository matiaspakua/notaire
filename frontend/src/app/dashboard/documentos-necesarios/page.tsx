"use client";

import { useState } from "react";
import { Printer } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { FormContainer, FormSection, FormField } from "@/theme/form-patterns";
import { useTiposTramite } from "@/hooks/useTiposTramite";
import { usePlantillaTramite } from "@/hooks/usePlantillaTramite";
import type { PlantillaTramite } from "@/types";

export default function DocumentosNecesariosPage() {
  const t = useTranslations("documentosNecesarios");
  const tc = useTranslations("common");

  const { data: tiposTramite = [] } = useTiposTramite();
  const [tramiteId, setTramiteId] = useState("");
  const idTipoTramite = tramiteId ? Number(tramiteId) : undefined;
  const { data: documentos = [], isLoading, isFetched } = usePlantillaTramite(idTipoTramite);

  const columns: Column<PlantillaTramite>[] = [
    {
      key: "nombre",
      header: t("fields.nombre"),
      render: (p) => <span className="font-medium">{p.tipoDeDocumento?.nombre}</span>,
    },
    {
      key: "vence",
      header: t("fields.vence"),
      render: (p) => (p.tipoDeDocumento?.vence ? tc("yes") : tc("no")),
    },
    {
      key: "diasVencimiento",
      header: t("fields.diasVencimiento"),
      render: (p) => (p.tipoDeDocumento?.vence ? (p.tipoDeDocumento?.diasVencimiento ?? "—") : "—"),
    },
    {
      key: "quienEntrega",
      header: t("fields.quienEntrega"),
      render: (p) => p.tipoDeDocumento?.quienEntrega ?? "—",
    },
  ];

  const selectedTramiteName = tiposTramite.find((tt) => tt.idTipoDeTramite === idTipoTramite)?.nombre ?? "";

  return (
    <div>
      <AppHeader
        title={t("title")}
        description={t("description")}
        actions={
          idTipoTramite && documentos.length > 0 ? (
            <Button onClick={() => window.print()} data-testid="btn-imprimir">
              <Printer size={16} className="mr-1" />
              {t("print")}
            </Button>
          ) : undefined
        }
      />

      <div className="max-w-[1600px] mx-auto p-6 lg:p-10 space-y-6">
        <FormContainer>
          <FormSection title={t("selectorTitle")}>
            <FormField label={t("fields.tramite")} required>
              <Select value={tramiteId} onValueChange={setTramiteId}>
                <SelectTrigger data-testid="select-tramite">
                  <SelectValue placeholder={t("fields.tramitePlaceholder")} />
                </SelectTrigger>
                <SelectContent>
                  {tiposTramite.map((tt) => (
                    <SelectItem key={tt.idTipoDeTramite} value={String(tt.idTipoDeTramite)}>
                      {tt.nombre}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </FormField>
          </FormSection>
        </FormContainer>

        {idTipoTramite !== undefined && (
          <div data-printable="true">
            <h2 className="text-lg font-semibold mb-4">
              {t("resultsTitle", { tramite: selectedTramiteName })}
            </h2>
            {isFetched && documentos.length === 0 ? (
              <p className="text-sm text-muted-foreground" data-testid="empty-state">
                {t("noDocuments")}
              </p>
            ) : (
              <DataTable
                data={documentos}
                columns={columns}
                isLoading={isLoading}
                emptyMessage={t("noDocuments")}
                keyExtractor={(p) => `${p.tipoDeTramite?.idTipoTramite}-${p.tipoDeDocumento?.idTipoDocumento}`}
              />
            )}
          </div>
        )}
      </div>
    </div>
  );
}
