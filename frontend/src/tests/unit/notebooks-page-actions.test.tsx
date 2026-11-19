/**
 * Creating a notebook without a notary on the selected folio is a field error (#2259).
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { fireEvent, render, screen } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";
import { toast } from "sonner";

const create = vi.fn();

vi.mock("sonner", () => ({
  toast: { error: vi.fn(), success: vi.fn() },
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
}));
vi.mock("@/composition/notebook", () => ({
  notebookUseCases: {
    list: vi.fn().mockResolvedValue([]),
    create: (...args: unknown[]) => create(...args),
    downloadCover: vi.fn(),
  },
}));
vi.mock("@/composition/folio", () => ({
  folioUseCases: {
    list: vi.fn().mockResolvedValue([
      { idFolio: 3, number: 10, status: "Nuevo" },
    ]),
  },
}));

import NotebooksPage from "@/app/dashboard/protocol/notebooks/page";

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const wrapper = ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
  return render(<NotebooksPage />, { wrapper });
}

describe("protocol notebooks create (#2259)", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("shows a field error when the selected folio has no notary and sends nothing", async () => {
    renderPage();
    fireEvent.click(await screen.findByRole("button", { name: "protocolo.cuadernos.newCuaderno" }));
    fireEvent.click(await screen.findByTestId("checkbox-folio-3"));
    fireEvent.click(screen.getByRole("button", { name: "common.create" }));

    const folio = screen.getByTestId("checkbox-folio-3");
    expect(folio).toHaveAttribute("aria-invalid", "true");
    const describedBy = folio.getAttribute("aria-describedby");
    expect(describedBy).toBeTruthy();
    expect(document.getElementById(describedBy!.split(" ")[0])).toHaveTextContent(
      "protocolo.cuadernos.selectFoliosFirst",
    );
    expect(toast.error).not.toHaveBeenCalled();
    expect(create).not.toHaveBeenCalled();
  });

  it("clears the folio error when the selection changes", async () => {
    renderPage();
    fireEvent.click(await screen.findByRole("button", { name: "protocolo.cuadernos.newCuaderno" }));
    fireEvent.click(await screen.findByTestId("checkbox-folio-3"));
    fireEvent.click(screen.getByRole("button", { name: "common.create" }));
    expect(screen.getByTestId("checkbox-folio-3")).toHaveAttribute("aria-invalid", "true");

    fireEvent.click(screen.getByTestId("checkbox-folio-3"));

    expect(screen.getByTestId("checkbox-folio-3")).not.toHaveAttribute("aria-invalid");
  });
});
