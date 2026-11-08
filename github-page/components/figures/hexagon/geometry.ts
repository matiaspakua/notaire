/**
 * The junction instrument (#2083): hexagonal ports and adapters as a sealed brass case on a
 * plinth. The case is the domain. Nothing is drilled through it. Glands sit on the skin —
 * two on the front face (inbound), two on the rear of the lid (outbound). Plug modules
 * (HTTP web, JPA persistence, PDF) mate only to those glands, along a short cable.
 *
 * Figure, about 55 solids. One mechanism: the selected plug backs off its gland.
 */
import * as k from "@/components/iso/iso-kit";
import type { Plan, Point3, Projection } from "@/components/iso/iso-kit";

export const WIDTH = 680;
export const HEIGHT = 500;

export const SOCKETS = [
  {
    id: "http",
    name: "HTTP web",
    side: "inbound",
    adapter: "HTTP web",
    readout: "HTTP web · inbound gland · adapter → port → domain",
  },
  {
    id: "open",
    name: "Open inbound",
    side: "inbound",
    adapter: null,
    readout: "Open gland · inbound port · the socket faces the domain",
  },
  {
    id: "jpa",
    name: "JPA persistence",
    side: "outbound",
    adapter: "JPA persistence",
    readout: "JPA persistence · outbound gland · adapter → port → domain",
  },
  {
    id: "pdf",
    name: "PDF",
    side: "outbound",
    adapter: "PDF",
    readout: "PDF · outbound gland · adapter → port → domain",
  },
] as const;

export type SocketId = (typeof SOCKETS)[number]["id"];
export type PlugId = "http" | "jpa" | "pdf";

const PLINTH: Plan = { x: -108, y: -76, w: 216, d: 164, r: 5 };
const PLINTH_TOP = 9;
const CASE: Plan = { x: -34, y: -28, w: 68, d: 60, r: 1.5 };
const CASE_Z = 12;
const CASE_H = 40;
const LID_H = 4;
const LID: Plan = { x: -35.4, y: -29.4, w: 70.8, d: 62.8, r: 1.6 };
const FRONT = CASE.y + CASE.d;
const LID_TOP = CASE_Z + CASE_H + LID_H;

const GLAND_Z = 26;
const GLAND_H = 16;
const HTTP_X = -15;
const OPEN_X = 15;
const BODY_D = 8;
const NUT_D = 3.2;
const SHELL_D = 5;
const SHELL_W = 11;

const HTTP_SHELL_Y = FRONT + BODY_D + NUT_D;
const HTTP_MOD_Y = 57;
const BOSS = 1.6;
const HTTP_CABLE_Y0 = HTTP_SHELL_Y + SHELL_D;
const HTTP_CABLE_Y1 = HTTP_MOD_Y - BOSS;

const DECK_Y = -14;
const PDF_X = -15;
const JPA_X = 15;
const STEM_R = 3.5;
const CABLE = 3.2;
const DECK_CABLE_Z = 60;
const DECK_CABLE_H = 4;

const PDF_SHELL_X1 = PDF_X - STEM_R;
const PDF_SHELL_X0 = PDF_SHELL_X1 - SHELL_D;
const PDF_RISER_X0 = -46;
const PDF_RISER_X1 = PDF_RISER_X0 + CABLE;
const PDF_MOD_X1 = -72;
const PDF_LOW_X0 = PDF_MOD_X1 + BOSS;
const PDF_LOW_X1 = PDF_RISER_X0;
const PDF_HIGH_X0 = PDF_RISER_X1;
const PDF_HIGH_X1 = PDF_SHELL_X0;

const JPA_SHELL_X0 = JPA_X + STEM_R;
const JPA_SHELL_X1 = JPA_SHELL_X0 + SHELL_D;
const JPA_RISER_X1 = 46;
const JPA_RISER_X0 = JPA_RISER_X1 - CABLE;
const JPA_MOD_X0 = 72;
const JPA_LOW_X0 = JPA_RISER_X1;
const JPA_LOW_X1 = JPA_MOD_X0 - BOSS;
const JPA_HIGH_X0 = JPA_SHELL_X1;
const JPA_HIGH_X1 = JPA_RISER_X0;

