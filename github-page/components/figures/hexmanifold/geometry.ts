/**
 * The hexagonal port manifold (#2083): a sealed domain core with six keyed face sockets.
 * Each outer plug is an adapter or port; seating one is the only way into the core — the same
 * rule ADR-030 enforces with ArchUnit. Pure geometry: paths built once with the anatomy kit.
 */
import * as k from "@/components/iso/iso-kit";
import type { Plan, Plane, Point3, Projection } from "@/components/iso/iso-kit";

export const WIDTH = 640;
export const HEIGHT = 460;

export const FACES = [
  {
    key: "web",
    title: "Web",
    layer: "adapter.in.web",
    kind: "inbound adapter",
    blurb: "Controllers and web DTOs plug in here; they never see JPA.",
  },
  {
    key: "security",
    title: "Security",
    layer: "security filters",
    kind: "inbound adapter",
    blurb: "JWT and role guards seat on the same inbound ring.",
  },
  {
    key: "in",
    title: "In-port",
    layer: "application.port.in",
    kind: "port",
    blurb: "One interface per use case; adapters stop at this keyed socket.",
  },
  {
    key: "out",
    title: "Out-port",
    layer: "application.port.out",
    kind: "port",
    blurb: "Repositories and gateways, spoken only in domain terms.",
  },
  {
    key: "persist",
    title: "Persistence",
    layer: "adapter.out.persistence",
    kind: "outbound adapter",
    blurb: "JPA implements the out-ports; the core never imports it.",
  },
  {
    key: "pdf",
    title: "Report",
    layer: "adapter.out.pdf",
    kind: "outbound adapter",
    blurb: "PDF and report adapters leave through the same outer ring.",
  },
] as const;

export type Face = (typeof FACES)[number];

const PLINTH: Plan = { x: -92, y: -78, w: 184, d: 156, r: 3 };
const DECK: Plan = { x: -78, y: -66, w: 156, d: 132, r: 2 };
const Z0 = 8;
const DECK_H = 6;
const CORE_R = 22;
const CORE_Z = Z0 + DECK_H;
const CORE_H = 38;
const SOCKET_R = 48;
const PLUG_R = 62;
const SEAT_MAX = 14;

export const P: Projection = k.fitProjection(
  [
    ...k.boxCorners(PLINTH, -4, CORE_Z + CORE_H + 8),
    [PLUG_R + SEAT_MAX + 18, PLUG_R + SEAT_MAX + 18, CORE_Z + 20],
    [-(PLUG_R + SEAT_MAX + 18), -(PLUG_R + SEAT_MAX + 18), CORE_Z + 20],
  ],
  WIDTH,
  HEIGHT,
  { pad: 28, azimuth: 48, elevation: 32 },
);

const at = (point: Point3) => k.iso(point, P);
const S = (paths: k.SolidPaths, style: Parameters<typeof k.solidSvg>[1] = {}) => k.solidSvg(paths, style);
const L = (d: string, style: Parameters<typeof k.lineSvg>[1] = {}) => k.lineSvg(d, style);
const D = (points: Array<k.Flat | [number, number, number]>, style: Parameters<typeof k.dotsSvg>[1] = {}) =>
  k.dotsSvg(points, style);
const byDepth = (points: k.Plane[]) =>
  [...points].sort((a, b) => k.depthOf([a[0], a[1], 0], P) - k.depthOf([b[0], b[1], 0], P));

/** Flat-top hexagon vertices, radius `r`, centred on the origin. */
function hexRing(r: number): Plane[] {
  const out: Plane[] = [];
  for (let i = 0; i < 6; i += 1) {
    const a = ((30 + i * 60) * Math.PI) / 180;
    out.push([r * Math.cos(a), r * Math.sin(a)]);
  }
  return out;
}

/** Unit vector from the origin toward face `i` (mid-edge of the flat-top hex). */
export function faceDir(i: number): Plane {
  const a = ((i * 60) * Math.PI) / 180;
  return [Math.cos(a), Math.sin(a)];
}

/** Screen offset of one world unit of seating inward (toward the domain core). */
export function seatVector(i: number): [number, number] {
  const [dx, dy] = faceDir(i);
  const outer = at([dx, dy, CORE_Z + 16]);
  const inner = at([0, 0, CORE_Z + 16]);
  return [inner[0] - outer[0], inner[1] - outer[1]];
}

