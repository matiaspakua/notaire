## ADDED Requirements

### Requirement: Cloud install provides python3-venv for local security gates

`.cursor/install.sh` SHALL install the Debian/Ubuntu `python3-venv` package (or an
equivalent that provides `ensurepip`) during the apt toolchain block so
`python3 security/run_gates.py --profile local` can create `security/.tools/venv`
without apt mid-run.

#### Scenario: install.sh installs python3-venv

- **WHEN** a reviewer inspects `.cursor/install.sh` on this change
- **THEN** the apt install list includes `python3-venv`

### Requirement: Cloud install installs Playwright browsers for e2e and pages

`.cursor/install.sh` SHALL install package dependencies and Playwright browsers
for both `testing/e2e` and `github-page`, using each package directory so browser
revisions match that package's Playwright version. The e2e install SHALL include
chromium and webkit (mobile project uses WebKit via iPhone SE). The github-page
install SHALL include chromium.

#### Scenario: install.sh installs e2e chromium and webkit

- **WHEN** a reviewer inspects `.cursor/install.sh` on this change
- **THEN** the script runs Playwright install under `testing/e2e` with both
  `chromium` and `webkit` (with deps)

#### Scenario: install.sh installs github-page chromium

- **WHEN** a reviewer inspects `.cursor/install.sh` on this change
- **THEN** the script runs Playwright install under `github-page` with `chromium`
  (with deps)

### Requirement: Environment checklist documents venv and Playwright browsers

`docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md` SHALL document
`python3-venv` / ensurepip for security gates and the Playwright browser set
(chromium + webkit) for `testing/e2e` and `github-page`.

#### Scenario: checklist documents python3-venv and Playwright browsers

- **WHEN** a Cloud Agent or human reads the ENVIRONMENT-CHECKLIST toolchain /
  frontend extras tables
- **THEN** python3-venv (or ensurepip) and Playwright chromium + webkit for
  e2e/pages are documented
