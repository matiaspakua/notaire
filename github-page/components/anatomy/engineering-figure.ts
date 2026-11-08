/**
 * Fig 2 — the release line. A work package rides a conveyor past six stations: the workbench (dev),
 * the test rig (QA), the scanner arch (security), the gate board (CI), the three environment crates
 * (infrastructure) and the observability cabinet (ops). An alarm lamp on the workbench closes the loop.
 */
import { base, box, dialFace, knob, needle, post, ruler, sceneOf, tag, k } from "./parts";
import type { FigureData } from "./figure-types";

const WIDTH = 780;
const HEIGHT = 440;
const PLATE = { x: -262, y: -96, w: 560, d: 176, r: 10 };
const PLATE_H = 12;
const FOOT_H = 5;
const BELT_Y = 34;
const PKG_REST = -226;

const P = k.fitProjection(
  [...k.boxCorners(PLATE, -PLATE_H - FOOT_H, 0), [-210, -40, 60], [-120, -40, 66], [-70, 20, 72], [190, -40, 70], [30, -50, 60]],
  WIDTH,
  HEIGHT,
  { pad: 26, azimuth: 40, elevation: 30 },
);
const s = sceneOf(P);
const { S, L, D, at } = s;

const move = (dx: number, dy = 0, dz = 0) => {
  const [x, y] = k.axisVector(P, [dx, dy, dz]);
  return `translate(${x.toFixed(2)}px, ${y.toFixed(2)}px)`;
};
type Item = { depth: number; svg: string };
const items: Item[] = [];
const put = (centre: [number, number, number], svg: string) => items.push({ depth: k.depthOf(centre, P), svg });
const station = (n: number, svg: string) => `<g data-station="${n}">${svg}</g>`;
const lamp = (x: number, y: number, z: number, on = false) =>
  S(k.cylinder(x, y, 2.8, z, 3.4, P, 18, 0.2), { tone: on ? "hi" : "lo", lit: on }) + D([at([x, y, z + 3.4])], { size: 0.45, tone: on ? "hi" : "mid" });

// ---- floor and conveyor ---------------------------------------------------------------------
const floor = base(s, PLATE, PLATE_H, FOOT_H);
const belt = (() => {
  const out: string[] = [];
  out.push(S(k.slabOf({ x: -244, y: BELT_Y - 4, w: 508, d: 30, r: 3 }, 0, 4, P, 4, 0.5), { tone: "mid" }));
  out.push(S(k.slabOf({ x: -240, y: BELT_Y, w: 500, d: 22, r: 2 }, 4, 1.6, P, 3, 0.3), { tone: "lo" }));
  for (let x = -230; x < 252; x += 14) out.push(L(k.lineOnTop([x, BELT_Y + 1], [x, BELT_Y + 21], 5.6, P), { tone: "faint" }));
  for (const x of [-244, 254]) out.push(S(k.cylinder(x + 5, BELT_Y + 11, 10, 0, 6, P, 28), { tone: "mid" }));
  out.push(D(k.corners({ x: -244, y: BELT_Y - 4, w: 508, d: 30, r: 3 }, 3).map(([x, y]) => at([x, y, 4])), { size: 0.4, tone: "lo" }));
  return out.join("");
})();

