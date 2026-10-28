/**
 * MetricCard — Wave 6 / #1953
 */
import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { FolderKanban } from "lucide-react";
import { MetricCard } from "@/components/shared/MetricCard";

describe("MetricCard (#1953)", () => {
  it("renders the label, value slot, and icon", () => {
    render(
      <MetricCard
        label="Cases"
        icon={<FolderKanban data-testid="metric-icon" className="h-8 w-8" />}
      >
        <span data-testid="metric-value">12</span>
      </MetricCard>,
    );
    expect(screen.getByTestId("metric-card")).toBeDefined();
    expect(screen.getByText("Cases")).toBeDefined();
    expect(screen.getByTestId("metric-value").textContent).toBe("12");
    expect(screen.getByTestId("metric-icon")).toBeDefined();
  });
});
