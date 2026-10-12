/**
 * #1574: the editor canvas reflects the query data when it arrives after the first render
 * (useNodesState used to keep only its initial, empty value).
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

let resolveNodes: (v: unknown) => void = () => {};
const apiGet = vi.fn((path: string) => {
  if (path.startsWith("/workflow-node/by-workflow")) return new Promise((r) => { resolveNodes = r; });
  if (path.startsWith("/workflow-transition/by-workflow")) return Promise.resolve([{ id: 9, originNodeId: 1, destinationNodeId: 2 }]);
  if (path === "/workflow-definition") return Promise.resolve([{ id: 5, name: "WF" }]);
  return Promise.resolve([]);
});
vi.mock("@/lib/api-client", async (orig) => ({
  ...(await orig<typeof import("@/lib/api-client")>()),
  apiGet: (p: string) => apiGet(p),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));
vi.mock("next/navigation", () => ({ useParams: () => ({ id: "5" }), useRouter: () => ({ back: vi.fn() }) }));

import WorkflowEditorPage from "@/app/dashboard/administration/workflows/[id]/page";

beforeEach(() => {
  class RO { observe() {} unobserve() {} disconnect() {} }
  (globalThis as unknown as { ResizeObserver: typeof RO }).ResizeObserver = RO;
});

function wrapper({ children }: { children: ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={qc}>{children}</QueryClientProvider>;
}

describe("WorkflowEditorPage (#1574)", () => {
  it("renders nodes that arrive after the first render", async () => {
    render(<WorkflowEditorPage />, { wrapper });
    resolveNodes([
      { id: 1, positionX: null, positionY: null, type: "INITIAL", statusManagementName: "Iniciada" },
      { id: 2, positionX: null, positionY: null, type: "FINAL", statusManagementName: "Archivada" },
    ]);
    await waitFor(() => expect(screen.getByText(/Iniciada/)).toBeInTheDocument());
    expect(screen.getByText(/Archivada/)).toBeInTheDocument();
  });
});
