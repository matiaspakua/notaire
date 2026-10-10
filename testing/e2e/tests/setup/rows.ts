import type { Locator, Page } from "@playwright/test";

/**
 * The table row that has a cell whose whole text is `text`. A row-name regex
 * such as /537/ also matches "1537" or "9537 ..." in other rows once the dev
 * database grows, which made strict-mode lookups flaky.
 */
export function rowWithCell(page: Page, text: string | number): Locator {
  return page.getByRole("row").filter({ has: page.getByRole("cell", { name: String(text), exact: true }) });
}
