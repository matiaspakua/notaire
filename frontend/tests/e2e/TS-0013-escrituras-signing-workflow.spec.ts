/**
 * TS-0013 - Escrituras Signing Workflow (CU05 → CU06)
 * CU05 - Preparar Escritura
 * CU06 - Firmar Escritura
 * CU52 - Modificar Escritura
 * CU63 - Buscar Escritura
 * Issue #892 (folio picker)
 * Sequence: docs/200-architecture/204-diagrams/Secuencias/CU05.puml
 */
import { type Page, test, expect } from '@playwright/test'
import { GherkinSteps } from './gherkin-helpers'
import { authenticateAsAdmin } from './setup/auth'
import { createFolio, createPersona, uniqueId } from './setup/api-helpers'
import { apiPost } from './setup/api-helpers'

// ──────────────────────────────────────────────
// Seed helpers
// ──────────────────────────────────────────────

/** Seed a folio in estado "Nuevo" and return its idFolio */
async function seedFolioNuevo(page: Page): Promise<number> {
  const result = await createFolio(page, 1, { estado: 'Nuevo' })
  if (!result.ok || !result.data?.idFolio) {
    throw new Error(`Failed to seed folio: ${result.error ?? JSON.stringify(result.data)}`)
  }
  return result.data.idFolio
}

/**
 * Seed a escritura in estado "Sin Firmar" WITH a folio assigned via the API.
 * Returns the idEscritura and numero — the table only renders `numero`, not `cuerpo`,
 * so tests must match rows by numero.
 */
async function seedEscrituraConFolio(page: Page, idFolio: number): Promise<{ idEscritura: number; numero: number }> {
  const id = uniqueId()
  const result = await apiPost<{ idEscritura: number }>(page, '/escrituras', {
    numero: id,
    fechaEscrituracion: new Date().toISOString().split('T')[0],
    cuerpo: `Escritura para firma E2E ${id}`,
    estado: 'Sin Firmar',
    idFolio,
    observaciones: 'Numeración no correlativa: seed de datos E2E aislado (CU86)',
  })
  if (!result.ok || !result.data?.idEscritura) {
    throw new Error(`Failed to seed escritura: ${result.error ?? JSON.stringify(result.data)}`)
  }
  return { idEscritura: result.data.idEscritura, numero: id }
}

/**
 * Seed a escritura in estado "Sin Firmar" WITHOUT a folio — used by the edge-case
 * test that verifies firmar is blocked when no folio is assigned.
 */
async function seedEscrituraSinFolio(page: Page): Promise<{ idEscritura: number; numero: number }> {
  const id = uniqueId()
  const result = await apiPost<{ idEscritura: number }>(page, '/escrituras', {
    numero: id,
    fechaEscrituracion: new Date().toISOString().split('T')[0],
    cuerpo: `Escritura sin folio E2E ${id}`,
    estado: 'Sin Firmar',
  })
  if (!result.ok || !result.data?.idEscritura) {
    throw new Error(`Failed to seed escritura sin folio: ${result.error ?? JSON.stringify(result.data)}`)
  }
  return { idEscritura: result.data.idEscritura, numero: id }
}

// ──────────────────────────────────────────────
// CU05 — Preparar Escritura
// ──────────────────────────────────────────────

