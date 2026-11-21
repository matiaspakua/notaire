import { createContext, useContext } from "react";

import type { Grant, WorkspaceInfo } from "./api.ts";

export interface WorkspaceContext {
  workspace: WorkspaceInfo;
  grant: Grant;
  /** The kit runs with --write and the token's scope allows writes. */
  writable: boolean;
  go: (path: string) => void;
  /** Ask the server to re-check the session after a 401 from the kit. */
  expire: () => void;
}

export const Current = createContext<WorkspaceContext | null>(null);

export function useWorkspace(): WorkspaceContext {
  const value = useContext(Current);
  if (!value) {
    throw new Error("useWorkspace outside a signed-in workspace");
  }
  return value;
}
