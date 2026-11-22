# factory-dashboard — SSE security headers

## MODIFIED Requirements

### Requirement: SSE events stream carries security headers

The dashboard SSE events route (`GET /api/workspaces/:ws/factory/events`) MUST carry the same security headers
(CSP, X-Content-Type-Options, Referrer-Policy, Cache-Control) as every other response, even though
`reply.hijack()` bypasses the `onSend` hook.

#### Scenario: SSE response has security headers

- **GIVEN** a signed-in user requests the events stream
- **WHEN** the response is received
- **THEN** it includes `Content-Security-Policy` (with `default-src 'self'` and `frame-ancestors 'none'`),
  `X-Content-Type-Options: nosniff`, `Referrer-Policy: no-referrer`, `Cache-Control: no-store`,
  and `X-Accel-Buffering: no`
