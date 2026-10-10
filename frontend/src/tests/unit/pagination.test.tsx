/**
 * Server-side pagination footer (#1340; RF-44, RNF-03).
 *
 * Paged lists used to request size=1000 and drop totalElements, so rows after
 * the 1000th were unreachable. The shared Pagination footer shows the range
 * and total and moves between pages.
 */
import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { NextIntlClientProvider } from "next-intl";
import enMessages from "../../../messages/en.json";
import esMessages from "../../../messages/es.json";
import { Pagination } from "@/components/shared/Pagination";
import { DataTable } from "@/components/shared/DataTable";

function renderEn(ui: React.ReactElement) {
  return render(
    <NextIntlClientProvider locale="en" messages={enMessages}>
      {ui}
    </NextIntlClientProvider>,
  );
}

describe("Pagination (#1340)", () => {
  it("is a labelled navigation landmark showing the range and the total", () => {
    renderEn(<Pagination page={0} size={20} totalElements={1384} onPageChange={() => {}} />);
    expect(screen.getByRole("navigation", { name: /pagination/i })).toBeInTheDocument();
    expect(screen.getByTestId("pagination-status")).toHaveTextContent("1–20 of 1384");
    expect(screen.getByTestId("pagination-page")).toHaveTextContent("Page 1 of 70");
  });

  it("disables previous on the first page and next on the last page", () => {
    const { rerender } = renderEn(
      <Pagination page={0} size={20} totalElements={45} onPageChange={() => {}} />,
    );
    expect(screen.getByRole("button", { name: /previous/i })).toBeDisabled();
    expect(screen.getByRole("button", { name: /next/i })).toBeEnabled();
    rerender(
      <NextIntlClientProvider locale="en" messages={enMessages}>
        <Pagination page={2} size={20} totalElements={45} onPageChange={() => {}} />
      </NextIntlClientProvider>,
    );
    expect(screen.getByTestId("pagination-status")).toHaveTextContent("41–45 of 45");
    expect(screen.getByRole("button", { name: /next/i })).toBeDisabled();
  });

  it("calls onPageChange with the zero-based target page, including first and last", () => {
    const onPageChange = vi.fn();
    renderEn(<Pagination page={3} size={20} totalElements={1384} onPageChange={onPageChange} />);
    fireEvent.click(screen.getByRole("button", { name: /next/i }));
    fireEvent.click(screen.getByRole("button", { name: /previous/i }));
    fireEvent.click(screen.getByRole("button", { name: /first/i }));
    fireEvent.click(screen.getByRole("button", { name: /last/i }));
    expect(onPageChange.mock.calls.map((c) => c[0])).toEqual([4, 2, 0, 69]);
  });

  it("offers 20/50/100 rows per page and reports the choice", () => {
    const onSizeChange = vi.fn();
    renderEn(
      <Pagination page={0} size={20} totalElements={100} onPageChange={() => {}} onSizeChange={onSizeChange} />,
    );
    const select = screen.getByLabelText(/rows per page/i) as HTMLSelectElement;
    expect(Array.from(select.options).map((o) => o.value)).toEqual(["20", "50", "100"]);
    fireEvent.change(select, { target: { value: "50" } });
    expect(onSizeChange).toHaveBeenCalledWith(50);
  });

  it("shows an empty range when there are no rows", () => {
    renderEn(<Pagination page={0} size={20} totalElements={0} onPageChange={() => {}} />);
    expect(screen.getByTestId("pagination-status")).toHaveTextContent("0 of 0");
    expect(screen.getByRole("button", { name: /next/i })).toBeDisabled();
  });

  it("has the same pagination keys in es and en", () => {
    const en = (enMessages as { common: { pagination?: Record<string, string> } }).common.pagination;
    const es = (esMessages as { common: { pagination?: Record<string, string> } }).common.pagination;
    expect(en).toBeDefined();
    expect(Object.keys(es ?? {}).sort()).toEqual(Object.keys(en ?? {}).sort());
  });
});

describe("DataTable with pagination (#1340)", () => {
  const columns = [{ key: "id", header: "ID", render: (r: { id: number }) => r.id }];

  it("renders the footer and marks the table busy while the next page loads", () => {
    renderEn(
      <DataTable
        data={[{ id: 1 }]}
        columns={columns}
        keyExtractor={(r) => r.id}
        isFetching
        pagination={{ page: 0, size: 20, totalElements: 30, onPageChange: () => {} }}
      />,
    );
    expect(screen.getByRole("navigation", { name: /pagination/i })).toBeInTheDocument();
    expect(screen.getByRole("table")).toHaveAttribute("aria-busy", "true");
  });

  it("renders no footer without the pagination prop", () => {
    renderEn(<DataTable data={[{ id: 1 }]} columns={columns} keyExtractor={(r) => r.id} />);
    expect(screen.queryByRole("navigation")).not.toBeInTheDocument();
  });
});
