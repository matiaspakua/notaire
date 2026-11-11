import { vi } from "vitest";

export class FakeApiError extends Error {
  status: number;
  body: string;
  constructor(status: number, path: string, body: string) {
    super(`[${status}] ${path}: ${body}`);
    this.name = "ApiError";
    this.status = status;
    this.body = body;
  }
}

/** next-intl stand-in: returns "<namespace>.<key>" so tests assert on keys, not copy. */
export function fakeUseTranslations(ns: string) {
  return (key: string) => `${ns}.${key}`;
}

export type PostRoute = (body: unknown, options?: { bearerToken?: string }) => unknown;

/** Routes apiPost calls by path; a route may return a value or throw. */
export function routePosts(apiPost: ReturnType<typeof vi.fn>, routes: Record<string, PostRoute>) {
  apiPost.mockImplementation(async (path: string, body: unknown, options?: { bearerToken?: string }) => {
    const route = routes[path];
    if (!route) throw new Error(`unrouted POST ${path}`);
    return route(body, options);
  });
}
