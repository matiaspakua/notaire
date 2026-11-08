/**
 * Shared machine parts for the Anatomy figures: every helper returns SVG markup built with the
 * isometric kit, so a figure is assembled from real parts (feet, screws, knurled knobs, dials,
 * drawers, label plates) instead of boxes and arrows.
 */
import * as k from "@/components/iso/iso-kit";
import type { Plan, Point3, Projection, Tone } from "@/components/iso/iso-kit";

export interface Scene {
  P: Projection;
  at: (point: Point3) => [number, number];
  S: (paths: ReturnType<typeof k.slabOf>, style?: Parameters<typeof k.solidSvg>[1]) => string;
  L: (d: string, style?: Parameters<typeof k.lineSvg>[1]) => string;
  D: (points: Array<[number, number]>, style?: Parameters<typeof k.dotsSvg>[1]) => string;
  byDepth: <T extends [number, number]>(points: T[]) => T[];
}

export function sceneOf(P: Projection): Scene {
  return {
    P,
    at: (point) => k.iso(point, P),
    S: (paths, style) => k.solidSvg(paths, style),
    L: (d, style) => k.lineSvg(d, style),
    D: (points, style) => k.dotsSvg(points, style),
    byDepth: (points) => [...points].sort((a, b) => k.depthOf([a[0], a[1], 0], P) - k.depthOf([b[0], b[1], 0], P)),
  };
}

/** Heavy base on four feet with a groove, corner screws, a ruler strip and a label plate. */
export function base(s: Scene, plan: Plan, thickness: number, footHeight: number): string {
  const { P } = s;
  const out = [`<path class="iso-halo" d="${k.haloOf(plan, -thickness - footHeight, thickness, P)}"/>`];
  for (const [x, y] of s.byDepth(k.corners(plan, 20))) {
    out.push(s.S(k.cylinder(x, y, 7, -thickness - footHeight, 1.6, P, 28), { tone: "lo" }));
    out.push(s.S(k.cylinder(x, y, 5.4, -thickness - footHeight + 1.6, footHeight - 1.6, P, 28), { tone: "mid" }));
  }
  out.push(s.S(k.slabOf(plan, -thickness, thickness, P, 8, 1.6), { tone: "mid" }));
  out.push(s.L(k.planOutline(k.insetPlan(plan, 5), 0, P), { tone: "lo" }));
  const screws = k.corners(plan, 10);
  out.push(s.L(screws.map(([x, y]) => k.ring(x, y, 2.2, 0, P, 16)).join(""), { tone: "lo" }));
  out.push(s.D(screws.map(([x, y]) => s.at([x, y, 0])), { size: 0.5 }));
  const strip = { x: plan.x + 14, y: plan.y + plan.d - 15, w: plan.w - 60, d: 7, r: 1 };
  out.push(s.S(k.slabOf(strip, 0, 0.6, P, 3), { tone: "lo", crease: "none" }));
  const ticks = k.topTicks(strip.x + 4, strip.x + strip.w - 4, 4, 5, strip.y + 0.8, 0.6, [2, 3.6], P);
  out.push(s.L(ticks.minor, { tone: "lo" }), s.L(ticks.major, { tone: "mid" }));
  out.push(s.D([s.at([strip.x + 2, strip.y + 3.5, 0.6]), s.at([strip.x + strip.w - 2, strip.y + 3.5, 0.6])], { size: 0.45 }));
  const label = { x: plan.x + plan.w - 38, y: plan.y + plan.d - 20, w: 28, d: 12, r: 1.5 };
  out.push(s.S(k.slabOf(label, 0, 0.8, P, 3), { tone: "lo", crease: "none" }));
  for (const dy of [3, 6, 9]) out.push(s.L(k.lineOnTop([label.x + 3, label.y + dy], [label.x + label.w - 3, label.y + dy], 0.8, P), { tone: "lo", free: true }));
  return out.join("");
}

/** A rounded box with an inset panel line, corner screws and optional vent slots. */
export function box(
  s: Scene,
  plan: Plan,
  z: number,
  h: number,
  { tone = "mid", steps = 4, bevel = 0.6, screws = true, panel = true }: { tone?: Tone; steps?: number; bevel?: number; screws?: boolean; panel?: boolean } = {},
): string {
  const { P } = s;
  const out = [s.S(k.slabOf(plan, z, h, P, steps, bevel), { tone })];
  if (panel && plan.w > 12 && plan.d > 12) out.push(s.L(k.planOutline(k.insetPlan(plan, 2.4), z + h, P, 3), { tone: "faint" }));
  if (screws && plan.w > 14 && plan.d > 14) out.push(s.D(k.corners(plan, 3.4).map(([x, y]) => s.at([x, y, z + h])), { size: 0.4, tone: "lo" }));
  return out.join("");
}

/** A round post with a collar. */
export function post(s: Scene, x: number, y: number, r: number, z: number, h: number, tone: Tone = "mid"): string {
  const { P } = s;
  return [
    s.S(k.cylinder(x, y, r * 1.5, z, 2.4, P, 24), { tone }),
    s.S(k.cylinder(x, y, r, z + 2.4, h - 2.4, P, 22), { tone, crease: "none" }),
    s.L(k.sideArc(x, y, r, z + h * 0.5, P, 12), { tone: "faint" }),
  ].join("");
}

