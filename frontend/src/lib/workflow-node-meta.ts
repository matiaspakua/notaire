import { theme } from "@/theme/tokens";

export interface WorkflowNodeMeta {
  background: string;
  border: string;
  color: string;
  icon: string;
}

export const NODE_META: Record<string, WorkflowNodeMeta> = {
  INITIAL: {
    background: theme.colors.success[50],
    border: theme.colors.success[500],
    color: theme.colors.success[600],
    icon: "▶",
  },
  INTERMEDIATE: {
    background: theme.colors.primary[50],
    border: theme.colors.primary[500],
    color: theme.colors.primary[700],
    icon: "●",
  },
  FINAL: {
    background: theme.colors.error[50],
    border: theme.colors.error[500],
    color: theme.colors.error[700],
    icon: "■",
  },
};

export function getNodeMeta(nodeType?: string): WorkflowNodeMeta {
  return NODE_META[nodeType ?? "INTERMEDIATE"] ?? NODE_META.INTERMEDIATE;
}

export function withNodeIcon(label: string, nodeType?: string): string {
  return `${getNodeMeta(nodeType).icon} ${label}`;
}