const LOW_Z = 22;
const LOW_H = 4;
const MOD_H = 30;

export const UNMATE = 7;

export type Aabb = { id: string; x0: number; y0: number; z0: number; x1: number; y1: number; z1: number };

/** Every gland, cable and plug. Each one stops on the skin: none of these enter the case. */
export const ATTACHMENTS: Aabb[] = [
  { id: "http-gland", x0: HTTP_X - 8, y0: FRONT, z0: GLAND_Z, x1: HTTP_X + 8, y1: HTTP_SHELL_Y, z1: GLAND_Z + GLAND_H },
  { id: "open-gland", x0: OPEN_X - 8, y0: FRONT, z0: GLAND_Z, x1: OPEN_X + 8, y1: FRONT + BODY_D + NUT_D, z1: GLAND_Z + GLAND_H + 1.4 },
  { id: "http-shell", x0: HTTP_X - SHELL_W / 2, y0: HTTP_SHELL_Y, z0: 28, x1: HTTP_X + SHELL_W / 2, y1: HTTP_CABLE_Y0, z1: 40 },
  { id: "http-cable", x0: HTTP_X - CABLE / 2, y0: HTTP_CABLE_Y0, z0: 31, x1: HTTP_X + CABLE / 2, y1: HTTP_CABLE_Y1, z1: 37 },
  { id: "pdf-gland", x0: PDF_X - 6, y0: DECK_Y - 6, z0: LID_TOP, x1: PDF_X + 6, y1: DECK_Y + 6, z1: 70 },
  { id: "jpa-gland", x0: JPA_X - 6, y0: DECK_Y - 6, z0: LID_TOP, x1: JPA_X + 6, y1: DECK_Y + 6, z1: 70 },
  { id: "pdf-shell", x0: PDF_SHELL_X0, y0: DECK_Y - SHELL_W / 2, z0: 59, x1: PDF_SHELL_X1, y1: DECK_Y + SHELL_W / 2, z1: 65 },
  { id: "jpa-shell", x0: JPA_SHELL_X0, y0: DECK_Y - SHELL_W / 2, z0: 59, x1: JPA_SHELL_X1, y1: DECK_Y + SHELL_W / 2, z1: 65 },
  { id: "pdf-low", x0: PDF_LOW_X0, y0: DECK_Y - CABLE / 2, z0: LOW_Z, x1: PDF_LOW_X1, y1: DECK_Y + CABLE / 2, z1: LOW_Z + LOW_H },
  { id: "pdf-riser", x0: PDF_RISER_X0, y0: DECK_Y - CABLE / 2, z0: LOW_Z, x1: PDF_RISER_X1, y1: DECK_Y + CABLE / 2, z1: DECK_CABLE_Z },
  { id: "pdf-high", x0: PDF_HIGH_X0, y0: DECK_Y - CABLE / 2, z0: DECK_CABLE_Z, x1: PDF_HIGH_X1, y1: DECK_Y + CABLE / 2, z1: DECK_CABLE_Z + DECK_CABLE_H },
  { id: "jpa-high", x0: JPA_HIGH_X0, y0: DECK_Y - CABLE / 2, z0: DECK_CABLE_Z, x1: JPA_HIGH_X1, y1: DECK_Y + CABLE / 2, z1: DECK_CABLE_Z + DECK_CABLE_H },
  { id: "jpa-riser", x0: JPA_RISER_X0, y0: DECK_Y - CABLE / 2, z0: LOW_Z, x1: JPA_RISER_X1, y1: DECK_Y + CABLE / 2, z1: DECK_CABLE_Z },
  { id: "jpa-low", x0: JPA_LOW_X0, y0: DECK_Y - CABLE / 2, z0: LOW_Z, x1: JPA_LOW_X1, y1: DECK_Y + CABLE / 2, z1: LOW_Z + LOW_H },
];

const INTERIOR: Aabb = {
  id: "case",
  x0: CASE.x + 0.05,
  y0: CASE.y + 0.05,
  z0: CASE_Z + 0.05,
  x1: CASE.x + CASE.w - 0.05,
  y1: CASE.y + CASE.d - 0.05,
  z1: CASE_Z + CASE_H - 0.05,
};