/** A knurled knob with a pointer mark. */
export function knob(s: Scene, x: number, y: number, r: number, z: number, h: number, tone: Tone = "mid"): string {
  const { P } = s;
  return [
    s.S(k.cylinder(x, y, r, z, h, P, 32, 0.5), { tone }),
    s.L(k.knurl(x, y, r, z, z + h, 30, P), { tone: "lo" }),
    s.L(k.lineOnTop([x, y], [x + r * 0.85, y], z + h, P), { tone: "hi" }),
    s.D([s.at([x, y, z + h])], { size: 0.5, tone: "hi" }),
  ].join("");
}

/** A dial gauge on a short stalk; the needle is separate so a figure can move it. */
export function dialFace(s: Scene, x: number, y: number, r: number, z: number): string {
  const { P } = s;
  const out: string[] = [];
  out.push(s.S(k.cylinder(x, y, r * 0.16, z, 6, P, 16), { tone: "mid" }));
  out.push(s.S(k.cylinder(x, y, r, z + 6, 9, P, 48, 0.7), { tone: "hi", crease: "lo" }));
  out.push(s.L(k.knurl(x, y, r, z + 12, z + 15, 60, P, 0.3), { tone: "lo" }));
  out.push(s.L(k.ring(x, y, r - 2.2, z + 15, P, 48), { tone: "lo" }));
  const ticks = k.radialTicks(x, y, r - 3, 40, 5, z + 15, [1.2, 2.6], P, -Math.PI * 0.75, Math.PI * 1.5);
  out.push(s.L(ticks.minor, { tone: "lo" }), s.L(ticks.major, { tone: "mid" }));
  out.push(s.L(k.ring(x, y, 1.5, z + 15, P, 14), { tone: "mid" }));
  return out.join("");
}

/** Needle path for a dial drawn by `dialFace`, `fraction` 0..1 across the 270-degree sweep. */
export function needle(s: Scene, x: number, y: number, r: number, z: number, fraction: number): string {
  const angle = -Math.PI * 0.75 + fraction * Math.PI * 1.5;
  const dx = Math.sin(angle);
  const dy = -Math.cos(angle);
  return k.lineOnTop([x - dx * 3, y - dy * 3], [x + dx * (r - 4), y + dy * (r - 4)], z + 15.2, s.P);
}

/** A tagged number disc used as a numbered callout stamped on the machine. */
export function tag(s: Scene, x: number, y: number, z: number, n: number): string {
  const { P } = s;
  return [
    s.S(k.cylinder(x, y, 5.4, z, 1.6, P, 24, 0.3), { tone: "hi", lit: true }),
    s.L(k.ring(x, y, 4.2, z + 1.6, P, 24), { tone: "lo" }),
    k.faceTextSvg(k.topMatrix([x, y + 1.7], z + 1.6, P, "x"), String(n), { size: 4.6, tone: "hi", anchor: "middle" }),
  ].join("");
}

/** A drawer front: recessed face, handle, label plate and a keyhole. Built at rest; slide it with a transform. */
export function drawer(s: Scene, x: number, y: number, w: number, d: number, z: number, h: number): string {
  const { P } = s;
  const plan = { x, y, w, d, r: 1.2 };
  const out = [s.S(k.slabOf(plan, z, h, P, 3, 0.4), { tone: "mid" })];
  out.push(s.L(k.planOutline(k.insetPlan(plan, 1.6), z + h, P, 3), { tone: "faint" }));
  const handle = { x: x + w / 2 - 6, y: y + d - 2.6, w: 12, d: 2.6, r: 1 };
  out.push(s.S(k.slabOf(handle, z + h * 0.4, 1.8, P, 2, 0.2), { tone: "hi" }));
  out.push(s.D([s.at([x + 4, y + 3, z + h]), s.at([x + w - 4, y + 3, z + h])], { size: 0.38, tone: "lo" }));
  return out.join("");
}

/** A short ruler strip along x on a top face. */
export function ruler(s: Scene, x0: number, x1: number, y: number, z: number): string {
  const { P } = s;
  const strip = { x: x0, y, w: x1 - x0, d: 5, r: 0.8 };
  const ticks = k.topTicks(x0 + 3, x1 - 3, 3, 5, y + 0.6, z + 0.5, [1.6, 3], P);
  return [s.S(k.slabOf(strip, z, 0.5, P, 2), { tone: "lo", crease: "none" }), s.L(ticks.minor, { tone: "lo" }), s.L(ticks.major, { tone: "mid" })].join("");
}

/** A coiled spring pair (back and front halves) around a short rod along x. */
export function spring(s: Scene, from: number, to: number, cy: number, cz: number, r: number, turns: number): { back: string; front: string } {
  const c = k.coil(from, to, [cy, cz], r, turns, "x", s.P);
  return { back: s.L(c.back, { tone: "mid" }), front: s.L(c.front, { tone: "hi" }) };
}

export { k };
