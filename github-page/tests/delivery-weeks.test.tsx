import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it } from "vitest";
import { DeliveryWeeks } from "@/components/DeliveryWeeks";

const series = [
  { week: "2026-W33", merged: 2 },
  { week: "2026-W34", merged: 10 },
];

describe("DeliveryWeeks", () => {
  it("prints every week from the snapshot and moves the live count with the keyboard control", async () => {
    render(<DeliveryWeeks series={series} medianPerWeek={6} />);
    expect(screen.getByText("2026-W34: 10 merged pull requests")).toBeInTheDocument();
    expect(screen.getByRole("table").textContent).toContain("2026-W33");
    expect(screen.getByRole("table").textContent).toContain("2");
    await userEvent.click(screen.getByRole("option", { name: "2026-W33" }));
    expect(screen.getByText("2026-W33: 2 merged pull requests")).toBeInTheDocument();
  });

  it("says when the snapshot has no weeks", () => {
    render(<DeliveryWeeks series={[]} medianPerWeek={null} />);
    expect(screen.getByText("This snapshot has no weekly counts.")).toBeInTheDocument();
  });
});
