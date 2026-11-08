/**
 * Fig 1 — the notary's counter. A request ticket rides a rail past seven stations; each station is one
 * real layer of Notaire: browser, Next.js desk, security gate, business-rule rack, PostgreSQL archive,
 * audit stamp press and the instrument panel that watches it all.
 */
import { base, box, dialFace, knob, needle, post, ruler, sceneOf, tag, k } from "./parts";
import type { FigureData } from "./figure-types";

const WIDTH = 780;
const HEIGHT = 440;
const PLATE = { x: -256, y: -100, w: 548, d: 186, r: 10 };
const PLATE_H = 12;
const FOOT_H = 5;
const RAIL_Y = [56, 69];
const TICKET_REST = -202;

const P = k.fitProjection(
  [
    ...k.boxCorners(PLATE, -PLATE_H - FOOT_H, 0),
    [-210, -40, 70],
    [100, -30, 74],
    [-60, -10, 62],
    [160, -40, 56],
    [255, -40, 18],
  ],
  WIDTH,
  HEIGHT,
  { pad: 26, azimuth: 40, elevation: 30 },
);
const s = sceneOf(P);
const { S, L, D, at } = s;

/** CSS transform (in px) that moves a pre-built group by a world offset. */
const move = (dx: number, dy = 0, dz = 0) => {
  const [x, y] = k.axisVector(P, [dx, dy, dz]);
  return `translate(${x.toFixed(2)}px, ${y.toFixed(2)}px)`;
};

/** A rectangle drawn on a vertical +y face: bezels, windows and plates. */
const frontRect = (x0: number, x1: number, y: number, z0: number, z1: number) =>
  [k.segment([x0, y, z0], [x1, y, z0], P), k.segment([x1, y, z0], [x1, y, z1], P), k.segment([x1, y, z1], [x0, y, z1], P), k.segment([x0, y, z1], [x0, y, z0], P)].join("");

type Item = { depth: number; svg: string };
const items: Item[] = [];
const put = (centre: [number, number, number], svg: string) => items.push({ depth: k.depthOf(centre, P), svg });
const station = (n: number, svg: string) => `<g data-station="${n}">${svg}</g>`;

// ---- floor, rail ----------------------------------------------------------------------------
const floor = base(s, PLATE, PLATE_H, FOOT_H);
const rails = (() => {
  const out: string[] = [];
  for (const y of RAIL_Y) {
    out.push(S(k.slabOf({ x: -222, y, w: 424, d: 5, r: 1 }, 0, 3, P, 3, 0.4), { tone: "mid" }));
    out.push(D([-212, 192].map((x) => at([x, y + 2.5, 3])), { size: 0.4 }));
  }
  for (const x of [-228, 202]) {
    out.push(S(k.slabOf({ x, y: 52, w: 5, d: 26, r: 1.2 }, 0, 7, P, 3, 0.5), { tone: "mid" }));
    out.push(D([at([x + 2.5, 56, 7]), at([x + 2.5, 73, 7])], { size: 0.45 }));
  }
  return out.join("");
})();

