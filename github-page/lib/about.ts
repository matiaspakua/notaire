/**
 * About (#1985): read from lib/about.json, where every fact names its source and missing facts
 * are placeholders that the site never renders.
 */
import data from "./about.json";

export type AboutFact = { label: string; value: string; href?: string; source: { label: string; href: string } };
export type AboutData = { name: string; checked: string; facts: AboutFact[]; placeholders: { field: string; todo: string }[] };

export function loadAbout(): AboutData {
  return data as AboutData;
}

export function fact(label: string): AboutFact | undefined {
  return loadAbout().facts.find((f) => f.label === label);
}
