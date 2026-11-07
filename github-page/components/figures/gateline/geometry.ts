/**
 * The gated line (#2067): the AI SDLC drawn as a test bench. A carriage carrying the Issue card
 * runs along a ruler of the Constitution's 24 steps; five barrier booms stand at the steps that
 * carry a hard gate. A boom lifts only once the carriage has reached its step, and the carriage
 * cannot pass a boom that is down. Steps and gates come from LIFECYCLE_PHASES, the same data the
 * lifecycle page lists, so the figure and the text cannot disagree.
 */
import * as k from "@/components/iso/iso-kit";
import type { Plan, Point3, Projection } from "@/components/iso/iso-kit";
import { LIFECYCLE_PHASES } from "@/lib/ai-sdlc-content";
import { BOOM_LEN, BOOM_T, BOOM_Z, PIVOT_Y } from "./boom";

export const WIDTH = 680;
export const HEIGHT = 380;

const STEPS = 24;
const X0 = -184;
const X1 = 184;
export const SPACING = (X1 - X0) / (STEPS - 1);
export const stepX = (step: number) => X0 + (step - 1) * SPACING;

const PLATE: Plan = { x: -206, y: -44, w: 412, d: 88, r: 6 };
const PLATE_H = 9;
const RAIL_Y = [-9, 6];
const CAR_HALF = 10;

/** Each lifecycle entry with the position the carriage stops at (ranges stop at their middle). */
export const STOPS = LIFECYCLE_PHASES.flatMap((phase) =>
  phase.steps.map((s) => {
    const numbers = s.n.split(/[–-]/).map(Number);
    const at = numbers.length > 1 ? (numbers[0] + numbers[1]) / 2 : numbers[0];
    return { n: s.n, title: s.title, gate: s.gate, phase: phase.phase, x: stepX(at) };
  }),
);

/** Booms sit just past their step, so a carriage stopped at the step is held in front of it. */
export const GATES = STOPS.filter((s) => s.gate).map((s) => ({ gate: s.gate as number, n: s.n, x: s.x + CAR_HALF + 2.4 }));

/** Phase boundaries: halfway between the last stop of a phase and the first of the next. */
const BOUNDARIES = LIFECYCLE_PHASES.slice(1).map((phase) => {
  const first = STOPS.findIndex((s) => s.n === phase.steps[0].n);
  return (STOPS[first - 1].x + STOPS[first].x) / 2;
});

export const P: Projection = k.fitProjection(
  [...k.boxCorners(PLATE, -PLATE_H - 4, 0), [GATES[0].x, PIVOT_Y, BOOM_Z + BOOM_LEN], [X1, PIVOT_Y, BOOM_Z + BOOM_LEN]],
  WIDTH,
  HEIGHT,
  { pad: 22, azimuth: 62 },
);

const at = (point: Point3) => k.iso(point, P);
const S = (paths: k.SolidPaths, style: Parameters<typeof k.solidSvg>[1] = {}) => k.solidSvg(paths, style);
const L = (d: string, style: Parameters<typeof k.lineSvg>[1] = {}) => k.lineSvg(d, style);
const D = (points: Array<k.Flat | [number, number, number]>, style: Parameters<typeof k.dotsSvg>[1] = {}) => k.dotsSvg(points, style);
const byDepth = (points: k.Plane[]) => [...points].sort((a, b) => k.depthOf([a[0], a[1], 0], P) - k.depthOf([b[0], b[1], 0], P));

