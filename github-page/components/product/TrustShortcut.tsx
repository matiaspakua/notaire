"use client";

import { useState } from "react";
import Link from "next/link";
import { Es } from "@/components/site/Es";

const STEPS = [
  { id: "browser", title: "Browser", body: "Staff send an HttpOnly cookie. The page does not keep the JWT in local storage." },
  { id: "bff", title: "BFF", body: "Next.js proxies /api/v1 with a server-side backend address. The public login page does not print that host." },
  { id: "api", title: "API", body: "The token is checked, the role is resolved, then the module grant. A missing grant is 403." },
];

/** Three stops on the product platform page. The full diagram is in Security. */
export function TrustShortcut() {
  const [id, setId] = useState(STEPS[0].id);
  const current = STEPS.find((step) => step.id === id) ?? STEPS[0];
  return (
    <Es>
    <section aria-labelledby="trust-shortcut-title" className="mt-12 rounded-3xl border border-slate-200 bg-white p-6">
      <h2 id="trust-shortcut-title" className="font-display text-2xl font-semibold text-slate-900">
        Where trust sits
      </h2>
      <p className="mt-2 max-w-3xl text-sm text-slate-600">A request crosses three boundaries before it touches a notarial record.</p>
      <div role="tablist" aria-label="Trust boundaries" className="mt-4 flex flex-wrap gap-2">
        {STEPS.map((step, index) => {
          const selected = step.id === current.id;
          return (
            <button
              key={step.id}
              type="button"
              role="tab"
              aria-selected={selected}
              onClick={() => setId(step.id)}
              className={`rounded-full border px-3 py-1.5 text-sm font-semibold ${
                selected ? "border-brand-700 bg-brand-50 text-brand-800" : "border-slate-200 text-slate-700"
              }`}
            >
              {index + 1}. {step.title}
            </button>
          );
        })}
      </div>
      <p className="mt-4 max-w-3xl leading-relaxed text-slate-800">{current.body}</p>
      <Link href="/security/architecture/" className="mt-4 inline-block text-sm font-semibold text-brand-700 hover:underline">
        Step through the full boundary diagram →
      </Link>
    </section>
    </Es>
  );
}
