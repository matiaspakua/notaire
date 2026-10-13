import assert from "node:assert/strict";
import { deflateSync } from "node:zlib";
import { test } from "node:test";
import { extractPdfText } from "./pdf-helpers.ts";

function flatePdf(content: string): Buffer {
  const payload = deflateSync(Buffer.from(content, "latin1"));
  return Buffer.concat([
    Buffer.from(
      `%PDF-1.6\n1 0 obj\n<< /Length ${payload.length} /Filter /FlateDecode >>\nstream\n`,
      "latin1",
    ),
    payload,
    Buffer.from("\nendstream\nendobj\n", "latin1"),
  ]);
}

test("extractPdfText reads ASCII literals in uncompressed PDFs", () => {
  const pdf = Buffer.from("%PDF-1.6\n(Index book) Tj\n", "latin1");
  assert.match(extractPdfText(pdf), /Index book/);
});

test("extractPdfText inflates FlateDecode ToUnicode CID text", () => {
  const content = [
    "begincmap",
    "beginbfchar",
    "<0001> <005A>",
    "<0002> <0061>",
    "<0003> <006D>",
    "<0004> <006F>",
    "<0005> <0072>",
    "<0006> <0061>",
    "<0007> <0031>",
    "<0008> <0032>",
    "endbfchar",
    "endcmap",
    "(\x00\x01\x00\x02\x00\x03\x00\x04\x00\x05\x00\x06\x00\x07\x00\x08) Tj",
  ].join("\n");
  const text = extractPdfText(flatePdf(content));
  assert.match(text, /Zamora12/);
});
