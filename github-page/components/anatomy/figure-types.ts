import type { LegendKey } from "@/components/iso/iso-kit";

/** One step of a figure: what the highlighted station is, in plain words, and how the moving parts sit. */
export interface FigureStep {
  /** Station number, matching the numbered disc stamped on the machine. */
  station: number;
  title: string;
  /** Plain-language explanation shown under the figure. */
  text: string;
  /** Where to look in the repository for the real thing. */
  where: string;
  /** Short live readout at the bottom right of the card. */
  readout: string;
  /** Pose of the moving parts for this step, keyed by `data-part`. */
  pose: Record<string, { transform?: string; d?: string; opacity?: number }>;
}

export interface FigureData {
  id: string;
  fig: string;
  title: string;
  hint: string;
  /** Accessible description of the whole drawing. */
  label: string;
  caption: string;
  width: number;
  height: number;
  /** Static SVG markup with `data-station` groups and `data-part` moving parts at rest. */
  svg: string;
  keys: LegendKey[];
  steps: FigureStep[];
}
