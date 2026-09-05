/**
 * TS-0012 - Documentación y Testimonio Workflow (CU03 → CU07 → CU08 → CU11 → CU12)
 * CU03 - Listar documentos necesarios
 * CU07 - Generar testimonio
 * CU08 - Verificar testimonio
 * CU11 - Ingresar para inscripción
 * CU12 - Retirar testimonio
 * Issue #832
 * Sequence: docs/200-architecture/204-diagrams/Secuencias/CU07.puml
 */
import { test, expect } from '@playwright/test'
import { GherkinSteps } from './gherkin-helpers'
import { authenticateAsAdmin } from './setup/auth'
import { apiPost, apiGet, uniqueId } from './setup/api-helpers'

// ──────────────────────────────────────────────
// Seed helpers
// ──────────────────────────────────────────────

interface EscrituraCreada {
  idEscritura: number
  numero: number
}

interface TestimonioCreado {
  idTestimonio: number
  numero: number
}

/** Seed a "Firmada" escritura so CU07 has something to reference. */
async function seedEscrituraFirmada(
  page: import('@playwright/test').Page,
): Promise<EscrituraCreada> {
  const numero = uniqueId() % 1_000_000
  const result = await apiPost<{ idEscritura: number }>(page, '/escrituras', {
    numero,
    fechaEscrituracion: new Date().toISOString().split('T')[0],
    cuerpo: `Contenido E2E TS-0012 ${numero}`,
    estado: 'Firmada',
  })
  if (!result.ok || !result.data?.idEscritura) {
    throw new Error(`seedEscrituraFirmada failed: status ${result.status} — ${result.error}`)
  }
  return { idEscritura: result.data.idEscritura, numero }
}

/**
 * Seed a testimonio that is already "verificado" (true) so CU11/CU12 can
 * start directly from the movimientos page without repeating CU07/CU08.
 */
async function seedTestimonioVerificado(
  page: import('@playwright/test').Page,
): Promise<TestimonioCreado> {
  const { idEscritura, numero: escrituraNumero } = await seedEscrituraFirmada(page)
  const numero = uniqueId() % 1_000_000
  const result = await apiPost<{ idTestimonio: number }>(page, '/testimonio', {
    escritura: { idEscritura, numero: escrituraNumero },
    numero,
    observado: false,
    verificado: true,
  })
  if (!result.ok || !result.data?.idTestimonio) {
    throw new Error(`seedTestimonioVerificado failed: status ${result.status} — ${result.error}`)
  }
  return { idTestimonio: result.data.idTestimonio, numero }
}

// ──────────────────────────────────────────────
// CU03 — Listar documentos necesarios
// ──────────────────────────────────────────────

test.describe('CU03 - Listar documentos y certificados necesarios', () => {
  let steps: GherkinSteps

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page)
    await steps.givenUserIsLoggedIn()
    await steps.givenUserIsOnPage('/dashboard/documentos-necesarios')
  })

  test('CU03-GW01: Given on documentos necesarios, When select trámite, Then shows results section', async ({
    page,
  }) => {
    await steps.thenPageHasHeading('Documentos Necesarios')

    await page.getByTestId('select-tramite').click()
    await page.getByRole('option').first().click()

    await expect(page.getByText(/Documentos necesarios para/i)).toBeVisible()
    const emptyState = page.getByTestId('empty-state')
    const table = page.getByRole('table')
    await expect(emptyState.or(table)).toBeVisible()
  })
})

// ──────────────────────────────────────────────
// CU07 + CU08 — Golden path: ciclo legal completo
// ──────────────────────────────────────────────

