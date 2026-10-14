/**
 * Every dashboard list page renders its shell, its empty state and its first requests without
 * throwing (issue #1667, CU76). Cheap smoke coverage for pages that had none; behaviour is covered
 * by the Playwright suite.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ComponentType, ReactNode } from "react";

const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);

vi.mock("sonner", () => ({
  toast: { error: vi.fn(), success: vi.fn(), warning: vi.fn() },
}));
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
  usePathname: () => "/dashboard",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));

const PAGES: Record<string, () => Promise<{ default: ComponentType }>> = {
  folios: () => import("@/app/dashboard/administration/folios/page"),
  templates: () => import("@/app/dashboard/administration/templates/page"),
  procedures: () => import("@/app/dashboard/administration/procedures/page"),
  workflows: () => import("@/app/dashboard/administration/workflows/page"),
  users: () => import("@/app/dashboard/administration/users/page"),
  "management-statuses": () =>
    import("@/app/dashboard/administration/management-statuses/page"),
  documents: () => import("@/app/dashboard/administration/documents/page"),
  roles: () => import("@/app/dashboard/administration/roles/page"),
  concepts: () => import("@/app/dashboard/administration/concepts/page"),
  "registration-drafts": () =>
    import("@/app/dashboard/registration-drafts/page"),
  items: () => import("@/app/dashboard/items/page"),
  protocol: () => import("@/app/dashboard/protocol/page"),
  "testimony-movements": () =>
    import("@/app/dashboard/testimony-movements/page"),
  "protocol-notebooks": () => import("@/app/dashboard/protocol/notebooks/page"),
  documents_list: () => import("@/app/dashboard/documents/page"),
  properties: () => import("@/app/dashboard/properties/page"),
  copies: () => import("@/app/dashboard/copies/page"),
  audit: () => import("@/app/dashboard/audit/page"),
  reports: () => import("@/app/dashboard/reports/page"),
  "required-documents": () => import("@/app/dashboard/required-documents/page"),
  "upcoming-expirations": () =>
    import("@/app/dashboard/upcoming-expirations/page"),
  "external-entity-documents": () =>
    import("@/app/dashboard/external-entity-documents/page"),
  "document-reentry": () => import("@/app/dashboard/document-reentry/page"),
  people: () => import("@/app/dashboard/people/page"),
  deeds: () => import("@/app/dashboard/deeds/page"),
  budgets: () => import("@/app/dashboard/budgets/page"),
  payments: () => import("@/app/dashboard/payments/page"),
  testimonies: () => import("@/app/dashboard/testimonies/page"),
  substitutions: () => import("@/app/dashboard/substitutions/page"),
  managements: () => import("@/app/dashboard/managements/page"),
  dashboard: () => import("@/app/dashboard/page"),
};

function emptyResponse(url: string) {
  const body = /[?&](page|size)=/.test(url)
    ? { content: [], totalElements: 0, totalPages: 0, number: 0, size: 20 }
    : [];
  return Promise.resolve({
    ok: true,
    status: 200,
    statusText: "OK",
    headers: new Headers({ "content-type": "application/json" }),
    text: () => Promise.resolve(JSON.stringify(body)),
    json: () => Promise.resolve(body),
  } as Response);
}

const ROW = {
  idAuditRecord: 1,
  idBudget: 1,
  idConcept: 1,
  idCopy: 1,
  idDeed: 1,
  idDocumentType: 1,
  idFolder: 1,
  idFolio: 1,
  idFolioType: 1,
  idHistory: 1,
  idIdentificationType: 1,
  idItem: 1,
  idItems: 1,
  idManagement: 1,
  idManagementStatus: 1,
  idMinuta: 1,
  idNotary: 1,
  idNotebook: 1,
  idPayment: 1,
  idPerson: 1,
  idProcedure: 1,
  idProcedureType: 1,
  idProperty: 1,
  idRegistrationDraft: 1,
  idRole: 1,
  idSubmittedDocument: 1,
  idSubstitution: 1,
  idTestimony: 1,
  idTestimonyMovement: 1,
  idTypeDocument: 1,
  idTypeProcedure: 1,
  idUser: 1,
  id: 1,
  name: "Fila",
  isActive: true,
  active: true,
  isArchived: false,
  number: 1,
  amount: 100,
  nodes: [],
  edges: [],
  steps: [],
  date: "2026-01-01",
  createdAt: "2026-01-01T00:00:00Z",
};

function populatedResponse(url: string) {
  const rows = [ROW];
  let body: unknown = rows;
  if (url.includes("/workflow-trace")) {
    body = { ...ROW, transitions: [] };
  } else if (/[?&](page|size)=/.test(url)) {
    body = {
      content: rows,
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 20,
    };
  }
  return Promise.resolve({
    ok: true,
    status: 200,
    statusText: "OK",
    headers: new Headers({ "content-type": "application/json" }),
    text: () => Promise.resolve(JSON.stringify(body)),
    json: () => Promise.resolve(body),
  } as Response);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

describe("dashboard list pages render (#1667)", () => {
  beforeEach(() => {
    mockFetch.mockReset();
    mockFetch.mockImplementation((input: RequestInfo | URL) =>
      emptyResponse(String(input)),
    );
  });

  it.each(Object.keys(PAGES))(
    "%s renders its shell and settles on an empty list",
    async (name) => {
      const { default: Page } = await PAGES[name]();
      const { container } = render(<Page />, { wrapper });

      await waitFor(() =>
        expect(
          container.querySelector(
            "h1, h2, [role='heading'], table, [data-testid]",
          ),
        ).not.toBeNull(),
      );
      expect(screen.queryByText(/Application error/i)).toBeNull();
    },
  );

  it.each(Object.keys(PAGES))(
    "%s renders a populated list without an application error",
    async (name) => {
      mockFetch.mockImplementation((input: RequestInfo | URL) =>
        populatedResponse(String(input)),
      );
      const { default: Page } = await PAGES[name]();
      const { container } = render(<Page />, { wrapper });

      await waitFor(() =>
        expect(
          container.querySelector(
            "h1, h2, [role='heading'], table, [data-testid]",
          ),
        ).not.toBeNull(),
      );
      await screen
        .findAllByText(/Fila/, {}, { timeout: 1500 })
        .catch(() => undefined);
      expect(screen.queryByText(/Application error/i)).toBeNull();
    },
  );
});
