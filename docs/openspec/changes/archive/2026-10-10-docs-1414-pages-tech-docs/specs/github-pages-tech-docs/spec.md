# github-pages-tech-docs

GitHub Pages exposes a technical documentation surface separate from the marketing timeline.

## ADDED Requirements

### Requirement: Docs navigation entry

The public site MUST provide a navigation entry that opens a Docs area distinct from in-page
marketing anchors (Origin, Evolution, Architecture, AI Era, Today).

#### Scenario: Visitor opens Docs

- **WHEN** a visitor uses the site navigation
- **THEN** a Docs (or Documentation) control routes to `/docs/` (with site `basePath`) and is not
  only an `#` fragment on the home page

### Requirement: Curated technical sections

The Docs area MUST present at least four sections: module ownership, architecture (SAD/ADR),
testing process, and security/DevSecOps process.

#### Scenario: Sections reachable

- **WHEN** a visitor opens `/docs/`
- **THEN** links or routes exist for Modules, Architecture, Testing, and Security (or equivalent
  English labels)

### Requirement: Deploy workflow unchanged

This change MUST NOT modify `.github/workflows/deploy-github-page.yml`.

#### Scenario: Workflow clean in the PR

- **WHEN** the pull request diff is reviewed
- **THEN** `deploy-github-page.yml` has no hunks
