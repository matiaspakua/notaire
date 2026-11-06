# frontend-security-headers Specification

## Purpose
Next.js static headers include Referrer-Policy and Permissions-Policy.
## Requirements
### Requirement: Referrer-Policy and Permissions-Policy on all routes

The Next.js static security headers for `/:path*` SHALL include `Referrer-Policy` set to `strict-origin-when-cross-origin` and a `Permissions-Policy` that disables unused powerful browser features (at least camera, microphone, geolocation, payment, and usb).

#### Scenario: Referrer-Policy present

- **GIVEN** `frontend/next.config.ts` headers configuration
- **WHEN** the Vitest security-headers suite runs
- **THEN** `Referrer-Policy` is `strict-origin-when-cross-origin`

#### Scenario: Permissions-Policy present

- **GIVEN** `frontend/next.config.ts` headers configuration
- **WHEN** the Vitest security-headers suite runs
- **THEN** `Permissions-Policy` is present and disables camera, microphone, geolocation, payment, and usb

