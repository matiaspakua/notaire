/**
 * TS-0015 - Personas y Clientes Workflow (CU17 → CU18 → CU61)
 * CU17 - Dar Alta Persona
 * CU18 - Dar Alta Cliente (via checkbox 'es cliente')
 * CU41 - Modificar Cliente
 * CU61 - Buscar persona o cliente
 * Issue #835 (validación duplicados por DNI)
 * Sequence: docs/200-architecture/204-diagrams/Secuencias/CU17.puml
 */
import { type Page, test, expect } from '@playwright/test'
import { GherkinSteps } from './gherkin-helpers'
import { authenticateAsAdmin } from './setup/auth'
import { createPersona, uniqueId } from './setup/api-helpers'

// ---------------------------------------------------------------------------
// NOTE ON CU18 MECHANISM
// ---------------------------------------------------------------------------
// CU18 "Dar Alta Cliente" is NOT a separate button or screen. The "Es cliente"
// checkbox inside the persona creation/edit form is the sole mechanism: checking
// it during persona creation (CU17) or editing (CU41) simultaneously registers
// the person as a client. There is no standalone "client upgrade" flow.
// Tests that previously used test.skip() for CU18 have been replaced with
// real assertions that verify this checkbox-driven contract.
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// Shared helpers
// ---------------------------------------------------------------------------

async function loginAndGoToPersonas(page: Page): Promise<void> {
  const steps = new GherkinSteps(page)
  await authenticateAsAdmin(page)
  await steps.givenUserIsOnPage('/dashboard/personas')
}

/** Opens "Nueva persona" modal and fills the required fields.
 *  `esCliente` controls whether the checkbox is checked (CU18 path). */
async function fillPersonaForm(
  page: Page,
  opts: {
    nombre: string
    apellido: string
    dni: string
    esCliente?: boolean
  },
): Promise<void> {
  await page.getByTestId('btn-nueva-persona').click()
  const dialog = page.getByRole('dialog')
  await expect(dialog).toBeVisible()

  await page.getByTestId('input-nombre').fill(opts.nombre)
  await page.getByTestId('input-apellido').fill(opts.apellido)
  await dialog.getByLabel(/dni/i).fill(opts.dni)

  if (opts.esCliente) {
    // CU18: checking this checkbox registers the person as a client at creation time.
    const checkbox = dialog.getByTestId('check-es-cliente')
    await checkbox.check()
    await expect(checkbox).toBeChecked()
  }
}

/** Submits the currently open form. */
async function submitForm(page: Page): Promise<void> {
  await page
    .getByRole('button', { name: /confirmar|guardar|crear|registrar|actualizar/i })
    .click()
}

// ---------------------------------------------------------------------------
// CU17 - Dar Alta Persona (Golden Path)
// ---------------------------------------------------------------------------

test.describe('CU17 - Dar Alta Persona', () => {
  test.beforeEach(async ({ page }) => {
    await loginAndGoToPersonas(page)
  })

  test(
    'CU17-GW01: btn-nueva-persona abre el formulario con los campos requeridos',
    async ({ page }) => {
      // When
      await page.getByTestId('btn-nueva-persona').click()

      // Then — modal visible with required fields
      const dialog = page.getByRole('dialog')
      await expect(dialog).toBeVisible()
      await expect(page.getByTestId('input-nombre')).toBeVisible()
      await expect(page.getByTestId('input-apellido')).toBeVisible()
      await expect(dialog.getByLabel(/dni/i)).toBeVisible()

      // CU18 mechanism: checkbox must be present in the creation form
      await expect(dialog.getByTestId('check-es-cliente')).toBeVisible()
    },
  )

  test(
    'CU17-GW02: crear persona sin marcar es-cliente → persona aparece en tabla, NO como cliente',
    async ({ page }) => {
      const dni = `17${uniqueId() % 10_000_000}`
      const apellido = `Alta${uniqueId() % 100_000}`

      // When — create without the es-cliente checkbox
      await fillPersonaForm(page, {
        nombre: 'Carlos',
        apellido,
        dni,
        esCliente: false,
      })
      await submitForm(page)

      // Then — success toast and table shows new persona
      await expect(
        page.locator('[data-sonner-toast]').getByText(/creada/i),
      ).toBeVisible({ timeout: 8000 })

      await page.getByTestId('input-search-apellido').fill(apellido)
      await expect(page.getByRole('table')).toBeVisible()
      await expect(
        page.getByRole('table').getByText(new RegExp(apellido, 'i')),
      ).toBeVisible()
    },
  )

  test(
    'CU17-GW03: crear persona con es-cliente marcado (CU17 + CU18) → aparece en tabla con flag cliente',
    async ({ page }) => {
      // NOTE (CU18): the 'es cliente' checkbox IS the CU18 registration mechanism.
      // Creating a persona with this checkbox checked simultaneously registers
      // the person as a client. No separate step is needed.
      const dni = `18${uniqueId() % 10_000_000}`
      const apellido = `Cliente${uniqueId() % 100_000}`

      // When — create WITH the es-cliente checkbox (CU17 + CU18 in one step)
      await fillPersonaForm(page, {
        nombre: 'María',
        apellido,
        dni,
        esCliente: true,
      })
      await submitForm(page)

      // Then — persona created successfully
      await expect(
        page.locator('[data-sonner-toast]').getByText(/creada/i),
      ).toBeVisible({ timeout: 8000 })

      // And — persona appears in the table
      await page.getByTestId('input-search-apellido').fill(apellido)
      await expect(page.getByRole('table')).toBeVisible()
      const row = page.getByRole('row', { name: new RegExp(apellido, 'i') })
      await expect(row).toBeVisible()

      // And — the row indicates the person is a client
      // (badge, icon, or "Sí" in the "Cliente" column)
      await expect(row.getByText(/^cliente$|^sí$/i)).toBeVisible()
    },
  )
})

