import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it } from "vitest";
import { CadenceBoard } from "@/components/security/CadenceBoard";
import { StrideExplorer } from "@/components/security/StrideExplorer";
import { CADENCE, STRIDE } from "@/lib/security-content";

describe("security explorers (#2003)", () => {
  it("covers the six STRIDE properties and keeps the snapshot free of secrets", () => {
    expect(STRIDE.map((item) => item.name)).toEqual([
      "Spoofing",
      "Tampering",
      "Repudiation",
      "Information disclosure",
      "Denial of service",
      "Elevation of privilege",
    ]);
    const blob = JSON.stringify(CADENCE) + JSON.stringify(STRIDE);
    expect(blob).not.toMatch(/ghp_|BEGIN PRIVATE KEY|AKIA/);
    expect(CADENCE.disclaimer.toLowerCase()).toContain("not a live scan");
  });

  it("shows the selected STRIDE property and moves with the arrow keys", async () => {
    render(<StrideExplorer />);
    expect(screen.getByRole("tab", { name: /Spoofing/ })).toHaveAttribute("aria-selected", "true");
    expect(screen.getByRole("tabpanel")).toHaveTextContent("SR-12");
    await userEvent.click(screen.getByRole("tab", { name: /Tampering/ }));
    expect(screen.getByRole("tabpanel")).toHaveTextContent("SR-13");
    screen.getByRole("tab", { name: /Tampering/ }).focus();
    await userEvent.keyboard("{ArrowRight}");
    expect(screen.getByRole("tab", { name: /Repudiation/ })).toHaveAttribute("aria-selected", "true");
  });

  it("filters the cadence down to the weekly tools", async () => {
    render(<CadenceBoard />);
    expect(screen.getByRole("row", { name: /OWASP ZAP/ })).toBeInTheDocument();
    await userEvent.click(screen.getByRole("button", { name: "Weekly" }));
    expect(screen.getByRole("row", { name: /OWASP ZAP/ })).toBeInTheDocument();
    expect(screen.queryByRole("row", { name: /scan_secrets/ })).not.toBeInTheDocument();
  });
});