export function baseArt(): string {
  const out = [`<path class="iso-halo" d="${k.haloOf(PLATE, -PLATE_H - 4, PLATE_H, P)}"/>`];
  for (const [x, y] of byDepth(k.corners(PLATE, 16))) {
    out.push(S(k.cylinder(x, y, 6.4, -PLATE_H - 4, 1.4, P, 28), { tone: "lo" }));
    out.push(S(k.cylinder(x, y, 4.8, -PLATE_H - 2.6, 2.6, P, 28), { tone: "mid" }));
  }
  out.push(S(k.slabOf(PLATE, -PLATE_H, PLATE_H, P, 8, 1.4), { tone: "mid", inner: L(k.sideSeam(PLATE, -PLATE_H + 3, P, 8), { tone: "faint" }) }));
  const groove = k.insetPlan(PLATE, 5);
  out.push(L(k.planOutline(groove, 0, P), { tone: "lo" }));
  // Phase seams run groove to groove across the plate: Plan | Prove | Publish | Ship.
  for (const x of BOUNDARIES) out.push(L(k.lineOnTop([x, groove.y], [x, groove.y + groove.d], 0, P), { tone: "faint" }));
  const screws = k.corners(PLATE, 10);
  out.push(L(screws.map(([x, y]) => k.ring(x, y, 2.2, 0, P, 16)).join(""), { tone: "lo" }));
  out.push(D(screws.map(([x, y]) => at([x, y, 0])), { size: 0.5 }));
  // The step ruler: a tick every half step, a major tick on every step.
  const strip: Plan = { x: X0 - 8, y: 24, w: X1 - X0 + 16, d: 7, r: 1 };
  out.push(S(k.slabOf(strip, 0, 0.6, P, 3), { tone: "lo", crease: "none" }));
  const ticks = k.topTicks(X0, X1, SPACING / 2, 2, strip.y + 0.8, 0.6, [1.8, 3.6], P);
  out.push(L(ticks.minor, { tone: "lo" }), L(ticks.major, { tone: "mid" }));
  out.push(D([at([strip.x + 3, strip.y + 3.5, 0.6]), at([strip.x + strip.w - 3, strip.y + 3.5, 0.6])], { size: 0.45 }));
  // Label plate at the start of the line, three rule lines for its engraving.
  const label: Plan = { x: X0 - 12, y: 34, w: 46, d: 6.5, r: 0.8 };
  out.push(S(k.slabOf(label, 0, 0.8, P, 3), { tone: "lo", crease: "none" }));
  out.push(L(k.lineOnTop([label.x + 4, label.y + 2.4], [label.x + 34, label.y + 2.4], 0.8, P), { tone: "lo", free: true }));
  out.push(L(k.lineOnTop([label.x + 4, label.y + 4.4], [label.x + 24, label.y + 4.4], 0.8, P), { tone: "lo", free: true }));
  // Rails on blocks, and end stops.
  for (const y of RAIL_Y) {
    out.push(S(k.slabOf({ x: X0 - 14, y, w: X1 - X0 + 28, d: 3, r: 1 }, 0, 3, P, 3, 0.4), { tone: "mid" }));
    out.push(D([X0 - 8, 0, X1 + 8].map((x) => at([x, y + 1.5, 3])), { size: 0.4 }));
  }
  for (const x of [X0 - 20, X1 + 15]) {
    out.push(S(k.slabOf({ x, y: -13, w: 5, d: 22, r: 1.2 }, 0, 8, P, 3, 0.5), { tone: "mid" }));
    out.push(D([at([x + 2.5, -9, 8]), at([x + 2.5, 5, 8])], { size: 0.45 }));
  }
  return out.join("");
}

/** The post stands beside the boom, on its -x side; the axle runs from the post through the boom. */
const POST_DX = -4.2;

/** The far half of a gate: counterweight base, post and lamp head. Drawn behind the boom. */
export function gateFarArt(x: number): string {
  const out: string[] = [];
  const px = x + POST_DX;
  const base: Plan = { x: px - 6, y: PIVOT_Y - 7, w: 12, d: 12, r: 1.6 };
  out.push(S(k.slabOf(base, 0, 3, P, 3, 0.4), { tone: "lo" }));
  out.push(D(k.corners(base, 2).map(([bx, by]) => at([bx, by, 3])), { size: 0.4 }));
  out.push(S(k.cylinder(px, PIVOT_Y - 1, 2.6, 3, BOOM_Z + 8, P, 24), { tone: "mid", crease: "none" }));
  out.push(L([8, 18].map((z) => k.sideArc(px, PIVOT_Y - 1, 2.6, z, P, 16)).join(""), { tone: "faint" }));
  out.push(S(k.slabOf({ x: px - 3.2, y: PIVOT_Y - 4, w: 6, d: 6, r: 1 }, BOOM_Z + 11, 6, P, 3, 0.4), { tone: "mid" }));
  return out.join("");
}