// ---------------------------------------------------------------------------
// CU18 - Dar Alta Cliente (via checkbox mechanism)
// ---------------------------------------------------------------------------

test.describe('CU18 - Dar Alta Cliente via checkbox es-cliente', () => {
  // NOTE: CU18 is exercised through the persona form's 'es cliente' checkbox.
  // There is no separate button, screen, or API flow for client registration.
  // All tests here are real, not skipped.

  test.beforeEach(async ({ page }) => {
    await loginAndGoToPersonas(page)
  })

  test(
    'CU18-GW01: persona creada con checkbox es-cliente aparece como cliente en la vista',
    async ({ page }) => {
      const dni = `CU18${uniqueId() % 10_000_000}`
      const apellido = `EsCliente${uniqueId() % 100_000}`

      // When — persona created with es-cliente checked (the CU18 mechanism)
      await fillPersonaForm(page, {
        nombre: 'Lucía',
        apellido,
        dni,
        esCliente: true,
      })
      await submitForm(page)

      // Then — success
      await expect(
        page.locator('[data-sonner-toast]').getByText(/creada/i),
      ).toBeVisible({ timeout: 8000 })

      // And — searching for the persona shows her in the table as a client
      await page.getByTestId('input-search-apellido').fill(apellido)
      await expect(page.getByRole('table')).toBeVisible()

      const row = page.getByRole('row', { name: new RegExp(apellido, 'i') })
      await expect(row).toBeVisible()
      // The row must reflect client status (column value, badge, or icon)
      await expect(row.getByText(/^cliente$|^sí$/i)).toBeVisible()
    },
  )

  test(
    'CU18-GW02: persona sin checkbox es-cliente puede ser promovida a cliente via edición (CU41)',
    async ({ page }) => {
      // NOTE (CU41): editing an existing persona and checking 'es cliente'
      // promotes them to client status. This is the CU41 path.
      const dni = `CU41${uniqueId() % 10_000_000}`
      const apellido = `Promover${uniqueId() % 100_000}`

      // Given — persona created without es-cliente
      await fillPersonaForm(page, {
        nombre: 'Pedro',
        apellido,
        dni,
        esCliente: false,
      })
      await submitForm(page)
      await expect(
        page.locator('[data-sonner-toast]').getByText(/creada/i),
      ).toBeVisible({ timeout: 8000 })

      // When — open the persona for editing
      await page.getByTestId('input-search-apellido').fill(apellido)
      await expect(page.getByRole('table')).toBeVisible()
      const row = page.getByRole('row', { name: new RegExp(apellido, 'i') })
      await row.getByRole('button').first().click()

      const editDialog = page.getByRole('dialog')
      await expect(editDialog).toBeVisible()

      // And — check the es-cliente checkbox to promote the person (CU18/CU41)
      const checkbox = editDialog.getByTestId('check-es-cliente')
      await checkbox.check()
      await expect(checkbox).toBeChecked()
      await submitForm(page)

      // Then — update succeeds
      await expect(
        page.locator('[data-sonner-toast]').getByText(/actualizada/i),
      ).toBeVisible({ timeout: 8000 })

      // And — persona now shows as client
      await page.getByTestId('input-search-apellido').fill(apellido)
      const updatedRow = page.getByRole('row', { name: new RegExp(apellido, 'i') })
      await expect(updatedRow.getByText(/cliente|sí/i)).toBeVisible()
    },
  )
})

