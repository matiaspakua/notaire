# security-posture-register Specification

## Purpose
Keep the Notaire security register and the public cadence aligned with the files they describe. The portfolio may explain that posture. It must not present itself as a live scan.

## Requirements

### Requirement: The STRIDE register matches the markdown models

The repository SHALL keep a YAML register of STRIDE and LINDDUN identifiers. Every requirement id in that register SHALL appear in the system threat model or the authentication threat model. The register SHALL name STRIDE and LINDDUN, SHALL record that exploit procedures are absent, and SHALL NOT contain a high-confidence secret marker.

#### Scenario: A register id is missing from the prose

- **WHEN** `system.stride.yml` lists an `SR-*` or `LD-*` id that neither threat-model markdown contains
- **THEN** `security/tests/test_threat_register.py` fails

#### Scenario: Module grants are recorded as enforced

- **WHEN** the register row `SR-11` is read
- **THEN** its verification names `RoleModuleAuthorizationFilter` and issue 1683, and its status is Accepted

### Requirement: The public cadence matches workflow triggers

The portfolio cadence file SHALL classify each workflow it names. A release entry SHALL match a workflow whose tag filters are `v*` and `notaire-v*` and that has no `pull_request` trigger. The file SHALL state that it is not a live scan.

#### Scenario: A classified workflow does not have that trigger

- **WHEN** a cadence entry says `release` and the named workflow has no `v*` tag filter, or it has a `pull_request` trigger
- **THEN** `security/tests/test_public_security_cadence.py` fails

### Requirement: Stale control claims are not in the program

The cybersecurity program, the OWASP map, and the security README SHALL NOT claim that module grants are not yet enforced or that the image scan is report-only.

#### Scenario: A stale phrase returns

- **WHEN** one of those three documents contains `module grants not yet enforced` or `image scan report-only`
- **THEN** `security/tests/test_assessment_currency.py` fails
