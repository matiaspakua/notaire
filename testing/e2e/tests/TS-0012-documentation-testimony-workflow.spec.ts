/**
 * TS-0012 - Documentation and testimony workflow (CU03 → CU07 → CU08 → CU11 → CU12)
 * CU03 - List required documents
 * CU07 - Generate a testimony
 * CU08 - Verify a testimony
 * CU11 - Enter for registration
 * CU12 - Withdraw a testimony
 * Issue #832
 * Sequence: docs/200-architecture/204-diagrams/Secuencias/CU07.puml
 */
import { test, expect } from '@playwright/test'
import { GherkinSteps } from './gherkin-helpers'
import { authenticateAsAdmin } from './setup/auth'
import { apiPost, apiGet, uniqueId } from './setup/api-helpers'
import { rowWithCell } from './setup/rows'

// ──────────────────────────────────────────────
// Seed helpers
// ──────────────────────────────────────────────

interface DeedCreated {
  idDeed: number
  number: number
}

interface TestimonyCreated {
  idTestimony: number
  number: number
}

/** Seed a "Signed" deed so CU07 has something to reference. */
async function seedSignedDeed(
  page: import('@playwright/test').Page,
): Promise<DeedCreated> {
  const number = uniqueId() % 1_000_000
  const result = await apiPost<{ idDeed: number }>(page, '/deeds', {
    number: number,
    dateDeedrecording: new Date().toISOString().split('T')[0],
    body: `E2E content TS-0012 ${number}`,
    status: 'Firmada',
  })
  if (!result.ok || !result.data?.idDeed) {
    throw new Error(`seedSignedDeed failed: status ${result.status} — ${result.error}`)
  }
  return { idDeed: result.data.idDeed, number }
}

/**
 * Seed a testimony that is already "verificado" (true) so CU11/CU12 can
 * start directly from the movements page without repeating CU07/CU08.
 */
async function seedVerifiedTestimony(
  page: import('@playwright/test').Page,
): Promise<TestimonyCreated> {
  const { idDeed, number: deedNumber } = await seedSignedDeed(page)
  const number = uniqueId() % 1_000_000
  const result = await apiPost<{ idTestimony: number }>(page, '/testimonies', {
    deed: { idDeed: idDeed, number: deedNumber },
    number: number,
    flagged: false,
    verified: true,
  })
  if (!result.ok || !result.data?.idTestimony) {
    throw new Error(`seedVerifiedTestimony failed: status ${result.status} — ${result.error}`)
  }
  return { idTestimony: result.data.idTestimony, number }
}

// ──────────────────────────────────────────────
// CU03 — List required documents
// ──────────────────────────────────────────────

test.describe('CU03 - List required documents and certificates', () => {
  let steps: GherkinSteps

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page)
    await steps.givenUserIsLoggedIn()
    await steps.givenUserIsOnPage('/dashboard/required-documents')
  })

  test('CU03-GW01: Given on required documents, When a procedure is selected, Then the results section is shown', async ({
    page,
  }) => {
    await steps.thenPageHasHeading('Documentos Necesarios')

    await page.getByTestId('select-procedure').click()
    await page.getByRole('listbox').getByRole('option').first().click()

    await expect(page.getByText(/Documentos necesarios para/i)).toBeVisible()
    const emptyState = page.getByTestId('empty-state')
    const table = page.getByRole('table')
    await expect(emptyState.or(table)).toBeVisible()
  })
})

// ──────────────────────────────────────────────
// CU07 + CU08 — Golden path: full legal cycle
// ──────────────────────────────────────────────

