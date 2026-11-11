/**
 * TS-0046 - MFA: enrol with TOTP, then sign in with password + TOTP
 *
 * Covers: CU78 / RNF-12, issue #1640 (ADR-032), OWASP A07
 * Test Level: E2E UI
 *
 * Golden Path:
 *   - Disposable user signs in, enrols on /dashboard/security (secret shown, code confirmed,
 *     recovery codes shown once)
 *   - Signs out; the password step now yields the code step instead of a session
 *   - A wrong code is refused and keeps the step; the current TOTP completes the login
 *   - Disables MFA with a recovery code
 *
 * Uses a disposable EMPLEADO so the shared admin account never gains a second factor.
 * Requires: backend with NOTAIRE_MFA_ENCRYPTION_KEY set (policy OPTIONAL, the default),
 * frontend at localhost:3000.
 */
import { test, expect, type Page } from "@playwright/test";
import { createUser } from "./setup/api-helpers";
import { currentStep, totpCode, TOTP_STEP_SECONDS } from "./setup/totp";

const PASSWORD = "Test1234!";

async function signInWithPassword(page: Page, username: string) {
  await page.goto("/login");
  await page.getByTestId("input-username").fill(username);
  await page.getByTestId("input-password").fill(PASSWORD);
  await page.getByTestId("btn-sign-in").click();
}

test.describe("TS-0046 - MFA TOTP enrol and login", () => {
  test("enrols, then requires a TOTP at login", async ({ page }) => {
    test.setTimeout(120_000);
    const username = `mfa-${Date.now()}`;
    const created = await createUser(page, undefined, {
      name: username,
      password: PASSWORD,
      type: "EMPLEADO",
      active: true,
    });
    expect(created.ok, created.error ?? "createUser failed").toBe(true);

    await signInWithPassword(page, username);
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });

    await page.goto("/dashboard/security");
    await page.getByTestId("btn-mfa-start").click();
    const secret = (await page.getByTestId("mfa-secret").innerText()).trim();
    expect(secret).toMatch(/^[A-Z2-7]{16,}$/);
    await expect(page.getByTestId("mfa-otpauth-link")).toHaveAttribute("href", /^otpauth:\/\/totp\//);

    const confirmStep = currentStep();
    await page.getByTestId("input-mfa-confirm-code").fill(totpCode(secret, confirmStep));
    await page.getByTestId("btn-mfa-confirm").click();

    const recoveryItems = page.getByTestId("mfa-recovery-codes").getByRole("listitem");
    await expect(recoveryItems).toHaveCount(10);
    const recoveryCode = (await recoveryItems.first().innerText()).trim();
    await page.getByTestId("check-mfa-saved").check();
    await page.getByTestId("btn-mfa-done").click();
    await expect(page.getByTestId("mfa-recovery-codes")).toHaveCount(0);

    await page.getByTestId("btn-logout").click();
    await expect(page).toHaveURL(/\/login/, { timeout: 5000 });

    await signInWithPassword(page, username);
    await expect(page.getByTestId("input-mfa-code")).toBeVisible();
    await expect(page).toHaveURL(/\/login/);

    await page.getByTestId("input-mfa-code").fill("000000");
    await page.getByTestId("btn-mfa-verify").click();
    await expect(page.getByTestId("input-mfa-code")).toHaveValue("");
    await expect(page).toHaveURL(/\/login/);

    // Replay protection rejects a step at or below the one used to confirm.
    await expect
      .poll(() => currentStep(), { timeout: (TOTP_STEP_SECONDS + 5) * 1000, intervals: [1000] })
      .toBeGreaterThan(confirmStep);
    await page.getByTestId("input-mfa-code").fill(totpCode(secret));
    await page.getByTestId("btn-mfa-verify").click();
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });
    const cookies = await page.context().cookies();
    expect(cookies.some((c) => c.name === "notaire-auth-token" && c.httpOnly)).toBe(true);

    await page.goto("/dashboard/security");
    await page.getByTestId("toggle-mfa-disable-recovery").click();
    await page.getByTestId("input-mfa-disable-code").fill(recoveryCode);
    await page.getByTestId("btn-mfa-disable").click();
    await expect(page.getByTestId("mfa-disabled-notice")).toBeVisible();
  });
});
