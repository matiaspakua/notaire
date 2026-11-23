## ADDED Requirements

### Requirement: The 0.10 minor has a release train

The product release check MUST resolve the train `v0.10 · Factory production line & delivery gates (2027-03)` for every `0.10.z` version, and MUST still fail closed for a minor with no train.

#### Scenario: release 0.10.0

- **WHEN** the product version is `0.10.0`
- **THEN** the release check resolves the train `v0.10 · Factory production line & delivery gates (2027-03)`
- **AND** `check-product-version-coherence.py --tag v0.10.0` passes on a coherent `0.10.0` tree

#### Scenario: an unmapped minor still fails closed

- **WHEN** the product version is `1.1.0` and no `v1.1 · …` train exists
- **THEN** the release check fails with a message naming the missing train

### Requirement: The release branch is gated on its train

The Release Please job MUST run `check-product-version-coherence.py --tag v<next version>` on the release branch after it syncs the OpenAPI version, so a cut without a train fails before its tag exists.

#### Scenario: next cut has no train

- **WHEN** Release Please updates the release PR for a minor with no train
- **THEN** the Release Please job fails naming the missing train
- **AND** no tag is created until the train exists