import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

const downloads = vi.hoisted(() => ({
  budget: vi.fn().mockResolvedValue(undefined),
  indexBook: vi.fn().mockResolvedValue(undefined),
}));

vi.mock("next-intl", () => ({ useTranslations: () => (key: string) => key }));
vi.mock("sonner", () => ({ toast: { success: vi.fn(), error: vi.fn() } }));
vi.mock("@/components/layout/AppHeader", () => ({
  AppHeader: ({ title }: { title: string }) => <h1>{title}</h1>,
}));
vi.mock("@/hooks/useReports", () => {
  const unused = () => ({ download: vi.fn().mockResolvedValue(undefined) });
  return {
    useBudgetReport: () => ({ download: downloads.budget }),
    useBudgetPropertiesReport: unused,
    useManagementHistoryReport: unused,
    useMonthlySwornStatementReport: unused,
    useTaxSwornStatementReport: unused,
    useIndexBookReport: () => ({ download: downloads.indexBook }),
    useDocumentDebtReport: unused,
  };
});

import ReportsPage from "@/app/dashboard/reports/page";

describe("Reports page (/dashboard/reports)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("shouldRenderOneCardForEachOfTheSevenReports", () => {
    render(<ReportsPage />);

    expect(screen.getByRole("heading", { level: 1 })).toHaveTextContent("title");
    expect(screen.getAllByTestId(/^card-report-/)).toHaveLength(7);
  });

  it("shouldKeepDownloadDisabledUntilTheRequiredIdIsEntered", async () => {
    render(<ReportsPage />);
    const button = screen.getByTestId("btn-download-budget");

    expect(button).toBeDisabled();
    await userEvent.type(screen.getByTestId("input-report-budget-id"), "42");
    expect(button).toBeEnabled();
  });

  it("shouldDownloadTheBudgetReportForTheEnteredId", async () => {
    render(<ReportsPage />);

    await userEvent.type(screen.getByTestId("input-report-budget-id"), "42");
    await userEvent.click(screen.getByTestId("btn-download-budget"));

    expect(downloads.budget).toHaveBeenCalledWith(42);
  });
});
