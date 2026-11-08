/**
 * Reports page (CU01, CU13, CU25, CU50, CU84, #1246): each id-based report stays disabled until its id is
 * entered, the period reports send the chosen notary and folio type only when one is picked, a download
 * is confirmed or its failure reported, and a failed notary/folio-type catalog shows a retry.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const toast = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() }));
const downloads = vi.hoisted(() => ({
  budget: vi.fn(),
  budgetProperties: vi.fn(),
  history: vi.fn(),
  monthly: vi.fn(),
  tax: vi.fn(),
  indexBook: vi.fn(),
  debt: vi.fn(),
}));
const listNotaries = vi.hoisted(() => vi.fn());
const listFolioTypes = vi.hoisted(() => vi.fn());

vi.mock("sonner", () => ({ toast }));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));
vi.mock("@/components/layout/AppHeader", () => ({
  AppHeader: ({ title }: { title: string }) => <h1>{title}</h1>,
}));
vi.mock("@/composition/folio", () => ({ folioUseCases: { listAvailableNotaries: listNotaries } }));
vi.mock("@/composition/folio-type", () => ({ folioTypeUseCases: { list: listFolioTypes } }));
vi.mock("@/hooks/useReports", () => ({
  useBudgetReport: () => ({ download: downloads.budget }),
  useBudgetPropertiesReport: () => ({ download: downloads.budgetProperties }),
  useManagementHistoryReport: () => ({ download: downloads.history }),
  useMonthlySwornStatementReport: () => ({ download: downloads.monthly }),
  useTaxSwornStatementReport: () => ({ download: downloads.tax }),
  useIndexBookReport: () => ({ download: downloads.indexBook }),
  useDocumentDebtReport: () => ({ download: downloads.debt }),
}));
vi.mock("@/components/ui/select", async () => {
  const React = await import("react");
  const Ctx = React.createContext<{
    value: string;
    onValueChange: (v: string) => void;
    open: boolean;
    setOpen: (o: boolean) => void;
  }>({ value: "", onValueChange: () => undefined, open: false, setOpen: () => undefined });
  return {
    Select: ({ value, onValueChange, children }: {
      value: string; onValueChange: (v: string) => void; children: ReactNode;
    }) => {
      const [open, setOpen] = React.useState(false);
      return <Ctx.Provider value={{ value, onValueChange, open, setOpen }}>{children}</Ctx.Provider>;
    },
    SelectTrigger: ({ children, ...rest }: { children: ReactNode; "data-testid"?: string }) => {
      const ctx = React.useContext(Ctx);
      return <button type="button" onClick={() => ctx.setOpen(!ctx.open)} {...rest}>{children}</button>;
    },
    SelectValue: () => null,
    SelectContent: ({ children }: { children: ReactNode }) => {
      const ctx = React.useContext(Ctx);
      return ctx.open ? <div>{children}</div> : null;
    },
    SelectItem: ({ value, children }: { value: string; children: ReactNode }) => {
      const ctx = React.useContext(Ctx);
      return (
        <button type="button" role="option" aria-selected={ctx.value === value}
          onClick={() => { ctx.onValueChange(value); ctx.setOpen(false); }}>
          {children}
        </button>
      );
    },
  };
});

import { ApiError } from "@/lib/api-client";
import ReportsPage from "@/app/dashboard/reports/page";

const NOTARIES = [{ personId: 5, firstName: "Ana", lastName: "Pérez" }];
const FOLIO_TYPES = [{ id: 7, name: "Protocolo" }];

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function renderPage() {
  render(<ReportsPage />, { wrapper });
  await waitFor(() => expect(listFolioTypes).toHaveBeenCalled());
  await screen.findByTestId("card-report-budget");
  await waitFor(() => expect(screen.queryByText("common.loading")).toBeNull());
}

beforeEach(() => {
  Object.values(downloads).forEach((fn) => fn.mockReset().mockResolvedValue(undefined));
  Object.values(toast).forEach((fn) => fn.mockReset());
  listNotaries.mockReset().mockResolvedValue(NOTARIES);
  listFolioTypes.mockReset().mockResolvedValue(FOLIO_TYPES);
});

async function choose(trigger: string, option: string | RegExp) {
  fireEvent.click(screen.getByTestId(trigger));
  fireEvent.click(await screen.findByRole("option", { name: option }));
}

describe.each([
  ["budget", "input-report-budget-id", "btn-download-budget", "budget"],
  ["budget with properties", "input-report-budget-properties-id", "btn-download-budget-properties", "budgetProperties"],
  ["management history", "input-report-management-id", "btn-download-management-history", "history"],
  ["document debt", "input-report-debt-management-number", "btn-download-document-debt", "debt"],
] as const)("%s report", (_name, input, button, download) => {
  it("stays disabled until the id is entered and downloads for that id", async () => {
    await renderPage();
    expect(screen.getByTestId(button)).toBeDisabled();

    fireEvent.change(screen.getByTestId(input), { target: { value: "42" } });
    expect(screen.getByTestId(button)).toBeEnabled();
    fireEvent.click(screen.getByTestId(button));

    await waitFor(() => expect(downloads[download]).toHaveBeenCalledWith(42));
    expect(toast.success).toHaveBeenCalledWith("reportes.generated");
  });

  it("disables the button again when the id is cleared", async () => {
    await renderPage();
    fireEvent.change(screen.getByTestId(input), { target: { value: "42" } });
    fireEvent.change(screen.getByTestId(input), { target: { value: "" } });

    expect(screen.getByTestId(button)).toBeDisabled();
  });
});

describe("downloads", () => {
  it("reports a failed download with the fallback message and no success", async () => {
    downloads.budget.mockRejectedValue(new ApiError(500, "/reports/budget/42", ""));
    await renderPage();
    fireEvent.change(screen.getByTestId("input-report-budget-id"), { target: { value: "42" } });

    fireEvent.click(screen.getByTestId("btn-download-budget"));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("reportes.errorGenerate"));
    expect(toast.success).not.toHaveBeenCalled();
  });

  it("reports a failed download with the server's reason when it gives one", async () => {
    downloads.budget.mockRejectedValue(new ApiError(404, "/reports/budget/42", '{"message":"Presupuesto inexistente"}'));
    await renderPage();
    fireEvent.change(screen.getByTestId("input-report-budget-id"), { target: { value: "42" } });

    fireEvent.click(screen.getByTestId("btn-download-budget"));

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
    expect(toast.error).not.toHaveBeenCalledWith("reportes.errorGenerate");
  });
});

describe.each([
  ["monthly sworn statement", "monthly", "btn-download-monthly-sworn", "input-report-monthly-year",
    "select-report-monthly-notary", "select-report-monthly-folio-type", "input-report-monthly-month"],
  ["tax sworn statement", "tax", "btn-download-tax-sworn", "input-report-tax-year",
    "select-report-tax-notary", "select-report-tax-folio-type", "input-report-tax-month"],
] as const)("%s", (_name, download, button, year, notary, folioType, month) => {
  it("sends the chosen notary and folio type", async () => {
    await renderPage();
    fireEvent.change(screen.getByTestId(year), { target: { value: "2026" } });
    fireEvent.change(screen.getByTestId(month), { target: { value: "3" } });
    await choose(notary, /Pérez/);
    await choose(folioType, "Protocolo");

    fireEvent.click(screen.getByTestId(button));

    await waitFor(() => expect(downloads[download]).toHaveBeenCalledWith(2026, 3, 5, 7));
  });

  it("sends neither when the choice goes back to all", async () => {
    await renderPage();
    await choose(notary, /Pérez/);
    await choose(notary, "common.all");
    await choose(folioType, "Protocolo");
    await choose(folioType, "common.all");

    fireEvent.click(screen.getByTestId(button));

    await waitFor(() =>
      expect(downloads[download]).toHaveBeenCalledWith(expect.any(Number), expect.any(Number), undefined, undefined),
    );
  });
});

describe("index book", () => {
  it("sends the chosen notary and folio type with the year", async () => {
    await renderPage();
    fireEvent.change(screen.getByTestId("input-report-index-year"), { target: { value: "2025" } });
    await choose("select-report-index-notary", /Pérez/);
    await choose("select-report-index-folio-type", "Protocolo");

    fireEvent.click(screen.getByTestId("btn-download-index-book"));

    await waitFor(() => expect(downloads.indexBook).toHaveBeenCalledWith(2025, 5, 7));
  });

  it("sends neither when the choice goes back to all", async () => {
    await renderPage();
    await choose("select-report-index-notary", /Pérez/);
    await choose("select-report-index-notary", "common.all");
    await choose("select-report-index-folio-type", "Protocolo");
    await choose("select-report-index-folio-type", "common.all");

    fireEvent.click(screen.getByTestId("btn-download-index-book"));

    await waitFor(() => expect(downloads.indexBook).toHaveBeenCalledWith(expect.any(Number), undefined, undefined));
  });
});

describe("catalogs", () => {
  it("shows a loading state while the notaries and folio types load", async () => {
    listNotaries.mockReturnValue(new Promise(() => undefined));
    render(<ReportsPage />, { wrapper });

    expect(await screen.findByText("common.loading")).toBeInTheDocument();
  });

  it("shows the catalog error when the notaries fail and retries both catalogs", async () => {
    listNotaries.mockRejectedValueOnce(new ApiError(500, "/deeds/available-notaries", "")).mockResolvedValue(NOTARIES);
    render(<ReportsPage />, { wrapper });

    expect(await screen.findByText("reportes.catalogLoadError")).toBeInTheDocument();
    const folioTypeCalls = listFolioTypes.mock.calls.length;
    fireEvent.click(screen.getByTestId("btn-retry-reports-catalog"));

    await waitFor(() => expect(screen.queryByText("reportes.catalogLoadError")).toBeNull());
    expect(listFolioTypes.mock.calls.length).toBeGreaterThan(folioTypeCalls);
  });

  it("shows the catalog error when the folio types fail", async () => {
    listFolioTypes.mockRejectedValue(new ApiError(500, "/folio-types", ""));
    render(<ReportsPage />, { wrapper });

    expect(await screen.findByText("reportes.catalogLoadError")).toBeInTheDocument();
  });
});
