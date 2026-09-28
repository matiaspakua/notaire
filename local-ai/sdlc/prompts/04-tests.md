# Phase 4 — Write FAILING tests for #{{ISSUE}} (Gate 2). No production code

Read `openspec/changes/{{CHANGE}}/specs/` (the scenarios) and the Testing
Strategy in `openspec/changes/{{CHANGE}}/design.md`.

1. For every scenario write at least one test. Put it where the design says.
   Open one neighbouring test in the same folder first and copy its setup style.
   A test must exercise the REAL code or the REAL resource file. Never build the
   bad state inside the test (e.g. `props.setProperty("bad.key", …)` then assert
   it is absent) — that test can never pass and proves nothing.
2. Run the new tests. EACH one MUST fail on its own — because the behaviour is missing, not
   because of a typo. A compile error for a class/method that does not exist
   yet is an acceptable red; a syntax error in your test is not.
3. Write `{{IO}}/tests.env` with one line — the exact command that runs ONLY
   your new/changed tests, e.g.

   ```text
   TEST_CMD=mvn -q -B test -pl backend-api -Dtest=FooServiceTest,FooControllerIntegrationTest
   ```

   (frontend example: `TEST_CMD=cd frontend && npx vitest run src/components/foo/Foo.test.tsx`)

4. Tick the group 3 tasks you completed in `openspec/changes/{{CHANGE}}/tasks.md`.
5. Commit tests only (never `.localai/`): `git add <test files> openspec/changes/{{CHANGE}}/tasks.md`
   then `git commit -m "test(<scope>): <what the tests pin down>" -m "Refs #{{ISSUE}}"`.

The foreman will run TEST_CMD and REQUIRE it to fail.
