/**
 * CU17 - Dar Alta Persona / CU18 - Dar Alta Cliente
 * Persona duplicate-document validation (issue #835).
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("Persona - validacion de documento duplicado", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/personas");
  });

  test("Golden path: create with a new document, then edit without changing it", async ({ page }) => {
    const apellido = `GoldenPath${Date.now() % 1000000}`;
    const dni = `77${Date.now() % 1000000}`;

    // When — create a persona with a document not yet registered
    await steps.whenUserClicksButton("nueva persona");
    await steps.thenModalIsVisible();
    let dialog = page.getByRole("dialog");
    await page.getByTestId("input-nombre").fill("Duplicado");
    await page.getByTestId("input-apellido").fill(apellido);
    await dialog.getByLabel(/dni/i).fill(dni);
    await steps.whenUserSubmitsForm();

    // Then
    await steps.thenShowsSuccessMessage("creada");

    // When — edit that same persona without changing its document
    await page.getByTestId("input-search-apellido").fill(apellido);
    await steps.thenTableIsVisible();
    const row = page.getByRole("row", { name: new RegExp(apellido, "i") });
    await row.getByRole("button").first().click();
    await steps.thenModalIsVisible();
    dialog = page.getByRole("dialog");
    await dialog.getByLabel(/email/i).fill("golden.path@test.com");
    await steps.whenUserSubmitsForm();

    // Then — the update succeeds because the document did not change
    await steps.thenShowsSuccessMessage("actualizada");
  });

  test("Edge path: creating with an already-registered document is blocked", async ({ page }) => {
    const dni = `88${Date.now() % 1000000}`;

    // Given — a persona already exists with this document
    await steps.whenUserClicksButton("nueva persona");
    await steps.thenModalIsVisible();
    const firstDialog = page.getByRole("dialog");
    await page.getByTestId("input-nombre").fill("Original");
    await page.getByTestId("input-apellido").fill("Existente");
    await firstDialog.getByLabel(/dni/i).fill(dni);
    await steps.whenUserSubmitsForm();
    await steps.thenShowsSuccessMessage("creada");

    // When — creating a second persona with the same document
    await steps.whenUserClicksButton("nueva persona");
    await steps.thenModalIsVisible();
    const secondDialog = page.getByRole("dialog");
    await page.getByTestId("input-nombre").fill("Otro");
    await page.getByTestId("input-apellido").fill("Duplicado");
    await secondDialog.getByLabel(/dni/i).fill(dni);
    await steps.whenUserSubmitsForm();

    // Then — the creation is rejected with a visible message and a link to the existing persona
    await steps.thenShowsErrorMessage("ya existe");
    await expect(page.locator("[data-sonner-toast]").getByText(/ver persona existente/i)).toBeVisible();

    // And — the form data is preserved (modal stays open, not lost)
    await expect(secondDialog).toBeVisible();
    await expect(page.getByTestId("input-nombre")).toHaveValue("Otro");
  });

  test("Edge path: editing towards another persona's document is blocked", async ({ page }) => {
    const dniA = `91${Date.now() % 1000000}`;
    const dniB = `92${Date.now() % 1000000}`;

    // Given — two personas exist with different documents
    await steps.whenUserClicksButton("nueva persona");
    await steps.thenModalIsVisible();
    let dialog = page.getByRole("dialog");
    await page.getByTestId("input-nombre").fill("Primera");
    await page.getByTestId("input-apellido").fill("Persona835");
    await dialog.getByLabel(/dni/i).fill(dniA);
    await steps.whenUserSubmitsForm();
    await steps.thenShowsSuccessMessage("creada");

    await steps.whenUserClicksButton("nueva persona");
    await steps.thenModalIsVisible();
    dialog = page.getByRole("dialog");
    await page.getByTestId("input-nombre").fill("Segunda");
    await page.getByTestId("input-apellido").fill("Persona835");
    await dialog.getByLabel(/dni/i).fill(dniB);
    await steps.whenUserSubmitsForm();
    await steps.thenShowsSuccessMessage("creada");

    // When — editing the second persona to use the first one's document
    await page.getByTestId("input-search-apellido").fill("Persona835");
    await steps.thenTableIsVisible();
    await page.getByRole("row", { name: /segunda/i }).getByRole("button").first().click();
    await steps.thenModalIsVisible();
    dialog = page.getByRole("dialog");
    await dialog.getByLabel(/dni/i).fill(dniA);
    await steps.whenUserSubmitsForm();

    // Then
    await steps.thenShowsErrorMessage("ya existe");
  });

  test("Responsive: duplicate error is visible at mobile, tablet and desktop viewports", async ({ page }) => {
    const dni = `93${Date.now() % 1000000}`;

    await steps.whenUserClicksButton("nueva persona");
    await steps.thenModalIsVisible();
    let dialog = page.getByRole("dialog");
    await page.getByTestId("input-nombre").fill("Viewport");
    await page.getByTestId("input-apellido").fill("Test");
    await dialog.getByLabel(/dni/i).fill(dni);
    await steps.whenUserSubmitsForm();
    await steps.thenShowsSuccessMessage("creada");

    for (const viewport of [
      { width: 320, height: 640 },
      { width: 768, height: 1024 },
      { width: 1280, height: 800 },
    ]) {
      await page.setViewportSize(viewport);
      await steps.whenUserClicksButton("nueva persona");
      await steps.thenModalIsVisible();
      dialog = page.getByRole("dialog");
      await page.getByTestId("input-nombre").fill("Otro");
      await page.getByTestId("input-apellido").fill("Viewport");
      await dialog.getByLabel(/dni/i).fill(dni);
      await steps.whenUserSubmitsForm();
      await steps.thenShowsErrorMessage("ya existe");
      await page.keyboard.press("Escape");
    }
  });
});
