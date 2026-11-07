import { Plate } from "@/components/iso/react/draw";
import "@/components/iso/iso.css";
import "../figures.css";
import type { ArtifactFacts } from "@/lib/artifact-facts";
import { STAGES } from "@/lib/artifacts-content";
import { CabinetLive } from "./live";
import { cabinetArt, HEIGHT, PULL_VECTOR, WIDTH } from "./geometry";

const fmt = (n: number | null) => (n === null ? "—" : n.toLocaleString("en-US"));

/**
 * Fig 1 of the Engineering artifacts page: the artifact cabinet. Its nine drawers are the nine
 * stages in `STAGES`, and each card stack is built from the same count the page prints.
 */
export function ArtifactCabinet({ facts }: { facts: ArtifactFacts }) {
  const counts = STAGES.map((s) => facts[s.drawerCount]);
  const { base, drawers } = cabinetArt(counts);
  const readouts = STAGES.map((s, i) => `${s.n}/9 ${s.drawer} · ${fmt(counts[i])} ${s.drawerUnit}`);
  return (
    <div className="iso" data-theme="light">
      <Plate
        fig="Fig 1"
        title="The artifact cabinet"
        hint="Hover a drawer, or use the arrow keys"
        readout={readouts[0]}
        keys={[
          { mark: "lit", label: "The front card: the stage in the open drawer" },
          { mark: "raised", label: "Three cards per doubling of the file count" },
        ]}
        caption="An index-card cabinet with one drawer per stage of the artifact chain, read left to right and top to bottom: requirements, use cases, design, architecture, specifications, code, tests, security and operation. Every drawer's card stack is built from the count in the readout, read from the repository when the site was built; a rod runs through each stack the way a traceability ID runs through every artifact of a stage."
      >
        <CabinetLive
          width={WIDTH}
          height={HEIGHT}
          label="An index-card cabinet on a plinth, with nine drawers in three rows. One drawer is pulled out and shows a stack of index cards on a rod; the front card is highlighted."
          base={base}
          drawers={drawers.map(({ index, art, clip, pull }) => ({ index, art, clip, pull }))}
          readouts={readouts}
          vector={[PULL_VECTOR[0], PULL_VECTOR[1]]}
        />
      </Plate>
    </div>
  );
}
