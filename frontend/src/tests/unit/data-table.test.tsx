/**
 * Unit tests for DataTable component
 */
import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { DataTable, type Column } from "@/components/shared/DataTable";

interface Row {
  id: number;
  name: string;
}

const columns: Column<Row>[] = [
  { key: "id", header: "ID", render: (r) => r.id },
  { key: "name", header: "Nombre", render: (r) => r.name },
];

const data: Row[] = [
  { id: 1, name: "Juan García" },
  { id: 2, name: "Ana López" },
];

describe("DataTable", () => {
  it("renders column headers", () => {
    render(
      <DataTable data={data} columns={columns} keyExtractor={(r) => r.id} />
    );
    expect(screen.getByText("ID")).toBeDefined();
    expect(screen.getByText("Nombre")).toBeDefined();
  });

  it("renders data rows", () => {
    render(
      <DataTable data={data} columns={columns} keyExtractor={(r) => r.id} />
    );
    expect(screen.getByText("Juan García")).toBeDefined();
    expect(screen.getByText("Ana López")).toBeDefined();
  });

  it("shows empty message when data is empty", () => {
    render(
      <DataTable
        data={[]}
        columns={columns}
        keyExtractor={(r) => r.id}
        emptyMessage="Sin datos disponibles"
      />
    );
    expect(screen.getByText("Sin datos disponibles")).toBeDefined();
  });

  it("shows skeleton rows when loading", () => {
    const { container } = render(
      <DataTable
        data={[]}
        columns={columns}
        isLoading={true}
        keyExtractor={(r) => r.id}
      />
    );
    // Skeleton rows have animate-pulse class
    const skeletons = container.querySelectorAll(".animate-pulse");
    expect(skeletons.length).toBeGreaterThan(0);
  });
});
