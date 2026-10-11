/**
 * TS-0013 - Deed signing workflow (CU05 → CU06)
 * CU05 - Prepare a deed
 * CU06 - Sign a deed
 * CU52 - Update a deed
 * CU63 - Search deeds
 * Issue #892 (folio picker)
 * Sequence: docs/200-architecture/204-diagrams/Secuencias/CU05.puml
 */
import { type Page, test, expect } from '@playwright/test'
import { GherkinSteps } from './gherkin-helpers'
import { authenticateAsAdmin } from './setup/auth'
import { createFolio, createPerson, uniqueId } from './setup/api-helpers'
import { apiPost } from './setup/api-helpers'

// ──────────────────────────────────────────────
// Seed helpers
// ──────────────────────────────────────────────

/** Seed a folio in estado "Nuevo" and return its idFolio */
async function seedNewFolio(page: Page): Promise<number> {
  const result = await createFolio(page, 1, { status: 'Nuevo' })
  if (!result.ok || !result.data?.idFolio) {
    throw new Error(`Failed to seed folio: ${result.error ?? JSON.stringify(result.data)}`)
  }
  return result.data.idFolio
}

/**
 * Seed a deed in estado "Sin Firmar" WITH a folio assigned via the API.
 * Returns the idDeed and number — the table only renders `number`, not `cuerpo`,
 * so tests must match rows by number.
 */
async function seedDeedWithFolio(page: Page, idFolio: number): Promise<{ idDeed: number; number: number }> {
  const id = uniqueId()
  const result = await apiPost<{ idDeed: number }>(page, '/deeds', {
    number: id,
    dateDeedrecording: new Date().toISOString().split('T')[0],
    body: `Deed para firma E2E ${id}`,
    status: 'Sin Firmar',
    idFolio,
    notes: 'Numeración no correlativa: seed de datos E2E aislado (CU86)',
  })
  if (!result.ok || !result.data?.idDeed) {
    throw new Error(`Failed to seed escritura: ${result.error ?? JSON.stringify(result.data)}`)
  }
  return { idDeed: result.data.idDeed, number: id }
}

/**
 * Seed a deed in estado "Sin Firmar" WITHOUT a folio — used by the edge-case
 * test that verifies firmar is blocked when no folio is assigned.
 */
async function seedDeedWithoutFolio(page: Page): Promise<{ idDeed: number; number: number }> {
  const id = uniqueId()
  const result = await apiPost<{ idDeed: number }>(page, '/deeds', {
    number: id,
    dateDeedrecording: new Date().toISOString().split('T')[0],
    body: `Deed sin folio E2E ${id}`,
    status: 'Sin Firmar',
  })
  if (!result.ok || !result.data?.idDeed) {
    throw new Error(`Failed to seed escritura sin folio: ${result.error ?? JSON.stringify(result.data)}`)
  }
  return { idDeed: result.data.idDeed, number: id }
}

// ──────────────────────────────────────────────
// CU05 — Preparar Deed
// ──────────────────────────────────────────────

