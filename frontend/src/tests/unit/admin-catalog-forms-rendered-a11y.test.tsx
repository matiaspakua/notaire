/**
 * Administration catalog create dialogs — rendered form-pattern contract
 * (#2088 / CU76 / RNF-07). Same checklist as people-form (#2087): labels,
 * required, errors, loading busy, CheckboxField touch, primary size token.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, within, fireEvent } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";
import { readFileSync, readdirSync, statSync } from "node:fs";
import { join, resolve } from "node:path";

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
  usePathname: () => "/dashboard/administration/concepts",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));

import ConceptsPage from "@/app/dashboard/administration/concepts/page";
import DocumentTypesPage from "@/app/dashboard/administration/documents/page";
import RolesPage from "@/app/dashboard/administration/roles/page";

const ADMIN_ROOT = resolve(__dirname, "../../app/dashboard/administration");

const CATALOG_PAGES = [
  "concepts/page.tsx",
  "documents/page.tsx",
  "folios/page.tsx",
  "identification-types/page.tsx",
  "management-statuses/page.tsx",
  "procedures/page.tsx",
  "procedures/RequiredDocumentsDialog.tsx",
  "roles/page.tsx",
  "templates/page.tsx",
  "users/page.tsx",
  "workflows/page.tsx",
  "workflows/[id]/page.tsx",
] as const;

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

function route(path: string) {
  if (path.startsWith("/concepts")) return Promise.resolve([{ idConcept: 1, name: "Sellado", value: 10 }]);
  if (path.startsWith("/document-types")) {
    return Promise.resolve([
      { idDocumentType: 3, name: "DNI", enabled: true, returned: false, expires: false, dueDays: null },
    ]);
  }
  if (path.startsWith("/roles")) {
    return Promise.resolve([
      { idRole: 1, name: "Operador", description: "carga", active: true, modulos: ["personas"] },
    ]);
  }
  if (path.endsWith("/in-use")) return Promise.resolve({ inUse: false });
  return Promise.resolve([]);
}

beforeEach(() => {
  apiGet.mockReset().mockImplementation(route);
  apiPost.mockReset().mockImplementation(
    () =>
      new Promise(() => {
        /* hang so isPending stays true */
      }),
  );
  apiPut.mockReset().mockResolvedValue({});
  apiDelete.mockReset().mockResolvedValue(undefined);
  Object.values(toast).forEach((fn) => fn.mockReset());
});

