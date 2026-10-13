import { expect, type Download } from "@playwright/test";
import { readFile } from "node:fs/promises";

export async function readDownload(download: Download): Promise<Buffer> {
  const path = await download.path();
  if (!path) {
    throw new Error("Download has no local path");
  }
  return readFile(path);
}

/** Best-effort text from a PDFBox stream: literals plus raw ASCII runs. */
export function extractPdfText(bytes: Buffer): string {
  const ascii = bytes.toString("latin1");
  const literals = [...ascii.matchAll(/\((?:\\.|[^\\)]){2,}\)/g)].map((match) =>
    match[0].slice(1, -1).replace(/\\(.)/g, "$1"),
  );
  const runs = ascii.match(/[A-Za-zÁÉÍÓÚÜÑáéíóúüñ0-9][A-Za-zÁÉÍÓÚÜÑáéíóúüñ0-9 .\/,_-]{2,}/g) ?? [];
  return `${literals.join(" ")}\n${runs.join(" ")}`;
}

export async function assertPdfDownload(
  download: Download,
  filenamePattern: RegExp,
  expectedSnippets: string[],
): Promise<void> {
  expect(download.suggestedFilename()).toMatch(filenamePattern);
  const bytes = await readDownload(download);
  expect(bytes.subarray(0, 5).toString("ascii")).toBe("%PDF-");
  expect(bytes.length).toBeGreaterThan(400);
  const text = extractPdfText(bytes);
  for (const snippet of expectedSnippets) {
    expect(text, `missing ${snippet} in extracted PDF text`).toContain(snippet);
  }
}