test.describe('CU05 - Preparar Escritura', () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  test('CU05-GW01: golden path — modal opens with número, fecha and folio selector', async ({ page }) => {
    // Seed: ensure at least one folio is available for the picker
    await seedFolioNuevo(page)

    await page.goto('/dashboard/escrituras')
    await page.waitForLoadState('domcontentloaded')

    // When: click "Nueva escritura"
    await page.getByRole('button', { name: /nueva escritura/i }).click()

    // Then: modal opens
    const dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()

    // Form must have campo "número"
    await expect(dialog.getByLabel(/número/i)).toBeVisible()

    // Form must have campo "fecha"
    await expect(dialog.getByLabel(/fecha/i)).toBeVisible()

    // Form must have folio selector (Issue #892)
    await expect(dialog.getByTestId('select-folio-escritura')).toBeVisible()
  })

  test('CU05-GW02: golden path — fill form with folio → escritura appears in table as Sin Firmar', async ({ page }) => {
    const idFolio = await seedFolioNuevo(page)

    await page.goto('/dashboard/escrituras')
    await page.waitForLoadState('domcontentloaded')

    const numeroEscritura = `${uniqueId()}`

    // When: open modal and fill fields
    await page.getByRole('button', { name: /nueva escritura/i }).click()
    const dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()

    await dialog.getByLabel(/número/i).fill(numeroEscritura)
    await dialog.getByLabel(/fecha/i).fill(new Date().toISOString().split('T')[0])

    // Select folio using the folio picker
    const folioPicker = dialog.getByTestId('select-folio-escritura')
    await folioPicker.click()
    // Pick the first available folio option
    const folioOption = page.getByRole('option').first()
    await folioOption.waitFor({ state: 'attached', timeout: 5000 })
    await folioOption.evaluate((el: HTMLElement) => el.click())

    // Justify the numbering gap (numero is random, per CU86 correlative validation)
    await dialog.getByLabel(/observaciones/i).fill('Numeración no correlativa: seed de datos E2E aislado (CU86)')

    // Submit
    await dialog.getByRole('button', { name: /confirmar|guardar|crear/i }).click()

    // Then: modal closes and success toast appears
    await expect(dialog).not.toBeVisible({ timeout: 8000 })
    await expect(
      page.locator('[data-sonner-toast]').getByText(/creada|éxito/i)
    ).toBeVisible({ timeout: 5000 })

    // And: escritura row appears in the table with estado "Sin Firmar" (the backend default
    // for newly-created escrituras — see ConstantesNegocio.ESCRITURA_SIN_FIRMAR)
    const table = page.getByRole('table')
    await expect(table).toBeVisible()
    const row = table.getByRole('row', { name: new RegExp(numeroEscritura) })
    await expect(row).toBeVisible({ timeout: 10000 })
    await expect(row).toContainText(/Sin Firmar/i)
  })
})

// ──────────────────────────────────────────────
// CU06 — Firmar Escritura
// ──────────────────────────────────────────────

test.describe('CU06 - Firmar Escritura', () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  test('CU06-GW01: golden path — escritura Sin Firmar con folio → firmar → estado Firmada', async ({ page }) => {
    // Seed: folio + escritura con folio asignado
    const idFolio = await seedFolioNuevo(page)
    const { idEscritura, numero } = await seedEscrituraConFolio(page, idFolio)

    await page.goto('/dashboard/escrituras')
    await page.waitForLoadState('domcontentloaded')

    // The escritura row must be visible
    const row = page.getByRole('row').filter({ hasText: new RegExp(String(numero)) })
    await expect(row).toBeVisible({ timeout: 10000 })

    // When: click the firmar button for this escritura
    await page.getByTestId(`btn-firmar-escritura-${idEscritura}`).click()

    // Confirm in the dialog/alert
    const confirmDialog = page.getByRole('dialog').or(page.getByRole('alertdialog'))
    await expect(confirmDialog).toBeVisible({ timeout: 5000 })
    await confirmDialog.getByRole('button', { name: /confirmar|firmar|aceptar/i }).click()

    // Then: dialog closes and escritura row shows estado "Firmada"
    await expect(confirmDialog).not.toBeVisible({ timeout: 8000 })
    await expect(row).toContainText(/Firmada/i, { timeout: 10000 })
  })

  test('CU06-EDGE01: escritura Sin Firmar SIN folio → botón firmar deshabilitado o muestra error', async ({ page }) => {
    // Seed: escritura sin folio asignado
    const { idEscritura, numero } = await seedEscrituraSinFolio(page)

    await page.goto('/dashboard/escrituras')
    await page.waitForLoadState('domcontentloaded')

    // The escritura row must be visible
    const row = page.getByRole('row').filter({ hasText: new RegExp(String(numero)) })
    await expect(row).toBeVisible({ timeout: 10000 })

    // The firmar button should be either absent, disabled, or trigger an error
    const firmarBtn = page.getByTestId(`btn-firmar-escritura-${idEscritura}`)
    const firmarBtnExists = await firmarBtn.isVisible({ timeout: 3000 }).catch(() => false)

    if (firmarBtnExists) {
      // If button is rendered, it must be disabled OR clicking it shows an error
      const isDisabled = await firmarBtn.isDisabled()
      if (isDisabled) {
        // Acceptable: button is disabled — no folio assigned
        expect(isDisabled).toBe(true)
      } else {
        // If not disabled, confirming the firmar dialog must produce an error toast
        // (backend rejects with 400 — EscrituraController: "no tiene folio asignado")
        await firmarBtn.click()
        const confirmDialog = page.getByRole('dialog').or(page.getByRole('alertdialog'))
        await expect(confirmDialog).toBeVisible({ timeout: 5000 })
        await confirmDialog.getByRole('button', { name: /confirmar|firmar|aceptar/i }).click()

        await expect(
          page
            .locator('[data-sonner-toast]')
            .getByText(/folio|sin folio|no tiene folio|no puede firmar/i)
            .first()
        ).toBeVisible({ timeout: 5000 })
      }
    } else {
      // Acceptable: button is not rendered at all when folio is absent
      expect(firmarBtnExists).toBe(false)
    }
  })
})

