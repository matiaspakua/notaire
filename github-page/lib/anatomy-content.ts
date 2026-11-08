/** Mermaid sources used by the Anatomy pages (#2089). Facts that change are read in lib/tech-facts.ts instead. */

export const MANAGEMENT_STATES_CHART = `
stateDiagram-v2
  [*] --> Iniciada
  Iniciada --> EnTramite: procedures assigned
  EnTramite --> DocCompleta: documents delivered
  DocCompleta --> SinFirmar: deed drafted
  SinFirmar --> Firmada: parties sign
  Firmada --> TestimonioGenerado: testimony generated
  TestimonioGenerado --> TestimonioIngresado: entered for registration
  TestimonioIngresado --> TestimonioRetirado: withdrawn
  DocCompleta --> Archivada: archive without a deed
  TestimonioRetirado --> [*]
  Archivada --> [*]
`;

export const DELIVERY_LOOP_CHART = `
flowchart LR
  ISSUE[Issue and use case] --> SPEC[OpenSpec change]
  SPEC --> TEST[Failing tests]
  TEST --> CODE[Implement]
  CODE --> LOCAL[preflight and pipeline]
  LOCAL --> PR[Pull request]
  PR --> MERGE[Review and merge]
  MERGE --> TAG[Version tag: product CI and images]
  TAG --> OPS[Operate and observe]
  OPS -->|finding or alert| ISSUE
`;

export const AI_SDLC_ANATOMY_CHART = `
flowchart TB
  subgraph rules [One rulebook]
    CON[CONSTITUTION.md and the agent card]
    OSP[OpenSpec schema notaire-sdlc]
    CAT[Skills and agents catalog]
  end
  BOARD[Roadmap, board and agent queue] --> FOR[Foreman agent]
  FOR -->|brief| WRK[Worker agents]
  WRK -->|spec, tests, code, PR| GATES{Five gates}
  GATES -->|evidence| FOR
  FOR -->|merge by rule| MAIN[main]
  OWN[Owner, human] -->|specs, exceptions, harness PRs| MAIN
  rules --> FOR
  rules --> WRK
`;
