/**
 * Wave 8 (#1961): copies create dialog blocks empty required fields via FormField errors.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiGet = vi.fn();
const apiPost = vi.fn();
const toast = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() }));

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

import CopiesPage from "@/app/dashboard/copies/page";

function wrap(ui: ReactNode) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{ui}</QueryClientProvider>;
}

describe("CopiesPage form validation (#1961)", () => {
  beforeEach(() => {
    apiGet.mockResolvedValue([]);
    apiPost.mockReset();
    toast.success.mockReset();
    toast.error.mockReset();
  });

  it("shows required errors and does not create when number is empty", async () => {
    render(wrap(<CopiesPage />));
    fireEvent.click(await screen.findByTestId("btn-new-copy"));
    const number = await screen.findByTestId("input-number-copy");
    fireEvent.change(number, { target: { value: "" } });
    fireEvent.click(screen.getByTestId("btn-save-copy"));
    await waitFor(() => {
      expect(screen.getByText("common.fieldRequired")).toBeTruthy();
    });
    expect(apiPost).not.toHaveBeenCalled();
  });
});