// ──────────────────────────────────────────────
// CU63 — Buscar Escritura
// ──────────────────────────────────────────────

test.describe('CU63 - Buscar Escritura', () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  test('CU63-GW01: buscar por número → GET /escrituras/buscar es invocado', async ({ page }) => {
    await page.goto('/dashboard/escrituras')
    await page.waitForLoadState('domcontentloaded')

    // Intercept before typing so we don't miss a fast request
    const searchRequestPromise = page.waitForRequest(
      (req) =>
        req.url().includes('/api/v1/escrituras/buscar') && req.method() === 'GET',
      { timeout: 10000 }
    )

    await page.getByTestId('input-search-escritura').fill('1')

    // Then: the search endpoint was called
    await searchRequestPromise
  })

  test('CU63-GW02: buscar escritura existente → resultado aparece en tabla', async ({ page }) => {
    // Seed a known escritura via API so the search has something to find
    const id = uniqueId()
    const numero = `${id}`
    const result = await apiPost<{ idEscritura: number }>(page, '/escrituras', {
      numero: id,
      fechaEscrituracion: new Date().toISOString().split('T')[0],
      cuerpo: `Escritura de búsqueda E2E ${id}`,
      estado: 'Sin Firmar',
    })
    if (!result.ok) throw new Error(`Seed failed: ${result.error}`)

    await page.goto('/dashboard/escrituras')
    await page.waitForLoadState('domcontentloaded')

    // When: search for the known numero
    await page.getByTestId('input-search-escritura').fill(numero)

    // Then: the matching row is visible in the table
    const table = page.getByRole('table')
    await expect(table).toBeVisible()
    await expect(table.getByRole('row', { name: new RegExp(numero) })).toBeVisible({ timeout: 10000 })
  })
})

// ──────────────────────────────────────────────
// Viewport responsive tests — escrituras screen
// ──────────────────────────────────────────────

for (const viewport of [
  { width: 320, height: 568, label: '320px (mobile)' },
  { width: 768, height: 1024, label: '768px (tablet)' },
  { width: 1024, height: 768, label: '1024px (desktop)' },
]) {
  test(`escrituras screen is usable at ${viewport.label}`, async ({ page }) => {
    await authenticateAsAdmin(page)
    await page.setViewportSize({ width: viewport.width, height: viewport.height })

    await page.goto('/dashboard/escrituras')
    await page.waitForLoadState('domcontentloaded')

    // Page heading visible
    await expect(page.getByRole('heading', { name: /escrituras/i })).toBeVisible()

    // "Nueva escritura" button accessible
    await expect(page.getByRole('button', { name: /nueva escritura/i })).toBeVisible()

    // Search input accessible
    await expect(page.getByTestId('input-search-escritura')).toBeVisible()

    // No horizontal overflow (ui-ux-design.md — Responsive Design)
    const noOverflow = await page.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth
    )
    expect(noOverflow).toBe(true)
  })
}
