import { Plate } from "@/components/iso/react/draw";
import "@/components/iso/iso.css";
import "../figures.css";
import { JunctionLive } from "./live";
import { HEIGHT, junctionArt, SOCKETS, WIDTH } from "./geometry";

export type JunctionFacts = {
  inPorts: number;
  outPorts: number;
  webAdapters: number;
  outAdapters: number;
};

/**
 * Fig 1 of the hexagonal architecture page. The sealed case is the domain; glands are
 * ports; plug modules are adapters. Counts in the caption are the same ones the page prints.
 */
export function JunctionInstrument({ facts }: { facts: JunctionFacts }) {
  const { chunks, plugs } = junctionArt();
  const readouts = SOCKETS.map((socket) => socket.readout);
  return (
    <div className="iso" data-theme="light">
      <Plate
        fig="Fig 1"
        title="The junction instrument"
        hint="Choose a gland, or use the arrow keys"
        readout={readouts[0]}
        keys={[
          { mark: "lit", label: "The gland in hand: its plug is backed off the socket" },
          { mark: "raised", label: "Sealed case: the domain, with no cable through the wall" },
          { mark: "edge", label: "A gland on the skin: the port the adapter is allowed to meet" },
        ]}
        caption={`A sealed brass case on a plinth. The case is the domain: every cable stops at a gland on the skin. Two glands on the front face are inbound ports; two on the rear of the lid are outbound ports. The HTTP plug mates to the left-hand inbound gland. The other inbound gland is open, its dust cap on the plinth, so the socket shows and nothing reaches the case. The JPA and PDF plugs mate only to the outbound glands. Backing a plug off its gland is the dependency, in metal: adapter → port → domain. The repository holds ${facts.inPorts} inbound port files, ${facts.outPorts} outbound port files, ${facts.webAdapters} inbound adapter files and ${facts.outAdapters} outbound adapter files.`}
      >
        <JunctionLive
          width={WIDTH}
          height={HEIGHT}
          label="A sealed brass junction case on a plinth. Cable glands stand on the front face and on the rear of the lid. Three plug modules sit beside the case on short cables; one inbound gland is open, with its dust cap on the plinth."
          chunks={chunks}
          plugs={plugs}
          readouts={readouts}
        />
      </Plate>
    </div>
  );
}
