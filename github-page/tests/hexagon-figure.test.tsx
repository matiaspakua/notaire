import { readFileSync } from "node:fs";
import path from "node:path";
import { beforeAll, describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { JunctionInstrument } from "@/components/figures/hexagon/figure";

const facts = { inPorts: 12, outPorts: 9, webAdapters: 4, outAdapters: 6 };

beforeAll(() => {
  Object.defineProperty(window, "matchMedia", {
    writable: true,
    value: (query: string) => ({
      matches: false,
      media: query,
      addEventListener: () => {},
      removeEventListener: () => {},
      dispatchEvent: () => false,
    }),
  });
  class Observer {
    observe() {}
    unobserve() {}
    disconnect() {}
  }
  Object.defineProperty(window, "IntersectionObserver", { writable: true, value: Observer });
});

describe("junction instrument on the page (#2083)", () => {
  it("leads with the sealed case, the dependency rule, and a keyboard tour", async () => {
    const user = userEvent.setup();
    render(<JunctionInstrument facts={facts} />);
    expect(screen.getByText("The junction instrument")).toBeInTheDocument();
    expect(screen.getByText(/Sealed case: the domain/)).toBeInTheDocument();
    const caption = document.querySelector(".iso-caption")?.textContent ?? "";
    expect(caption).toMatch(/sealed brass case/);
    expect(caption).toContain("adapter → port → domain");
    expect(caption).toContain("12 inbound port files");
    expect(caption).toContain("9 outbound port files");

    const slider = screen.getByRole("slider");
    expect(slider).toHaveAttribute("aria-valuetext", "HTTP web · inbound gland · adapter → port → domain");
    slider.focus();
    await user.keyboard("{ArrowRight}");
    expect(slider).toHaveAttribute("aria-valuetext", "Open gland · inbound port · the socket faces the domain");
    await user.keyboard("{ArrowRight}");
    expect(slider).toHaveAttribute("aria-valuetext", "JPA persistence · outbound gland · adapter → port → domain");
  });

  it("points the factory home at the gated line instead of mounting a second one", () => {
    const home = readFileSync(path.resolve(__dirname, "../app/ai-sdlc/page.tsx"), "utf8");
    const hex = readFileSync(path.resolve(__dirname, "../app/docs/hexagonal/page.tsx"), "utf8");
    expect(home).toContain("/ai-sdlc/lifecycle/");
    expect(home).toMatch(/card on a carriage/);
    expect(home).not.toContain("GateLine");
    expect(hex).toContain("JunctionInstrument");
    expect(hex.indexOf("JunctionInstrument")).toBeLessThan(hex.indexOf('id="map"'));
  });
});