/** True when a part passes through the sealed case instead of stopping on its skin. */
export function entersCase(box: Aabb): boolean {
  return box.x0 < INTERIOR.x1 && box.x1 > INTERIOR.x0 && box.y0 < INTERIOR.y1 && box.y1 > INTERIOR.y0 && box.z0 < INTERIOR.z1 && box.z1 > INTERIOR.z0;
}

export const P: Projection = k.fitProjection(
  [
    ...k.boxCorners(PLINTH, -4, 72),
    [PDF_MOD_X1 - 28, DECK_Y - 14, 48],
    [JPA_MOD_X0 + 30, DECK_Y + 14, 48],
    [HTTP_X, HTTP_MOD_Y + 20, 50],
    [PDF_X, DECK_Y, 72],
    [JPA_X, DECK_Y, 72],
  ],
  WIDTH,
  HEIGHT,
  { pad: 28, azimuth: 50, elevation: 38 },
);

const at = (point: Point3) => k.iso(point, P);
const S = (paths: k.SolidPaths, style: Parameters<typeof k.solidSvg>[1] = {}) => k.solidSvg(paths, style);
const L = (d: string, style: Parameters<typeof k.lineSvg>[1] = {}) => k.lineSvg(d, style);
const D = (points: Array<k.Flat | [number, number, number]>, style: Parameters<typeof k.dotsSvg>[1] = {}) => k.dotsSvg(points, style);
const byDepth = (points: k.Plane[]) => [...points].sort((a, b) => k.depthOf([a[0], a[1], 0], P) - k.depthOf([b[0], b[1], 0], P));

const hit = (id: string, html: string) => `<g data-socket="${id}">${html}</g>`;
const accent = (id: string, html: string) => `<g data-accent="${id}">${html}</g>`;

function faceRect(x0: number, x1: number, z0: number, z1: number, y: number): string {
  return k.pathOf([at([x0, y, z0]), at([x1, y, z0]), at([x1, y, z1]), at([x0, y, z1])], true);
}

function plinth(): string {
  const out = [`<path class="iso-halo" d="${k.haloOf(PLINTH, -4, 4, P)}"/>`];
  for (const [x, y] of byDepth(k.corners(PLINTH, 16))) {
    out.push(S(k.cylinder(x, y, 6.2, -4, 1.4, P, 24), { tone: "lo" }));
    out.push(S(k.cylinder(x, y, 4.6, -2.6, 6.2, P, 24), { tone: "mid" }));
  }
  out.push(S(k.slabOf(PLINTH, 4, PLINTH_TOP - 4, P, 6, 1.2), { tone: "mid", inner: L(k.sideSeam(PLINTH, 6.5, P, 6), { tone: "faint" }) }));
  out.push(L(k.planOutline(k.insetPlan(PLINTH, 5), PLINTH_TOP, P, 6), { tone: "lo" }));
  const screws = k.corners(PLINTH, 10);
  out.push(L(screws.map(([x, y]) => k.ring(x, y, 2.1, PLINTH_TOP, P, 16)).join(""), { tone: "lo" }));
  out.push(D(screws.map(([x, y]) => at([x, y, PLINTH_TOP])), { size: 0.48 }));
  const strip: Plan = { x: -78, y: PLINTH.y + PLINTH.d - 3.2, w: 156, d: 2.4, r: 0.6 };
  out.push(S(k.slabOf(strip, PLINTH_TOP, 0.6, P, 3), { tone: "lo", crease: "none" }));
  const ticks = k.topTicks(strip.x + 3, strip.x + strip.w - 3, 4, 5, strip.y + 0.35, PLINTH_TOP + 0.6, [0.8, 1.5], P);
  out.push(L(ticks.minor, { tone: "lo" }), L(ticks.major, { tone: "mid" }));
  const label: Plan = { x: 36, y: 74, w: 44, d: 8, r: 0.8 };
  out.push(S(k.slabOf(label, PLINTH_TOP, 0.8, P, 3), { tone: "lo", crease: "none" }));
  out.push(L(k.planOutline(k.insetPlan(label, 1.2), PLINTH_TOP + 0.8, P, 3), { tone: "faint" }));
  out.push(L(k.lineOnTop([label.x + 4, label.y + 2.6], [label.x + 32, label.y + 2.6], PLINTH_TOP + 0.8, P), { tone: "lo", free: true }));
  out.push(L(k.lineOnTop([label.x + 4, label.y + 4.6], [label.x + 24, label.y + 4.6], PLINTH_TOP + 0.8, P), { tone: "lo", free: true }));
  out.push(L(k.lineOnTop([label.x + 4, label.y + 6.4], [label.x + 18, label.y + 6.4], PLINTH_TOP + 0.8, P), { tone: "lo", free: true }));
  out.push(D([at([label.x + 2.2, label.y + 4, PLINTH_TOP + 0.8]), at([label.x + label.w - 2.2, label.y + 4, PLINTH_TOP + 0.8])], { size: 0.4 }));
  out.push(S(k.cylinder(90, 52, 3.2, PLINTH_TOP, 1.2, P, 20), { tone: "lo" }));
  out.push(S(k.cylinder(90, 52, 1.5, PLINTH_TOP + 1.2, 5.5, P, 16), { tone: "mid", crease: "none" }));
  out.push(L(k.sideArc(90, 52, 1.5, PLINTH_TOP + 3.4, P, 16), { tone: "faint" }));
  return out.join("");
}

