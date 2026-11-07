## ADDED Requirements

### Requirement: The Technical docs tell the engineering artifact chain

The Technical docs SHALL include an Engineering artifacts page that walks the artifact chain in order: analyze, define, design, specify, build, test, secure, release and operate. For each artifact it SHALL say what it is, why it exists, the method and tool behind it, and the repository path where it lives. Every count it shows SHALL be read from the repository at build time and SHALL show as missing when it cannot be read. Every path it cites SHALL exist.

#### Scenario: A reader looks for the threat model

- **WHEN** a reader searches the site for "threat model"
- **THEN** the Engineering artifacts page is among the results and its security stage links `THREAT-MODEL-SYSTEM.md`

#### Scenario: A use case is added

- **WHEN** a new `CUxx` file is committed and the site is rebuilt
- **THEN** the use-case count on the Engineering artifacts page changes without an edit to the page

### Requirement: Explainer figures are honest and accessible

The artifact cabinet and the gated line figures SHALL draw their numbers from the same data the page prints. They SHALL be operable with the keyboard, SHALL stay still under reduced motion, and SHALL read at 390 px wide.

#### Scenario: Reduced motion

- **WHEN** the reader's system asks for reduced motion
- **THEN** neither figure animates and both show their resting state with a correct readout

#### Scenario: Keyboard

- **WHEN** a keyboard user focuses the cabinet and presses the arrow keys
- **THEN** the next drawer opens and the readout names its artifact and count