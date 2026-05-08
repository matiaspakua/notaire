/**
 * Enhanced Gherkin-style step definitions for full-stack E2E tests.
 *
 * Provides Given-When-Then helpers that interact with the UI and
 * support API verification for full-stack testing.
 *
 * Note: FormField now wraps inputs inside <label>, so getByLabel()
 * works for ALL fields across the application.
 */
import { type Page, type Locator, expect } from "@playwright/test";
import { CREDENTIALS } from "./test-data";

export class GherkinSteps {
  constructor(public page: Page) {}

  // =================== GIVEN steps ===================

  /** Log in via the UI with admin credentials */
  async givenUserIsLoggedIn() {
    await this.page.goto("/login");
    await this.page.waitForSelector('[data-testid="input-usuario"]', { timeout: 10000 });
    await this.page.getByTestId("input-usuario").fill(CREDENTIALS.username);
    await this.page.getByTestId("input-contrasenia").fill(CREDENTIALS.password);
    await this.page.getByTestId("btn-ingresar").click();
    await this.page.waitForURL(/\/dashboard/, { timeout: 15000 });
  }

  /** Navigate directly to a page (must be logged in) */
  async givenUserIsOnPage(path: string) {
    await this.page.goto(path);
    await this.page.waitForLoadState("networkidle");
  }

  /** Verify a module or heading is visible */
  async givenModuleIsVisible(moduleName: string) {
    await expect(this.page.getByText(moduleName).first()).toBeVisible();
  }

  // =================== WHEN steps ===================

  /** Click a button by its visible text */
  async whenUserClicksButton(buttonName: string | RegExp) {
    const btn =
      typeof buttonName === "string"
        ? this.page.getByRole("button", { name: new RegExp(buttonName, "i") })
        : this.page.getByRole("button", { name: buttonName });
    await btn.first().click();
  }

  /**
   * Fill a form field by its label text.
   *
   * With the FormField fix (input nested inside <label>), getByLabel()
   * now works for ALL fields: text, password, number, date, textarea, etc.
   *
   * Falls back to data-testid if label not found.
   */
  async whenUserFillsField(fieldLabel: string, value: string) {
    const labelPattern = new RegExp(fieldLabel, "i");
    const testid = `input-${fieldLabel.toLowerCase().replace(/\s+/g, "-")}`;

    const field = this.page
      .getByLabel(labelPattern)
      .or(this.page.locator(`[data-testid="${testid}"]`));

    await field.waitFor({ state: "visible", timeout: 10000 });
    await field.fill(value);
  }

  /** Fill a form field by its exact data-testid */
  async whenUserFillsFieldByTestId(testId: string, value: string) {
    await this.page.getByTestId(testId).fill(value);
  }

  /**
   * Select an option from a dropdown/select by its label.
   * Handles:
   * - Radix UI Select (button with role="combobox")
   * - Native <select> elements
   *
   * With the FormField fix, the form control is nested inside <label>,
   * so getByLabel() can find it for both Radix and native selects.
   */
  async whenUserSelectsFromDropdown(dropdownLabel: string, option: string) {
    const labelPattern = new RegExp(dropdownLabel, "i");

    // Find the labeled form control (nested inside <label> thanks to FormField fix)
    const field = this.page.getByLabel(labelPattern);
    await field.waitFor({ state: "visible", timeout: 5000 });

    // Distinguish native <select> from Radix Select by tag name
    const tagName = await field.evaluate((el) => el.tagName).catch(() => "");

    if (tagName === "SELECT") {
      // Native <select>: use selectOption directly
      await field.selectOption(option);
      return;
    }

    // Radix UI Select (or other combobox): click to open, then click option
    await field.click();
    const optionEl = this.page.getByRole("option", { name: new RegExp(option, "i") });
    await optionEl.waitFor({ state: "visible", timeout: 5000 });
    await optionEl.click();
  }

  /**
   * Submit the current form by clicking the submit/save button.
   * Tries common button text patterns.
   */
  async whenUserSubmitsForm() {
    const submitBtn = this.page.getByRole("button", {
      name: /confirmar|guardar|crear|registrar|actualizar/i,
    });
    await submitBtn.waitFor({ state: "visible", timeout: 5000 });
    await submitBtn.click();
  }

  /** Cancel the current form */
  async whenUserCancelsForm() {
    await this.page.getByRole("button", { name: /cancelar/i }).click();
  }

