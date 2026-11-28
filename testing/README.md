# testing/ (moved)

Product-level QA suites live in **[matiaspakua/notaire-testing](https://github.com/matiaspakua/notaire-testing)**.

Notaire pins reusable workflows (do not vendor a second copy of the suites):

```yaml
jobs:
  e2e:
    uses: matiaspakua/notaire-testing/.github/workflows/e2e.yml@e2bfc27200439898f5d7c0a29a218ceb0358cb28
    with:
      app-ref: ${{ github.sha }}
      testing-ref: e2bfc27200439898f5d7c0a29a218ceb0358cb28
  database-vv:
    uses: matiaspakua/notaire-testing/.github/workflows/database-vv.yml@e2bfc27200439898f5d7c0a29a218ceb0358cb28
    with:
      app-ref: ${{ github.sha }}
      testing-ref: e2bfc27200439898f5d7c0a29a218ceb0358cb28
```

## Local development

```bash
git clone https://github.com/matiaspakua/notaire-testing.git ~/workspace/notaire-testing
export NOTAIRE_APP_ROOT=$(pwd)
export NOTAIRE_TESTING_ROOT=~/workspace/notaire-testing
bash "$NOTAIRE_TESTING_ROOT/verify.sh"
```

Bruno API tests and `pg-integration` remain in this repository (#1190).
