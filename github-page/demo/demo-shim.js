/*
 * Notaire Pages demo fetch shim (#1985). Inlined as a classic <script> in <head>, before the app.
 *
 * The real frontend calls the API only through fetch("/api/v1/..."). This shim answers those calls
 * from recorded fixtures (demo-fixtures.json, made by demo/record-fixtures.mjs from the seeded local
 * stack), so the static export runs with real sample data and no server:
 *   - GET: exact "path?query" match, then the path alone, else 404 (never 401: a 401 would trigger the
 *     app's session-expiry redirect to /login, outside the /notaire/demo base path).
 *   - POST /users/login: succeeds with the recorded user, whatever the credentials.
 *   - logout and client-error beacons: no-op 204.
 *   - any other POST/PUT/PATCH/DELETE: 403 "Demo is read-only", without touching the network.
 * Anything not under /api/ (scripts, styles, fonts) goes to the network unchanged.
 */
(function (g) {
  "use strict";
  var READ_ONLY = "Demo is read-only: changes are not saved.";
  var NO_OP = ["/users/logout"];

  function json(status, body) {
    return new Response(body === undefined ? null : JSON.stringify(body), {
      status: status,
      headers: { "content-type": "application/json" },
    });
  }

  function parts(input, init) {
    var url = typeof input === "string" ? input : input instanceof URL ? input.href : input.url;
    var method = ((init && init.method) || (typeof input === "object" && !(input instanceof URL) && input.method) || "GET").toUpperCase();
    var u = new URL(url, (g.location && g.location.origin) || "http://localhost");
    return { url: u, method: method };
  }

  function apiPath(pathname) {
    var i = pathname.indexOf("/api/");
    return i < 0 ? null : pathname.slice(i);
  }

  function createDemoFetch(loadFixtures, realFetch) {
    var fixtures = null;
    function load() {
      if (!fixtures) fixtures = Promise.resolve(loadFixtures()).then(function (f) { return f.responses || {}; });
      return fixtures;
    }
    return function demoFetch(input, init) {
      var p = parts(input, init);
      var api = apiPath(p.url.pathname);
      if (api === null) return realFetch(input, init);
      if (api.indexOf("/api/v1/") !== 0) return Promise.resolve(new Response(null, { status: 204 }));
      var path = api.slice("/api/v1".length);
      if (p.method === "GET" || p.method === "HEAD") {
        return load().then(function (r) {
          var hit = r["GET " + path + p.url.search] || r["GET " + path];
          return hit ? json(hit.status === 401 ? 404 : hit.status, hit.body) : json(404, { message: "Not part of the demo data." });
        });
      }
      if (p.method === "POST" && path === "/users/login") {
        return load().then(function (r) {
          var hit = r["POST /users/login"];
          return hit ? json(200, hit.body) : json(403, { message: READ_ONLY });
        });
      }
      if (NO_OP.indexOf(path) >= 0) return Promise.resolve(new Response(null, { status: 204 }));
      return Promise.resolve(json(403, { message: READ_ONLY, error: "read-only" }));
    };
  }

  g.NotaireDemo = { createDemoFetch: createDemoFetch, READ_ONLY: READ_ONLY };

  // Install only in a browser page. build-demo.mjs inlines this file in <head> (so it runs before any
  // app chunk) and sets __NOTAIRE_DEMO_BASE__; loaded as a file, it uses its own folder.
  var script = g.document && g.document.currentScript;
  var base = g.__NOTAIRE_DEMO_BASE__ || (script && script.src ? script.src.replace(/[^/]*$/, "") : null);
  if (typeof g.fetch === "function" && g.document && base) {
    var realFetch = g.fetch.bind(g);
    g.fetch = createDemoFetch(function () {
      return realFetch(base + "demo-fixtures.json").then(function (r) { return r.json(); });
    }, realFetch);
    if (g.navigator && typeof g.navigator.sendBeacon === "function") {
      var beacon = g.navigator.sendBeacon.bind(g.navigator);
      g.navigator.sendBeacon = function (url, data) {
        return apiPath(new URL(url, g.location.origin).pathname) === null ? beacon(url, data) : true;
      };
    }
  }
})(typeof window !== "undefined" ? window : globalThis);
