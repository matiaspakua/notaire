/**
 * The artifact cabinet (#2067): an index-card catalog with nine drawers, one per stage of the
 * engineering artifact chain. Pure geometry: every path is built once, on the server, with the
 * anatomy kit. A drawer is drawn at rest (closed) and slides out along +y with a translate; a
 * clip shaped like its opening swept toward the viewer hides the part still inside the carcass.
 *
 * The card stack in each drawer is `cardsFor(count)` cards deep: three cards per doubling of
 * the file count (3·log2(count + 1)), so a drawer of 7 files and one of 383 both read.
 */
import * as k from "@/components/iso/iso-kit";
import type { Plan, Point3, Projection } from "@/components/iso/iso-kit";

export const WIDTH = 640;
export const HEIGHT = 470;

const CARCASS: Plan = { x: -78, y: -36, w: 156, d: 72, r: 1.5 };
const FACE = CARCASS.y + CARCASS.d; // the front plane, y = 36
const Z0 = 13;
const Z1 = 126;
const PLINTH: Plan = { x: -82, y: -40, w: 164, d: 80, r: 2 };
const TOP: Plan = { x: -81.5, y: -39.5, w: 163, d: 79, r: 2.5 };
const TOP_Z = Z1;
const TOP_H = 5;

const COLS = [-71, -22, 27];
const ROWS = [91, 54.5, 18]; // top row first, so stage 1 is top left
const DW = 44;
const DH = 31;
const FRONT_D = 2.6;
const BODY_L = 64;
const SPACING = 1.9;
const PULL_MAX = 58;

export const DRAWERS = ROWS.flatMap((z, row) => COLS.map((x, col) => ({ x, z, index: row * 3 + col })));

export function cardsFor(count: number | null): number {
  if (count === null || count <= 0) return 0;
  return Math.max(3, Math.min(26, Math.round(3 * Math.log2(count + 1))));
}

/** How far a drawer opens: enough to show its whole stack, never past its back wall. */
export function pullFor(cards: number): number {
  return Math.min(PULL_MAX, 9 + cards * SPACING + 5);
}

export const P: Projection = k.fitProjection(
  [
    ...k.boxCorners(PLINTH, -4, TOP_Z + TOP_H),
    [COLS[0], FACE + FRONT_D + PULL_MAX, ROWS[2]],
    [COLS[2] + DW, FACE + FRONT_D + PULL_MAX, ROWS[0] + DH],
  ],
  WIDTH,
  HEIGHT,
  { pad: 26, azimuth: 58 },
);

const at = (point: Point3) => k.iso(point, P);
const S = (paths: k.SolidPaths, style: Parameters<typeof k.solidSvg>[1] = {}) => k.solidSvg(paths, style);
const L = (d: string, style: Parameters<typeof k.lineSvg>[1] = {}) => k.lineSvg(d, style);
const D = (points: Array<k.Flat | [number, number, number]>, style: Parameters<typeof k.dotsSvg>[1] = {}) => k.dotsSvg(points, style);
const byDepth = (points: k.Plane[]) => [...points].sort((a, b) => k.depthOf([a[0], a[1], 0], P) - k.depthOf([b[0], b[1], 0], P));

/** A closed rectangle on the plane y = `y`. */
function faceRect(x0: number, x1: number, z0: number, z1: number, y: number): string {
  return k.pathOf([at([x0, y, z0]), at([x1, y, z0]), at([x1, y, z1]), at([x0, y, z1])], true);
}

/** A closed rectangle on the plane x = `x`. */
function sideRect(y0: number, y1: number, z0: number, z1: number, x: number): string {
  return k.pathOf([at([x, y0, z0]), at([x, y1, z0]), at([x, y1, z1]), at([x, y0, z1])], true);
}

