"use client";

import { useState } from "react";
import Link from "next/link";

const QUESTIONS = [
  {
    id: "request",
    ask: "Where does a request go?",
    answer: "From the browser, through the BFF, into a port and a use case, then PostgreSQL. Step through the payment slice.",
    href: "/docs/hexagonal/",
    label: "Open the hexagon",
  },
  {
    id: "trust",
    ask: "Who is allowed to call the API?",
    answer: "A JWT, then a role, then a module grant. The interactive boundaries are in the Security section.",
    href: "/security/architecture/",
    label: "Open the trust boundaries",
  },
  {
    id: "proof",
    ask: "What has to pass before a pull request?",
    answer: "The local preflight, including the security case catalog. GitHub does not re-run the product suite on the pull request.",
    href: "/ai-sdlc/quality-gates/",
    label: "Open the quality gates",
  },
  {
    id: "schema",
    ask: "How does the database change?",
    answer: "A Flyway migration. Hibernate does not create the schema.",
    href: "/docs/database/",
    label: "Open Flyway",
  },
];

/** Four questions on the docs home. One answer is visible at a time. */
export function QuestionPicker() {
  const [id, setId] = useState(QUESTIONS[0].id);
  const current = QUESTIONS.find((item) => item.id === id) ?? QUESTIONS[0];
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-4 sm:p-6">
      <div role="tablist" aria-label="Documentation questions" className="flex flex-wrap gap-2">
        {QUESTIONS.map((item) => {
          const selected = item.id === current.id;
          return (
            <button
              key={item.id}
              type="button"
              role="tab"
              aria-selected={selected}
              onClick={() => setId(item.id)}
              className={`rounded-full border px-3 py-1.5 text-sm font-medium ${
                selected ? "border-brand-700 bg-brand-50 text-brand-800" : "border-slate-200 text-slate-700 hover:border-slate-400"
              }`}
            >
              {item.ask}
            </button>
          );
        })}
      </div>
      <div role="tabpanel" className="mt-4">
        <p className="max-w-3xl leading-relaxed text-slate-700">{current.answer}</p>
        <Link href={current.href} className="mt-3 inline-block text-sm font-semibold text-brand-700 hover:underline">
          {current.label} →
        </Link>
      </div>
    </div>
  );
}