function yoke(): string {
  const frame: Plan = { x: CASE.x - 1.2, y: CASE.y - 1.2, w: CASE.w + 2.4, d: CASE.d + 2.4, r: 1.2 };
  return S(k.slabOf(frame, PLINTH_TOP, CASE_Z - PLINTH_TOP, P, 4, 0.4), { tone: "lo" });
}

function sealedCase(): string {
  const out: string[] = [];
  out.push(S(k.slabOf(CASE, CASE_Z, CASE_H, P, 5, 1.1), { tone: "hi", inner: L(k.sideSeam(CASE, CASE_Z + 18, P, 5), { tone: "faint" }) }));
  out.push(S(k.slabOf(LID, CASE_Z + CASE_H, LID_H, P, 5, 0.8), { tone: "mid", inner: L(k.sideSeam(LID, LID_TOP - 1.6, P, 5), { tone: "faint" }) }));
  out.push(L(k.planOutline(k.insetPlan(LID, 3.5), LID_TOP, P, 5), { tone: "faint" }));
  for (const x of [CASE.x + 6, CASE.x + CASE.w - 6]) {
    out.push(L(k.segment([x, FRONT, CASE_Z], [x, FRONT, CASE_Z + CASE_H], P), { tone: "lo" }));
  }
  for (const y of [CASE.y + 8, CASE.y + CASE.d - 8]) {
    out.push(L(k.segment([CASE.x + CASE.w, y, CASE_Z], [CASE.x + CASE.w, y, CASE_Z + CASE_H], P), { tone: "lo" }));
  }
  out.push(L(faceRect(CASE.x + 10, CASE.x + CASE.w - 10, CASE_Z + 6, CASE_Z + CASE_H - 6, FRONT), { tone: "faint" }));
  const plate: Plan = { x: -13, y: 8, w: 26, d: 12, r: 0.8 };
  out.push(S(k.slabOf(plate, LID_TOP, 0.7, P, 3), { tone: "lo", crease: "none" }));
  out.push(L(k.planOutline(k.insetPlan(plate, 1.2), LID_TOP + 0.7, P, 3), { tone: "faint" }));
  out.push(L(k.lineOnTop([plate.x + 3, plate.y + 3.4], [plate.x + 18, plate.y + 3.4], LID_TOP + 0.7, P), { tone: "lo", free: true }));
  out.push(L(k.lineOnTop([plate.x + 3, plate.y + 6], [plate.x + 22, plate.y + 6], LID_TOP + 0.7, P), { tone: "lo", free: true }));
  out.push(L(k.lineOnTop([plate.x + 3, plate.y + 8.6], [plate.x + 14, plate.y + 8.6], LID_TOP + 0.7, P), { tone: "lo", free: true }));
  const screws = k.corners(LID, 4.5);
  out.push(L(screws.map(([x, y]) => k.ring(x, y, 1.5, LID_TOP, P, 14)).join(""), { tone: "lo" }));
  out.push(D(screws.map(([x, y]) => at([x, y, LID_TOP])), { size: 0.42 }));
  return out.join("");
}

