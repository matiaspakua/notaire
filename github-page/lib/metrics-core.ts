/** Types and pure helpers safe for client components (no Node fs). */

export interface CoverageSnapshot {
  /** JSON null when the generator had no measured value (#2203). */
  backendInstruction: number | null;
  backendBranch: number | null;
  frontendComponent: number | null;
  e2eTests: number | null;
  apiEndpoints: number | null;
  lastUpdated: string;
}

export interface SiteMetrics {
  generatedAt: string;
  commits: number;
  pullRequests: number;
  markdownFiles: number;
  testClasses: number;
  backendTestClasses: number;
  frontendTestClasses: number;
  /** Playwright specs under testing/e2e (issue #1982). */
  e2eSpecFiles: number;
  apiTestFiles: number;
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

/** True when a coverage snapshot was present. "unknown" / null percentages mean unmeasured — never read as 0%. */
export function coverageIsMeasured(coverage: CoverageSnapshot): boolean {
  return (
    coverage.lastUpdated !== "unknown" &&
    coverage.lastUpdated.trim() !== "" &&
    coverage.backendInstruction !== null &&
    coverage.backendBranch !== null
  );
}

/** Format a coverage percentage for display; null / unmeasured → "n/a". */
export function formatCoveragePercent(value: number | null, measured: boolean): string {
  if (!measured || value === null) {
    return "n/a";
  }
  return `${value}%`;
}