function plinth(): string {
  const out = [`<path class="iso-halo" d="${k.haloOf(PLINTH, -4, 4, P)}"/>`];
  for (const [x, y] of byDepth(k.corners(PLINTH, 10))) {
    out.push(S(k.cylinder(x, y, 5.6, -4, 1.2, P, 24), { tone: "lo" }));
    out.push(S(k.cylinder(x, y, 4.2, -2.8, 5.8, P, 24), { tone: "mid" }));
  }
  out.push(
    S(k.slabOf(PLINTH, 4, Z0 - 4, P, 6, 1.2), {
      tone: "mid",
      inner: L(k.sideSeam(PLINTH, 7, P, 6), { tone: "faint" }),
    }),
  );
  const strip: Plan = { x: -70, y: PLINTH.y + PLINTH.d - 2.8, w: 140, d: 2.2, r: 0.5 };
  out.push(S(k.slabOf(strip, Z0, 0.5, P, 3), { tone: "lo", crease: "none" }));
  const ticks = k.topTicks(strip.x + 4, strip.x + strip.w - 4, 4, 5, strip.y + 0.3, Z0 + 0.5, [0.7, 1.4], P);
  out.push(L(ticks.minor, { tone: "lo" }), L(ticks.major, { tone: "mid" }));
  return out.join("");
}

function deck(): string {
  const out: string[] = [];
  out.push(S(k.slabOf(DECK, Z0, DECK_H, P, 5, 0.8), { tone: "mid", crease: "faint" }));
  // Hexagonal raised plate under the sockets.
  out.push(S(k.extrude(hexRing(56), Z0 + DECK_H, 2.4, P, { bevel: 0.5 }), { tone: "lo" }));
  // Groove ring.
  out.push(L(k.pathOf(hexRing(54).map((p) => at([p[0], p[1], Z0 + DECK_H + 2.5])), true), { tone: "faint" }));
  out.push(L(k.pathOf(hexRing(40).map((p) => at([p[0], p[1], Z0 + DECK_H + 2.5])), true), { tone: "lo" }));
  // Corner screws on the deck.
  out.push(
    D(
      [
        at([DECK.x + 6, DECK.y + 6, Z0 + DECK_H + 0.2]),
        at([DECK.x + DECK.w - 6, DECK.y + 6, Z0 + DECK_H + 0.2]),
        at([DECK.x + 6, DECK.y + DECK.d - 6, Z0 + DECK_H + 0.2]),
        at([DECK.x + DECK.w - 6, DECK.y + DECK.d - 6, Z0 + DECK_H + 0.2]),
      ],
      { size: 0.45, tone: "lo" },
    ),
  );
  return out.join("");
}

function core(): string {
  const out: string[] = [];
  const baseZ = CORE_Z;
  // Pedestal collar.
  out.push(S(k.cylinder(0, 0, CORE_R + 6, baseZ, 3, P, 36, 0.4), { tone: "mid" }));
  // Sealed hexagonal domain chamber — the lit subject.
  out.push(S(k.extrude(hexRing(CORE_R), baseZ + 3, CORE_H, P, { bevel: 0.8 }), { tone: "hi", lit: true }));
  // Top cap with three rule lines (engraved label plate).
  const capZ = baseZ + 3 + CORE_H;
  out.push(S(k.extrude(hexRing(CORE_R - 1.5), capZ, 2.2, P, { bevel: 0.3 }), { tone: "hi", lit: true }));
  const label: Plan = { x: -10, y: -6, w: 20, d: 12, r: 0.6 };
  out.push(S(k.slabOf(label, capZ + 2.2, 0.4, P, 3), { tone: "lo", crease: "none", flat: true }));
  for (const t of [-2.5, 0, 2.5]) {
    out.push(L(k.pathOf([at([-7, t, capZ + 2.7]), at([7, t, capZ + 2.7])]), { tone: "mid", free: true }));
  }
  // Collar screws.
  for (let i = 0; i < 6; i += 1) {
    const [dx, dy] = faceDir(i);
    out.push(D([at([dx * (CORE_R + 4), dy * (CORE_R + 4), baseZ + 3.2])], { size: 0.4, tone: "lo" }));
  }
  return out.join("");
}

