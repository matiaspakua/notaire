"use client";

import { Table as TableIcon } from "lucide-react";
import { motion as motionDom } from "motion/react";
import { useTranslations } from "next-intl";
import { useMediaQuery, MOBILE_QUERY } from "@/hooks/useMediaQuery";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Pagination, type PaginationProps } from "@/components/shared/Pagination";
import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorState } from "@/components/shared/ErrorState";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { staggerDelay } from "@/components/motion";
import { motion } from "@/theme/motion";

const MotionTableRow = motionDom.create(TableRow);

/** Row/card entrance transition — shared motion tokens (#2070 / #1368). */
export const rowTransition = (i: number) => ({
  duration: motion.duration.base,
  ease: motion.ease.standard,
  delay: staggerDelay(i),
});

export interface Column<T> {
  key: string;
  header: string;
  render: (row: T) => React.ReactNode;
  className?: string;
  /**
   * Card layout below 768px (#1356). "primary" is the card title (default: the
   * first column that is not `id` or `actions`), "actions" renders as the card's
   * button row (default for key `actions`), "hidden" leaves the column out.
   */
  mobile?: "primary" | "actions" | "hidden";
}

interface DataTableProps<T> {
  data: T[];
  columns: Column<T>[];
  isLoading?: boolean;
  emptyMessage?: string;
  /** Short invitation under the empty title (#2071). */
  emptyDescription?: string;
  /** One next-step control for empty lists (#2071); reuse an existing create action. */
  emptyAction?: React.ReactNode;
  keyExtractor: (row: T) => string | number;
  /** A newer page is loading while the previous rows stay visible (#1340). */
  isFetching?: boolean;
  /** Server-side pagination footer (#1340); omit for lists rendered whole. */
  pagination?: PaginationProps;
  /** Query failure message; when set, ErrorState replaces empty/rows (#1947). */
  error?: string;
  onRetry?: () => void;
}

export function DataTable<T>({
  data,
  columns,
  isLoading,
  emptyMessage,
  emptyDescription,
  emptyAction,
  keyExtractor,
  isFetching,
  pagination,
  error,
  onRetry,
}: DataTableProps<T>) {
  const isMobile = useMediaQuery(MOBILE_QUERY);
  if (error) {
    return (
      <div className="rounded-[24px] border border-border/40 overflow-hidden bg-white apple-shadow">
        <TableError message={error} onRetry={onRetry} />
      </div>
    );
  }
  if (isMobile) {
    return (
      <MobileCards
        data={data}
        columns={columns}
        isLoading={isLoading}
        emptyMessage={emptyMessage}
        emptyDescription={emptyDescription}
        emptyAction={emptyAction}
        keyExtractor={keyExtractor}
      />
    );
  }
  return (
    <div className="rounded-[24px] border border-border/40 overflow-hidden bg-white apple-shadow animate-in fade-in-0 duration-base ease-standard">
      <Table aria-busy={isFetching || isLoading ? true : undefined}>
        <TableHeader>
          <TableRow className="bg-secondary/50 border-b border-border/40 hover:bg-secondary/50">
            {columns.map((col) => (
              <TableHead
                key={col.key}
                className={cn("font-semibold text-[13px] tracking-tight text-muted-foreground py-5 px-6", col.className)}
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
                    <Skeleton className="h-5 w-full" />
                  </TableCell>
                ))}
              </TableRow>
            ))
          ) : data.length === 0 ? (
            <TableRow>
              <TableCell colSpan={columns.length} className="h-64 p-0">
                <TableEmpty
                  emptyMessage={emptyMessage}
                  emptyDescription={emptyDescription}
                  emptyAction={emptyAction}
                />
              </TableCell>
            </TableRow>
          ) : (
            data.map((row, i) => (
              <MotionTableRow
                key={keyExtractor(row)}
                initial={{ opacity: 0, y: motion.distance.sm }}
                animate={{ opacity: 1, y: 0 }}
                transition={rowTransition(i)}
                className="border-b border-border/20 last:border-0 hover:bg-secondary/30 transition-colors duration-fast group"
              >
                {columns.map((col) => (
                  <TableCell key={col.key} className={cn("py-5 px-6 text-foreground font-medium", col.className)}>
                    {col.render(row)}
                  </TableCell>
                ))}
              </MotionTableRow>
            ))
          )}
        </TableBody>
      </Table>
      {pagination && <Pagination {...pagination} />}
    </div>
  );
}

const emptyIcon = <TableIcon className="h-8 w-8 opacity-40" aria-hidden />;

