/**
 * Workflow definitions (#1577, #1246): a workflow needs a name, cannot be saved as active unless the
 * backend validates it, and is edited, deleted and searched from the list.
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
  usePathname: () => "/dashboard/administration/workflows",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));

import WorkflowsPage from "@/app/dashboard/administration/workflows/page";

const WORKFLOWS = [
  { id: 1, name: "Compraventa", description: "flujo", active: false },
  { id: 2, name: "Poder", description: null, active: true },
];

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function renderPage() {
  render(<WorkflowsPage />, { wrapper });
  await screen.findByText("Compraventa");
}

beforeEach(() => {
  apiGet.mockReset().mockImplementation((path: string) =>
    path === "/workflow-definition" ? Promise.resolve(WORKFLOWS) : Promise.resolve([]),
  );
  apiPost.mockReset().mockResolvedValue({});
  apiPut.mockReset().mockResolvedValue(undefined);
  apiDelete.mockReset().mockResolvedValue(undefined);
  Object.values(toast).forEach((fn) => fn.mockReset());
});

describe("workflow list", () => {
  it("lists each workflow with its state and filters by name", async () => {
    await renderPage();

    expect(screen.getByText("Poder")).toBeInTheDocument();
    fireEvent.change(screen.getByTestId("search-workflows"), { target: { value: "comp" } });

    expect(screen.getByText("Compraventa")).toBeInTheDocument();
    expect(screen.queryByText("Poder")).toBeNull();
  });

  it("links each row to its graph editor", async () => {
    await renderPage();

    expect(screen.getByTestId("btn-editor-1")).toHaveAttribute("href", "/dashboard/administration/workflows/1");
  });

  it("deletes a workflow after confirmation", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-delete-wf-1"));

    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiDelete).toHaveBeenCalledWith("/workflow-definition/1"));
    expect(toast.success).toHaveBeenCalled();
  });

  it("reports why a workflow with nodes cannot be deleted", async () => {
    apiDelete.mockRejectedValue(new Error("has nodes"));
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-delete-wf-1"));

    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
  });
});

describe("workflow form", () => {
  it("needs a name", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-workflow"));

    fireEvent.click(await screen.findByTestId("btn-save-workflow"));

    expect(toast.error).toHaveBeenCalledWith("administracion.workflows.nameRequired");
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("creates an inactive workflow", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-workflow"));

    fireEvent.change(await screen.findByTestId("input-name-workflow"), { target: { value: "Donación" } });
    fireEvent.click(screen.getByTestId("btn-save-workflow"));

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith("/workflow-definition", expect.objectContaining({ name: "Donación", active: false })),
    );
  });

  it("refuses to create a workflow as active because it has no nodes yet", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-workflow"));
    fireEvent.change(await screen.findByTestId("input-name-workflow"), { target: { value: "Donación" } });
    fireEvent.click(screen.getByTestId("checkbox-active-workflow"));

    fireEvent.click(screen.getByTestId("btn-save-workflow"));

    const errors = await screen.findByTestId("workflow-activate-errors");
    expect(within(errors).getByText("administracion.workflows.activateNewHasNoNodes")).toBeInTheDocument();
    expect(apiPost).not.toHaveBeenCalledWith("/workflow-definition", expect.anything());
  });

  it("activates an edited workflow only when the backend validates it", async () => {
    apiPost.mockResolvedValue({ valid: true, errors: [] });
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-edit-wf-1"));
    fireEvent.click(await screen.findByTestId("checkbox-active-workflow"));

    fireEvent.click(screen.getByTestId("btn-save-workflow"));

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/workflow-definition/1/validate", {}));
    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith("/workflow-definition/1", expect.objectContaining({ id: 1, active: true })),
    );
  });

  it("keeps an invalid workflow inactive and explains the problems in the user's language", async () => {
    apiPost.mockResolvedValue({ valid: false, errors: ["sin nodo final"] });
    apiGet.mockImplementation((path: string) => {
      if (path === "/workflow-definition") return Promise.resolve(WORKFLOWS);
      if (path === "/workflow-node/by-workflow/1") {
        return Promise.resolve([{ id: 10, type: "START", statusManagementName: "Inicio" }]);
      }
      return Promise.resolve([]);
    });
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-edit-wf-1"));
    fireEvent.click(await screen.findByTestId("checkbox-active-workflow"));

    fireEvent.click(screen.getByTestId("btn-save-workflow"));

    const errors = await screen.findByTestId("workflow-activate-errors");
    expect(errors).toHaveTextContent("workflowViewer.problemList.noFinal");
    expect(within(errors).getByRole("link")).toHaveAttribute("href", "/dashboard/administration/workflows/1");
    expect(apiPut).not.toHaveBeenCalled();
    expect(toast.error).toHaveBeenCalledWith("administracion.workflows.activateInvalid");
  });
});
