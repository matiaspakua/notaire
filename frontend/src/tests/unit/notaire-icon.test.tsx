/**
 * #1525 — NotaireIcon must keep its declared size. Tailwind preflight gives
 * every <img> `max-width: 100%`, so inside a flex button in a narrow action
 * column the button could shrink to its padding and the icon was drawn 0px
 * wide (invisible row actions on 6 admin pages).
 */
import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { NotaireIcon } from "@/components/ui/notaire-icon";

describe("NotaireIcon (#1525)", () => {
  it("can't be squeezed: no flex shrink and no percentage max-width", () => {
    render(<NotaireIcon src="/icons/actions/delete.png" alt="Delete" size={16} />);
    const cls = screen.getByAltText("Delete").className.split(/\s+/);
    expect(cls).toContain("shrink-0");
    expect(cls).toContain("max-w-none");
  });

  it("keeps the declared width and height and the caller's classes", () => {
    render(<NotaireIcon src="/icons/actions/add.png" alt="Add" size={16} className="mr-1" />);
    const img = screen.getByAltText("Add");
    expect(img.getAttribute("width")).toBe("16");
    expect(img.getAttribute("height")).toBe("16");
    expect(img.className.split(/\s+/)).toContain("mr-1");
  });

  it("renders nothing without a source", () => {
    const { container } = render(<NotaireIcon src="" alt="x" />);
    expect(container.firstChild).toBeNull();
  });
});
