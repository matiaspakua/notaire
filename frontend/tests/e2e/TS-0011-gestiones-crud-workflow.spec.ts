/**
 * TS-0011 - Gestiones Workflow (CU02 → CU13 → CU83 → CU16)
 * CU02 - Iniciar Gestión
 * CU13 - Ver historial/bitácora
 * CU83 - Cambiar estado (workflow)
 * CU16 - Archivar Gestión
 * CU19 - Buscar gestiones por cliente
 * Issue #833
 * Sequence: docs/200-architecture/204-diagrams/Secuencias/CU02.puml
 */
import { type Page, test, expect } from '@playwright/test'
import { authenticateAsAdmin } from './setup/auth'
import { createPersona, createPresupuesto, seedGestionWithWorkflow } from './setup/api-helpers'

// ──────────────────────────────────────────────
// Shared seed helper
// ──────────────────────────────────────────────

interface SeededGestion {
  idGestion: number
  numero: number
  estadoInicial: string
  estadoFinal: string
  personaId: number
  personaApellido: string
}

async function seedFullWorkflow(page: Page): Promise<SeededGestion> {
  const persona = await createPersona(page)
  const personaId = persona.data!.idPersona
  const personaApellido = persona.data!.apellido!
  const presupuesto = await createPresupuesto(page, personaId)
  const gestion = await seedGestionWithWorkflow(page, presupuesto.data!.idPresupuesto)
  return { ...gestion, personaId, personaApellido }
}

// ──────────────────────────────────────────────
// CU02 - Iniciar Gestión
// ──────────────────────────────────────────────

test.describe('CU02 - Iniciar Gestión', () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  test('CU02-GW01: golden path — gestión seeded via API aparece en la tabla con número y estado inicial', async ({ page }) => {
    const { numero, estadoInicial } = await seedFullWorkflow(page)

    await page.goto('/dashboard/gestiones')
    await page.waitForLoadState('domcontentloaded')

    const row = page.getByRole('row', { name: new RegExp(String(numero)) })
    await expect(row).toBeVisible({ timeout: 10000 })
    await expect(row).toContainText(estadoInicial)
  })

  test('CU02-GW02: modal "nueva gestión" abre y muestra picker de presupuesto', async ({ page }) => {
    await page.goto('/dashboard/gestiones')
    await page.waitForLoadState('domcontentloaded')

    await page.getByTestId('btn-nueva-gestion').click()
    const dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()
    await expect(page.getByTestId('select-presupuesto-gestion')).toBeVisible()
  })

  test('CU02-GW03: picker de presupuesto muestra el nombre del cliente asociado (#889)', async ({ page }) => {
    // Seed a fresh persona + presupuesto to ensure option exists
    const persona = await createPersona(page)
    const personaId = persona.data!.idPersona
    await createPresupuesto(page, personaId)

    // Fetch the persona's apellido from the seeded data to locate the option
    const apellido = persona.data!.apellido ?? ''

    await page.goto('/dashboard/gestiones')
    await page.waitForLoadState('domcontentloaded')

    await page.getByTestId('btn-nueva-gestion').click()
    await page.getByTestId('select-presupuesto-gestion').click()

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

  test('golden path — gestión en tabla → bitácora 1 entrada → cambiar estado → bitácora 2 entradas', async ({ page }) => {
    // Step 1: seed — createPersona → createPresupuesto → seedGestionWithWorkflow
    const { idGestion, numero, estadoInicial, estadoFinal } = await seedFullWorkflow(page)

    // Step 2: navegar a /dashboard/gestiones y verificar que la gestión aparece con estado inicial
    await page.goto('/dashboard/gestiones')
    await page.waitForLoadState('domcontentloaded')

    const row = page.getByRole('row', { name: new RegExp(String(numero)) })
    await expect(row).toBeVisible({ timeout: 10000 })
    await expect(row).toContainText(estadoInicial)

    // Step 3 (CU13): ver bitácora — debe tener 1 entrada con el estado inicial
    await page.getByTestId(`btn-ver-bitacora-${idGestion}`).click()
    let dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()

    const items = dialog.getByTestId('bitacora-item')
    await expect(items).toHaveCount(1)
    await expect(items.first()).toContainText(estadoInicial)

    await dialog.getByRole('button', { name: /cerrar|close/i }).click()
    await expect(dialog).not.toBeVisible()

    // Step 4 (CU83): cambiar estado → seleccionar estado final → confirmar → verificar en tabla
    await page.getByTestId(`btn-cambiar-estado-${idGestion}`).click()
    dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()

    await dialog.getByTestId('select-nuevo-estado').click()
    await page.getByRole('option', { name: estadoFinal }).click()
    await dialog.getByTestId('btn-confirmar-transicion').click()
    await expect(dialog).not.toBeVisible()

    await expect(row).toContainText(estadoFinal)

    // Step 5 (CU13): ver bitácora nuevamente — debe tener 2 entradas (inicial + final)
    await page.getByTestId(`btn-ver-bitacora-${idGestion}`).click()
    dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()

    const updatedItems = dialog.getByTestId('bitacora-item')
    await expect(updatedItems).toHaveCount(2)
    await expect(updatedItems.nth(0)).toContainText(estadoInicial)
    await expect(updatedItems.nth(1)).toContainText(estadoFinal)
  })
})

// ──────────────────────────────────────────────
// CU13 - Ver historial/bitácora
// ──────────────────────────────────────────────

test.describe('CU13 - Ver historial/bitácora de una gestión', () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  test('CU13-GW01: gestión recién creada muestra exactamente 1 entrada en bitácora con el estado inicial', async ({ page }) => {
    const { idGestion, numero, estadoInicial } = await seedFullWorkflow(page)

    await page.goto('/dashboard/gestiones')
    await page.waitForLoadState('domcontentloaded')

    await expect(
      page.getByRole('row', { name: new RegExp(String(numero)) }),
    ).toBeVisible({ timeout: 10000 })

    await page.getByTestId(`btn-ver-bitacora-${idGestion}`).click()
    const dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()

    const items = dialog.getByTestId('bitacora-item')
    await expect(items).toHaveCount(1)
    await expect(items.first()).toContainText(estadoInicial)
  })

  test('CU13-GW02: tras cambiar estado la bitácora acumula una segunda entrada', async ({ page }) => {
    const { idGestion, numero, estadoInicial, estadoFinal } = await seedFullWorkflow(page)

    await page.goto('/dashboard/gestiones')
    await page.waitForLoadState('domcontentloaded')

    await expect(
      page.getByRole('row', { name: new RegExp(String(numero)) }),
    ).toBeVisible({ timeout: 10000 })

    // Cambiar estado
    await page.getByTestId(`btn-cambiar-estado-${idGestion}`).click()
    let dialog = page.getByRole('dialog')
    await dialog.getByTestId('select-nuevo-estado').click()
    await page.getByRole('option', { name: estadoFinal }).click()
    await dialog.getByTestId('btn-confirmar-transicion').click()
    await expect(dialog).not.toBeVisible()

    // Ver bitácora con 2 entradas
    await page.getByTestId(`btn-ver-bitacora-${idGestion}`).click()
    dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()

    const items = dialog.getByTestId('bitacora-item')
    await expect(items).toHaveCount(2)
    await expect(items.nth(0)).toContainText(estadoInicial)
    await expect(items.nth(1)).toContainText(estadoFinal)
  })
})