// ---- 1  browser counter -----------------------------------------------------------------------
const counter = (() => {
  const out: string[] = [];
  out.push(box(s, { x: -244, y: -64, w: 66, d: 74, r: 2 }, 0, 26, { tone: "mid" }));
  out.push(S(k.slabOf({ x: -248, y: -68, w: 74, d: 82, r: 3 }, 26, 4, P, 4, 0.6), { tone: "mid" }));
  out.push(L(k.planOutline(k.insetPlan({ x: -248, y: -68, w: 74, d: 82, r: 3 }, 3), 30, P, 3), { tone: "faint" }));
  // monitor on a stalk
  out.push(post(s, -222, -44, 2.6, 30, 10, "mid"));
  out.push(S(k.slabOf({ x: -238, y: -48, w: 34, d: 4, r: 1.2 }, 40, 30, P, 3, 0.5), { tone: "hi" }));
  out.push(L(frontRect(-235, -207, -43.6, 43, 67), { tone: "lo" }));
  out.push(L(frontRect(-232, -210, -43.6, 46, 64), { tone: "faint" }));
  for (const z of [52, 57, 61]) out.push(L(k.segment([-229, -43.6, z], [-229 + (z === 57 ? 14 : 18), -43.6, z], P), { tone: "lo", free: true }));
  out.push(D([at([-236, -43.6, 41.5]), at([-206, -43.6, 41.5])], { size: 0.38, tone: "lo" }));
  // keyboard
  out.push(S(k.slabOf({ x: -232, y: -22, w: 38, d: 13, r: 1.5 }, 30, 2.4, P, 3, 0.3), { tone: "lo" }));
  out.push(D(k.dotGrid(-229, -19.5, 8, 3, 4.2, 32.4, P), { size: 0.32, tone: "mid" }));
  // ticket printer with a slot
  out.push(box(s, { x: -206, y: -36, w: 20, d: 22, r: 2 }, 30, 9, { tone: "mid" }));
  out.push(L(k.lineOnTop([-203, -25], [-189, -25], 39, P), { tone: "hi" }));
  out.push(knob(s, -196, -18, 2.4, 39, 2.6, "mid"));
  // cable tie dots on the front skirt
  out.push(D([at([-240, 10, 6]), at([-240, 10, 14]), at([-240, 10, 22])], { size: 0.38, tone: "lo" }));
  out.push(tag(s, -243, 6, 30, 1));
  return out.join("");
})();
put([-211, -27, 20], station(1, counter));

// ---- 2  Next.js desk -------------------------------------------------------------------------
const desk = (() => {
  const out: string[] = [];
  out.push(box(s, { x: -152, y: -62, w: 58, d: 64, r: 2 }, 0, 22, { tone: "mid" }));
  out.push(S(k.slabOf({ x: -156, y: -66, w: 66, d: 72, r: 3 }, 22, 3.4, P, 4, 0.6), { tone: "mid" }));
  out.push(L(k.planOutline(k.insetPlan({ x: -156, y: -66, w: 66, d: 72, r: 3 }, 3), 25.4, P, 3), { tone: "faint" }));
  // stack of forms: every form is one design-system layout
  [0, 1, 2, 3].forEach((i) => out.push(S(k.slabOf({ x: -146 + i * 1.4, y: -56 + i * 0.8, w: 24, d: 32, r: 0.8 }, 25.4 + i * 2, 2, P, 2, 0.2), { tone: i === 3 ? "hi" : "mid" })));
  out.push(L(k.lineOnTop([-139, -48], [-126, -48], 33.4, P), { tone: "lo", free: true }));
  out.push(L(k.lineOnTop([-139, -43], [-122, -43], 33.4, P), { tone: "lo", free: true }));
  out.push(L(k.lineOnTop([-139, -38], [-129, -38], 33.4, P), { tone: "lo", free: true }));
  // pneumatic outlet tube to the gate: a stand with a funnel mouth
  out.push(S(k.cylinder(-112, -22, 9, 25.4, 3, P, 28, 0.4), { tone: "lo" }));
  out.push(S(k.cylinder(-112, -22, 5.4, 28.4, 26, P, 26), { tone: "hi", lit: true }));
  out.push(L(k.sideArc(-112, -22, 5.4, 40, P, 14), { tone: "faint" }));
  out.push(L(k.sideArc(-112, -22, 5.4, 48, P, 14), { tone: "faint" }));
  out.push(S(k.cylinder(-112, -22, 7.6, 54.4, 2.4, P, 28, 0.3), { tone: "mid" }));
  out.push(L(k.ring(-112, -22, 4.6, 56.8, P, 20), { tone: "lo" }));
  // tray labels
  out.push(S(k.slabOf({ x: -150, y: -20, w: 22, d: 10, r: 1 }, 25.4, 4, P, 2, 0.3), { tone: "mid" }));
  out.push(L(k.planOutline(k.insetPlan({ x: -150, y: -20, w: 22, d: 10, r: 1 }, 1.4), 29.4, P, 2), { tone: "faint" }));
  out.push(ruler(s, -150, -126, -6, 25.4));
  out.push(tag(s, -154, 4, 25.4, 2));
  return out.join("");
})();
put([-124, -30, 14], station(2, desk));

