/**
 * Workflow editor page (CU30, #1246): the edit toggle gates the editing controls, adding a status needs
 * a choice, transitions are checked before they are sent, deletes ask first, descriptions and types are
 * saved, the live validity banner and the server validation report are shown, and the list view is the
 * keyboard path to all of it.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiGet = vi.fn();
const apiPost = vi.fn();
const apiPut = vi.fn();
const apiDelete = vi.fn();
const back = vi.fn();
const toast = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() }));

vi.mock("@/lib/api-client", async (orig) => ({
  ...(await orig<typeof import("@/lib/api-client")>()),
  apiGet: (path: string) => apiGet(path),
  apiPost: (path: string, body: unknown) => apiPost(path, body),
  apiPut: (path: string, body: unknown) => apiPut(path, body),
  apiDelete: (path: string) => apiDelete(path),
}));
vi.mock("sonner", () => ({ toast }));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));
vi.mock("next/navigation", () => ({ useParams: () => ({ id: "5" }), useRouter: () => ({ back }) }));

import { ApiError } from "@/lib/api-client";
import WorkflowEditorPage from "@/app/dashboard/administration/workflows/[id]/page";

const NODES = [
  { id: 1, type: "INITIAL", statusManagementName: "Iniciada", positionX: 0, positionY: 0 },
  { id: 2, type: "INTERMEDIATE", statusManagementName: "Documentación", positionX: 300, positionY: 0 },
  { id: 3, type: "FINAL", statusManagementName: "Archivada", positionX: 600, positionY: 0 },
];
const CONNECTED = [
  { id: 9, originNodeId: 1, destinationNodeId: 2, description: "ok" },
  { id: 10, originNodeId: 2, destinationNodeId: 3, description: "" },
];

let transitions: unknown[] = CONNECTED;

function wrapper({ children }: { children: ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={qc}>{children}</QueryClientProvider>;
}

beforeEach(() => {
  class RO { observe() {} unobserve() {} disconnect() {} }
  (globalThis as unknown as { ResizeObserver: typeof RO }).ResizeObserver = RO;
  transitions = CONNECTED;
  apiGet.mockReset().mockImplementation((path: string) => {
    if (path.startsWith("/workflow-node/by-workflow")) return Promise.resolve(NODES);
    if (path.startsWith("/workflow-transition/by-workflow")) return Promise.resolve(transitions);
    if (path === "/workflow-definition") return Promise.resolve([{ id: 5, name: "WF", description: "Descripción" }]);
    if (path === "/management-statuses") {
      return Promise.resolve([{ idManagementStatus: 7, name: "Nueva" }, { idManagementStatus: 8, name: "Cerrada" }]);
    }
    return Promise.resolve([]);
  });
  apiPost.mockReset().mockResolvedValue({});
  apiPut.mockReset().mockResolvedValue(undefined);
  apiDelete.mockReset().mockResolvedValue(undefined);
  back.mockReset();
  Object.values(toast).forEach((fn) => fn.mockReset());
});

async function renderEditor(asList = false) {
  render(<WorkflowEditorPage />, { wrapper });
  await screen.findByText("WF");
  if (asList) fireEvent.click(screen.getByTestId("wf-tab-list"));
}

async function editList() {
  await renderEditor(true);
  fireEvent.click(screen.getByTestId("btn-toggle-edit"));
  await screen.findByTestId("wf-list-add-node");
}

describe("header", () => {
  it("shows the workflow name and goes back", async () => {
    await renderEditor();
    expect(screen.getByText("Descripción")).toBeInTheDocument();
    fireEvent.click(screen.getByText("administracion.workflows.editor.back"));
    expect(back).toHaveBeenCalledTimes(1);
  });

  it("offers the editing controls only in edit mode", async () => {
    await renderEditor();
    expect(screen.queryByTestId("btn-add-node")).toBeNull();
    expect(screen.queryByTestId("btn-auto-layout")).toBeNull();

    fireEvent.click(screen.getByTestId("btn-toggle-edit"));

    expect(screen.getByTestId("btn-add-node")).toBeInTheDocument();
    expect(screen.getByTestId("btn-auto-layout")).toBeInTheDocument();
    expect(screen.getByText("administracion.workflows.editor.hint")).toBeInTheDocument();
  });
});

describe("validity banner", () => {
  it("confirms a connected workflow", async () => {
    await renderEditor();
    expect(await screen.findByTestId("workflow-validity")).toBeInTheDocument();
    expect(screen.queryByTestId("workflow-problems")).toBeNull();
  });

  it("lists the problems of a broken workflow", async () => {
    transitions = [];
    await renderEditor();
    const problems = await screen.findByTestId("workflow-problems");
    expect(within(problems).getAllByRole("listitem").length).toBeGreaterThan(0);
    expect(screen.queryByTestId("workflow-validity")).toBeNull();
  });
});

describe("server validation", () => {
  it("confirms a valid workflow", async () => {
    apiPost.mockResolvedValue({ valid: true, errors: [] });
    await renderEditor();

    fireEvent.click(screen.getByTestId("btn-validate"));

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/workflow-definition/5/validate", {}));
    expect(toast.success).toHaveBeenCalledWith("administracion.workflows.editor.valid");
    expect(screen.queryByTestId("validation-errors")).toBeNull();
  });

  it("shows the errors of an invalid workflow", async () => {
    apiPost.mockResolvedValue({ valid: false, errors: ["Sin nodo final"] });
    await renderEditor();

    fireEvent.click(screen.getByTestId("btn-validate"));

    expect(await screen.findByText("Sin nodo final")).toBeInTheDocument();
    expect(toast.error).toHaveBeenCalledWith("administracion.workflows.editor.invalid");
  });

  it("reports a failed validation call", async () => {
    apiPost.mockRejectedValue(new Error("down"));
    await renderEditor();

    fireEvent.click(screen.getByTestId("btn-validate"));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("administracion.workflows.editor.errorValidate"));
  });
});

describe("views", () => {
  it("switches between graph and list with the arrow keys", async () => {
    await renderEditor();
    expect(screen.getByTestId("wf-tab-graph")).toHaveAttribute("aria-selected", "true");

    fireEvent.keyDown(screen.getByTestId("wf-tab-graph"), { key: "ArrowRight" });
    expect(await screen.findByTestId("wf-list-view")).toBeInTheDocument();
    expect(screen.getByTestId("wf-tab-list")).toHaveAttribute("aria-selected", "true");

    fireEvent.keyDown(screen.getByTestId("wf-tab-list"), { key: "ArrowLeft" });
    await waitFor(() => expect(screen.queryByTestId("wf-list-view")).toBeNull());

    fireEvent.keyDown(screen.getByTestId("wf-tab-graph"), { key: "Enter" });
    expect(screen.getByTestId("wf-tab-graph")).toHaveAttribute("aria-selected", "true");
  });
});

describe("add status dialog", () => {
  async function openDialog() {
    await renderEditor();
    fireEvent.click(screen.getByTestId("btn-toggle-edit"));
    fireEvent.click(screen.getByTestId("btn-add-node"));
    await screen.findByTestId("select-status-node");
  }

  it("needs a status before adding", async () => {
    await openDialog();

    fireEvent.click(screen.getByTestId("btn-confirm-node"));

    expect(toast.error).toHaveBeenCalledWith("administracion.workflows.editor.selectStatus");
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("creates the node with the chosen status and type and closes", async () => {
    await openDialog();
    fireEvent.change(screen.getByTestId("select-status-node"), { target: { value: "7" } });
    fireEvent.change(screen.getByTestId("select-node-type"), { target: { value: "FINAL" } });

    fireEvent.click(screen.getByTestId("btn-confirm-node"));

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith(
        "/workflow-node",
        expect.objectContaining({ workflowDefinitionId: 5, statusManagementId: 7, type: "FINAL" }),
      ),
    );
    expect(toast.success).toHaveBeenCalledWith("administracion.workflows.editor.nodeAdded");
    await waitFor(() => expect(screen.queryByTestId("btn-confirm-node")).toBeNull());
  });

  it("keeps the dialog open and reports when the node cannot be created", async () => {
    apiPost.mockRejectedValue(new ApiError(409, "dup", JSON.stringify({ message: "Estado repetido" })));
    await openDialog();
    fireEvent.change(screen.getByTestId("select-status-node"), { target: { value: "7" } });

    fireEvent.click(screen.getByTestId("btn-confirm-node"));

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
    expect(toast.success).not.toHaveBeenCalled();
    expect(screen.getByTestId("btn-confirm-node")).toBeInTheDocument();
  });
});

describe("auto layout", () => {
  it("saves a position for every node", async () => {
    await renderEditor();
    fireEvent.click(screen.getByTestId("btn-toggle-edit"));

    fireEvent.click(screen.getByTestId("btn-auto-layout"));

    await waitFor(() => expect(toast.success).toHaveBeenCalledWith("administracion.workflows.editor.autoLayoutDone"));
    expect(apiPut.mock.calls.map((c) => c[0]).sort()).toEqual(["/workflow-node/1", "/workflow-node/2", "/workflow-node/3"]);
    expect(apiPut.mock.calls[0][1]).toEqual(
      expect.objectContaining({ workflowDefinitionId: 5, positionX: expect.any(Number), positionY: expect.any(Number) }),
    );
  });

  it("reports a failed save", async () => {
    apiPut.mockRejectedValue(new Error("down"));
    await renderEditor();
    fireEvent.click(screen.getByTestId("btn-toggle-edit"));

    fireEvent.click(screen.getByTestId("btn-auto-layout"));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("administracion.workflows.editor.errorSavePosition"));
    expect(toast.success).not.toHaveBeenCalled();
  });
});

describe("list view editing", () => {
  it("is read only until edit mode", async () => {
    await renderEditor(true);
    expect(screen.queryByTestId("wf-list-add-node")).toBeNull();
    expect(screen.queryByTestId("wf-list-delete-node-1")).toBeNull();
  });

  it("adds a status from the list", async () => {
    await editList();
    fireEvent.change(screen.getByTestId("wf-list-add-status"), { target: { value: "8" } });

    fireEvent.click(screen.getByTestId("wf-list-add-node"));

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith("/workflow-node", expect.objectContaining({ statusManagementId: 8 })),
    );
  });

  it("changes a node type", async () => {
    await editList();

    fireEvent.change(screen.getAllByRole("combobox", { name: /typeOf/ })[1], { target: { value: "FINAL" } });

    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith("/workflow-node/2", { workflowDefinitionId: 5, type: "FINAL" }),
    );
    expect(toast.success).toHaveBeenCalledWith("administracion.workflows.editor.panel.typeSaved");
  });

  it("reports a rejected type change", async () => {
    apiPut.mockRejectedValue(new Error("down"));
    await editList();

    fireEvent.change(screen.getAllByRole("combobox", { name: /typeOf/ })[1], { target: { value: "FINAL" } });

    await waitFor(() =>
      expect(toast.error).toHaveBeenCalledWith("administracion.workflows.editor.panel.errorSaveType"),
    );
  });

  it("adds a transition from the list", async () => {
    await editList();
    fireEvent.change(screen.getByTestId("wf-list-add-from"), { target: { value: "1" } });
    fireEvent.change(screen.getByTestId("wf-list-add-to"), { target: { value: "3" } });
    fireEvent.change(screen.getByTestId("wf-list-add-desc"), { target: { value: " salta " } });

    fireEvent.click(screen.getByTestId("wf-list-add-transition"));

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith("/workflow-transition", {
        workflowDefinitionId: 5,
        originNodeId: 1,
        destinationNodeId: 3,
        description: "salta",
      }),
    );
    expect(toast.success).toHaveBeenCalledWith("administracion.workflows.editor.panel.transitionAdded");
  });

  it("sends no description when none is typed", async () => {
    await editList();
    fireEvent.change(screen.getByTestId("wf-list-add-from"), { target: { value: "1" } });
    fireEvent.change(screen.getByTestId("wf-list-add-to"), { target: { value: "3" } });

    fireEvent.click(screen.getByTestId("wf-list-add-transition"));

    await waitFor(() => expect(apiPost).toHaveBeenCalled());
    expect(apiPost.mock.calls[0][1]).not.toHaveProperty("description");
  });

  it("refuses a duplicate transition without calling the server", async () => {
    await editList();
    fireEvent.change(screen.getByTestId("wf-list-add-from"), { target: { value: "1" } });
    fireEvent.change(screen.getByTestId("wf-list-add-to"), { target: { value: "2" } });

    fireEvent.click(screen.getByTestId("wf-list-add-transition"));

    expect(screen.getByTestId("wf-list-add-error")).toHaveTextContent("duplicate");
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("reports a transition the server rejects", async () => {
    apiPost.mockRejectedValue(new Error("down"));
    await editList();
    fireEvent.change(screen.getByTestId("wf-list-add-from"), { target: { value: "1" } });
    fireEvent.change(screen.getByTestId("wf-list-add-to"), { target: { value: "3" } });

    fireEvent.click(screen.getByTestId("wf-list-add-transition"));

    await waitFor(() =>
      expect(toast.error).toHaveBeenCalledWith("administracion.workflows.editor.errorCreateTransition"),
    );
  });

  it("saves a transition description keeping its endpoints", async () => {
    await editList();
    fireEvent.change(screen.getAllByRole("textbox", { name: /descriptionOf/ })[0], { target: { value: " nuevo " } });

    fireEvent.click(screen.getByTestId("wf-list-save-desc-9"));

    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith("/workflow-transition/9", {
        workflowDefinitionId: 5,
        originNodeId: 1,
        destinationNodeId: 2,
        condition: undefined,
        description: "nuevo",
      }),
    );
    expect(toast.success).toHaveBeenCalledWith("administracion.workflows.editor.panel.descriptionSaved");
  });

  it("reports a description that cannot be saved", async () => {
    apiPut.mockRejectedValue(new Error("down"));
    await editList();

    fireEvent.click(screen.getByTestId("wf-list-save-desc-9"));

    await waitFor(() =>
      expect(toast.error).toHaveBeenCalledWith("administracion.workflows.editor.panel.errorSaveDescription"),
    );
  });

  it("asks before deleting a node and deletes it once confirmed", async () => {
    await editList();

    fireEvent.click(screen.getByTestId("wf-list-delete-node-3"));
    expect(apiDelete).not.toHaveBeenCalled();
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiDelete).toHaveBeenCalledWith("/workflow-node/3"));
    expect(toast.success).toHaveBeenCalledWith("administracion.workflows.editor.nodeDeleted");
  });

  it("reports a node that cannot be deleted", async () => {
    apiDelete.mockRejectedValue(new Error("linked"));
    await editList();

    fireEvent.click(screen.getByTestId("wf-list-delete-node-3"));
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() =>
      expect(toast.error).toHaveBeenCalledWith("administracion.workflows.editor.errorDeleteNodeHasTransitions"),
    );
  });

  it("deletes a transition once confirmed and reports a failure", async () => {
    await editList();
    fireEvent.click(screen.getByTestId("wf-list-delete-transition-10"));
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));
    await waitFor(() => expect(apiDelete).toHaveBeenCalledWith("/workflow-transition/10"));
    expect(toast.success).toHaveBeenCalledWith("administracion.workflows.editor.transitionDeleted");
  });

  it("reports a transition that cannot be deleted", async () => {
    apiDelete.mockRejectedValue(new Error("down"));
    await editList();
    fireEvent.click(screen.getByTestId("wf-list-delete-transition-10"));
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));
    await waitFor(() =>
      expect(toast.error).toHaveBeenCalledWith("administracion.workflows.editor.errorDeleteTransition"),
    );
  });
});
