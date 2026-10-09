import * as React from "react";
import { cn } from "@/lib/utils";
import { theme } from "@/theme/tokens";

const neutral100 = theme.colors.neutral[100];
const headerBg = `color-mix(in srgb, ${neutral100} 60%, transparent)`;
const rowHoverBg = `color-mix(in srgb, ${neutral100} 40%, transparent)`;

const Table = React.forwardRef<HTMLTableElement, React.HTMLAttributes<HTMLTableElement>>(
  ({ className, ...props }, ref) => (
    <div className="relative w-full overflow-auto">
      <table ref={ref} className={cn("w-full caption-bottom text-sm", className)} {...props} />
    </div>
  )
);
Table.displayName = "Table";

const TableHeader = React.forwardRef<HTMLTableSectionElement, React.HTMLAttributes<HTMLTableSectionElement>>(
  ({ className, style, ...props }, ref) => (
    <thead
      ref={ref}
      className={cn("border-b border-border/40", className)}
      style={{ backgroundColor: headerBg, ...style }}
      {...props}
    />
  )
);
TableHeader.displayName = "TableHeader";

const TableBody = React.forwardRef<HTMLTableSectionElement, React.HTMLAttributes<HTMLTableSectionElement>>(
  ({ className, ...props }, ref) => (
    <tbody ref={ref} className={cn("[&_tr:last-child]:border-0", className)} {...props} />
  )
);
TableBody.displayName = "TableBody";

const TableRow = React.forwardRef<HTMLTableRowElement, React.HTMLAttributes<HTMLTableRowElement>>(
  ({ className, style, ...props }, ref) => (
    <tr
      ref={ref}
      className={cn(
        "border-b border-border/20 transition-colors hover:bg-[var(--table-row-hover)] data-[state=selected]:bg-[var(--table-row-selected)]",
        className,
      )}
      style={
        {
          "--table-row-hover": rowHoverBg,
          "--table-row-selected": neutral100,
          ...style,
        } as React.CSSProperties
      }
      {...props}
    />
  )
);
TableRow.displayName = "TableRow";

const TableHead = React.forwardRef<HTMLTableCellElement, React.ThHTMLAttributes<HTMLTableCellElement>>(
  ({ className, style, ...props }, ref) => (
    <th
      ref={ref}
      className={cn(
        "h-14 px-6 text-left align-middle font-bold text-[13px] text-muted-foreground uppercase tracking-wider [&:has([role=checkbox])]:pr-0",
        className,
      )}
      style={style}
      {...props}
    />
  )
);
TableHead.displayName = "TableHead";

const TableCell = React.forwardRef<HTMLTableCellElement, React.TdHTMLAttributes<HTMLTableCellElement>>(
  ({ className, style, ...props }, ref) => (
    <td
      ref={ref}
      className={cn("p-6 align-middle font-medium [&:has([role=checkbox])]:pr-0", className)}
      style={{ color: theme.colors.neutral[900], ...style }}
      {...props}
    />
  )
);
TableCell.displayName = "TableCell";

export { Table, TableHeader, TableBody, TableRow, TableHead, TableCell };
