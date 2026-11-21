import { useCallback, useEffect, useRef, useState } from "react";

import { api, ApiError } from "./api.ts";

export interface Resource<T> {
  data: T | undefined;
  error: ApiError | undefined;
  loading: boolean;
  stale: boolean;
  updatedAt: Date | undefined;
  reload: () => void;
}

/** GET a path, optionally polling. A failed refresh keeps the last data and marks it stale. */
export function useResource<T>(path: string | null, poll?: number): Resource<T> {
  const [state, setState] = useState<Omit<Resource<T>, "reload">>({
    data: undefined, error: undefined, loading: Boolean(path), stale: false, updatedAt: undefined,
  });
  const [tick, setTick] = useState(0);
  const current = useRef(path);
  current.current = path;

  useEffect(() => {
    setState({ data: undefined, error: undefined, loading: Boolean(path), stale: false, updatedAt: undefined });
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
  }, [path, poll, tick]);

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
