/**
 * Dashboard hero (#1347, RF-23/CU14): it shows the NEWEST management that has a
 * workflow, not the first one in backend order (management 1001 from the seed),
 * says which case it is, and has an empty state when no recent case has one.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { readFileSync } from "node:fs";
import { join } from "node:path";
import type { ReactNode } from "react";

const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string, values?: Record<string, unknown>) =>
    values ? `${ns}.${key}:${JSON.stringify(values)}` : `${ns}.${key}`,
}));
vi.mock("next/link", () => ({
  default: ({ href, children, ...rest }: { href: string; children: ReactNode }) => <a href={href} {...rest}>{children}</a>,
}));
vi.mock("@/components/motion/WorkflowTracker", () => ({
  default: ({ trace }: { trace: { managementId: number } }) => <div data-testid="workflow-tracker">tracker {trace.managementId}</div>,
}));

import { WorkflowHero } from "@/components/workflow/WorkflowHero";
import { findLatestTracedGestion, LATEST_TRACE_CANDIDATES } from "@/hooks/useGestionWorkflow";

function json(body: unknown, status = 200) {
  const text = JSON.stringify(body);
  return Promise.resolve({ ok: status < 400, status, text: () => Promise.resolve(text) } as Response);
}

const trace = (id: number, number: number, encabezado?: string) => ({
  managementId: id, number, encabezado, statusActual: "Iniciada", nodes: [], transitions: [], history: [], nodeStatuses: {},
});

/** Managements newest first; `traced` ids have a workflow, the rest answer 400. */
function routes(ids: number[], traced: number[]) {
  return (u: string) => {
    const url = String(u);
    if (/\/gestiones\?page=0&size=\d+&sort=idManagement%2Cdesc$/.test(url)) {
      return json({ content: ids.map((id) => ({ idManagement: id, number: 1000 + id })), number: 0, size: ids.length, totalElements: ids.length, totalPages: 1 });
    }
    const m = url.match(/\/gestiones\/(\d+)\/workflow-trace$/);
    if (m) {
      const id = Number(m[1]);
      return traced.includes(id) ? json(trace(id, 1000 + id, `Caso ${id}`)) : json({ error: "no workflow" }, 400);
    }
    return json({}, 404);
  };
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

const urls = () => mockFetch.mock.calls.map((c) => String(c[0]));

describe("findLatestTracedGestion (#1347)", () => {
  beforeEach(() => mockFetch.mockReset());

  it("asks for the newest managements first and returns the newest one with a workflow", async () => {
    mockFetch.mockImplementation(routes([30, 29, 28, 27], [29, 27]));
    const found = await findLatestTracedGestion();
    expect(found?.trace.managementId).toBe(29);
    expect(urls()[0]).toMatch(new RegExp(`/gestiones\\?page=0&size=${LATEST_TRACE_CANDIDATES}&sort=idManagement%2Cdesc$`));
  });

  it("skips a long run of managements without a workflow", async () => {
    const ids = Array.from({ length: 15 }, (_, i) => 100 - i);
    mockFetch.mockImplementation(routes(ids, [87]));
    expect((await findLatestTracedGestion())?.trace.managementId).toBe(87);
  });

  it("returns null when no recent management has a workflow", async () => {
    mockFetch.mockImplementation(routes([3, 2, 1], []));
    expect(await findLatestTracedGestion()).toBeNull();
  });
});

describe("WorkflowHero (#1347)", () => {
  beforeEach(() => mockFetch.mockReset());

  it("shows the newest traced case and says which one it is", async () => {
    mockFetch.mockImplementation(routes([30, 29], [29]));
    render(<WorkflowHero />, { wrapper });
    expect(await screen.findByTestId("workflow-tracker")).toHaveTextContent("tracker 29");
    expect(screen.getByTestId("workflow-subtitle")).toHaveTextContent("#1029 — Caso 29");
    expect(screen.getByTestId("workflow-hero")).toHaveAttribute("data-management-id", "29");
  });

  it("shows an empty state with a link to the managements list when no case has a workflow", async () => {
    mockFetch.mockImplementation(routes([2, 1], []));
    render(<WorkflowHero />, { wrapper });
    const empty = await screen.findByTestId("workflow-empty");
    expect(empty).toHaveTextContent("dashboard.workflow.noRecentWorkflow");
    expect(screen.getByRole("link", { name: "dashboard.workflow.openGestiones" })).toHaveAttribute("href", "/dashboard/gestiones");
    expect(screen.queryByTestId("workflow-tracker")).not.toBeInTheDocument();
  });
});

describe("dashboard page (#1347)", () => {
  const src = readFileSync(join(__dirname, "..", "..", "app", "dashboard", "page.tsx"), "utf8");
  it("has no dead 'view all' button", () => {
    expect(src).not.toMatch(/td\("viewAll"\)/);
  });
  it("has no English fallback literal", () => {
    expect(src).not.toMatch(/Management #/);
  });
});
