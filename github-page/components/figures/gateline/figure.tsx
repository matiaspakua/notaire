import { Plate } from "@/components/iso/react/draw";
import "@/components/iso/iso.css";
import "../figures.css";
import { GateLineLive } from "./live";
import { baseArt, CAR_VECTOR, carriageArt, gateFarArt, gateNearArt, GATES, HEIGHT, HUB, lampPoints, LIMITS, P, STOPS, WIDTH } from "./geometry";

const label = (s: (typeof STOPS)[number]) => `step ${s.n} · ${s.title}${s.gate ? ` · G${s.gate}` : ""}`;

/**
 * Fig 1 of the lifecycle page: the AI SDLC as a gated line. The stops are the Constitution's
 * steps from LIFECYCLE_PHASES and the booms are its five hard gates, at the steps that carry them.
 */
export function GateLine() {
  const stops = STOPS.map((s) => ({ x: s.x, label: label(s) }));
  const gates = GATES.map((g) => ({
    gate: g.gate,
    x: g.x,
    stop: STOPS.findIndex((s) => s.n === g.n),
    far: gateFarArt(g.x),
    lamps: lampPoints(g.x)
      .map(([x, y]) => `<circle cx="${x.toFixed(1)}" cy="${y.toFixed(1)}" r="0.9"/>`)
      .join(""),
    near: gateNearArt(g.x),
    hub: HUB(g.x),
  }));
  return (
    <div className="iso" data-theme="light">
      <Plate
        fig="Fig 1"
        title="The gated line"
        hint="Move along the line, or use the arrow keys"
        readout={stops[0].label}
        keys={[
          { mark: "lit", label: "The Issue card: one change on its way to production" },
          { mark: "raised", label: "Five booms: the Constitution's hard gates" },
          { mark: "edge", label: "One tick per step, 1 to 24" },
        ]}
        caption={`A change rides a carriage along the Constitution's ${STOPS.length} stops (24 numbered steps, some taken together). At steps ${GATES.map((g) => g.n).join(", ")} a boom is down: the carriage waits in front of it while the gate's check runs, and the boom lifts only when it passes. Gate 1 is the specification, Gate 2 the failing tests, Gate 3 the docs and the full local pipeline, Gate 4 the review and merge, Gate 5 the deploy and smoke test. The seams across the bench divide Plan, Prove, Publish and Ship.`}
      >
        <GateLineLive
          width={WIDTH}
          height={HEIGHT}
          label="A long test bench with two rails and a step ruler. A carriage holding a card runs along the rails past five barrier gates; each gate has a post, a lamp head, a striped boom and a rest fork."
          base={baseArt()}
          carriage={carriageArt()}
          gates={gates}
          stops={stops}
          P={P}
          carVector={[CAR_VECTOR[0], CAR_VECTOR[1]]}
          carHalf={LIMITS.carHalf}
          gap={LIMITS.gateGap}
        />
      </Plate>
    </div>
  );
}