// ---- 1  workbench (dev) ----------------------------------------------------------------------
const bench = (() => {
  const out: string[] = [];
  out.push(box(s, { x: -246, y: -62, w: 62, d: 62, r: 2 }, 0, 20, { tone: "mid" }));
  out.push(S(k.slabOf({ x: -250, y: -66, w: 70, d: 70, r: 3 }, 20, 3.4, P, 4, 0.6), { tone: "mid" }));
  out.push(L(k.planOutline(k.insetPlan({ x: -250, y: -66, w: 70, d: 70, r: 3 }, 3), 23.4, P, 3), { tone: "faint" }));
  // vise: fixed jaw, moving jaw, screw and handle
  out.push(S(k.slabOf({ x: -240, y: -56, w: 10, d: 22, r: 1.2 }, 23.4, 9, P, 2, 0.4), { tone: "hi" }));
  out.push(S(k.slabOf({ x: -228, y: -56, w: 4, d: 22, r: 1 }, 23.4, 8, P, 2, 0.3), { tone: "mid" }));
  out.push(S(k.cylinder(-212, -45, 1.6, 26, 10, P, 14), { tone: "mid", crease: "none" }));
  out.push(knob(s, -212, -45, 3, 36, 2.2, "mid"));
  // spec binder: a stack of OpenSpec artifacts, the top one lit
  [0, 1, 2, 3, 4].forEach((i) => out.push(S(k.slabOf({ x: -242 + i * 0.8, y: -26 + i * 0.6, w: 20, d: 24, r: 0.8 }, 23.4 + i * 2, 2, P, 2, 0.2), { tone: i === 4 ? "hi" : "mid", lit: i === 4 })));
  out.push(L(k.lineOnTop([-236, -16], [-226, -16], 33.4, P), { tone: "lo", free: true }));
  // branch flag on a post
  out.push(post(s, -200, -54, 1.8, 23.4, 30, "mid"));
  out.push(S(k.slabOf({ x: -200, y: -66, w: 1.4, d: 22, r: 0.6 }, 40, 12, P, 2), { tone: "lo", crease: "none" }));
  out.push(L(k.segment([-198.6, -62, 48], [-198.6, -48, 48], P), { tone: "hi", free: true }));
  // alarm lamp: the feedback loop ends here
  out.push(S(k.cylinder(-240, -6, 4.6, 23.4, 2, P, 22), { tone: "lo" }));
  out.push(S(k.cylinder(-240, -6, 3.4, 25.4, 6, P, 22), { tone: "mid" }));
  out.push(tag(s, -222, -2, 23.4, 1));
  return out.join("");
})();
const alarm = `<g data-part="alarm" style="opacity:0">${S(k.cylinder(-240, -6, 3.4, 25.4, 6, P, 22), { tone: "hi", lit: true })}${S(k.cylinder(-240, -6, 2.2, 31.4, 2.6, P, 18), { tone: "hi", lit: true })}</g>`;
put([-216, -30, 12], station(1, bench));
put([-240, -6, 28], station(1, alarm));

// ---- 2  test rig (QA) -------------------------------------------------------------------------
const rig = (() => {
  const out: string[] = [];
  out.push(box(s, { x: -172, y: -64, w: 64, d: 68, r: 2 }, 0, 10, { tone: "mid" }));
  // test pyramid: many unit blocks, fewer integration, one end-to-end
  const rows: Array<[number, number, number, number]> = [
    [4, 11, 10, 0], [4, 11, 10, 1], [4, 11, 10, 2], [4, 11, 10, 3],
  ];
  rows.forEach(([, , , i]) => out.push(S(k.slabOf({ x: -166 + i * 13.4, y: -56, w: 12, d: 22, r: 1.2 }, 10, 8, P, 2, 0.3), { tone: "mid" })));
  [0, 1].forEach((i) => out.push(S(k.slabOf({ x: -158 + i * 17, y: -54, w: 14, d: 18, r: 1.2 }, 18, 8, P, 2, 0.3), { tone: "hi" })));
  out.push(S(k.slabOf({ x: -150, y: -52, w: 14, d: 14, r: 1.2 }, 26, 8, P, 2, 0.3), { tone: "hi", lit: true }));
  out.push(L(k.planOutline(k.insetPlan({ x: -150, y: -52, w: 14, d: 14, r: 1.2 }, 2.4), 34, P, 2), { tone: "faint" }));
  // coverage gauge on a post
  out.push(post(s, -126, -22, 2.6, 10, 26, "mid"));
  out.push(dialFace(s, -126, -22, 12, 36));
  out.push(`<path data-part="needle-qa" class="iso-line" data-tone="lit" data-free="start" d="${needle(s, -126, -22, 12, 36, 0.3)}" style="stroke-width:1"/>`);
  // ratchet floor marker on the ruler
  out.push(ruler(s, -168, -112, -14, 10));
  out.push(knob(s, -160, -28, 3, 10, 3.4, "mid"));
  out.push(D(k.corners({ x: -172, y: -64, w: 64, d: 68, r: 0 }, 3.4).map(([x, y]) => at([x, y, 10])), { size: 0.4, tone: "lo" }));
  out.push(tag(s, -168, -60, 34, 2));
  return out.join("");
})();
put([-140, -30, 14], station(2, rig));

