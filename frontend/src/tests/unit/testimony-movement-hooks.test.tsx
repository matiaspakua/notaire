/**
 * Unit tests for the post-signing legal cycle hooks (testimony generation/
 * verification, copy download, and property-registry movements).
 *
 * Covers: useTestimonies, useTestimonyMovements (CU07, CU08, CU11, CU12, CU44)
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
  apiGetBytes: vi.fn(),
}));

import { apiGet, apiPost, apiGetBytes } from "@/lib/api-client";
import {
  testimoniesKeys,
  useTestimonies,
  useGenerateTestimony,
  useVerifyTestimony,
  downloadTestimonyCopy,
} from "@/hooks/useTestimonies";
import {
  useEnterRegistration,
  useRegisterInscription,
  useWithdraw,
  useReenter,
} from "@/hooks/useTestimonyMovements";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return Wrapper;
}

describe("testimoniesKeys (CU07, CU08)", () => {
  it("all key is ['testimonies']", () => {
    expect(testimoniesKeys.all).toEqual(["testimonies"]);
  });
});

describe("useTestimonies (CU07, CU08)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("fetches the testimony list via GET /testimonies", async () => {
    vi.mocked(apiGet).mockResolvedValue([{ idTestimony: 1, number: 10 }]);

    const { result } = renderHook(() => useTestimonies(), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiGet).toHaveBeenCalledWith("/testimonies");
    expect(result.current.data).toEqual([{ idTestimony: 1, number: 10 }]);
  });
});

describe("useGenerateTestimony (CU07)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("posts to /testimonies/{idDeed}/generate", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idTestimony: 2, number: 20 });

    const { result } = renderHook(() => useGenerateTestimony(), { wrapper: createWrapper() });
    result.current.mutate(5);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiPost).toHaveBeenCalledWith("/testimonies/5/generate", {});
  });
});

describe("useVerifyTestimony (CU08)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("posts flagged and notes (DtoTestimony's actual fields) to /testimonies/{id}/verify", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idTestimony: 3, verified: true });

    const { result } = renderHook(() => useVerifyTestimony(), { wrapper: createWrapper() });
    result.current.mutate({ id: 3, flagged: true, notes: "Missing signature" });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiPost).toHaveBeenCalledWith("/testimonies/3/verify", {
      flagged: true,
      notes: "Missing signature",
    });
  });
});

describe("downloadTestimonyCopy", () => {
  beforeEach(() => vi.clearAllMocks());

  it("downloads the PDF blob from /reports/testimonies/{id}/copies", async () => {
    const blob = new Blob(["pdf"], { type: "application/pdf" });
    vi.mocked(apiGetBytes).mockResolvedValue(blob);
    const originalCreateElement = document.createElement.bind(document);
    const clickSpy = vi.fn();
    vi.spyOn(document, "createElement").mockImplementation((tag: string) => {
      const el = originalCreateElement(tag);
      if (tag === "a") el.click = clickSpy;
      return el;
    });
    URL.createObjectURL = vi.fn().mockReturnValue("blob:mock-url");
    URL.revokeObjectURL = vi.fn();

    await downloadTestimonyCopy(7);

    expect(apiGetBytes).toHaveBeenCalledWith("/reports/testimonies/7/copies");
    expect(clickSpy).toHaveBeenCalled();
    expect(URL.revokeObjectURL).toHaveBeenCalledWith("blob:mock-url");

    vi.mocked(document.createElement).mockRestore();
  });
});

describe("Testimony movement hooks (CU11, CU12, CU44)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("useEnterRegistration posts to /testimony-movements/{id}/enter-registration", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idTestimonyMovement: 1 });

    const { result } = renderHook(() => useEnterRegistration(), { wrapper: createWrapper() });
    result.current.mutate(5);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiPost).toHaveBeenCalledWith("/testimony-movements/5/enter-registration", {});
  });

  it("useRegisterInscription posts to /testimony-movements/{id}/register-inscription", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idTestimonyMovement: 1, registered: true });

    const { result } = renderHook(() => useRegisterInscription(), { wrapper: createWrapper() });
    result.current.mutate(5);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiPost).toHaveBeenCalledWith("/testimony-movements/5/register-inscription", {});
  });

  it("useWithdraw posts cardNumber to /testimony-movements/{id}/withdraw", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idTestimonyMovement: 1, cardNumber: 123 });

    const { result } = renderHook(() => useWithdraw(), { wrapper: createWrapper() });
    result.current.mutate({ idTestimony: 5, cardNumber: 123 });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiPost).toHaveBeenCalledWith("/testimony-movements/5/withdraw", { cardNumber: 123 });
  });

  it("useReenter posts the card number, observation flag, and notes to /testimony-movements/{id}/reenter", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idTestimonyMovement: 2 });

    const { result } = renderHook(() => useReenter(), { wrapper: createWrapper() });
    result.current.mutate({
      idTestimony: 5,
      cardNumber: 77,
      observedByRegistry: true,
      notes: "Missing stamp",
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiPost).toHaveBeenCalledWith("/testimony-movements/5/reenter", {
      cardNumber: 77,
      observedByRegistry: true,
      notes: "Missing stamp",
    });
  });
});