function frontGland(cx: number, id: string, withAccent: boolean): string {
  const body: Plan = { x: cx - 7, y: FRONT, w: 14, d: BODY_D, r: 1.1 };
  const nut: Plan = { x: cx - 8, y: FRONT + BODY_D, w: 16, d: NUT_D, r: 1 };
  const zNut = GLAND_Z + 1;
  const nutH = GLAND_H - 2;
  const nutSolid = S(k.slabOf(nut, zNut, nutH, P, 3, 0.3), { tone: "mid" });
  const parts = [
    S(k.slabOf(body, GLAND_Z, GLAND_H, P, 3, 0.45), { tone: "mid" }),
    withAccent ? accent(id, nutSolid) : nutSolid,
    S(k.slabOf({ x: cx - 1.3, y: FRONT + BODY_D, w: 2.6, d: NUT_D, r: 0.4 }, zNut + nutH, 1.3, P, 2), { tone: "lo", crease: "none" }),
    L(k.sideRing([cx, FRONT + BODY_D + NUT_D, GLAND_Z + GLAND_H / 2], 4.2, "y", P, 22), { tone: "lo" }),
    D(
      [
        at([cx - 5.2, FRONT + BODY_D + NUT_D, GLAND_Z + 4]),
        at([cx + 5.2, FRONT + BODY_D + NUT_D, GLAND_Z + 4]),
        at([cx - 5.2, FRONT + BODY_D + NUT_D, GLAND_Z + GLAND_H - 4]),
        at([cx + 5.2, FRONT + BODY_D + NUT_D, GLAND_Z + GLAND_H - 4]),
      ],
      { size: 0.38, tone: "lo" },
    ),
  ];
  return hit(id, parts.join(""));
}

function deckGland(cx: number, id: string): string {
  const parts = [
    S(k.cylinder(cx, DECK_Y, 5.8, LID_TOP, 2.2, P, 28, 0.3), { tone: "lo" }),
    S(k.cylinder(cx, DECK_Y, STEM_R, LID_TOP + 2.2, 9, P, 24), { tone: "mid", crease: "none" }),
    L(k.sideArc(cx, DECK_Y, STEM_R, LID_TOP + 5.4, P, 20), { tone: "faint" }),
    L(k.sideArc(cx, DECK_Y, STEM_R, LID_TOP + 8.6, P, 20), { tone: "faint" }),
    S(k.cylinder(cx, DECK_Y, 4.7, LID_TOP + 11.2, 2.6, P, 24, 0.25), { tone: "mid" }),
    L(k.ring(cx, DECK_Y, 2.4, LID_TOP + 13.8, P, 20), { tone: "lo" }),
    D(
      [at([cx - 3.6, DECK_Y, LID_TOP + 2.2]), at([cx + 3.6, DECK_Y, LID_TOP + 2.2]), at([cx, DECK_Y - 3.6, LID_TOP + 2.2]), at([cx, DECK_Y + 3.6, LID_TOP + 2.2])],
      { size: 0.36, tone: "lo" },
    ),
  ];
  return hit(id, parts.join(""));
}

function shell(box: Plan, z: number, h: number, id: PlugId, lit: boolean): string {
  return accent(id, S(k.slabOf(box, z, h, P, 3, 0.35), { tone: "hi", lit }));
}