test.describe('CU07→CU08→CU11→CU12 — Full legal cycle (golden path)', () => {
  let steps: GherkinSteps

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page)
    await steps.givenUserIsLoggedIn()
  })

  test('Golden path: generate → verify → enter → inscribe → withdraw', async ({ page }) => {
    // ── Seed ──────────────────────────────────────────
    const { idDeed, number: deedNumber } = await seedSignedDeed(page)

    // ── CU07: Generate a testimony ──────────────────────
    await steps.givenUserIsOnPage('/dashboard/testimonies')
    await page.getByTestId('btn-generate-testimony').click()
    await steps.thenModalIsVisible()

    await page.getByTestId('select-deed-testimony').click()
    await page.getByRole('option', { name: new RegExp(String(deedNumber)) }).click()
    await page.getByTestId('btn-save-testimony').click()
    await steps.thenShowsSuccessMessage('generado')
    await steps.thenModalIsNotVisible()

    const rowTestimonies = rowWithCell(page, deedNumber)
    await expect(rowTestimonies).toBeVisible({ timeout: 10_000 })

    // Retrieve the generated testimony id from the API
    const list = await apiGet<Array<{ idTestimony: number; deed?: { number?: number } }>>(
      page,
      '/testimonies',
    )
    const idTestimony = list.data!.find(
      (t) => t.deed?.number === deedNumber,
    )!.idTestimony

    // ── CU08: Verify without notes ─────────────
    await page.getByTestId(`btn-verify-testimony-${idTestimony}`).click()
    await steps.thenModalIsVisible()
    await page.getByTestId('btn-confirm-verify-testimony').click()
    await steps.thenShowsSuccessMessage('verificado')
    await expect(rowTestimonies).toContainText(/verificado/i)

    // ── CU11: Enter for registration ───────────────
    await steps.givenUserIsOnPage('/dashboard/testimony-movements')
    const rowMovements = rowWithCell(page, deedNumber)
    await expect(rowMovements).toBeVisible({ timeout: 10_000 })

    await page.getByTestId(`btn-sign-in-testimony-${idTestimony}`).click()
    await steps.thenShowsSuccessMessage('ingresado')
    await expect(rowMovements).toContainText(/ingresado/i)

    // ── CU11: Enter for registration ───────────────────
    await page.getByTestId(`btn-record-registration-${idTestimony}`).click()
    await steps.thenShowsSuccessMessage('inscripción registrada')
    await expect(rowMovements).toContainText(/inscripto/i)

    // ── CU12: Withdraw the testimony ──────────────────────
    await page.getByTestId(`btn-withdraw-testimony-${idTestimony}`).click()
    await steps.thenModalIsVisible()
    await page.getByTestId('input-card-number').fill('123')
    await page.getByTestId('btn-confirm-retiro').click()
    await steps.thenShowsSuccessMessage('retirado')
    await expect(rowMovements).toContainText(/retirado/i)
  })
})

// ──────────────────────────────────────────────
// CU07 — Generate a testimony (unit / edge)
// ──────────────────────────────────────────────

test.describe('CU07 - Generate a testimony', () => {
  let steps: GherkinSteps

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page)
    await steps.givenUserIsLoggedIn()
  })

  test('CU07-GW01: Given a signed deed, When a testimony is generated, Then the generated status appears in the table', async ({
    page,
  }) => {
    const { number } = await seedSignedDeed(page)

    await steps.givenUserIsOnPage('/dashboard/testimonies')
    await page.getByTestId('btn-generate-testimony').click()
    await steps.thenModalIsVisible()

    await page.getByTestId('select-deed-testimony').click()
    await page.getByRole('option', { name: new RegExp(String(number)) }).click()
    await page.getByTestId('btn-save-testimony').click()
    await steps.thenShowsSuccessMessage('generado')
    await steps.thenModalIsNotVisible()

    const row = rowWithCell(page, number)
    await expect(row).toBeVisible({ timeout: 10_000 })
  })
})

// ──────────────────────────────────────────────
// CU08 — Verify a testimony (edge paths)
// ──────────────────────────────────────────────

