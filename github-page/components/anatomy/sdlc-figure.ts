/**
 * Fig 3 — the lock chamber. A change is a boat that must pass five lock gates on its way from an issue to
 * a running, verified release. A gate opens only when its condition is proved (the water has risen to
 * its level). The control tower holds the three roles; the lectern holds the Constitution every agent reads.
 */
import { base, box, knob, ruler, sceneOf, tag, k } from "./parts";
import type { FigureData } from "./figure-types";

const WIDTH = 780;
const HEIGHT = 440;
const PLATE = { x: -262, y: -112, w: 548, d: 222, r: 10 };
const PLATE_H = 12;
const FOOT_H = 5;
const GATES = [-190, -105, -20, 65, 150];
const LEVEL = (chamber: number) => 4 + chamber * 2;
const CHAMBER_X: Array<[number, number]> = [-250, ...GATES.map((g) => g + 4)].map((from, i) => [from, i < GATES.length ? GATES[i] - 4 : 248]);
const BOAT_Z = (chamber: number) => LEVEL(chamber);
const BOAT_X = (chamber: number) => (CHAMBER_X[chamber][0] + CHAMBER_X[chamber][1]) / 2;

const P = k.fitProjection(
  [...k.boxCorners(PLATE, -PLATE_H - FOOT_H, 0), [-60, 70, 66], [209, -86, 62], [-190, 0, 95], [150, 0, 95]],
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

// ---- floor, canal -----------------------------------------------------------------------------
const floor = base(s, PLATE, PLATE_H, FOOT_H);
const canal = (() => {
  const out: string[] = [];
  out.push(S(k.slabOf({ x: -256, y: -40, w: 536, d: 8, r: 2 }, 0, 24, P, 3, 0.5), { tone: "mid" }));
  out.push(S(k.slabOf({ x: -256, y: 32, w: 536, d: 8, r: 2 }, 0, 24, P, 3, 0.5), { tone: "mid" }));
  out.push(S(k.slabOf({ x: -256, y: -32, w: 536, d: 64, r: 0 }, 0, 2, P, 2), { tone: "lo", crease: "none" }));
  for (let x = -244; x < 270; x += 18) out.push(D([at([x, -36, 24]), at([x, 36, 24])], { size: 0.38, tone: "lo" }));
  return out.join("");
})();
const waters = CHAMBER_X.map(([x0, x1], i) => {
  const out: string[] = [];
  out.push(S(k.slabOf({ x: x0, y: -31, w: x1 - x0, d: 62, r: 0 }, 2, LEVEL(i) - 2, P, 2), { tone: "lo", crease: "faint" }));
  for (const y of [-18, 0, 18]) out.push(L(k.lineOnTop([x0 + 6, y], [x1 - 6, y], LEVEL(i), P), { tone: "faint", free: true }));
  return out.join("");
});

// ---- the five gates -----------------------------------------------------------------------------
const gateFrame = (gx: number, n: number) => {
  const out: string[] = [];
  for (const y of [-40, 33]) {
    out.push(S(k.cylinder(gx + 0.5, y + 3.5, 7, 24, 2, P, 22), { tone: "lo" }));
    out.push(S(k.slabOf({ x: gx - 3, y, w: 7, d: 7, r: 1.4 }, 26, 34, P, 3, 0.5), { tone: "mid" }));
    out.push(D([at([gx + 0.5, y + 3.5, 60])], { size: 0.42 }));
  }
  out.push(S(k.slabOf({ x: gx - 4.5, y: -42, w: 10, d: 87, r: 2 }, 60, 7, P, 3, 0.5), { tone: "mid" }));
  out.push(L(k.planOutline(k.insetPlan({ x: gx - 4.5, y: -42, w: 10, d: 87, r: 2 }, 2), 67, P, 3), { tone: "faint" }));
  out.push(S(k.cylinder(gx + 0.5, 0, 4, 67, 3, P, 22, 0.3), { tone: "hi", lit: true }));
  out.push(tag(s, gx + 0.5, -30, 67, n));
  return out.join("");
};
const gatePlate = (gx: number, i: number) =>
  `<g data-part="gate-${i}" style="transition:none">${S(k.slabOf({ x: gx - 1.5, y: -31, w: 3, d: 62, r: 0.6 }, 2, 46, P, 2, 0.3), { tone: "hi", lit: true })}${[ -20, -8, 4, 16 ].map((y) => L(k.segment([gx + 1.5, y, 8], [gx + 1.5, y, 42], P), { tone: "lo", free: true })).join("")}</g>`;

// ---- the boat (the change) ---------------------------------------------------------------------
const boat = (() => {
  const out: string[] = [];
  out.push(S(k.slabOf({ x: -16, y: -9, w: 38, d: 18, r: 8 }, 0, 6, P, 8, 0.4), { tone: "hi", lit: true }));
  out.push(L(k.planOutline(k.insetPlan({ x: -16, y: -9, w: 38, d: 18, r: 8 }, 2.4), 6, P, 8), { tone: "lo" }));
  out.push(S(k.slabOf({ x: -8, y: -5, w: 14, d: 10, r: 1.6 }, 6, 8, P, 3, 0.4), { tone: "mid" }));
  out.push(D([at([-5, -2, 14]), at([3, -2, 14]), at([-5, 2, 14]), at([3, 2, 14])], { size: 0.38, tone: "lo" }));
  out.push(S(k.cylinder(14, 0, 1.2, 6, 16, P, 12), { tone: "mid", crease: "none" }));
  out.push(S(k.slabOf({ x: 14, y: -5, w: 1, d: 10, r: 0.4 }, 14, 7, P, 2), { tone: "lo", crease: "none" }));
  out.push(L(k.segment([15, -3, 19], [15, 3, 19], P), { tone: "hi", free: true }));
  return out.join("");
})();

// ---- 6  control tower: Owner, Foreman, Worker ----------------------------------------------------
const tower = (() => {
  const out: string[] = [];
  out.push(`<path class="iso-halo" d="${k.haloOf({ x: -88, y: 52, w: 64, d: 40, r: 3 }, 0, 4, P)}"/>`);
  for (const [x, y] of [[-84, 56], [-34, 56], [-84, 86], [-34, 86]] as Array<[number, number]>) {
    out.push(S(k.cylinder(x + 2, y + 2, 5, 0, 2, P, 18), { tone: "lo" }));
    out.push(S(k.slabOf({ x, y, w: 4, d: 4, r: 0.8 }, 2, 30, P, 2, 0.2), { tone: "mid" }));
  }
  out.push(S(k.slabOf({ x: -90, y: 52, w: 62, d: 40, r: 3 }, 32, 3, P, 4, 0.5), { tone: "mid" }));
  out.push(box(s, { x: -84, y: 58, w: 50, d: 28, r: 2 }, 35, 18, { tone: "mid" }));
  out.push(S(k.slabOf({ x: -90, y: 52, w: 62, d: 40, r: 3 }, 53, 3, P, 4, 0.5), { tone: "hi" }));
  out.push(L(k.segment([-34, 60, 40], [-34, 84, 40], P), { tone: "faint" }));
  out.push(L(k.segment([-34, 60, 48], [-34, 84, 48], P), { tone: "faint" }));
  // three levers on the console: Owner, Foreman, Worker (heights rise with authority over the harness)
  [[-30, 62, 8], [-30, 72, 12], [-30, 82, 16]].forEach(([x, y, h]) => {
    out.push(S(k.cylinder(x, y, 2.6, 35, 1.6, P, 16), { tone: "lo" }));
    out.push(S(k.cylinder(x, y, 1.1, 36.6, h, P, 10), { tone: "hi", crease: "none" }));
    out.push(S(k.cylinder(x, y, 2.4, 36.6 + h, 2.6, P, 14, 0.2), { tone: "mid" }));
  });
  // ladder
  for (let z = 6; z < 30; z += 6) out.push(L(k.segment([-86, 56, z], [-86, 60, z], P), { tone: "mid" }));
  out.push(tag(s, -62, 90, 53, 6));
  return out.join("");
})();
const lit = `<g data-part="tower-lit" style="opacity:0">${S(k.cylinder(-30, 72, 2.4, 48.6, 2.6, P, 14, 0.2), { tone: "hi", lit: true })}</g>`;
put([-60, 72, 20], station(6, tower));
put([-30, 72, 48], station(6, lit));

// ---- 7  the Constitution lectern -------------------------------------------------------------------
const lectern = (() => {
  const out: string[] = [];
  out.push(`<path class="iso-halo" d="${k.haloOf({ x: 100, y: -96, w: 56, d: 44, r: 3 }, 0, 4, P)}"/>`);
  out.push(S(k.slabOf({ x: 104, y: -92, w: 48, d: 36, r: 3 }, 0, 4, P, 4, 0.5), { tone: "mid" }));
  out.push(S(k.slabOf({ x: 116, y: -82, w: 24, d: 16, r: 2 }, 4, 24, P, 3, 0.5), { tone: "mid" }));
  out.push(S(k.slabOf({ x: 108, y: -90, w: 40, d: 32, r: 2 }, 28, 3, P, 3, 0.5), { tone: "hi" }));
  out.push(S(k.slabOf({ x: 110, y: -88, w: 36, d: 28, r: 1.6 }, 31, 1.4, P, 3, 0.3), { tone: "mid" }));
  // an open book: two pages with rule lines, the Constitution
  out.push(S(k.slabOf({ x: 113, y: -85, w: 14, d: 22, r: 1 }, 32.4, 1.6, P, 2, 0.2), { tone: "hi", lit: true }));
  out.push(S(k.slabOf({ x: 129, y: -85, w: 14, d: 22, r: 1 }, 32.4, 1.6, P, 2, 0.2), { tone: "hi", lit: true }));
  [0, 1, 2, 3, 4].forEach((i) => {
    out.push(L(k.lineOnTop([115, -82 + i * 4], [125, -82 + i * 4], 34, P), { tone: "lo", free: true }));
    out.push(L(k.lineOnTop([131, -82 + i * 4], [141, -82 + i * 4], 34, P), { tone: "lo", free: true }));
  });
  out.push(D(k.corners({ x: 104, y: -92, w: 48, d: 36, r: 0 }, 4).map(([x, y]) => at([x, y, 4])), { size: 0.4, tone: "lo" }));
  out.push(knob(s, 146, -60, 2.8, 0, 4, "mid"));
  out.push(ruler(s, 108, 140, -62, 0));
  out.push(tag(s, 108, -57, 0.6, 7));
  return out.join("");
})();
const [LX, LY] = k.axisVector(P, [84, -8, 0]);
put([212, -82, 18], station(7, `<g transform="translate(${LX.toFixed(2)} ${LY.toFixed(2)})">${lectern}</g>`));

// ---- the intake hopper at the head of the canal ------------------------------------------------------
const hopper = (() => {
  const out: string[] = [];
  out.push(S(k.cylinder(-244, -52, 11, 0, 4, P, 28, 0.4), { tone: "lo" }));
  out.push(S(k.cylinder(-244, -52, 8, 4, 22, P, 28, 0.4), { tone: "mid" }));
  out.push(S(k.cylinder(-244, -52, 11, 26, 3, P, 28, 0.4), { tone: "mid" }));
  out.push(L(k.ring(-244, -52, 7.4, 29, P, 24), { tone: "lo" }));
  out.push(L(k.sideArc(-244, -52, 8, 14, P, 14), { tone: "faint" }));
  return out.join("");
})();
put([-244, -52, 14], hopper);

// ---- assembly ------------------------------------------------------------------------------------------
GATES.forEach((gx, i) => {
  put([gx, 0, 30], station(i + 1, gateFrame(gx, i + 1)));
  put([gx, 0, 28], station(i + 1, gatePlate(gx, i + 1)));
});
const parts = [
  floor,
  canal,
  ...waters.map((w, i) => ({ depth: k.depthOf([BOAT_X(i), 0, 2], P), svg: w })).sort((a, b) => a.depth - b.depth).map((w) => w.svg),
  ...items.sort((a, b) => a.depth - b.depth).map((i) => i.svg),
];

const closedGates = Object.fromEntries(GATES.map((_, i) => [`gate-${i + 1}`, { transform: move(0, 0, 0) }]));
const openTo = (n: number) =>
  Object.fromEntries(GATES.map((_, i) => [`gate-${i + 1}`, { transform: move(0, 0, i < n ? 44 : 0) }]));
const boatPose = (chamber: number) => ({ boat: { transform: move(BOAT_X(chamber), 0, BOAT_Z(chamber)) } });

export const sdlcFigure: FigureData = {
  id: "ai-sdlc",
  fig: "Fig 3",
  title: "The lock chamber",
  hint: "Pick a gate",
  label:
    "An isometric canal with five lock gates across it, each in a frame with a numbered disc. A boat, which is the change, is raised from chamber to chamber and passes a gate only when it is open. On the bank stand a control tower on stilts with three levers, the three roles, and a lectern with an open book, the Constitution.",
  caption:
    "The AI SDLC is a lock, not a ladder: a gate cannot be skipped, because the water on the far side only rises when the evidence is in. Humans and agents use the same locks.",
  width: WIDTH,
  height: HEIGHT,
  svg: k.figureSvg({
    width: WIDTH,
    height: HEIGHT,
    label:
      "A canal with five lock gates, a boat that is raised through them, a control tower with three levers and a lectern with an open book.",
    body: [...parts, `<g data-part="boat" style="transform:${move(BOAT_X(0), 0, BOAT_Z(0))}">${boat}</g>`],
  }),
  keys: [
    { mark: "lit", label: "The change (boat) and the gate it must pass" },
    { mark: "raised", label: "Five gates, a control tower for the roles, a lectern for the rules" },
    { mark: "dim", label: "Dimmed while another part is explained" },
  ],
  steps: [
    {
      station: 1,
      title: "Gate 1: specification",
      text: "Nothing is built from a prompt alone. The change needs a GitHub issue tied to a use case, then an OpenSpec change (proposal, traceability, specs, design, tasks) that validates. Until it does, gate 1 stays shut.",
      where: "docs/openspec/, workspace/sdlc/validate-sdlc-plan.sh",
      readout: "gate 1 open: spec valid",
      pose: { ...closedGates, ...openTo(1), ...boatPose(1), "tower-lit": { opacity: 0 } },
    },
    {
      station: 2,
      title: "Gate 2: tests first",
      text: "Failing tests are written and seen to fail before any production code: unit, integration, API and, for UI, Playwright. The agent proves red, then green, and may not weaken a test to pass.",
      where: "CONSTITUTION.md section 6 (Gate 2), .claude/rules/ai-agent-workflow.md",
      readout: "gate 2 open: red, then green",
      pose: { ...closedGates, ...openTo(2), ...boatPose(2), "tower-lit": { opacity: 0 } },
    },
    {
      station: 3,
      title: "Gate 3: docs and local pipeline",
      text: "Before a pull request: permanent documentation updated, coverage above the ratchet floor, lint and format clean, the local pipeline and the local security cases passing, and the OpenSpec change archived in the same PR.",
      where: "workspace/sdlc/run_pipeline.sh, preflight.sh, security/run_gates.py",
      readout: "gate 3 open: pipeline exit 0",
      pose: { ...closedGates, ...openTo(3), ...boatPose(3), "tower-lit": { opacity: 0 } },
    },
    {
      station: 4,
      title: "Gate 4: review and merge",
      text: "A change does not merge with failing checks, unresolved review, merge conflicts or pending docs. GitHub does not re-run the product suite on a pull request, so preflight --full proves it on the agent machine, and check-heavy-ci.sh fails only if a heavy run started on the PR and is red or pending. Workers never merge; the Foreman merges Worker PRs under its rule.",
      where: "docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md, workspace/sdlc/check-heavy-ci.sh",
      readout: "gate 4 open: review clean, checks green",
      pose: { ...closedGates, ...openTo(4), ...boatPose(4), "tower-lit": { opacity: 0 } },
    },
    {
      station: 5,
      title: "Gate 5: deploy, smoke, close",
      text: "The issue is not closed when the PR opens. It closes after the deploy succeeds and a smoke test passes on the target environment, and the board status and labels agree with the issue state.",
      where: ".github/workflows/cd.yml, CONSTITUTION.md section 6 (Gate 5)",
      readout: "gate 5 open: released and verified",
      pose: { ...closedGates, ...openTo(5), ...boatPose(5), "tower-lit": { opacity: 0 } },
    },
    {
      station: 6,
      title: "Three roles: the control tower",
      text: "The Owner is the human code owner: approves specifications, merges the Constitution and harness changes, grants exceptions. The Foreman is a supervising agent that runs the gates and merges Worker PRs by rule. The Worker is the implementing agent and never merges or pushes to main.",
      where: "CONSTITUTION.md section 10, .claude/agents/ (cloud-foreman.md and specialists)",
      readout: "who may pull which lever",
      pose: { ...closedGates, ...openTo(5), ...boatPose(5), "tower-lit": { opacity: 1 } },
    },
    {
      station: 7,
      title: "The Constitution: the rulebook",
      text: "One document, CONSTITUTION.md, defines the workflow, the five gates and the exceptions. A short digest and the OpenSpec schema carry it to every agent, so Claude Code, Copilot, OpenCode and the cloud fleet follow the same rules without agent-specific files.",
      where: "CONSTITUTION.md, CONSTITUTION-AGENT-CARD.md, AGENTS.md, .claude/skills/",
      readout: "same rules for every agent",
      pose: { ...closedGates, ...boatPose(0), "tower-lit": { opacity: 0 } },
    },
  ],
};
