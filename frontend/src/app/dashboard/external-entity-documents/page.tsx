"use client";

import { Suspense, useState } from "react";
import { toast } from "sonner";
import { Eye, Pencil } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { FormContainer, FormSection, FormField, FormActions, CheckboxField } from "@/theme/form-patterns";
import { useThemeClasses } from "@/theme";
import { useManagementsPage } from "@/hooks/useManagements";
import { useClampPage, useUrlPagination } from "@/hooks/useUrlPagination";
import {
  useExternalEntityDocuments,
  useRegisterExternalEntityDocumentMovement,
} from "@/hooks/useExternalEntityDocuments";
import { extractApiError, formatDate } from "@/lib/utils";
import type { ExternalEntityDocument, DeedManagement, ExternalEntityDocumentMovementInput } from "@/types";
import { toDateInputValue } from "@/lib/dates";

const emptyMovement: ExternalEntityDocumentMovementInput = {
  prepared: false,
  cardNumber: undefined,
  dateEntry: "",
  dateExit: "",
  flagged: false,
  amountToPay: undefined,
  datePayment: "",
  dateReleased: "",
  notes: "",
  delivered: false,
};

export default function ExternalEntityDocumentsPage() {
  // useUrlPagination reads useSearchParams, which needs a Suspense boundary.
  return (
    <Suspense>
      <ExternalEntityDocumentsList />
    </Suspense>
  );
}

