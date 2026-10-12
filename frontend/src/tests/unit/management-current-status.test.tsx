/**
 * #1553 / CU14: the case summary shows the management's current status from
 * GET /managements/{id}/current-status; a 404 means "Sin estado registrado".
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiGet = vi.fn();
vi.mock("@/lib/api-client", async (orig) => ({
  ...(await orig<typeof import("@/lib/api-client")>()),
  apiGet: (path: string) => apiGet(path),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));

import { ApiError } from "@/lib/api-client";
import { useManagementCurrentStatus } from "@/hooks/useManagements";
import { ManagementCurrentStatus } from "@/app/dashboard/managements/ManagementCurrentStatus";

function wrapper({ children }: { children: ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={qc}>{children}</QueryClientProvider>;
}

beforeEach(() => apiGet.mockReset());

describe("useManagementCurrentStatus (#1553)", () => {
  it("calls GET /managements/{id}/current-status", async () => {
    apiGet.mockResolvedValue({ statusManagementName: "Iniciada" });
    const { result } = renderHook(() => useManagementCurrentStatus(42), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiGet).toHaveBeenCalledWith("/managements/42/current-status");
  });

  it("does not fetch without an id", () => {
    renderHook(() => useManagementCurrentStatus(undefined), { wrapper });
    expect(apiGet).not.toHaveBeenCalled();
  });
});

describe("ManagementCurrentStatus (#1553)", () => {
  it("shows a History-based summary with status, date and notes", async () => {
    apiGet.mockResolvedValue({
      idHistory: 9, managementId: 42, statusManagementId: 3,
      statusManagementName: "En registro", date: "2026-10-01T10:00:00Z", notes: "Enviada al registro",
    });
    render(<ManagementCurrentStatus managementId={42} />, { wrapper });
    const card = await screen.findByTestId("management-current-status");
    expect(card).toHaveTextContent("En registro");
    expect(card).toHaveTextContent("Enviada al registro");
    expect(screen.getByTestId("management-current-status-date")).not.toHaveTextContent("—");
  });

  it("shows a synthesized summary (no History row) with just the status", async () => {
    apiGet.mockResolvedValue({ managementId: 42, statusManagementId: 1, statusManagementName: "Iniciada" });
    render(<ManagementCurrentStatus managementId={42} />, { wrapper });
    const card = await screen.findByTestId("management-current-status");
    expect(card).toHaveTextContent("Iniciada");
    expect(screen.queryByTestId("management-current-status-notes")).toBeNull();
  });

  it("shows 'Sin estado registrado' on 404", async () => {
    apiGet.mockRejectedValue(new ApiError(404, "/managements/42/current-status", ""));
    render(<ManagementCurrentStatus managementId={42} />, { wrapper });
    expect(await screen.findByTestId("management-current-status-empty")).toHaveTextContent(
      "gestiones.estadoActual.sinEstado",
    );
  });
});