function plinth(): string {
  const out = [`<path class="iso-halo" d="${k.haloOf(PLINTH, -4, 4, P)}"/>`];
  for (const [x, y] of byDepth(k.corners(PLINTH, 12))) {
    out.push(S(k.cylinder(x, y, 6.4, -4, 1.4, P, 28), { tone: "lo" }));
    out.push(S(k.cylinder(x, y, 4.8, -2.6, 6.6, P, 28), { tone: "mid" }));
  }
  out.push(S(k.slabOf(PLINTH, 4, Z0 - 4, P, 6, 1.2), { tone: "mid", inner: L(k.sideSeam(PLINTH, 7, P, 6), { tone: "faint" }) }));
  // A ruler strip along the plinth front: one major tick per drawer column.
  const strip: Plan = { x: -70, y: PLINTH.y + PLINTH.d - 2.6, w: 140, d: 2.2, r: 0.6 };
  out.push(S(k.slabOf(strip, Z0, 0.5, P, 3), { tone: "lo", crease: "none" }));
  const ticks = k.topTicks(strip.x + 2, strip.x + strip.w - 2, 3.4, 5, strip.y + 0.3, Z0 + 0.5, [0.7, 1.4], P);
  out.push(L(ticks.minor, { tone: "lo" }), L(ticks.major, { tone: "mid" }));
  return out.join("");
}

function carcass(): string {
  const out: string[] = [];
  out.push(S(k.slabOf(CARCASS, Z0, Z1 - Z0, P, 4, 0), { tone: "mid" }));
  // Raised side panel on the +x face: two nested frames.
  const sx = CARCASS.x + CARCASS.w;
  out.push(L(sideRect(CARCASS.y + 8, FACE - 8, Z0 + 9, Z1 - 9, sx), { tone: "lo" }));
  out.push(L(sideRect(CARCASS.y + 11, FACE - 11, Z0 + 12, Z1 - 12, sx), { tone: "faint" }));
  out.push(D([at([sx, CARCASS.y + 4, Z0 + 5]), at([sx, FACE - 4, Z0 + 5]), at([sx, CARCASS.y + 4, Z1 - 5]), at([sx, FACE - 4, Z1 - 5])], { size: 0.45, tone: "lo" }));
  // Pilasters at the front corners, proud of the face.
  for (const x of [CARCASS.x, CARCASS.x + CARCASS.w - 5]) {
    out.push(S(k.slabOf({ x, y: FACE, w: 5, d: 1.6, r: 0.4 }, Z0, Z1 - Z0, P, 2, 0.4), { tone: "mid", crease: "faint" }));
  }
  // Drawer openings: a dark recess behind every drawer front, seen only when one is out.
  for (const d of DRAWERS) {
    out.push(`<path class="cab-hole" d="${faceRect(d.x, d.x + DW, d.z, d.z + DH, FACE)}"/>`);
    out.push(L(faceRect(d.x, d.x + DW, d.z, d.z + DH, FACE), { tone: "lo" }));
  }
  // Rails between the rows and stiles between the columns, as seams on the face.
  for (const z of [ROWS[0] - 2.75, ROWS[1] - 2.75]) out.push(L(k.segment([CARCASS.x + 5, FACE, z], [CARCASS.x + CARCASS.w - 5, FACE, z], P), { tone: "faint" }));
  for (const x of [COLS[1] - 2.5, COLS[2] - 2.5]) out.push(L(k.segment([x, FACE, Z0], [x, FACE, Z1], P), { tone: "faint" }));
  // The reading shelf under the bottom row, pulled out a little, with its knob.
  const shelf: Plan = { x: -58, y: FACE, w: 116, d: 11, r: 0.8 };
  out.push(S(k.slabOf(shelf, Z0 + 0.6, 2.6, P, 3, 0.5), { tone: "mid" }));
  out.push(L(k.lineOnTop([shelf.x + 3, shelf.y + shelf.d - 3], [shelf.x + shelf.w - 3, shelf.y + shelf.d - 3], Z0 + 3.2, P), { tone: "faint" }));
  out.push(S(k.slabOf({ x: -5, y: shelf.y + shelf.d, w: 10, d: 1.8, r: 0.9 }, Z0 + 0.9, 2, P, 3, 0.3), { tone: "mid" }));
  return out.join("");
}

