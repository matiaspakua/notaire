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
  useCases: number;
  functionalRequirements: number;
  dockerServices: number;
  grafanaDashboards: number;
  aiTools: number;
  contributors: number;
  firstCommitDate: string;
  coverage: CoverageSnapshot;
}

const DEFAULTS: SiteMetrics = {
  generatedAt: new Date().toISOString(),
  commits: 474,
  pullRequests: 30,
  markdownFiles: 193,
  testClasses: 122,
  adrCount: 12,
  useCases: 78,
  functionalRequirements: 94,
  dockerServices: 12,
  grafanaDashboards: 4,
  aiTools: 4,
  contributors: 3,
  firstCommitDate: "2014-03-27",
  coverage: {
    backendInstruction: 32,
    backendBranch: 19,
    frontendComponent: 45,
    e2eTests: 85,
    apiEndpoints: 78,
    lastUpdated: "2026-06-18",
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
