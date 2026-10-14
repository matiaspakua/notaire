/**
 * Protocol reports (CU90-CU92, #1246): each of the three reports opens a dialog with only the
 * filters it supports and downloads the matching PDF.
 */
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiGet = vi.fn();
const apiGetBytes = vi.fn();
const toast = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn(), warning: vi.fn() }));

vi.mock("@/lib/api-client", async (orig) => ({
  ...(await orig<typeof import("@/lib/api-client")>()),
  apiGet: (path: string) => apiGet(path),
  apiGetBytes: (path: string) => apiGetBytes(path),
}));
vi.mock("sonner", () => ({ toast }));
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
  usePathname: () => "/dashboard/protocol",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));

import ProtocolPage from "@/app/dashboard/protocol/page";

const FOLIOS = [{ idFolio: 1, number: 77, status: "Nuevo", fkIdFolioType: { idFolioType: 2, name: "Protocolo" } }];
const YEAR = new Date().getFullYear().toString();
const MONTH = (new Date().getMonth() + 1).toString();

let clicked: HTMLAnchorElement[];

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function openReport(buttonKey: string) {
  render(<ProtocolPage />, { wrapper });
  await screen.findByText("77");
  fireEvent.click(screen.getByRole("button", { name: new RegExp(buttonKey.replace(/\./g, "\\.")) }));
  return screen.findByRole("dialog");
}

beforeEach(() => {
  clicked = [];
  apiGet.mockReset().mockImplementation((path: string) =>
    path === "/folios" ? Promise.resolve(FOLIOS) : Promise.resolve([]),
  );
  apiGetBytes.mockReset().mockResolvedValue(new Blob(["pdf"]));
  Object.values(toast).forEach((fn) => fn.mockReset());
  URL.createObjectURL = vi.fn(() => "blob:report");
  URL.revokeObjectURL = vi.fn();
  vi.spyOn(HTMLAnchorElement.prototype, "click").mockImplementation(function (this: HTMLAnchorElement) {
    clicked.push(this);
  });
});

afterEach(() => vi.restoreAllMocks());

describe("protocol page", () => {
  it("lists the folios with their type", async () => {
    render(<ProtocolPage />, { wrapper });

    expect(await screen.findByText("77")).toBeInTheDocument();
    expect(screen.getByText("Protocolo")).toBeInTheDocument();
  });

  it("downloads the index book for the chosen year without a month", async () => {
    const dialog = await openReport("protocolo.reports.libroIndice.button");

    expect(within(dialog).queryByText("common.month")).toBeNull();
    fireEvent.change(within(dialog).getByTestId("input-protocol-report-year"), { target: { value: "2025" } });
    fireEvent.click(within(dialog).getByRole("button", { name: /common\.downloadPdf/ }));

    await waitFor(() => expect(apiGetBytes).toHaveBeenCalledWith("/reports/index-book?year=2025"));
    expect(clicked[0].download).toBe("libro_indice_2025.pdf");
    expect(toast.success).toHaveBeenCalledWith("protocolo.reportDownloaded");
  });

  it("downloads the monthly statement with the year, the month and both optional filters offered", async () => {
    const dialog = await openReport("protocolo.reports.ddjjMensual.button");

    expect(within(dialog).getByTestId("select-protocol-index-notary")).toBeInTheDocument();
    expect(within(dialog).getByTestId("select-protocol-index-folio-type")).toBeInTheDocument();
    fireEvent.click(within(dialog).getByRole("button", { name: /common\.downloadPdf/ }));

    await waitFor(() =>
      expect(apiGetBytes).toHaveBeenCalledWith(`/reports/monthly-sworn-statement?year=${YEAR}&month=${MONTH}`),
    );
    expect(clicked[0].download).toBe(`ddjj_mensual_${YEAR}_${MONTH}.pdf`);
  });

  it("downloads the tax statement without the notary and folio type filters", async () => {
    const dialog = await openReport("protocolo.reports.ddjjRentas.button");

    expect(within(dialog).queryByTestId("select-protocol-index-notary")).toBeNull();
    expect(within(dialog).queryByTestId("select-protocol-index-folio-type")).toBeNull();
    fireEvent.click(within(dialog).getByRole("button", { name: /common\.downloadPdf/ }));

    await waitFor(() =>
      expect(apiGetBytes).toHaveBeenCalledWith(`/reports/tax-sworn-statement?year=${YEAR}&month=${MONTH}`),
    );
    expect(clicked[0].download).toBe(`declaracion_jurada_rentas_${MONTH}_${YEAR}.pdf`);
  });

  it("reports a failed download and keeps the dialog open", async () => {
    apiGetBytes.mockRejectedValue(new Error("boom"));
    const dialog = await openReport("protocolo.reports.libroIndice.button");

    fireEvent.click(within(dialog).getByRole("button", { name: /common\.downloadPdf/ }));

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
    expect(screen.getByRole("dialog")).toBeInTheDocument();
    expect(clicked).toHaveLength(0);
  });
});