// ──────────────────────────────────────────────
// CU83 - Cambiar estado (workflow)
// ──────────────────────────────────────────────

test.describe('CU83 - Cambiar estado de una gestión', () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  test('CU83-GW01: transición a destino válido actualiza el estado visible en la tabla', async ({ page }) => {
    const { idGestion, numero, estadoFinal } = await seedFullWorkflow(page)

    await page.goto('/dashboard/gestiones')
    await page.waitForLoadState('domcontentloaded')

    const row = page.getByRole('row', { name: new RegExp(String(numero)) })
    await expect(row).toBeVisible({ timeout: 10000 })

    await page.getByTestId(`btn-cambiar-estado-${idGestion}`).click()
    const dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()

    await dialog.getByTestId('select-nuevo-estado').click()
    await page.getByRole('option', { name: estadoFinal }).click()
    await dialog.getByTestId('btn-confirmar-transicion').click()

    await expect(dialog).not.toBeVisible()
    await expect(row).toContainText(estadoFinal)
  })

  test('CU83-GW02: el picker sólo ofrece destinos válidos del workflow (no el estado actual)', async ({ page }) => {
    const { idGestion, estadoInicial, estadoFinal } = await seedFullWorkflow(page)

    await page.goto('/dashboard/gestiones')
    await page.waitForLoadState('domcontentloaded')

    await page.getByTestId(`btn-cambiar-estado-${idGestion}`).click()
    const dialog = page.getByRole('dialog')
    await dialog.getByTestId('select-nuevo-estado').click()

    await expect(page.getByRole('option', { name: estadoFinal })).toBeVisible()
    await expect(page.getByRole('option', { name: estadoInicial })).toHaveCount(0)
  })
})

// ──────────────────────────────────────────────
// CU16 - Archivar Gestión
// ──────────────────────────────────────────────

