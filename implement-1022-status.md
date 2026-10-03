# Implement #1022 — English NamedQuery name strings

| Field | Value |
|-------|-------|
| Issue | [#1022](https://github.com/matiaspakua/notaire/issues/1022) |
| PR | https://github.com/matiaspakua/notaire/pull/1187 |
| Branch | `cursor/chore-1022-namedquery-english-69d3` |
| Head SHA | `fdab215a52c3907aa5a0f0916ed03ba5014653b6` |
| Base | `main` (`489599e1`) |
| OpenSpec | `openspec/changes/chore-1022-namedquery-english/` — `validate-sdlc-plan.sh` ✓ |
| Use Case | none (same exception as epic #973) |
| Store copy | Agent Store not mounted — no `internal/openspec-1022/` copy |

## Results

| Check | Result |
|-------|--------|
| Hygiene TDD red | 3 failures (Spanish prefixes / Persona / Folio·Item·Person tails) |
| Hygiene green after rename | 3/3 pass |
| `mvn test -pl backend-api` | **1941** tests, 0 failures |
| `mvn verify -pl backend-api -DskipTests` | BUILD SUCCESS |
| Residual Spanish NamedQuery prefixes | **0** |
| NamedQuery total in `business/` | 137 |

## Notes for coordinator

- Do **not** merge until `bash scripts/check-heavy-ci.sh 1187` exits 0.
- Spring Data collisions: JPQL named *parameters* Englishized only where
  NamedQuery name matches a repository method; `User.findByPersonId` avoids
  colliding with `UserRepository.findByFkIdPerson(Person)`.
- `in-progress` label ACL returned 403 (ignored).
