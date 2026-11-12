/**
 * A searchable picker inside a FormField carries the field's error state (#2259, CU76 audit method):
 * a visible "required" error must be tied to the combobox input, like every other FormField control.
 */
import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { FormField } from "@/theme/form-patterns";
import { SearchCombobox } from "@/components/shared/SearchCombobox";

const messages = {
  placeholder: "Search",
  searching: "Searching",
  noResults: "No results",
  results: (count: number) => `${count} results`,
  recentHint: "Recent",
  clear: "Clear",
  loadError: "Could not load",
};

function Picker({ error, required }: { error?: string; required?: boolean }) {
  return (
    <FormField label="Budget" error={error} required={required}>
      <SearchCombobox<{ id: number; name: string }>
        useSource={() => ({ options: [], pending: false, failed: false })}
        getKey={(o) => o.id}
        getLabel={(o) => o.name}
        displayValue=""
        onSelect={() => undefined}
        messages={messages}
        aria-label="Budget"
      />
    </FormField>
  );
}

describe("SearchCombobox inside FormField", () => {
  it("marks the input invalid and described by the visible error", () => {
    render(<Picker error="This field is required" required />);
    const input = screen.getByRole("combobox", { name: "Budget" });
    expect(input).toHaveAttribute("aria-invalid", "true");
    const describedBy = input.getAttribute("aria-describedby");
    expect(describedBy).toBeTruthy();
    const error = document.getElementById(describedBy!.split(" ")[0]);
    expect(error).toHaveTextContent("This field is required");
  });

  it("exposes the required state on the input", () => {
    render(<Picker required />);
    expect(screen.getByRole("combobox", { name: "Budget" })).toHaveAttribute("aria-required", "true");
  });

  it("stays unmarked when the field has no error", () => {
    render(<Picker />);
    const input = screen.getByRole("combobox", { name: "Budget" });
    expect(input).not.toHaveAttribute("aria-invalid");
    expect(input).not.toHaveAttribute("aria-describedby");
  });

  it("works outside a FormField without aria wiring", () => {
    render(
      <SearchCombobox<{ id: number; name: string }>
        useSource={() => ({ options: [], pending: false, failed: false })}
        getKey={(o) => o.id}
        getLabel={(o) => o.name}
        displayValue=""
        onSelect={() => undefined}
        messages={messages}
        aria-label="Standalone"
      />,
    );
    expect(screen.getByRole("combobox", { name: "Standalone" })).not.toHaveAttribute("aria-invalid");
  });
});
