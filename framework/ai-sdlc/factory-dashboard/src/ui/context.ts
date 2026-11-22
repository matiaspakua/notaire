import { createContext, useContext } from "react";

import type { Grant, WorkspaceInfo } from "./api.ts";

export interface WorkspaceContext {
  workspace: WorkspaceInfo;
  grant: Grant;
  writable: boolean;
  go: (path: string) => void;
  expire: () => void;
  version: number;
  bumpVersion: () => void;
}

export const Current = createContext<WorkspaceContext | null>(null);

export function useWorkspace(): WorkspaceContext {
  const value = useContext(Current);
  if (!value) {
    throw new Error("useWorkspace outside a signed-in workspace");
  }
  return value;
}
