// Unit tests for the LikeC4 computed view -> Mermaid conversion (#2483). Run: node --test scripts/
import assert from "node:assert/strict";
import { test } from "node:test";

import { toMermaid, mermaidId } from "./mermaid.mjs";

const node = (id, extra = {}) => ({
  id,
  parent: null,
  children: [],
  title: id,
  technology: null,
  shape: "rectangle",
  color: "primary",
  tags: [],
  ...extra,
});

const edge = (source, target, label, extra = {}) => ({
  id: `${source}:${target}`,
  source,
  target,
  label,
  line: "solid",
  relations: ["r1"],
  tags: [],
  ...extra,
});

const elementView = (nodes, edges, extra = {}) => ({
  _type: "element",
  id: "v",
  title: "A view",
  nodes,
  edges,
  ...extra,
});

test("ids become Mermaid-safe identifiers", () => {
  assert.equal(mermaidId("notaire.backend.security"), "notaire_backend_security");
  assert.equal(mermaidId("my-app.api"), "my_app_api");
});

test("an element view becomes a flowchart with the title in front matter", () => {
  const out = toMermaid(elementView([node("a"), node("b")], [edge("a", "b", "calls")]));
  assert.match(out, /^---\ntitle: "A view"\n---\nflowchart TB\n/);
  assert.match(out, /a -- "calls" --> b/);
});

test("shapes follow the LikeC4 shape", () => {
  const out = toMermaid(
    elementView(
      [node("p", { shape: "person" }), node("db", { shape: "storage" }), node("q", { shape: "queue" })],
      [],
    ),
  );
  assert.match(out, /p\(\["p"\]\)/);
  assert.match(out, /db\[\("db"\)\]/);
  assert.match(out, /q\[\["q"\]\]/);
});

test("technology is shown under the title", () => {
  const out = toMermaid(elementView([node("a", { title: "API", technology: "Spring Boot" })], []));
  assert.match(out, /a\["API<br\/><i>Spring Boot<\/i>"\]/);
});

test("nodes with children become nested subgraphs", () => {
  const out = toMermaid(
    elementView(
      [
        node("s", { children: ["s.c"], title: "System" }),
        node("s.c", { parent: "s", children: ["s.c.x"], title: "Container" }),
        node("s.c.x", { parent: "s.c", title: "X" }),
      ],
      [],
    ),
  );
  assert.match(out, /subgraph s\["System"\]\n {4}subgraph s_c\["Container"\]\n {6}s_c_x\["X"\]\n {4}end\n {2}end/);
});

test("dotted lines draw dotted, the default dashed line draws solid", () => {
  const out = toMermaid(
    elementView([node("a"), node("b")], [edge("a", "b", "x", { line: "dotted" }), edge("b", "a", "y", { line: "dashed" })]),
  );
  assert.match(out, /a -. "x" .-> b/);
  assert.match(out, /b -- "y" --> a/);
});

test("finding, debt and planned tags get their own classes", () => {
  const out = toMermaid(
    elementView(
      [node("f", { tags: ["finding"] }), node("d", { tags: ["debt", "finding"] }), node("p", { tags: ["planned"] })],
      [],
    ),
  );
  assert.match(out, /class f,d finding/);
  assert.match(out, /class p planned/);
  assert.match(out, /classDef finding /);
});

test("a finding relationship is drawn in red", () => {
  const out = toMermaid(
    elementView([node("a"), node("b")], [edge("a", "b", "ok"), edge("a", "b", "leak", { tags: ["finding"] })]),
  );
  assert.match(out, /linkStyle 1 stroke:#b91c1c/);
});

test("labels never carry characters that break Mermaid", () => {
  const out = toMermaid(elementView([node("a", { title: 'Say "hi"; bye' }), node("b")], [edge("a", "b", "x; y")]));
  assert.doesNotMatch(out, /;/);
  assert.match(out, /Say 'hi', bye/);
});

test("merged edges show the first relation and how many more", () => {
  const view = elementView([node("a"), node("b")], [edge("a", "b", "[...]", { relations: ["r1", "r2", "r3"] })]);
  const out = toMermaid(view, { relationTitle: (id) => ({ r1: "Reads", r2: "Writes", r3: "Locks" })[id] });
  assert.match(out, /a -- "Reads \(\+2\)" --> b/);
});

test("a sequence dynamic view becomes a numbered sequence diagram with returns", () => {
  const out = toMermaid({
    _type: "dynamic",
    id: "login",
    title: "Login",
    variant: "sequence",
    nodes: [node("u", { title: "User", shape: "person" }), node("api", { title: "API" })],
    edges: [
      edge("u", "api", "POST /login", { id: "step-01" }),
      edge("api", "u", "token", { id: "step-02", dir: "back" }),
    ],
  });
  assert.match(out, /^---\ntitle: "Login"\n---\nsequenceDiagram\n {2}autonumber\n/);
  assert.match(out, /actor u as User/);
  assert.match(out, /participant api as API/);
  assert.match(out, /u->>api: POST \/login/);
  assert.match(out, /api-->>u: token/);
});

test("a diagram dynamic view numbers its steps", () => {
  const out = toMermaid({
    _type: "dynamic",
    id: "flow",
    title: "Flow",
    variant: "diagram",
    nodes: [node("a"), node("b")],
    edges: [edge("a", "b", "first", { id: "step-01" }), edge("b", "a", "second", { id: "step-02" })],
  });
  assert.match(out, /flowchart LR/);
  assert.match(out, /a -- "1\. first" --> b/);
  assert.match(out, /b -- "2\. second" --> a/);
});
