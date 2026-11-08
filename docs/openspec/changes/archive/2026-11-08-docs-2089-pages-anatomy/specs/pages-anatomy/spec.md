<!-- Governed by CONSTITUTION.md. CU76, issue #2089. Evidence: vitest tests/anatomy.test.tsx, Playwright + axe on the three new pages at 375 and 1280 px. -->

## ADDED Requirements

### Requirement: The Pages site explains the system, the engineering loop and the AI SDLC with illustrated step-through figures

The site MUST offer, from the Product, Technical docs and AI Factory sections, three illustrated machines that a reader steps through, each step naming the station it lights and the repository paths where the real thing lives. Every named station and moving part MUST exist in the drawing, every named path MUST exist in the repository, and no step MAY claim that GitHub re-runs the product suite on a pull request. The pages MUST pass axe at WCAG 2 A and AA at 375 and 1280 px and MUST NOT play on their own.

#### Scenario: a step names things that exist

- **WHEN** a figure step lights a station and moves a part
- **THEN** both exist in the SVG and every path in its where line exists in the repository

#### Scenario: a reader steps through

- **WHEN** the reader presses Next
- **THEN** the readout changes, the other stations dim and the moving parts take the step's pose

#### Scenario: terms are defined

- **WHEN** a reader meets a notarial or engineering term
- **THEN** the glossary defines it in plain words and the anatomy pages link to it
