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
  await expect(page.getByTestId("station").first()).toBeVisible();
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
  test("draws the line with the run at its station, gate evidence and the correction loop", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    await expect(page.getByRole("heading", { level: 1, name: "Production line" })).toBeVisible();
    const stations = page.getByTestId("station");
    await expect(stations).toHaveCount(4);
    expect(await stations.evaluateAll((els) => els.map((e) => e.getAttribute("data-stage")))).toEqual([
      "plan", "red", "pre-push", "signal",
    ]);
    await expect(page.locator('[data-stage="plan"][data-testid="station"]')).toHaveAttribute("data-state", "done");
    await expect(page.locator('[data-stage="red"][data-testid="station"]')).toHaveAttribute("data-state", "running");
    await expect(page.locator('[data-stage="red"][data-testid="station"]')).toContainText("Running");
    await expect(page.getByTestId("line-token")).toHaveAttribute("data-stage", "red");
    await expect(page.getByTestId("intent-strip")).toContainText("1 of 4 stations complete");
    await expect(page.getByTestId("intent-strip")).toContainText("No intent attached to this run");
    await expect(page.locator('[data-testid="gate"][data-state="passed"]').filter({ hasText: "plan check" })).toBeVisible();
    await expect(page.locator('[data-testid="gate"].advisory').filter({ hasText: "lint check" })).toContainText("Not run");
    await expect(page.getByTestId("correction-loop")).toContainText("One correction per blocker");
    await expect(page.getByText("AI agent").first()).toBeVisible();
    await expectAccessible(page);
  });

  test("a station is chosen with the keyboard and its detail lists agents, I/O and gates", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    await expect(page.getByTestId("station-details")).toContainText("red");
    await page.locator('[data-stage="red"][data-testid="station"]').focus();
    await page.keyboard.press("ArrowRight");
    await expect(page.locator('[data-stage="pre-push"][data-testid="station"]')).toBeFocused();
    await page.keyboard.press("Enter");
    const detail = page.getByTestId("station-details");
    await expect(detail.getByRole("heading", { name: "pre-push" })).toBeVisible();
    await expect(detail).toContainText("implementer");
    await expect(page.getByTestId("stage-inputs")).toContainText("failing tests");
    await expect(page.getByTestId("stage-outputs")).toContainText("passing tests");
    await expect(page.locator('[data-stage="pre-push"][data-testid="station"]')).toHaveAttribute("aria-pressed", "true");
  });

  test("on a phone the line is vertical and the page does not scroll sideways", async ({ page }) => {
    await page.setViewportSize({ width: 320, height: 900 });
    await signIn(page, tokens().owner.token);
    const plan = await page.locator('[data-stage="plan"][data-testid="station"]').boundingBox();
    const red = await page.locator('[data-stage="red"][data-testid="station"]').boundingBox();
    expect(red!.y).toBeGreaterThan(plan!.y + plan!.height - 1);
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
    expect(overflow).toBeLessThanOrEqual(0);
    await expectAccessible(page);
  });

  test("intent bar shows the intent title and drawer shows acceptance criteria and references", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    const intentBar = page.getByTestId("intent-bar");
    await expect(intentBar).toBeVisible();
    await expect(page.getByText("Dashboard Production Line")).toBeVisible();
    await intentBar.click();
    await expect(page.getByTestId("intent-drawer")).toBeVisible();
    await expect(page.getByTestId("acceptance-criteria")).toBeVisible();
    await expect(page.getByText("Timeline shows stations from /factory/line with gates between them")).toBeVisible();
    await expect(page.getByTestId("intent-references")).toBeVisible();
    const refLink = page.getByRole("link", { name: "Issue #2400" });
    await expect(refLink).toBeVisible();
    await expect(refLink).toHaveAttribute("href", "https://github.com/example/repo/issues/2400");
    await expectAccessible(page);
  });

  test("Projects view shows file tree and read-only viewer with selected file content", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    await page.goto("/#/e2e/projects");
    await expect(page.getByTestId("file-tree")).toBeVisible();
    await page.getByTestId("file-entry").first().click();
    await expect(page.getByTestId("file-viewer")).toBeVisible();
    await expect(page.getByTestId("line-number")).toBeVisible();
    await expect(page.locator("textarea, [contenteditable=true]")).toHaveCount(0);
    await expectAccessible(page);
  });

  test("Configuration delete asks for inline confirmation and removes the item", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    await page.goto("/#/e2e/catalogs/prompts");
    const entry = page.getByTestId("catalog-entry-throwaway-e2e");
    await expect(entry).toBeVisible();
    await entry.getByTestId("delete-button").click();
    const confirmDialog = page.getByTestId("delete-confirmation");
    await expect(confirmDialog).toBeVisible();
    await confirmDialog.getByRole("button", { name: "Delete" }).click();
    await expect(entry).not.toBeVisible();
    await expectAccessible(page);
  });

  test("Metrics view renders a telemetry panel", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    await page.goto("/#/e2e/metrics");
    await expect(page.getByTestId("telemetry-panel")).toBeVisible();
    await expect(page.getByTestId("telemetry-tab-stage")).toBeVisible();
    const rows = page.getByTestId("telemetry-row");
    expect(await rows.count()).toBeGreaterThanOrEqual(1);
    await expect(rows.first()).toBeVisible();
    await expect(rows.filter({ hasText: "plan" }).first()).toBeVisible();
    await expectAccessible(page);
  });

  test("live indicator is present in the sidebar", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    await expect(page.getByTestId("live-indicator")).toBeVisible();
    await expectAccessible(page);
  });
});