test.describe('CU05 - Preparar Deed', () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  test('CU05-GW01: golden path — the modal opens with number, date, and folio selector', async ({ page }) => {
    // Seed: ensure at least one folio is available for the picker
    await seedNewFolio(page)

    await page.goto('/dashboard/deeds')
    await page.waitForLoadState('domcontentloaded')

    // When: click "Nueva deed"
    await page.getByRole('button', { name: /nueva escritura/i }).click()

    // Then: modal opens
    const dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()

    // Form must have campo "número"
    await expect(dialog.getByLabel(/número/i)).toBeVisible()

    // Form must have campo "fecha"
    await expect(dialog.getByLabel(/date/i)).toBeVisible()

    // Form must have folio selector (Issue #892)
    await expect(dialog.getByTestId('select-folio-deed')).toBeVisible()
  })

  test('CU05-GW02: golden path — filling the form with a folio → the deed appears in the table as Sin Firmar', async ({ page }) => {
    const idFolio = await seedNewFolio(page)

    await page.goto('/dashboard/deeds')
    await page.waitForLoadState('domcontentloaded')

    const numberDeed = `${uniqueId()}`

    // When: open modal and fill fields
    await page.getByRole('button', { name: /nueva escritura/i }).click()
    const dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()

    await dialog.getByLabel(/número/i).fill(numberDeed)
    await dialog.getByLabel(/date/i).fill(new Date().toISOString().split('T')[0])

    // Select folio using the folio picker
    const folioPicker = dialog.getByTestId('select-folio-deed')
    await folioPicker.click()
    // Pick the first available folio option
    const folioOption = page.getByRole('listbox').getByRole('option').first()
    await folioOption.waitFor({ state: 'attached', timeout: 5000 })
    await folioOption.evaluate((el: HTMLElement) => el.click())

    // Justify the numbering gap (number is random, per CU86 correlative validation)
    await dialog.getByLabel(/observaciones/i).fill('Numeración no correlativa: seed de datos E2E aislado (CU86)')

    // Submit
    await dialog.getByRole('button', { name: /confirmar|guardar|crear/i }).click()

    // Then: modal closes and success toast appears
    await expect(dialog).not.toBeVisible({ timeout: 8000 })
    await expect(
      page.locator('[data-sonner-toast]').getByText(/creada|éxito/i)
    ).toBeVisible({ timeout: 5000 })

    // And: deed row appears in the table with estado "Sin Firmar" (the backend default
    // for newly-created deeds — see ConstantesNegocio.ESCRITURA_SIN_FIRMAR)
    const table = page.getByRole('table')
    await expect(table).toBeVisible()
    const row = table.getByRole('row', { name: new RegExp(numberDeed) })
    await expect(row).toBeVisible({ timeout: 10000 })
    await expect(row).toContainText(/Sin Firmar/i)
  })
})

// ──────────────────────────────────────────────
// CU06 — Firmar Deed
// ──────────────────────────────────────────────

