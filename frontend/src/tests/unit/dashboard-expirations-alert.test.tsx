/**
 * #1556 / CU42: the dashboard home alerts about documents that expire soon.
 * Same endpoint and window as /dashboard/upcoming-expirations; hidden when
 * there are none or the request fails.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";

const hook = vi.fn();
vi.mock("@/hooks/useUpcomingExpirations", () => ({
  UPCOMING_EXPIRATIONS_DEFAULT_DAYS: 30,
  useUpcomingExpirations: (days: number) => hook(days),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string, values?: Record<string, unknown>) =>
    `${ns}.${key}${values ? JSON.stringify(values) : ""}`,
  useLocale: () => "es",
}));
vi.mock("next/link", () => ({
  default: ({ href, children, ...rest }: { href: string; children: React.ReactNode }) => (
    <a href={href} {...rest}>{children}</a>
  ),
}));

import { ExpirationsAlertCard } from "@/components/dashboard/ExpirationsAlertCard";

const row = (id: number, name: string, days: number) => ({
  idSubmittedDocument: id,
  documentName: name,
  managementNumber: 100 + id,
  managementHeading: `Cliente ${id}`,
  dateDue: `2031-01-${String(10 + days).padStart(2, "0")}`,
  daysRemaining: days,
});

beforeEach(() => hook.mockReset());

describe("ExpirationsAlertCard (#1556)", () => {
  it("asks for the page's default window", () => {
    hook.mockReturnValue({ data: [], isError: false });
    render(<ExpirationsAlertCard />);
    expect(hook).toHaveBeenCalledWith(30);
  });

  it("shows the count, the 3 nearest and a link to the page", () => {
    hook.mockReturnValue({
      data: [row(1, "Far", 20), row(2, "Near", 1), row(3, "Mid", 5), row(4, "Mid2", 9)],
      isError: false,
    });
    render(<ExpirationsAlertCard />);
    const card = screen.getByTestId("expirations-alert");
    expect(card).toHaveTextContent('dashboard.vencimientos.titulo{"count":4,"days":30}');
    const items = screen.getAllByTestId("expirations-alert-item").map((li) => li.textContent);
    expect(items).toHaveLength(3);
    expect(items[0]).toContain("Near");
    expect(items[1]).toContain("Mid");
    expect(items[2]).toContain("Mid2");
    expect(screen.queryByText("Far")).toBeNull();
    expect(screen.getByTestId("expirations-alert-link")).toHaveAttribute("href", "/dashboard/upcoming-expirations");
  });

  it("is hidden when nothing expires", () => {
    hook.mockReturnValue({ data: [], isError: false });
    const { container } = render(<ExpirationsAlertCard />);
    expect(container).toBeEmptyDOMElement();
  });

  it("is hidden on error, without throwing", () => {
    hook.mockReturnValue({ data: undefined, isError: true });
    const { container } = render(<ExpirationsAlertCard />);
    expect(container).toBeEmptyDOMElement();
  });
});
