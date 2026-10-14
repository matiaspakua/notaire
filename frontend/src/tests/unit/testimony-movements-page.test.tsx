/**
 * CU11 / CU12 / CU44 (#1246): the testimony movements page offers the one action each testimony
 * status allows and sends it to the matching endpoint.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiGet = vi.fn();
const apiPost = vi.fn();
const toast = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn() }));

vi.mock("@/lib/api-client", async (orig) => ({
  ...(await orig<typeof import("@/lib/api-client")>()),
  apiGet: (path: string) => apiGet(path),
  apiPost: (path: string, body: unknown) => apiPost(path, body),
}));
vi.mock("sonner", () => ({ toast }));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));

import MovementsTestimonyPage from "@/app/dashboard/testimony-movements/page";

const testimonies = [
  { idTestimony: 1, number: 101, verified: true, testimonyMovements: [] },
  {
    idTestimony: 2,
    number: 102,
    verified: true,
    testimonyMovements: [{ idTestimonyMovement: 1, dateEntry: "2026-01-01", registered: false }],
  },
  {
    idTestimony: 3,
    number: 103,
    verified: true,
    testimonyMovements: [{ idTestimonyMovement: 2, dateEntry: "2026-01-01", registered: true }],
  },
  {
    idTestimony: 4,
    number: 104,
    verified: true,
    testimonyMovements: [
      { idTestimonyMovement: 3, dateEntry: "2026-01-01", registered: true, dateExit: "2026-02-01" },
    ],
  },
  { idTestimony: 5, number: 105, verified: false, testimonyMovements: [] },
];

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function renderPage() {
  render(<MovementsTestimonyPage />, { wrapper });
  await screen.findByText("101");
}

beforeEach(() => {
  apiGet.mockReset().mockResolvedValue(testimonies);
  apiPost.mockReset().mockResolvedValue({});
  toast.success.mockReset();
  toast.error.mockReset();
});

describe("testimony movements page (CU11, CU12, CU44)", () => {
  it("lists only verified testimonies with the one action their status allows", async () => {
    await renderPage();

    expect(screen.queryByText("105")).toBeNull();
    expect(screen.getByTestId("btn-sign-in-testimony-1")).toBeInTheDocument();
    expect(screen.getByTestId("btn-record-registration-2")).toBeInTheDocument();
    expect(screen.getByTestId("btn-withdraw-testimony-3")).toBeInTheDocument();
    expect(screen.getByTestId("btn-reenter-testimony-4")).toBeInTheDocument();
    expect(screen.queryByTestId("btn-withdraw-testimony-1")).toBeNull();
  });

  it("sends a testimony to the registry", async () => {
    await renderPage();

    fireEvent.click(screen.getByTestId("btn-sign-in-testimony-1"));

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/testimony-movements/1/enter-registration", {}));
    await waitFor(() => expect(toast.success).toHaveBeenCalled());
  });

  it("records the inscription", async () => {
    await renderPage();

    fireEvent.click(screen.getByTestId("btn-record-registration-2"));

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/testimony-movements/2/register-inscription", {}));
  });

  it("withdraws a registered testimony only once the card number is typed", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-withdraw-testimony-3"));

    const confirm = await screen.findByTestId("btn-confirm-withdraw");
    expect(confirm).toBeDisabled();
    fireEvent.change(screen.getByTestId("input-card-number"), { target: { value: "77" } });
    expect(confirm).toBeEnabled();
    fireEvent.click(confirm);

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith("/testimony-movements/3/withdraw", { cardNumber: 77 }),
    );
  });

  it("reenters a withdrawn testimony with the card number and notes", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-reenter-testimony-4"));

    fireEvent.change(await screen.findByTestId("input-reentry-card-number"), { target: { value: "9" } });
    fireEvent.click(screen.getByTestId("btn-confirm-reentry"));

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith("/testimony-movements/4/reenter", {
        cardNumber: 9,
        observedByRegistry: false,
        notes: "",
      }),
    );
  });

  it("shows an error and keeps the page usable when the movement fails", async () => {
    apiPost.mockRejectedValue(new Error("boom"));
    await renderPage();

    fireEvent.click(screen.getByTestId("btn-sign-in-testimony-1"));

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
    expect(screen.getByTestId("btn-sign-in-testimony-1")).toBeInTheDocument();
  });
});