// ---- 3  security scanner arch ------------------------------------------------------------------
const arch = (() => {
  const out: string[] = [];
  for (const y of [14, 64]) {
    out.push(S(k.cylinder(-62, y + 4, 8, 0, 3, P, 24), { tone: "lo" }));
    out.push(S(k.slabOf({ x: -66, y, w: 9, d: 9, r: 1.5 }, 3, 62, P, 3, 0.5), { tone: "mid" }));
    out.push(D([at([-61.5, y + 4.5, 65])], { size: 0.45 }));
  }
  out.push(S(k.slabOf({ x: -68, y: 12, w: 13, d: 62, r: 2 }, 64, 9, P, 3, 0.6), { tone: "mid" }));
  out.push(L(k.planOutline(k.insetPlan({ x: -68, y: 12, w: 13, d: 62, r: 2 }, 2.2), 73, P, 3), { tone: "faint" }));
  // scanner lenses (three tools) on the lintel
  [24, 38, 52].forEach((y, i) => {
    out.push(S(k.cylinder(-61.5, y, 5, 73, 3, P, 24, 0.4), { tone: i === 1 ? "hi" : "mid", lit: i === 1 }));
    out.push(L(k.ring(-61.5, y, 3, 76, P, 18), { tone: "lo" }));
  });
  out.push(tag(s, -61.5, 66, 73, 3));
  return out.join("");
})();
const scanRack = (() => {
  const out: string[] = [];
  out.push(box(s, { x: -90, y: -60, w: 44, d: 60, r: 2 }, 0, 12, { tone: "mid" }));
  [0, 1, 2].forEach((i) => {
    out.push(S(k.slabOf({ x: -84, y: -54 + i * 18, w: 30, d: 14, r: 1.4 }, 12, 12, P, 3, 0.4), { tone: "mid" }));
    out.push(L(k.planOutline(k.insetPlan({ x: -84, y: -54 + i * 18, w: 30, d: 14, r: 1.4 }, 2), 24, P, 2), { tone: "faint" }));
    out.push(lamp(-60, -47 + i * 18, 24, false));
    out.push(k.faceTextSvg(k.topMatrix([-72, -45.5 + i * 18], 24, P, "x"), ["SAST", "SCA", "DAST"][i], { size: 3.6, tone: "mid", anchor: "middle" }));
  });
  return out.join("");
})();
const scanLamps = `<g data-part="scan-lamps" style="opacity:0">${[0, 1, 2].map((i) => S(k.cylinder(-60, -47 + i * 18, 2.8, 24, 3.4, P, 18, 0.2), { tone: "hi", lit: true })).join("")}</g>`;
put([-62, 38, 30], station(3, arch));
put([-68, -30, 12], station(3, scanRack));
put([-60, -30, 26], station(3, scanLamps));

// ---- 4  CI gate board --------------------------------------------------------------------------
const board = (() => {
  const out: string[] = [];
  out.push(box(s, { x: -22, y: -62, w: 84, d: 22, r: 2 }, 0, 14, { tone: "mid" }));
  out.push(S(k.slabOf({ x: -26, y: -66, w: 92, d: 30, r: 3 }, 14, 3, P, 4, 0.6), { tone: "mid" }));
  const names = ["lint", "unit", "api", "e2e", "sec", "docs"];
  names.forEach((n, i) => {
    const x = -14 + i * 15;
    out.push(S(k.cylinder(x, -50, 5.4, 17, 1.2, P, 22), { tone: "lo" }));
    out.push(lamp(x, -50, 18.2, false));
    out.push(k.faceTextSvg(k.topMatrix([x, -41], 17, P, "x"), n, { size: 3, tone: "mid", anchor: "middle" }));
  });
  // foreman's merge lever
  out.push(S(k.slabOf({ x: 54, y: -58, w: 10, d: 14, r: 1.4 }, 17, 4, P, 3, 0.4), { tone: "mid" }));
  out.push(S(k.slabOf({ x: -22, y: -34, w: 84, d: 5, r: 1 }, 0, 12, P, 2, 0.3), { tone: "lo", crease: "none" }));
  out.push(tag(s, -20, -64, 17, 4));
  return out.join("");
})();
const boardLamps = `<g data-part="gate-lamps" style="opacity:0">${[0, 1, 2, 3, 4, 5].map((i) => S(k.cylinder(-14 + i * 15, -50, 2.8, 18.2, 3.4, P, 18, 0.2), { tone: "hi", lit: true })).join("")}</g>`;
const lever = `<g data-part="lever">${S(k.cylinder(59, -51, 1.6, 21, 14, P, 14), { tone: "hi", crease: "none" })}${S(k.cylinder(59, -51, 3.4, 35, 3.4, P, 18, 0.3), { tone: "hi", lit: true })}</g>`;
put([20, -50, 8], station(4, board));
put([20, -50, 18], station(4, boardLamps));
put([59, -51, 24], station(4, lever));

