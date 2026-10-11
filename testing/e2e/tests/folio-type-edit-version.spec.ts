/**
 * Issue #655, Owner decision (Oct 9): a stale version answers 409. The folio-type edit
 * dialog sent only { name, isAuxiliary }, so the backend read version 0: the first edit
 * worked, every later edit of the same folio type failed (500, now 409), and notes and
 * enabled were reset. The dialog now sends the loaded row with the edited fields.
 * CU64 — Folio types.
 */
import { test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import { apiGet, apiPost } from "./setup/api-helpers";

test.describe("CU64 — Tipos de folio: editing twice keeps working", () => {
  test("second edit of the same folio type succeeds and keeps notes and enabled", async ({ page }) => {
    await authenticateAsAdmin(page);
    const name = `TF655-${Date.now()}`;
    const created = await apiPost<{ idFolioType: number }>(page, "/folio-types", {
      name,
      isAuxiliary: false,
      enabled: true,
      notes: "keep me",
    });
    expect(created.ok).toBeTruthy();
    const id = created.data!.idFolioType;

    await page.goto("/dashboard/administration/folios");
    await page.getByTestId("input-search-folio-type").fill(name);

    for (const suffix of ["-a", "-b"]) {
      await page.getByRole("row", { name: new RegExp(name) }).getByTestId("btn-edit-folio-type").click();
      await page.getByTestId("input-name-folio-type").fill(name + suffix);
      const put = page.waitForResponse(
        (r) => r.url().includes(`/api/v1/folio-types/${id}`) && r.request().method() === "PUT",
      );
      await page.getByTestId("btn-save-folio-type").click();
      expect((await put).status()).toBe(200);
      await expect(page.getByRole("dialog")).not.toBeVisible();
    }

    const stored = await apiGet<{ name: string; notes: string; enabled: boolean }>(page, `/folio-types/${id}`);
    expect(stored.data?.name).toBe(`${name}-b`);
    expect(stored.data?.notes).toBe("keep me");
    expect(stored.data?.enabled).toBe(true);
  });
});
