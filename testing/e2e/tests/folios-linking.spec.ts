/**
 * CU87 - Link a deed, a folio, and a testimony copy
 * CU28 - Enter new folios
 * Issue #838
 */
import { type Page, test, expect } from '@playwright/test'
import { authenticateAsAdmin } from './setup/auth'
import { apiPost, createFolio, uniqueId } from './setup/api-helpers'

const STATUS_DEED_SIGNED = 'Firmada'

/** Seed a deed already in status Firmada (no folio linked) */
async function seedSignedDeed(page: Page): Promise<{ idDeed: number; number: number }> {
  const id = uniqueId()
  const result = await apiPost<{ idDeed: number }>(page, '/deeds', {
    number: id,
    dateDeedrecording: new Date().toISOString().split('T')[0],
    body: `Deed in status Firmada E2E ${id}`,
    status: STATUS_DEED_SIGNED,
    notes: 'Non-sequential numbering: isolated E2E data seed (CU86)',
  })
  if (!result.ok || !result.data?.idDeed) {
    throw new Error(`Failed to seed a deed in status Firmada: ${result.error ?? JSON.stringify(result.data)}`)
  }
  return { idDeed: result.data.idDeed, number: id }
}

/** Select the first option of an already-open Radix listbox */
async function pickFirstOption(page: Page) {
  const option = page.getByRole('listbox').getByRole('option').first()
  await option.waitFor({ state: 'attached', timeout: 5000 })
  await option.evaluate((el: HTMLElement) => el.click())
}

/** Open the new-folio dialog and fill the required fields (number, type, notary) */
async function openNewFolioAndFillRequired(page: Page, folioNumber: number) {
  await page.getByTestId('btn-new-folio').click()
  const dialog = page.getByRole('dialog')
  await expect(dialog).toBeVisible()

  await page.getByTestId('input-folio-number').fill(String(folioNumber))
  await page.getByTestId('select-folio-type').click()
  await pickFirstOption(page)
  await page.getByTestId('select-notary-folio').click()
  await pickFirstOption(page)

  return dialog
}

test.describe('CU87 - Link a deed and a folio', () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  test('CU87-GW01: golden path — creating a folio linked to a signed deed → the folio is Utilizado and the deed shows the folio', async ({
    page,
  }) => {
    const { number } = await seedSignedDeed(page)
    const folioNumber = Math.floor(10000 + Math.random() * 90000)

    await page.goto('/dashboard/administration/folios')
    await page.waitForLoadState('domcontentloaded')

    const dialog = await openNewFolioAndFillRequired(page, folioNumber)

    await page.getByTestId('select-deed-folio').click()
    const deedOption = page.getByRole('option', { name: `Escritura Nº ${number}` })
    await deedOption.waitFor({ state: 'attached', timeout: 5000 })
    await deedOption.evaluate((el: HTMLElement) => el.click())

    await dialog.getByRole('button', { name: /guardar/i }).click()

    await expect(dialog).not.toBeVisible({ timeout: 8000 })
    await expect(page.locator('[data-sonner-toast]').getByText(/creado/i)).toBeVisible({ timeout: 5000 })

    // Then: the new folio row shows status Utilizado and the linked deed number
    const table = page.getByRole('table')
    const row = table.getByRole('row', { name: new RegExp(String(folioNumber)) })
    await expect(row).toBeVisible({ timeout: 10000 })
    await expect(row).toContainText(/Utilizado/i)
    await expect(row).toContainText(new RegExp(String(number)))

    // And: the deed screen shows it linked to a folio
    await page.goto('/dashboard/deeds')
    await page.waitForLoadState('domcontentloaded')
    const deedRow = page.getByRole('row').filter({ hasText: new RegExp(String(number)) })
    await expect(deedRow).toBeVisible({ timeout: 10000 })
    await expect(deedRow).toContainText(/Folio #/i)
  })

  test('CU87-EDGE01: a deed already linked to another folio is not offered in the selector', async ({ page }) => {
    const { idDeed, number } = await seedSignedDeed(page)
    const linked = await createFolio(page, 1, { status: 'Nuevo', deedId: idDeed })
    if (!linked.ok) throw new Error(`Failed to seed linked folio: ${linked.error}`)

    await page.goto('/dashboard/administration/folios')
    await page.waitForLoadState('domcontentloaded')

    const dialog = await openNewFolioAndFillRequired(page, Math.floor(10000 + Math.random() * 90000))
    await page.getByTestId('select-deed-folio').click()

    // Then: the picker never offers an deed already Utilizado by another folio
    await expect(page.getByRole('option', { name: `Escritura Nº ${number}` })).toHaveCount(0)

    // Escape closes the open select; a second Escape (or the first, if focus is on
    // the dialog) dismisses the modal. Never click Cancelar after Escape — the
    // button is gone once the dialog is already closed (CI flake CU87-EDGE01).
    await page.keyboard.press('Escape')
    if (await dialog.isVisible().catch(() => false)) {
      await page.keyboard.press('Escape')
    }
    await expect(dialog).not.toBeVisible({ timeout: 5000 })
  })

  test('CU87-EDGE02: a folio in status Utilizado cannot be edited or deleted from the grid', async ({ page }) => {
    const { idDeed } = await seedSignedDeed(page)
    const folioNumber = Math.floor(10000 + Math.random() * 90000)
    const linked = await createFolio(page, 1, { number: folioNumber, status: 'Nuevo', deedId: idDeed })
    if (!linked.ok) throw new Error(`Failed to seed linked folio: ${linked.error}`)

    await page.goto('/dashboard/administration/folios')
    await page.waitForLoadState('domcontentloaded')

    const table = page.getByRole('table')
    const row = table.getByRole('row', { name: new RegExp(String(folioNumber)) })
    await expect(row).toBeVisible({ timeout: 10000 })
    await expect(row).toContainText(/Utilizado/i)

    await expect(row.getByTestId('btn-edit-folio')).toBeDisabled()
    await expect(row.getByTestId('btn-delete-folio')).toBeDisabled()
  })

  test('CU87-EDGE03: creating a folio without linking a deed still works (the field is optional)', async ({ page }) => {
    const folioNumber = Math.floor(10000 + Math.random() * 90000)

    await page.goto('/dashboard/administration/folios')
    await page.waitForLoadState('domcontentloaded')

    const dialog = await openNewFolioAndFillRequired(page, folioNumber)

    // No deed selected — leave the picker at "Sin vincular"
    await dialog.getByRole('button', { name: /guardar/i }).click()

    await expect(dialog).not.toBeVisible({ timeout: 8000 })
    await expect(page.locator('[data-sonner-toast]').getByText(/creado/i)).toBeVisible({ timeout: 5000 })

    const table = page.getByRole('table')
    const row = table.getByRole('row', { name: new RegExp(String(folioNumber)) })
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

    await page.goto('/dashboard/administration/folios')
    await page.waitForLoadState('domcontentloaded')

    await expect(page.getByRole('heading', { name: /folios/i }).first()).toBeVisible()
    await expect(page.getByTestId('btn-new-folio')).toBeVisible()

    const noOverflow = await page.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth
    )
    expect(noOverflow).toBe(true)
  })
}
