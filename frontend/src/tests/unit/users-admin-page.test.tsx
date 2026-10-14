/**
 * User administration (CU20, CU21, #1246): a user needs a name, keeps the role it already has when
 * edited, and is deleted after confirmation.
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
  usePathname: () => "/dashboard/administration/users",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));

import UsersPage from "@/app/dashboard/administration/users/page";

const USERS = [
  { idUser: 11, name: "marta", type: "ADMIN", active: true, role: { idRole: 1, name: "Operador" } },
  { idUser: 12, name: "pablo", type: "EMPLEADO", active: false, role: null },
];
const ROLES = [{ idRole: 1, name: "Operador", active: true }];

function route(path: string) {
  if (path === "/users") return Promise.resolve(USERS);
  if (path === "/roles") return Promise.resolve(ROLES);
  return Promise.resolve([]);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function renderPage() {
  render(<UsersPage />, { wrapper });
  await screen.findByText("marta");
}

function row(name: string) {
  return screen.getAllByRole("row").find((r) => within(r).queryByText(name))!;
}

function saveButton() {
  return within(screen.getByRole("dialog")).getByRole("button", { name: /common\.(create|update)/ });
}

beforeEach(() => {
  apiGet.mockReset().mockImplementation(route);
  apiPost.mockReset().mockResolvedValue({ idUser: 99 });
  apiPut.mockReset().mockResolvedValue({});
  apiDelete.mockReset().mockResolvedValue(undefined);
  Object.values(toast).forEach((fn) => fn.mockReset());
});

describe("users", () => {
  it("lists each user with its type and role", async () => {
    await renderPage();

    expect(within(row("marta")).getByText("ADMIN")).toBeInTheDocument();
    expect(within(row("marta")).getByText("Operador")).toBeInTheDocument();
    expect(within(row("pablo")).getByText("EMPLEADO")).toBeInTheDocument();
  });

  it("needs a name", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-user"));
    await screen.findByRole("dialog");

    fireEvent.click(saveButton());

    expect(toast.error).toHaveBeenCalledWith("administracion.usuarios.fields.nombre common.required");
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("creates a user without assigning any role", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-user"));
    fireEvent.change(await screen.findByTestId("input-name-user"), { target: { value: "ana" } });

    fireEvent.click(saveButton());

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/users", expect.objectContaining({ name: "ana" })));
    expect(apiPut).not.toHaveBeenCalledWith(expect.stringContaining("/roles/"), expect.anything());
    expect(toast.success).toHaveBeenCalledWith("administracion.usuarios.created");
  });

  it("edits a user with a role and assigns that role again", async () => {
    await renderPage();
    fireEvent.click(within(row("marta")).getByRole("button", { name: "common.edit" }));

    const name = await screen.findByTestId("input-name-user");
    expect(name).toHaveValue("marta");
    fireEvent.change(name, { target: { value: "marta b" } });
    fireEvent.click(saveButton());

    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith("/users/11", expect.objectContaining({ name: "marta b", password: "" })),
    );
    await waitFor(() => expect(apiPut).toHaveBeenCalledWith("/roles/1/users/11", {}));
  });

  it("edits a user without a role and assigns none", async () => {
    await renderPage();
    fireEvent.click(within(row("pablo")).getByRole("button", { name: "common.edit" }));

    fireEvent.click(await screen.findByRole("dialog").then(() => saveButton()));

    await waitFor(() => expect(apiPut).toHaveBeenCalledWith("/users/12", expect.anything()));
    expect(apiPut).not.toHaveBeenCalledWith(expect.stringContaining("/roles/"), expect.anything());
    expect(apiDelete).not.toHaveBeenCalled();
  });

  it("deletes a user after confirmation", async () => {
    await renderPage();

    fireEvent.click(within(row("pablo")).getByRole("button", { name: "common.delete" }));
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiDelete).toHaveBeenCalledWith("/users/12"));
  });
});