function ExternalEntityDocumentsList() {
  const t = useTranslations("documentosEntidadesExternas");
  const tc = useTranslations("common");
  const themeClass = useThemeClasses();

  // One server page at a time, newest first, page and size in the URL (#1340):
  // the list used to load size=1000 and silently miss every later case.
  const paging = useUrlPagination();
  const { data: managementsPage, isLoading, isFetching } = useManagementsPage({ page: paging.page, size: paging.size });
  const managements = managementsPage?.content ?? [];
  useClampPage(paging, managementsPage?.totalPages);

  const [selectedManagementId, setSelectedManagementId] = useState<number | null>(null);
  const { data: detail, isLoading: isLoadingDetail } = useExternalEntityDocuments(selectedManagementId ?? undefined);

  const [editingDocument, setEditingDocument] = useState<ExternalEntityDocument | null>(null);
  const [movement, setMovement] = useState<ExternalEntityDocumentMovementInput>(emptyMovement);

  const registerMutation = useRegisterExternalEntityDocumentMovement();

  function openEdit(document: ExternalEntityDocument) {
    setEditingDocument(document);
    setMovement({
      prepared: document.prepared ?? false,
      cardNumber: document.cardNumber,
      dateEntry: document.dateEntry ?? "",
      dateExit: document.dateExit ?? "",
      flagged: document.flagged ?? false,
      amountToPay: document.amountToPay,
      datePayment: document.datePayment ?? "",
      dateReleased: document.dateReleased ?? "",
      notes: document.notes ?? "",
      delivered: document.delivered ?? false,
    });
  }

  async function handleSaveMovement() {
    if (!selectedManagementId || !editingDocument) return;
    try {
      await registerMutation.mutateAsync({
        managementId: selectedManagementId,
        idSubmittedDocument: editingDocument.idSubmittedDocument,
        movement,
      });
      toast.success(t("movimientoRegistrado"));
      setEditingDocument(null);
    } catch (err) {
      toast.error(extractApiError(err) ?? t("errorMovimiento"));
    }
  }

  const managementColumns: Column<DeedManagement>[] = [
    { key: "number", header: t("fields.numero"), render: (g) => <span className="font-medium">{g.number}</span> },
    { key: "heading", header: t("fields.encabezado"), render: (g) => g.heading ?? "—" },
    { key: "currentStatus", header: tc("status"), render: (g) => g.currentStatus ?? "—" },
    {
      key: "actions",
      header: "",
      className: "w-32",
      render: (g) => (
        <div className="flex justify-end">
          <Button
            size="sm"
            variant="ghost"
            onClick={() => setSelectedManagementId(g.idManagement!)}
            aria-label={t("verDocumentos")}
            data-testid={`btn-view-documents-${g.idManagement}`}
          >
            <Eye className="h-4 w-4" />
          </Button>
        </div>
      ),
    },
  ];

  const documentColumns: Column<ExternalEntityDocument>[] = [
    { key: "name", header: t("fields.nombre"), render: (d) => d.name ?? "—" },
    {
      key: "delivered",
      header: t("fields.entregado"),
      render: (d) => (
        <Badge variant={d.delivered ? "success" : "secondary"}>
          {d.delivered ? tc("yes") : tc("no")}
        </Badge>
      ),
    },
    { key: "dateEntry", header: t("fields.fechaIngreso"), render: (d) => formatDate(d.dateEntry) },
    { key: "dateExit", header: t("fields.fechaSalida"), render: (d) => formatDate(d.dateExit) },
    {
      key: "actions",
      header: "",
      className: "w-24",
      render: (d) => (
        <div className="flex justify-end">
          <Button
            size="sm"
            variant="ghost"
            onClick={() => openEdit(d)}
            aria-label={t("registrarMovimiento")}
            data-testid={`btn-edit-document-${d.idSubmittedDocument}`}
          >
            <Pencil className="h-4 w-4" />
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader title={t("title")} />
      <DataTable
        data={managements}
        columns={managementColumns}
        isLoading={isLoading}
        isFetching={isFetching}
        keyExtractor={(g) => g.idManagement!}
        emptyMessage={t("noData")}
        pagination={{
          page: managementsPage?.number ?? paging.page,
          size: paging.size,
          totalElements: managementsPage?.totalElements ?? 0,
          onPageChange: paging.setPage,
          onSizeChange: paging.setSize,
        }}
      />

      <Dialog open={!!selectedManagementId} onOpenChange={(v) => !v && setSelectedManagementId(null)}>
        <DialogContent className="max-w-3xl max-h-[85vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>{t("detalleTitle")}</DialogTitle>
          </DialogHeader>
          {detail && (
            <div className="mb-6 grid grid-cols-2 gap-4 text-sm">
              <div>
                <span className="font-semibold">{t("fields.numero")}:</span> {detail.number ?? "—"}
              </div>
              <div>
                <span className="font-semibold">{t("fields.escribano")}:</span> {detail.notary ?? "—"}
              </div>
              <div>
                <span className="font-semibold">{t("fields.encabezado")}:</span> {detail.heading ?? "—"}
              </div>
              <div>
                <span className="font-semibold">{t("fields.nomenclaturaCatastral")}:</span>{" "}
                {detail.cadastralDesignation ?? "—"}
              </div>
            </div>
          )}
          <DataTable
            data={detail?.documents ?? []}
            columns={documentColumns}
            isLoading={isLoadingDetail}
            keyExtractor={(d) => d.idSubmittedDocument}
            emptyMessage={t("noDocumentos")}
          />
        </DialogContent>
      </Dialog>

      <Dialog open={!!editingDocument} onOpenChange={(v) => !v && setEditingDocument(null)}>
        <DialogContent className="max-h-[85vh] overflow-y-auto" data-testid="dialog-movement">
          <FormContainer>
            <FormSection dialogTitle title={t("registrarMovimiento")}>
              <FormField label={t("fields.numeroCarton")}>
                <Input
                  type="number"
                  value={movement.cardNumber ?? ""}
                  onChange={(e) =>
                    setMovement((m) => ({ ...m, cardNumber: e.target.value ? Number(e.target.value) : undefined }))
                  }
                  data-testid="input-card-number"
                />
              </FormField>
              <FormField label={t("fields.fechaIngreso")}>
                <Input
                  type="date"
                  value={toDateInputValue(movement.dateEntry)}
                  onChange={(e) => setMovement((m) => ({ ...m, dateEntry: e.target.value }))}
                  data-testid="input-entry-date"
                />
              </FormField>
              <FormField label={t("fields.fechaSalida")}>
                <Input
                  type="date"
                  value={toDateInputValue(movement.dateExit)}
                  onChange={(e) => setMovement((m) => ({ ...m, dateExit: e.target.value }))}
                  data-testid="input-exit-date"
                />
              </FormField>
              <FormField label={t("fields.importeAPagar")}>
                <Input
                  type="number"
                  value={movement.amountToPay ?? ""}
                  onChange={(e) =>
                    setMovement((m) => ({ ...m, amountToPay: e.target.value ? Number(e.target.value) : undefined }))
                  }
                  data-testid="input-importe-a-pagar"
                />
              </FormField>
              <FormField label={t("fields.fechaPago")}>
                <Input
                  type="date"
                  value={toDateInputValue(movement.datePayment)}
                  onChange={(e) => setMovement((m) => ({ ...m, datePayment: e.target.value }))}
                  data-testid="input-payment-date"
                />
              </FormField>
              <FormField label={t("fields.fechaLiberado")}>
                <Input
                  type="date"
                  value={toDateInputValue(movement.dateReleased)}
                  onChange={(e) => setMovement((m) => ({ ...m, dateReleased: e.target.value }))}
                  data-testid="input-released-date"
                />
              </FormField>
              <FormField label={t("fields.observaciones")}>
                <textarea
                  className={themeClass("textarea")}
                  value={movement.notes ?? ""}
                  onChange={(e) => setMovement((m) => ({ ...m, notes: e.target.value }))}
                  data-testid="input-notes"
                />
              </FormField>
              <CheckboxField
                label={t("fields.preparado")}
                checked={!!movement.prepared}
                onChange={(checked) => setMovement((m) => ({ ...m, prepared: checked }))}
                data-testid="checkbox-prepared"
              />
              <CheckboxField
                label={t("fields.observado")}
                checked={!!movement.flagged}
                onChange={(checked) => setMovement((m) => ({ ...m, flagged: checked }))}
                data-testid="checkbox-flagged"
              />
              <CheckboxField
                label={t("fields.entregado")}
                checked={!!movement.delivered}
                onChange={(checked) => setMovement((m) => ({ ...m, delivered: checked }))}
                data-testid="checkbox-delivered"
              />
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setEditingDocument(null)}>
                {tc("cancel")}
              </Button>
              <Button
                onClick={handleSaveMovement}
                disabled={registerMutation.isPending}
                data-testid="btn-save-movement"
              >
                {tc("save")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>
    </div>
  );
}
