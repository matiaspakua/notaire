import { createRoot } from "react-dom/client";
import { StrictMode } from "react";
import App from "./App";
import { ErrorBoundary } from "./components/ErrorBoundary";

// Catch any unhandled errors before React mounts
window.addEventListener("error", (e) => {
  console.error("[Factory Dashboard] Unhandled error:", e.error);
});
window.addEventListener("unhandledrejection", (e) => {
  console.error("[Factory Dashboard] Unhandled rejection:", e.reason);
});

const rootEl = document.getElementById("root");
if (!rootEl) {
  document.body.innerHTML = '<div style="padding:2rem;font-family:system-ui">Factory Dashboard failed to mount — root element missing.</div>';
} else {
  const root = createRoot(rootEl);
  root.render(
    <StrictMode>
      <ErrorBoundary>
        <App />
      </ErrorBoundary>
    </StrictMode>
  );
}
