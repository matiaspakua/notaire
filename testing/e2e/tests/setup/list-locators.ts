/**
 * Locators that work for both DataTable layouts (#1356): the table at 768px
 * and above, and the card list (`data-table-cards`) below 768px.
 */
import type { Locator, Page } from "@playwright/test";

/** The list container: the table on desktop, the card list on phones. */
export function listView(page: Page): Locator {
  return page.getByRole("table").or(page.getByTestId("data-table-cards"));
}

/** One list entry whose text matches `name`: a table row or a card. */
export function listRow(page: Page, name: RegExp): Locator {
  return page
    .getByRole("row", { name })
    .or(page.getByTestId("data-table-card").filter({ hasText: name }));
}
