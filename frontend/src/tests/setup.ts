import "@testing-library/jest-dom";

// jsdom does not implement scrollIntoView/hasPointerCapture/PointerEvent methods
// that Radix UI primitives (e.g. Select) rely on for focus/scroll management.
if (typeof window !== "undefined") {
  if (!Element.prototype.scrollIntoView) {
    Element.prototype.scrollIntoView = () => {};
  }
  if (!Element.prototype.hasPointerCapture) {
    Element.prototype.hasPointerCapture = () => false;
  }
  if (!Element.prototype.releasePointerCapture) {
    Element.prototype.releasePointerCapture = () => {};
  }
}

// jsdom in this project does not provide window.localStorage; polyfill it with
// a simple in-memory store so zustand's `persist` middleware (auth-store) and
// any test reading/writing localStorage directly work without extra mocking.
if (typeof window !== "undefined" && !window.localStorage) {
  const store = new Map<string, string>();
  Object.defineProperty(window, "localStorage", {
    value: {
      getItem: (key: string) => store.get(key) ?? null,
      setItem: (key: string, value: string) => store.set(key, value),
      removeItem: (key: string) => store.delete(key),
      clear: () => store.clear(),
      key: () => null,
      get length() {
        return store.size;
      },
    },
    writable: true,
  });
}
