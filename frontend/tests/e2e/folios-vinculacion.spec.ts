/**
 * CU87 - Vincular Escritura, Folio y Copia de Testimonio
 * CU28 - Ingresar nuevos folios
 * Issue #838
 */
import { type Page, test, expect } from '@playwright/test'
import { authenticateAsAdmin } from './setup/auth'
import { apiPost, createFolio, uniqueId } from './setup/api-helpers'

const ESTADO_ESCRITURA_FIRMADA = 'Firmada'

/** Seed a escritura already in estado "Firmada" (no folio linked) */
async function seedEscrituraFirmada(page: Page): Promise<{ idEscritura: number; numero: number }> {
  const id = uniqueId()
  const result = await apiPost<{ idDeed: number }>(page, '/escrituras', {
    number: id,
    dateDeedrecording: new Date().toISOString().split('T')[0],
    body: `Escritura firmada E2E ${id}`,
    status: ESTADO_ESCRITURA_FIRMADA,
    notes: 'Numeración no correlativa: seed de datos E2E aislado (CU86)',
  })
  if (!result.ok || !result.data?.idDeed) {
    throw new Error(`Failed to seed escritura firmada: ${result.error ?? JSON.stringify(result.data)}`)
  }
  return { idEscritura: result.data.idDeed, numero: id }
}

/** Select the first option of an already-open Radix listbox */
async function pickFirstOption(page: Page) {
  const option = page.getByRole('option').first()
  await option.waitFor({ state: 'attached', timeout: 5000 })
  await option.evaluate((el: HTMLElement) => el.click())
}

/** Open the "nuevo folio" modal and fill the required fields (número, tipo, escribano) */
async function openNewFolioAndFillRequired(page: Page, numeroFolio: number) {
  await page.getByTestId('btn-nuevo-folio').click()
  const dialog = page.getByRole('dialog')
  await expect(dialog).toBeVisible()

  await page.getByTestId('input-numero-folio').fill(String(numeroFolio))
  await page.getByTestId('select-tipo-folio').click()
  await pickFirstOption(page)
  await page.getByTestId('select-escribano-folio').click()
  await pickFirstOption(page)

  return dialog
}

