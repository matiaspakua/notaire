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
    color: theme.colors.error[600],
    icon: "■",
  },
};

export function getNodeMeta(tipo?: string): WorkflowNodeMeta {
  return NODE_META[tipo ?? "INTERMEDIATE"] ?? NODE_META.INTERMEDIATE;
}

export function withNodeIcon(label: string, tipo?: string): string {
  return `${getNodeMeta(tipo).icon} ${label}`;
}