// ---- 3  security gate ------------------------------------------------------------------------
const barrier = (() => {
  const out: string[] = [];
  out.push(S(k.slabOf({ x: -64, y: -48, w: 4.4, d: 58, r: 1.2 }, 17, 4, P, 2, 0.3), { tone: "hi", lit: true }));
  for (const y of [-40, -24, -8, 6]) out.push(S(k.slabOf({ x: -64.4, y, w: 5.2, d: 5, r: 0.8 }, 17, 4, P, 2), { tone: "lo", crease: "none" }));
  return out.join("");
})();
const gate = (() => {
  const out: string[] = [];
  for (const y of [-56, 12]) {
    out.push(S(k.cylinder(-62, y + 4, 8, 0, 3, P, 24), { tone: "lo" }));
    out.push(S(k.slabOf({ x: -66, y, w: 9, d: 9, r: 1.5 }, 3, 60, P, 3, 0.5), { tone: "mid" }));
    out.push(L(k.rise([-57, y + 9], 12, 58, P), { tone: "faint" }));
    out.push(D([at([-61.5, y + 4.5, 63])], { size: 0.45 }));
  }
  out.push(S(k.slabOf({ x: -68, y: -58, w: 13, d: 82, r: 2 }, 62, 9, P, 3, 0.6), { tone: "mid" }));
  out.push(L(k.planOutline(k.insetPlan({ x: -68, y: -58, w: 13, d: 82, r: 2 }, 2.2), 71, P, 3), { tone: "faint" }));
  // scanner lens and badge reader on the lintel
  out.push(S(k.cylinder(-61.5, -18, 6, 71, 3, P, 28, 0.4), { tone: "hi", lit: true }));
  out.push(L(k.ring(-61.5, -18, 3.4, 74, P, 20), { tone: "lo" }));
  out.push(D([at([-61.5, -18, 74])], { size: 0.6, tone: "hi" }));
  out.push(box(s, { x: -66, y: -4, w: 9, d: 12, r: 1.2 }, 71, 4, { tone: "mid", panel: false }));
  out.push(L(k.lineOnTop([-64, 0], [-59, 0], 75, P), { tone: "lo", free: true }));
  // key plate on the near post
  out.push(S(k.slabOf({ x: -66.2, y: 12.5, w: 9.4, d: 1.2, r: 0.4 }, 36, 14, P, 2), { tone: "lo", crease: "none" }));
  out.push(L(k.sideRing([-62, 13.8, 44], 2.4, "y", P, 16), { tone: "hi" }));
  out.push(tag(s, -66, -50, 71, 3));
  return out.join("");
})();
put([-61, -20, 30], station(3, gate));
const barrierSvg = `<g data-part="barrier">${barrier}</g>`;
put([-62, -20, 19], station(3, barrierSvg));