function moduleBox(plan: Plan, id: string, knobAt: [number, number], boss: Plan): string {
  const top = PLINTH_TOP + MOD_H;
  const parts = [
    S(k.slabOf(plan, PLINTH_TOP, MOD_H, P, 4, 0.6), { tone: "mid" }),
    S(k.slabOf(k.insetPlan(plan, 1.4), top, 1.2, P, 3), { tone: "lo", crease: "none" }),
    S(k.cylinder(knobAt[0], knobAt[1], 3.3, top + 1.2, 5, P, 24, 0.3), { tone: "mid" }),
    L(k.sideArc(knobAt[0], knobAt[1], 3.3, top + 3.2, P, 18), { tone: "faint" }),
    L(k.sideArc(knobAt[0], knobAt[1], 3.3, top + 4.8, P, 18), { tone: "lo" }),
    S(k.slabOf(boss, 30, 8, P, 2, 0.3), { tone: "lo" }),
    D(
      k.corners(plan, 2.2).map(([x, y]) => at([x, y, top + 1.2])),
      { size: 0.38, tone: "lo" },
    ),
  ];
  return hit(id, parts.join(""));
}

function cableRun(plan: Plan, z: number, h: number): string {
  return S(k.slabOf(plan, z, h, P, 2), { tone: "lo", crease: "none" });
}

function clipOn(plan: Plan, z: number): string {
  return S(k.slabOf(plan, z, 1.4, P, 2), { tone: "mid", crease: "none" });
}

function elbow(x: number, y: number, z: number): string {
  return S(k.slabOf({ x: x - 0.7, y: y - 0.7, w: CABLE + 1.4, d: CABLE + 1.4, r: 0.8 }, z, 8, P, 2), { tone: "mid", crease: "none" });
}

const deckCableY = DECK_Y - CABLE / 2;

function pdfRoute(): string {
  return [
    moduleBox(
      { x: PDF_MOD_X1 - 28, y: DECK_Y - 14, w: 28, d: 28, r: 1.3 },
      "pdf",
      [PDF_MOD_X1 - 14, DECK_Y],
      { x: PDF_MOD_X1, y: DECK_Y - 2.2, w: BOSS, d: 4.4, r: 0.4 },
    ),
    cableRun({ x: PDF_LOW_X0, y: deckCableY, w: PDF_LOW_X1 - PDF_LOW_X0, d: CABLE, r: 1.1 }, LOW_Z, LOW_H),
    clipOn({ x: (PDF_LOW_X0 + PDF_LOW_X1) / 2 - 2.2, y: deckCableY - 0.5, w: 4.4, d: CABLE + 1, r: 0.4 }, LOW_Z + LOW_H),
    cableRun({ x: PDF_RISER_X0, y: deckCableY, w: CABLE, d: CABLE, r: 1.1 }, LOW_Z, DECK_CABLE_Z - LOW_Z),
    elbow(PDF_RISER_X0 + CABLE / 2, DECK_Y, LOW_Z - 1),
    elbow(PDF_RISER_X0 + CABLE / 2, DECK_Y, DECK_CABLE_Z - 2),
  ].join("");
}

function jpaRoute(): string {
  return [
    cableRun({ x: JPA_HIGH_X0, y: deckCableY, w: JPA_HIGH_X1 - JPA_HIGH_X0, d: CABLE, r: 1.1 }, DECK_CABLE_Z, DECK_CABLE_H),
    cableRun({ x: JPA_RISER_X0, y: deckCableY, w: CABLE, d: CABLE, r: 1.1 }, LOW_Z, DECK_CABLE_Z - LOW_Z),
    elbow(JPA_RISER_X0 + CABLE / 2, DECK_Y, LOW_Z - 1),
    elbow(JPA_RISER_X0 + CABLE / 2, DECK_Y, DECK_CABLE_Z - 2),
    cableRun({ x: JPA_LOW_X0, y: deckCableY, w: JPA_LOW_X1 - JPA_LOW_X0, d: CABLE, r: 1.1 }, LOW_Z, LOW_H),
    clipOn({ x: (JPA_LOW_X0 + JPA_LOW_X1) / 2 - 2.2, y: deckCableY - 0.5, w: 4.4, d: CABLE + 1, r: 0.4 }, LOW_Z + LOW_H),
    moduleBox(
      { x: JPA_MOD_X0, y: DECK_Y - 14, w: 30, d: 28, r: 1.3 },
      "jpa",
      [JPA_MOD_X0 + 15, DECK_Y],
      { x: JPA_MOD_X0 - BOSS, y: DECK_Y - 2.2, w: BOSS, d: 4.4, r: 0.4 },
    ),
  ].join("");
}

