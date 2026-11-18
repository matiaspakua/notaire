import snap from "./repolens-bench-snapshot.json";

export type RetrievalArm = {
  recall_at_5: number;
  recall_at_10: number;
  recall_at_20: number;
  mrr: number;
  latency_ms: number;
  output_bytes: number;
};

export type AgentArm = {
  recall: number;
  tool_calls: number;
  turns: number;
  duration_s: number;
  cost_usd: number;
  input_tokens: number;
  output_tokens: number;
  runs: number;
};

export type FactoryPair = {
  issue: string;
  label: string;
  order: string;
  pass: string;
  calls: number;
  output_bytes: number;
  wall: string;
  hit_rate: string;
  named_files: string | null;
};

export type RepoLensBench = {
  generated_at: string;
  method: string;
  sources: { path: string }[];
  headlines: {
    tuning_recall: string;
    holdout_recall: string;
    latency: string;
    bytes: string;
    sonnet_tool_calls: string;
    sonnet_recall: string;
  };
  retrieval: {
    tuning: {
      path: string;
      taskset: string;
      commit: string;
      tasks: number;
      k: number;
      arms: { grep: RetrievalArm; hybrid: RetrievalArm; graph: RetrievalArm };
    };
    holdout: {
      path: string;
      taskset: string;
      commit: string;
      tasks: number;
      k: number;
      arms: { grep: RetrievalArm; hybrid: RetrievalArm; graph: RetrievalArm };
    };
  };
  agents: {
    haikuTuning: { path: string; model: string; taskset: string; commit: string; repeat: number; baseline: AgentArm; graph: AgentArm };
    haikuHoldout: { path: string; model: string; taskset: string; commit: string; repeat: number; baseline: AgentArm; graph: AgentArm };
    sonnetHoldout: { path: string; model: string; taskset: string; commit: string; repeat: number; baseline: AgentArm; graph: AgentArm };
    sonnetScale: { path: string; model: string; taskset: string; commit: string; repeat: number; baseline: AgentArm; graph: AgentArm };
  };
  factoryLoop: {
    pairs: FactoryPair[];
    implemented_issues_n: number | null;
    caveat: string;
  };
};

export function loadRepoLensBench(): RepoLensBench {
  return snap as RepoLensBench;
}

/** Labels already rounded by generate-repolens-bench.py (same rules as bench/report.py::_factor). */
export function headlineFactors(bench: RepoLensBench = loadRepoLensBench()) {
  return {
    tuningRecallLabel: bench.headlines.tuning_recall,
    holdoutRecallLabel: bench.headlines.holdout_recall,
    latencyLabel: bench.headlines.latency,
    bytesLabel: bench.headlines.bytes,
    sonnetToolCallsLabel: bench.headlines.sonnet_tool_calls,
    sonnetRecallLabel: bench.headlines.sonnet_recall,
  };
}

export function kb(bytes: number): number {
  return bytes / 1024;
}
