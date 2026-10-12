import fs from "fs";
import path from "path";

export type ReleaseEntry = {
  version: string;
  date: string;
  tag: string;
  train: string;
  url: string;
};

export type ReleaseHistory = {
  productVersion: string;
  train: string;
  tag: string;
  components: {
    backend: string;
    frontend: string;
    databaseSchema: string;
  };
  releases: ReleaseEntry[];
};

/** Written by `workspace/release/product_release.py` before `next build`. */
export function loadReleaseHistory(): ReleaseHistory {
  const file = path.join(process.cwd(), "lib", "releases.json");
  return JSON.parse(fs.readFileSync(file, "utf8")) as ReleaseHistory;
}
