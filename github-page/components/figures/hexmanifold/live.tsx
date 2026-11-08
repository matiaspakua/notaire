"use client";

import * as React from "react";
import { follow, useInView, useLoop, useReadout, useStillness } from "@/components/iso/react/live";
import type { PlugPiece } from "./geometry";

type Props = {
  width: number;
  height: number;
  label: string;
  base: string;
  plugs: PlugPiece[];
  readouts: string[];
};

const TOUR_EVERY = 3.4;
const IDLE_BEFORE_TOUR = 6.5;

const translate = (vector: [number, number], by: number) =>
  `translate(${(vector[0] * by).toFixed(2)} ${(vector[1] * by).toFixed(2)})`;

/**
 * One plug seats at a time. Choosing another retracts the open one first so two never overlap
 * mid-motion; the seated plug is painted last because it is nearest the core collar.
 */
export function HexManifoldLive({ width, height, label, base, plugs, readouts }: Props) {
  const groups = React.useRef<(SVGGElement | null)[]>([]);
  const seats = React.useRef(plugs.map((p) => (p.index === 0 ? p.seat : 0)));
  const chosen = React.useRef(0);
  const idle = React.useRef(IDLE_BEFORE_TOUR);
  const clock = React.useRef(0);
  const [front, setFront] = React.useState(0);
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
          chosen.current = (chosen.current + 1) % plugs.length;
          setSelected(chosen.current);
        }
      }
      const want = chosen.current;
      const othersOut = seats.current.every((s, i) => i === want || s < 0.4);
      let moving = false;
      let nearest = -1;
      plugs.forEach((p, i) => {
        const target = i === want && othersOut ? p.seat : 0;
        const next = follow(seats.current[i], target, dt, 0.22, still);
        const settled = Math.abs(next - target) < 0.05 ? target : next;
        if (settled !== seats.current[i]) {
          seats.current[i] = settled;
          groups.current[i]?.setAttribute("transform", translate(p.vector, settled));
        }
        if (settled !== target) moving = true;
        if (settled > 0.4) nearest = i;
      });
      if (nearest >= 0) setFront((current) => (current === nearest ? current : nearest));
      return moving || (!still && inView);
    },
    [plugs, inView],
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
    const hit = (event.target as Element).closest("[data-plug]");
    if (hit) choose(Number(hit.getAttribute("data-plug")));
  };

  const onKey = (event: React.KeyboardEvent) => {
    const here = chosen.current;
    const last = plugs.length - 1;
    const moves: Record<string, number> = {
      ArrowRight: (here + 1) % plugs.length,
      ArrowLeft: (here + last) % plugs.length,
      Home: 0,
      End: last,
    };
    if (!(event.key in moves)) return;
    event.preventDefault();
    choose(moves[event.key]);
  };

  const order = [...plugs.filter((_, i) => i !== front), ...plugs.filter((_, i) => i === front)];

  return (
    <div
      ref={anchor}
      className="iso-stage hexmanifold-stage"
      tabIndex={0}
      role="slider"
      aria-label="Hexagonal port manifold: arrow keys seat the next plug"
      aria-valuemin={1}
      aria-valuemax={plugs.length}
      aria-valuenow={selected + 1}
      aria-valuetext={readouts[selected]}
      data-calm={calm ? "" : undefined}
      onPointerOver={onPointer}
      onClick={onPointer}
      onKeyDown={onKey}
    >
      <svg className="iso-svg" viewBox={`0 0 ${width} ${height}`} role="img" aria-label={label} focusable="false">
        <g dangerouslySetInnerHTML={{ __html: base }} />
        {order.map((p) => (
          <g
            key={p.index}
            data-plug={p.index}
            data-seated={p.index === selected ? "" : undefined}
            ref={(node) => {
              groups.current[p.index] = node;
            }}
            transform={translate(p.vector, p.index === 0 ? p.seat : 0)}
            dangerouslySetInnerHTML={{ __html: p.art }}
          />
        ))}
      </svg>
    </div>
  );
}
