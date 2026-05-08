/**
 * Debug test to diagnose toast and field fill issues
 */
import { test, expect } from "@playwright/test";
import * as fs from "fs";

test.describe("Debug", () => {
  test("Debug presupuesto label and form fill", async ({ page }) => {
    const log: string[] = [];
    function debug(...args: unknown[]) {
      const msg = args.map((a) => (typeof a === "object" ? JSON.stringify(a, null, 2) : String(a))).join(" ");
      log.push(msg);
      console.log(msg);
    }

    // Login
    await page.goto("/login");
    await page.waitForLoadState("networkidle");
    await page.getByTestId("input-usuario").fill("admin");
    await page.getByTestId("input-contrasenia").fill("admin");
    await page.getByTestId("btn-ingresar").click();
    await page.waitForURL(/dashboard/);
    debug("Step 1: Logged in");

    // Go to presupuestos
    await page.goto("/dashboard/presupuestos");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(2000);
    debug("Step 2: On presupuestos page");

    // Check page content
    const bodyText = await page.evaluate(() => document.body.innerText);
    debug("Page text contains 'Presupuestos':", bodyText.includes("Presupuestos"));

    // Click new presupuesto
    await page.getByTestId("btn-nuevo-presupuesto").click();
    await page.waitForTimeout(2000);
    debug("Step 3: Clicked new presupuesto");

    // Debug what labels exist
    const labels = await page.evaluate(() =>
      Array.from(document.querySelectorAll("label")).map((l) => ({
        text: l.innerText,
        html: l.innerHTML.slice(0, 300),
      }))
    );
    debug("Labels found:", labels);

    // Debug getByLabel
    const fechaInputs = await page.getByLabel(/Fecha/i).count();
    debug("Found by label /Fecha/i:", fechaInputs);
    const montoInputs = await page.getByLabel(/Monto/i).count();
    debug("Found by label /Monto/i:", montoInputs);

    // Try to fill date
    if (fechaInputs > 0) {
      const fechaField = page.getByLabel(/Fecha/i);
      const tagName = await fechaField.evaluate((el) => el.tagName);
      debug("Fecha field tag name:", tagName);
      await fechaField.fill("2026-05-08");
      debug("Step 4: Filled fecha");
    }

    if (montoInputs > 0) {
      const montoField = page.getByLabel(/Monto/i);
      const tagName = await montoField.evaluate((el) => el.tagName);
      debug("Monto field tag name:", tagName);
      await montoField.fill(String(5000));
      debug("Step 4b: Filled monto");
    }

    // Screenshot after filling
    await page.screenshot({ path: "/tmp/debug-presupuesto-filled.png" });

    // Submit
    const submitBtn = page.getByRole("button", { name: /crear/i });
    debug("Submit button count:", await submitBtn.count());
    debug("Submit button visible:", await submitBtn.isVisible().catch(() => false));
    if (await submitBtn.isVisible().catch(() => false)) {
      await submitBtn.click();
      debug("Step 5: Submitted form");

      // Debug network responses
      const [response] = await Promise.all([
        page.waitForResponse((res) => res.url().includes("/api/v1/presupuestos") && res.request().method() === "POST"),
        page.waitForTimeout(1000),
      ]);
      if (response) {
        debug("POST /api/v1/presupuestos status:", response.status());
        try {
          const respBody = await response.text();
          debug("Response body:", respBody);
        } catch {
          debug("Could not read response body");
        }
      }

      await page.waitForTimeout(3000);

      // Check page content after submit
      const pageText = await page.evaluate(() => document.body.innerText);
      debug("Page text after submit (first 1500 chars):", pageText.substring(0, 1500));

      // Check for toast elements
      const toastElements = await page.evaluate(() => {
        const allEls = Array.from(document.querySelectorAll("*"));
        return allEls
          .filter((el) => {
            const text = el.textContent || "";
            return (
              (text.includes("creado") || text.includes("Error") || text.includes("éxito")) &&
              el.children.length === 0
            );
          })
          .map((el) => ({
            tag: el.tagName,
            text: el.textContent?.substring(0, 100),
            className: el.className?.substring(0, 100),
          }));
      });
      debug("Text elements with 'creado'/'Error' etc:", toastElements);

      // Check for sonner toaster
      const toasterDiv = await page.evaluate(() => {
        const allDivs = document.querySelectorAll("div");
        const results: Array<{ className: string; text: string }> = [];
        allDivs.forEach((d) => {
          if (d.className && d.className.includes && d.className.includes("sonner")) {
            results.push({ className: d.className, text: d.textContent?.substring(0, 200) || "" });
          }
        });
        return results;
      });
      debug("Sonner toaster elements:", toasterDiv);

      await page.screenshot({ path: "/tmp/debug-presupuesto-submitted.png" });
    }

    // Write debug log to file
    fs.writeFileSync("/tmp/debug-log.txt", log.join("\n"));
    debug("Debug log written to /tmp/debug-log.txt");
  });
});
