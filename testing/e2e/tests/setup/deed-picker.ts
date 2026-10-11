import { expect, type Locator, type Page } from "@playwright/test";

/**
 * Deed pickers are server-search comboboxes (#1340): they list the newest
 * signed deeds and find any other one by number, so specs type the number
 * instead of scrolling a size=1000 Radix select.
 */
export function deedOption(page: Page, number: number | string): Locator {
  return page.getByRole("listbox").getByRole("option", { name: new RegExp(`\\b${number}$`) });
}

/** Types `number` into the deed picker `testId` and returns its matching option (not clicked). */
export async function searchDeed(page: Page, testId: string, number: number | string): Promise<Locator> {
  const picker = page.getByTestId(testId);
  await picker.click();
  await picker.fill(String(number));
  return deedOption(page, number);
}

/** Picks the deed with `number` in the deed picker `testId`. */
export async function chooseDeed(page: Page, testId: string, number: number | string): Promise<void> {
  const option = await searchDeed(page, testId, number);
  await expect(option).toBeVisible({ timeout: 8000 });
  await option.click();
  await expect(page.getByTestId(testId)).toHaveValue(new RegExp(`\\b${number}$`));
}
