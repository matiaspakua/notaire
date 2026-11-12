import { readFileSync } from "node:fs";
import path from "node:path";
import { describe, expect, it } from "vitest";
import { POSTURE, STRIDE } from "@/lib/security-content";
import { loadReleaseHistory } from "@/lib/releases";

describe("public pages match the product on main (#2270)", () => {
  it("says TOTP MFA ships and does not claim there is no second factor", () => {
    const identity = POSTURE.find((row) => row.id === "identity");
    const spoof = STRIDE.flatMap((group) => group.items).find((item) => item.id === "AT-1");
    const text = `${identity?.summary ?? ""}\n${spoof?.summary ?? ""}`;
    expect(text).toMatch(/TOTP|MFA/i);
    expect(text).not.toMatch(/no second factor/i);
    expect(text).toMatch(/OPTIONAL|ENFORCE|optional/i);
  });

  it("lists the product version from the release-please manifest", () => {
    const manifest = JSON.parse(readFileSync(path.join(process.cwd(), "..", ".release-please-manifest.json"), "utf8")) as {
      ".": string;
    };
    const history = loadReleaseHistory();
    expect(history.productVersion).toBe(manifest["."]);
    expect(history.tag).toBe(`v${manifest["."]}`);
    expect(history.releases[0]?.version).toBe(manifest["."]);
  });
});