/** The lamp on the gate head: two dots on its front face, lit once the gate has passed. */
export function lampPoints(x: number): k.Flat[] {
  const px = x + POST_DX;
  return [at([px - 1.6, PIVOT_Y + 2, BOOM_Z + 14]), at([px + 1.2, PIVOT_Y + 2, BOOM_Z + 14])];
}

/** The near half: the rest fork the boom lands in, on the viewer's side of the track. */
export function gateNearArt(x: number): string {
  const out: string[] = [];
  const y = PIVOT_Y + BOOM_LEN - 2;
  out.push(S(k.slabOf({ x: x - 4, y: y - 3, w: 8, d: 6, r: 1 }, 0, 2, P, 3, 0.3), { tone: "lo" }));
  out.push(S(k.slabOf({ x: x - 1.2, y: y - 1.2, w: 2.4, d: 2.4, r: 0.6 }, 2, BOOM_Z - 2 - BOOM_T / 2 - 0.2, P, 3), { tone: "mid", crease: "none" }));
  for (const dx of [-3.2, 1.6]) {
    out.push(S(k.slabOf({ x: x + dx, y: y - 1.2, w: 1.6, d: 2.4, r: 0.4 }, BOOM_Z - BOOM_T / 2 - 0.2, 4, P, 2), { tone: "mid" }));
  }
  return out.join("");
}

/** The carriage at x = 0: base, bushings, tray and the Issue card standing in its holder. */
export function carriageArt(): string {
  const out = [`<path class="iso-halo" d="${k.haloOf({ x: -CAR_HALF + 2, y: -14, w: CAR_HALF * 2 - 4, d: 26, r: 2 }, 3, 0.1, P)}" style="filter:blur(4px)"/>`];
  for (const y of RAIL_Y) {
    for (const x of [-CAR_HALF + 1, CAR_HALF - 6]) out.push(S(k.slabOf({ x, y: y - 1, w: 5, d: 5, r: 0.8 }, 3, 3, P, 2, 0.3), { tone: "mid" }));
  }
  const body: Plan = { x: -CAR_HALF, y: -15, w: CAR_HALF * 2, d: 28, r: 2.4 };
  out.push(S(k.slabOf(body, 6, 3, P, 4, 0.6), { tone: "mid" }));
  out.push(D([-6, -2, 2, 6].map((x) => at([x, body.y + body.d - 2, 9])), { size: 0.4, tone: "lo" }));
  const holder: Plan = { x: -7, y: -5, w: 14, d: 6, r: 1 };
  out.push(S(k.slabOf(holder, 9, 3, P, 3, 0.4), { tone: "lo" }));
  out.push(S(k.slabOf({ x: -6, y: -2.6, w: 12, d: 0.8, r: 0 }, 11, 15, P, 2), { tone: "hi", lit: true }));
  out.push(L(k.segment([-4, -1.8, 23], [3.6, -1.8, 23], P), { tone: "lo", free: true }));
  out.push(L(k.segment([-4, -1.8, 20.6], [1.6, -1.8, 20.6], P), { tone: "lo", free: true }));
  out.push(L(k.segment([-4, -1.8, 18.2], [2.8, -1.8, 18.2], P), { tone: "lo", free: true }));
  // Thumb knob on the front, for pushing it by hand.
  out.push(S(k.slabOf({ x: -1.6, y: body.y + body.d, w: 3.2, d: 2, r: 0.6 }, 6.4, 2.2, P, 2), { tone: "mid", crease: "none" }));
  return out.join("");
}

export const CAR_VECTOR = k.axisVector(P, [1, 0, 0]);
export const HUB = (x: number) => k.sideRing([x + BOOM_T / 2 + 0.2, PIVOT_Y, BOOM_Z], 2.2, "x", P, 18);
export const LIMITS = { carHalf: CAR_HALF, gateGap: 2.4 };
