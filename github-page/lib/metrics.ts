import fs from "fs";
import path from "path";

export interface CoverageSnapshot {
  backendInstruction: number;
  backendBranch: number;
  frontendComponent: number;
  e2eTests: number;
  apiEndpoints: number;
  lastUpdated: string;
}

export interface SiteMetrics {
  generatedAt: string;
  commits: number;
  pullRequests: number;
  markdownFiles: number;
  testClasses: number;
  adrCount: number;
  coverage: CoverageSnapshot;
}

const DEFAULTS: SiteMetrics = {
  generatedAt: new Date().toISOString(),
  commits: 449,
  pullRequests: 159,
  markdownFiles: 577,
  testClasses: 94,
  adrCount: 11,
  coverage: {
    backendInstruction: 31,
    backendBranch: 18,
    frontendComponent: 45,
    e2eTests: 85,
    apiEndpoints: 78,
    lastUpdated: "2026-06-16",
  },
};

export function loadMetrics(): SiteMetrics {
  try {
    const filePath = path.join(process.cwd(), "public", "metrics.json");
    const raw = fs.readFileSync(filePath, "utf-8");
    const parsed = JSON.parse(raw) as Partial<SiteMetrics>;
    return {
      ...DEFAULTS,
      ...parsed,
      coverage: { ...DEFAULTS.coverage, ...(parsed.coverage ?? {}) },
    };
  } catch {
    return DEFAULTS;
  }
}
