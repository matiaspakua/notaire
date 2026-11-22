import { describe, expect, it } from "vitest";

import { buildConfigTree, encodeConfigPath, formatDuration, type ConfigFileEntry } from "../src/ui/model.ts";

describe("buildConfigTree", () => {
  it("groups files by directory", () => {
    const entries: ConfigFileEntry[] = [
      { path: ".aisdlc/aisdlc.toml", kind: "profile", size: 100, media_type: "text/toml", read_only: false },
      { path: ".aisdlc/catalogs/agents/a.json", kind: "catalog", size: 50, media_type: "application/json", read_only: false },
      { path: ".aisdlc/catalogs/agents/b.json", kind: "catalog", size: 60, media_type: "application/json", read_only: false },
      { path: ".aisdlc/catalogs/skills/s.json", kind: "catalog", size: 40, media_type: "application/json", read_only: false },
    ];
    const tree = buildConfigTree(entries);
    expect(tree.length).toBe(1);
    expect(tree[0].name).toBe(".aisdlc");
    expect(tree[0].path).toBeUndefined();
    expect(tree[0].children.length).toBe(2);
    const toml = tree[0].children.find((c) => c.name === "aisdlc.toml");
    expect(toml?.path).toBe(".aisdlc/aisdlc.toml");
    expect(toml?.kind).toBe("profile");
    const catalogs = tree[0].children.find((c) => c.name === "catalogs");
    expect(catalogs?.path).toBeUndefined();
    expect(catalogs?.children.length).toBe(2);
  });

  it("handles flat file list", () => {
    const entries: ConfigFileEntry[] = [
      { path: "file1.toml", kind: "profile", size: 10, media_type: "text/toml", read_only: false },
      { path: "file2.json", kind: "catalog", size: 20, media_type: "application/json", read_only: false },
    ];
    const tree = buildConfigTree(entries);
    expect(tree.length).toBe(2);
    expect(tree[0].name).toBe("file1.toml");
    expect(tree[0].path).toBe("file1.toml");
    expect(tree[1].name).toBe("file2.json");
  });

  it("returns empty array for empty input", () => {
    expect(buildConfigTree([])).toEqual([]);
  });

  it("handles deeply nested paths", () => {
    const entries: ConfigFileEntry[] = [
      { path: "a/b/c/d/file.txt", kind: "context", size: 1, media_type: "text/plain", read_only: false },
    ];
    const tree = buildConfigTree(entries);
    const a = tree[0];
    expect(a.name).toBe("a");
    expect(a.children[0].name).toBe("b");
    expect(a.children[0].children[0].name).toBe("c");
    expect(a.children[0].children[0].children[0].name).toBe("d");
    const file = a.children[0].children[0].children[0].children[0];
    expect(file.name).toBe("file.txt");
    expect(file.path).toBe("a/b/c/d/file.txt");
  });
});

describe("formatDuration", () => {
  it("formats seconds", () => {
    expect(formatDuration(0, "en")).toBe("0s");
    expect(formatDuration(1.5, "en")).toBe("1.5s");
    expect(formatDuration(0.05, "en")).toBe("0.05s");
    expect(formatDuration(59, "en")).toBe("59s");
  });

  it("formats minutes and seconds", () => {
    expect(formatDuration(60, "en")).toBe("1m");
    expect(formatDuration(90, "en")).toBe("1m 30s");
    expect(formatDuration(359, "en")).toBe("5m 59s");
  });

  it("formats hours", () => {
    expect(formatDuration(3600, "en")).toBe("1h");
    expect(formatDuration(3900, "en")).toBe("1h 5m");
    expect(formatDuration(7200, "en")).toBe("2h");
  });
});

describe("encodeConfigPath", () => {
  it("encodes each path segment", () => {
    expect(encodeConfigPath(".aisdlc/aisdlc.toml")).toBe(".aisdlc/aisdlc.toml");
    expect(encodeConfigPath("path with spaces/file.json")).toBe("path%20with%20spaces/file.json");
    expect(encodeConfigPath("a/b/c")).toBe("a/b/c");
  });

  it("handles special characters", () => {
    expect(encodeConfigPath("file (1).json")).toBe("file%20(1).json");
  });
});
