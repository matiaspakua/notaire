import { readFileSync } from "node:fs";

import AxeBuilder from "@axe-core/playwright";
import { expect, type Page, test } from "@playwright/test";

interface Tokens {
  owner: { token: string };
  reader: { token: string };
}

const tokens = (): Tokens => JSON.parse(readFileSync(`${import.meta.dirname}/../../.e2e-factory/tokens.json`, "utf8"));

async function signIn(page: Page, token: string, workspace = "e2e") {
  await page.goto(`/#/${workspace}/overview`);
  await page.getByLabel("Token").fill(token);
  await page.getByRole("button", { name: "Sign in" }).click();
}

async function expectAccessible(page: Page) {
  const result = await new AxeBuilder({ page }).withTags(["wcag2a", "wcag2aa", "wcag21aa", "wcag22aa"]).analyze();
  const serious = result.violations.filter((v) => v.impact === "serious" || v.impact === "critical");
  expect(serious.map((v) => `${v.id}: ${v.nodes.map((n) => n.target.join(" ")).join(", ")}`)).toEqual([]);
}

test.use({ locale: "en-US" });

test("an owner sees the production line from the kit's evidence, accessibly in both themes", async ({ page }) => {
  await signIn(page, tokens().owner.token);
  await expect(page.getByRole("heading", { level: 1, name: "Production line" })).toBeVisible();
  await expect(page.getByRole("button", { name: "Queued: 1 runs" })).toBeVisible();
  await expect(page.getByText("No runner adapter reports; requested runs stay queued.")).toBeVisible();
  await expect(page.locator(".who")).toContainText("maria");
  await expectAccessible(page);
  await page.getByRole("button", { name: "Switch light or dark" }).click();
  await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
  await page.emulateMedia({ reducedMotion: "reduce" });
  await expectAccessible(page);
});

test("a writer creates a provider whose credential is an env reference, recorded with its actor", async ({ page }) => {
  await signIn(page, tokens().owner.token);
  await page.goto("/#/e2e/catalogs/providers");
  await page.getByRole("button", { name: "New entry" }).click();
  await page.getByLabel("Id", { exact: true }).fill("anthropic");
  await page.getByLabel("Summary").fill("Claude models");
  const values = page.getByLabel("Value", { exact: true });
  await values.nth(0).fill("claude-opus-5-5");
  await values.nth(1).fill("https://api.anthropic.com");
  await page.getByLabel("api_key (env:NAME)").fill("ANTHROPIC_API_KEY");
  await page.getByRole("button", { name: "Create entry" }).click();
  await expect(page.locator(".tag.env")).toContainText("ANTHROPIC_API_KEY");
  await expect(page.getByText("ui:maria")).toBeVisible();
});

test("a literal secret is refused with the kit's explanation", async ({ page }) => {
  await signIn(page, tokens().owner.token);
  await page.goto("/#/e2e/catalogs/integrations");
  await page.getByRole("button", { name: "New entry" }).click();
  await page.getByLabel("Id", { exact: true }).fill("ci");
  await page.getByLabel("Value", { exact: true }).first().fill("https://ci.example.invalid");
  await page.getByRole("button", { name: "Add field" }).click();
  await page.getByLabel("Field", { exact: true }).last().fill("note");
  await page.getByLabel("Value", { exact: true }).last().fill("Bearer abc.def");
  await page.getByRole("button", { name: "Create entry" }).click();
  await expect(page.getByRole("alert")).toContainText("env:NAME");
});

test("a reader gets read-only screens and no coach", async ({ page }) => {
  await signIn(page, tokens().reader.token);
  await expect(page.getByText("Read-only: your token's scope is read.")).toBeVisible();
  await page.goto("/#/e2e/catalogs/providers");
  await expect(page.getByRole("heading", { name: "providers" })).toBeVisible();
  await expect(page.getByRole("button", { name: "New entry" })).toHaveCount(0);
  await page.goto("/#/e2e/coach");
  await expect(page.getByText("only an owner token reads it")).toBeVisible();
  await expectAccessible(page);
});

test("a workspace whose kit is down says how to start it", async ({ page }) => {
  await page.goto("/#/offline/overview");
  await page.getByLabel("Token").fill("anything");
  await page.getByRole("button", { name: "Sign in" }).click();
  await expect(page.getByRole("alert")).toContainText("aisdlc serve --write");
});

test("the coach answers the owner and the screen speaks Spanish on request", async ({ page }) => {
  await signIn(page, tokens().owner.token);
  await page.goto("/#/e2e/coach");
  await expect(page.getByRole("heading", { name: "Sessions of this project" })).toBeVisible();
  await page.getByRole("button", { name: "Español" }).click();
  await expect(page.getByRole("heading", { level: 1, name: "Coach" })).toBeVisible();
  await expect(page.getByRole("heading", { name: "Sesiones de este proyecto" })).toBeVisible();
});