function httpRoute(): string {
  const cable: Plan = { x: HTTP_X - CABLE / 2, y: HTTP_CABLE_Y0, w: CABLE, d: HTTP_CABLE_Y1 - HTTP_CABLE_Y0, r: 1.1 };
  return [
    cableRun(cable, 31, 6),
    clipOn({ x: HTTP_X - 2.4, y: (HTTP_CABLE_Y0 + HTTP_CABLE_Y1) / 2 - 1.6, w: 4.8, d: 3.2, r: 0.4 }, 37),
    moduleBox(
      { x: HTTP_X - 16, y: HTTP_MOD_Y, w: 32, d: 18, r: 1.3 },
      "http",
      [HTTP_X, HTTP_MOD_Y + 10],
      { x: HTTP_X - 2.2, y: HTTP_MOD_Y - BOSS, w: 4.4, d: BOSS, r: 0.4 },
    ),
  ].join("");
}

function dustCap(): string {
  return [
    S(k.cylinder(32, 48, 4.4, PLINTH_TOP, 1.3, P, 22), { tone: "lo" }),
    S(k.cylinder(32, 48, 3.1, PLINTH_TOP + 1.3, 4.2, P, 20, 0.25), { tone: "mid" }),
    L(k.ring(32, 48, 1.6, PLINTH_TOP + 5.5, P, 16), { tone: "lo" }),
  ].join("");
}

export type PlugSpec = { id: PlugId; art: string; travel: number; vector: [number, number] };
export type ArtChunk = { html: string } | { plug: PlugId };

const httpShellPlan: Plan = { x: HTTP_X - SHELL_W / 2, y: HTTP_SHELL_Y, w: SHELL_W, d: SHELL_D, r: 1 };
const pdfShellPlan: Plan = { x: PDF_SHELL_X0, y: DECK_Y - SHELL_W / 2, w: SHELL_D, d: SHELL_W, r: 1 };
const jpaShellPlan: Plan = { x: JPA_SHELL_X0, y: DECK_Y - SHELL_W / 2, w: SHELL_D, d: SHELL_W, r: 1 };

export function junctionArt(): { chunks: ArtChunk[]; plugs: PlugSpec[] } {
  const plugs: PlugSpec[] = [
    { id: "pdf", art: shell(pdfShellPlan, 59, 6, "pdf", false), travel: UNMATE, vector: k.axisVector(P, [-1, 0, 0]) },
    { id: "jpa", art: shell(jpaShellPlan, 59, 6, "jpa", false), travel: UNMATE, vector: k.axisVector(P, [1, 0, 0]) },
    { id: "http", art: shell(httpShellPlan, 28, 12, "http", true), travel: UNMATE, vector: k.axisVector(P, [0, 1, 0]) },
  ];
  const chunks: ArtChunk[] = [
    {
      html: [
        plinth(),
        pdfRoute(),
        yoke(),
        sealedCase(),
        cableRun({ x: PDF_HIGH_X0, y: deckCableY, w: PDF_HIGH_X1 - PDF_HIGH_X0, d: CABLE, r: 1.1 }, DECK_CABLE_Z, DECK_CABLE_H),
      ].join(""),
    },
    { plug: "pdf" },
    { html: deckGland(PDF_X, "pdf") + deckGland(JPA_X, "jpa") },
    { plug: "jpa" },
    { html: jpaRoute() + frontGland(HTTP_X, "http", false) + frontGland(OPEN_X, "open", true) + dustCap() },
    { plug: "http" },
    { html: httpRoute() },
  ];
  return { chunks, plugs };
}

export function solidCount(): number {
  const art = junctionArt();
  const html = art.chunks.map((chunk) => ("html" in chunk ? chunk.html : "")).join("") + art.plugs.map((plug) => plug.art).join("");
  return html.match(/class="iso-solid/g)?.length ?? 0;
}

export function plugVector(id: PlugId): [number, number] {
  const plug = junctionArt().plugs.find((item) => item.id === id);
  return plug ? plug.vector : [0, 0];
}
