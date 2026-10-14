/**
 * Managements list actions (CU13, CU16, CU85, #1246): archive warns about debt and asks again after
 * a 409, an archived management cannot change state or be archived again, the history and the
 * folders open from the row, and putting a folder on hold needs a reason.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiGet = vi.fn();
const apiGetPage = vi.fn();
const apiPost = vi.fn();
const apiPut = vi.fn();
const apiDelete = vi.fn();
const toast = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() }));

vi.mock("@/lib/api-client", async (orig) => ({
  ...(await orig<typeof import("@/lib/api-client")>()),
  apiGet: (path: string) => apiGet(path),
  apiGetPage: (path: string, request: unknown) => apiGetPage(path, request),
  apiPost: (path: string, body: unknown) => apiPost(path, body),
  apiPut: (path: string, body: unknown) => apiPut(path, body),
  apiDelete: (path: string) => apiDelete(path),
}));
vi.mock("sonner", () => ({ toast }));
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
  usePathname: () => "/dashboard/managements",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string, values?: Record<string, unknown>) =>
    values ? `${ns}.${key}:${JSON.stringify(values)}` : `${ns}.${key}`,
  useLocale: () => "es",
}));

import { ApiError } from "@/lib/api-client";
import ManagementsPage from "@/app/dashboard/managements/page";

const ROWS = [
  { idManagement: 1, number: 5001, currentStatus: "Iniciada", procedureCount: 2 },
  { idManagement: 2, number: 5002, currentStatus: "Archivada", procedureCount: 1 },
];

let outstanding = { pendingBalance: 0 };

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

function route(path: string) {
  if (path.endsWith("/outstanding-balance")) return Promise.resolve(outstanding);
  if (path.endsWith("/history")) {
    return Promise.resolve([{ idHistory: 7, statusManagementName: "Iniciada", date: "2026-01-01", notes: "alta" }]);
  }
  if (path.startsWith("/procedure-folders?")) {
    return Promise.resolve([
      { idFolder: 9, number: 3, status: "Activa", waitReason: null },
      { idFolder: 10, number: 4, status: "En espera", waitReason: "falta firma" },
    ]);
  }
  return Promise.resolve([]);
}

async function renderPage() {
  render(<ManagementsPage />, { wrapper });
  await screen.findByText("5001");
}

beforeEach(() => {
  outstanding = { pendingBalance: 0 };
  apiGet.mockReset().mockImplementation(route);
  apiGetPage.mockReset().mockResolvedValue({ content: ROWS, number: 0, size: 20, totalElements: 2, totalPages: 1 });
  apiPost.mockReset().mockResolvedValue({});
  apiPut.mockReset().mockResolvedValue({});
  apiDelete.mockReset().mockResolvedValue(undefined);
  Object.values(toast).forEach((fn) => fn.mockReset());
});

describe("row actions", () => {
  it("hides change state and archive for an archived management", async () => {
    await renderPage();

    expect(screen.getByTestId("btn-change-status-1")).toBeInTheDocument();
    expect(screen.getByTestId("btn-archive-management-1")).toBeInTheDocument();
    expect(screen.queryByTestId("btn-change-status-2")).toBeNull();
    expect(screen.queryByTestId("btn-archive-management-2")).toBeNull();
  });

  it("deletes a management after confirmation", async () => {
    await renderPage();
    const [deleteButton] = screen.getAllByRole("button", { name: "common.delete" });

    fireEvent.click(deleteButton);
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiDelete).toHaveBeenCalledWith("/managements/1"));
    expect(toast.success).toHaveBeenCalledWith("gestiones.deleted");
  });

  it("lists the history of a management", async () => {
    await renderPage();

    fireEvent.click(screen.getByTestId("btn-view-history-1"));

    const items = await screen.findAllByTestId("history-item");
    expect(items).toHaveLength(1);
    expect(items[0]).toHaveTextContent("alta");
  });
});

describe("archive", () => {
  it("archives without debt after one confirmation", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-archive-management-1"));

    expect(await screen.findByText("gestiones.archiveConfirmDescriptionNoDebt")).toBeInTheDocument();
    fireEvent.click(screen.getByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/managements/1/archive?confirmado=false", {}));
    expect(toast.success).toHaveBeenCalledWith("gestiones.archived");
  });

  it("warns about the outstanding debt before archiving", async () => {
    outstanding = { pendingBalance: 1500 };
    await renderPage();

    fireEvent.click(screen.getByTestId("btn-archive-management-1"));

    expect(await screen.findByText(/gestiones\.archiveConfirmDescriptionWithDebt/)).toBeInTheDocument();
  });

  it("asks again after a conflict and archives anyway with the confirmation flag", async () => {
    apiPost.mockRejectedValueOnce(new ApiError(409, "/managements/1/archive", '{"message":"carpetas en espera"}'));
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-archive-management-1"));

    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    expect(await screen.findByText("gestiones.archiveConflictTitle")).toBeInTheDocument();
    expect(screen.getByTestId("confirm-dialog-confirm")).toHaveTextContent("gestiones.archiveConfirmAnyway");
    fireEvent.click(screen.getByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiPost).toHaveBeenLastCalledWith("/managements/1/archive?confirmado=true", {}));
  });

  it("reports an archive failure that is not a conflict and closes the dialog", async () => {
    apiPost.mockRejectedValueOnce(new Error("boom"));
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-archive-management-1"));

    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("gestiones.errorArchive"));
    await waitFor(() => expect(screen.queryByTestId("confirm-dialog-confirm")).toBeNull());
  });
});

describe("procedure folders", () => {
  it("offers to hold only the active folder", async () => {
    await renderPage();

    fireEvent.click(screen.getByTestId("btn-view-folders-1"));

    expect(await screen.findAllByTestId("folder-item")).toHaveLength(2);
    expect(screen.getByTestId("btn-place-on-hold-9")).toBeInTheDocument();
    expect(screen.queryByTestId("btn-place-on-hold-10")).toBeNull();
    expect(screen.getByText("falta firma")).toBeInTheDocument();
  });

  it("needs a reason to put a folder on hold", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-view-folders-1"));
    fireEvent.click(await screen.findByTestId("btn-place-on-hold-9"));

    fireEvent.click(await screen.findByTestId("btn-confirm-wait"));

    expect(toast.error).toHaveBeenCalledWith("gestiones.errorMotivoRequerido");
    expect(apiPut).not.toHaveBeenCalled();
  });

  it("puts a folder on hold with its reason", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-view-folders-1"));
    fireEvent.click(await screen.findByTestId("btn-place-on-hold-9"));

    fireEvent.change(await screen.findByTestId("input-wait-reason"), { target: { value: "falta firma" } });
    fireEvent.click(screen.getByTestId("btn-confirm-wait"));

    await waitFor(() => expect(apiPut).toHaveBeenCalledWith("/procedure-folders/9/wait", { reason: "falta firma" }));
    expect(toast.success).toHaveBeenCalledWith("gestiones.carpetaEnEspera");
  });
});
