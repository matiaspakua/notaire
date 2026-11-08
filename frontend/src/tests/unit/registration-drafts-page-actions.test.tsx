/**
 * Registration drafts page (CU82, #1246): a draft is generated for a chosen deed (failures reported), found by
 * id, shown with its status label, and each step (submit, observe, register) closes its dialog on success and
 * keeps it with the reason on failure; the report downloads while the button locks.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiGet = vi.fn();
const apiPost = vi.fn();
const apiPut = vi.fn();
const apiGetBytes = vi.fn();
const toast = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() }));

vi.mock("@/lib/api-client", async (orig) => ({
  ...(await orig<typeof import("@/lib/api-client")>()),
  apiGet: (path: string) => apiGet(path),
  apiPost: (path: string, body: unknown) => apiPost(path, body),
  apiPut: (path: string, body: unknown) => apiPut(path, body),
  apiGetBytes: (path: string) => apiGetBytes(path),
}));
vi.mock("sonner", () => ({ toast }));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));
vi.mock("@/components/shared/DeedPicker", () => ({
  DeedPicker: ({ onChange }: { onChange: (deed: { idDeed?: number } | undefined) => void }) => (
    <div>
      <button type="button" data-testid="pick-deed" onClick={() => onChange({ idDeed: 77 })} />
      <button type="button" data-testid="pick-deed-without-id" onClick={() => onChange({})} />
      <button type="button" data-testid="clear-deed" onClick={() => onChange(undefined)} />
    </div>
  ),
}));

import { ApiError } from "@/lib/api-client";
import RegistrationDraftsRegistrationPage from "@/app/dashboard/registration-drafts/page";

let status: string | undefined = "Generada";
const draft = () => ({ idRegistrationDraft: 12, number: 5001, status });

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function openDraft(next: string | undefined) {
  status = next;
  render(<RegistrationDraftsRegistrationPage />, { wrapper });
  fireEvent.change(screen.getByTestId("input-search-registration-draft"), { target: { value: "12" } });
  fireEvent.click(screen.getByTestId("btn-search-registration-draft"));
  await screen.findByTestId("registration-draft-status");
}

beforeEach(() => {
  status = "Generada";
  apiGet.mockReset().mockImplementation((path: string) =>
    path === "/registration-drafts/12" ? Promise.resolve(draft()) : Promise.resolve([]),
  );
  apiPost.mockReset().mockResolvedValue({ idRegistrationDraft: 12, number: 5001, status: "Generada" });
  apiPut.mockReset().mockResolvedValue({});
  apiGetBytes.mockReset().mockResolvedValue(new Blob(["pdf"]));
  Object.values(toast).forEach((fn) => fn.mockReset());
  URL.createObjectURL = vi.fn(() => "blob:report");
  URL.revokeObjectURL = vi.fn();
});

describe("generate", () => {
  it("needs a deed and generates the draft for it, then shows the new draft", async () => {
    render(<RegistrationDraftsRegistrationPage />, { wrapper });
    const generate = screen.getByTestId("btn-generate-registration-draft");
    expect(generate).toBeDisabled();

    fireEvent.click(screen.getByTestId("pick-deed"));
    expect(generate).toBeEnabled();
    fireEvent.click(generate);

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/registration-drafts", { idDeed: 77 }));
    expect(toast.success).toHaveBeenCalledWith("minutasInscripcion.generated");
    expect(await screen.findByTestId("registration-draft-status")).toHaveTextContent("minutasInscripcion.estadoGenerada");
  });

  it("locks the button again when the deed is cleared or has no id", async () => {
    render(<RegistrationDraftsRegistrationPage />, { wrapper });
    const generate = screen.getByTestId("btn-generate-registration-draft");

    fireEvent.click(screen.getByTestId("pick-deed"));
    fireEvent.click(screen.getByTestId("clear-deed"));
    expect(generate).toBeDisabled();
    fireEvent.click(screen.getByTestId("pick-deed"));
    fireEvent.click(screen.getByTestId("pick-deed-without-id"));
    expect(generate).toBeDisabled();
  });

  it("reports the server's reason when the deed cannot be generated", async () => {
    apiPost.mockRejectedValue(new ApiError(409, "/registration-drafts", '{"message":"La escritura no está firmada"}'));
    render(<RegistrationDraftsRegistrationPage />, { wrapper });
    fireEvent.click(screen.getByTestId("pick-deed"));

    fireEvent.click(screen.getByTestId("btn-generate-registration-draft"));

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
    expect(toast.error).not.toHaveBeenCalledWith("minutasInscripcion.errorGenerate");
    expect(screen.queryByTestId("registration-draft-status")).toBeNull();
  });

  it("falls back to the generic message when the server gives no reason", async () => {
    apiPost.mockRejectedValue(new ApiError(500, "/registration-drafts", ""));
    render(<RegistrationDraftsRegistrationPage />, { wrapper });
    fireEvent.click(screen.getByTestId("pick-deed"));

    fireEvent.click(screen.getByTestId("btn-generate-registration-draft"));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("minutasInscripcion.errorGenerate"));
  });

  it("does not show a draft when the created one has no id", async () => {
    apiPost.mockResolvedValue({ number: 1 });
    render(<RegistrationDraftsRegistrationPage />, { wrapper });
    fireEvent.click(screen.getByTestId("pick-deed"));

    fireEvent.click(screen.getByTestId("btn-generate-registration-draft"));

    await waitFor(() => expect(toast.success).toHaveBeenCalled());
    expect(screen.queryByTestId("registration-draft-status")).toBeNull();
  });
});

describe("status label", () => {
  it.each([
    ["Presentado para inscripción", "minutasInscripcion.estadoPresentada"],
    ["Observado", "minutasInscripcion.estadoObservada"],
    ["Inscripto", "minutasInscripcion.estadoInscripta"],
    ["Otro estado", "Otro estado"],
    [undefined, "—"],
  ])("shows %s as %s", async (value, label) => {
    await openDraft(value);

    expect(screen.getByTestId("registration-draft-status")).toHaveTextContent(label);
  });

  it("shows the loading text while the draft loads", async () => {
    apiGet.mockReturnValue(new Promise(() => undefined));
    render(<RegistrationDraftsRegistrationPage />, { wrapper });
    fireEvent.change(screen.getByTestId("input-search-registration-draft"), { target: { value: "12" } });

    fireEvent.click(screen.getByTestId("btn-search-registration-draft"));

    expect(await screen.findByText("common.loading")).toBeInTheDocument();
  });

  it("clears nothing when an empty id is searched after a draft is shown", async () => {
    await openDraft("Generada");
    fireEvent.change(screen.getByTestId("input-search-registration-draft"), { target: { value: "" } });

    fireEvent.click(screen.getByTestId("btn-search-registration-draft"));

    expect(screen.getByTestId("registration-draft-status")).toBeInTheDocument();
  });
});

describe("steps", () => {
  it("closes the submit dialog after a successful submission", async () => {
    await openDraft("Generada");
    fireEvent.click(screen.getByTestId("btn-submit-registration-draft"));
    fireEvent.change(await screen.findByTestId("input-submission-date"), { target: { value: "2026-10-01" } });
    fireEvent.change(screen.getByTestId("input-registry-entry-number"), { target: { value: "E-77" } });

    fireEvent.click(screen.getByTestId("btn-confirm-submit"));

    await waitFor(() => expect(toast.success).toHaveBeenCalledWith("minutasInscripcion.presented"));
    await waitFor(() => expect(screen.queryByTestId("btn-confirm-submit")).toBeNull());
  });

  it("closes the observe dialog after a successful observation and cancels without sending", async () => {
    await openDraft("Presentado para inscripción");
    fireEvent.click(screen.getByTestId("btn-observe-registration-draft"));
    await screen.findByTestId("btn-confirm-observe");
    fireEvent.click(within(screen.getByRole("dialog")).getByRole("button", { name: "common.cancel" }));
    await waitFor(() => expect(screen.queryByTestId("btn-confirm-observe")).toBeNull());
    expect(apiPut).not.toHaveBeenCalled();

    fireEvent.click(screen.getByTestId("btn-observe-registration-draft"));
    fireEvent.change(await screen.findByTestId("input-notes-registry"), { target: { value: "falta firma" } });
    fireEvent.change(screen.getByTestId("input-correction-date"), { target: { value: "2026-10-09" } });
    fireEvent.click(screen.getByTestId("btn-confirm-observe"));

    await waitFor(() => expect(toast.success).toHaveBeenCalledWith("minutasInscripcion.observed"));
    await waitFor(() => expect(screen.queryByTestId("btn-confirm-observe")).toBeNull());
  });

  it("closes the register dialog after a successful registration", async () => {
    await openDraft("Presentado para inscripción");
    fireEvent.click(screen.getByTestId("btn-register-registration-draft"));
    fireEvent.change(await screen.findByTestId("input-receipt-date"), { target: { value: "2026-10-15" } });
    fireEvent.change(screen.getByTestId("input-final-registration-number"), { target: { value: "F-9" } });

    fireEvent.click(screen.getByTestId("btn-confirm-register"));

    await waitFor(() => expect(toast.success).toHaveBeenCalledWith("minutasInscripcion.inscribed"));
    await waitFor(() => expect(screen.queryByTestId("btn-confirm-register")).toBeNull());
  });

  it("keeps the observe dialog open with the fallback message when it fails", async () => {
    apiPut.mockRejectedValue(new ApiError(500, "/registration-drafts/12/observe", ""));
    await openDraft("Presentado para inscripción");
    fireEvent.click(screen.getByTestId("btn-observe-registration-draft"));
    fireEvent.change(await screen.findByTestId("input-notes-registry"), { target: { value: "falta firma" } });
    fireEvent.change(screen.getByTestId("input-correction-date"), { target: { value: "2026-10-09" } });

    fireEvent.click(screen.getByTestId("btn-confirm-observe"));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("minutasInscripcion.errorObservar"));
    expect(screen.getByTestId("btn-confirm-observe")).toBeInTheDocument();
  });

  it("shows the server's reason when an observation is rejected", async () => {
    apiPut.mockRejectedValue(new ApiError(409, "/registration-drafts/12/observe", '{"message":"Ya observada"}'));
    await openDraft("Presentado para inscripción");
    fireEvent.click(screen.getByTestId("btn-observe-registration-draft"));
    fireEvent.change(await screen.findByTestId("input-notes-registry"), { target: { value: "falta firma" } });
    fireEvent.change(screen.getByTestId("input-correction-date"), { target: { value: "2026-10-09" } });

    fireEvent.click(screen.getByTestId("btn-confirm-observe"));

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
    expect(toast.error).not.toHaveBeenCalledWith("minutasInscripcion.errorObservar");
  });

  it("keeps the register dialog open with the server's reason when it fails", async () => {
    apiPut.mockRejectedValue(new ApiError(409, "/registration-drafts/12/register", '{"message":"Número repetido"}'));
    await openDraft("Presentado para inscripción");
    fireEvent.click(screen.getByTestId("btn-register-registration-draft"));
    fireEvent.change(await screen.findByTestId("input-receipt-date"), { target: { value: "2026-10-15" } });
    fireEvent.change(screen.getByTestId("input-final-registration-number"), { target: { value: "F-9" } });

    fireEvent.click(screen.getByTestId("btn-confirm-register"));

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
    expect(toast.error).not.toHaveBeenCalledWith("minutasInscripcion.errorInscribir");
    expect(screen.getByTestId("btn-confirm-register")).toBeInTheDocument();
  });

  it("keeps the submit dialog open with the fallback message when it fails", async () => {
    apiPut.mockRejectedValue(new ApiError(500, "/registration-drafts/12/submit", ""));
    await openDraft("Generada");
    fireEvent.click(screen.getByTestId("btn-submit-registration-draft"));
    fireEvent.change(await screen.findByTestId("input-submission-date"), { target: { value: "2026-10-01" } });
    fireEvent.change(screen.getByTestId("input-registry-entry-number"), { target: { value: "E-77" } });

    fireEvent.click(screen.getByTestId("btn-confirm-submit"));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("minutasInscripcion.errorPresentar"));
  });

  it("cancels the register dialog without sending", async () => {
    await openDraft("Presentado para inscripción");
    fireEvent.click(screen.getByTestId("btn-register-registration-draft"));
    await screen.findByTestId("btn-confirm-register");

    fireEvent.click(within(screen.getByRole("dialog")).getByRole("button", { name: "common.cancel" }));

    await waitFor(() => expect(screen.queryByTestId("btn-confirm-register")).toBeNull());
    expect(apiPut).not.toHaveBeenCalled();
  });

  it("cancels the submit dialog without sending", async () => {
    await openDraft("Generada");
    fireEvent.click(screen.getByTestId("btn-submit-registration-draft"));
    await screen.findByTestId("btn-confirm-submit");

    fireEvent.click(within(screen.getByRole("dialog")).getByRole("button", { name: "common.cancel" }));

    await waitFor(() => expect(screen.queryByTestId("btn-confirm-submit")).toBeNull());
    expect(apiPut).not.toHaveBeenCalled();
  });
});

describe("report", () => {
  it("downloads the report of the draft and locks the button while it runs", async () => {
    let release: (blob: Blob) => void = () => undefined;
    apiGetBytes.mockReturnValue(new Promise<Blob>((resolve) => { release = resolve; }));
    await openDraft("Generada");

    fireEvent.click(screen.getByTestId("btn-download-registration-draft"));

    await waitFor(() => expect(screen.getByTestId("btn-download-registration-draft")).toBeDisabled());
    expect(apiGetBytes).toHaveBeenCalledWith("/reports/registration-draft/12");
    release(new Blob(["pdf"]));
    await waitFor(() => expect(screen.getByTestId("btn-download-registration-draft")).toBeEnabled());
    expect(toast.error).not.toHaveBeenCalled();
  });
});
