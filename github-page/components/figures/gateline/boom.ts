/**
 * The one part of the gated line that changes shape: a barrier boom turning about its pivot.
 * Kept apart from geometry.ts so the client bundle carries only this and the kit's camera.
 */
import { iso, pathOf, type Flat, type Point3, type Projection } from "@/components/iso/iso-kit";

export const PIVOT_Y = -30;
export const BOOM_Z = 15;
export const BOOM_LEN = 54;
export const BOOM_T = 2.6;

export function hullOf(points: Flat[]): Flat[] {
  const p = [...points].sort((a, b) => a[0] - b[0] || a[1] - b[1]);
  const cross = (o: Flat, a: Flat, b: Flat) => (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0]);
  const lower: Flat[] = [];
  for (const q of p) {
    while (lower.length >= 2 && cross(lower[lower.length - 2], lower[lower.length - 1], q) <= 0) lower.pop();
    lower.push(q);
  }
  const upper: Flat[] = [];
  for (const q of [...p].reverse()) {
    while (upper.length >= 2 && cross(upper[upper.length - 2], upper[upper.length - 1], q) <= 0) upper.pop();
    upper.push(q);
  }
  return [...lower.slice(0, -1), ...upper.slice(0, -1)];
}

/**
 * A boom at `angle` radians (0 = down across the track, π/2 = standing up): its silhouette, the
 * +x side face (always toward the viewer), and four warning bands that wrap the top and the side.
 */
export function boomPaths(P: Projection, x: number, angle: number): { outline: string; side: string; bands: string } {
  const c = Math.cos(angle);
  const s = Math.sin(angle);
  const point = (along: number, up: number, side: number): Point3 => [x + side, PIVOT_Y + c * along - s * up, BOOM_Z + s * along + c * up];
  const h = BOOM_T / 2;
  const corners: Flat[] = [];
  for (const along of [-6, BOOM_LEN]) for (const up of [-h, h]) for (const side of [-h, h]) corners.push(iso(point(along, up, side), P));
  const sideFace = [point(-6, -h, h), point(BOOM_LEN, -h, h), point(BOOM_LEN, h, h), point(-6, h, h)].map((q) => iso(q, P));
  const bands = [12, 24, 36, 48]
    .map((a) => pathOf([iso(point(a, h, -h), P), iso(point(a, h, h), P), iso(point(a, -h, h), P)]))
    .join("");
  return { outline: pathOf(hullOf(corners), true), side: pathOf(sideFace, true), bands };
}
