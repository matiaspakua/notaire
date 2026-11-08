/**
 * Testimonies page (CU07, CU08, CU44, #1246): a testimony is generated from a chosen deed, verified with an
 * optional observation, corrected only with a positive whole number, deleted after confirmation, and its copy
 * is offered once verified; stale, in-use and failed requests are reported.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiGet = vi.fn();
const apiPost = vi.fn();
const apiPut = vi.fn();
const apiDelete = vi.fn();
const apiGetBytes = vi.fn();
const toast = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() }));

vi.mock("@/lib/api-client", async (orig) => ({
  ...(await orig<typeof import("@/lib/api-client")>()),
  apiGet: (path: string) => apiGet(path),
  apiPost: (path: string, body: unknown) => apiPost(path, body),
  apiPut: (path: string, body: unknown) => apiPut(path, body),
  apiDelete: (path: string) => apiDelete(path),
  apiGetBytes: (path: string) => apiGetBytes(path),
}));
vi.mock("sonner", () => ({ toast }));
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
  usePathname: () => "/dashboard/testimonies",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string, values?: Record<string, unknown>) =>
    values ? `${ns}.${key}:${JSON.stringify(values)}` : `${ns}.${key}`,
  useLocale: () => "es",
}));
vi.mock("@/components/shared/DeedPicker", () => ({
  DeedPicker: (props: { onChange: (d: { idDeed: number } | undefined) => void; value?: number; "data-testid"?: string }) => (
    <>
      <button type="button" data-testid={props["data-testid"]} onClick={() => props.onChange({ idDeed: 70 })}>
        {`deed:${props.value ?? "none"}`}
      </button>
      <button type="button" data-testid="clear-deed" onClick={() => props.onChange(undefined)}>clear</button>
    </>
  ),
}));
vi.mock("@/app/dashboard/testimonies/TestimonyMovementsDialog", () => ({
  TestimonyMovementsDialog: (props: { testimony: { idTestimony?: number } | null; onClose: () => void }) =>
    props.testimony ? (
      <button type="button" data-testid="movements-open" onClick={props.onClose}>{`movements:${props.testimony.idTestimony}`}</button>
    ) : null,
}));
vi.mock("@/app/dashboard/testimonies/RegisterTestimonyDialog", () => ({
  RegisterTestimonyDialog: (props: { open: boolean; onClose: () => void }) =>
    props.open ? <button type="button" data-testid="register-open" onClick={props.onClose}>register</button> : null,
}));

import { ApiError } from "@/lib/api-client";
import TestimoniesPage from "@/app/dashboard/testimonies/page";

const T = "testimonios";

const ROWS = [
  { idTestimony: 7, number: 1200, flagged: false, verified: true, notes: "First", version: 3, deed: { idDeed: 70, number: 500 } },
  { idTestimony: 8, number: 1201, flagged: true, verified: false, notes: null, version: 0 },
];

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function renderPage() {
  render(<TestimoniesPage />, { wrapper });
  await screen.findByTestId("btn-edit-testimony-7");
}

beforeEach(() => {
  apiGet.mockReset().mockResolvedValue(ROWS);
  apiPost.mockReset().mockResolvedValue({ idTestimony: 9 });
  apiPut.mockReset().mockResolvedValue(undefined);
  apiDelete.mockReset().mockResolvedValue(undefined);
  apiGetBytes.mockReset().mockResolvedValue(new Blob(["pdf"]));
  Object.values(toast).forEach((fn) => fn.mockReset());
  URL.createObjectURL = vi.fn(() => "blob:copy");
  URL.revokeObjectURL = vi.fn();
  vi.spyOn(HTMLAnchorElement.prototype, "click").mockImplementation(() => undefined);
});

describe("list", () => {
  it("shows the deed number or a dash, and verify or copy depending on the verification", async () => {
    await renderPage();

    const verified = screen.getByTestId("btn-edit-testimony-7").closest("tr")!;
    expect(within(verified).getByText("500")).toBeInTheDocument();
    expect(within(verified).getByTestId("btn-issue-copy-7")).toBeInTheDocument();
    expect(within(verified).queryByTestId("btn-verify-testimony-7")).toBeNull();
    const pending = screen.getByTestId("btn-edit-testimony-8").closest("tr")!;
    expect(within(pending).getByText("—")).toBeInTheDocument();
    expect(within(pending).getByTestId("btn-verify-testimony-8")).toBeInTheDocument();
    expect(within(pending).queryByTestId("btn-issue-copy-8")).toBeNull();
  });

  it("reports a failed load and reloads on retry", async () => {
    apiGet.mockRejectedValue(new Error("down"));
    render(<TestimoniesPage />, { wrapper });

    fireEvent.click(await screen.findByTestId("btn-retry"));

    await waitFor(() => expect(apiGet.mock.calls.length).toBeGreaterThan(1));
  });

  it("opens the movements and register dialogs", async () => {
    await renderPage();

    fireEvent.click(screen.getByTestId("btn-movements-testimony-7"));
    expect(await screen.findByTestId("movements-open")).toHaveTextContent("movements:7");
    fireEvent.click(screen.getByTestId("movements-open"));
    await waitFor(() => expect(screen.queryByTestId("movements-open")).toBeNull());

    fireEvent.click(screen.getByTestId("btn-register-testimony"));
    fireEvent.click(await screen.findByTestId("register-open"));
    await waitFor(() => expect(screen.queryByTestId("register-open")).toBeNull());
  });
});

describe("generate", () => {
  it("cannot be created before a deed is chosen, nor after it is cleared", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-generate-testimony"));
    expect(await screen.findByTestId("btn-save-testimony")).toBeDisabled();

    fireEvent.click(screen.getByTestId("select-deed-testimony"));
    expect(screen.getByTestId("btn-save-testimony")).toBeEnabled();
    fireEvent.click(screen.getByTestId("clear-deed"));

    expect(screen.getByTestId("btn-save-testimony")).toBeDisabled();
  });

  it("generates from the chosen deed and closes", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-generate-testimony"));
    fireEvent.click(await screen.findByTestId("select-deed-testimony"));

    fireEvent.click(screen.getByTestId("btn-save-testimony"));

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/testimonies/70/generate", {}));
    expect(toast.success).toHaveBeenCalledWith(`${T}.generated`);
    await waitFor(() => expect(screen.queryByTestId("btn-save-testimony")).toBeNull());
  });

  it("shows the server reason when generation is rejected, and a fallback otherwise", async () => {
    apiPost.mockRejectedValueOnce(new ApiError(409, "/testimonies/70/generate", JSON.stringify({ message: "Deed already has one" })));
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-generate-testimony"));
    fireEvent.click(await screen.findByTestId("select-deed-testimony"));
    fireEvent.click(screen.getByTestId("btn-save-testimony"));
    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("Deed already has one"));

    apiPost.mockRejectedValueOnce(new Error("boom"));
    fireEvent.click(screen.getByTestId("btn-save-testimony"));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith(`${T}.errorGenerate`));
    expect(toast.success).not.toHaveBeenCalled();
  });

  it("forgets the previous deed when reopened", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-generate-testimony"));
    fireEvent.click(await screen.findByTestId("select-deed-testimony"));
    fireEvent.click(screen.getByRole("button", { name: /common\.cancel/ }));
    await waitFor(() => expect(screen.queryByTestId("btn-save-testimony")).toBeNull());

    fireEvent.click(screen.getByTestId("btn-generate-testimony"));

    expect(await screen.findByTestId("select-deed-testimony")).toHaveTextContent("deed:none");
  });
});

describe("verify", () => {
  async function openVerify() {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-verify-testimony-8"));
    await screen.findByTestId("btn-confirm-verify-testimony");
  }

  it("verifies as not observed and without notes by default", async () => {
    await openVerify();

    fireEvent.click(screen.getByTestId("btn-confirm-verify-testimony"));

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/testimonies/8/verify", { flagged: false, notes: undefined }));
    expect(toast.success).toHaveBeenCalledWith(`${T}.verified`);
  });

  it("sends the observation and the flag", async () => {
    await openVerify();
    fireEvent.click(screen.getByTestId("checkbox-observed-testimony"));
    fireEvent.change(screen.getByTestId("input-notes-testimony"), { target: { value: "falta sello" } });

    fireEvent.click(screen.getByTestId("btn-confirm-verify-testimony"));

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/testimonies/8/verify", { flagged: true, notes: "falta sello" }));
  });

  it("starts clean each time and reports a failure without closing", async () => {
    apiPost.mockRejectedValue(new Error("boom"));
    await openVerify();
    fireEvent.change(screen.getByTestId("input-notes-testimony"), { target: { value: "algo" } });
    fireEvent.click(screen.getByTestId("btn-confirm-verify-testimony"));
    await waitFor(() => expect(toast.error).toHaveBeenCalledWith(`${T}.errorVerify`));
    expect(screen.getByTestId("btn-confirm-verify-testimony")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: /common\.cancel/ }));
    await waitFor(() => expect(screen.queryByTestId("btn-confirm-verify-testimony")).toBeNull());
    fireEvent.click(screen.getByTestId("btn-verify-testimony-8"));

    expect(await screen.findByTestId("input-notes-testimony")).toHaveValue("");
  });
});

describe("edit", () => {
  async function openEdit(id: number) {
    await renderPage();
    fireEvent.click(screen.getByTestId(`btn-edit-testimony-${id}`));
    return screen.findByTestId("input-edit-testimony-number");
  }

  it("prefills the number and notes, and sends the trimmed notes with the flags and version", async () => {
    const number = await openEdit(7);
    expect(number).toHaveValue("1200");
    expect(screen.getByTestId("input-edit-testimony-notes")).toHaveValue("First");
    fireEvent.change(screen.getByTestId("input-edit-testimony-notes"), { target: { value: "  Nueva  " } });

    fireEvent.click(screen.getByTestId("btn-save-edit-testimony"));

    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith("/testimonies/7", { number: 1200, notes: "Nueva", flagged: false, verified: true, version: 3 }),
    );
    expect(toast.success).toHaveBeenCalledWith(`${T}.updated`);
    await waitFor(() => expect(screen.queryByTestId("input-edit-testimony-number")).toBeNull());
  });

  it("prefills empty notes for a testimony without them and keeps its flags", async () => {
    await openEdit(8);
    expect(screen.getByTestId("input-edit-testimony-notes")).toHaveValue("");

    fireEvent.click(screen.getByTestId("btn-save-edit-testimony"));

    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith("/testimonies/8", { number: 1201, notes: "", flagged: true, verified: false, version: 0 }),
    );
  });

  it.each(["", "0", "-3", "12.5", "abc", "007"])("refuses the number %j", async (value) => {
    const number = await openEdit(7);
    fireEvent.change(number, { target: { value } });

    fireEvent.click(screen.getByTestId("btn-save-edit-testimony"));

    expect(toast.error).toHaveBeenCalledWith(`${T}.numberRequired`);
    expect(apiPut).not.toHaveBeenCalled();
  });

  it("says the testimony changed when the version is stale", async () => {
    apiPut.mockRejectedValue(new ApiError(409, "/testimonies/7", "stale"));
    await openEdit(7);

    fireEvent.click(screen.getByTestId("btn-save-edit-testimony"));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith(`${T}.staleVersion`));
    expect(screen.getByTestId("btn-save-edit-testimony")).toBeInTheDocument();
  });

  it("shows the server reason, or a fallback, for other failures", async () => {
    apiPut.mockRejectedValueOnce(new ApiError(400, "/testimonies/7", JSON.stringify({ message: "Number taken" })));
    await openEdit(7);
    fireEvent.click(screen.getByTestId("btn-save-edit-testimony"));
    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("Number taken"));

    apiPut.mockRejectedValueOnce(new Error("boom"));
    fireEvent.click(screen.getByTestId("btn-save-edit-testimony"));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith(`${T}.errorUpdate`));
    expect(toast.success).not.toHaveBeenCalled();
  });
});

describe("delete", () => {
  async function confirmDelete(id: number) {
    await renderPage();
    fireEvent.click(screen.getByTestId(`btn-delete-testimony-${id}`));
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));
  }

  it("deletes after confirmation, naming the testimony number in the question", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-delete-testimony-8"));
    expect(await screen.findByText(`${T}.deleteDescription:{"number":1201}`)).toBeInTheDocument();
    fireEvent.click(screen.getByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiDelete).toHaveBeenCalledWith("/testimonies/8"));
    expect(toast.success).toHaveBeenCalledWith(`${T}.deleted`);
  });

  it("warns, and closes the question, when the testimony is in use", async () => {
    apiDelete.mockRejectedValue(new ApiError(409, "/testimonies/7", "in use"));
    await confirmDelete(7);

    await waitFor(() => expect(toast.warning).toHaveBeenCalledWith(`${T}.deleteInUse`));
    await waitFor(() => expect(screen.queryByTestId("confirm-dialog-confirm")).toBeNull());
    expect(toast.success).not.toHaveBeenCalled();
  });

  it("reports any other failure through the delete handler", async () => {
    apiDelete.mockRejectedValue(new ApiError(404, "/testimonies/7", "gone"));
    await confirmDelete(7);

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("common.errors.notFound"));
  });

  it("does not delete when the question is cancelled", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-delete-testimony-7"));
    fireEvent.click(await screen.findByRole("button", { name: "confirmDialog.cancel" }));

    expect(apiDelete).not.toHaveBeenCalled();
  });
});

describe("issue a copy", () => {
  it("downloads the copy of a verified testimony as a PDF", async () => {
    await renderPage();

    fireEvent.click(screen.getByTestId("btn-issue-copy-7"));

    await waitFor(() => expect(apiGetBytes).toHaveBeenCalledWith("/reports/testimonies/7/copies"));
    await waitFor(() => expect(URL.revokeObjectURL).toHaveBeenCalled());
    expect(toast.error).not.toHaveBeenCalled();
  });

  it("reports a failed download and re-enables the button", async () => {
    apiGetBytes.mockRejectedValue(new Error("no pdf"));
    await renderPage();

    fireEvent.click(screen.getByTestId("btn-issue-copy-7"));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith(`${T}.errorCopia`));
    await waitFor(() => expect(screen.getByTestId("btn-issue-copy-7")).toBeEnabled());
  });
});
