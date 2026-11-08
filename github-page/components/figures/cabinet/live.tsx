"use client";

import * as React from "react";
import { follow, useInView, useLoop, useReadout, useStillness } from "@/components/iso/react/live";

export type CabinetDrawer = { index: number; art: string; clip: string; pull: number };

type Props = {
  width: number;
  height: number;
  label: string;
  base: string;
  drawers: CabinetDrawer[];
  /** One readout line per drawer, in drawer order. */
  readouts: string[];
  /** Screen offset of one unit of pull. */
  vector: [number, number];
};

const TOUR_EVERY = 3.2;
const IDLE_BEFORE_TOUR = 7;
const COLS = 3;

const translate = (vector: [number, number], by: number) => `translate(${(vector[0] * by).toFixed(2)} ${(vector[1] * by).toFixed(2)})`;

/**
 * The cabinet's live part: one drawer is out at a time. Choosing another closes the open one
 * first, then opens the new one, so two drawers never overlap mid-motion; the drawer that is out
 * is painted last because it is nearest the viewer. Pulls ease toward their targets and are
 * written straight to the DOM; React renders only when the drawer that is out changes.
 */
export function CabinetLive({ width, height, label, base, drawers, readouts, vector }: Props) {
  const groups = React.useRef<(SVGGElement | null)[]>([]);
  const pulls = React.useRef(drawers.map((d) => (d.index === 0 ? d.pull : 0)));
  const chosen = React.useRef(0);
  const idle = React.useRef(IDLE_BEFORE_TOUR);
  const clock = React.useRef(0);
  const [out, setOut] = React.useState(0);
  const [selected, setSelected] = React.useState(0);
  const calm = useStillness();
  const { anchor, say } = useReadout<HTMLDivElement>();
  const inView = useInView(anchor);

  const tick = React.useCallback(
    (dt: number, still: boolean) => {
      idle.current += dt;
      if (!still && idle.current > IDLE_BEFORE_TOUR) {
        clock.current += dt;
        if (clock.current > TOUR_EVERY) {
          clock.current = 0;
          chosen.current = (chosen.current + 1) % drawers.length;
          setSelected(chosen.current);
        }
      }
      const want = chosen.current;
      const othersShut = pulls.current.every((p, i) => i === want || p < 0.4);
      let moving = false;
      let front = -1;
      drawers.forEach((d, i) => {
        const target = i === want && othersShut ? d.pull : 0;
        const next = follow(pulls.current[i], target, dt, 0.2, still);
        const settled = Math.abs(next - target) < 0.05 ? target : next;
        if (settled !== pulls.current[i]) {
          pulls.current[i] = settled;
          groups.current[i]?.setAttribute("transform", translate(vector, settled));
        }
        if (settled !== target) moving = true;
        if (settled > 0.4) front = i;
      });
      if (front >= 0) setOut((current) => (current === front ? current : front));
      return moving || (!still && inView);
    },
    [drawers, vector, inView],
  );
  const kick = useLoop(tick, inView);

  React.useEffect(() => {
    say(readouts[selected]);
  }, [selected, readouts, say]);

  const choose = React.useCallback(
    (index: number) => {
      idle.current = 0;
      clock.current = 0;
      if (chosen.current === index) return;
      chosen.current = index;
      setSelected(index);
      kick();
    },
    [kick],
  );

  const onPointer = (event: React.PointerEvent | React.MouseEvent) => {
    const hit = (event.target as Element).closest("[data-drawer]");
    if (hit) choose(Number(hit.getAttribute("data-drawer")));
  };

  const onKey = (event: React.KeyboardEvent) => {
    const here = chosen.current;
    const last = drawers.length - 1;
    const moves: Record<string, number> = {
      ArrowRight: Math.min(last, here + 1),
      ArrowLeft: Math.max(0, here - 1),
      ArrowDown: Math.min(last, here + COLS),
      ArrowUp: Math.max(0, here - COLS),
      Home: 0,
      End: last,
    };
    if (!(event.key in moves)) return;
    event.preventDefault();
    choose(moves[event.key]);
  };

  const order = [...drawers.filter((d) => d.index !== out), ...drawers.filter((d) => d.index === out)];

  return (
    <div
      ref={anchor}
      className="iso-stage cabinet-stage"
      tabIndex={0}
      role="slider"
      aria-label="Artifact cabinet: arrow keys open the next drawer"
      aria-valuemin={1}
      aria-valuemax={drawers.length}
      aria-valuenow={selected + 1}
      aria-valuetext={readouts[selected]}
      data-calm={calm ? "" : undefined}
      onPointerOver={onPointer}
      onClick={onPointer}
      onKeyDown={onKey}
    >
      <svg className="iso-svg" viewBox={`0 0 ${width} ${height}`} role="img" aria-label={label} focusable="false">
        <defs>
          {drawers.map((d) => (
            <clipPath key={d.index} id={`cab-open-${d.index}`}>
              <path d={d.clip} />
            </clipPath>
          ))}
        </defs>
        <g dangerouslySetInnerHTML={{ __html: base }} />
        {order.map((d) => (
          <g key={d.index} clipPath={`url(#cab-open-${d.index})`} data-drawer={d.index} data-open={d.index === selected ? "" : undefined}>
            <g
              ref={(node) => {
                groups.current[d.index] = node;
              }}
              transform={translate(vector, d.index === 0 ? d.pull : 0)}
              dangerouslySetInnerHTML={{ __html: d.art }}
            />
          </g>
        ))}
      </svg>
    </div>
  );
}
