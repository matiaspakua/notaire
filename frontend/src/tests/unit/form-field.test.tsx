/**
 * FormField links its error and helper text to the control (#1351, RNF-08):
 * aria-describedby, aria-invalid, aria-required, an announced error, no emoji.
 */
import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { FormField } from "@/theme/form-patterns";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

describe("FormField (#1351)", () => {
  it("an error makes the control invalid, describes it and is announced, without an emoji", () => {
    render(
      <FormField label="Name" error="The name is required" required>
        <Input />
      </FormField>,
    );
    const input = screen.getByRole("textbox", { name: /Name/ });
    expect(input).toHaveAttribute("aria-invalid", "true");
    expect(input).toHaveAttribute("aria-required", "true");
    expect(input).toHaveAccessibleDescription("The name is required");
    const alert = screen.getByRole("alert");
    expect(alert).toHaveTextContent("The name is required");
    expect(alert.textContent).not.toMatch(/\p{Extended_Pictographic}/u);
  });

  it("helper text describes the control when there is no error", () => {
    render(
      <FormField label="CUIT" helperText="Without hyphens">
        <Input aria-describedby="extra" />
      </FormField>,
    );
    const input = screen.getByRole("textbox", { name: "CUIT" });
    expect(input).not.toHaveAttribute("aria-invalid");
    expect(input).not.toHaveAttribute("aria-required");
    // The caller's own description is kept.
    expect(input.getAttribute("aria-describedby")!.split(" ")).toContain("extra");
    expect(input).toHaveAccessibleDescription(/Without hyphens/);
    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
  });

  it("works for a native control and for a Select trigger", () => {
    render(
      <>
        <FormField label="Notes" error="Too long">
          <textarea />
        </FormField>
        <FormField label="Status" error="Choose a status">
          <Select>
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="a">A</SelectItem>
            </SelectContent>
          </Select>
        </FormField>
      </>,
    );
    expect(screen.getByRole("textbox", { name: "Notes" })).toHaveAccessibleDescription("Too long");
    const trigger = screen.getByRole("combobox");
    expect(trigger).toHaveAttribute("aria-invalid", "true");
    expect(trigger).toHaveAccessibleDescription("Choose a status");
  });
});