test.describe('CU07→CU08→CU11→CU12 — Ciclo legal completo (golden path)', () => {
  let steps: GherkinSteps

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page)
    await steps.givenUserIsLoggedIn()
  })

  test('Golden path: generar → verificar → ingresar → inscribir → retirar', async ({ page }) => {
    // ── Seed ──────────────────────────────────────────
    const { idEscritura, numero: escrituraNumero } = await seedEscrituraFirmada(page)

    // ── CU07: Generar testimonio ──────────────────────
    await steps.givenUserIsOnPage('/dashboard/testimonios')
    await page.getByTestId('btn-generar-testimonio').click()
    await steps.thenModalIsVisible()

    await page.getByTestId('select-escritura-testimonio').click()
    await page.getByRole('option', { name: new RegExp(String(escrituraNumero)) }).click()
    await page.getByTestId('btn-guardar-testimonio').click()
    await steps.thenShowsSuccessMessage('generado')
    await steps.thenModalIsNotVisible()

    const rowTestimonios = page.getByRole('row', { name: new RegExp(String(escrituraNumero)) })
    await expect(rowTestimonios).toBeVisible({ timeout: 10_000 })

    // Retrieve the generated testimonio id from the API
    const list = await apiGet<Array<{ idTestimonio: number; escritura?: { numero?: number } }>>(
      page,
      '/testimonio',
    )
    const idTestimonio = list.data!.find(
      (t) => t.escritura?.numero === escrituraNumero,
    )!.idTestimonio

    // ── CU08: Verificar sin observaciones ─────────────
    await page.getByTestId(`btn-verificar-testimonio-${idTestimonio}`).click()
    await steps.thenModalIsVisible()
    await page.getByTestId('btn-confirmar-verificar-testimonio').click()
    await steps.thenShowsSuccessMessage('verificado')
    await expect(rowTestimonios).toContainText(/verificado/i)

    // ── CU11: Ingresar para inscripción ───────────────
    await steps.givenUserIsOnPage('/dashboard/movimientos-testimonio')
    const rowMovimientos = page.getByRole('row', { name: new RegExp(String(escrituraNumero)) })
    await expect(rowMovimientos).toBeVisible({ timeout: 10_000 })

    await page.getByTestId(`btn-ingresar-testimonio-${idTestimonio}`).click()
    await steps.thenShowsSuccessMessage('ingresado')
    await expect(rowMovimientos).toContainText(/ingresado/i)

    // ── CU11: Registrar inscripción ───────────────────
    await page.getByTestId(`btn-registrar-inscripcion-${idTestimonio}`).click()
    await steps.thenShowsSuccessMessage('inscripción registrada')
    await expect(rowMovimientos).toContainText(/inscripto/i)

    // ── CU12: Retirar testimonio ──────────────────────
    await page.getByTestId(`btn-retirar-testimonio-${idTestimonio}`).click()
    await steps.thenModalIsVisible()
    await page.getByTestId('input-numero-carton').fill('123')
    await page.getByTestId('btn-confirmar-retiro').click()
    await steps.thenShowsSuccessMessage('retirado')
    await expect(rowMovimientos).toContainText(/retirado/i)
  })
})

// ──────────────────────────────────────────────
// CU07 — Generar testimonio (unit / edge)
// ──────────────────────────────────────────────

test.describe('CU07 - Generar testimonio', () => {
  let steps: GherkinSteps

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page)
    await steps.givenUserIsLoggedIn()
  })

  test('CU07-GW01: Given escritura firmada, When generar testimonio, Then estado generado aparece en tabla', async ({
    page,
  }) => {
    const { numero } = await seedEscrituraFirmada(page)

    await steps.givenUserIsOnPage('/dashboard/testimonios')
    await page.getByTestId('btn-generar-testimonio').click()
    await steps.thenModalIsVisible()

    await page.getByTestId('select-escritura-testimonio').click()
    await page.getByRole('option', { name: new RegExp(String(numero)) }).click()
    await page.getByTestId('btn-guardar-testimonio').click()
    await steps.thenShowsSuccessMessage('generado')
    await steps.thenModalIsNotVisible()

    const row = page.getByRole('row', { name: new RegExp(String(numero)) })
    await expect(row).toBeVisible({ timeout: 10_000 })
  })
})

// ──────────────────────────────────────────────
// CU08 — Verificar testimonio (edge paths)
// ──────────────────────────────────────────────

test.describe('CU08 - Verificar testimonio', () => {
  let steps: GherkinSteps

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page)
    await steps.givenUserIsLoggedIn()
  })

  test('CU08-GW01: Edge: verificar con observaciones → estado Observado', async ({ page }) => {
    // Seed escritura and generate testimonio via API so this test is independent
    const { idEscritura, numero } = await seedEscrituraFirmada(page)
    const generated = await apiPost<{ idTestimonio: number }>(
      page,
      `/testimonio/${idEscritura}/generar`,
      {},
    )
    const idTestimonio = generated.data!.idTestimonio

    await steps.givenUserIsOnPage('/dashboard/testimonios')
    const row = page.getByRole('row', { name: new RegExp(String(numero)) })
    await expect(row).toBeVisible({ timeout: 10_000 })

    await page.getByTestId(`btn-verificar-testimonio-${idTestimonio}`).click()
    await steps.thenModalIsVisible()
    await page.getByTestId('checkbox-observado-testimonio').click()
    await page.getByTestId('input-observaciones-testimonio').fill('Falta firma del otorgante')
    await page.getByTestId('btn-confirmar-verificar-testimonio').click()
    await steps.thenShowsSuccessMessage('verificado')

    await expect(row).toContainText(/observado/i)
  })

  test('CU08-GW02: Edge: emitir copia está bloqueado hasta que el testimonio esté verificado', async ({
    page,
  }) => {
    const { idEscritura, numero } = await seedEscrituraFirmada(page)
    const generated = await apiPost<{ idTestimonio: number }>(
      page,
      `/testimonio/${idEscritura}/generar`,
      {},
    )
    const idTestimonio = generated.data!.idTestimonio

    await steps.givenUserIsOnPage('/dashboard/testimonios')
    const row = page.getByRole('row', { name: new RegExp(String(numero)) })
    await expect(row).toBeVisible({ timeout: 10_000 })

    // btn-emitir-copia must NOT exist while testimonio is still unverified
    await expect(page.getByTestId(`btn-emitir-copia-${idTestimonio}`)).toHaveCount(0)
    // The verify button must still be available
    await expect(page.getByTestId(`btn-verificar-testimonio-${idTestimonio}`)).toBeVisible()
  })
})

