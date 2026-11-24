/**
 * The architecture views on Pages (#2483, ADR-033). architecture-model.json is generated from the LikeC4 model in
 * docs/200-architecture/210-architecture-model by `npm run generate`; never edit it by hand.
 */
import data from "./architecture-model.json";

export type ModelViewData = { id: string; title: string; description: string; type: string; mermaid: string };
export type ModelFinding = { kind: string; id: string; title: string; description: string; issue: string };

export const MODEL_VIEWS: ModelViewData[] = data.views;
export const MODEL_FINDINGS: ModelFinding[] = data.findings;
export const LIKEC4_VERSION: string = data.likec4;

/** The interactive LikeC4 site, built into the export by `npm run build:model` (hash routing, see ADR-033). */
export const MODEL_SITE_HREF = "/architecture-model/";

export const MODEL_GROUPS: { label: string; views: string[] }[] = [
  { label: "Context and containers", views: ["index", "containers"] },
  { label: "Inside the hexagons", views: ["backend", "frontend", "apiRequest"] },
  { label: "Security", views: ["security", "loginMfa", "securityPipeline"] },
  { label: "Testing and delivery", views: ["testing", "delivery"] },
  { label: "Infrastructure", views: ["dev", "prod", "staging", "observability"] },
  { label: "Findings", views: ["findings"] },
];

export function modelView(id: string): ModelViewData {
  const view = MODEL_VIEWS.find((v) => v.id === id);
  if (!view) throw new Error(`architecture model has no view '${id}'`);
  return view;
}

export function modelViewHref(id: string): string {
  return `${MODEL_SITE_HREF}#/view/${id}/`;
}
