"use client";

import { useState } from "react";
import { toast } from "sonner";
import { FileText, Book, ClipboardList, AlertCircle } from "lucide-react";
import { useTranslations } from "next-intl";
import { useQuery } from "@tanstack/react-query";
import { AppHeader } from "@/components/layout/AppHeader";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { FormField } from "@/theme/form-patterns";
import { PageSection } from "@/components/shared/PageSection";
import { ReportCard } from "@/components/shared/ReportCard";
import { LoadingState } from "@/components/shared/LoadingState";
import { ErrorState } from "@/components/shared/ErrorState";
import { folioUseCases } from "@/composition/folio";
import { folioTypeUseCases } from "@/composition/folio-type";
import type { FolioType as DomainFolioType } from "@/core/folio-type/domain/folio-type";
import type { FolioType, Person } from "@/types";
import {
  useBudgetReport,
  useBudgetPropertiesReport,
  useManagementHistoryReport,
  useMonthlySwornStatementReport,
  useTaxSwornStatementReport,
  useIndexBookReport,
  useDocumentDebtReport,
} from "@/hooks/useReports";
import { presentMutationError } from "@/lib/mutation-error";

function toWireFolioType(t: DomainFolioType): FolioType {
  return {
    idFolioType: t.id,
    name: t.name,
    isAuxiliary: t.isAuxiliary,
    notes: t.notes,
    enabled: t.enabled,
  };
}

type NumberFieldProps = {
  label: string;
  value: string;
  onChange: (value: string) => void;
  testId?: string;
  placeholder?: string;
  required?: boolean;
  min?: string;
  max?: string;
};

function NumberField({ label, value, onChange, testId, placeholder, required, min, max }: NumberFieldProps) {
  return (
    <FormField label={label} required={required}>
      <Input
        type="number"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        min={min}
        max={max}
        data-testid={testId}
      />
    </FormField>
  );
}

const reportGridClass = "grid gap-4 grid-cols-[repeat(auto-fill,minmax(300px,1fr))]";

const currentYear = () => new Date().getFullYear().toString();
const currentMonth = () => (new Date().getMonth() + 1).toString();

