import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

vi.mock("next-intl", () => ({
  useTranslations: () => (key: string) => key,
}));

import { ConfirmDialog } from "@/components/shared/ConfirmDialog";

describe("ConfirmDialog", () => {
  it("renders nothing visible when closed", () => {
    render(<ConfirmDialog open={false} onOpenChange={vi.fn()} onConfirm={vi.fn()} />);
    expect(screen.queryByRole("alertdialog")).not.toBeInTheDocument();
  });

  it("renders the default title, description, and actions when open", () => {
    render(<ConfirmDialog open={true} onOpenChange={vi.fn()} onConfirm={vi.fn()} />);
    expect(screen.getByRole("alertdialog")).toBeInTheDocument();
    expect(screen.getByText("title")).toBeInTheDocument();
    expect(screen.getByText("description")).toBeInTheDocument();
    expect(screen.getByText("confirm")).toBeInTheDocument();
    expect(screen.getByText("cancel")).toBeInTheDocument();
  });

  it("prefers explicit title/description props over the translated defaults", () => {
    render(
      <ConfirmDialog
        open={true}
        onOpenChange={vi.fn()}
        onConfirm={vi.fn()}
        title="Eliminar persona"
        description="Se eliminará permanentemente."
      />
    );
    expect(screen.getByText("Eliminar persona")).toBeInTheDocument();
    expect(screen.getByText("Se eliminará permanentemente.")).toBeInTheDocument();
  });

  it("calls onConfirm when the confirm action is clicked", async () => {
    const user = userEvent.setup();
    const onConfirm = vi.fn();
    render(<ConfirmDialog open={true} onOpenChange={vi.fn()} onConfirm={onConfirm} />);

    await user.click(screen.getByText("confirm"));

    expect(onConfirm).toHaveBeenCalledTimes(1);
  });

  it("disables confirm and cancel actions while loading", () => {
    render(<ConfirmDialog open={true} onOpenChange={vi.fn()} onConfirm={vi.fn()} loading />);

    expect(screen.getByText("confirm").closest("button")).toBeDisabled();
    expect(screen.getByText("cancel").closest("button")).toBeDisabled();
  });
});
