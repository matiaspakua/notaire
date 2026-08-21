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
| Specification | `openspec/changes/escritura-post-firma-legal-cycle/` | in progress |
| Branch | `feat/832_escritura-post-firma-legal-cycle` | not created |
| Tasks | `tasks.md` | 0/N complete |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Firma exitosa de una escritura lista | `EscrituraFirmaServiceTest#shouldSignEscrituraWhenUnsignedWithFolio` | pending |
| Rechazo por escritura ya firmada | `EscrituraFirmaServiceTest#shouldRejectSignWhenAlreadySigned` | pending |
| Rechazo por falta de folio asignado | `EscrituraFirmaServiceTest#shouldRejectSignWhenNoFolioAssigned` | pending |
| Generación exitosa desde escritura firmada | `TestimonioGeneracionServiceTest#shouldGenerateTestimonioFromSignedEscritura` | pending |
| Rechazo de generación desde escritura no firmada | `TestimonioGeneracionServiceTest#shouldRejectGenerationWhenEscrituraNotSigned` | pending |
| Verificación sin observaciones | `TestimonioVerificacionServiceTest#shouldVerifyWithoutObservations` | pending |
| Verificación con observaciones | `TestimonioVerificacionServiceTest#shouldVerifyWithObservations` | pending |
| Rechazo de verificación de testimonio inexistente | `TestimonioVerificacionServiceTest#shouldRejectVerificationWhenTestimonioNotFound` | pending |
| Emisión de copia PDF de testimonio verificado | `TestimonioCopiaReportIntegrationTest#shouldGenerateCopiaPdfForVerifiedTestimonio` | pending |
| Rechazo de emisión de copia de testimonio no verificado | `TestimonioCopiaReportIntegrationTest#shouldRejectCopiaWhenNotVerified` | pending |
| Ingreso exitoso registra fecha de ingreso | `MovimientoTestimonioServiceTest#shouldRegisterIngresoInscripcion` | pending |
| Rechazo de ingreso de testimonio ya ingresado sin retirar | `MovimientoTestimonioServiceTest#shouldRejectIngresoWhenAlreadyOpen` | pending |
| Registro exitoso marca inscripto con fecha | `MovimientoTestimonioServiceTest#shouldRegisterInscripcion` | pending |
| Rechazo de registrar inscripción sin ingreso previo | `MovimientoTestimonioServiceTest#shouldRejectInscripcionWithoutIngreso` | pending |
| Retiro exitoso registra fecha de salida y número de cartón | `MovimientoTestimonioServiceTest#shouldRegisterRetiro` | pending |
| Rechazo de retiro de testimonio no inscripto | `MovimientoTestimonioServiceTest#shouldRejectRetiroWhenNotInscripto` | pending |
| Reingreso exitoso crea nuevo movimiento preservando historial | `MovimientoTestimonioServiceTest#shouldCreateNewMovementOnReingreso` | pending |
| Rechazo de reingreso de testimonio no retirado previamente | `MovimientoTestimonioServiceTest#shouldRejectReingresoWhenNotWithdrawn` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU06 – Firmar escritura (Esta Junto a Preparar Escritura).md` | no | pending |
| `docs/100-business/102-use-cases/CU07 – Generar testimonio.md` | no | pending |
| `docs/100-business/102-use-cases/CU08 – Verificar Testimonio.md` | no | pending |
| `docs/100-business/102-use-cases/CU11 – Ingresar para inscripción.md` | no | pending |
| `docs/100-business/102-use-cases/CU12 – Retirar testimonio.md` | no | pending |
| `docs/100-business/102-use-cases/CU44 – Reingresar testimonio.md` | no | pending |
| `CHANGELOG.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | — |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
