# General rules to apply always

1. Before any change in the codebase, ensure you understand the existing code and its functionality.

2. Always create a branch for your changes following the naming convention `<type>/<issue-number>_<description>`. Always pull from main before creating the branch.

3. Every change **must** have an associated GitHub issue. Create one if it does not exist (using the GitHub CLI with the correct labels — run `gh label list` to see the repo's defined labels; there is no separate labels CSV) and link the PR to that issue.

4. Every issue **must** be associated with a Use Case (Caso de Uso) from the project documentation. If no Use Case exists, create it and update all related documentation first. No exceptions.

5. After creating the branch and starting work, move the GitHub issue to **IN PROGRESS**.

6. Write clear and concise commit messages following Conventional Commits format (`<type>(<scope>): <description>`, ending with `Closes #<issue-number>`).

6.1. **Commits must be atomic — no exceptions.** One logical change per commit: a commit that mixes unrelated concerns (e.g. a rule change and an unrelated data fix, or two independent features) must be split. This keeps `git log`, `git bisect`, and `git revert` useful, and makes PR review tractable. Only the commit(s) that actually close the issue carry `Closes #<issue-number>`; a commit that is one atomic step of a larger issue references the issue number without `Closes`.

7. Follow the coding standards and style guidelines established for the project. See `.claude/rules/programming.md`.

8. Code must be self-explanatory. Do not add comments unless strictly necessary (hidden constraint, subtle invariant, or a specific workaround). Never write comments that explain what the code does — only why.

9. Write unit tests for your code using TDD: write failing tests first, then implement, then refactor.

10. Before merging, ensure **all tests pass**: unit, integration, and E2E (Playwright for UI changes). Never skip tests without explicit, documented justification.

11. Apply **KIS** (Keep It Simple): write clean, simple, clear, easy-to-maintain code. Reject unnecessary complexity.

12. Apply **SRP** (Single Responsibility Principle) in both code and tests. Each class, method, and test should do exactly one thing.

13. Remove dead code when found. Refactor duplicate code to a single reusable unit. Avoid cyclomatic and cognitive complexity.

14. After every change, review and update the project documentation (business and engineering). Ensure documents are consistent and up to date. Centralize duplicated information. Move outdated or non-applicable documents to `docs/archive/`.
