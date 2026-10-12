/**
 * #1555 / CU07: manual testimony registration through the bare POST /testimonies.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, renderHook, waitFor, fireEvent } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiPost = vi.fn();
vi.mock("@/lib/api-client", async (orig) => ({
  ...(await orig<typeof import("@/lib/api-client")>()),
  apiPost: (path: string, body: unknown) => apiPost(path, body),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));
const toastError = vi.fn();
vi.mock("sonner", () => ({ toast: { error: (m: string) => toastError(m), success: vi.fn(), warning: vi.fn() } }));
vi.mock("@/components/shared/DeedPicker", () => ({
  DeedPicker: ({ onChange, "data-testid": tid }: { onChange: (d: { idDeed: number; number: number } | undefined) => void; "data-testid"?: string }) => (
    <button type="button" data-testid={tid} onClick={() => onChange({ idDeed: 77, number: 1234 })}>pick</button>
  ),
}));

import { ApiError } from "@/lib/api-client";
import { useCreateTestimony } from "@/hooks/useTestimonies";
import { RegisterTestimonyDialog } from "@/app/dashboard/testimonies/RegisterTestimonyDialog";

let qc: QueryClient;
function wrapper({ children }: { children: ReactNode }) {
  return <QueryClientProvider client={qc}>{children}</QueryClientProvider>;
}

beforeEach(() => {
  apiPost.mockReset();
  toastError.mockReset();
  qc = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
});

describe("useCreateTestimony (#1555)", () => {
  it("posts the body to /testimonies", async () => {
    apiPost.mockResolvedValue({ idTestimony: 5 });
    const { result } = renderHook(() => useCreateTestimony(), { wrapper });
    await result.current.mutateAsync({ deed: { idDeed: 77, number: 1234 }, number: 12, notes: "x", flagged: false, verified: true });
    expect(apiPost).toHaveBeenCalledWith("/testimonies", { deed: { idDeed: 77, number: 1234 }, number: 12, notes: "x", flagged: false, verified: true });
  });
});

describe("RegisterTestimonyDialog (#1555)", () => {
  function open() {
    const onClose = vi.fn();
    render(<RegisterTestimonyDialog open onClose={onClose} />, { wrapper });
    return onClose;
  }

  it("requires a deed and a positive number before posting", async () => {
    open();
    fireEvent.click(screen.getByTestId("btn-save-register-testimony"));
    expect(await screen.findByTestId("register-testimony-error-deed")).toBeInTheDocument();
    expect(screen.getByTestId("register-testimony-error-number")).toBeInTheDocument();
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("posts deed, number, notes and verified, then closes", async () => {
    apiPost.mockResolvedValue({ idTestimony: 5 });
    const onClose = open();
    fireEvent.click(screen.getByTestId("select-deed-register-testimony"));
    fireEvent.change(screen.getByTestId("input-number-register-testimony"), { target: { value: "345" } });
    fireEvent.change(screen.getByTestId("input-notes-register-testimony"), { target: { value: "Histórico" } });
    fireEvent.click(screen.getByTestId("btn-save-register-testimony"));
    await waitFor(() => expect(onClose).toHaveBeenCalled());
    expect(apiPost).toHaveBeenCalledWith("/testimonies", expect.objectContaining({ deed: { idDeed: 77, number: 1234 }, number: 345, notes: "Histórico", flagged: false }));
  });

  it("shows 400 field errors next to their fields", async () => {
    apiPost.mockRejectedValue(new ApiError(400, "/testimonies", JSON.stringify({ message: "number: must be greater than 0" })));
    open();
    fireEvent.click(screen.getByTestId("select-deed-register-testimony"));
    fireEvent.change(screen.getByTestId("input-number-register-testimony"), { target: { value: "9" } });
    fireEvent.click(screen.getByTestId("btn-save-register-testimony"));
    expect(await screen.findByTestId("register-testimony-error-number")).toHaveTextContent("must be greater than 0");
  });

  it("toasts a 404 (deed gone)", async () => {
    apiPost.mockRejectedValue(new ApiError(404, "/testimonies", JSON.stringify({ message: "Deed not found: 77" })));
    open();
    fireEvent.click(screen.getByTestId("select-deed-register-testimony"));
    fireEvent.change(screen.getByTestId("input-number-register-testimony"), { target: { value: "9" } });
    fireEvent.click(screen.getByTestId("btn-save-register-testimony"));
    await waitFor(() => expect(toastError).toHaveBeenCalledWith("testimonios.registrar.deedNotFound"));
  });
});
