/**
 * HTTP failure type shared by the API client and UI error handling (#1732).
 * Lives outside api-client so app/hooks may import it without violating ADR-030
 * (only adapters/http may import the client itself).
 */
export class ApiError extends Error {
  readonly status: number;
  readonly path: string;
  readonly body: string;

  constructor(status: number, path: string, body: string) {
    super(`[${status}] ${path}: ${body}`);
    this.name = "ApiError";
    this.status = status;
    this.path = path;
    this.body = body;
  }
}