test.describe('CU08 - Verify a testimony', () => {
  let steps: GherkinSteps

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page)
    await steps.givenUserIsLoggedIn()
  })

  test('CU08-GW01: Edge: verify with notes → status Observado', async ({ page }) => {
    // Seed deed and generate testimony via API so this test is independent
    const { idDeed, number } = await seedSignedDeed(page)
    const generated = await apiPost<{ idTestimony: number }>(
      page,
      `/testimonies/${idDeed}/generate`,
      {},
    )
    const idTestimony = generated.data!.idTestimony

    await steps.givenUserIsOnPage('/dashboard/testimonies')
    const row = rowWithCell(page, number)
    await expect(row).toBeVisible({ timeout: 10_000 })

    await page.getByTestId(`btn-verify-testimony-${idTestimony}`).click()
    await steps.thenModalIsVisible()
    await page.getByTestId('checkbox-observed-testimony').click()
    await page.getByTestId('input-notes-testimony').fill('Falta firma del otorgante')
    await page.getByTestId('btn-confirm-verify-testimony').click()
    await steps.thenShowsSuccessMessage('verificado')

    await expect(row).toContainText(/observado/i)
  })

  test('CU08-GW02: Edge: issuing a copy stays blocked until the testimony is verified', async ({
    page,
  }) => {
    const { idDeed, number } = await seedSignedDeed(page)
    const generated = await apiPost<{ idTestimony: number }>(
      page,
      `/testimonies/${idDeed}/generate`,
      {},
    )
    const idTestimony = generated.data!.idTestimony

    await steps.givenUserIsOnPage('/dashboard/testimonies')
    const row = rowWithCell(page, number)
    await expect(row).toBeVisible({ timeout: 10_000 })

    // The issue-copy button must NOT exist while the testimony is still unverified
    await expect(page.getByTestId(`btn-issue-copy-${idTestimony}`)).toHaveCount(0)
    // The verify button must still be available
    await expect(page.getByTestId(`btn-verify-testimony-${idTestimony}`)).toBeVisible()
  })
})

// ──────────────────────────────────────────────
// CU11 — Testimony movements (edge paths)
// ──────────────────────────────────────────────

test.describe('CU11 - Submit for registration', () => {
  let steps: GherkinSteps

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page)
    await steps.givenUserIsLoggedIn()
  })

  test('CU11-GW01: submitting and recording registration changes the status to inscripto', async ({ page }) => {
    const { idTestimony, number } = await seedVerifiedTestimony(page)

    await steps.givenUserIsOnPage('/dashboard/testimony-movements')
    const row = rowWithCell(page, number)
    await expect(row).toBeVisible({ timeout: 10_000 })

    await page.getByTestId(`btn-sign-in-testimony-${idTestimony}`).click()
    await steps.thenShowsSuccessMessage('ingresado')
    await expect(row).toContainText(/ingresado/i)

    await page.getByTestId(`btn-record-registration-${idTestimony}`).click()
    await steps.thenShowsSuccessMessage('inscripción registrada')
    await expect(row).toContainText(/inscripto/i)
  })
})

// ──────────────────────────────────────────────
// CU12 — Withdraw a testimony (edge paths)
// ──────────────────────────────────────────────

test.describe('CU12 - Withdraw a testimony', () => {
  let steps: GherkinSteps

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page)
    await steps.givenUserIsLoggedIn()
  })

  test('CU12-GW01: withdrawing after registration → status withdrawn', async ({ page }) => {
    const { idTestimony, number } = await seedVerifiedTestimony(page)

    await steps.givenUserIsOnPage('/dashboard/testimony-movements')
    const row = rowWithCell(page, number)
    await expect(row).toBeVisible({ timeout: 10_000 })

    // Move to 'inscripto' first
    await page.getByTestId(`btn-sign-in-testimony-${idTestimony}`).click()
    await steps.thenShowsSuccessMessage('ingresado')
    await page.getByTestId(`btn-record-registration-${idTestimony}`).click()
    await steps.thenShowsSuccessMessage('inscripción registrada')

    // Now retire
    await page.getByTestId(`btn-withdraw-testimony-${idTestimony}`).click()
    await steps.thenModalIsVisible()
    await page.getByTestId('input-card-number').fill('456')
    await page.getByTestId('btn-confirm-retiro').click()
    await steps.thenShowsSuccessMessage('retirado')
    await expect(row).toContainText(/retirado/i)
  })
})

// ──────────────────────────────────────────────
// Responsive — testimonies screens
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

    test(`testimonies screen has no horizontal overflow at ${viewport.label}`, async ({ page }) => {
      await steps.givenUserIsOnPage('/dashboard/testimonies')
      await page.waitForLoadState('networkidle')
      await steps.thenHasNoHorizontalOverflow()
    })

    test(`testimony-movements screen has no horizontal overflow at ${viewport.label}`, async ({
      page,
    }) => {
      await steps.givenUserIsOnPage('/dashboard/testimony-movements')
      await page.waitForLoadState('networkidle')
      await steps.thenHasNoHorizontalOverflow()
    })
  })
}