test.describe('CU06 - Firmar Deed', () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  test('CU06-GW01: golden path — deed Sin Firmar with a folio → sign → status Firmada', async ({ page }) => {
    // Seed: a folio plus a deed with a folio assigned
    const idFolio = await seedNewFolio(page)
    const { idDeed, number } = await seedDeedWithFolio(page, idFolio)

    await page.goto('/dashboard/deeds')
    await page.waitForLoadState('domcontentloaded')

    // The deed row must be visible
    const row = page.getByRole('row').filter({ hasText: new RegExp(String(number)) })
    await expect(row).toBeVisible({ timeout: 10000 })

    // When: click the firmar button for this deed
    await page.getByTestId(`btn-sign-deed-${idDeed}`).click()

    // Confirm in the dialog/alert
    const confirmDialog = page.getByRole('dialog').or(page.getByRole('alertdialog'))
    await expect(confirmDialog).toBeVisible({ timeout: 5000 })
    await confirmDialog.getByRole('button', { name: /confirmar|firmar|aceptar/i }).click()

    // Then: dialog closes and deed row shows estado "Signed"
    await expect(confirmDialog).not.toBeVisible({ timeout: 8000 })
    await expect(row).toContainText(/Firmada/i, { timeout: 10000 })
  })

  test('CU06-EDGE01: deed Sin Firmar without a folio → the sign button is disabled or shows an error', async ({ page }) => {
    // Seed: a deed with no folio assigned
    const { idDeed, number } = await seedDeedWithoutFolio(page)

    await page.goto('/dashboard/deeds')
    await page.waitForLoadState('domcontentloaded')

    // The deed row must be visible
    const row = page.getByRole('row').filter({ hasText: new RegExp(String(number)) })
    await expect(row).toBeVisible({ timeout: 10000 })

    // The firmar button should be either absent, disabled, or trigger an error
    const firmarBtn = page.getByTestId(`btn-sign-deed-${idDeed}`)
    const firmarBtnExists = await firmarBtn.isVisible({ timeout: 3000 }).catch(() => false)

    if (firmarBtnExists) {
      // If button is rendered, it must be disabled OR clicking it shows an error
      const isDisabled = await firmarBtn.isDisabled()
      if (isDisabled) {
        // Acceptable: button is disabled — no folio assigned
        expect(isDisabled).toBe(true)
      } else {
        // If not disabled, confirming the firmar dialog must produce an error toast
        // (backend rejects with 400 when the deed has no folio assigned)
        await firmarBtn.click()
        const confirmDialog = page.getByRole('dialog').or(page.getByRole('alertdialog'))
        await expect(confirmDialog).toBeVisible({ timeout: 5000 })
        await confirmDialog.getByRole('button', { name: /confirmar|firmar|aceptar/i }).click()

        await expect(
          page
            .locator('[data-sonner-toast]')
            .getByText(/folios|sin folio|no tiene folio|no puede firmar/i)
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
// CU63 — Search deeds
// ──────────────────────────────────────────────

test.describe('CU63 - Search deeds', () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  test('CU63-GW01: searching by number → GET /deeds/search is called', async ({ page }) => {
    await page.goto('/dashboard/deeds')
    await page.waitForLoadState('domcontentloaded')

    // Intercept before typing so we don't miss a fast request
    const searchRequestPromise = page.waitForRequest(
      (req) =>
        req.url().includes('/api/v1/deeds/search') && req.method() === 'GET',
      { timeout: 10000 }
    )

    await page.getByTestId('input-search-deed').fill('1')

    // Then: the search endpoint was called
    await searchRequestPromise
  })

  test('CU63-GW02: searching for an existing deed → the result appears in the table', async ({ page }) => {
    // Seed a known deed via API so the search has something to find
    const id = uniqueId()
    const number = `${id}`
    const result = await apiPost<{ idDeed: number }>(page, '/deeds', {
      number: id,
      dateDeedrecording: new Date().toISOString().split('T')[0],
      body: `Deed de búsqueda E2E ${id}`,
      status: 'Sin Firmar',
    })
    if (!result.ok) throw new Error(`Seed failed: ${result.error}`)

    await page.goto('/dashboard/deeds')
    await page.waitForLoadState('domcontentloaded')

    // When: search for the known number
    await page.getByTestId('input-search-deed').fill(number)

    // Then: the matching row is visible in the table
    const table = page.getByRole('table')
    await expect(table).toBeVisible()
    await expect(table.getByRole('row', { name: new RegExp(number) })).toBeVisible({ timeout: 10000 })
  })
})

// ──────────────────────────────────────────────
// Viewport responsive tests — deeds screen
// ──────────────────────────────────────────────

for (const viewport of [
  { width: 320, height: 568, label: '320px (mobile)' },
  { width: 768, height: 1024, label: '768px (tablet)' },
  { width: 1024, height: 768, label: '1024px (desktop)' },
]) {
  test(`escrituras screen is usable at ${viewport.label}`, async ({ page }) => {
    await authenticateAsAdmin(page)
    await page.setViewportSize({ width: viewport.width, height: viewport.height })

    await page.goto('/dashboard/deeds')
    await page.waitForLoadState('domcontentloaded')

    // Page heading visible
    await expect(page.getByRole('heading', { level: 1, name: /deeds/i })).toBeVisible()

    // "Nueva deed" button accessible
    await expect(page.getByRole('button', { name: /nueva escritura/i })).toBeVisible()

    // Search input accessible
    await expect(page.getByTestId('input-search-deed')).toBeVisible()

    // No horizontal overflow (ui-ux-design.md — Responsive Design)
    const noOverflow = await page.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth
    )
    expect(noOverflow).toBe(true)
  })
}