// ---- 5  environment crates ---------------------------------------------------------------------
const crates = (() => {
  const out: string[] = [];
  out.push(S(k.slabOf({ x: 78, y: -66, w: 92, d: 100, r: 3 }, 0, 3, P, 4, 0.5), { tone: "lo" }));
  // dev (compose), staging (kustomize), production (compose + one reverse proxy)
  const defs: Array<{ x: number; y: number; w: number; d: number; h: number; tone: "mid" | "hi" }> = [
    { x: 84, y: -60, w: 24, d: 30, h: 16, tone: "mid" },
    { x: 84, y: -26, w: 30, d: 32, h: 22, tone: "mid" },
    { x: 124, y: -60, w: 40, d: 56, h: 30, tone: "hi" },
  ];
  defs.forEach((c) => {
    out.push(S(k.slabOf({ x: c.x, y: c.y, w: c.w, d: c.d, r: 2 }, 3, c.h, P, 3, 0.5), { tone: c.tone }));
    out.push(L(k.planOutline(k.insetPlan({ x: c.x, y: c.y, w: c.w, d: c.d, r: 2 }, 2.4), 3 + c.h, P, 3), { tone: "faint" }));
    out.push(D(k.corners({ x: c.x, y: c.y, w: c.w, d: c.d, r: 0 }, 3).map(([x, y]) => at([x, y, 3 + c.h])), { size: 0.4, tone: "lo" }));
  });
  // production crate carries the only host-facing door: a reverse proxy
  out.push(S(k.slabOf({ x: 156, y: -42, w: 8, d: 14, r: 1.2 }, 12, 12, P, 2, 0.3), { tone: "mid" }));
  out.push(L(k.segment([164, -40, 18], [164, -30, 18], P), { tone: "hi", free: true }));
  // chip dots: services inside each crate
  [[90, -50], [96, -50], [90, -16], [96, -16], [104, -16]].forEach(([x, y]) => out.push(S(k.cylinder(x, y, 2, 3 + (x < 100 ? 16 : 22), 0.1, P, 12), { tone: "lo", flat: true })));
  [[96, -45, "DEV"], [99, -10, "STG"], [144, -32, "PROD"]].forEach(([x, y, label], i) => {
    const top = 3 + [16, 22, 30][i];
    out.push(k.faceTextSvg(k.topMatrix([x as number, (y as number) + 2], top, P, "x"), label as string, { size: 4.4, tone: "hi", anchor: "middle" }));
  });
  out.push(tag(s, 90, 30, 3, 5));
  return out.join("");
})();
const lift = `<g data-part="crate-lift">${S(k.slabOf({ x: 124, y: -60, w: 40, d: 56, r: 2 }, 3, 30, P, 3, 0.5), { tone: "hi", lit: true })}</g>`;
put([124, -30, 10], station(5, crates));
put([144, -32, 20], station(5, `<g data-part="crate-top" style="opacity:0">${S(k.slabOf({ x: 122, y: -62, w: 44, d: 60, r: 2 }, 33, 3, P, 3, 0.5), { tone: "hi", lit: true })}</g>`));

