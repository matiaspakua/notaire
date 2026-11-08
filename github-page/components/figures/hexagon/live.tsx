"use client";

import * as React from "react";
import { follow, useInView, useLoop, useReadout, useStillness } from "@/components/iso/react/live";
import { SOCKETS, type ArtChunk, type PlugSpec } from "./geometry";

type Props = {
  width: number;
  height: number;
  label: string;
  chunks: ArtChunk[];
  plugs: PlugSpec[];
  readouts: string[];
};

const TOUR_EVERY = 3.2;
const IDLE_BEFORE_TOUR = 6;

const translate = (vector: [number, number], by: number) => `translate(${(vector[0] * by).toFixed(2)} ${(vector[1] * by).toFixed(2)})`;

/**
 * One gland is in hand: its plug eases back along the cable, and the readout names that socket.
 * An idle tour walks the glands. Reduced motion snaps the plugs and leaves the tour still.
 */
export function JunctionLive({ width, height, label, chunks, plugs, readouts }: Props) {
  const svg = React.useRef<SVGSVGElement>(null);
  const groups = React.useRef<Record<string, SVGGElement | null>>({});
  const seats = React.useRef<Record<string, number>>(Object.fromEntries(plugs.map((plug) => [plug.id, plug.id === "http" ? plug.travel : 0])));
  const chosen = React.useRef(0);
  const idle = React.useRef(IDLE_BEFORE_TOUR);
  const clock = React.useRef(0);
  const [selected, setSelected] = React.useState(0);
  const calm = useStillness();
  const { anchor, say } = useReadout<HTMLDivElement>();
  const inView = useInView(anchor);
  const plugById = React.useMemo(() => Object.fromEntries(plugs.map((plug) => [plug.id, plug])), [plugs]);

  const tick = React.useCallback(
    (dt: number, still: boolean) => {
      idle.current += dt;
      if (!still && idle.current > IDLE_BEFORE_TOUR) {
        clock.current += dt;
        if (clock.current > TOUR_EVERY) {
          clock.current = 0;
          chosen.current = (chosen.current + 1) % SOCKETS.length;
          setSelected(chosen.current);
        }
      }
      const want = SOCKETS[chosen.current].id;
      let moving = false;
      for (const plug of plugs) {
        const target = plug.id === want ? plug.travel : 0;
        const next = follow(seats.current[plug.id] ?? 0, target, dt, 0.22, still);
        const settled = Math.abs(next - target) < 0.04 ? target : next;
        if (settled !== seats.current[plug.id]) {
          seats.current[plug.id] = settled;
          groups.current[plug.id]?.setAttribute("transform", translate(plug.vector, settled));
        }
        if (settled !== target) moving = true;
      }
      return moving || (!still && inView);
    },
    [plugs, inView],
  );
  const kick = useLoop(tick, inView);

  React.useEffect(() => {
    say(readouts[selected]);
    const root = svg.current;
    if (!root) return;
    const id = SOCKETS[selected].id;
    root.querySelectorAll<SVGGElement>("[data-accent]").forEach((node) => {
      const solid = node.querySelector(".iso-solid");
      if (!solid) return;
      if (node.getAttribute("data-accent") === id) solid.setAttribute("data-lit", "");
      else solid.removeAttribute("data-lit");
    });
  }, [selected, readouts, say]);

  const choose = React.useCallback(
    (index: number) => {
      const next = (index + SOCKETS.length) % SOCKETS.length;
      idle.current = 0;
      clock.current = 0;
      if (chosen.current === next) return;
      chosen.current = next;
      setSelected(next);
      kick();
    },
    [kick],
  );

  const onPointer = (event: React.PointerEvent | React.MouseEvent) => {
    const target = (event.target as Element).closest("[data-socket]");
    if (!target) return;
    const index = SOCKETS.findIndex((socket) => socket.id === target.getAttribute("data-socket"));
    if (index >= 0) choose(index);
  };

  const onKey = (event: React.KeyboardEvent) => {
    const here = chosen.current;
    const moves: Record<string, number> = {
      ArrowRight: here + 1,
      ArrowDown: here + 1,
      ArrowLeft: here - 1,
      ArrowUp: here - 1,
      Home: 0,
      End: SOCKETS.length - 1,
    };
    if (!(event.key in moves)) return;
    event.preventDefault();
    choose(moves[event.key]);
  };

  return (
    <div
      ref={anchor}
      className="iso-stage hexagon-stage"
      tabIndex={0}
      role="slider"
      aria-label="Junction instrument: arrow keys choose the next gland. A plug mates only to its socket."
      aria-valuemin={1}
      aria-valuemax={SOCKETS.length}
      aria-valuenow={selected + 1}
      aria-valuetext={readouts[selected]}
      data-calm={calm ? "" : undefined}
      onPointerOver={onPointer}
      onClick={onPointer}
      onKeyDown={onKey}
    >
      <svg ref={svg} className="iso-svg" viewBox={`0 0 ${width} ${height}`} role="img" aria-label={label} focusable="false">
        {chunks.map((chunk, index) => {
          if ("plug" in chunk) {
            const plug = plugById[chunk.plug];
            const atRest = plug.id === "http" ? plug.travel : 0;
            return (
              <g
                key={plug.id}
                data-socket={plug.id}
                data-plug={plug.id}
                ref={(node) => {
                  groups.current[plug.id] = node;
                }}
                transform={translate(plug.vector, atRest)}
                dangerouslySetInnerHTML={{ __html: plug.art }}
              />
            );
          }
          return <g key={index} dangerouslySetInnerHTML={{ __html: chunk.html }} />;
        })}
      </svg>
    </div>
  );
}
