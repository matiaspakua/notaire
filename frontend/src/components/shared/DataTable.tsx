"use client";

import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

export interface Column<T> {
  key: string;
  header: string;
  render: (row: T) => React.ReactNode;
  className?: string;
}

interface DataTableProps<T> {
  data: T[];
  columns: Column<T>[];
  isLoading?: boolean;
  emptyMessage?: string;
  keyExtractor: (row: T) => string | number;
}

export function DataTable<T>({
  data,
  columns,
  isLoading,
  emptyMessage = "Sin datos disponibles",
  keyExtractor,
}: DataTableProps<T>) {
  return (
    <div className="rounded-2xl border border-border/50 overflow-hidden bg-card apple-shadow">
      <Table>
        <TableHeader>
          <TableRow className="bg-muted/30 border-b border-border/50">
            {columns.map((col) => (
              <TableHead
                key={col.key}
                className={cn("font-semibold text-sm text-muted-foreground py-3", col.className)}
              >
                {col.header}
              </TableHead>
            ))}
          </TableRow>
        </TableHeader>
        <TableBody>
          {isLoading ? (
            Array.from({ length: 5 }).map((_, i) => (
              <TableRow key={i}>
                {columns.map((col) => (
                  <TableCell key={col.key} className="py-3">
                    <div className="h-4 bg-muted/60 animate-pulse rounded-lg" />
                  </TableCell>
                ))}
              </TableRow>
            ))
          ) : data.length === 0 ? (
            <TableRow>
              <TableCell
                colSpan={columns.length}
                className="h-32 text-center text-muted-foreground"
              >
                <div className="flex flex-col items-center gap-2">
                  <p className="text-base">{emptyMessage}</p>
                </div>
              </TableCell>
            </TableRow>
          ) : (
            data.map((row) => (
              <TableRow
                key={keyExtractor(row)}
                className="border-b border-border/30 last:border-0 hover:bg-muted/20 transition-colors"
              >
                {columns.map((col) => (
                  <TableCell key={col.key} className={cn("py-3", col.className)}>
                    {col.render(row)}
                  </TableCell>
                ))}
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </div>
  );
}

function cn(...classes: (string | undefined | false)[]) {
  return classes.filter(Boolean).join(" ");
}
