> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

#2005 after Wave 9 security headers (#562) left Referrer/Permissions unset.

## Goals / Non-Goals

**Goals:** ['Static Referrer-Policy + Permissions-Policy', 'Vitest proof']
**Non-Goals:** ['CSP changes']

## Decisions

['Referrer-Policy: strict-origin-when-cross-origin', 'Permissions-Policy: disable camera/microphone/geolocation/payment/usb']

## Riesgos / Trade-offs

['Over-restrictive Permissions-Policy is fine — product does not use those APIs']

## Testing Strategy

['Extend next-config-security-headers.test.ts']

## Regression Strategy

['Existing header Vitest cases']

## Playwright Strategy

n/a — config-level headers covered by Vitest; pages axe already green.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