test.describe('CU87 - Vincular Escritura y Folio', () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  test('CU87-GW01: golden path — crear folio vinculado a escritura firmada → folio Utilizado y escritura muestra el folio', async ({
    page,
  }) => {
    const { numero } = await seedEscrituraFirmada(page)
    const numeroFolio = Math.floor(10000 + Math.random() * 90000)

    await page.goto('/dashboard/administracion/folios')
    await page.waitForLoadState('domcontentloaded')

    const dialog = await openNewFolioAndFillRequired(page, numeroFolio)

    await page.getByTestId('select-escritura-folio').click()
    const escrituraOption = page.getByRole('option', { name: `Escritura Nº ${numero}` })
    await escrituraOption.waitFor({ state: 'attached', timeout: 5000 })
    await escrituraOption.evaluate((el: HTMLElement) => el.click())

    await dialog.getByRole('button', { name: /guardar/i }).click()

    await expect(dialog).not.toBeVisible({ timeout: 8000 })
    await expect(page.locator('[data-sonner-toast]').getByText(/creado/i)).toBeVisible({ timeout: 5000 })

    // Then: the new folio row shows estado Utilizado and the linked escritura número
    const table = page.getByRole('table')
    const row = table.getByRole('row', { name: new RegExp(String(numeroFolio)) })
    await expect(row).toBeVisible({ timeout: 10000 })
    await expect(row).toContainText(/Utilizado/i)
    await expect(row).toContainText(new RegExp(String(numero)))

    // And: the escritura screen shows it linked to a folio
    await page.goto('/dashboard/escrituras')
    await page.waitForLoadState('domcontentloaded')
    const escrituraRow = page.getByRole('row').filter({ hasText: new RegExp(String(numero)) })
    await expect(escrituraRow).toBeVisible({ timeout: 10000 })
    await expect(escrituraRow).toContainText(/Folio #/i)
  })

  test('CU87-EDGE01: escritura ya vinculada a otro folio no aparece disponible en el selector', async ({ page }) => {
    const { idEscritura, numero } = await seedEscrituraFirmada(page)
    const linked = await createFolio(page, 1, { status: 'Nuevo', deedId: idEscritura })
    if (!linked.ok) throw new Error(`Failed to seed linked folio: ${linked.error}`)

    await page.goto('/dashboard/administracion/folios')
    await page.waitForLoadState('domcontentloaded')

    const dialog = await openNewFolioAndFillRequired(page, Math.floor(10000 + Math.random() * 90000))
    await page.getByTestId('select-escritura-folio').click()

    // Then: the picker never offers an escritura already Utilizado by another folio
    await expect(page.getByRole('option', { name: `Escritura Nº ${numero}` })).toHaveCount(0)

    await page.keyboard.press('Escape')
    await dialog.getByRole('button', { name: /cancelar/i }).click()
  })

  test('CU87-EDGE02: folio en estado Utilizado no puede editarse ni borrarse desde la grilla', async ({ page }) => {
    const { idEscritura } = await seedEscrituraFirmada(page)
    const numeroFolio = Math.floor(10000 + Math.random() * 90000)
    const linked = await createFolio(page, 1, { number: numeroFolio, status: 'Nuevo', deedId: idEscritura })
    if (!linked.ok) throw new Error(`Failed to seed linked folio: ${linked.error}`)

    await page.goto('/dashboard/administracion/folios')
    await page.waitForLoadState('domcontentloaded')

    const table = page.getByRole('table')
    const row = table.getByRole('row', { name: new RegExp(String(numeroFolio)) })
    await expect(row).toBeVisible({ timeout: 10000 })
    await expect(row).toContainText(/Utilizado/i)

    await expect(row.getByTestId('btn-edit-folio')).toBeDisabled()
    await expect(row.getByTestId('btn-delete-folio')).toBeDisabled()
  })

  test('CU87-EDGE03: crear folio sin vincular escritura sigue funcionando (campo opcional)', async ({ page }) => {
    const numeroFolio = Math.floor(10000 + Math.random() * 90000)

    await page.goto('/dashboard/administracion/folios')
    await page.waitForLoadState('domcontentloaded')

    const dialog = await openNewFolioAndFillRequired(page, numeroFolio)

    // No escritura selected — leave the picker at "Sin vincular"
    await dialog.getByRole('button', { name: /guardar/i }).click()

    await expect(dialog).not.toBeVisible({ timeout: 8000 })
    await expect(page.locator('[data-sonner-toast]').getByText(/creado/i)).toBeVisible({ timeout: 5000 })

    const table = page.getByRole('table')
    const row = table.getByRole('row', { name: new RegExp(String(numeroFolio)) })
    await expect(row).toBeVisible({ timeout: 10000 })
    await expect(row).toContainText(/Nuevo/i)
  })
})

// ──────────────────────────────────────────────
// Viewport responsive tests — folios admin screen
// ──────────────────────────────────────────────

for (const viewport of [
  { width: 320, height: 568, label: '320px (mobile)' },
  { width: 768, height: 1024, label: '768px (tablet)' },
  { width: 1024, height: 768, label: '1024px (desktop)' },
]) {
  test(`folios admin screen is usable at ${viewport.label}`, async ({ page }) => {
    await authenticateAsAdmin(page)
    await page.setViewportSize({ width: viewport.width, height: viewport.height })

    await page.goto('/dashboard/administracion/folios')
    await page.waitForLoadState('domcontentloaded')

    await expect(page.getByRole('heading', { name: /folios/i }).first()).toBeVisible()
    await expect(page.getByTestId('btn-nuevo-folio')).toBeVisible()

    const noOverflow = await page.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth
    )
    expect(noOverflow).toBe(true)
  })
}
