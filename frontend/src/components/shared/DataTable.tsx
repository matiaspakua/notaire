"use client";

import { Table as TableIcon } from "lucide-react";
import { motion } from "motion/react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

const MotionTableRow = motion.create(TableRow);

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
    <div className="rounded-[24px] border border-border/40 overflow-hidden bg-white apple-shadow animate-in fade-in duration-500">
      <Table>
        <TableHeader>
          <TableRow className="bg-[#F5F5F7]/50 border-b border-border/40 hover:bg-[#F5F5F7]/50">
            {columns.map((col) => (
              <TableHead
                key={col.key}
                className={cn("font-bold text-[13px] uppercase tracking-wider text-[#86868b] py-5 px-6", col.className)}
              >
                {col.header}
              </TableHead>
            ))}
          </TableRow>
        </TableHeader>
        <TableBody>
          {isLoading ? (
            Array.from({ length: 5 }).map((_, i) => (
              <TableRow key={i} className="border-b border-border/20 last:border-0">
                {columns.map((col) => (
                  <TableCell key={col.key} className="py-6 px-6">
                    <div className="h-5 bg-[#F5F5F7] animate-pulse rounded-full w-full" />
                  </TableCell>
                ))}
              </TableRow>
            ))
          ) : data.length === 0 ? (
            <TableRow>
              <TableCell
                colSpan={columns.length}
                className="h-64 text-center"
              >
                <div className="flex flex-col items-center justify-center gap-3 text-[#86868b]">
                  <div className="bg-[#F5F5F7] p-4 rounded-full">
                    <TableIcon className="h-8 w-8 opacity-20" />
                  </div>
                  <p className="text-lg font-medium">{emptyMessage}</p>
                </div>
              </TableCell>
            </TableRow>
          ) : (
            data.map((row, i) => (
              <MotionTableRow
                key={keyExtractor(row)}
                initial={{ opacity: 0, y: 6 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.3, ease: [0.4, 0, 0.2, 1], delay: Math.min(i * 0.03, 0.3) }}
                className="border-b border-border/20 last:border-0 hover:bg-[#F5F5F7]/30 transition-colors duration-200 group"
              >
                {columns.map((col) => (
                  <TableCell key={col.key} className={cn("py-5 px-6 text-[#1d1d1f] font-medium", col.className)}>
                    {col.render(row)}
                  </TableCell>
                ))}
              </MotionTableRow>
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
