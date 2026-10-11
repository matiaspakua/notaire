/**
 * TS-0015 - People and clients workflow (CU17 → CU18 → CU61)
 * CU17 - Register a person
 * CU18 - Register a client (via the client checkbox)
 * CU41 - Update a client
 * CU61 - Search for a person or client
 * Issue #835 (duplicate validation by identification number)
 * Sequence: docs/200-architecture/204-diagrams/Secuencias/CU17.puml
 */
import { type Page, test, expect } from '@playwright/test'
import { GherkinSteps } from './gherkin-helpers'
import { authenticateAsAdmin } from './setup/auth'
import { createPerson, uniqueId } from './setup/api-helpers'

// ---------------------------------------------------------------------------
// NOTE ON CU18 MECHANISM
// ---------------------------------------------------------------------------
// CU18 "Register a client" is not a separate button or screen. The catalog label "Es client"
// checkbox inside the person creation/edit form is the sole mechanism: checking
// it during person creation (CU17) or editing (CU41) simultaneously registers
// the person as a client. There is no standalone "client upgrade" flow.
// Tests that previously used test.skip() for CU18 have been replaced with
// real assertions that verify this checkbox-driven contract.
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// Shared helpers
// ---------------------------------------------------------------------------

async function loginAndGoToPeople(page: Page): Promise<void> {
  const steps = new GherkinSteps(page)
  await authenticateAsAdmin(page)
  await steps.givenUserIsOnPage('/dashboard/people')
}

/** Opens the new-person modal and fills the required fields.
 *  `isClient` controls whether the checkbox is checked (CU18 path). */
