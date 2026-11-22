## ADDED Requirements
### Requirement: Every product minor has a release train
The product release check MUST find a train titled v<MAJOR>.<MINOR> · … for every released version, and MUST fail closed when none exists.
#### Scenario: release 0.8.0
- **WHEN** the product version is 0.8.0
- **THEN** the release check resolves the train 'v0.8 · Observable self-managing factory (2027-03)'