// ---- 4  business-rule rack -------------------------------------------------------------------
const rack = (() => {
  const out: string[] = [];
  const X = 0;
  out.push(S(k.slabOf({ x: X, y: -64, w: 3, d: 64, r: 0.6 }, 0, 46, P, 2, 0.3), { tone: "lo", crease: "none" }));
  for (const y of [-64, -3]) out.push(S(k.slabOf({ x: X, y, w: 44, d: 3, r: 0.6 }, 0, 46, P, 2, 0.3), { tone: "mid" }));
  for (const z of [0, 15, 30, 44]) out.push(S(k.slabOf({ x: X + 3, y: -61, w: 41, d: 58, r: 0.6 }, z, 2, P, 2, 0.2), { tone: "mid" }));
  for (const y of [-42, -22]) out.push(S(k.slabOf({ x: X + 3, y, w: 41, d: 2, r: 0.4 }, 2, 42, P, 2), { tone: "mid", crease: "none" }));
  // rule cards standing in the pigeonholes; the active rule is lit
  const cards: Array<[number, number, number, boolean]> = [
    [10, -56, 12, false], [24, -54, 11, false], [14, -37, 12, false], [30, -35, 11, true], [18, -17, 12, false], [32, -15, 10, false],
    [10, -56, 27, false], [26, -55, 26, false], [16, -38, 27, false], [28, -18, 26, false],
  ];
  cards.forEach(([x, y, z, lit]) => {
    out.push(S(k.slabOf({ x, y, w: 10, d: 1.4, r: 0.4 }, z + 2, 11, P, 2), { tone: lit ? "hi" : "mid", lit }));
    out.push(L(k.segment([x + 2, y + 1.4, z + 11], [x + 8, y + 1.4, z + 11], P), { tone: "lo", free: true }));
  });
  out.push(D(k.corners({ x: X, y: -64, w: 44, d: 64, r: 0 }, 3.4).map(([x, y]) => at([x, y, 46])), { size: 0.4, tone: "lo" }));
  // calculator dial on top: services own the arithmetic of a budget
  out.push(dialFace(s, 22, -32, 13, 46));
  out.push(`<path data-part="needle-rules" class="iso-line" data-tone="lit" data-free="start" d="${needle(s, 22, -32, 13, 46, 0.2)}" style="stroke-width:1"/>`);
  out.push(knob(s, 6, -10, 3.2, 46, 4, "mid"));
  out.push(tag(s, 38, -8, 46, 4));
  return out.join("");
})();
put([22, -32, 20], station(4, rack));

// ---- 5  PostgreSQL archive -------------------------------------------------------------------
const DRAWER_Z = [4, 15, 26, 37, 48];
const cabinet = (() => {
  const out: string[] = [];
  out.push(`<path class="iso-halo" d="${k.haloOf({ x: 76, y: -68, w: 58, d: 68, r: 3 }, 0, 4, P)}"/>`);
  out.push(box(s, { x: 76, y: -68, w: 56, d: 68, r: 3 }, 0, 60, { tone: "mid", steps: 4 }));
  out.push(S(k.slabOf({ x: 74, y: -70, w: 60, d: 72, r: 3 }, 60, 4, P, 4, 0.6), { tone: "mid" }));
  out.push(L(k.planOutline(k.insetPlan({ x: 74, y: -70, w: 60, d: 72, r: 3 }, 3), 64, P, 3), { tone: "faint" }));
  // flyway plate on top
  out.push(S(k.slabOf({ x: 100, y: -52, w: 26, d: 16, r: 1.2 }, 64, 1.2, P, 3), { tone: "lo", crease: "none" }));
  out.push(k.faceTextSvg(k.topMatrix([113, -42.5], 65.2, P, "x"), "FLYWAY", { size: 4.2, tone: "hi", anchor: "middle" }));
  out.push(L(k.lineOnTop([103, -38], [123, -38], 65.2, P), { tone: "lo", free: true }));
  // drawer slots: dark faint rectangles on the +x face behind the drawers
  for (const z of DRAWER_Z) out.push(L(k.segment([132, -62, z - 0.4], [132, -6, z - 0.4], P), { tone: "faint" }));
  out.push(tag(s, 82, -64, 64, 5));
  return out.join("");
})();
const drawerFront = (z: number, lit: boolean) => {
  const out: string[] = [];
  out.push(S(k.slabOf({ x: 132, y: -62, w: 3.4, d: 56, r: 1 }, z, 9, P, 3, 0.3), { tone: lit ? "hi" : "mid", lit }));
  out.push(S(k.slabOf({ x: 135.4, y: -38, w: 2.2, d: 14, r: 1 }, z + 3, 2.8, P, 2, 0.2), { tone: "hi" }));
  out.push(S(k.slabOf({ x: 135.4, y: -58, w: 0.8, d: 12, r: 0.4 }, z + 3, 3.4, P, 2), { tone: "lo", crease: "none" }));
  out.push(D([at([135.4, -26, z + 4.5]), at([135.4, -10, z + 4.5])], { size: 0.38, tone: "lo" }));
  return out.join("");
};
const drawers = DRAWER_Z.map((z, i) => (i === 2 ? "" : drawerFront(z, false))).join("");
const slidDrawer = `<g data-part="drawer">${drawerFront(DRAWER_Z[2], true)}</g>`;
const cards = (() => {
  const out: string[] = [];
  out.push(S(k.slabOf({ x: 135.4, y: -62, w: 18, d: 56, r: 1 }, 26, 2, P, 3, 0.3), { tone: "lo" }));
  for (let i = 0; i < 9; i++) out.push(S(k.slabOf({ x: 138 + (i % 2) * 7, y: -58 + i * 5.2, w: 5.4, d: 1.4, r: 0.3 }, 28, 7 + (i % 2) * 2, P, 2), { tone: i === 4 ? "hi" : "mid", lit: i === 4 }));
  out.push(S(k.slabOf({ x: 151, y: -62, w: 2, d: 56, r: 0.6 }, 28, 5, P, 2), { tone: "mid" }));
  return out.join("");
})();
put([104, -34, 30], station(5, cabinet + drawers));
put([146, -34, 28], `<g data-station="5">${slidDrawer}<g data-part="cards" style="opacity:0">${cards}</g></g>`);

