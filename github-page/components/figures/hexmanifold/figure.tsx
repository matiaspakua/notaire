import { Plate } from "@/components/iso/react/draw";
import "@/components/iso/iso.css";
import "../figures.css";
import { HexManifoldLive } from "./live";
import { FACES, HEIGHT, manifoldArt, readoutFor, WIDTH } from "./geometry";

export type HexManifoldFacts = {
  domainContexts: number;
  inPorts: number;
  outPorts: number;
  webAdapters: number;
  outAdapters: number;
  baselineViolations: number;
};

/**
 * Fig 1 of the hexagonal architecture page: a sealed domain core with six keyed plugs.
 * Counts in the caption come from the same `hexagon()` facts the page prints.
 */
export function HexManifold({ facts }: { facts: HexManifoldFacts }) {
  const { base, plugs } = manifoldArt();
  const readouts = FACES.map((_, i) => readoutFor(i));
  return (
    <div className="iso" data-theme="light">
      <Plate
        fig="Fig 1"
        title="The port manifold"
        hint="Hover a plug, or use the arrow keys"
        readout={readouts[0]}
        keys={[
          { mark: "lit", label: "Domain core: sealed, depends on the JDK only" },
          { mark: "raised", label: "Keyed plug: an adapter or port that seats into one socket" },
          { mark: "edge", label: "Six faces: inbound, ports, outbound — never a shortcut through the core" },
        ]}
        caption={`A hexagonal instrument manifold on a plinth. The lit chamber is the domain (${facts.domainContexts} contexts). Around it, six keyed sockets stand for the ports and adapters: ${facts.inPorts} inbound port files and ${facts.outPorts} outbound port files, with ${facts.webAdapters} inbound and ${facts.outAdapters} outbound adapter files seating only through those sockets. ArchUnit keeps the baseline at ${facts.baselineViolations} known violations, raise-only shrink.`}
      >
        <HexManifoldLive
          width={WIDTH}
          height={HEIGHT}
          label="A hexagonal instrument manifold on a laboratory plinth. A sealed lit core sits in the centre; six keyed plug cartridges surround it, one seated into its socket."
          base={base}
          plugs={plugs}
          readouts={readouts}
        />
      </Plate>
    </div>
  );
}