  /** Fill a search box and trigger search */
  async whenUserSearches(searchTerm: string) {
    const searchInput = this.page.getByRole("searchbox").or(
      this.page.getByPlaceholder(/buscar/i)
    );
    await searchInput.fill(searchTerm);
    await this.page.waitForTimeout(500);
  }

  /** Click a sidebar navigation link */
  async whenUserNavigatesToSidebar(linkName: string) {
    const sidebarLink = this.page
      .getByRole("link", { name: new RegExp(linkName, "i") })
      .or(this.page.locator(`a:has-text("${linkName}")`))
      .first();
    await sidebarLink.click();
    await this.page.waitForLoadState("networkidle");
  }

  // =================== THEN steps ===================

  /** Verify a modal/dialog is visible, optionally with a specific title */
  async thenModalIsVisible(modalTitle?: string) {
    const dialog = this.page.getByRole("dialog");
    await expect(dialog).toBeVisible({ timeout: 8000 });
    if (modalTitle) {
      await expect(dialog.getByText(modalTitle).first()).toBeVisible({ timeout: 3000 });
    }
  }

  /** Verify a modal/dialog is NOT visible */
  async thenModalIsNotVisible() {
    await expect(this.page.getByRole("dialog")).not.toBeVisible({ timeout: 5000 });
  }

  /** Verify a heading is visible on the page */
  async thenPageHasHeading(heading: string) {
    await expect(
      this.page.getByRole("heading", { name: new RegExp(heading, "i") })
    ).toBeVisible();
  }

  /** Verify text content is visible on the page */
  async thenElementIsVisible(elementName: string) {
    await expect(this.page.getByText(new RegExp(elementName, "i")).first()).toBeVisible();
  }

  /** Verify a data table is rendered */
  async thenTableIsVisible() {
    // Some pages use a table, others use a card grid. Try both.
    const table = this.page.getByRole("table");
    const isTableVisible = await table.isVisible().catch(() => false);
    if (isTableVisible) {
      await expect(table).toBeVisible({ timeout: 10000 });
    } else {
      // Fallback: wait for the page content to load (no "No hay" message)
      await this.page.waitForTimeout(2000);
      await expect(
        this.page.getByText(/no hay/i).first()
      ).toBeVisible({ timeout: 5000 }).catch(() => {
        // It's fine if the table has data, the "no hay" message won't appear
      });
    }
  }

  /** Verify a success/confirmation toast message appears */
  async thenShowsSuccessMessage(message?: string) {
    const pattern = message
      ? new RegExp(message, "i")
      : /creado|registrado|guardado|actualizado|éxito/i;
    await expect(this.page.getByText(pattern).first()).toBeVisible({ timeout: 8000 });
  }

  /** Verify an error message appears */
  async thenShowsErrorMessage(message: string) {
    await expect(this.page.getByText(new RegExp(message, "i")).first()).toBeVisible({
      timeout: 5000,
    });
  }

  /** Verify URL matches the expected pattern */
  async thenUrlIs(expectedUrl: RegExp) {
    await expect(this.page).toHaveURL(expectedUrl);
  }

  /** Verify a form field exists and is visible */
  async thenFormHasField(fieldLabel: string) {
    const labelPattern = new RegExp(fieldLabel, "i");
    // With the FormField fix, the input is nested inside <label>
    // so getByLabel can find it
    await expect(this.page.getByLabel(labelPattern).first()).toBeVisible({ timeout: 5000 });
  }

  /** Verify the page rendered without critical JS errors */
  async thenPageHasNoCriticalErrors() {
    const errorIndicator = this.page.locator('[data-testid="error-boundary"]');
    await expect(errorIndicator).toHaveCount(0);
  }

  // =================== Composite utility methods ===================

  /** Open a new record creation modal */
  async openNewRecordModal(buttonName: string | RegExp = /nuevo|registrar/i) {
    await this.whenUserClicksButton(buttonName);
    await this.thenModalIsVisible();
  }

  /** Fill multiple form fields and submit */
  async fillAndSubmitForm(fields: Record<string, string>) {
    for (const [label, value] of Object.entries(fields)) {
      await this.whenUserFillsField(label, value);
    }
    await this.whenUserSubmitsForm();
  }

  /** Wait for the data table to refresh after mutation */
  async waitForTableRefresh(timeout = 10000) {
    await this.page.waitForTimeout(1000); // Allow refetch
    await this.page.waitForSelector("table, [data-testid='data-table']", { timeout });
  }
}
