/**
 * TS-0100 - Login account lockout
 *
 * Covers: CU78 (login lockout, #560), AUTH-001, issue #689
 * Test Level: E2E UI/UX
 * Fixtures: a unique throwaway username per run. The backend records failed
 * attempts for unknown usernames too, so no real account (admin) is locked
 * and the 15-minute window never leaks into the other specs.
 *
 * Golden Path:
 *   - Repeated wrong credentials lock the username
 *   - The login page shows the lockout message and stays on /login
 *
 * Edge Cases:
 *   - Same flow at 320 px (mobile) without horizontal scroll
 *   - A locked username stays locked on the next attempt
 *
 * Requires: backend running at localhost:8080, frontend at localhost:3000
 * Reference: docs/300-development/303-testing/E2E-TEST-MAPPING.md (TS-0100 row)
 */
import { test, expect, type Page } from "@playwright/test";

const MAX_ATTEMPTS = 5;
const LOCKOUT_MESSAGE =
  /bloqueada temporalmente|temporarily locked/i;

const VIEWPORTS = [
  { name: "desktop", width: 1024, height: 768 },
  { name: "mobile", width: 320, height: 640 },
] as const;

async function submitCredentials(page: Page, username: string, password: string) {
  await page.getByTestId("input-usuario").fill(username);
  await page.getByTestId("input-contrasenia").fill(password);
  await page.getByTestId("btn-ingresar").click();
}

async function failUntilLocked(page: Page, username: string) {
  for (let attempt = 1; attempt <= MAX_ATTEMPTS + 1; attempt++) {
    await submitCredentials(page, username, `wrong-${attempt}`);
    const locked = await page
      .getByText(LOCKOUT_MESSAGE)
      .first()
      .waitFor({ state: "visible", timeout: 3000 })
      .then(() => true)
      .catch(() => false);
    if (locked) {
      return attempt;
    }
  }
  return null;
}

for (const viewport of VIEWPORTS) {
  test.describe(`TS-0100 - Login account lockout (${viewport.name} ${viewport.width}px)`, () => {
    test.use({ viewport: { width: viewport.width, height: viewport.height } });

    test.beforeEach(async ({ page }) => {
      await page.goto("/login");
    });

    test("locks the username after repeated failed logins", async ({ page }) => {
      const username = `lockout-${Date.now()}-${viewport.name}`;

      const lockedAt = await failUntilLocked(page, username);

      expect(lockedAt, "lockout message never appeared").not.toBeNull();
      expect(lockedAt).toBeLessThanOrEqual(MAX_ATTEMPTS + 1);
      await expect(page).toHaveURL(/\/login/);
      await expect(page.getByTestId("btn-ingresar")).toBeVisible();
    });

    test("keeps a locked username locked on the next attempt", async ({ page }) => {
      const username = `lockout-again-${Date.now()}-${viewport.name}`;
      await failUntilLocked(page, username);

      await submitCredentials(page, username, "another-password");

      await expect(page.getByText(LOCKOUT_MESSAGE).first()).toBeVisible();
      await expect(page).toHaveURL(/\/login/);
    });

    test("keeps the form usable without horizontal scroll while locked", async ({ page }) => {
      await failUntilLocked(page, `lockout-layout-${Date.now()}-${viewport.name}`);

      const overflows = await page.evaluate(
        () => document.documentElement.scrollWidth > document.documentElement.clientWidth,
      );

      expect(overflows).toBe(false);
      await expect(page.getByTestId("input-usuario")).toBeVisible();
    });
  });
}
