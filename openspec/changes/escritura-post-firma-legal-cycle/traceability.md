# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #832 | open |
| Use Case | CU06 (#159), CU07 (#160), CU08 (#161), CU11 (#164), CU12 (#165), CU44 (#197) | exists |
| Specification | `openspec/changes/escritura-post-firma-legal-cycle/` | complete |
| Branch | `feat/832_escritura-post-firma-legal-cycle` | created |
| Tasks | `tasks.md` | 76/76 complete |
| Commits | `376b868`, `080b1af`, `8e98d06`, `04b92e3`, `9e3c5f4`, and the final ledger commit closing #832 | done |
| Pull Request | [#852](https://github.com/matiaspakua/notaire/pull/852) | open |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Firma exitosa de una escritura lista | `EscrituraFirmaServiceTest#shouldSignEscrituraWhenUnsignedWithFolio` | passed |
| Rechazo por escritura ya firmada | `EscrituraFirmaServiceTest#shouldRejectSignWhenAlreadySigned` | passed |
| Rechazo por falta de folio asignado | `EscrituraFirmaServiceTest#shouldRejectSignWhenNoFolioAssigned` | passed |
| Generación exitosa desde escritura firmada | `TestimonioGeneracionServiceTest#shouldGenerateTestimonioFromSignedEscritura` | passed |
| Rechazo de generación desde escritura no firmada | `TestimonioGeneracionServiceTest#shouldRejectGenerationWhenEscrituraNotSigned` | passed |
| Verificación sin observaciones | `TestimonioVerificacionServiceTest#shouldVerifyWithoutObservations` | passed |
| Verificación con observaciones | `TestimonioVerificacionServiceTest#shouldVerifyWithObservations` | passed |
| Rechazo de verificación de testimonio inexistente | `TestimonioVerificacionServiceTest#shouldRejectVerificationWhenTestimonioNotFound` | passed |
| Emisión de copia PDF de testimonio verificado | `TestimonioCopiaReportIntegrationTest#shouldGenerateCopiaPdfForVerifiedTestimonio` | passed |
| Rechazo de emisión de copia de testimonio no verificado | `TestimonioCopiaReportIntegrationTest#shouldRejectCopiaWhenNotVerified` | passed |
| Ingreso exitoso registra fecha de ingreso | `MovimientoTestimonioServiceTest#shouldRegisterIngresoInscripcion` | passed |
| Rechazo de ingreso de testimonio ya ingresado sin retirar | `MovimientoTestimonioServiceTest#shouldRejectIngresoWhenAlreadyOpen` | passed |
| Rechazo de ingreso de testimonio no verificado | `MovimientoTestimonioServiceTest#shouldRejectIngresoWhenTestimonioNotVerified` | passed |
| Registro exitoso marca inscripto con fecha | `MovimientoTestimonioServiceTest#shouldRegisterInscripcion` | passed |
| Rechazo de registrar inscripción sin ingreso previo | `MovimientoTestimonioServiceTest#shouldRejectInscripcionWithoutIngreso` | passed |
| Retiro exitoso registra fecha de salida y número de cartón | `MovimientoTestimonioServiceTest#shouldRegisterRetiro` | passed |
| Rechazo de retiro de testimonio no inscripto | `MovimientoTestimonioServiceTest#shouldRejectRetiroWhenNotInscripto` | passed |
| Reingreso exitoso crea nuevo movimiento preservando historial | `MovimientoTestimonioServiceTest#shouldCreateNewMovementOnReingreso` | passed |
| Rechazo de reingreso de testimonio no retirado previamente | `MovimientoTestimonioServiceTest#shouldRejectReingresoWhenNotWithdrawn` | passed |
| 404 (no 400) para `idTestimonio` inexistente en registrar-inscripción/retirar/reingresar | `MovimientoTestimonioControllerIntegrationTest#shouldReturn404When{RegistrarInscripcion,Retirar,Reingresar}ForNonExistingTestimonio` | passed |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU06 – Firmar escritura (Esta Junto a Preparar Escritura).md` | yes | `9e3c5f4` |
| `docs/100-business/102-use-cases/CU07 – Generar testimonio.md` | no (already accurate, no changes needed) | — |
| `docs/100-business/102-use-cases/CU08 – Verificar Testimonio.md` | yes | `9e3c5f4` |
| `docs/100-business/102-use-cases/CU11 – Ingresar para inscripción.md` | yes | `9e3c5f4` |
| `docs/100-business/102-use-cases/CU12 – Retirar testimonio.md` | yes | `9e3c5f4` |
| `docs/100-business/102-use-cases/CU44 – Reingresar testimonio.md` | yes | `9e3c5f4` |
| `CHANGELOG.md` | yes | final ledger commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #832, `proposal.md`, `specs/*/spec.md` |
| 2 | Failing tests written, test cases designed | yes | Tests confirmed failing pre-implementation (compile failure) per `tasks.md` 3.4; the `MovimientoTestimonioService` 404 tests confirmed failing (`Tests run: 3, Failures: 3`) before the fix |
| 3 | Suite green, coverage held, docs updated | yes | `mvn test -pl backend-api` — 1561 tests, 0 failures; `mvn verify -pl backend-api` — coverage floor met; `npx playwright test` — 16 passed, 1 documented skip; CU06/CU08/CU11/CU12/CU44 and `CHANGELOG.md` updated |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
