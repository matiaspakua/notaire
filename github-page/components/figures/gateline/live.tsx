"use client";

import * as React from "react";
import type { Projection } from "@/components/iso/iso-kit";
import { follow, useInView, useLoop, useReadout, useStillness } from "@/components/iso/react/live";
import { boomPaths } from "./boom";

export type GateArt = { gate: number; x: number; stop: number; far: string; lamps: string; near: string; hub: string };
export type StopInfo = { x: number; label: string };

type Props = {
  width: number;
  height: number;
  label: string;
  base: string;
  carriage: string;
  gates: GateArt[];
  stops: StopInfo[];
  P: Projection;
  carVector: [number, number];
  carHalf: number;
  gap: number;
};

const UP = Math.PI / 2 - 0.08;
const STEP_EVERY = 1.25;
const HOLD_AT_END = 3.5;
const IDLE_BEFORE_TOUR = 6;
const CHECK = 0.45;

/**
 * The gated line's live part. The carriage eases toward the chosen step but is held in front of
 * any boom that is not yet up; a boom starts to lift CHECK seconds after the carriage arrives
 * (the gate's check running) and lowers again once the carriage backs off past its step. Paint
 * order follows the model: a boom that is down is in front of a carriage still waiting behind
 * it, a raised boom stands behind the track, and a rest fork is in front of anything left of it.
 */
