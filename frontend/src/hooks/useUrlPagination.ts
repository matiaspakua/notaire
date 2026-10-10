/**
 * Page and page size of a server-paged list, kept in the URL (`?page=&size=`)
 * so reload, links and back/forward keep the position (#1340).
 */
import { useEffect } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { PAGE_SIZE_OPTIONS } from "@/components/shared/Pagination";

export const DEFAULT_PAGE_SIZE = PAGE_SIZE_OPTIONS[0];

function readInt(value: string | null, fallback: number): number {
  const n = Number(value);
  return Number.isInteger(n) && n >= 0 ? n : fallback;
}

export function useUrlPagination() {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  const page = readInt(searchParams.get("page"), 0);
  const sizeParam = readInt(searchParams.get("size"), DEFAULT_PAGE_SIZE);
  const size = (PAGE_SIZE_OPTIONS as readonly number[]).includes(sizeParam) ? sizeParam : DEFAULT_PAGE_SIZE;

  function update(changes: Record<string, string | number | null>) {
    const next = new URLSearchParams(searchParams.toString());
    for (const [key, value] of Object.entries(changes)) {
      if (value === null || value === "") next.delete(key);
      else next.set(key, String(value));
    }
    const qs = next.toString();
    router.replace(qs ? `${pathname}?${qs}` : pathname, { scroll: false });
  }

  return {
    page,
    size,
    setPage: (next: number) => update({ page: next === 0 ? null : next }),
    setSize: (next: number) => update({ size: next === DEFAULT_PAGE_SIZE ? null : next, page: null }),
  };
}

/**
 * Moves a page past the end (a stale link, or the last rows were deleted) to
 * the last page, instead of an empty table under a "1101–1104 of 1104" footer.
 */
export function useClampPage(
  paging: Pick<ReturnType<typeof useUrlPagination>, "page" | "setPage">,
  totalPages: number | undefined,
) {
  const { page, setPage } = paging;
  useEffect(() => {
    if (totalPages && totalPages > 0 && page >= totalPages) setPage(totalPages - 1);
    // setPage is recreated each render; page and totalPages decide.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, totalPages]);
}
