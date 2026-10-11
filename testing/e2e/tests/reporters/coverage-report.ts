/**
 * Custom Playwright reporter: Business Coverage Report
 *
 * Generates an HTML report mapping test results to the 68 CU matrix.
 * Each test's tags (@cu-XX) are matched to CU entries, and pass/fail/skip
 * status is tracked for business-level coverage reporting.
 *
 * Usage in playwright.config.ts:
 *   reporter: [
 *     'html',
 *     ['./tests/reporters/coverage-report.ts', { outputFile: 'test-results/coverage-report.html' }]
 *   ]
 */

import type {
  FullConfig,
  FullResult,
  Suite,
  TestCase,
  TestResult,
  Reporter,
} from "@playwright/test/reporter";

/**
 * CU matrix entry extracted from CU-API-MATRIX.csv
 */
interface CuEntry {
  id: string;
  name: string;
  module: string;
  priority: string;
}

/**
 * Test result for a specific CU
 */
interface CuTestResult {
  cuId: string;
  cuName: string;
  module: string;
  status: "pass" | "fail" | "skip" | "not-run";
  duration: number;
  testTitle: string;
  testFile: string;
  error?: string;
}

/**
 * CU matrix data — canonical source of truth
 */
const CU_MATRIX: CuEntry[] = [
  { id: "CU01", name: "Prepare a budget", module: "Budgets", priority: "Essential" },
  { id: "CU02", name: "Start a management", module: "Managements", priority: "Essential" },
  { id: "CU03", name: "List required documents and certificates", module: "Managements", priority: "Essential" },
  { id: "CU04", name: "Record client documentation", module: "Managements", priority: "Essential" },
  { id: "CU05", name: "Prepare a deed", module: "Managements", priority: "Essential" },
  { id: "CU06", name: "Sign a deed", module: "Managements", priority: "Essential" },
  { id: "CU07", name: "Generate a testimony", module: "Managements", priority: "Essential" },
  { id: "CU08", name: "Verify a testimony", module: "Managements", priority: "Necessary" },
  { id: "CU09", name: "Record client document debts", module: "Managements", priority: "Essential" },
  { id: "CU10", name: "Record external-entity document movements", module: "Managements", priority: "Essential" },
  { id: "CU11", name: "Enter for registration", module: "Managements", priority: "Essential" },
  { id: "CU12", name: "Withdraw a testimony", module: "Managements", priority: "Necessary" },
  { id: "CU13", name: "View management history", module: "Managements", priority: "Nice to have" },
  { id: "CU14", name: "Look up management status", module: "Managements", priority: "Necessary" },
  { id: "CU15", name: "Process a payment", module: "Payments", priority: "Nice to have" },
  { id: "CU16", name: "Archive a management", module: "Managements", priority: "Necessary" },
  { id: "CU17", name: "Register a person", module: "Clients", priority: "Essential" },
  { id: "CU18", name: "Register a client", module: "Clients", priority: "Essential" },
  { id: "CU19", name: "Search a client's managements", module: "Clients", priority: "Necessary" },
  { id: "CU20", name: "Create a user", module: "Administration", priority: "Essential" },
  { id: "CU21", name: "Update a user account", module: "Administration", priority: "Essential" },
  { id: "CU22", name: "Register a substitution", module: "Administration", priority: "Essential" },
  { id: "CU23", name: "View user activity log", module: "Administration", priority: "Necessary" },
  { id: "CU24", name: "Generate the index book", module: "Protocols", priority: "Nice to have" },
  { id: "CU25", name: "Generate the monthly sworn statement", module: "Protocols", priority: "Nice to have" },
  { id: "CU26", name: "Enter a new procedure type", module: "Administration", priority: "Essential" },
  { id: "CU27", name: "Enter a new document type", module: "Administration", priority: "Essential" },
  { id: "CU28", name: "Enter new folios", module: "Protocols", priority: "Essential" },
  { id: "CU29", name: "Enter a new concept", module: "Administration", priority: "Essential" },
  { id: "CU30", name: "Enter a new management status", module: "Administration", priority: "Essential" },
  { id: "CU31", name: "Update a procedure type", module: "Administration", priority: "Essential" },
  { id: "CU32", name: "Update a document type", module: "Administration", priority: "Essential" },
  { id: "CU33", name: "Update a folio", module: "Protocols", priority: "Essential" },
  { id: "CU34", name: "Update a concept", module: "Administration", priority: "Essential" },
  { id: "CU35", name: "Update a management status", module: "Administration", priority: "Essential" },
  { id: "CU36", name: "Enter folio types", module: "Administration", priority: "Essential" },
  { id: "CU37", name: "Delete a concept", module: "Administration", priority: "Essential" },
  { id: "CU38", name: "Delete a document type", module: "Administration", priority: "Essential" },
  { id: "CU39", name: "Create a budget template", module: "Administration", priority: "Essential" },
  { id: "CU40", name: "Update a folio type", module: "Administration", priority: "Essential" },
  { id: "CU41", name: "Update a client", module: "Clients", priority: "Essential" },
  { id: "CU42", name: "Report upcoming expirations", module: "Managements", priority: "Necessary" },
  { id: "CU43", name: "Re-enter documentation", module: "Managements", priority: "Essential" },
  { id: "CU44", name: "Re-enter a testimony", module: "Managements", priority: "Essential" },
  { id: "CU45", name: "Update a budget", module: "Budgets", priority: "Essential" },
  { id: "CU46", name: "View client details", module: "Clients", priority: "Essential" },
  { id: "CU47", name: "Look up a payment", module: "Payments", priority: "Nice to have" },
  { id: "CU48", name: "Register a notary", module: "Administration", priority: "Essential" },
  { id: "CU49", name: "Delete a budget template", module: "Administration", priority: "Essential" },
  { id: "CU50", name: "Generate the tax sworn statement", module: "Protocols", priority: "Nice to have" },
  { id: "CU51", name: "Update a notary", module: "Administration", priority: "Essential" },
  { id: "CU52", name: "Update a deed", module: "Managements", priority: "Essential" },
  { id: "CU53", name: "Update a management", module: "Managements", priority: "Essential" },
  { id: "CU54", name: "Update a person", module: "Clients", priority: "Essential" },
  { id: "CU55", name: "Update a budget template", module: "Administration", priority: "Essential" },
  { id: "CU56", name: "Record a registration", module: "Managements", priority: "Essential" },
  { id: "CU57", name: "Delete a procedure type", module: "Administration", priority: "Essential" },
  { id: "CU58", name: "Delete a folio type", module: "Administration", priority: "Essential" },
  { id: "CU59", name: "Look up substitutions", module: "Administration", priority: "Necessary" },
  { id: "CU60", name: "Search for a budget", module: "Budgets", priority: "Necessary" },
  { id: "CU61", name: "Search for a person or client", module: "Clients", priority: "Necessary" },
  { id: "CU62", name: "Search for a deed", module: "Managements", priority: "Necessary" },
  { id: "CU63", name: "Search folios", module: "Protocols", priority: "Necessary" },
  { id: "CU64", name: "Search procedure types", module: "Administration", priority: "Necessary" },
  { id: "CU65", name: "Search document types", module: "Administration", priority: "Necessary" },
  { id: "CU66", name: "Search concepts", module: "Administration", priority: "Necessary" },
  { id: "CU67", name: "Search management statuses", module: "Administration", priority: "Necessary" },
  { id: "CU68", name: "Search folio types", module: "Administration", priority: "Necessary" },
];