// ---- 6  observability cabinet ------------------------------------------------------------------
const OX = 196;
const cabinet = (() => {
  const out: string[] = [];
  out.push(box(s, { x: OX, y: -70, w: 62, d: 80, r: 3 }, 0, 16, { tone: "mid" }));
  out.push(S(k.slabOf({ x: OX - 3, y: -73, w: 68, d: 86, r: 3 }, 16, 3, P, 4, 0.6), { tone: "mid" }));
  [OX + 14, OX + 44].forEach((x) => [-52, -26, 0].forEach((y) => {
    if (x === OX + 14 && y === 0) return;
    out.push(S(k.cylinder(x, y, 9.4, 19, 1.2, P, 36), { tone: "lo" }));
    out.push(dialFace(s, x, y, 8.2, 20.2));
  }));
  [-52, -26].forEach((y, i) => out.push(`<path data-part="needle-ops-${i}" class="iso-line" data-tone="lit" data-free="start" d="${needle(s, OX + 44, y, 8.2, 20.2, 0.25)}" style="stroke-width:1"/>`));
  out.push(`<path data-part="needle-ops-2" class="iso-line" data-tone="lit" data-free="start" d="${needle(s, OX + 14, -52, 8.2, 20.2, 0.25)}" style="stroke-width:1"/>`);
  out.push(lamp(OX + 14, -26, 20.2, false));
  out.push(tag(s, OX + 8, 4, 19, 6));
  return out.join("");
})();
put([OX + 30, -30, 10], station(6, cabinet));

// ---- the package ------------------------------------------------------------------------------
const pkg = (() => {
  const out: string[] = [];
  out.push(S(k.slabOf({ x: -14, y: BELT_Y + 2, w: 24, d: 18, r: 2 }, 5.6, 14, P, 3, 0.5), { tone: "hi", lit: true }));
  out.push(L(k.planOutline(k.insetPlan({ x: -14, y: BELT_Y + 2, w: 24, d: 18, r: 2 }, 2.4), 19.6, P, 3), { tone: "lo" }));
  out.push(L(k.lineOnTop([-14, BELT_Y + 11], [10, BELT_Y + 11], 19.6, P), { tone: "lo" }));
  return out.join("");
})();

const lateral = (x: number) => move(x, 0, 0);
const bodyParts = [floor, belt, ...items.sort((a, b) => a.depth - b.depth).map((i) => i.svg), `<g data-part="package" style="transform:${lateral(PKG_REST)}">${pkg}</g>`];

const NEUTRAL = {
  alarm: { opacity: 0 },
  "scan-lamps": { opacity: 0 },
  "gate-lamps": { opacity: 0 },
  "crate-top": { opacity: 0 },
  lever: { transform: move(0, 0, 0) },
  "needle-qa": { d: needle(s, -126, -22, 12, 36, 0.3) },
  "needle-ops-0": { d: needle(s, OX + 44, -52, 8.2, 20.2, 0.25) },
  "needle-ops-1": { d: needle(s, OX + 44, -26, 8.2, 20.2, 0.25) },
  "needle-ops-2": { d: needle(s, OX + 14, -52, 8.2, 20.2, 0.25) },
};
const at1 = (x: number) => ({ package: { transform: lateral(x) } });
void lift;

