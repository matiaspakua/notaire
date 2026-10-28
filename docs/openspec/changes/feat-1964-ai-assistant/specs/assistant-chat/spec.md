## ADDED Requirements

### Requirement: Assistant availability is controlled by a feature flag

The system SHALL keep the assistant disabled unless `NOTAIRE_ASSISTANT_ENABLED=true`, and SHALL expose
`GET /api/v1/assistant/status` returning `enabled`, `available` and `model` to any authenticated user.

#### Scenario: Assistant disabled by default

- **WHEN** the backend starts without `NOTAIRE_ASSISTANT_ENABLED`
- **THEN** `GET /api/v1/assistant/status` returns `enabled: false`, no request is sent to any model
  endpoint, and the dashboard renders no assistant panel

#### Scenario: Model server down

- **WHEN** the assistant is enabled and the model endpoint does not answer `GET /v1/models`
- **THEN** the status reports `available: false`, asking returns `503 ASSISTANT_UNAVAILABLE`, the panel
  shows the unavailable state, and every other screen keeps working

### Requirement: Model endpoint stays local

The system SHALL only call a model endpoint whose host is loopback, `host.docker.internal` or a
private-network address, and SHALL refuse to start the assistant with any other host unless
`NOTAIRE_ASSISTANT_ALLOW_REMOTE=true`.

#### Scenario: Remote endpoint refused

- **WHEN** `NOTAIRE_ASSISTANT_BASE_URL` points to a public host and `NOTAIRE_ASSISTANT_ALLOW_REMOTE` is unset
- **THEN** the assistant stays unavailable, the reason is logged without secrets, and no request leaves
  the installation

### Requirement: Streaming answers grounded in the current case

The system SHALL answer `POST /api/v1/assistant/conversations/{id}/messages` as a `text/event-stream`
of `token`, `tool`, `citation`, `done` and `error` events when the client accepts it, SHALL load the
case context named by `contextType` and `contextId` itself through the existing inbound ports, and
SHALL attach a citation for every case fact taken from a tool or context call.

#### Scenario: Ask about the management on screen

- **WHEN** an Escribano viewing management 123 asks what is missing to sign the deed
- **THEN** the answer streams token by token and cites the case summary and workflow records of
  management 123 with links to their screens

#### Scenario: User stops the answer

- **WHEN** the user presses Stop while an answer is streaming
- **THEN** the client closes the stream and the backend cancels the model call

### Requirement: Tools enforce the user's authorization

The system SHALL expose to the model only an allow-list of tools that delegate to existing inbound
ports, SHALL execute every tool in the security context of the requesting user, SHALL check the role
each tool declares before running it, and SHALL NOT resolve tools that were not attached to the request.

#### Scenario: Data the REST API denies is denied to the assistant

- **WHEN** a `ROLE_USER` user asks the assistant for data that only `ROLE_ADMIN` may read
- **THEN** the tool is not executed, the answer says the user lacks permission, and no restricted data
  reaches the model

#### Scenario: Prompt injection inside a document

- **WHEN** a retrieved document or tool result contains text instructing the assistant to ignore its
  rules or reveal another case
- **THEN** the assistant treats it as data, calls no extra tool because of it, and reveals nothing
  outside the requested case

### Requirement: No autonomous state change

The system SHALL NOT change any record as a result of a model decision alone; an action suggested by
the assistant SHALL be returned as a proposal that the user confirms in the UI, and the confirmed
action SHALL run through the existing REST endpoint and use case.

#### Scenario: Proposed action needs confirmation

- **WHEN** the assistant suggests preparing a registration draft
- **THEN** the client shows a confirmation card and nothing is written until the user confirms it

### Requirement: Privacy, retention and audit

The system SHALL record an audit entry for each interaction with user, timestamp, context, tool names
and outcomes, model id, token usage and latency, SHALL NOT write prompt or completion text to logs,
traces or metrics, SHALL scope conversations to their owner, and SHALL delete conversations older than
`NOTAIRE_ASSISTANT_RETENTION_DAYS`.

#### Scenario: Interaction is audited without content

- **WHEN** a user asks a question that calls the case-summary tool
- **THEN** one audit entry lists the tool and the context id, a personal-data read is audited when
  personal data was returned, and no span, log line or metric contains the question or answer text

#### Scenario: Conversation belongs to its owner

- **WHEN** a user requests or deletes another user's conversation
- **THEN** the system answers `404` and the conversation is unchanged

### Requirement: Assistant observability

The system SHALL export model, tool and retrieval observations through the existing OpenTelemetry
pipeline with model id, token usage, latency and outcome, and SHALL propagate the browser trace context
to the backend and the model server.

#### Scenario: One trace per question

- **WHEN** a user asks a question from the panel
- **THEN** a single trace links the browser request, the backend chat observation, each tool
  observation and the call to the model server