/**
 * Extract CU IDs from test tags (e.g., @cu-01, @cu-02)
 */
function extractCuTags(tags: string[]): string[] {
  return tags
    .filter((t) => /^cu-\d{2}$/i.test(t))
    .map((t) => `CU${t.match(/\d{2}/)![0]}`);
}

/**
 * Extract module tag (e.g., @module-budgets)
 */
function extractModuleTag(tags: string[]): string | null {
  const match = tags.find((t) => /^module-/i.test(t));
  return match ? match.replace(/^module-/i, "") : null;
}

/**
 * Compute coverage stats
 */
interface CoverageStats {
  total: number;
  passed: number;
  failed: number;
  skipped: number;
  notRun: number;
  passRate: number;
  coveragePct: number;
  byModule: Record<string, { total: number; pass: number; fail: number }>;
  byPriority: Record<string, { total: number; pass: number }>;
}

class CoverageReport implements Reporter {
  private results: Map<string, CuTestResult> = new Map();
  private outputFile: string;
  private startTime: number = 0;

  constructor(options: { outputFile?: string } = {}) {
    this.outputFile = options.outputFile || "test-results/coverage-report.html";
  }

  onBegin(config: FullConfig, suite: Suite): void {
    this.startTime = Date.now();
    // Initialize all CU as "not-run"
    for (const cu of CU_MATRIX) {
      this.results.set(cu.id, {
        cuId: cu.id,
        cuName: cu.name,
        module: cu.module,
        status: "not-run",
        duration: 0,
        testTitle: "",
        testFile: "",
      });
    }
  }

