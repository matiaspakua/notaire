import "@testing-library/jest-dom";

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
