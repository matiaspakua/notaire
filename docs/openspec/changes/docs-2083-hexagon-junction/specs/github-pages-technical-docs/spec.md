## ADDED Requirements

### Requirement: The hexagonal page explains ports and adapters as a junction instrument

The hexagonal architecture page SHALL lead, after its header and counts, with an isometric junction instrument. The sealed case SHALL be the domain. Glands on the front face SHALL be inbound ports and glands on the rear of the lid SHALL be outbound ports. Plug modules for HTTP web, JPA persistence and PDF SHALL mate only to glands. The live readout SHALL name the selected gland and, when a plug is mated, the dependency rule adapter → port → domain. The figure SHALL be operable from the keyboard and SHALL stay still under reduced motion. The caption counts SHALL be the same counts the page prints.

#### Scenario: A reader looks at the instrument

- **WHEN** a reader opens the hexagonal architecture page
- **THEN** the junction instrument is above the package map and its caption states adapter → port → domain

#### Scenario: Keyboard

- **WHEN** a keyboard user focuses the instrument and presses the right arrow
- **THEN** the readout names the next gland

#### Scenario: Nothing is drilled through the case

- **WHEN** the figure geometry is checked
- **THEN** every gland, cable and plug stops on the skin of the case

### Requirement: The AI Factory home points at the gated line

The AI Factory home SHALL explain the factory as the gated line in one short passage and SHALL link to the lifecycle page, where that figure is drawn. It SHALL NOT mount a second copy of the gated line.

#### Scenario: A reader wants the instrument

- **WHEN** a reader reads the factory home
- **THEN** the passage names the carriage and the boom and links to `/ai-sdlc/lifecycle/`
