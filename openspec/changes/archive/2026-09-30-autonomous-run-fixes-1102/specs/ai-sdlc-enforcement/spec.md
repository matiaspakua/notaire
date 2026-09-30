## ADDED Requirements

### Requirement: A pending review note requires a change

When a foreman review note for the phase exists and the worker's run changed no
file and made no commit, the attempt SHALL fail with a message naming the note.

#### Scenario: Review note ignored

- **WHEN** `review-spec.md` exists and the spec worker exits without changing anything
- **THEN** the attempt fails and the retry prompt asks to apply the review