test.describe("phone width", () => {
  test.use({ viewport: { width: 360, height: 780 } });

  test("no horizontal scroll and every section reachable", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    for (const view of ["overview", "runs", "metrics", "projects", "catalogs", "knowledge", "coach"]) {
      await page.goto(`/#/e2e/${view}`);
      await page.waitForLoadState("networkidle");
      const overflow = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
      expect(overflow, view).toBeLessThanOrEqual(0);
    }
    await expectAccessible(page);
  });
});

test.describe("production line", () => {
  test("shows stations and gates between them, expanding reveals inputs and outputs", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    // The production line view shows stations from /factory/line.
    // Each station has a data-testid="station" attribute; gates between stations have data-testid="gate".
    await expect(page.getByRole("heading", { level: 1, name: "Production line" })).toBeVisible();
    // Stations are rendered — at least one must be visible
    const stations = page.getByTestId("station");
    await expect(stations.first()).toBeVisible();
    // Gates between stations show required or advisory label
    const gates = page.getByTestId("gate");
    if ((await gates.count()) > 0) {
      await expect(gates.first()).toBeVisible();
    }
    // Expanding a station reveals inputs and outputs
    await stations.first().click();
    const expanded = page.getByTestId("station-details");
    await expect(expanded).toBeVisible();
    // Inputs and outputs are listed
    await expect(page.getByTestId("stage-inputs")).toBeVisible();
    await expect(page.getByTestId("stage-outputs")).toBeVisible();
    await expectAccessible(page);
  });

  test("intent bar shows the intent title and drawer shows acceptance criteria and references", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    // The intent bar is always visible at the bottom; data-testid="intent-bar"
    const intentBar = page.getByTestId("intent-bar");
    await expect(intentBar).toBeVisible();
    // It shows the intent title from the seeded spec
    await expect(page.getByText("Dashboard Production Line")).toBeVisible();
    // Clicking opens the drawer with acceptance criteria
    await intentBar.click();
    const drawer = page.getByTestId("intent-drawer");
    await expect(drawer).toBeVisible();
    // Acceptance criteria are listed
    await expect(page.getByTestId("acceptance-criteria")).toBeVisible();
    await expect(page.getByText("Timeline shows stations from /factory/line with gates between them")).toBeVisible();
    // References are rendered as clickable links
    const refs = page.getByTestId("intent-references");
    await expect(refs).toBeVisible();
    const refLink = page.getByRole("link", { name: "Issue #2400" });
    await expect(refLink).toBeVisible();
    await expect(refLink).toHaveAttribute("href", "https://github.com/example/repo/issues/2400");
    await expectAccessible(page);
  });

  test("Projects view shows file tree and read-only viewer with selected file content", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    await page.goto("/#/e2e/projects");
    // File tree from /factory/config/files
    const fileTree = page.getByTestId("file-tree");
    await expect(fileTree).toBeVisible();
    // Click a file to open the viewer
    const firstFile = page.getByTestId("file-entry").first();
    await firstFile.click();
    // Read-only viewer with line numbers
    const viewer = page.getByTestId("file-viewer");
    await expect(viewer).toBeVisible();
    // Line numbers are present
    await expect(page.getByTestId("line-number")).toBeVisible();
    // Content is read-only (no editable textarea)
    const editable = page.locator("textarea, [contenteditable=true]");
    await expect(editable).toHaveCount(0);
    await expectAccessible(page);
  });

  test("Configuration delete asks for inline confirmation and removes the item", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    await page.goto("/#/e2e/catalogs/prompts");
    // Find the throwaway-e2e entry
    const entry = page.getByTestId("catalog-entry-throwaway-e2e");
    await expect(entry).toBeVisible();
    // Click delete — inline confirmation appears (no browser dialog)
    await page.getByTestId("delete-button").click();
    // Inline confirmation dialog
    const confirmDialog = page.getByTestId("delete-confirmation");
    await expect(confirmDialog).toBeVisible();
    // Confirm the deletion
    await page.getByRole("button", { name: "Delete" }).click();
    // Entry is removed
    await expect(entry).not.toBeVisible();
    await expectAccessible(page);
  });

  test("Metrics view renders a telemetry panel", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    await page.goto("/#/e2e/metrics");
    // Telemetry panel with tabs for group dimensions
    const telemetryPanel = page.getByTestId("telemetry-panel");
    await expect(telemetryPanel).toBeVisible();
    // Tabs for different group dimensions
    await expect(page.getByTestId("telemetry-tab-stage")).toBeVisible();
    // Telemetry rows with key, runs, duration, cost, failures
    const rows = page.getByTestId("telemetry-row");
    if ((await rows.count()) > 0) {
      await expect(rows.first()).toBeVisible();
    }
    await expectAccessible(page);
  });

  test("live indicator is present in the sidebar", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    // The live indicator dot is in the sidebar footer; data-testid="live-indicator"
    const liveIndicator = page.getByTestId("live-indicator");
    await expect(liveIndicator).toBeVisible();
    await expectAccessible(page);
  });
});