function top(): string {
  const out: string[] = [];
  out.push(S(k.slabOf(TOP, TOP_Z, TOP_H, P, 6, 1.2), { tone: "mid", inner: L(k.sideSeam(TOP, TOP_Z + 2, P, 6), { tone: "faint" }) }));
  out.push(L(k.planOutline(k.insetPlan(TOP, 5), TOP_Z + TOP_H, P, 6), { tone: "faint" }));
  // Brass label plate with three rule lines for the engraved title, and two screws.
  const plate: Plan = { x: -26, y: 14, w: 52, d: 14, r: 1 };
  const z = TOP_Z + TOP_H;
  out.push(S(k.slabOf(plate, z, 0.8, P, 4), { tone: "lo", crease: "none" }));
  out.push(L(k.planOutline(k.insetPlan(plate, 1.4), z + 0.8, P, 4), { tone: "faint" }));
  out.push(L(k.lineOnTop([plate.x + 6, plate.y + 4.5], [plate.x + 40, plate.y + 4.5], z + 0.8, P), { tone: "lo", free: true }));
  out.push(L(k.lineOnTop([plate.x + 6, plate.y + 7.2], [plate.x + 46, plate.y + 7.2], z + 0.8, P), { tone: "lo", free: true }));
  out.push(L(k.lineOnTop([plate.x + 6, plate.y + 9.9], [plate.x + 30, plate.y + 9.9], z + 0.8, P), { tone: "lo", free: true }));
  out.push(D([at([plate.x + 2.6, plate.y + plate.d / 2, z + 0.8]), at([plate.x + plate.w - 2.6, plate.y + plate.d / 2, z + 0.8])], { size: 0.45 }));
  // An index-card tray on the top: the one card being read.
  const tray: Plan = { x: 30, y: -24, w: 30, d: 22, r: 1 };
  out.push(S(k.slabOf(tray, z, 4, P, 4, 0.5), { tone: "mid" }));
  out.push(S(k.slabOf(k.insetPlan(tray, 1.6), z + 1, 3.2, P, 4), { tone: "lo", crease: "none" }));
  for (let i = 0; i < 5; i += 1) {
    out.push(S(k.slabOf({ x: tray.x + 3, y: tray.y + 4 + i * 3, w: 24, d: 0.5, r: 0 }, z + 1.2, 6, P, 2), { tone: "hi", crease: "faint" }));
  }
  return out.join("");
}

export type DrawerArt = {
  index: number;
  /** Moving art: body, cards and front, at rest. */
  art: string;
  /** Static clip: the opening swept toward the viewer. */
  clip: string;
  cards: number;
  pull: number;
};