export const engineeringFigure: FigureData = {
  id: "engineering",
  fig: "Fig 2",
  title: "The release line",
  hint: "Pick a station",
  label:
    "An isometric conveyor machine on a base plate. A work package rides a belt past six stations: a workbench with a vise and a stack of spec documents (development), a test rig with a pyramid of blocks and a coverage dial (QA), a scanner arch over the belt with three lenses (security), a gate board with six check lamps and a merge lever (the local preflight), three environment crates of growing size (infrastructure) and a cabinet of six dials and a lamp (operations).",
  caption:
    "From a request to a running system. Every package is checked at every station, and what operations learn at the end comes back to the workbench as a new issue.",
  width: WIDTH,
  height: HEIGHT,
  svg: k.figureSvg({
    width: WIDTH,
    height: HEIGHT,
    label:
      "A work package rides a conveyor past a workbench, a test rig, a security scanner arch, a CI gate board, three environment crates and an observability cabinet.",
    body: bodyParts,
  }),
  keys: [
    { mark: "lit", label: "The work package and the check that is running" },
    { mark: "raised", label: "Six numbered stations: dev, QA, security, gates, environments, ops" },
    { mark: "dim", label: "Dimmed while another station is explained" },
  ],
  steps: [
    {
      station: 1,
      title: "Develop: the workbench",
      text: "Every change starts as a GitHub issue tied to a use case (CU-XX), then an OpenSpec change: proposal, specs, design and tasks. The branch is named type/issue_description, and tests are written to fail first (TDD).",
      where: "CONSTITUTION.md, docs/openspec/, .claude/rules/ai-agent-workflow.md",
      readout: "issue, spec, failing test",
      pose: { ...NEUTRAL, ...at1(PKG_REST) },
    },
    {
      station: 2,
      title: "QA: the test rig",
      text: "The pyramid has many unit tests (JUnit, Vitest), fewer integration and API tests (Spring, Bruno), a few end-to-end tests (Playwright) and database checks. Coverage is a raise-only ratchet: the floor can go up, never down.",
      where: "docs/300-development/303-testing/, .claude/rules/code-quality.md",
      readout: "coverage floor holds",
      pose: { ...NEUTRAL, "needle-qa": { d: needle(s, -126, -22, 12, 36, 0.72) }, ...at1(-136) },
    },
    {
      station: 3,
      title: "Security: the scanner arch",
      text: "Scanners look at the same package everywhere: secret scanning and Semgrep rules in the local catalog, Trivy on files and images, CodeQL on a version tag and every Monday, and an OWASP ZAP baseline every Monday. The local case catalog runs inside the pipeline before a pull request.",
      where: "security/ (run_gates.py, cases/catalog.yml), .github/workflows/codeql.yml, dast-zap.yml",
      readout: "secrets, SAST, SCA clean",
      pose: { ...NEUTRAL, "scan-lamps": { opacity: 1 }, ...at1(-62) },
    },
    {
      station: 4,
      title: "Gates: the preflight board",
      text: "A pull request is proved on the agent machine: preflight --full runs lint, unit, API and end-to-end checks, the security cases and the docs checks. GitHub does not re-run the product suite on a pull request; it runs on a version tag. The Foreman merges Worker pull requests by rule, and the Owner merges the Constitution and harness changes.",
      where: "workspace/sdlc/preflight.sh, run_pipeline.sh, check-heavy-ci.sh, docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md",
      readout: "all lamps green, lever pulled",
      pose: { ...NEUTRAL, "gate-lamps": { opacity: 1 }, lever: { transform: move(0, 0, -7) }, ...at1(20) },
    },
    {
      station: 5,
      title: "Environments: the crates",
      text: "Development runs from Docker Compose with published ports. Staging is Kubernetes manifests (Kustomize base plus a staging overlay). Production is a separate Compose file where only the reverse proxy is published on the host. Images are built, signed and published to GHCR on a version tag.",
      where: "docker-compose.yml, docker-compose.prod.yml, infra/deploy/kustomize",
      readout: "image promoted",
      pose: { ...NEUTRAL, "crate-top": { opacity: 1 }, ...at1(118) },
    },
    {
      station: 6,
      title: "Operate: the observability cabinet",
      text: "Prometheus scrapes metrics, Loki keeps logs, Tempo keeps traces through an OpenTelemetry collector, Alertmanager pages, Grafana draws it all, and SonarQube tracks code quality. A smoke test proves a release before its issue closes.",
      where: "infra/observability, infra/scripts, docs/200-architecture/207-monitoring/",
      readout: "dials live, alerts armed",
      pose: {
        ...NEUTRAL,
        "needle-ops-0": { d: needle(s, OX + 44, -52, 8.2, 20.2, 0.55) },
        "needle-ops-1": { d: needle(s, OX + 44, -26, 8.2, 20.2, 0.4) },
        "needle-ops-2": { d: needle(s, OX + 14, -52, 8.2, 20.2, 0.68) },
        ...at1(226),
      },
    },
  ],
};
