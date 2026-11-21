/**
 * Factory Dashboard — Error Boundary.
 *
 * Catches React render errors so the dashboard never shows a blank screen.
 */
import { Component, type ReactNode, type ErrorInfo } from "react";

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error("[Factory Dashboard] Uncaught React error:", error, info);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{
          fontFamily: "system-ui, -apple-system, sans-serif",
          padding: "2rem",
          maxWidth: 600,
          margin: "2rem auto",
          background: "#fef2f2",
          border: "1px solid #fecaca",
          borderRadius: 12,
        }}>
          <h1 style={{ fontSize: 18, color: "#991b1b", margin: "0 0 0.5rem" }}>
            Factory Dashboard Error
          </h1>
          <p style={{ color: "#7f1d1d", margin: "0 0 1rem" }}>
            Something went wrong while rendering the dashboard. Check the browser console for details.
          </p>
          <details style={{ color: "#7f1d1d", fontSize: 13 }}>
            <summary style={{ cursor: "pointer" }}>Error details</summary>
            <pre style={{
              background: "#fff",
              padding: "0.75rem",
              borderRadius: 8,
              overflow: "auto",
              marginTop: "0.5rem",
              fontSize: 12,
            }}>
              {this.state.error?.toString()}
            </pre>
          </details>
          <button
            onClick={() => window.location.reload()}
            style={{
              marginTop: "1rem",
              padding: "0.5rem 1rem",
              background: "#991b1b",
              color: "#fff",
              border: "none",
              borderRadius: 8,
              cursor: "pointer",
              fontSize: 14,
            }}
          >
            Reload Dashboard
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