function drawer(d: { x: number; z: number; index: number }, cards: number): DrawerArt {
  const { x: x0, z: z0 } = d;
  const back = FACE - BODY_L;
  const out: string[] = [];
  const wallH = 13;
  out.push(S(k.slabOf({ x: x0 + 1.5, y: back, w: DW - 3, d: BODY_L, r: 0.4 }, z0 + 1.4, 1.2, P, 2), { tone: "lo" }));
  out.push(S(k.slabOf({ x: x0 + 1.5, y: back, w: DW - 3, d: 1.2, r: 0 }, z0 + 2.6, wallH, P, 2), { tone: "mid" }));
  out.push(S(k.slabOf({ x: x0 + 1.5, y: back, w: 1.2, d: BODY_L, r: 0 }, z0 + 2.6, wallH, P, 2), { tone: "mid" }));
  // The catalog rod the cards hang on: the traceability that keeps them in order.
  out.push(L(k.segment([x0 + DW / 2, back + 1.2, z0 + 5.2], [x0 + DW / 2, FACE, z0 + 5.2], P), { tone: "mid" }));
  // Follower block behind the last card.
  const last = FACE - 4 - (Math.max(cards, 1) - 1) * SPACING - 2.6;
  out.push(S(k.slabOf({ x: x0 + 4, y: last - 2.4, w: DW - 8, d: 2.4, r: 0.4 }, z0 + 2.6, 15, P, 2, 0.3), { tone: "mid" }));
  for (let i = cards - 1; i >= 0; i -= 1) {
    const y = FACE - 4 - i * SPACING;
    // The front card is lifted on the rod: it is the one being read, and the only lit part.
    const lit = i === 0;
    const lift = lit ? 5 : 0;
    out.push(S(k.slabOf({ x: x0 + 4, y: y - 0.5, w: DW - 8, d: 0.5, r: 0 }, z0 + 2.8 + lift, 19.2, P, 2), { tone: "hi", crease: "faint", lit }));
    const tab = (i % 3) * 11.5;
    out.push(S(k.slabOf({ x: x0 + 5 + tab, y: y - 0.5, w: 10, d: 0.5, r: 0 }, z0 + 22 + lift, 2.4, P, 2), { tone: "hi", crease: "faint", lit }));
    if (lit) out.push(L(k.segment([x0 + 8, y, z0 + 16 + lift], [x0 + DW - 8, y, z0 + 16 + lift], P), { tone: "lo", free: true }));
  }
  out.push(S(k.slabOf({ x: x0 + DW - 2.7, y: back, w: 1.2, d: BODY_L, r: 0 }, z0 + 2.6, wallH, P, 2), { tone: "mid" }));
  // Front panel with its label holder, cup pull and rod knob.
  const fy = FACE + FRONT_D;
  out.push(S(k.slabOf({ x: x0, y: FACE, w: DW, d: FRONT_D, r: 0.8 }, z0, DH, P, 3, 0.6), { tone: "mid" }));
  out.push(L(faceRect(x0 + 2.4, x0 + DW - 2.4, z0 + 2.4, z0 + DH - 2.4, fy), { tone: "faint" }));
  out.push(S(k.slabOf({ x: x0 + 12, y: fy, w: 20, d: 0.6, r: 0.3 }, z0 + 18, 9.4, P, 2), { tone: "lo", crease: "none" }));
  out.push(L(faceRect(x0 + 13.4, x0 + 30.6, z0 + 19.2, z0 + 26.2, fy + 0.6), { tone: "faint" }));
  out.push(L(k.segment([x0 + 15.4, fy + 0.6, z0 + 23.6], [x0 + 27.4, fy + 0.6, z0 + 23.6], P), { tone: "lo", free: true }));
  out.push(L(k.segment([x0 + 15.4, fy + 0.6, z0 + 21.4], [x0 + 23.6, fy + 0.6, z0 + 21.4], P), { tone: "lo", free: true }));
  out.push(D([at([x0 + 12.9, fy + 0.6, z0 + 22.7]), at([x0 + 31.1, fy + 0.6, z0 + 22.7])], { size: 0.4 }));
  out.push(S(k.slabOf({ x: x0 + 15, y: fy, w: 14, d: 2.6, r: 1.2 }, z0 + 10.5, 3.6, P, 3, 0.4), { tone: "mid" }));
  out.push(S(k.slabOf({ x: x0 + DW / 2 - 1.6, y: fy, w: 3.2, d: 1.6, r: 1.5 }, z0 + 3.6, 3.2, P, 4), { tone: "mid" }));
  out.push(D([at([x0 + DW / 2, fy + 1.6, z0 + 5.2])], { size: 0.45, tone: "hi" }));

  const pad = 0.8;
  const far = FACE + PULL_MAX + 12;
  const pts = [
    // No padding at the face plane: anything inside the carcass that projects past the opening stays hidden.
    at([x0, FACE, z0]), at([x0 + DW, FACE, z0]), at([x0 + DW, FACE, z0 + DH]), at([x0, FACE, z0 + DH]),
    at([x0 - pad, far, z0 - pad]), at([x0 + DW + pad, far, z0 - pad]), at([x0 + DW + pad, far, z0 + DH + pad]), at([x0 - pad, far, z0 + DH + pad]),
  ];
  return { index: d.index, art: out.join(""), clip: k.pathOf(hull(pts), true), cards, pull: pullFor(cards) };
}

function hull(points: k.Flat[]): k.Flat[] {
  const p = [...points].sort((a, b) => a[0] - b[0] || a[1] - b[1]);
  const cross = (o: k.Flat, a: k.Flat, b: k.Flat) => (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0]);
  const lower: k.Flat[] = [];
  for (const q of p) {
    while (lower.length >= 2 && cross(lower[lower.length - 2], lower[lower.length - 1], q) <= 0) lower.pop();
    lower.push(q);
  }
  const upper: k.Flat[] = [];
  for (const q of [...p].reverse()) {
    while (upper.length >= 2 && cross(upper[upper.length - 2], upper[upper.length - 1], q) <= 0) upper.pop();
    upper.push(q);
  }
  return [...lower.slice(0, -1), ...upper.slice(0, -1)];
}

/** Screen offset of one unit of pull (+y). */
export const PULL_VECTOR = k.axisVector(P, [0, 1, 0]);

export function cabinetArt(counts: (number | null)[]): { base: string; drawers: DrawerArt[] } {
  return {
    base: plinth() + carcass() + top(),
    drawers: DRAWERS.map((d) => drawer(d, cardsFor(counts[d.index] ?? null))),
  };
}
