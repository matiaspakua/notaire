# Phase 5 — Implement #{{ISSUE}} (make the tests green)

Tests to satisfy: `{{TEST_CMD}}` (see `{{IO}}/tests.env`).
Spec: `docs/openspec/changes/{{CHANGE}}/`. Group 4 of `tasks.md` is your work list.

1. Write the minimum production code that makes the tests pass and satisfies
   the scenarios. Follow the design. Do NOT edit the tests to make them pass
   (fixing a genuine mistake in a test is allowed — explain it in the commit body).
2. Update existing tests broken by the intended behaviour change (group 5).
3. Run `{{TEST_CMD}}` until green, plus the existing test classes of the code you
   changed (`grep -rl <ClassName> backend-api/src/test`). Do NOT run the full
   suite — the foreman runs it and sends you any failure.
4. Refactor what you touched: KIS, SRP, no duplication, no dead code.
5. Tick ONLY the group 4 and 5 items of `tasks.md` that are now true — change `[ ]` to `[x]`, nothing else
   in that line, no rewording, no new items. Groups 6 and later are ticked by the foreman after its own gates
   run them: never tick them, never write results you did not produce.
6. Commit (can be several atomic commits). The last one ends with
   `Closes #{{ISSUE}}` in the body.

The foreman will run TEST_CMD and the full suite and REQUIRE both to pass.
