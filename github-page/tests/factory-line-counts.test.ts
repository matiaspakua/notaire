import { readFileSync } from "node:fs";
import path from "node:path";

import { describe, expect, it } from "vitest";

import { AGENTS_ON_LINE, STATIONS } from "@/lib/factory-line-content";

const ROOT = path.resolve(__dirname, "..");
const COPY = ["lib/site.ts", "lib/i18n.ts", "app/ai-sdlc/factory-line/page.tsx"];

/** #2475: the copy said 16 stations after a 17th (foreman-claim) was added; numbers follow the data. */
describe("factory line copy matches its data", () => {
  const counts: Record<string, number> = {
    "stations|estaciones": STATIONS.length,
    "agents|agentes": AGENTS_ON_LINE.length,
    "gates": STATIONS.filter((s) => "gate" in s && s.gate).length,
    "phases|fases": new Set(STATIONS.map((s) => s.phase)).size,
  };

  for (const file of COPY) {
    it(`every station, agent, gate and phase count in ${file}`, () => {
      const text = readFileSync(path.join(ROOT, file), "utf8");
      for (const [words, expected] of Object.entries(counts)) {
        for (const match of text.matchAll(new RegExp(`(\\d+) (?:hard )?(?:${words})\\b`, "g"))) {
          expect(Number(match[1]), `${file}: "${match[0]}"`).toBe(expected);
        }
      }
    });
  }
});