// ---------------------------------------------------------------------------
// Issue #835 - Deduplicación por DNI
// ---------------------------------------------------------------------------

test.describe('Issue #835 - Deduplicación por DNI', () => {
  test.beforeEach(async ({ page }) => {
    await loginAndGoToPersonas(page)
  })

  test(
    'Dedup-GW01: crear segunda persona con mismo DNI → error de duplicado visible',
    async ({ page }) => {
      const dni = `DUP${uniqueId() % 10_000_000}`

      // Given — first persona created successfully
      await fillPersonaForm(page, { nombre: 'Original', apellido: 'Duplicado', dni })
      await submitForm(page)
      await expect(
        page.locator('[data-sonner-toast]').getByText(/creada/i),
      ).toBeVisible({ timeout: 8000 })

      // When — attempt to create a second persona with the same DNI
      await fillPersonaForm(page, { nombre: 'Otra', apellido: 'Persona', dni })
      await submitForm(page)

      // Then — error toast references duplicate document
      await expect(
        page.locator('[data-sonner-toast]').getByText(/ya existe/i),
      ).toBeVisible({ timeout: 5000 })

      // And — form stays open (data not lost)
      await expect(page.getByRole('dialog')).toBeVisible()
      await expect(page.getByTestId('input-nombre')).toHaveValue('Otra')
    },
  )

  test(
    'Dedup-GW02: toast de duplicado incluye enlace a la persona existente',
    async ({ page }) => {
      const dni = `DUP2${uniqueId() % 10_000_000}`

      await fillPersonaForm(page, { nombre: 'Primero', apellido: 'Exist', dni })
      await submitForm(page)
      await expect(
        page.locator('[data-sonner-toast]').getByText(/creada/i),
      ).toBeVisible({ timeout: 8000 })

      await fillPersonaForm(page, { nombre: 'Segundo', apellido: 'Dup', dni })
      await submitForm(page)

      // Then — link to existing persona is shown in the toast
      await expect(
        page.locator('[data-sonner-toast]').getByText(/ver persona existente/i),
      ).toBeVisible({ timeout: 5000 })
    },
  )

  test(
    'Dedup-EDGE: crear persona sin DNI → error de validación en el form',
    async ({ page }) => {
      // When — open form and submit without filling DNI
      await page.getByTestId('btn-nueva-persona').click()
      await expect(page.getByRole('dialog')).toBeVisible()

      await page.getByTestId('input-nombre').fill('SinDNI')
      await page.getByTestId('input-apellido').fill('Prueba')
      // DNI left empty intentionally
      await submitForm(page)

      // Then — form-level validation error (inline message or toast)
      const dialog = page.getByRole('dialog')
      const errorVisible = await Promise.race([
        dialog
          .getByText(/requerido|obligatorio|blank/i)
          .waitFor({ state: 'visible', timeout: 8000 })
          .then(() => true)
          .catch(() => false),
        page
          .locator('[data-sonner-toast]')
          .getByText(/dni|identificacion|requerido|obligatorio|blank/i)
          .waitFor({ state: 'visible', timeout: 8000 })
          .then(() => true)
          .catch(() => false),
      ])

      expect(errorVisible).toBe(true)

      // And — form stays open (dialog not closed on validation failure)
      await expect(dialog).toBeVisible()
    },
  )
})

// ---------------------------------------------------------------------------
// CU61 - Buscar persona o cliente
// ---------------------------------------------------------------------------