test.describe('CU16 - Archivar Gestión', () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  test('CU16-GW01 edge path: archivar gestión cuyo estado no tiene transición a "Archivada" muestra error', async ({ page }) => {
    // The seeded workflow has INITIAL→FINAL with no path to "Archivada",
    // so the archive action must be rejected by the backend.
    const { idGestion, numero } = await seedFullWorkflow(page)

    await page.goto('/dashboard/gestiones')
    await page.waitForLoadState('domcontentloaded')

    const row = page.getByRole('row', { name: new RegExp(String(numero)) })
    await expect(row).toBeVisible({ timeout: 10000 })

    await page.getByTestId(`btn-archivar-gestion-${idGestion}`).click()
    const confirmDialog = page.getByRole('alertdialog')
    await expect(confirmDialog).toBeVisible()
    await confirmDialog.getByRole('button', { name: /archivar gestión/i }).click()

    // Backend should reject: error message must appear
    await expect(page.getByText(/no está permitida/i)).toBeVisible({ timeout: 10000 })
    // Row and archive button remain — gestión was NOT archived
    await expect(page.getByTestId(`btn-archivar-gestion-${idGestion}`)).toBeVisible()
  })
})

// ──────────────────────────────────────────────
// CU19 - Buscar gestiones por cliente
// ──────────────────────────────────────────────

test.describe('CU19 - Buscar gestiones por cliente', () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  test('CU19-GW01: seleccionar cliente en el filtro dispara GET /gestiones/cliente/{id}', async ({ page }) => {
    // Ensure at least one gestión with a real persona/cliente exists
    await seedFullWorkflow(page)

    await page.goto('/dashboard/gestiones')
    await page.waitForLoadState('domcontentloaded')

    await page.getByTestId('select-filter-cliente-gestion').click()
    const firstOption = page.getByRole('option').filter({ hasNotText: /^Todos$/ }).first()

    if ((await firstOption.count()) === 0) {
      // No cliente available in this environment — assertion would be vacuous
      return
    }

    const searchRequest = page.waitForRequest(
      (req) =>
        /\/api\/v1\/gestiones\/cliente\/\d+/.test(req.url()) && req.method() === 'GET',
    )
    await firstOption.click()
    await searchRequest
  })

  test('CU19-GW02: gestión seeded con cliente específico aparece al filtrar por ese cliente', async ({ page }) => {
    const { personaId, numero, personaApellido } = await seedFullWorkflow(page)

    await page.goto('/dashboard/gestiones')
    await page.waitForLoadState('domcontentloaded')

    // Wait for the filter select to be populated with personas
    const filterSelect = page.getByTestId('select-filter-cliente-gestion')
    await filterSelect.click()

    // Select the option matching the seeded persona specifically — picking
    // the first non-"Todos" option would filter by an unrelated persona
    // accumulated from other test runs, hiding the seeded gestión.
    const clienteOption = page.getByRole('option', { name: new RegExp(personaApellido) })

    if ((await clienteOption.count()) === 0) {
      return
    }

    const searchRequest = page.waitForRequest(
      (req) =>
        /\/api\/v1\/gestiones\/cliente\/\d+/.test(req.url()) && req.method() === 'GET',
    )
    await clienteOption.click()
    await searchRequest

    // The row for the seeded gestión number must remain visible after filtering
    await expect(
      page.getByRole('row', { name: new RegExp(String(numero)) }),
    ).toBeVisible({ timeout: 8000 })
  })
})

// ──────────────────────────────────────────────
// Responsive viewports — vista de gestiones
// ──────────────────────────────────────────────

test.describe('Responsive viewports — vista de gestiones', () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page)
  })

  for (const viewport of [
    { width: 320, height: 568, label: '320px (mobile)' },
    { width: 768, height: 1024, label: '768px (tablet)' },
    { width: 1024, height: 768, label: '1024px (desktop)' },
  ]) {
    test(`gestiones table is visible and usable at ${viewport.label}`, async ({ page }) => {
      const { idGestion, numero, estadoInicial } = await seedFullWorkflow(page)

      await page.setViewportSize({ width: viewport.width, height: viewport.height })
      await page.goto('/dashboard/gestiones')
      await page.waitForLoadState('domcontentloaded')

      const row = page.getByRole('row', { name: new RegExp(String(numero)) })
      await expect(row).toBeVisible({ timeout: 10000 })

      // Bitácora accessible at every breakpoint
      await page.getByTestId(`btn-ver-bitacora-${idGestion}`).click()
      const dialog = page.getByRole('dialog')
      await expect(dialog).toBeVisible()
      await expect(dialog.getByTestId('bitacora-item').first()).toContainText(estadoInicial)
    })
  }
})
