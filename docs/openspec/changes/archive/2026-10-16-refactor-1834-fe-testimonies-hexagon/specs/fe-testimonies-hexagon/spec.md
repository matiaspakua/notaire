## ADDED Requirements

### Requirement: Testimony UI uses core ports only

Testimony and testimony-movement hooks and the testimonies dashboard SHALL call composition/use cases. HTTP gateways SHALL be the only paths that import `api-client`. Create/update payloads SHALL preserve deed refs and optimistic-lock `version`.

#### Scenario: Testimony hooks leave the eslint baseline
- **WHEN** architecture-boundaries runs after this slice
- **THEN** eslint-suppressions has no entries for `useTestimonies.ts` or `useTestimonyMovements.ts`

#### Scenario: Testimonies pages leave the eslint baseline
- **WHEN** architecture-boundaries runs after this slice
- **THEN** eslint-suppressions has no entries for testimonies `page.tsx` or `RegisterTestimonyDialog.tsx`

#### Scenario: REST behaviour unchanged
- **WHEN** clients generate, register, verify, update, delete testimonies or run movements
- **THEN** behaviour matches the pre-slice hooks