/**
 * Role administration (CU78, #1246): a role needs a name, carries the modules it grants, and a
 * server rejection of a field is shown on that field.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiGet = vi.fn();
const apiPost = vi.fn();
const apiPut = vi.fn();
const apiDelete = vi.fn();
const toast = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn(), warning: vi.fn() }));

vi.mock("@/lib/api-client", async (orig) => ({
  ...(await orig<typeof import("@/lib/api-client")>()),
  apiGet: (path: string) => apiGet(path),
  apiPost: (path: string, body: unknown) => apiPost(path, body),
  apiPut: (path: string, body: unknown) => apiPut(path, body),
  apiDelete: (path: string) => apiDelete(path),
}));
vi.mock("sonner", () => ({ toast }));
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
  usePathname: () => "/dashboard/administration/roles",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));

import { ApiError } from "@/lib/api-client";
import RolesPage from "@/app/dashboard/administration/roles/page";

const ROLES = [
  { idRole: 1, name: "Operador", description: "carga", active: true, modulos: ["personas", "tramites"] },
  { idRole: 2, name: "Lector", description: null, active: false, modulos: [] },
];

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function renderPage() {
  render(<RolesPage />, { wrapper });
  await screen.findByText("Operador");
}

function saveButton() {
  return within(screen.getByRole("dialog")).getByRole("button", { name: /common\.(create|update)/ });
}

beforeEach(() => {
  apiGet.mockReset().mockResolvedValue(ROLES);
  apiPost.mockReset().mockResolvedValue({});
  apiPut.mockReset().mockResolvedValue({});
  apiDelete.mockReset().mockResolvedValue(undefined);
  Object.values(toast).forEach((fn) => fn.mockReset());
});

describe("roles", () => {
  it("lists the modules each role grants and says when it grants none", async () => {
    await renderPage();

    const operator = screen.getAllByRole("row").find((r) => within(r).queryByText("Operador"))!;
    expect(within(operator).getByText("personas")).toBeInTheDocument();
    expect(within(operator).getByText("tramites")).toBeInTheDocument();
    const reader = screen.getAllByRole("row").find((r) => within(r).queryByText("Lector"))!;
    expect(within(reader).getByText("administracion.roles.noPermissions")).toBeInTheDocument();
  });

  it("needs a name", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-role"));
    await screen.findByRole("dialog");

    fireEvent.click(saveButton());

    expect(toast.error).toHaveBeenCalledWith("administracion.roles.nameRequired");
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("creates a role with the modules ticked", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-role"));
    fireEvent.change(await screen.findByTestId("input-name-role"), { target: { value: "Auditor" } });
    fireEvent.click(screen.getByTestId("check-modulo-auditoria"));
    fireEvent.click(screen.getByTestId("check-modulo-personas"));
    fireEvent.click(screen.getByTestId("check-modulo-personas"));

    fireEvent.click(saveButton());

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith(
        "/roles",
        expect.objectContaining({ name: "Auditor", modulos: ["auditoria"], active: true }),
      ),
    );
  });

  it("edits a role keeping its granted modules", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-edit-role-1"));

    expect(await screen.findByTestId("input-name-role")).toHaveValue("Operador");
    expect(screen.getByTestId("check-modulo-personas")).toBeChecked();
    expect(screen.getByTestId("check-modulo-usuarios")).not.toBeChecked();
    fireEvent.click(screen.getByTestId("check-modulo-usuarios"));
    fireEvent.click(saveButton());

    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith(
        "/roles/1",
        expect.objectContaining({ modulos: ["personas", "tramites", "usuarios"] }),
      ),
    );
  });

  it("shows a server rejection on the name field", async () => {
    apiPost.mockRejectedValue(new ApiError(400, "/roles", '{"message":"name: El nombre ya existe"}'));
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-role"));
    fireEvent.change(await screen.findByTestId("input-name-role"), { target: { value: "Operador" } });

    fireEvent.click(saveButton());

    await waitFor(() => expect(screen.getByTestId("input-name-role")).toHaveAttribute("aria-invalid", "true"));
    expect(screen.getAllByText("El nombre ya existe").length).toBeGreaterThan(0);
  });

  it("deletes a role after confirmation", async () => {
    await renderPage();
    const reader = screen.getAllByRole("row").find((r) => within(r).queryByText("Lector"))!;

    fireEvent.click(within(reader).getByRole("button", { name: "common.delete" }));
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiDelete).toHaveBeenCalledWith("/roles/2"));
  });
});