// ---- 6  audit stamp press --------------------------------------------------------------------
const press = (() => {
  const out: string[] = [];
  out.push(box(s, { x: 178, y: -70, w: 46, d: 60, r: 2 }, 0, 14, { tone: "mid" }));
  out.push(S(k.slabOf({ x: 186, y: -62, w: 28, d: 34, r: 1.2 }, 14, 2.6, P, 3, 0.4), { tone: "hi" }));
  for (let i = 0; i < 5; i++) out.push(L(k.lineOnTop([190, -57 + i * 6], [210, -57 + i * 6], 16.6, P), { tone: "lo", free: true }));
  out.push(L(k.lineOnTop([200, -62], [200, -28], 16.6, P), { tone: "faint" }));
  out.push(post(s, 200, -66, 3.2, 14, 44, "mid"));
  out.push(S(k.slabOf({ x: 195.5, y: -70, w: 9, d: 30, r: 2 }, 58, 6, P, 3, 0.5), { tone: "mid" }));
  out.push(D([at([200, -67, 64]), at([200, -45, 64])], { size: 0.45 }));
  out.push(S(k.cylinder(200, -45, 2.2, 17, 41, P, 18), { tone: "hi", crease: "none" }));
  out.push(L(k.ring(200, -45, 3.6, 58, P, 14), { tone: "lo" }));
  out.push(knob(s, 218, -40, 3.6, 14, 5, "mid"));
  out.push(tag(s, 182, -66, 14, 6));
  return out.join("");
})();
const stamp = (() => {
  const out: string[] = [];
  out.push(S(k.cylinder(200, -45, 7, 41, 6, P, 28, 0.4), { tone: "hi", lit: true }));
  out.push(L(k.knurl(200, -45, 7, 41, 47, 24, P), { tone: "lo" }));
  out.push(S(k.slabOf({ x: 193, y: -52, w: 14, d: 14, r: 1.4 }, 34, 7, P, 3, 0.4), { tone: "mid" }));
  return out.join("");
})();
put([200, -40, 20], station(6, press));
put([200, -48, 40], `<g data-station="6"><g data-part="stamp">${stamp}</g></g>`);

