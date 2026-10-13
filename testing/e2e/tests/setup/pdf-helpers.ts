import { expect, type Download } from "@playwright/test";
import { readFile } from "node:fs/promises";
import { inflateRawSync, inflateSync } from "node:zlib";

export async function readDownload(download: Download): Promise<Buffer> {
  const path = await download.path();
  if (!path) {
    throw new Error("Download has no local path");
  }
  return readFile(path);
}

/** PDFBox embeds Liberation Sans as subsetted Type0; text lives in FlateDecode + ToUnicode. */
export function extractPdfText(bytes: Buffer): string {
  const inflated = inflatePdfStreams(bytes);
  const cmap = parseToUnicode(inflated);
  const fromOperators = decodeTextOperators(inflated, cmap);
  const literals = [...inflated.matchAll(/\((?:\\.|[^\\)]){2,}\)/g)].map((match) =>
    decodePdfString(match[0].slice(1, -1), cmap),
  );
  const hex = [...inflated.matchAll(/<([0-9A-Fa-f]{4,})>/g)].map((match) => decodeHexCids(match[1], cmap));
  const runs = inflated.match(/[A-Za-zÁÉÍÓÚÜÑáéíóúüñ0-9][A-Za-zÁÉÍÓÚÜÑáéíóúüñ0-9 .,/_-]{2,}/g) ?? [];
  return [fromOperators, literals.join(" "), hex.join(" "), runs.join(" ")].join("\n");
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

function inflatePdfStreams(bytes: Buffer): string {
  const ascii = bytes.toString("latin1");
  const parts = [ascii];
  const stream = /stream\r?\n([\s\S]*?)endstream/g;
  let match: RegExpExecArray | null;
  while ((match = stream.exec(ascii)) !== null) {
    let raw = Buffer.from(match[1], "latin1");
    if (raw.at(-1) === 0x0d) {
      raw = raw.subarray(0, -1);
    }
    const inflated = tryInflate(raw);
    if (inflated) {
      parts.push(inflated.toString("latin1"));
    }
  }
  return parts.join("\n");
}

function tryInflate(raw: Buffer): Buffer | null {
  try {
    return inflateSync(raw);
  } catch {
    try {
      return inflateRawSync(raw);
    } catch {
      return null;
    }
  }
}

function parseToUnicode(source: string): Map<number, string> {
  const cmap = new Map<number, string>();
  for (const block of source.matchAll(/beginbfchar([\s\S]*?)endbfchar/g)) {
    for (const row of block[1].matchAll(/<([0-9A-Fa-f]+)>\s*<([0-9A-Fa-f]+)>/g)) {
      cmap.set(Number.parseInt(row[1], 16), utf16BeFromHex(row[2]));
    }
  }
  for (const block of source.matchAll(/beginbfrange([\s\S]*?)endbfrange/g)) {
    for (const row of block[1].matchAll(/<([0-9A-Fa-f]+)>\s*<([0-9A-Fa-f]+)>\s*<([0-9A-Fa-f]+)>/g)) {
      const lo = Number.parseInt(row[1], 16);
      const hi = Number.parseInt(row[2], 16);
      const start = Buffer.from(row[3], "hex");
      for (let cid = lo; cid <= hi; cid += 1) {
        cmap.set(cid, utf16BeFromBytes(incrementCode(start, cid - lo)));
      }
    }
    for (const row of block[1].matchAll(/<([0-9A-Fa-f]+)>\s*<([0-9A-Fa-f]+)>\s*\[([^\]]+)\]/g)) {
      const lo = Number.parseInt(row[1], 16);
      const dests = [...row[3].matchAll(/<([0-9A-Fa-f]+)>/g)].map((item) => utf16BeFromHex(item[1]));
      dests.forEach((value, index) => cmap.set(lo + index, value));
    }
  }
  return cmap;
}

function decodeTextOperators(source: string, cmap: Map<number, string>): string {
  const chunks: string[] = [];
  for (const match of source.matchAll(/\((?:\\.|[^\\)])*\)\s*Tj/g)) {
    const body = match[0].slice(1, match[0].lastIndexOf(")"));
    chunks.push(decodePdfString(body, cmap));
  }
  for (const match of source.matchAll(/<([0-9A-Fa-f]+)>\s*Tj/g)) {
    chunks.push(decodeHexCids(match[1], cmap));
  }
  return chunks.join(" ");
}

function decodePdfString(body: string, cmap: Map<number, string>): string {
  const raw = unescapePdfLiteral(body);
  const mapped = decodeCids(raw, cmap);
  if (mapped) {
    return mapped;
  }
  return raw.toString("latin1");
}

function decodeHexCids(hex: string, cmap: Map<number, string>): string {
  const raw = Buffer.from(hex, "hex");
  return decodeCids(raw, cmap) ?? raw.toString("latin1");
}

function decodeCids(raw: Buffer, cmap: Map<number, string>): string | null {
  if (cmap.size === 0 || raw.length < 2 || raw.length % 2 !== 0) {
    return null;
  }
  const chars: string[] = [];
  let mapped = 0;
  for (let index = 0; index < raw.length; index += 2) {
    const cid = (raw[index] << 8) | raw[index + 1];
    const unicode = cmap.get(cid);
    if (unicode !== undefined) {
      mapped += 1;
      chars.push(unicode);
    } else {
      chars.push(String.fromCharCode(cid));
    }
  }
  return mapped > 0 ? chars.join("") : null;
}

function unescapePdfLiteral(body: string): Buffer {
  const out: number[] = [];
  for (let index = 0; index < body.length; index += 1) {
    const current = body.charCodeAt(index);
    if (current !== 0x5c || index + 1 >= body.length) {
      out.push(current);
      continue;
    }
    const next = body.charAt(index + 1);
    index += 1;
    if (next >= "0" && next <= "7") {
      let octal = next;
      for (let extra = 0; extra < 2 && index + 1 < body.length; extra += 1) {
        const digit = body.charAt(index + 1);
        if (digit < "0" || digit > "7") {
          break;
        }
        octal += digit;
        index += 1;
      }
      out.push(Number.parseInt(octal, 8));
      continue;
    }
    const escapes: Record<string, number> = { n: 0x0a, r: 0x0d, t: 0x09, b: 0x08, f: 0x0c };
    out.push(escapes[next] ?? next.charCodeAt(0));
  }
  return Buffer.from(out);
}

function utf16BeFromHex(hex: string): string {
  return utf16BeFromBytes(Buffer.from(hex, "hex"));
}

function utf16BeFromBytes(raw: Buffer): string {
  let text = "";
  for (let index = 0; index + 1 < raw.length; index += 2) {
    text += String.fromCharCode((raw[index] << 8) | raw[index + 1]);
  }
  return text;
}

function incrementCode(start: Buffer, offset: number): Buffer {
  const next = Buffer.from(start);
  let carry = offset;
  for (let index = next.length - 1; index >= 0 && carry > 0; index -= 1) {
    const value = next[index] + carry;
    next[index] = value & 0xff;
    carry = value >> 8;
  }
  return next;
}