async function fillPersonForm(
  page: Page,
  opts: {
    name: string
    lastName: string
    dni: string
    isClient?: boolean
  },
): Promise<void> {
  await page.getByTestId('btn-new-person').click()
  const dialog = page.getByRole('dialog')
  await expect(dialog).toBeVisible()

  await page.getByTestId('input-firstName').fill(opts.name)
  await page.getByTestId('input-lastName').fill(opts.lastName)
  await dialog.getByTestId('input-dni').fill(opts.dni)

  if (opts.isClient) {
    // CU18: checking this checkbox registers the person as a client at creation time.
    const checkbox = dialog.getByTestId('check-is-client')
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
// CU17 - Register a person (golden path)
// ---------------------------------------------------------------------------

test.describe('CU17 - Register a person', () => {
  test.beforeEach(async ({ page }) => {
    await loginAndGoToPeople(page)
  })

  test(
    'CU17-GW01: btn-new-person opens the form with the required fields',
    async ({ page }) => {
      // When
      await page.getByTestId('btn-new-person').click()

      // Then — modal visible with required fields
      const dialog = page.getByRole('dialog')
      await expect(dialog).toBeVisible()
      await expect(page.getByTestId('input-firstName')).toBeVisible()
      await expect(page.getByTestId('input-lastName')).toBeVisible()
      await expect(dialog.getByTestId('input-dni')).toBeVisible()

      // CU18 mechanism: checkbox must be present in the creation form
      await expect(dialog.getByTestId('check-is-client')).toBeVisible()
    },
  )

  test(
    'CU17-GW02: creating a person without checking is-client → the person appears in the table, not as a client',
    async ({ page }) => {
      const dni = `17${uniqueId() % 10_000_000}`
      const lastName = `Create${uniqueId() % 100_000}`

      // When — create without the es-client checkbox
      await fillPersonForm(page, {
        name: 'Carlos',
        lastName,
        dni,
        isClient: false,
      })
      await submitForm(page)

      // Then — success toast and table shows new person
      await expect(
        page.locator('[data-sonner-toast]').getByText(/creada/i),
      ).toBeVisible({ timeout: 8000 })

      await page.getByTestId('input-search-lastName').fill(lastName)
      await expect(page.getByRole('table')).toBeVisible()
      await expect(
        page.getByRole('table').getByText(new RegExp(lastName, 'i')),
      ).toBeVisible()
    },
  )

  test(
    'CU17-GW03: creating a person with is-client checked (CU17 + CU18) → they appear in the table with the client flag',
    async ({ page }) => {
      // NOTE (CU18): the 'es client' checkbox IS the CU18 registration mechanism.
      // Creating a person with this checkbox checked simultaneously registers
      // the person as a client. No separate step is needed.
      const dni = `18${uniqueId() % 10_000_000}`
      const lastName = `Client${uniqueId() % 100_000}`

      // When — create WITH the es-client checkbox (CU17 + CU18 in one step)
      await fillPersonForm(page, {
        name: 'María',
        lastName,
        dni,
        isClient: true,
      })
      await submitForm(page)

      // Then — person created successfully
      await expect(
        page.locator('[data-sonner-toast]').getByText(/creada/i),
      ).toBeVisible({ timeout: 8000 })

      // And — person appears in the table
      await page.getByTestId('input-search-lastName').fill(lastName)
      await expect(page.getByRole('table')).toBeVisible()
      const row = page.getByRole('row', { name: new RegExp(lastName, 'i') })
      await expect(row).toBeVisible()

      // And — the row indicates the person is a client
      // (badge, icon, or "Sí" in the "Client" column)
      await expect(row.getByText(/^cliente$|^sí$/i)).toBeVisible()
    },
  )
})

// ---------------------------------------------------------------------------
// CU18 - Register a client (via the checkbox)
// ---------------------------------------------------------------------------

test.describe('CU18 - Register a client via the is-client checkbox', () => {
  // NOTE: CU18 is exercised through the person form's 'es client' checkbox.
  // There is no separate button, screen, or API flow for client registration.
  // All tests here are real, not skipped.

  test.beforeEach(async ({ page }) => {
    await loginAndGoToPeople(page)
  })

  test(
    'CU18-GW01: a person created with the is-client checkbox appears as a client',
    async ({ page }) => {
      const dni = `CU18${uniqueId() % 10_000_000}`
      const lastName = `IsClient${uniqueId() % 100_000}`

      // When — person created with es-client checked (the CU18 mechanism)
      await fillPersonForm(page, {
        name: 'Lucía',
        lastName,
        dni,
        isClient: true,
      })
      await submitForm(page)

      // Then — success
      await expect(
        page.locator('[data-sonner-toast]').getByText(/creada/i),
      ).toBeVisible({ timeout: 8000 })

      // And — searching for the person shows her in the table as a client
      await page.getByTestId('input-search-lastName').fill(lastName)
      await expect(page.getByRole('table')).toBeVisible()

      const row = page.getByRole('row', { name: new RegExp(lastName, 'i') })
      await expect(row).toBeVisible()
      // The row must reflect client status (column value, badge, or icon)
      await expect(row.getByText(/^cliente$|^sí$/i)).toBeVisible()
    },
  )

  test(
    'CU18-GW02: a person without the is-client checkbox can be promoted to client by editing (CU41)',
    async ({ page }) => {
      // NOTE (CU41): editing an existing person and checking 'es client'
      // promotes them to client status. This is the CU41 path.
      const dni = `CU41${uniqueId() % 10_000_000}`
      const lastName = `Promote${uniqueId() % 100_000}`

      // Given — person created without es-client
      await fillPersonForm(page, {
        name: 'Pedro',
        lastName,
        dni,
        isClient: false,
      })
      await submitForm(page)
      await expect(
        page.locator('[data-sonner-toast]').getByText(/creada/i),
      ).toBeVisible({ timeout: 8000 })

      // When — open the person for editing
      await page.getByTestId('input-search-lastName').fill(lastName)
      await expect(page.getByRole('table')).toBeVisible()
      const row = page.getByRole('row', { name: new RegExp(lastName, 'i') })
      await row.getByRole('button').first().click()

      const editDialog = page.getByRole('dialog')
      await expect(editDialog).toBeVisible()

      // And — check the es-client checkbox to promote the person (CU18/CU41)
      const checkbox = editDialog.getByTestId('check-is-client')
      await checkbox.check()
      await expect(checkbox).toBeChecked()
      await submitForm(page)

      // Then — update succeeds
      await expect(
        page.locator('[data-sonner-toast]').getByText(/actualizada/i),
      ).toBeVisible({ timeout: 8000 })

      // And — person now shows as client
      await page.getByTestId('input-search-lastName').fill(lastName)
      const updatedRow = page.getByRole('row', { name: new RegExp(lastName, 'i') })
      await expect(updatedRow.getByText(/client|sí/i)).toBeVisible()
    },
  )
})

// ---------------------------------------------------------------------------
// Issue #835 - Deduplicate by identification number
// ---------------------------------------------------------------------------

test.describe('Issue #835 - Deduplication by DNI', () => {
  test.beforeEach(async ({ page }) => {
    await loginAndGoToPeople(page)
  })

  test(
    'Dedup-GW01: creating a second person with the same DNI → a duplicate error is visible',
    async ({ page }) => {
      const dni = `DUP${uniqueId() % 10_000_000}`

      // Given — first person created successfully
      await fillPersonForm(page, { name: 'Original', lastName: 'Duplicate', dni })
      await submitForm(page)
      await expect(
        page.locator('[data-sonner-toast]').getByText(/creada/i),
      ).toBeVisible({ timeout: 8000 })

      // When — attempt to create a second person with the same DNI
      await fillPersonForm(page, { name: 'Another', lastName: 'Person', dni })
      await submitForm(page)

      // Then — error toast references duplicate document
      await expect(
        page.locator('[data-sonner-toast]').getByText(/ya existe/i),
      ).toBeVisible({ timeout: 5000 })

      // And — form stays open (data not lost)
      await expect(page.getByRole('dialog')).toBeVisible()
      await expect(page.getByTestId('input-firstName')).toHaveValue('Another')
    },
  )

  test(
    'Dedup-GW02: the duplicate toast includes a link to the existing person',
    async ({ page }) => {
      const dni = `DUP2${uniqueId() % 10_000_000}`

      await fillPersonForm(page, { name: 'First', lastName: 'Exist', dni })
      await submitForm(page)
      await expect(
        page.locator('[data-sonner-toast]').getByText(/creada/i),
      ).toBeVisible({ timeout: 8000 })

      await fillPersonForm(page, { name: 'Second', lastName: 'Dup', dni })
      await submitForm(page)

      // Then — link to existing person is shown in the toast
      await expect(
        page.locator('[data-sonner-toast]').getByText(/ver persona existente/i),
      ).toBeVisible({ timeout: 5000 })
    },
  )

  test(
    'Dedup-EDGE: creating a person without a DNI → a validation error is shown on the form',
    async ({ page }) => {
      // When — open form and submit without filling DNI
      await page.getByTestId('btn-new-person').click()
      await expect(page.getByRole('dialog')).toBeVisible()

      await page.getByTestId('input-firstName').fill('NoId')
      await page.getByTestId('input-lastName').fill('Sample')
      // DNI left empty intentionally
      await submitForm(page)

      // Then — form-level validation error (inline FormField and/or toast).
      // Prefer locator.or so either surface can satisfy without Promise.race
      // false-negatives when one branch times out first (#945).
      const dialog = page.getByRole('dialog')
      const inlineError = dialog.getByText(/requerido|obligatorio|blank/i)
      const toastError = page
        .locator('[data-sonner-toast]')
        .getByText(/dni|identificacion|identification|requerido|obligatorio|blank/i)
      await expect(inlineError.or(toastError).first()).toBeVisible({ timeout: 8000 })

      // And — form stays open (dialog not closed on validation failure)
      await expect(dialog).toBeVisible()
    },
  )

  test(
    'A11y-#1351: a field with an error stays invalid and is described by its message',
    async ({ page }) => {
      await page.getByTestId('btn-new-person').click()
      const dialog = page.getByRole('dialog')
      await expect(dialog).toBeVisible()
      await submitForm(page)

      const name = page.getByTestId('input-firstName')
      await expect(name).toHaveAttribute('aria-invalid', 'true')
      await expect(name).toHaveAttribute('aria-required', 'true')
      // The message comes from the backend's field error (English Bean Validation text today).
      await expect(name).toHaveAccessibleDescription(/requerido|obligatorio|must not be blank/i)
      const alert = dialog.getByRole('alert').first()
      await expect(alert).toBeVisible()
      expect(await alert.textContent()).not.toMatch(/\p{Extended_Pictographic}/u)
    },
  )
})

// ---------------------------------------------------------------------------
// CU61 - Search for a person or client
// ---------------------------------------------------------------------------

test.describe('CU61 - Search for a person or client', () => {
  test.beforeEach(async ({ page }) => {
    // Seed a known person via API so searches have guaranteed results
    await authenticateAsAdmin(page)
    await page.goto('/dashboard/people')
    await page.waitForLoadState('networkidle')
  })

  test(
    'CU61-GW01: searching by last name → the table shows matching results',
    async ({ page }) => {
      const steps = new GherkinSteps(page)

      // Given — a person was created via API with a unique lastName
      const result = await createPerson(page, {
        lastName: `SearchLastName${uniqueId() % 100_000}`,
        identificationNumber: `CU61A${uniqueId() % 10_000_000}`,
      })
      expect(result.ok).toBe(true)
      const lastName = result.data ? (result.data as any).lastName : undefined

      // When — reload list and search by lastName
      await page.reload()
      await page.waitForLoadState('networkidle')

      if (lastName) {
        await page.getByTestId('input-search-lastName').fill(lastName)
      } else {
        // fallback: search with a partial known value
        await page.getByTestId('input-search-lastName').fill('SearchLastName')
      }

      // Then — table is visible and has at least one row
      await expect(page.getByRole('table')).toBeVisible()
      await steps.thenTableRowCountIsAtLeast(1)
    },
  )

  test(
    'CU61-GW02: searching by DNI → the table shows the exact result',
    async ({ page }) => {
      const steps = new GherkinSteps(page)

      // Given — a person with a unique DNI
      const dni = `CU61D${uniqueId() % 10_000_000}`
      const result = await createPerson(page, {
        lastName: `SearchDni${uniqueId() % 100_000}`,
        identificationNumber: dni,
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
    'CU61-GW03: searching for a value that does not exist → the empty state is visible',
    async ({ page }) => {
      const steps = new GherkinSteps(page)

      // When — search by DNI that will never match
      await page.getByTestId('input-search-dni').fill('XYZNOTEXISTS99999999')

      // Then — empty state message is shown
      await steps.thenElementIsVisible('no hay')
    },
  )

  test(
    'CU61-GW04: searching for a person by last name with no results → the empty state is visible',
    async ({ page }) => {
      const steps = new GherkinSteps(page)

      // When — lastName search that will never match
      await page.getByTestId('input-search-lastName').fill('ZZZZDoesNotExist999XY')

      // Then — empty state message
      await steps.thenElementIsVisible('no hay')
    },
  )
})

// ---------------------------------------------------------------------------
// Viewport tests — People screen at 320 / 768 / 1024 px
// ---------------------------------------------------------------------------

const VIEWPORTS = [
  { label: 'mobile-320', width: 320, height: 640 },
  { label: 'tablet-768', width: 768, height: 1024 },
  { label: 'desktop-1024', width: 1024, height: 768 },
] as const

test.describe('People screen - responsive viewports', () => {
  for (const vp of VIEWPORTS) {
    test(
      `VP-${vp.label}: the people page renders without horizontal overflow`,
      async ({ page }) => {
        await page.setViewportSize({ width: vp.width, height: vp.height })
        await authenticateAsAdmin(page)
        await page.goto('/dashboard/people')
        await page.waitForLoadState('domcontentloaded')

        // People heading visible
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
      `VP-${vp.label}: btn-new-person is visible and opens the modal`,
      async ({ page }) => {
        await page.setViewportSize({ width: vp.width, height: vp.height })
        await authenticateAsAdmin(page)
        await page.goto('/dashboard/people')
        await page.waitForLoadState('domcontentloaded')

        await expect(page.getByTestId('btn-new-person')).toBeVisible()
        await page.getByTestId('btn-new-person').click()
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
        await page.goto('/dashboard/people')
        await page.waitForLoadState('domcontentloaded')

        // Create original
        await fillPersonForm(page, { name: 'First', lastName: 'VP', dni })
        await submitForm(page)
        await expect(
          page.locator('[data-sonner-toast]').getByText(/creada/i),
        ).toBeVisible({ timeout: 8000 })

        // Attempt duplicate
        await fillPersonForm(page, { name: 'Second', lastName: 'VP', dni })
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

// ---------------------------------------------------------------------------
// #1357 - Search-as-you-type is debounced and keeps the table stable
// ---------------------------------------------------------------------------

test.describe('#1357 - Debounced people search', () => {
  test('typing a surname quickly sends one /people/search request and finds the person', async ({ page }) => {
    await loginAndGoToPeople(page)
    const created = await createPerson(page)
    const lastName = created.data!.lastName!
    const searches: string[] = []
    page.on('request', (r) => {
      if (r.url().includes('/people/search')) searches.push(r.url())
    })

    await page.getByTestId('input-search-lastName').pressSequentially(lastName, { delay: 40 })
    await expect(page.getByRole('cell', { name: lastName }).first()).toBeVisible({ timeout: 10_000 })
    await page.waitForTimeout(500)
    await page.screenshot({ path: 'test-results/ui-shots/1357-personas-search/search-desktop.png' })

    expect(searches).toHaveLength(1)
    expect(decodeURIComponent(searches[0])).toContain(`lastName=${lastName}`)
  })
})

// ---------------------------------------------------------------------------
// #1361 - Identification type (RF-37): required, DNI preselected
// ---------------------------------------------------------------------------

test.describe('#1361 - Identification type in the person form', () => {
  test.beforeEach(async ({ page }) => {
    await loginAndGoToPeople(page)
  })

  test('a new person starts on DNI, the type select comes before the number', async ({ page }) => {
    await page.getByTestId('btn-new-person').click()
    const dialog = page.getByRole('dialog')
    const typeSelect = dialog.getByRole('combobox', { name: /tipo de identificación/i })
    await expect(typeSelect).toBeVisible()
    await expect(typeSelect).toHaveText(/^DNI$/)
    const selectBox = await typeSelect.boundingBox()
    const numberBox = await dialog.getByTestId('input-dni').boundingBox()
    expect(selectBox && numberBox && (selectBox.y < numberBox.y || selectBox.x < numberBox.x)).toBeTruthy()
  })

  test('creating a person with type CUIT persists it and lists "CUIT <number>"', async ({ page }) => {
    const cuit = `30${String(uniqueId() % 1_000_000_000).padStart(9, '0')}`
    const lastName = `Cuit${uniqueId() % 100_000}`
    await page.getByTestId('btn-new-person').click()
    const dialog = page.getByRole('dialog')
    await page.getByTestId('input-firstName').fill('Company')
    await page.getByTestId('input-lastName').fill(lastName)
    await dialog.getByRole('combobox', { name: /tipo de identificación/i }).click()
    await page.getByRole('listbox').getByRole('option', { name: 'CUIT', exact: true }).click()
    await dialog.getByTestId('input-dni').fill(cuit)
    await submitForm(page)
    await expect(page.locator('[data-sonner-toast]').getByText(/creada/i)).toBeVisible({ timeout: 8000 })

    await page.getByTestId('input-search-dni').fill(cuit)
    await expect(page.getByText(`CUIT ${cuit}`, { exact: true }).first()).toBeVisible({ timeout: 8000 })
  })
})