test.describe('CU61 - Buscar persona o cliente', () => {
  test.beforeEach(async ({ page }) => {
    // Seed a known persona via API so searches have guaranteed results
    await authenticateAsAdmin(page)
    await page.goto('/dashboard/personas')
    await page.waitForLoadState('networkidle')
  })

  test(
    'CU61-GW01: buscar por apellido → tabla muestra resultados coincidentes',
    async ({ page }) => {
      const steps = new GherkinSteps(page)

      // Given — a persona was created via API with a unique apellido
      const result = await createPersona(page, {
        apellido: `BuscaApellido${uniqueId() % 100_000}`,
        numeroIdentificacion: `CU61A${uniqueId() % 10_000_000}`,
      })
      expect(result.ok).toBe(true)
      const apellido = result.data ? (result.data as any).apellido : undefined

      // When — reload list and search by apellido
      await page.reload()
      await page.waitForLoadState('networkidle')

      if (apellido) {
        await page.getByTestId('input-search-apellido').fill(apellido)
      } else {
        // fallback: search with a partial known value
        await page.getByTestId('input-search-apellido').fill('BuscaApellido')
      }

      // Then — table is visible and has at least one row
      await expect(page.getByRole('table')).toBeVisible()
      await steps.thenTableRowCountIsAtLeast(1)
    },
  )

  test(
    'CU61-GW02: buscar por DNI → tabla muestra resultado exacto',
    async ({ page }) => {
      const steps = new GherkinSteps(page)

      // Given — a persona with a unique DNI
      const dni = `CU61D${uniqueId() % 10_000_000}`
      const result = await createPersona(page, {
        apellido: `BuscaDni${uniqueId() % 100_000}`,
        numeroIdentificacion: dni,
      })
      expect(result.ok).toBe(true)

      // When — search by DNI
      await page.reload()
      await page.waitForLoadState('networkidle')
      await page.getByTestId('input-search-dni').fill(dni)

      // Then — exactly one matching row
      await expect(page.getByRole('table')).toBeVisible()
      await expect(
        page.getByRole('table').getByText(new RegExp(dni, 'i')),
      ).toBeVisible()
    },
  )

  test(
    'CU61-GW03: buscar con valor inexistente → estado vacío visible',
    async ({ page }) => {
      const steps = new GherkinSteps(page)

      // When — search by DNI that will never match
      await page.getByTestId('input-search-dni').fill('XYZNOTEXISTS99999999')

      // Then — empty state message is shown
      await steps.thenElementIsVisible('no hay')
    },
  )

  test(
    'CU61-GW04: buscar persona por apellido sin resultados → estado vacío visible',
    async ({ page }) => {
      const steps = new GherkinSteps(page)

      // When — apellido search that will never match
      await page.getByTestId('input-search-apellido').fill('ZZZZNoExiste999XY')

      // Then — empty state message
      await steps.thenElementIsVisible('no hay')
    },
  )
})

// ---------------------------------------------------------------------------
// Viewport tests — Personas screen at 320 / 768 / 1024 px
// ---------------------------------------------------------------------------

const VIEWPORTS = [
  { label: 'mobile-320', width: 320, height: 640 },
  { label: 'tablet-768', width: 768, height: 1024 },
  { label: 'desktop-1024', width: 1024, height: 768 },
] as const

test.describe('Personas screen - responsive viewports', () => {
  for (const vp of VIEWPORTS) {
    test(
      `VP-${vp.label}: personas page renders without horizontal overflow`,
      async ({ page }) => {
        await page.setViewportSize({ width: vp.width, height: vp.height })
        await authenticateAsAdmin(page)
        await page.goto('/dashboard/personas')
        await page.waitForLoadState('domcontentloaded')

        // Personas heading visible
        await expect(
          page.getByRole('heading', { name: /personas/i }),
        ).toBeVisible()

        // No horizontal scroll at this breakpoint
        const noOverflow = await page.evaluate(
          () => document.documentElement.scrollWidth <= window.innerWidth,
        )
        expect(noOverflow).toBe(true)
      },
    )

    test(
      `VP-${vp.label}: btn-nueva-persona visible and opens modal`,
      async ({ page }) => {
        await page.setViewportSize({ width: vp.width, height: vp.height })
        await authenticateAsAdmin(page)
        await page.goto('/dashboard/personas')
        await page.waitForLoadState('domcontentloaded')

        await expect(page.getByTestId('btn-nueva-persona')).toBeVisible()
        await page.getByTestId('btn-nueva-persona').click()
        await expect(page.getByRole('dialog')).toBeVisible()
        // Modal has no horizontal overflow either
        const noOverflow = await page.evaluate(
          () => document.documentElement.scrollWidth <= window.innerWidth,
        )
        expect(noOverflow).toBe(true)
      },
    )

    test(
      `VP-${vp.label}: duplicate-DNI error toast visible at this breakpoint`,
      async ({ page }) => {
        const dni = `VP${vp.width}_${uniqueId() % 10_000_000}`

        await page.setViewportSize({ width: vp.width, height: vp.height })
        await authenticateAsAdmin(page)
        await page.goto('/dashboard/personas')
        await page.waitForLoadState('domcontentloaded')

        // Create original
        await fillPersonaForm(page, { nombre: 'Primero', apellido: 'VP', dni })
        await submitForm(page)
        await expect(
          page.locator('[data-sonner-toast]').getByText(/creada/i),
        ).toBeVisible({ timeout: 8000 })

        // Attempt duplicate
        await fillPersonaForm(page, { nombre: 'Segundo', apellido: 'VP', dni })
        await submitForm(page)

        // Then — duplicate error is visible even on narrow screens
        await expect(
          page.locator('[data-sonner-toast]').getByText(/ya existe/i),
        ).toBeVisible({ timeout: 5000 })

        await page.keyboard.press('Escape')
      },
    )
  }
})
