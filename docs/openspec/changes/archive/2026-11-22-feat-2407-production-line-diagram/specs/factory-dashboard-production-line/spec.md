## ADDED Requirements

### Requirement: Overview shows the line as a diagram

The Overview SHALL draw the production line as a rail with one station per stage from
`GET /factory/line`, in line order. Each station SHALL show its stage name, its state in words
(Done, Running, Failed, Next or Idle), its kind and its throughput. The diagram SHALL be operable
with the keyboard, and SHALL NOT make the page scroll sideways at 320px.

#### Scenario: One station per stage in line order

- **WHEN** the kit's line has the stages plan, red, pre-push, pre-pr and merge
- **THEN** the Overview shows five stations in that order on one rail

#### Scenario: The running station holds the token

- **WHEN** `GET /factory/status` reports a running run whose current stage is red
- **THEN** plan is Done, red is Running and holds the token, and the later stations are Next

#### Scenario: A failed run marks its station

- **WHEN** the run's status is failed at stage pre-push
- **THEN** the pre-push station says Failed, and the stations before it say Done

#### Scenario: Small screens get a vertical line

- **WHEN** the viewport is 320px wide
- **THEN** the stations stack on a vertical rail and the page has no horizontal scroll

### Requirement: Stations show their kind

Each station SHALL show a distinct marker and word for its kind: AI agent when the stage lists
agents, Human decision for the merge stage, and Deterministic step otherwise.

#### Scenario: Kinds follow the line data

- **WHEN** plan lists agents, merge lists none and release lists none
- **THEN** plan says AI agent, merge says Human decision, and release says Deterministic step

### Requirement: Gates show their last evidence state

Each station SHALL list its gates with the last state that `GET /factory/report` records for that
stage and gate: passed, failed, not evaluable (skip or todo) or not run (no evidence). A skipped
gate SHALL NEVER be shown as passed. The detail panel SHALL show each gate's mode, pass rate,
median time and run count.

#### Scenario: A skipped gate is not evaluable

- **WHEN** the report's last status for a gate is skip
- **THEN** the gate shows "Not evaluable", not "Passed"

#### Scenario: A gate without evidence says so

- **WHEN** the report has no entry for a gate
- **THEN** the gate shows "Not run"

### Requirement: The correction loop is drawn

When the line has both the red and pre-push stages, the diagram SHALL draw a loop from pre-push
back to red, labelled with the rule "One correction per blocker; if it persists, a human
decision".

#### Scenario: Loop between pre-push and red

- **WHEN** the line contains red and pre-push
- **THEN** a correction loop and its rule appear between those stations

### Requirement: Stations show throughput and cost

Each station SHALL show the runs through it, their average time, cost and failures, summed from
`GET /factory/telemetry?group=stage` rows whose key is the stage or one of the workflow's steps
mapped to that stage. With no rows the station SHALL say "no runs yet"; unknown cost SHALL be
named, never shown as zero.

#### Scenario: Step rows sum into their station

- **WHEN** the telemetry rows spec (2 runs, 60 s) and plan (1 run, 30 s) exist and the workflow
  maps spec to plan
- **THEN** the plan station shows 3 runs and a 30 s average

#### Scenario: No telemetry

- **WHEN** no telemetry row maps to the release station
- **THEN** the release station says "no runs yet"

### Requirement: Intent progress strip

Above the line, the Overview SHALL show the run's intent title, or that no intent is attached;
stations completed of total as a segmented bar with an accessible name; the elapsed time; the
cost so far, or that cost was not reported; and the current step.

#### Scenario: Progress of a running intent

- **WHEN** a run is at the third of seven stations with two finished steps costing $0.20 and $0.42
- **THEN** the strip reads "2 of 7 stations complete" and shows $0.62

#### Scenario: No intent attached

- **WHEN** the status has no intent
- **THEN** the strip says that no intent is attached to the run and offers to submit one

### Requirement: Station detail panel

Selecting a station with a click, Enter or Space SHALL show its kind, agents, skills, MCP and
tools, model, effort, inputs, outputs and grants, and a table of its gates, in a polite live
region. The running station SHALL be selected by default; with no run, the first station is.

#### Scenario: Keyboard selection

- **WHEN** a person moves to the pre-pr station with the arrow keys and presses Enter
- **THEN** the detail panel shows pre-pr's agents, skills and gates table