/** Empty invitation for table and card layouts (#1942 / #1354 / #2071). */
function TableEmpty({
  emptyMessage,
  emptyDescription,
  emptyAction,
}: {
  emptyMessage?: string;
  emptyDescription?: string;
  emptyAction?: React.ReactNode;
}) {
  if (emptyMessage) {
    return (
      <EmptyState
        title={emptyMessage}
        description={emptyDescription}
        action={emptyAction}
        icon={emptyIcon}
      />
    );
  }
  return <TableEmptyDefault emptyDescription={emptyDescription} emptyAction={emptyAction} />;
}

function TableEmptyDefault({
  emptyDescription,
  emptyAction,
}: {
  emptyDescription?: string;
  emptyAction?: React.ReactNode;
}) {
  const tc = useTranslations("common");
  return (
    <EmptyState
      title={tc("noData")}
      description={emptyDescription}
      action={emptyAction}
      icon={emptyIcon}
    />
  );
}

/** Query failure for table and card layouts (#1947). */
function TableError({ message, onRetry }: { message: string; onRetry?: () => void }) {
  if (!onRetry) {
    return <ErrorState title={message} />;
  }
  return <TableErrorWithRetry message={message} onRetry={onRetry} />;
}

function TableErrorWithRetry({ message, onRetry }: { message: string; onRetry: () => void }) {
  const tc = useTranslations("common");
  return (
    <ErrorState
      title={message}
      action={
        <Button type="button" variant="secondary" onClick={onRetry} data-testid="btn-retry">
          {tc("retry")}
        </Button>
      }
    />
  );
}

function isActions<T>(col: Column<T>) {
  return col.mobile === "actions" || (col.mobile === undefined && col.key === "actions");
}

function MobileCards<T>({
  data,
  columns,
  isLoading,
  emptyMessage,
  emptyDescription,
  emptyAction,
  keyExtractor,
}: Required<Pick<DataTableProps<T>, "data" | "columns" | "keyExtractor">> &
  Pick<DataTableProps<T>, "emptyMessage" | "emptyDescription" | "emptyAction"> &
  Pick<DataTableProps<T>, "isLoading">) {
  const visible = columns.filter((c) => c.mobile !== "hidden");
  const actions = visible.filter(isActions);
  const fields = visible.filter((c) => !isActions(c));
  const title =
    fields.find((c) => c.mobile === "primary") ??
    fields.find((c) => c.key !== "id") ??
    fields[0];
  const details = fields.filter((c) => c !== title);

  if (isLoading) {
    return (
      <ul role="list" data-testid="data-table-cards" aria-busy="true" className="flex flex-col gap-3">
        {Array.from({ length: 3 }).map((_, i) => (
          <li key={i} className="rounded-2xl border border-border/40 bg-white p-4 apple-shadow">
            <Skeleton className="h-5 w-2/3" />
            <Skeleton className="mt-3 h-4 w-full" />
          </li>
        ))}
      </ul>
    );
  }

  if (data.length === 0) {
    return (
      <div className="rounded-2xl border border-border/40 bg-white apple-shadow">
        <TableEmpty
          emptyMessage={emptyMessage}
          emptyDescription={emptyDescription}
          emptyAction={emptyAction}
        />
      </div>
    );
  }

  return (
    <ul role="list" data-testid="data-table-cards" className="flex flex-col gap-3">
      {data.map((row, i) => (
        <motionDom.li
          key={keyExtractor(row)}
          data-testid="data-table-card"
          initial={{ opacity: 0, y: motion.distance.sm }}
          animate={{ opacity: 1, y: 0 }}
          transition={rowTransition(i)}
          className="rounded-2xl border border-border/40 bg-white p-4 apple-shadow"
        >
          {title && (
            <div data-testid="data-table-card-title" className="text-base font-semibold text-foreground break-words">
              {title.render(row)}
            </div>
          )}
          {details.length > 0 && (
            <dl className="mt-2 grid grid-cols-[minmax(0,auto)_minmax(0,1fr)] gap-x-3 gap-y-1.5 text-sm">
              {details.map((col) => (
                <div key={col.key} className="contents">
                  <dt className="text-muted-foreground">{col.header}</dt>
                  <dd className="text-foreground font-medium break-words min-w-0">{col.render(row)}</dd>
                </div>
              ))}
            </dl>
          )}
          {actions.length > 0 && (
            <div
              data-testid="data-table-card-actions"
              className="mt-3 flex flex-wrap justify-end gap-2 border-t border-border/40 pt-2 [&_button]:min-h-11 [&_button]:min-w-11"
            >
              {actions.map((col) => (
                <div key={col.key} className="contents">
                  {col.render(row)}
                </div>
              ))}
            </div>
          )}
        </motionDom.li>
      ))}
    </ul>
  );
}

function cn(...classes: (string | undefined | false)[]) {
  return classes.filter(Boolean).join(" ");
}