export default function ReportsPage() {
  const t = useTranslations("reportes");
  const tc = useTranslations("common");
  const tf = useTranslations("administracion.folios");
  const budgetReport = useBudgetReport();
  const budgetPropertiesReport = useBudgetPropertiesReport();
  const managementHistoryReport = useManagementHistoryReport();
  const monthlySwornReport = useMonthlySwornStatementReport();
  const taxSwornReport = useTaxSwornStatementReport();
  const indexBookReport = useIndexBookReport();
  const documentDebtReport = useDocumentDebtReport();

  const [budgetId, setBudgetId] = useState("");
  const [budgetPropertiesId, setBudgetPropertiesId] = useState("");
  const [managementId, setManagementId] = useState("");
  const [monthlyYear, setMonthlyYear] = useState(currentYear());
  const [monthlyMonth, setMonthlyMonth] = useState(currentMonth());
  const [monthlyNotary, setMonthlyNotary] = useState("");
  const [monthlyFolioType, setMonthlyFolioType] = useState("");
  const [taxYear, setTaxYear] = useState(currentYear());
  const [taxMonth, setTaxMonth] = useState(currentMonth());
  const [taxNotary, setTaxNotary] = useState("");
  const [taxFolioType, setTaxFolioType] = useState("");
  const [indexYear, setIndexYear] = useState(currentYear());
  const [indexNotary, setIndexNotary] = useState("");
  const [indexFolioType, setIndexFolioType] = useState("");
  const [debtManagementNumber, setDebtManagementNumber] = useState("");

  const {
    data: notaries = [],
    isLoading: isNotariesLoading,
    isError: isNotariesError,
    refetch: refetchNotaries,
  } = useQuery({
    queryKey: ["availableNotaries"],
    queryFn: async () => (await folioUseCases.listAvailableNotaries()) as Person[],
  });
  const {
    data: folioTypes = [],
    isLoading: isFolioTypesLoading,
    isError: isFolioTypesError,
    refetch: refetchFolioTypes,
  } = useQuery({
    queryKey: ["folioTypes"],
    queryFn: async () => (await folioTypeUseCases.list()).map(toWireFolioType),
  });

  const catalogLoading =
    (isNotariesLoading && notaries.length === 0) || (isFolioTypesLoading && folioTypes.length === 0);
  const catalogError = isNotariesError || isFolioTypesError;

  async function handle(download: () => Promise<void>) {
    try {
      await download();
      toast.success(t("generated"));
    } catch (err) {
      presentMutationError(err, { fallback: t("errorGenerate") });
    }
  }

  const downloadLabel = tc("downloadPdf");

  return (
    <div>
      <AppHeader title={t("title")} description={t("description")} />

      <div className="space-y-10 px-4 pb-8">
        {catalogError ? (
          <div className="rounded-[24px] border border-border/40 bg-white apple-shadow">
            <ErrorState
              title={t("catalogLoadError")}
              action={
                <Button
                  type="button"
                  variant="secondary"
                  data-testid="btn-retry-reports-catalog"
                  onClick={() => {
                    void refetchNotaries();
                    void refetchFolioTypes();
                  }}
                >
                  {tc("retry")}
                </Button>
              }
            />
          </div>
        ) : catalogLoading ? (
          <LoadingState label={tc("loading")} />
        ) : null}

        <PageSection title={t("sections.quotesAndCases")}>
          <div className={reportGridClass}>
            <ReportCard
              testId="card-report-budget"
              icon={FileText}
              title={t("presupuesto.title")}
              description={t("presupuesto.description")}
              downloadLabel={downloadLabel}
              downloadTestId="btn-download-budget"
              canDownload={Boolean(budgetId)}
              onDownload={() => handle(() => budgetReport.download(Number(budgetId)))}
            >
              <NumberField
                label={t("fields.presupuestoId")}
                value={budgetId}
                onChange={setBudgetId}
                placeholder={t("fields.ejemplo")}
                testId="input-report-budget-id"
                required
              />
            </ReportCard>

            <ReportCard
              testId="card-report-budget-properties"
              icon={FileText}
              title={t("presupuestoInmuebles.title")}
              description={t("presupuestoInmuebles.description")}
              downloadLabel={downloadLabel}
              downloadTestId="btn-download-budget-properties"
              canDownload={Boolean(budgetPropertiesId)}
              onDownload={() => handle(() => budgetPropertiesReport.download(Number(budgetPropertiesId)))}
            >
              <NumberField
                label={t("fields.presupuestoId")}
                value={budgetPropertiesId}
                onChange={setBudgetPropertiesId}
                placeholder={t("fields.ejemplo")}
                testId="input-report-budget-properties-id"
                required
              />
            </ReportCard>

            <ReportCard
              testId="card-report-management-history"
              icon={ClipboardList}
              title={t("historial.title")}
              description={t("historial.description")}
              downloadLabel={downloadLabel}
              downloadTestId="btn-download-management-history"
              canDownload={Boolean(managementId)}
              onDownload={() => handle(() => managementHistoryReport.download(Number(managementId)))}
            >
              <NumberField
                label={t("fields.gestionId")}
                value={managementId}
                onChange={setManagementId}
                placeholder={t("fields.ejemplo")}
                testId="input-report-management-id"
                required
              />
            </ReportCard>

            <ReportCard
              testId="card-report-document-debt"
              icon={AlertCircle}
              title={t("deudaDocumentos.title")}
              description={t("deudaDocumentos.description")}
              downloadLabel={downloadLabel}
              downloadTestId="btn-download-document-debt"
              canDownload={Boolean(debtManagementNumber)}
              onDownload={() => handle(() => documentDebtReport.download(Number(debtManagementNumber)))}
            >
              <NumberField
                label={t("fields.numeroGestion")}
                value={debtManagementNumber}
                onChange={setDebtManagementNumber}
                placeholder={t("fields.ejemploGestion")}
                testId="input-report-debt-management-number"
                required
              />
            </ReportCard>
          </div>
        </PageSection>

        <PageSection title={t("sections.swornAndBooks")}>
          <div className={reportGridClass}>
            <ReportCard
              testId="card-report-monthly-sworn"
              icon={Book}
              title={t("ddjjMensual.title")}
              description={t("ddjjMensual.description")}
              downloadLabel={downloadLabel}
              downloadTestId="btn-download-monthly-sworn"
              canDownload
              onDownload={() =>
                handle(() =>
                  monthlySwornReport.download(
                    Number(monthlyYear),
                    Number(monthlyMonth),
                    monthlyNotary ? Number(monthlyNotary) : undefined,
                    monthlyFolioType ? Number(monthlyFolioType) : undefined,
                  ),
                )
              }
            >
              <div className="grid grid-cols-2 gap-3">
                <NumberField
                  label={tc("year")}
                  value={monthlyYear}
                  onChange={setMonthlyYear}
                  min="2000"
                  max="2099"
                  testId="input-report-monthly-year"
                  required
                />
                <NumberField
                  label={tc("month")}
                  value={monthlyMonth}
                  onChange={setMonthlyMonth}
                  min="1"
                  max="12"
                  testId="input-report-monthly-month"
                  required
                />
              </div>
              <FormField label={tf("fields.escribano")} helperText={tc("optional")}>
                <Select
                  value={monthlyNotary || "all"}
                  onValueChange={(value) => setMonthlyNotary(value === "all" ? "" : value)}
                >
                  <SelectTrigger data-testid="select-report-monthly-notary">
                    <SelectValue placeholder={tf("fields.escribanoPlaceholder")} />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">{tc("all")}</SelectItem>
                    {notaries.map((notary) => (
                      <SelectItem key={notary.personId} value={String(notary.personId)}>
                        {notary.lastName} {notary.firstName}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label={tf("fields.tipo")} helperText={tc("optional")}>
                <Select
                  value={monthlyFolioType || "all"}
                  onValueChange={(value) => setMonthlyFolioType(value === "all" ? "" : value)}
                >
                  <SelectTrigger data-testid="select-report-monthly-folio-type">
                    <SelectValue placeholder={tf("fields.tipoPlaceholder")} />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">{tc("all")}</SelectItem>
                    {folioTypes.map((type) => (
                      <SelectItem key={type.idFolioType} value={String(type.idFolioType)}>
                        {type.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
            </ReportCard>

            <ReportCard
              testId="card-report-tax-sworn"
              icon={Book}
              title={t("ddjjRentas.title")}
              description={t("ddjjRentas.description")}
              downloadLabel={downloadLabel}
              downloadTestId="btn-download-tax-sworn"
              canDownload
              onDownload={() =>
                handle(() =>
                  taxSwornReport.download(
                    Number(taxYear),
                    Number(taxMonth),
                    taxNotary ? Number(taxNotary) : undefined,
                    taxFolioType ? Number(taxFolioType) : undefined,
                  ),
                )
              }
            >
              <div className="grid grid-cols-2 gap-3">
                <NumberField
                  label={tc("year")}
                  value={taxYear}
                  onChange={setTaxYear}
                  min="2000"
                  max="2099"
                  testId="input-report-tax-year"
                  required
                />
                <NumberField
                  label={tc("month")}
                  value={taxMonth}
                  onChange={setTaxMonth}
                  min="1"
                  max="12"
                  testId="input-report-tax-month"
                  required
                />
              </div>
              <FormField label={tf("fields.escribano")} helperText={tc("optional")}>
                <Select
                  value={taxNotary || "all"}
                  onValueChange={(value) => setTaxNotary(value === "all" ? "" : value)}
                >
                  <SelectTrigger data-testid="select-report-tax-notary">
                    <SelectValue placeholder={tf("fields.escribanoPlaceholder")} />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">{tc("all")}</SelectItem>
                    {notaries.map((notary) => (
                      <SelectItem key={notary.personId} value={String(notary.personId)}>
                        {notary.lastName} {notary.firstName}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label={tf("fields.tipo")} helperText={tc("optional")}>
                <Select
                  value={taxFolioType || "all"}
                  onValueChange={(value) => setTaxFolioType(value === "all" ? "" : value)}
                >
                  <SelectTrigger data-testid="select-report-tax-folio-type">
                    <SelectValue placeholder={tf("fields.tipoPlaceholder")} />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">{tc("all")}</SelectItem>
                    {folioTypes.map((type) => (
                      <SelectItem key={type.idFolioType} value={String(type.idFolioType)}>
                        {type.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
            </ReportCard>

            <ReportCard
              testId="card-report-index-book"
              icon={Book}
              title={t("libroIndice.title")}
              description={t("libroIndice.description")}
              downloadLabel={downloadLabel}
              downloadTestId="btn-download-index-book"
              canDownload
              onDownload={() =>
                handle(() =>
                  indexBookReport.download(
                    Number(indexYear),
                    indexNotary ? Number(indexNotary) : undefined,
                    indexFolioType ? Number(indexFolioType) : undefined,
                  ),
                )
              }
            >
              <NumberField
                label={tc("year")}
                value={indexYear}
                onChange={setIndexYear}
                min="2000"
                max="2099"
                testId="input-report-index-year"
                required
              />
              <FormField label={tf("fields.escribano")} helperText={tc("optional")}>
                <Select
                  value={indexNotary || "all"}
                  onValueChange={(value) => setIndexNotary(value === "all" ? "" : value)}
                >
                  <SelectTrigger data-testid="select-report-index-notary">
                    <SelectValue placeholder={tf("fields.escribanoPlaceholder")} />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">{tc("all")}</SelectItem>
                    {notaries.map((notary) => (
                      <SelectItem key={notary.personId} value={String(notary.personId)}>
                        {notary.lastName} {notary.firstName}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label={tf("fields.tipo")} helperText={tc("optional")}>
                <Select
                  value={indexFolioType || "all"}
                  onValueChange={(value) => setIndexFolioType(value === "all" ? "" : value)}
                >
                  <SelectTrigger data-testid="select-report-index-folio-type">
                    <SelectValue placeholder={tf("fields.tipoPlaceholder")} />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">{tc("all")}</SelectItem>
                    {folioTypes.map((type) => (
                      <SelectItem key={type.idFolioType} value={String(type.idFolioType)}>
                        {type.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
            </ReportCard>
          </div>
        </PageSection>
      </div>
    </div>
  );
}
