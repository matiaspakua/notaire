## ADDED Requirements

### Requirement: Security Identity matches shipped MFA

The public security posture SHALL describe TOTP MFA as shipped. It SHALL say the default policy is OPTIONAL and that ENFORCE can require MFA for ADMIN and NOTARY. It SHALL NOT claim there is no second factor.

#### Scenario: Identity summary

- **WHEN** a visitor opens the security overview
- **THEN** the Identity posture names TOTP MFA and OPTIONAL / ENFORCE

### Requirement: Release history matches the product version on main

The Pages release snapshot SHALL use the root version from `.release-please-manifest.json`. Local preflight SHALL run the Pages `npm run build` so that file is refreshed before the export is checked.

#### Scenario: Current tag

- **WHEN** the release-please manifest root version is `X.Y.Z`
- **THEN** the Pages release history product version is `X.Y.Z` and its first tag is `vX.Y.Z`
