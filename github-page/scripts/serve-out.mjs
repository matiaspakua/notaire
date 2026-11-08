// Serves the static export (out/) under the production base path /notaire, for the
// Playwright smoke suite and local checks (#1970). No dependencies.
import { createServer } from "node:http";
import { existsSync, readFileSync, statSync } from "node:fs";
import path from "node:path";

const ROOT = path.resolve(process.argv[2] || "out");
const PORT = Number(process.env.PORT || 4173);
const BASE = "/notaire";
const TYPES = { ".html": "text/html; charset=utf-8", ".js": "text/javascript", ".css": "text/css", ".svg": "image/svg+xml", ".json": "application/json", ".png": "image/png", ".ico": "image/x-icon", ".woff2": "font/woff2", ".txt": "text/plain" };

createServer((req, res) => {
  const url = decodeURIComponent((req.url || "/").split("?")[0]);
  if (!url.startsWith(BASE)) {
    res.writeHead(302, { location: `${BASE}/` }).end();
    return;
  }
  let file = path.join(ROOT, url.slice(BASE.length));
  if (!file.startsWith(ROOT)) return res.writeHead(403).end();
  if (existsSync(file) && statSync(file).isDirectory()) file = path.join(file, "index.html");
  if (!existsSync(file)) {
    res.writeHead(404, { "content-type": "text/html" }).end(existsSync(path.join(ROOT, "404.html")) ? readFileSync(path.join(ROOT, "404.html")) : "not found");
    return;
  }
  res.writeHead(200, { "content-type": TYPES[path.extname(file)] || "application/octet-stream" }).end(readFileSync(file));
}).listen(PORT, () => console.log(`serving ${ROOT} at http://localhost:${PORT}${BASE}/`));
