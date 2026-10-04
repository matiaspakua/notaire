# estado-actual-tie-test Specification

## Purpose

Keep the #1198 tie-break regression test valid under the #804 transition rule. Source: #1198; owner CU13.

## Requirements

### Requirement: The tie-break test does not rely on a status-changing PUT

The regression test for tied history dates MUST build its history without a PUT that changes
the status, which the transition rule (#804) rejects, and MUST still fail when the id tie-break
is removed from the endpoint.

#### Scenario: The tie test passes with the fix

- **WHEN** two history rows share one instant, the second appended directly, and `estado-actual` is requested
- **THEN** the later insert is returned and the test passes

#### Scenario: The tie test fails without the fix

- **WHEN** the id tie-break is removed from the endpoint
- **THEN** the same test fails
