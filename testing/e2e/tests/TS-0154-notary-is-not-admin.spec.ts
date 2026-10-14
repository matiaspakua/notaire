/**
 * TS-0154 - A notary is not an administrator (CU78 / #1638, SR-16)
 *
 * Golden path: admin creates an ESCRIBANO user → login as the notary → administration
 * and audit URLs redirect with the access-denied message, the user, role and audit-log
 * APIs answer 403, and notarial reads stay open.
 */
import { test, expect, type Page } from "@playwright/test";
import { createUser } from "./setup/api-helpers";
import { authenticateAsAdmin } from "./setup/auth";

async function loginAsNewNotary(page: Page): Promise<void> {
  const username = `not1638-${Date.now()}`;
  const password = "Test1234!";

  await authenticateAsAdmin(page);
  const created = await createUser(page, undefined, { name: username, password, type: "ESCRIBANO", active: true });
  expect(created.ok, `createUser failed: ${created.status} ${created.error}`).toBe(true);

  await page.context().clearCookies();
  await page.goto("/login");
  await page.evaluate(() => localStorage.clear());

  await page.getByTestId("input-username").fill(username);
  await page.getByTestId("input-password").fill(password);
  await page.getByTestId("btn-sign-in").click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });
}

test.describe("TS-0154 - A notary is not an administrator (CU78 / #1638)", () => {
  test("the notary is redirected from the administration and audit screens", async ({ page }) => {
    await loginAsNewNotary(page);

    await page.goto("/dashboard/administration/users");
    await expect(page).toHaveURL(/\/dashboard\?forbidden=1/, { timeout: 15000 });
    await expect(page.getByTestId("access-denied-message")).toBeVisible();

    await page.goto("/dashboard/audit");
    await expect(page).toHaveURL(/\/dashboard\?forbidden=1/, { timeout: 15000 });
  });

  test("the API denies the notary user administration but serves notarial reads", async ({ page }) => {
    await loginAsNewNotary(page);

    // page.request carries the notary's session cookie (not the admin helper token).
    for (const path of ["/users", "/roles", "/audit-log"]) {
      const denied = await page.request.get(`/api/v1${path}`);
      expect(denied.status(), `GET ${path}`).toBe(403);
    }
    const people = await page.request.get("/api/v1/people");
    expect(people.status()).toBe(200);
  });
});