// ---- 7  instrument panel ---------------------------------------------------------------------
const DIAL_X = 246;
const DIAL_YS = [-72, -42, -12];
const instruments = (() => {
  const out: string[] = [];
  out.push(`<path class="iso-halo" d="${k.haloOf({ x: 236, y: -92, w: 40, d: 90, r: 3 }, 0, 4, P)}"/>`);
  out.push(box(s, { x: 238, y: -90, w: 36, d: 86, r: 3 }, 0, 12, { tone: "mid" }));
  out.push(S(k.slabOf({ x: 235, y: -93, w: 42, d: 92, r: 3 }, 12, 3, P, 4, 0.6), { tone: "mid" }));
  DIAL_YS.forEach((y) => {
    out.push(S(k.cylinder(DIAL_X, y, 11.2, 15, 1.2, P, 40), { tone: "lo" }));
    out.push(dialFace(s, DIAL_X, y, 9.6, 16.2));
  });
  DIAL_YS.forEach((y, i) =>
    out.push(`<path data-part="needle-${i}" class="iso-line" data-tone="lit" data-free="start" d="${needle(s, DIAL_X, y, 9.6, 16.2, 0.25)}" style="stroke-width:1"/>`),
  );
  for (const y of [-86, -78]) out.push(S(k.cylinder(268, y, 2.6, 15, 3.6, P, 18, 0.2), { tone: y === -86 ? "hi" : "mid", lit: y === -86 }));
  out.push(D([at([268, -86, 18.6]), at([268, -78, 18.6])], { size: 0.55, tone: "hi" }));
    out.push(tag(s, 268, -8, 15, 7));
  return out.join("");
})();
put([DIAL_X, -44, 8], station(7, instruments));

// ---- the ticket -------------------------------------------------------------------------------
const ticket = (() => {
  const out: string[] = [];
  out.push(S(k.slabOf({ x: -14, y: 55, w: 28, d: 20, r: 2 }, 3, 3, P, 3, 0.4), { tone: "mid" }));
  out.push(D([at([-10, 58, 6]), at([10, 58, 6]), at([-10, 72, 6]), at([10, 72, 6])], { size: 0.38, tone: "lo" }));
  out.push(S(k.slabOf({ x: -9, y: 60, w: 18, d: 1.6, r: 0.4 }, 6, 17, P, 2), { tone: "hi", lit: true }));
  out.push(L(k.segment([-6, 61.6, 19], [6, 61.6, 19], P), { tone: "lo", free: true }));
  out.push(L(k.segment([-6, 61.6, 15], [3, 61.6, 15], P), { tone: "lo", free: true }));
  out.push(L(k.segment([-6, 61.6, 11], [5, 61.6, 11], P), { tone: "lo", free: true }));
  return out.join("");
})();

const lateral = (x: number) => move(x - 0, 0, 0);
const bodyParts = [
  floor,
  ...items.sort((a, b) => a.depth - b.depth).map((i) => i.svg),
  rails,
  `<g data-part="ticket" style="transform:${lateral(TICKET_REST)}">${ticket}</g>`,
];

const poseTicket = (x: number) => ({ ticket: { transform: lateral(x) } });
const needleSet = (a: number, b: number, c: number) => ({
  "needle-0": { d: needle(s, DIAL_X, DIAL_YS[0], 9.6, 16.2, a) },
  "needle-1": { d: needle(s, DIAL_X, DIAL_YS[1], 9.6, 16.2, b) },
  "needle-2": { d: needle(s, DIAL_X, DIAL_YS[2], 9.6, 16.2, c) },
});

const NEUTRAL = {
  barrier: { transform: move(0, 0, 0) },
  drawer: { transform: move(0, 0, 0) },
  cards: { opacity: 0 },
  stamp: { transform: move(0, 0, 0) },
  "needle-rules": { d: needle(s, 22, -32, 13, 46, 0.2) },
  ...needleSet(0.25, 0.25, 0.25),
};