export function GateLineLive({ width, height, label, base, carriage, gates, stops, P, carVector, carHalf, gap }: Props) {
  const layer = React.useRef<SVGGElement>(null);
  const car = React.useRef<SVGGElement>(null);
  const far = React.useRef<(SVGGElement | null)[]>([]);
  const boom = React.useRef<(SVGGElement | null)[]>([]);
  const near = React.useRef<(SVGGElement | null)[]>([]);
  const lamps = React.useRef<(SVGGElement | null)[]>([]);
  const model = React.useRef({ x: stops[0].x, target: 0, angles: gates.map(() => 0), waited: gates.map(() => 0), order: "", clock: 0, idle: IDLE_BEFORE_TOUR, end: 0 });
  const [chosen, setChosen] = React.useState(0);
  const calm = useStillness();
  const { anchor, say } = useReadout<HTMLDivElement>();
  const inView = useInView(anchor);

  const describe = React.useCallback(
    (x: number, angles: number[]) => {
      let here = 0;
      stops.forEach((s, i) => {
        if (Math.abs(s.x - x) < Math.abs(stops[here].x - x)) here = i;
      });
      const gate = gates.find((g) => g.stop === here);
      let state = "";
      if (gate) {
        const angle = angles[gates.indexOf(gate)];
        state = angle >= UP ? " · open" : angle > 0.02 ? " · lifting" : Math.abs(x - stops[here].x) < 0.6 ? " · checking" : " · closed";
      }
      return stops[here].label + state;
    },
    [stops, gates],
  );

  const tick = React.useCallback(
    (dt: number, still: boolean) => {
      const m = model.current;
      m.idle += dt;
      if (!still && m.idle > IDLE_BEFORE_TOUR) {
        const atEnd = m.target === stops.length - 1;
        m.clock += dt;
        if (atEnd && Math.abs(m.x - stops[m.target].x) < 0.5) {
          m.end += dt;
          if (m.end > HOLD_AT_END) {
            m.end = 0;
            m.clock = 0;
            m.target = 0;
            setChosen(0);
          }
        } else if (!atEnd && m.clock > STEP_EVERY && Math.abs(m.x - stops[m.target].x) < 0.5) {
          m.clock = 0;
          m.target += 1;
          setChosen(m.target);
        }
      }
      // Hold the carriage in front of the first boom ahead that is not up.
      let goal = stops[m.target].x;
      for (let i = 0; i < gates.length; i += 1) {
        const stopAt = gates[i].x - carHalf - gap;
        if (m.x <= stopAt + 0.01 && goal > stopAt && m.angles[i] < UP) {
          goal = stopAt;
          break;
        }
      }
      m.x = follow(m.x, goal, dt, 0.32, still);
      if (Math.abs(m.x - goal) < 0.02) m.x = goal;
      let moving = m.x !== goal || m.x !== stops[m.target].x;
      gates.forEach((g, i) => {
        const stopAt = g.x - carHalf - gap;
        const arrived = m.x >= stopAt - 0.3;
        const backedOff = m.x < stopAt - 4;
        if (arrived) m.waited[i] += dt;
        if (backedOff) m.waited[i] = 0;
        const want = arrived && (still || m.waited[i] > CHECK) ? Math.PI / 2 : backedOff ? 0 : m.angles[i];
        const next = follow(m.angles[i], want, dt, 0.28, still);
        const angle = Math.abs(next - want) < 0.004 ? want : next;
        if (angle !== m.angles[i]) {
          m.angles[i] = angle;
          const paths = boomPaths(P, g.x, angle);
          const node = boom.current[i];
          node?.querySelector("[data-boom=outline]")?.setAttribute("d", paths.outline);
          node?.querySelector("[data-boom=fill]")?.setAttribute("d", paths.outline);
          node?.querySelector("[data-boom=side]")?.setAttribute("d", paths.side);
          node?.querySelector("[data-boom=bands]")?.setAttribute("d", paths.bands);
          node?.querySelector("[data-boom=hub]")?.setAttribute("opacity", angle > 0.6 ? "0" : "1");
        }
        if (angle !== want || (arrived && m.waited[i] <= CHECK && !still)) moving = true;
        lamps.current[i]?.setAttribute("data-tone", angle >= UP ? "hi" : "lo");
      });
      car.current?.setAttribute("transform", `translate(${(carVector[0] * m.x).toFixed(2)} ${(carVector[1] * m.x).toFixed(2)})`);
      // Paint order: far halves, then booms and forks that sit behind the carriage, the carriage, the rest.
      const behind: Element[] = [];
      const front: Element[] = [];
      gates.forEach((g, i) => {
        const down = m.angles[i] < 1.2;
        const b = boom.current[i];
        const n = near.current[i];
        if (b) (down && m.x < g.x ? front : behind).push(b);
        if (n) (m.x < g.x + 17 ? front : behind).push(n);
      });
      const order = [...far.current.filter(Boolean), ...behind, car.current, ...front] as Element[];
      const signature = order.map((el) => el.getAttribute("data-key")).join(",");
      if (signature !== m.order && layer.current) {
        m.order = signature;
        for (const el of order) layer.current.appendChild(el);
      }
      say(describe(m.x, m.angles));
      return moving || (!still && inView);
    },
    [stops, gates, carHalf, gap, P, carVector, say, describe, inView],
  );
  const kick = useLoop(tick, inView);

  const choose = React.useCallback(
    (index: number) => {
      const m = model.current;
      m.idle = 0;
      m.clock = 0;
      m.end = 0;
      m.target = Math.max(0, Math.min(stops.length - 1, index));
      setChosen(m.target);
      kick();
    },
    [stops.length, kick],
  );

  const onPointer = (event: React.PointerEvent) => {
    const box = anchor.current?.getBoundingClientRect();
    if (!box) return;
    const share = Math.min(1, Math.max(0, ((event.clientX - box.left) / box.width - 0.12) / 0.76));
    choose(Math.round(share * (stops.length - 1)));
  };

  const onKey = (event: React.KeyboardEvent) => {
    const here = model.current.target;
    const moves: Record<string, number> = { ArrowRight: here + 1, ArrowUp: here + 1, ArrowLeft: here - 1, ArrowDown: here - 1, Home: 0, End: stops.length - 1 };
    if (!(event.key in moves)) return;
    event.preventDefault();
    choose(moves[event.key]);
  };

  const initial = gates.map((g) => boomPaths(P, g.x, 0));
  const startX = stops[0].x;

  return (
    <div
      ref={anchor}
      className="iso-stage gateline-stage"
      tabIndex={0}
      role="slider"
      aria-label="The AI SDLC line: arrow keys move the change one step; a gate opens only when the change reaches it"
      aria-valuemin={1}
      aria-valuemax={stops.length}
      aria-valuenow={chosen + 1}
      aria-valuetext={stops[chosen].label}
      data-calm={calm ? "" : undefined}
      onPointerMove={onPointer}
      onPointerDown={onPointer}
      onKeyDown={onKey}
    >
      <svg className="iso-svg" viewBox={`0 0 ${width} ${height}`} role="img" aria-label={label} focusable="false">
        <g dangerouslySetInnerHTML={{ __html: base }} />
        <g ref={layer}>
          {gates.map((g, i) => (
            <g key={`far-${g.gate}`} data-key={`far-${g.gate}`} ref={(node) => { far.current[i] = node; }}>
              <g dangerouslySetInnerHTML={{ __html: g.far }} />
              <g className="iso-dots" data-tone="lo" ref={(node) => { lamps.current[i] = node; }} dangerouslySetInnerHTML={{ __html: g.lamps }} />
            </g>
          ))}
          {gates.map((g, i) => (
            <g key={`boom-${g.gate}`} data-key={`boom-${g.gate}`} ref={(node) => { boom.current[i] = node; }} className="iso-solid gl-boom">
              <path className="iso-fill" data-boom="fill" d={initial[i].outline} />
              <path className="iso-shade" data-shade="1" data-boom="side" d={initial[i].side} />
              <path className="iso-line" data-tone="mid" data-boom="bands" d={initial[i].bands} />
              <path className="iso-line iso-edge" data-tone="hi" data-boom="outline" d={initial[i].outline} />
              <path className="iso-line" data-tone="mid" data-boom="hub" d={g.hub} />
            </g>
          ))}
          <g key="car" data-key="car" ref={car} transform={`translate(${(carVector[0] * startX).toFixed(2)} ${(carVector[1] * startX).toFixed(2)})`} dangerouslySetInnerHTML={{ __html: carriage }} />
          {gates.map((g, i) => (
            <g key={`near-${g.gate}`} data-key={`near-${g.gate}`} ref={(node) => { near.current[i] = node; }} dangerouslySetInnerHTML={{ __html: g.near }} />
          ))}
        </g>
      </svg>
    </div>
  );
}
