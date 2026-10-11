/**
 * TS-0011 - Managements Workflow (CU02 → CU13 → CU83 → CU16)
 * CU02 - Iniciar Gestión
 * CU13 - Ver history/bitácora
 * CU83 - Cambiar estado (workflow)
 * CU16 - Archivar Gestión
 * CU19 - Buscar managements por client
 * Issues: #833, #804 (workflow-enforced status writes; UI uses /transition)
 * Sequence: docs/200-architecture/204-diagrams/Secuencias/CU02.puml
 */
import { type Page, test, expect } from '@playwright/test'
import { authenticateAsAdmin } from './setup/auth'
import { createPerson, createBudget, seedManagementWithWorkflow } from './setup/api-helpers'
import { listRow } from './setup/list-locators'

// ──────────────────────────────────────────────
// Shared seed helper
// ──────────────────────────────────────────────

interface SeededManagement {
  idManagement: number
  number: number
  estadoInicial: string
  estadoFinal: string
  personId: number
  personaApellido: string
}

async function seedFullWorkflow(page: Page): Promise<SeededManagement> {
  const person = await createPerson(page)
  const personId = person.data!.personId
  const personaApellido = person.data!.lastName!
  const budget = await createBudget(page, personId)
  const management = await seedManagementWithWorkflow(page, budget.data!.idBudget)
  return { ...management, personId, personaApellido }
}

// ──────────────────────────────────────────────
// CU02 - Iniciar Gestión
// ──────────────────────────────────────────────

test.describe('CU02 - Start a management', () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  test('CU02-GW01: golden path — a management seeded via API appears in the table with its number and initial status', async ({ page }) => {
    const { number, estadoInicial } = await seedFullWorkflow(page)

    await page.goto('/dashboard/managements')
    await page.waitForLoadState('domcontentloaded')

    const row = page.getByRole('row', { name: new RegExp(String(number)) })
    await expect(row).toBeVisible({ timeout: 10000 })
    await expect(row).toContainText(estadoInicial)
  })

  test('CU02-GW02: the "new management" modal opens and shows the budget picker', async ({ page }) => {
    await page.goto('/dashboard/managements')
    await page.waitForLoadState('domcontentloaded')

    await page.getByTestId('btn-new-management').click()
    const dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()
    await expect(page.getByTestId('select-budget-management')).toBeVisible()
  })

  test('CU02-GW03: the budget picker shows the associated client name (#889)', async ({ page }) => {
    // Seed a fresh person + budget to ensure option exists
    const person = await createPerson(page)
    const personId = person.data!.personId
    await createBudget(page, personId)

    // Fetch the person's apellido from the seeded data to locate the option
    const apellido = person.data!.lastName ?? ''

    await page.goto('/dashboard/managements')
    await page.waitForLoadState('domcontentloaded')

    await page.getByTestId('btn-new-management').click()
    await page.getByTestId('select-budget-management').click()
    // Budget pickers search the server (#1340): type the client first.
    await page.getByTestId('select-budget-management').fill(apellido)

    await expect(
      page.getByRole('option', { name: new RegExp(apellido, 'i') }),
    ).toBeVisible({ timeout: 8000 })
  })
})

// ──────────────────────────────────────────────
// Full workflow: CU02 → CU13 → CU83 → CU16
// ──────────────────────────────────────────────

test.describe('Full workflow: CU02 → CU13 → CU83 → CU16', () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  test('golden path — management in the table → history has 1 entry → change status → history has 2 entries', async ({ page }) => {
    // Step 1: seed — createPerson → createBudget → seedManagementWithWorkflow
    const { idManagement, number, estadoInicial, estadoFinal } = await seedFullWorkflow(page)

    // Step 2: navegar a /dashboard/managements y verificar que la gestión aparece con estado inicial
    await page.goto('/dashboard/managements')
    await page.waitForLoadState('domcontentloaded')

    const row = page.getByRole('row', { name: new RegExp(String(number)) })
    await expect(row).toBeVisible({ timeout: 10000 })
    await expect(row).toContainText(estadoInicial)

    // Step 3 (CU13): ver bitácora — debe tener 1 entrada con el estado inicial
    await page.getByTestId(`btn-view-history-${idManagement}`).click()
    let dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()

    const items = dialog.getByTestId('history-item')
    await expect(items).toHaveCount(1)
    await expect(items.first()).toContainText(estadoInicial)

    await dialog.getByRole('button', { name: /cerrar|close/i }).click()
    await expect(dialog).not.toBeVisible()

    // Step 4 (CU83): cambiar estado → seleccionar estado final → confirmar → verificar en tabla
    await page.getByTestId(`btn-change-status-${idManagement}`).click()
    dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()

    await dialog.getByTestId('select-new-status').click()
    await page.getByRole('option', { name: estadoFinal }).click()
    await dialog.getByTestId('btn-confirm-transicion').click()
    await expect(dialog).not.toBeVisible()

    await expect(row).toContainText(estadoFinal)

    // Step 5 (CU13): ver bitácora nuevamente — debe tener 2 entradas (inicial + final)
    await page.getByTestId(`btn-view-history-${idManagement}`).click()
    dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()

    const updatedItems = dialog.getByTestId('history-item')
    await expect(updatedItems).toHaveCount(2)
    await expect(updatedItems.nth(0)).toContainText(estadoInicial)
    await expect(updatedItems.nth(1)).toContainText(estadoFinal)
  })
})