test.describe("collapsible sidebar (#2434)", () => {
  test.use({ viewport: { width: 1280, height: 800 } });

  test("collapses to an icon rail, keeps names, and remembers the choice", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    const toggle = page.getByRole("button", { name: "Collapse navigation" });
    await expect(toggle).toHaveAttribute("aria-expanded", "true");
    const sidebar = page.locator(".sidebar");
    const wide = (await sidebar.boundingBox())!.width;
    await toggle.click();
    const expand = page.getByRole("button", { name: "Expand navigation" });
    await expect(expand).toHaveAttribute("aria-expanded", "false");
    await expect(page.locator(".app-shell")).toHaveAttribute("data-sidebar", "collapsed");
    await expect.poll(async () => (await sidebar.boundingBox())!.width).toBeLessThan(wide / 2);
    const runs = page.getByRole("link", { name: "Runs" });
    await expect(runs).toBeVisible();
    await expect(runs).toHaveAttribute("title", "Runs");
    await expect(page.getByRole("link", { name: "Production line" })).toHaveAttribute("aria-current", "page");
    await expectAccessible(page);
    await page.reload();
    await expect(page.getByRole("button", { name: "Expand navigation" })).toBeVisible();
    await page.getByRole("button", { name: "Expand navigation" }).focus();
    await page.keyboard.press("Enter");
    await expect(page.getByRole("button", { name: "Collapse navigation" })).toHaveAttribute("aria-expanded", "true");
    await expect(page.locator(".app-shell")).toHaveAttribute("data-sidebar", "expanded");
  });

  test("collapsed in dark mode stays accessible", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    await page.getByRole("button", { name: "Collapse navigation" }).click();
    await page.getByRole("button", { name: "Switch light or dark" }).click();
    await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
    await expectAccessible(page);
  });
});

test.describe("collapsible sidebar on a phone (#2434)", () => {
  test.use({ viewport: { width: 375, height: 780 } });

  test("the toggle is hidden where the sidebar is a top bar", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    await expect(page.getByTestId("station").first()).toBeVisible();
    await expect(page.getByRole("button", { name: "Collapse navigation" })).toBeHidden();
  });
});

test.describe("step inputs and outputs (#2435)", () => {
  test("a station shows what its step received and produced, with repository links", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    await page.locator('[data-stage="plan"][data-testid="station"]').click();
    const io = page.getByTestId("step-io");
    await expect(io).toHaveCount(1);
    await expect(io).toContainText("plan");
    const received = io.getByRole("region", { name: "Received" });
    await expect(received).toContainText("Add a checkout flow");
    await received.getByText("Prompt").click();
    await expect(received.getByText("Write the OpenSpec change for the checkout flow.")).toBeVisible();
    await expect(received.getByRole("link", { name: "docs/checkout.md" }))
      .toHaveAttribute("href", "https://example.invalid/web/blob/HEAD/docs/checkout.md");
    const issue = received.getByRole("link", { name: "Issue #12" });
    await expect(issue).toHaveAttribute("target", "_blank");
    await expect(issue).toHaveAttribute("rel", /noopener/);
    const produced = io.getByRole("region", { name: "Produced" });
    await expect(produced).toContainText("OpenSpec change written");
    await expect(produced.getByRole("link", { name: "0123abcd" }))
      .toHaveAttribute("href", "https://example.invalid/web/commit/0123abcd");
    await expect(produced.getByRole("link", { name: "docs/openspec/changes/checkout/proposal.md" }))
      .toHaveAttribute("href", "https://example.invalid/web/blob/0123abcd/docs/openspec/changes/checkout/proposal.md");
    await expectAccessible(page);
  });

  test("a running step shows its inputs and says nothing was produced yet", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    await page.locator('[data-stage="red"][data-testid="station"]').click();
    const io = page.getByTestId("step-io");
    await expect(io.getByRole("region", { name: "Received" })).toContainText("Prompt");
    await expect(io.getByRole("region", { name: "Produced" })).toContainText("Nothing recorded yet");
  });

  test("a station no step reached says so", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    await page.locator('[data-stage="signal"][data-testid="station"]').click();
    await expect(page.getByTestId("step-io-empty")).toBeVisible();
  });
});

test.describe("factory metrics (#2447)", () => {
  test("the Metrics view answers time, cost and RepoLens per intent", async ({ page }) => {
    await signIn(page, tokens().owner.token);
    await page.goto("/#/e2e/metrics");
    const intents = page.getByTestId("intent-metrics");
    await expect(intents.getByRole("heading", { name: "Time, cost and RepoLens per intent" })).toBeVisible();
    const row = intents.getByRole("row", { name: /Checkout totals/ });
    await expect(row).toContainText("$0.42");
    await expect(row).toContainText("1,500");
    await expect(page.getByTestId("factory-overall")).toContainText("$0.42");
    await expect(page.getByTestId("repolens-effect")).toBeVisible();
    await expectAccessible(page);
  });
});