describe("Admin catalog source contract (#2088)", () => {
  it("lists every in-scope surface under administration/", () => {
    for (const rel of CATALOG_PAGES) {
      expect(statSync(join(ADMIN_ROOT, rel)).isFile()).toBe(true);
    }
    // Guard against silently dropping a catalog page from the audit set.
    const found = readdirSync(ADMIN_ROOT, { withFileTypes: true })
      .filter((d) => d.isDirectory())
      .map((d) => d.name);
    for (const name of [
      "concepts",
      "documents",
      "folios",
      "identification-types",
      "management-statuses",
      "procedures",
      "roles",
      "templates",
      "users",
      "workflows",
    ]) {
      expect(found).toContain(name);
    }
  });

  it("primary FormActions saves expose aria-busy wiring and avoid size=sm", () => {
    for (const rel of CATALOG_PAGES) {
      const src = readFileSync(join(ADMIN_ROOT, rel), "utf8");
      if (!src.includes("FormActions")) continue;
      // Dialog primary save must not force size="sm" (32px); default button is h-11.
      const actionsBlocks = src.split("FormActions");
      for (let i = 1; i < actionsBlocks.length; i++) {
        const block = actionsBlocks[i].slice(0, 500);
        // Ghost/secondary cancel may be size-less; primary Button inside FormActions
        // must not be size="sm".
        if (/onClick=\{handleSave|onClick=\{handleAdd|btn-save|btn-add/.test(block) || /disabled=\{/.test(block)) {
          expect(block, rel).not.toMatch(/size="sm"/);
        }
      }
      expect(src, `${rel} must wire aria-busy on pending primary save`).toMatch(/aria-busy=/);
    }
  });
});

describe("ConceptsPage form rendered a11y (#2088)", () => {
  it("labels required fields with a visible * and aria-required", async () => {
    render(<ConceptsPage />, { wrapper });
    fireEvent.click(await screen.findByRole("button", { name: /newConcepto/ }));
    const dialog = await screen.findByRole("dialog");
    const name = within(dialog).getByTestId("input-name-concept");
    expect(name).toHaveAttribute("aria-required", "true");
    expect(within(dialog).getByText(/fields\.nombre/i).textContent).toMatch(/\*/);
  });

  it("shows linked alerts and aria-invalid when required fields are empty", async () => {
    render(<ConceptsPage />, { wrapper });
    fireEvent.click(await screen.findByRole("button", { name: /newConcepto/ }));
    const dialog = await screen.findByRole("dialog");
    fireEvent.click(within(dialog).getByTestId("btn-save-concept"));
    await waitFor(() => expect(within(dialog).getAllByRole("alert").length).toBeGreaterThan(0));
    expect(within(dialog).getByTestId("input-name-concept")).toHaveAttribute("aria-invalid", "true");
    expect(within(dialog).getByTestId("input-value-concept")).toHaveAttribute("aria-invalid", "true");
  });

  it("disables the primary save and marks it busy while create is pending", async () => {
    render(<ConceptsPage />, { wrapper });
    fireEvent.click(await screen.findByRole("button", { name: /newConcepto/ }));
    const dialog = await screen.findByRole("dialog");
    fireEvent.change(within(dialog).getByTestId("input-name-concept"), { target: { value: "Timbre" } });
    fireEvent.change(within(dialog).getByTestId("input-value-concept"), { target: { value: "10" } });
    const save = within(dialog).getByTestId("btn-save-concept");
    fireEvent.click(save);
    await waitFor(() => expect(save).toBeDisabled());
    expect(save).toHaveAttribute("aria-busy", "true");
  });
});

describe("DocumentTypesPage form rendered a11y (#2088)", () => {
  it("shows linked alerts and aria-invalid when name is empty", async () => {
    render(<DocumentTypesPage />, { wrapper });
    await screen.findByText("DNI");
    fireEvent.click(screen.getByTestId("btn-new-document-type"));
    const dialog = await screen.findByRole("dialog");
    fireEvent.click(within(dialog).getByTestId("btn-save-document"));
    await waitFor(() => expect(within(dialog).getAllByRole("alert").length).toBeGreaterThan(0));
    expect(within(dialog).getByTestId("input-name-document")).toHaveAttribute("aria-invalid", "true");
    expect(toast.error).not.toHaveBeenCalled();
  });

  it("uses CheckboxField hit targets (≥28px declared) on the create dialog", async () => {
    render(<DocumentTypesPage />, { wrapper });
    await screen.findByText("DNI");
    fireEvent.click(screen.getByTestId("btn-new-document-type"));
    const dialog = await screen.findByRole("dialog");
    const box = within(dialog).getByTestId("checkbox-enabled-document");
    const style = box.getAttribute("style") ?? "";
    expect(style).toMatch(/width:\s*28px/);
    expect(style).toMatch(/height:\s*28px/);
  });
});

describe("RolesPage form rendered a11y (#2088)", () => {
  it("shows linked alerts when name is empty instead of toast-only", async () => {
    render(<RolesPage />, { wrapper });
    await screen.findByText("Operador");
    fireEvent.click(screen.getByTestId("btn-new-role"));
    const dialog = await screen.findByRole("dialog");
    fireEvent.click(within(dialog).getByTestId("btn-save-role"));
    await waitFor(() => expect(within(dialog).getAllByRole("alert").length).toBeGreaterThan(0));
    expect(within(dialog).getByTestId("input-name-role")).toHaveAttribute("aria-invalid", "true");
    expect(toast.error).not.toHaveBeenCalled();
  });

  it("uses CheckboxField for module permissions (shared touch floor)", async () => {
    render(<RolesPage />, { wrapper });
    await screen.findByText("Operador");
    fireEvent.click(screen.getByTestId("btn-new-role"));
    const dialog = await screen.findByRole("dialog");
    const box = within(dialog).getByTestId("check-modulo-personas");
    const style = box.getAttribute("style") ?? "";
    expect(style).toMatch(/width:\s*28px/);
    expect(style).toMatch(/height:\s*28px/);
  });

  it("stacks module checkboxes on narrow viewports", () => {
    const src = readFileSync(join(ADMIN_ROOT, "roles/page.tsx"), "utf8");
    expect(src).toMatch(/grid-cols-1\s+sm:grid-cols-2/);
  });
});
