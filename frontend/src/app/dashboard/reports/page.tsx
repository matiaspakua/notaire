"use client";

import { useState } from "react";
import type { ReactNode } from "react";
import { toast } from "sonner";
import { FileDown, FileText, Book, ClipboardList, AlertCircle } from "lucide-react";
import type { LucideIcon } from "lucide-react";
import { useTranslations } from "next-intl";
import { useQuery } from "@tanstack/react-query";
import { AppHeader } from "@/components/layout/AppHeader";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { FormField } from "@/theme/form-patterns";
import { theme } from "@/theme/tokens";
import { apiGet } from "@/lib/api-client";
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

const cardStyle: React.CSSProperties = {
  backgroundColor: theme.semantic.card.bg,
  borderRadius: theme.borderRadius["2xl"],
  border: `1px solid ${theme.semantic.card.border}`,
  boxShadow: theme.shadows.sm,
  padding: theme.spacing[6],
  display: "flex",
  flexDirection: "column",
  gap: theme.spacing[4],
};

const cardTitleStyle: React.CSSProperties = {
  fontSize: theme.typography.fontSize.base,
  fontWeight: theme.typography.fontWeight.semibold,
  color: theme.colors.neutral[900],
  fontFamily: theme.typography.fontFamily.display,
  margin: 0,
};

const cardDescStyle: React.CSSProperties = {
  fontSize: theme.typography.fontSize.sm,
  color: theme.colors.neutral[600],
  fontFamily: theme.typography.fontFamily.body,
  margin: 0,
};

const iconStyle: React.CSSProperties = {
  color: theme.colors.primary[600],
  width: "20px",
  height: "20px",
  flexShrink: 0,
};

const currentYear = () => new Date().getFullYear().toString();
const currentMonth = () => (new Date().getMonth() + 1).toString();

type ReportCardProps = {
  testId: string;
  icon: LucideIcon;
  title: string;
  description: string;
  downloadLabel: string;
  downloadTestId: string;
  canDownload: boolean;
  onDownload: () => void;
  children: ReactNode;
};

function ReportCard({
  testId,
  icon: Icon,
  title,
  description,
  downloadLabel,
  downloadTestId,
  canDownload,
  onDownload,
  children,
}: ReportCardProps) {
  return (
    <div style={cardStyle} data-testid={testId}>
      <div style={{ display: "flex", flexDirection: "column", gap: theme.spacing[1] }}>
        <div style={{ display: "flex", alignItems: "center", gap: theme.spacing[2] }}>
          <Icon style={iconStyle} />
          <h3 style={cardTitleStyle}>{title}</h3>
        </div>
        <p style={cardDescStyle}>{description}</p>
      </div>
      {children}
      <Button
        className="w-full"
        variant="secondary"
        disabled={!canDownload}
        onClick={onDownload}
        data-testid={downloadTestId}
      >
        <FileDown className="h-4 w-4 mr-2" />
        {downloadLabel}
      </Button>
    </div>
  );
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

const yearMonthGrid: React.CSSProperties = {
  display: "grid",
  gridTemplateColumns: "1fr 1fr",
  gap: theme.spacing[3],
};

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
  const [taxYear, setTaxYear] = useState(currentYear());
  const [taxMonth, setTaxMonth] = useState(currentMonth());
  const [indexYear, setIndexYear] = useState(currentYear());
  const [indexNotary, setIndexNotary] = useState("");
  const [indexFolioType, setIndexFolioType] = useState("");
  const [debtManagementNumber, setDebtManagementNumber] = useState("");

  const { data: notaries = [] } = useQuery({
    queryKey: ["availableNotaries"],
    queryFn: () => apiGet<Person[]>("/deeds/available-notaries"),
  });
  const { data: folioTypes = [] } = useQuery({
    queryKey: ["folioTypes"],
    queryFn: () => apiGet<FolioType[]>("/folio-types"),
  });

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

      <div
        style={{
          display: "grid",
          gap: theme.spacing[4],
          gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))",
          padding: theme.spacing[4],
        }}
      >
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
          testId="card-report-monthly-sworn"
          icon={Book}
          title={t("ddjjMensual.title")}
          description={t("ddjjMensual.description")}
          downloadLabel={downloadLabel}
          downloadTestId="btn-download-monthly-sworn"
          canDownload
          onDownload={() =>
            handle(() => monthlySwornReport.download(Number(monthlyYear), Number(monthlyMonth)))
          }
        >
          <div style={yearMonthGrid}>
            <NumberField label={tc("year")} value={monthlyYear} onChange={setMonthlyYear} min="2000" max="2099" />
            <NumberField label={tc("month")} value={monthlyMonth} onChange={setMonthlyMonth} min="1" max="12" />
          </div>
        </ReportCard>

        <ReportCard
          testId="card-report-tax-sworn"
          icon={Book}
          title={t("ddjjRentas.title")}
          description={t("ddjjRentas.description")}
          downloadLabel={downloadLabel}
          downloadTestId="btn-download-tax-sworn"
          canDownload
          onDownload={() => handle(() => taxSwornReport.download(Number(taxYear), Number(taxMonth)))}
        >
          <div style={yearMonthGrid}>
            <NumberField label={tc("year")} value={taxYear} onChange={setTaxYear} min="2000" max="2099" />
            <NumberField label={tc("month")} value={taxMonth} onChange={setTaxMonth} min="1" max="12" />
          </div>
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
    </div>
  );
}