export const systemFigure: FigureData = {
  id: "system",
  fig: "Fig 1",
  title: "The notary's counter",
  hint: "Pick a station",
  label:
    "An isometric machine on a base plate: a request ticket rides a rail past seven stations. A counter with a monitor is the browser, a desk with a stack of forms is the Next.js frontend, a barrier arch is the security gate, a pigeonhole rack with a calculator dial holds the business rules, a drawer cabinet is the PostgreSQL archive, a stamp press is the audit trail and a console of three dials is the monitoring stack.",
  caption:
    "One request, end to end. The ticket is a call from the browser; it never touches the database directly, only the API does, and every change leaves a stamp in the audit book.",
  width: WIDTH,
  height: HEIGHT,
  svg: k.figureSvg({
    width: WIDTH,
    height: HEIGHT,
    label:
      "A request ticket rides a rail past seven stations: browser counter, Next.js desk, security gate, business-rule rack, PostgreSQL archive, audit stamp press and monitoring console.",
    body: bodyParts,
  }),
  keys: [
    { mark: "lit", label: "The request ticket and the part doing the work" },
    { mark: "raised", label: "Seven numbered stations, one per layer" },
    { mark: "dim", label: "Dimmed while another station is explained" },
  ],
  steps: [
    {
      station: 1,
      title: "Browser: the counter",
      text: "Notary staff, receptionists and clerks work in a web browser. It shows screens and sends requests; it holds no business rules and never talks to the database.",
      where: "frontend/ (the only active client; the old Swing client was removed in #1046)",
      readout: "request leaves the browser",
      pose: { ...NEUTRAL, ...poseTicket(TICKET_REST) },
    },
    {
      station: 2,
      title: "Next.js 16 frontend: the desk",
      text: "The desk turns clicks into forms built from one design system (FormContainer, FormSection, FormField) and forwards every data call to the API under /api/v1. React 19 and TanStack Query handle loading, errors and empty states.",
      where: "frontend/src/app, frontend/src/theme, frontend/src/lib",
      readout: "form submitted to /api/v1",
      pose: { ...NEUTRAL, ...poseTicket(-120) },
    },
    {
      station: 3,
      title: "Security gate",
      text: "Spring Security checks the JWT (kept in an HttpOnly cookie), the user's role and a rate limit before any rule runs. A request without a valid token never gets past the barrier.",
      where: "backend-api security package; docs/200-architecture/206-security/",
      readout: "token valid, role allowed",
      pose: { ...NEUTRAL, barrier: { transform: move(0, 0, 34) }, ...poseTicket(-58) },
    },
    {
      station: 4,
      title: "Business rules: the rack",
      text: "Services in the Spring Boot 4 API (Java 26) apply the notarial rules: workflow transitions, budgets, payment status, deed signing. Controllers stay thin; services own transactions and throw domain exceptions.",
      where: "backend-api service, application and domain packages",
      readout: "rule applied",
      pose: { ...NEUTRAL, "needle-rules": { d: needle(s, 22, -32, 13, 46, 0.78) }, ...poseTicket(22) },
    },
    {
      station: 5,
      title: "PostgreSQL 16: the archive",
      text: "Repositories file the data in PostgreSQL. Only the backend connects to it, and Flyway migrations are the single source of truth for the schema, so the archive can be rebuilt from scratch.",
      where: "backend-api/src/main/resources/db/migration; docs/200-architecture/205-data-model",
      readout: "record filed in a drawer",
      pose: { ...NEUTRAL, drawer: { transform: move(18, 0, 0) }, cards: { opacity: 1, transform: move(18, 0, 0) }, ...poseTicket(104) },
    },
    {
      station: 6,
      title: "Audit stamp",
      text: "An aspect records every create, update, delete and login with the user taken from the security context, never from a client header. Reading personal data is audited too. The stamp lands in the ledger on every change.",
      where: "backend-api audit package",
      readout: "audit entry written",
      pose: { ...NEUTRAL, stamp: { transform: move(0, 0, -17.4) }, ...poseTicket(160) },
    },
    {
      station: 7,
      title: "Instrument console",
      text: "Micrometer metrics go to Prometheus, structured JSON logs to Loki, traces through an OpenTelemetry collector to Tempo, and Grafana draws the dashboards. Operators read the dials; they do not touch the machine.",
      where: "infra/observability; docs/200-architecture/207-monitoring/",
      readout: "dials: metrics, logs, traces",
      pose: { ...NEUTRAL, ...needleSet(0.62, 0.48, 0.7), ...poseTicket(160) },
    },
  ],
};
