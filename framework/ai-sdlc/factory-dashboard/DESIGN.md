# Factory Control design

Mode: Operate. A manufacturing control room where engineers track, inspect and intervene in an
agentic production line, often for hours, next to an editor. The look is the one iterated on the
first dashboard (commit `57a0ea32`): industrial precision, cool steel grays for structure, and
phase and status colors as the only chroma. Version 2 keeps that look and changes what it shows:
only what the kit and RepoLens report.

## Principles

- **Evidence, not theatre.** Every number comes from the kit or RepoLens. When the kit has no
  runner, the queued station turns amber and says it waits for a runner adapter.
- **One vocabulary.** A status pill always carries its word (Idle, Waiting, Running, Done, Error);
  the tint only reinforces it. Cards, chips, tabs, tags and tables look the same on every screen.
- **States are designed.** Loading (skeleton), empty (says what to do, commands in code),
  error (names the problem and the recovery), stale, forbidden and read-only.

## Layout

- A 260px white sidebar with the brand mark, uppercase section labels (Views, Insights,
  Integrations), items with a 3px blue bar on the current one, and a footer with the theme and
  language toggles and the workspace's connection dot.
- A 56px top bar: the `aisdlc` badge, the workspace switcher with its id in mono, and the signed-in
  identity with its scope tag.
- The canvas (`#f7f8f9`) holds a 24px view title, a one-line subtitle, and phase groups: a 3px
  colored indicator, an uppercase label and a mono count, then cards or KPI rows.
- Under 768px the sidebar becomes a row with the brand and footer icons, and the navigation
  scrolls sideways under it. KPI rows take two columns under 900px.

## Tokens (`src/ui/styles.css`)

| Token | Light | Dark | Use |
|-------|-------|------|-----|
| `--bg-canvas` / `--bg-primary` | `#f7f8f9` / `#ffffff` | `#010409` / `#0d1117` | Canvas / cards, sidebar, top bar |
| `--linehair` / `--line-strong` | `#e8eaed` / `#b0b3b8` | `#21262d` / `#484f58` | Card borders / input borders |
| `--text` / `--text-secondary` | `#1d1f23` / `#5f6368` | `#e6edf3` / `#8b949e` | Text |
| `--text-faint` | `#696f75` | `#848d97` | Labels; darkened from `#9aa0a6` / `#6e7681` for 4.5:1 |
| `--phase-intake` `plan` `prove` `publish` `ship` | blue, violet, green, cyan, amber | GitHub-dark equivalents | Group indicators, KPI top borders, bars |
| `--status-*` and `--status-*-ink` | fill and text per state | same | Pills and station borders; inks keep pill text at 4.5:1 |

Type is the platform UI font at a 15px root; 13px body, 11px uppercase labels with 0.04em
tracking, 24px titles and KPI values with -0.03em, mono for ids, paths, tags and commands. Cards
use a 12px radius with a hairline border and a soft shadow on hover. Motion is 120–200 ms with
`cubic-bezier(0.16, 1, 0.3, 1)`; `prefers-reduced-motion` turns it off. Light or dark follows the
system until the person picks one in the sidebar footer.

## Components

`Group` (phase band), `Section` (card with title), `Kpi` (KPI card with a colored top border),
`StatusBadge` (pill), station cards on the production line, `Loaded` (the state machine for one
resource), `ErrorNote`, `Empty`, `ActionForm`, `Field`, and the icon set in `Icon.tsx`.

New in v2: station timeline with gate pills (required/advisory), intent bar and drawer with intake
form, live indicator dot in the sidebar, file tree and read-only viewer for configuration as code,
telemetry panel with grouped tables.

## Charts

Stage durations are horizontal bars in phase colors with mono values, like the first dashboard.
The coach's prompts-by-hour chart is one series with a hover readout and a table twin.
