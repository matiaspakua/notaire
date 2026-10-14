/**
 * CU82 (#1246): a registration draft is found by id and offers the one step its status allows —
 * submit when generated, observe or register when submitted — each gated on its required fields.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiGet = vi.fn();
const apiPut = vi.fn();
const apiGetBytes = vi.fn();
const toast = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn(), warning: vi.fn() }));

vi.mock("@/lib/api-client", async (orig) => ({
  ...(await orig<typeof import("@/lib/api-client")>()),
  apiGet: (path: string) => apiGet(path),
  apiPut: (path: string, body: unknown) => apiPut(path, body),
  apiGetBytes: (path: string) => apiGetBytes(path),
}));
vi.mock("sonner", () => ({ toast }));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));

import RegistrationDraftsRegistrationPage from "@/app/dashboard/registration-drafts/page";

function draft(status: string) {
  return { idRegistrationDraft: 12, number: 5001, status };
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function openDraft(status: string) {
  apiGet.mockImplementation((path: string) =>
    path.startsWith("/registration-drafts/12") ? Promise.resolve(draft(status)) : Promise.resolve([]),
  );
  render(<RegistrationDraftsRegistrationPage />, { wrapper });
  fireEvent.change(screen.getByTestId("input-search-registration-draft"), { target: { value: "12" } });
  fireEvent.click(screen.getByTestId("btn-search-registration-draft"));
  await screen.findByTestId("registration-draft-status");
}

beforeEach(() => {
  apiGet.mockReset();
  apiPut.mockReset().mockResolvedValue({});
  apiGetBytes.mockReset();
  Object.values(toast).forEach((fn) => fn.mockReset());
});

describe("registration draft lookup", () => {
  it("loads nothing until an id is searched", () => {
    render(<RegistrationDraftsRegistrationPage />, { wrapper });

    fireEvent.click(screen.getByTestId("btn-search-registration-draft"));

    expect(apiGet).not.toHaveBeenCalledWith(expect.stringContaining("/registration-drafts/"));
    expect(screen.queryByTestId("registration-draft-status")).toBeNull();
  });

  it("shows the draft status and offers only submit while it is generated", async () => {
    await openDraft("Generada");

    expect(screen.getByTestId("registration-draft-status")).toHaveTextContent("minutasInscripcion.estadoGenerada");
    expect(screen.getByTestId("btn-submit-registration-draft")).toBeInTheDocument();
    expect(screen.queryByTestId("btn-observe-registration-draft")).toBeNull();
    expect(screen.queryByTestId("btn-register-registration-draft")).toBeNull();
  });

  it("offers observe and register once the draft was submitted", async () => {
    await openDraft("Presentado para inscripción");

    expect(screen.getByTestId("btn-observe-registration-draft")).toBeInTheDocument();
    expect(screen.getByTestId("btn-register-registration-draft")).toBeInTheDocument();
    expect(screen.queryByTestId("btn-submit-registration-draft")).toBeNull();
  });

  it("offers no step for a registered draft", async () => {
    await openDraft("Inscripto");

    expect(screen.queryByTestId("btn-submit-registration-draft")).toBeNull();
    expect(screen.queryByTestId("btn-observe-registration-draft")).toBeNull();
    expect(screen.queryByTestId("btn-register-registration-draft")).toBeNull();
  });
});

describe("registration draft steps", () => {
  it("submits only with a date and an entry number", async () => {
    await openDraft("Generada");
    fireEvent.click(screen.getByTestId("btn-submit-registration-draft"));

    const confirm = await screen.findByTestId("btn-confirm-submit");
    expect(confirm).toBeDisabled();
    fireEvent.change(screen.getByTestId("input-submission-date"), { target: { value: "2026-10-01" } });
    expect(confirm).toBeDisabled();
    fireEvent.change(screen.getByTestId("input-registry-entry-number"), { target: { value: "E-77" } });
    expect(confirm).toBeEnabled();
    fireEvent.click(confirm);

    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith("/registration-drafts/12/submit", {
        dateSubmission: "2026-10-01",
        registryEntryNumber: "E-77",
      }),
    );
    expect(toast.success).toHaveBeenCalled();
  });

  it("records the registry's observation with its correction date", async () => {
    await openDraft("Presentado para inscripción");
    fireEvent.click(screen.getByTestId("btn-observe-registration-draft"));

    const confirm = await screen.findByTestId("btn-confirm-observe");
    expect(confirm).toBeDisabled();
    fireEvent.change(screen.getByTestId("input-notes-registry"), { target: { value: "falta firma" } });
    fireEvent.change(screen.getByTestId("input-correction-date"), { target: { value: "2026-10-09" } });
    fireEvent.click(confirm);

    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith("/registration-drafts/12/observe", {
        registryNotes: "falta firma",
        dateCorrection: "2026-10-09",
      }),
    );
  });

  it("records the final registration", async () => {
    await openDraft("Presentado para inscripción");
    fireEvent.click(screen.getByTestId("btn-register-registration-draft"));

    const confirm = await screen.findByTestId("btn-confirm-register");
    expect(confirm).toBeDisabled();
    fireEvent.change(screen.getByTestId("input-receipt-date"), { target: { value: "2026-10-15" } });
    fireEvent.change(screen.getByTestId("input-final-registration-number"), { target: { value: "F-9" } });
    fireEvent.click(confirm);

    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith("/registration-drafts/12/register", {
        dateReception: "2026-10-15",
        finalRegistrationNumber: "F-9",
      }),
    );
  });

  it("keeps the dialog open and reports the failure when the step is rejected", async () => {
    apiPut.mockRejectedValue(new Error("boom"));
    await openDraft("Generada");
    fireEvent.click(screen.getByTestId("btn-submit-registration-draft"));
    fireEvent.change(await screen.findByTestId("input-submission-date"), { target: { value: "2026-10-01" } });
    fireEvent.change(screen.getByTestId("input-registry-entry-number"), { target: { value: "E-77" } });

    fireEvent.click(screen.getByTestId("btn-confirm-submit"));

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
    expect(screen.getByTestId("btn-confirm-submit")).toBeInTheDocument();
  });

  it("reports a failed report download", async () => {
    apiGetBytes.mockRejectedValue(new Error("boom"));
    await openDraft("Generada");

    fireEvent.click(screen.getByTestId("btn-download-registration-draft"));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("minutasInscripcion.errorReporte"));
  });
});