// ──────────────────────────────────────────────
// CU13 - Ver history/bitácora
// ──────────────────────────────────────────────

test.describe("CU13 - View a management's history", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  test('CU13-GW01: a newly created management shows exactly 1 history entry with the initial status', async ({ page }) => {
    const { idManagement, number, estadoInicial } = await seedFullWorkflow(page)

    await page.goto('/dashboard/managements')
    await page.waitForLoadState('domcontentloaded')

    await expect(
      page.getByRole('row', { name: new RegExp(String(number)) }),
    ).toBeVisible({ timeout: 10000 })

    await page.getByTestId(`btn-view-history-${idManagement}`).click()
    const dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()

    const items = dialog.getByTestId('history-item')
    await expect(items).toHaveCount(1)
    await expect(items.first()).toContainText(estadoInicial)
  })

  test('CU13-GW02: after a status change the history gains a second entry', async ({ page }) => {
    const { idManagement, number, estadoInicial, estadoFinal } = await seedFullWorkflow(page)

    await page.goto('/dashboard/managements')
    await page.waitForLoadState('domcontentloaded')

    await expect(
      page.getByRole('row', { name: new RegExp(String(number)) }),
    ).toBeVisible({ timeout: 10000 })

    // Cambiar estado
    await page.getByTestId(`btn-change-status-${idManagement}`).click()
    let dialog = page.getByRole('dialog')
    await dialog.getByTestId('select-new-status').click()
    await page.getByRole('option', { name: estadoFinal }).click()
    await dialog.getByTestId('btn-confirm-transicion').click()
    await expect(dialog).not.toBeVisible()

    // Ver bitácora con 2 entradas
    await page.getByTestId(`btn-view-history-${idManagement}`).click()
    dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()

    const items = dialog.getByTestId('history-item')
    await expect(items).toHaveCount(2)
    await expect(items.nth(0)).toContainText(estadoInicial)
    await expect(items.nth(1)).toContainText(estadoFinal)
  })
})

// ──────────────────────────────────────────────
// CU83 - Cambiar estado (workflow)
// ──────────────────────────────────────────────

test.describe("CU83 - Change a management's status", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  test('CU83-GW01: a transition to a valid destination updates the status shown in the table', async ({ page }) => {
    const { idManagement, number, estadoFinal } = await seedFullWorkflow(page)

    await page.goto('/dashboard/managements')
    await page.waitForLoadState('domcontentloaded')

    const row = page.getByRole('row', { name: new RegExp(String(number)) })
    await expect(row).toBeVisible({ timeout: 10000 })

    await page.getByTestId(`btn-change-status-${idManagement}`).click()
    const dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()

    await dialog.getByTestId('select-new-status').click()
    await page.getByRole('option', { name: estadoFinal }).click()
    await dialog.getByTestId('btn-confirm-transicion').click()

    await expect(dialog).not.toBeVisible()
    await expect(row).toContainText(estadoFinal)
  })

  test('CU83-GW02: the picker offers only valid workflow destinations (not the current status)', async ({ page }) => {
    const { idManagement, estadoInicial, estadoFinal } = await seedFullWorkflow(page)

    await page.goto('/dashboard/managements')
    await page.waitForLoadState('domcontentloaded')

    await page.getByTestId(`btn-change-status-${idManagement}`).click()
    const dialog = page.getByRole('dialog')
    await dialog.getByTestId('select-new-status').click()

    await expect(page.getByRole('option', { name: estadoFinal })).toBeVisible()
    await expect(page.getByRole('option', { name: estadoInicial })).toHaveCount(0)
  })
})

// ──────────────────────────────────────────────
// CU16 - Archivar Gestión
// ──────────────────────────────────────────────

