# Design — #1951 business forms audit

## Approach

Keep FormContainer composition as-is. Add a tiny pure helper:

```ts
requiredFieldErrors(values, message): Record<string, string>
```

Pages map domain required keys → helper → `FormField error={…}` and abort `handleSave`
when any error remains. List pages wire `isError` / `refetch` into DataTable like #1947.

## Decisions

- **Shared helper, not a form library** — KISS; pages own which fields are required.
- **Message** — one `common.fieldRequired` string reused across namespaces.
- **Management save** — close the dialog only on success (today `finally` closes even on error).

## Out of scope

- Migrating every admin CRUD dialog
- React Hook Form / Zod form schemas
- Wave 6 charts / Wave 7 a11y automation