  onTestEnd(test: TestCase, result: TestResult): void {
    const tags = test.tags || [];
    const cuIds = extractCuTags(tags);

    for (const cuId of cuIds) {
      const existing = this.results.get(cuId);
      if (!existing) continue;

      // Don't overwrite a failure with a pass from another test
      if (existing.status === "fail") continue;

      const status = result.status === "passed" ? "pass"
        : result.status === "failed" ? "fail"
        : result.status === "skipped" ? "skip"
        : "fail";

      this.results.set(cuId, {
        ...existing,
        status,
        duration: result.duration,
        testTitle: test.title,
        testFile: test.location?.file || "",
        error: result.error?.message,
      });
    }
  }

  onEnd(result: FullResult): Promise<void> {
    const stats = this.computeStats();
    const html = this.generateHtml(stats);
    const fs = require("fs");
    const dir = require("path").dirname(this.outputFile);
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true });
    }
    fs.writeFileSync(this.outputFile, html, "utf8");
    console.log(`[coverage-report] Generated: ${this.outputFile}`);
    return Promise.resolve();
  }

  private computeStats(): CoverageStats {
    const total = CU_MATRIX.length;
    let passed = 0;
    let failed = 0;
    let skipped = 0;
    let notRun = 0;

    const byModule: Record<string, { total: number; pass: number; fail: number }> = {};
    const byPriority: Record<string, { total: number; pass: number }> = {};

    for (const cu of CU_MATRIX) {
      const r = this.results.get(cu.id)!;
      if (!byModule[cu.module]) byModule[cu.module] = { total: 0, pass: 0, fail: 0 };
      if (!byPriority[cu.priority]) byPriority[cu.priority] = { total: 0, pass: 0 };

      byModule[cu.module].total++;
      byPriority[cu.priority].total++;

      switch (r.status) {
        case "pass":
          passed++;
          byModule[cu.module].pass++;
          byPriority[cu.priority].pass++;
          break;
        case "fail":
          failed++;
          byModule[cu.module].fail++;
          break;
        case "skip":
          skipped++;
          break;
        case "not-run":
          notRun++;
          break;
      }
    }

    const coveragePct = total > 0 ? Math.round(((passed + failed) / total) * 100) : 0;
    const passRate = total > 0 ? Math.round((passed / total) * 100) : 0;

    return { total, passed, failed, skipped, notRun, passRate, coveragePct, byModule, byPriority };
  }

  private generateHtml(stats: CoverageStats): string {
    const rows = CU_MATRIX.map((cu) => {
      const r = this.results.get(cu.id)!;
      const statusIcon =
        r.status === "pass" ? "✅" :
        r.status === "fail" ? "❌" :
        r.status === "skip" ? "⏭️" : "⚪";
      const statusClass = r.status;
      return `
        <tr class="${statusClass}">
          <td>${cu.id}</td>
          <td>${cu.name}</td>
          <td>${cu.module}</td>
          <td>${cu.priority}</td>
          <td class="status-${statusClass}">${statusIcon} ${r.status.toUpperCase()}</td>
          <td>${r.duration > 0 ? (r.duration / 1000).toFixed(1) + "s" : "-"}</td>
          <td><small>${r.testTitle || "-"}</small></td>
        </tr>`;
    }).join("\n");

    const moduleRows = Object.entries(stats.byModule)
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([mod, m]) => `
        <tr>
          <td>${mod}</td>
          <td>${m.total}</td>
          <td class="pass">${m.pass}</td>
          <td class="fail">${m.fail}</td>
          <td>${m.total > 0 ? Math.round((m.pass / m.total) * 100) : 0}%</td>
        </tr>`).join("\n");

    const priorityRows = Object.entries(stats.byPriority)
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([priority, g]) => `
        <tr>
          <td>${priority}</td>
          <td>${g.total}</td>
          <td class="pass">${g.pass}</td>
          <td>${g.total > 0 ? Math.round((g.pass / g.total) * 100) : 0}%</td>
        </tr>`).join("\n");

    const duration = ((Date.now() - this.startTime) / 1000).toFixed(0);

    return `<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Notaire E2E — Business Coverage Report</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      padding: 2rem;
      background: #f5f5f7;
      color: #1d1d1f;
    }
    h1 { font-size: 1.8rem; margin-bottom: 0.5rem; }
    h2 { font-size: 1.3rem; margin: 1.5rem 0 0.75rem; color: #555; }
    p { color: #666; margin-bottom: 1.5rem; }
    .summary-cards {
      display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
      gap: 1rem; margin-bottom: 2rem;
    }
    .card {
      background: white; border-radius: 12px; padding: 1rem;
      box-shadow: 0 1px 3px rgba(0,0,0,0.08); text-align: center;
    }
    .card .number { font-size: 2rem; font-weight: 700; }
    .card .label { font-size: 0.8rem; color: #888; margin-top: 0.25rem; }
    .card.pass { border-left: 4px solid #30d158; }
    .card.fail { border-left: 4px solid #ff453a; }
    .card.warn { border-left: 4px solid #ff9f0a; }
    table { width: 100%; border-collapse: collapse; background: white;
      border-radius: 12px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.08);
      margin-bottom: 1.5rem; font-size: 0.9rem;
    }
    th { background: #f5f5f7; text-align: left; padding: 0.75rem 1rem;
      font-weight: 600; border-bottom: 2px solid #e5e5ea; }
    td { padding: 0.6rem 1rem; border-bottom: 1px solid #f0f0f2; }
    tr:hover td { background: #f9f9fb; }
    .status-pass { color: #30d158; }
    .status-fail { color: #ff453a; }
    .status-skip { color: #ff9f0a; }
    .status-not-run { color: #8e8e93; }
    .pass { color: #30d158; font-weight: 600; }
    .fail { color: #ff453a; font-weight: 600; }
    .footer { text-align: center; color: #8e8e93; font-size: 0.8rem; margin-top: 2rem; }
  </style>
</head>
<body>
  <h1>📊 Notaire E2E Business Coverage Report</h1>
  <p>Generated ${new Date().toISOString().split("T")[0]} | Duration: ${duration}s</p>

  <div class="summary-cards">
    <div class="card pass">
      <div class="number">${stats.passed}</div>
      <div class="label">Passed</div>
    </div>
    <div class="card fail">
      <div class="number">${stats.failed}</div>
      <div class="label">Failed</div>
    </div>
    <div class="card warn">
      <div class="number">${stats.skipped}</div>
      <div class="label">Skipped</div>
    </div>
    <div class="card">
      <div class="number">${stats.notRun}</div>
      <div class="label">Not Run</div>
    </div>
    <div class="card">
      <div class="number">${stats.passRate}%</div>
      <div class="label">Pass Rate</div>
    </div>
    <div class="card">
      <div class="number">${stats.coveragePct}%</div>
      <div class="label">CU Coverage</div>
    </div>
  </div>

  <h2>📋 CU Coverage by Module</h2>
  <table>
    <thead><tr><th>Module</th><th>Total</th><th>Pass</th><th>Fail</th><th>Rate</th></tr></thead>
    <tbody>${moduleRows}</tbody>
  </table>

  <h2>🏷️ Coverage by Priority</h2>
  <table>
    <thead><tr><th>Priority</th><th>Total</th><th>Pass</th><th>Rate</th></tr></thead>
    <tbody>${priorityRows}</tbody>
  </table>

  <h2>📋 Detailed CU Matrix (${stats.total} Use Cases)</h2>
  <table>
    <thead>
      <tr><th>CU</th><th>Name</th><th>Module</th><th>Priority</th><th>Status</th><th>Duration</th><th>Test</th></tr>
    </thead>
    <tbody>${rows}</tbody>
  </table>

  <div class="footer">
    Generated by Playwright Coverage Reporter | Issue #400
  </div>
</body>
</html>`;
  }
}

export default CoverageReport;
