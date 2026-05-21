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
 *     ['./tests/e2e/reporters/coverage-report.ts', { outputFile: 'test-results/coverage-report.html' }]
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
  grado: string;
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
  { id: "CU01", name: "Preparar Presupuesto", module: "Presupuestos", grado: "Esencial" },
  { id: "CU02", name: "Iniciar Gestión", module: "Gestiones", grado: "Esencial" },
  { id: "CU03", name: "Lista documentos y certificados necesarios", module: "Gestiones", grado: "Esencial" },
  { id: "CU04", name: "Registrar documentación cliente", module: "Gestiones", grado: "Esencial" },
  { id: "CU05", name: "Preparar escritura", module: "Gestiones", grado: "Esencial" },
  { id: "CU06", name: "Firmar escritura", module: "Gestiones", grado: "Esencial" },
  { id: "CU07", name: "Generar testimonio", module: "Gestiones", grado: "Esencial" },
  { id: "CU08", name: "Verificar Testimonio", module: "Gestiones", grado: "Necesario" },
  { id: "CU09", name: "Registrar deudas documentos de Cliente", module: "Gestiones", grado: "Esencial" },
  { id: "CU10", name: "Registrar movimientos documentación", module: "Gestiones", grado: "Esencial" },
  { id: "CU11", name: "Ingresar para inscripción", module: "Gestiones", grado: "Esencial" },
  { id: "CU12", name: "Retirar testimonio", module: "Gestiones", grado: "Necesario" },
  { id: "CU13", name: "Ver historial de gestión", module: "Gestiones", grado: "Estaría Bueno" },
  { id: "CU14", name: "Consultar estado gestión", module: "Gestiones", grado: "Necesario" },
  { id: "CU15", name: "Procesar pago", module: "Pagos", grado: "Estaría Bueno" },
  { id: "CU16", name: "Archivar Gestión", module: "Gestiones", grado: "Necesario" },
  { id: "CU17", name: "Dar Alta persona", module: "Clientes", grado: "Esencial" },
  { id: "CU18", name: "Dar Alta Cliente", module: "Clientes", grado: "Esencial" },
  { id: "CU19", name: "Buscar gestiones de un Cliente", module: "Clientes", grado: "Necesario" },
  { id: "CU20", name: "Dar alta usuario", module: "Administracion", grado: "Esencial" },
  { id: "CU21", name: "Modificar Usuario", module: "Administracion", grado: "Esencial" },
  { id: "CU22", name: "Registrar Suplencia", module: "Administracion", grado: "Esencial" },
  { id: "CU23", name: "Ver registro de actividades de usuario", module: "Administracion", grado: "Necesario" },
  { id: "CU24", name: "Generar libro de índices", module: "Protocolos", grado: "Estaría Bueno" },
  { id: "CU25", name: "Generar Declaración Jurada del mes", module: "Protocolos", grado: "Estaría Bueno" },
  { id: "CU26", name: "Ingresar nuevo tipo de trámite", module: "Administracion", grado: "Esencial" },
  { id: "CU27", name: "Ingresar nuevo tipo de documento", module: "Administracion", grado: "Esencial" },
  { id: "CU28", name: "Ingresar nuevos folios", module: "Protocolos", grado: "Esencial" },
  { id: "CU29", name: "Ingresar nuevo concepto", module: "Administracion", grado: "Esencial" },
  { id: "CU30", name: "Ingresar nuevo estado de Gestión", module: "Administracion", grado: "Esencial" },
  { id: "CU31", name: "Modificar tipo de trámite", module: "Administracion", grado: "Esencial" },
  { id: "CU32", name: "Modificar tipo de documento", module: "Administracion", grado: "Esencial" },
  { id: "CU33", name: "Modificar folio", module: "Protocolos", grado: "Esencial" },
  { id: "CU34", name: "Modificar concepto", module: "Administracion", grado: "Esencial" },
  { id: "CU35", name: "Modificar estado de Gestión", module: "Administracion", grado: "Esencial" },
  { id: "CU36", name: "Ingresar tipos de folio", module: "Administracion", grado: "Esencial" },
  { id: "CU37", name: "Eliminar concepto", module: "Administracion", grado: "Esencial" },
  { id: "CU38", name: "Eliminar tipo de documento", module: "Administracion", grado: "Esencial" },
  { id: "CU39", name: "Crear Plantilla Presupuesto", module: "Administracion", grado: "Esencial" },
  { id: "CU40", name: "Modificar Tipo de folio", module: "Administracion", grado: "Esencial" },
  { id: "CU41", name: "Modificar Cliente", module: "Clientes", grado: "Esencial" },
  { id: "CU42", name: "Informar próximos vencimientos", module: "Gestiones", grado: "Necesario" },
  { id: "CU43", name: "Reingresar documentación", module: "Gestiones", grado: "Esencial" },
  { id: "CU44", name: "Reingresar testimonio", module: "Gestiones", grado: "Esencial" },
  { id: "CU45", name: "Modificar presupuesto", module: "Presupuestos", grado: "Esencial" },
  { id: "CU46", name: "Ver detalle cliente", module: "Clientes", grado: "Esencial" },
  { id: "CU47", name: "Consultar Pago", module: "Pagos", grado: "Estaría Bueno" },
  { id: "CU48", name: "Dar alta escribano", module: "Administracion", grado: "Esencial" },
  { id: "CU49", name: "Eliminar Plantilla Presupuesto", module: "Administracion", grado: "Esencial" },
  { id: "CU50", name: "Generar Declaración Jurada de Rentas", module: "Protocolos", grado: "Estaría Bueno" },
  { id: "CU51", name: "Modificar escribano", module: "Administracion", grado: "Esencial" },
  { id: "CU52", name: "Modificar Escritura", module: "Gestiones", grado: "Esencial" },
  { id: "CU53", name: "Modificar Gestión", module: "Gestiones", grado: "Esencial" },
  { id: "CU54", name: "Modificar Persona", module: "Clientes", grado: "Esencial" },
  { id: "CU55", name: "Modificar Plantilla Presupuesto", module: "Administracion", grado: "Esencial" },
  { id: "CU56", name: "Registrar inscripción", module: "Gestiones", grado: "Esencial" },
  { id: "CU57", name: "Eliminar tipo de trámite", module: "Administracion", grado: "Esencial" },
  { id: "CU58", name: "Eliminar Tipo de folio", module: "Administracion", grado: "Esencial" },
  { id: "CU59", name: "Consultar Suplencias", module: "Administracion", grado: "Necesario" },
  { id: "CU60", name: "Buscar Presupuesto", module: "Presupuestos", grado: "Necesario" },
  { id: "CU61", name: "Buscar persona o cliente", module: "Clientes", grado: "Necesario" },
  { id: "CU62", name: "Buscar Escritura", module: "Gestiones", grado: "Necesario" },
  { id: "CU63", name: "Buscar Folios", module: "Protocolos", grado: "Necesario" },
  { id: "CU64", name: "Buscar Tipo de tramite", module: "Administracion", grado: "Necesario" },
  { id: "CU65", name: "Buscar Tipos de documentos", module: "Administracion", grado: "Necesario" },
  { id: "CU66", name: "Buscar Conceptos", module: "Administracion", grado: "Necesario" },
  { id: "CU67", name: "Buscar Estados de Gestión", module: "Administracion", grado: "Necesario" },
  { id: "CU68", name: "Buscar tipos de folios", module: "Administracion", grado: "Necesario" },
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
 * Extract module tag (e.g., @module-presupuestos)
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
  byGrado: Record<string, { total: number; pass: number }>;
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
    const byGrado: Record<string, { total: number; pass: number }> = {};

    for (const cu of CU_MATRIX) {
      const r = this.results.get(cu.id)!;
      if (!byModule[cu.module]) byModule[cu.module] = { total: 0, pass: 0, fail: 0 };
      if (!byGrado[cu.grado]) byGrado[cu.grado] = { total: 0, pass: 0 };

      byModule[cu.module].total++;
      byGrado[cu.grado].total++;

      switch (r.status) {
        case "pass":
          passed++;
          byModule[cu.module].pass++;
          byGrado[cu.grado].pass++;
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

    return { total, passed, failed, skipped, notRun, passRate, coveragePct, byModule, byGrado };
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
          <td>${cu.grado}</td>
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

    const gradoRows = Object.entries(stats.byGrado)
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([grado, g]) => `
        <tr>
          <td>${grado}</td>
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

  <h2>🏷️ Coverage by Priority (Grado)</h2>
  <table>
    <thead><tr><th>Priority</th><th>Total</th><th>Pass</th><th>Rate</th></tr></thead>
    <tbody>${gradoRows}</tbody>
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
