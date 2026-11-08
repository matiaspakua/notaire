/**
 * Deeds page (CU05, CU06, CU52, CU62, #1246): required fields block a save and the folio is only required
 * on create, a deed is created, edited and deleted, only an unsigned deed can be signed (after a
 * confirmation), the list narrows by a number looked up on the server, and load errors retry.
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
  usePathname: () => "/dashboard/deeds",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string, values?: Record<string, unknown>) =>
    values ? `${ns}.${key}:${JSON.stringify(values)}` : `${ns}.${key}`,
  useLocale: () => "es",
}));
vi.mock("@/components/ui/select", async () => {
  const React = await import("react");
  const Ctx = React.createContext<{
    value: string;
    onValueChange: (v: string) => void;
    disabled?: boolean;
    open: boolean;
    setOpen: (o: boolean) => void;
  }>({ value: "", onValueChange: () => undefined, open: false, setOpen: () => undefined });
  return {
    Select: ({ value, onValueChange, disabled, children }: {
      value: string; onValueChange: (v: string) => void; disabled?: boolean; children: ReactNode;
    }) => {
      const [open, setOpen] = React.useState(false);
      return <Ctx.Provider value={{ value, onValueChange, disabled, open, setOpen }}>{children}</Ctx.Provider>;
    },
    SelectTrigger: ({ children, ...rest }: { children: ReactNode; "data-testid"?: string }) => {
      const ctx = React.useContext(Ctx);
      return <button type="button" disabled={ctx.disabled} onClick={() => ctx.setOpen(!ctx.open)} {...rest}>{children}</button>;
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
import DeedsPage from "@/app/dashboard/deeds/page";

const ROWS = [
  { idDeed: 1, number: 701, dateDeedrecording: "2026-01-05", status: "Sin Firmar" },
  { idDeed: 2, number: 702, dateDeedrecording: "2026-02-06", status: "Firmada" },
  { idDeed: 3, dateDeedrecording: "2026-03-07" },
];
const FOLIOS = [
  { idFolio: 9, status: "Nuevo", fkIdFolioType: { name: "Simple" } },
  { idFolio: 10, status: "Usado", fkIdDeed: { idDeed: 2 } },
  { idFolio: 11, status: "Nuevo" },
];

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

function route(path: string): Promise<unknown> {
  if (path === "/folios") return Promise.resolve(FOLIOS);
  if (path === "/deeds/search?number=702") return Promise.resolve([ROWS[1]]);
  if (path.startsWith("/deeds/search")) return Promise.resolve([]);
  return Promise.resolve([]);
}

async function renderPage() {
  render(<DeedsPage />, { wrapper });
  await screen.findByTestId("btn-sign-deed-1");
}

beforeEach(() => {
  apiGet.mockReset().mockImplementation(route);
  apiGetPage.mockReset().mockResolvedValue({ content: ROWS, number: 0, size: 20, totalElements: 3, totalPages: 1 });
  apiPost.mockReset().mockResolvedValue({ idDeed: 5, status: "Firmada" });
  apiPut.mockReset().mockResolvedValue(undefined);
  apiDelete.mockReset().mockResolvedValue(undefined);
  Object.values(toast).forEach((fn) => fn.mockReset());
});

const dateInput = () => document.querySelector<HTMLInputElement>('input[type="date"]')!;
const numberInput = () => document.querySelectorAll<HTMLInputElement>('[role="dialog"] input[type="number"]')[0];

async function openCreate() {
  fireEvent.click(screen.getByTestId("btn-new-deed"));
  await screen.findByTestId("select-folio-deed");
}

describe("save", () => {
  it("needs a number, a date and a folio to create", async () => {
    await renderPage();
    await openCreate();

    fireEvent.click(screen.getByRole("button", { name: "common.create" }));

    expect(await screen.findAllByText("common.fieldRequired")).toHaveLength(3);
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("creates with the chosen folio and offers only new folios", async () => {
    await renderPage();
    await openCreate();
    fireEvent.change(numberInput(), { target: { value: "800" } });
    fireEvent.change(dateInput(), { target: { value: "2026-05-01" } });
    fireEvent.change(screen.getByTestId("input-notes-deed"), { target: { value: "nota" } });
    fireEvent.click(screen.getByTestId("select-folio-deed"));
    expect(await screen.findAllByRole("option")).toHaveLength(2);
    fireEvent.click(screen.getByRole("option", { name: /9/ }));

    fireEvent.click(screen.getByRole("button", { name: "common.create" }));

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/deeds", expect.objectContaining({ number: 800, notes: "nota" })));
    expect(toast.success).toHaveBeenCalledWith("escrituras.created");
  });

  it("reports the server's reason when a save fails and keeps the form", async () => {
    apiPost.mockRejectedValue(new ApiError(409, "/deeds", "Número repetido"));
    await renderPage();
    await openCreate();
    fireEvent.change(numberInput(), { target: { value: "800" } });
    fireEvent.change(dateInput(), { target: { value: "2026-05-01" } });
    fireEvent.click(screen.getByTestId("select-folio-deed"));
    fireEvent.click(await screen.findByRole("option", { name: /9/ }));

    fireEvent.click(screen.getByRole("button", { name: "common.create" }));

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
    expect(toast.success).not.toHaveBeenCalled();
    expect(screen.getByTestId("select-folio-deed")).toBeInTheDocument();
  });

  it("edits without asking for a folio and saves with PUT", async () => {
    await renderPage();
    fireEvent.click(screen.getAllByRole("button", { name: "common.edit" })[0]);
    await screen.findByTestId("select-folio-deed");

    fireEvent.change(numberInput(), { target: { value: "750" } });
    fireEvent.click(screen.getByRole("button", { name: "common.update" }));

    await waitFor(() => expect(apiPut).toHaveBeenCalledWith("/deeds/1", expect.objectContaining({ number: 750 })));
    expect(toast.success).toHaveBeenCalledWith("escrituras.updated");
  });
});

describe("delete and sign", () => {
  it("deletes once confirmed", async () => {
    await renderPage();
    fireEvent.click(screen.getAllByRole("button", { name: "common.delete" })[0]);
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiDelete).toHaveBeenCalledWith("/deeds/1"));
    expect(toast.success).toHaveBeenCalledWith("escrituras.deleted");
  });

  it("reports a deed that is in use", async () => {
    apiDelete.mockRejectedValue(new ApiError(409, "/deeds/1", "in use"));
    await renderPage();
    fireEvent.click(screen.getAllByRole("button", { name: "common.delete" })[0]);
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(toast.warning).toHaveBeenCalled());
    expect(toast.success).not.toHaveBeenCalled();
  });

  it("offers signing only for an unsigned deed", async () => {
    await renderPage();
    expect(screen.getByTestId("btn-sign-deed-1")).toBeInTheDocument();
    expect(screen.queryByTestId("btn-sign-deed-2")).toBeNull();
    expect(screen.queryByTestId("btn-sign-deed-3")).toBeNull();
  });

  it("signs after the confirmation and not before", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-sign-deed-1"));
    expect(apiPost).not.toHaveBeenCalled();
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/deeds/1/sign", {}));
    expect(toast.success).toHaveBeenCalledWith("escrituras.firmada");
  });

  it("explains why a deed could not be signed", async () => {
    apiPost.mockRejectedValue(new ApiError(409, "/deeds/1/sign", "Falta un folio"));
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-sign-deed-1"));
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
    expect(toast.success).not.toHaveBeenCalled();
  });
});

describe("list", () => {
  it("shows the linked folio, and a dash for a deed without one or without a number", async () => {
    await renderPage();
    expect(screen.getByText("Folio #10")).toBeInTheDocument();
    expect(screen.getAllByText("—").length).toBeGreaterThan(0);
  });

  it("looks a number up on the server and hides the pagination", async () => {
    await renderPage();
    fireEvent.change(screen.getByTestId("input-search-deed"), { target: { value: "702" } });

    await waitFor(() => expect(apiGet).toHaveBeenCalledWith("/deeds/search?number=702"));
    await waitFor(() => expect(screen.queryByTestId("btn-sign-deed-1")).toBeNull());
    expect(screen.getByText("702")).toBeInTheDocument();
  });

  it("shows no deeds for a number that matches none", async () => {
    await renderPage();
    fireEvent.change(screen.getByTestId("input-search-deed"), { target: { value: "5" } });

    await waitFor(() => expect(screen.queryByTestId("btn-sign-deed-1")).toBeNull());
    expect(await screen.findByTestId("empty-state")).toHaveTextContent("escrituras.noData");
  });

  it("shows the load error and retries the page", async () => {
    apiGetPage.mockRejectedValueOnce(new ApiError(500, "/deeds", "down")).mockResolvedValue({ content: ROWS, number: 0, size: 20, totalElements: 3, totalPages: 1 });
    render(<DeedsPage />, { wrapper });

    expect(await screen.findByText("escrituras.loadError")).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: /retry|reintentar/i }));

    expect(await screen.findByTestId("btn-sign-deed-1")).toBeInTheDocument();
  });

  it("shows the load error of a number search and retries it", async () => {
    let fail = true;
    apiGet.mockImplementation((path: string) =>
      path === "/deeds/search?number=702" && fail ? Promise.reject(new ApiError(500, path, "")) : route(path),
    );
    await renderPage();
    fireEvent.change(screen.getByTestId("input-search-deed"), { target: { value: "702" } });

    expect(await screen.findByText("escrituras.loadError")).toBeInTheDocument();
    fail = false;
    fireEvent.click(screen.getByRole("button", { name: /retry|reintentar/i }));

    expect(await screen.findByText("702")).toBeInTheDocument();
  });
});
