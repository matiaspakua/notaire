import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Select, SelectTrigger, SelectValue, SelectContent, SelectItem } from "@/components/ui/select";

function renderSelect(onValueChange: (value: string) => void) {
  return render(
    <Select value={undefined} onValueChange={onValueChange}>
      <SelectTrigger aria-label="Estado">
        <SelectValue placeholder="Elegir estado" />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="borrador">Borrador</SelectItem>
        <SelectItem value="aprobado">Aprobado</SelectItem>
        <SelectItem value="rechazado">Rechazado</SelectItem>
      </SelectContent>
    </Select>
  );
}

describe("Select keyboard accessibility (issue #607)", () => {
  it("opens the listbox and selects an option using only the keyboard", async () => {
    const user = userEvent.setup();
    const onValueChange = vi.fn();
    renderSelect(onValueChange);

    await user.tab();
    expect(screen.getByRole("combobox")).toHaveFocus();

    await user.keyboard("{Enter}");
    expect(await screen.findByRole("listbox")).toBeInTheDocument();

    await user.keyboard("{ArrowDown}{Enter}");

    expect(onValueChange).toHaveBeenCalledWith("aprobado");
  });

  it("closes the listbox on Escape without changing the value", async () => {
    const user = userEvent.setup();
    const onValueChange = vi.fn();
    renderSelect(onValueChange);

    await user.tab();
    await user.keyboard("{Enter}");
    expect(await screen.findByRole("listbox")).toBeInTheDocument();

    await user.keyboard("{Escape}");

    expect(screen.queryByRole("listbox")).not.toBeInTheDocument();
    expect(onValueChange).not.toHaveBeenCalled();
  });
});