// ──────────────────────────────────────────────
// CU11 — Movimientos testimonio (edge paths)
// ──────────────────────────────────────────────

test.describe('CU11 - Ingresar para inscripción', () => {
  let steps: GherkinSteps

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page)
    await steps.givenUserIsLoggedIn()
  })

  test('CU11-GW01: ingresar y registrar inscripción cambia estado a inscripto', async ({ page }) => {
    const { idTestimonio, numero } = await seedTestimonioVerificado(page)

    await steps.givenUserIsOnPage('/dashboard/movimientos-testimonio')
    const row = page.getByRole('row', { name: new RegExp(String(numero)) })
    await expect(row).toBeVisible({ timeout: 10_000 })

    await page.getByTestId(`btn-ingresar-testimonio-${idTestimonio}`).click()
    await steps.thenShowsSuccessMessage('ingresado')
    await expect(row).toContainText(/ingresado/i)

    await page.getByTestId(`btn-registrar-inscripcion-${idTestimonio}`).click()
    await steps.thenShowsSuccessMessage('inscripción registrada')
    await expect(row).toContainText(/inscripto/i)
  })
})

// ──────────────────────────────────────────────
// CU12 — Retirar testimonio (edge paths)
// ──────────────────────────────────────────────

test.describe('CU12 - Retirar testimonio', () => {
  let steps: GherkinSteps

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page)
    await steps.givenUserIsLoggedIn()
  })

  test('CU12-GW01: retirar después de inscripción → estado retirado', async ({ page }) => {
    const { idTestimonio, numero } = await seedTestimonioVerificado(page)

    await steps.givenUserIsOnPage('/dashboard/movimientos-testimonio')
    const row = page.getByRole('row', { name: new RegExp(String(numero)) })
    await expect(row).toBeVisible({ timeout: 10_000 })

    // Move to 'inscripto' first
    await page.getByTestId(`btn-ingresar-testimonio-${idTestimonio}`).click()
    await steps.thenShowsSuccessMessage('ingresado')
    await page.getByTestId(`btn-registrar-inscripcion-${idTestimonio}`).click()
    await steps.thenShowsSuccessMessage('inscripción registrada')

    // Now retire
    await page.getByTestId(`btn-retirar-testimonio-${idTestimonio}`).click()
    await steps.thenModalIsVisible()
    await page.getByTestId('input-numero-carton').fill('456')
    await page.getByTestId('btn-confirmar-retiro').click()
    await steps.thenShowsSuccessMessage('retirado')
    await expect(row).toContainText(/retirado/i)
  })
})

// ──────────────────────────────────────────────
// Responsive — testimonios screens
// ──────────────────────────────────────────────

for (const viewport of [
  { width: 320, height: 568, label: '320px (mobile)' },
  { width: 768, height: 1024, label: '768px (tablet)' },
  { width: 1024, height: 768, label: '1024px (desktop)' },
]) {
  test.describe(`Responsive ${viewport.label}`, () => {
    let steps: GherkinSteps

    test.beforeEach(async ({ page }) => {
      steps = new GherkinSteps(page)
      await page.setViewportSize({ width: viewport.width, height: viewport.height })
      await steps.givenUserIsLoggedIn()
    })

    test(`testimonios screen has no horizontal overflow at ${viewport.label}`, async ({ page }) => {
      await steps.givenUserIsOnPage('/dashboard/testimonios')
      await page.waitForLoadState('networkidle')
      await steps.thenHasNoHorizontalOverflow()
    })

    test(`movimientos-testimonio screen has no horizontal overflow at ${viewport.label}`, async ({
      page,
    }) => {
      await steps.givenUserIsOnPage('/dashboard/movimientos-testimonio')
      await page.waitForLoadState('networkidle')
      await steps.thenHasNoHorizontalOverflow()
    })
  })
}