function socketArt(i: number): string {
  const [dx, dy] = faceDir(i);
  const out: string[] = [];
  const cx = dx * SOCKET_R;
  const cy = dy * SOCKET_R;
  const z = CORE_Z + 10;
  // Socket block on the deck.
  const block: Plan = { x: cx - 9, y: cy - 7, w: 18, d: 14, r: 1 };
  out.push(S(k.slabOf(block, CORE_Z + 2.4, 10, P, 4, 0.5), { tone: "mid" }));
  // Keyed bayonet collar.
  out.push(S(k.cylinder(cx, cy, 5.2, z, 4, P, 24, 0.3), { tone: "lo" }));
  out.push(S(k.cylinder(cx, cy, 3.4, z + 0.5, 3.2, P, 20), { tone: "faint", flat: true }));
  // Key notch.
  const kx = cx + dx * 4.2;
  const ky = cy + dy * 4.2;
  out.push(S(k.slabOf({ x: kx - 1.2, y: ky - 1.8, w: 2.4, d: 3.6, r: 0.3 }, z + 1, 2.4, P, 2), { tone: "hi" }));
  out.push(D([at([cx - 5, cy - 4, z + 4.2]), at([cx + 5, cy - 4, z + 4.2]), at([cx - 5, cy + 4, z + 4.2]), at([cx + 5, cy + 4, z + 4.2])], { size: 0.35, tone: "lo" }));
  return out.join("");
}

function plugArt(i: number, face: Face): string {
  const [dx, dy] = faceDir(i);
  const out: string[] = [];
  const cx = dx * PLUG_R;
  const cy = dy * PLUG_R;
  const z = CORE_Z + 8;
  // Cartridge body.
  const body: Plan = { x: cx - 11, y: cy - 8, w: 22, d: 16, r: 1.2 };
  out.push(S(k.slabOf(body, z, 14, P, 4, 0.6), { tone: face.kind.includes("port") ? "mid" : "hi" }));
  // Nose that seats into the socket.
  out.push(S(k.cylinder(cx - dx * 8, cy - dy * 8, 3.1, z + 4, 8, P, 20, 0.2), { tone: "lo" }));
  // Knurled grip ring on the outer face.
  out.push(S(k.cylinder(cx + dx * 6, cy + dy * 6, 4.5, z + 3, 8, P, 24, 0.3), { tone: "mid" }));
  for (let n = 0; n < 8; n += 1) {
    const a = (n * Math.PI) / 4;
    const px = cx + dx * 6 + Math.cos(a) * 3.6;
    const py = cy + dy * 6 + Math.sin(a) * 3.6;
    out.push(D([at([px, py, z + 11])], { size: 0.28, tone: "lo" }));
  }
  // Tiny engraved tag plate on top.
  const tag: Plan = { x: cx - 7, y: cy - 3.5, w: 14, d: 7, r: 0.4 };
  out.push(S(k.slabOf(tag, z + 14, 0.5, P, 2), { tone: "lo", crease: "none", flat: true }));
  out.push(L(k.pathOf([at([cx - 5, cy - 1, z + 14.6]), at([cx + 5, cy - 1, z + 14.6])]), { tone: "mid", free: true }));
  out.push(L(k.pathOf([at([cx - 4, cy + 1.2, z + 14.6]), at([cx + 4, cy + 1.2, z + 14.6])]), { tone: "faint", free: true }));
  // Corner screws.
  out.push(
    D(
      [
        at([cx - 8, cy - 5.5, z + 14.2]),
        at([cx + 8, cy - 5.5, z + 14.2]),
        at([cx - 8, cy + 5.5, z + 14.2]),
        at([cx + 8, cy + 5.5, z + 14.2]),
      ],
      { size: 0.35, tone: "lo" },
    ),
  );
  return out.join("");
}

export function readoutFor(i: number): string {
  const f = FACES[i];
  return `${f.title} · ${f.layer} · ${f.kind}`;
}

export type PlugPiece = {
  index: number;
  art: string;
  seat: number;
  vector: [number, number];
};

/** Static base (plinth, deck, core, sockets) plus one seatable plug per face. */
export function manifoldArt(): { base: string; plugs: PlugPiece[] } {
  const sockets = FACES.map((_, i) => ({ i, depth: k.depthOf([faceDir(i)[0] * SOCKET_R, faceDir(i)[1] * SOCKET_R, CORE_Z + 12], P) }))
    .sort((a, b) => a.depth - b.depth)
    .map(({ i }) => socketArt(i))
    .join("");

  const base = [plinth(), deck(), sockets, core()].join("");
  // Plugs stay in face order (0..5) so live motion can index them; paint order is decided at runtime.
  const plugs = FACES.map((face, i) => ({
    index: i,
    art: plugArt(i, face),
    seat: SEAT_MAX,
    vector: seatVector(i),
  }));
  return { base, plugs };
}

export { SEAT_MAX };