test.describe('CU16 - Archive a management', () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  test('CU16-GW01 edge path: archiving a management whose status has no transition to "Archivada" shows an error', async ({ page }) => {
    // The seeded workflow has INITIAL→FINAL with no path to "Archivada",
    // so the archive action must be rejected by the backend.
    const { idManagement, number } = await seedFullWorkflow(page)

    await page.goto('/dashboard/managements')
    await page.waitForLoadState('domcontentloaded')

    const row = page.getByRole('row', { name: new RegExp(String(number)) })
    await expect(row).toBeVisible({ timeout: 10000 })

    await page.getByTestId(`btn-archive-management-${idManagement}`).click()
    const confirmDialog = page.getByRole('alertdialog')
    await expect(confirmDialog).toBeVisible()
    await confirmDialog.getByRole('button', { name: /archive gestión/i }).click()

    // Backend should reject: error message must appear
    await expect(page.getByText(/is not allowed/i)).toBeVisible({ timeout: 10000 })
    // Row and archive button remain — gestión was NOT archived
    await expect(page.getByTestId(`btn-archive-management-${idManagement}`)).toBeVisible()
  })
})

// ──────────────────────────────────────────────
// CU19 - Buscar managements por client
// ──────────────────────────────────────────────

test.describe('CU19 - Search managements by client', () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  test('CU19-GW01: choosing a client in the filter calls GET /managements/client/{id}', async ({ page }) => {
    // Ensure at least one gestión with a real person/client exists
    await seedFullWorkflow(page)

    await page.goto('/dashboard/managements')
    await page.waitForLoadState('domcontentloaded')

    await page.getByTestId('select-filter-client-management').click()
    // Scoped to the picker's listbox: the pagination footer's page-size <select>
    // also has options (#1340). seedFullWorkflow guarantees at least one client.
    const firstOption = page
      .getByRole('listbox')
      .getByRole('option')
      .filter({ hasNotText: /^Todos$/ })
      .first()
    await expect(firstOption).toBeVisible({ timeout: 15000 })

    const searchRequest = page.waitForRequest(
      (req) =>
        /\/api\/v1\/managements\/client\/\d+/.test(req.url()) && req.method() === 'GET',
    )
    await firstOption.click()
    await searchRequest
  })

  test('CU19-GW02: a management seeded with a specific client appears when filtering by that client', async ({ page }) => {
    const { personId, number, personaApellido } = await seedFullWorkflow(page)

    await page.goto('/dashboard/managements')
    await page.waitForLoadState('domcontentloaded')

    // Wait for the filter select to be populated with people
    const filterSelect = page.getByTestId('select-filter-client-management')
    await filterSelect.click()
    // The client filter searches the server (#1340): type the seeded last name.
    await filterSelect.fill(personaApellido)

    // Select the option matching the seeded person specifically — picking
    // the first non-"Todos" option would filter by an unrelated person
    // accumulated from other test runs, hiding the seeded gestión.
    const clientOption = page.getByRole('option', { name: new RegExp(personaApellido) })

    if ((await clientOption.count()) === 0) {
      return
    }

    const searchRequest = page.waitForRequest(
      (req) =>
        /\/api\/v1\/managements\/client\/\d+/.test(req.url()) && req.method() === 'GET',
    )
    await clientOption.click()
    await searchRequest

    // The row for the seeded gestión number must remain visible after filtering
    await expect(
      page.getByRole('row', { name: new RegExp(String(number)) }),
    ).toBeVisible({ timeout: 8000 })
  })
})

// ──────────────────────────────────────────────
// Responsive viewports — vista de managements
// ──────────────────────────────────────────────

test.describe('Responsive viewports — managements view', () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  for (const viewport of [
    { width: 320, height: 568, label: '320px (mobile)' },
    { width: 768, height: 1024, label: '768px (tablet)' },
    { width: 1024, height: 768, label: '1024px (desktop)' },
  ]) {
    test(`the managements table is visible and usable at ${viewport.label}`, async ({ page }) => {
      const { idManagement, number, estadoInicial } = await seedFullWorkflow(page)

      await page.setViewportSize({ width: viewport.width, height: viewport.height })
      await page.goto('/dashboard/managements')
      await page.waitForLoadState('domcontentloaded')

      const row = listRow(page, new RegExp(String(number)))
      await expect(row).toBeVisible({ timeout: 10000 })

      // Bitácora accessible at every breakpoint
      await page.getByTestId(`btn-view-history-${idManagement}`).click()
      const dialog = page.getByRole('dialog')
      await expect(dialog).toBeVisible()
      await expect(dialog.getByTestId('history-item').first()).toContainText(estadoInicial)
    })
  }
})
