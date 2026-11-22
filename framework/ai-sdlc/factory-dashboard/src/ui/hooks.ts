import { useCallback, useContext, useEffect, useRef, useState } from "react";

import { api, ApiError, eventsUrl } from "./api.ts";
import { Current } from "./context.ts";

export interface Resource<T> {
  data: T | undefined;
  error: ApiError | undefined;
  loading: boolean;
  stale: boolean;
  updatedAt: Date | undefined;
  reload: () => void;
}

export function useResource<T>(path: string | null, poll?: number): Resource<T> {
  const ctx = useContext(Current);
  const version = ctx?.version ?? 0;
  const [state, setState] = useState<Omit<Resource<T>, "reload">>({
    data: undefined, error: undefined, loading: Boolean(path), stale: false, updatedAt: undefined,
  });
  const [tick, setTick] = useState(0);
  const current = useRef(path);
  current.current = path;
  const hasData = useRef(false);

  useEffect(() => {
    if (path) {
      setState((prev) => ({ ...prev, loading: !hasData.current }));
    }
  }, [path]);

  useEffect(() => {
    if (!path) {
      return undefined;
    }
    let alive = true;
    const load = async () => {
      try {
        const data = await api<T>(path);
        if (alive && current.current === path) {
          hasData.current = true;
          setState({ data, error: undefined, loading: false, stale: false, updatedAt: new Date() });
        }
      } catch (error) {
        if (alive && current.current === path) {
          const failure = error instanceof ApiError ? error : new ApiError(0, "unknown", String(error));
          setState((prev) => ({ ...prev, error: failure, loading: false, stale: prev.data !== undefined }));
        }
      }
    };
    void load();
    const timer = poll ? window.setInterval(() => {
      if (document.visibilityState === "visible") {
        void load();
      }
    }, poll) : undefined;
    return () => {
      alive = false;
      if (timer) {
        window.clearInterval(timer);
      }
    };
  }, [path, poll, tick, version]);

  const reload = useCallback(() => setTick((n) => n + 1), []);
  return { ...state, reload };
}

export function useHashRoute(): [string[], (next: string) => void] {
  const read = () => window.location.hash.replace(/^#\/?/, "").split("?")[0].split("/").filter(Boolean).map(decodeURIComponent);
  const [parts, setParts] = useState<string[]>(read);
  useEffect(() => {
    const onChange = () => setParts(read());
    window.addEventListener("hashchange", onChange);
    return () => window.removeEventListener("hashchange", onChange);
  }, []);
  const go = useCallback((next: string) => {
    window.location.hash = `#/${next}`;
  }, []);
  return [parts, go];
}

export function hashQuery(): URLSearchParams {
  const [, query = ""] = window.location.hash.split("?", 2);
  return new URLSearchParams(query);
}

export type LiveSyncState = "live" | "connecting" | "polling";

export function useLiveSync(workspaceId: string | undefined, enabled: boolean, onInvalidate: () => void) {
  const [state, setState] = useState<LiveSyncState>("connecting");
  const ref = useRef<EventSource | null>(null);
  const errorCount = useRef(0);
  const invalidateRef = useRef(onInvalidate);
  invalidateRef.current = onInvalidate;

  useEffect(() => {
    if (!workspaceId || !enabled) {
      ref.current?.close();
      ref.current = null;
      setState("polling");
      return;
    }
    errorCount.current = 0;

    const url = eventsUrl(workspaceId);
    const es = new EventSource(url);
    ref.current = es;
    setState("connecting");

    es.onopen = () => {
      errorCount.current = 0;
      setState("live");
    };

    es.onmessage = () => {
      invalidateRef.current();
    };

    es.onerror = () => {
      errorCount.current += 1;
      es.close();
      ref.current = null;
      if (errorCount.current >= 2) {
        setState("polling");
      } else {
        setState("connecting");
      }
    };

    return () => {
      es.close();
      ref.current = null;
    };
  }, [workspaceId, enabled]);

  useEffect(() => {
    if (state !== "polling" || !workspaceId || !enabled) return;
    const pollTimer = setInterval(() => {
      if (document.visibilityState === "visible") {
        invalidateRef.current();
      }
    }, 5_000);

    const retryTimer = setInterval(() => {
      if (!workspaceId || !enabled) return;
      ref.current?.close();
      const url = eventsUrl(workspaceId);
      const es = new EventSource(url);
      ref.current = es;
      es.onopen = () => {
        errorCount.current = 0;
        setState("live");
      };
      es.onmessage = () => {
        invalidateRef.current();
      };
      es.onerror = () => {
        es.close();
        ref.current = null;
        setState("polling");
      };
    }, 30_000);

    return () => {
      clearInterval(pollTimer);
      clearInterval(retryTimer);
      ref.current?.close();
      ref.current = null;
    };
  }, [state, workspaceId, enabled]);

  return state;
}
