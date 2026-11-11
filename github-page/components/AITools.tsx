"use client";
import { Es } from "@/components/site/Es";
import { T } from "@/components/site/T";

const tools = [
  {
    name: "Claude Code",
    maker: "Anthropic",
    role: "Primary AI agent — TDD, code review, E2E tests, docs, architecture decisions",
    badge: "Core",
  },
  {
    name: "Cursor Cloud",
    maker: "Cursor",
    role: "Autonomous cloud fleet — foreman + specialists run Issue → OpenSpec → PR → heavy-CI on remote VMs",
    badge: "Fleet",
  },
  {
    name: "OpenCode",
    maker: "OpenCode",
    role: "Terminal-native AI coding agent, integrated directly into the shell workflow",
    badge: "Shell",
  },
  {
    name: "GitHub Copilot",
    maker: "Microsoft / GitHub",
    role: "VS Code inline completions, chat, and PR summaries for rapid in-editor development",
    badge: "IDE",
  },
  {
    name: "Gemini CLI",
    maker: "Google",
    role: "Command-line Gemini access for research, alternative perspectives, and cross-validation",
    badge: "Research",
  },
];

export function AITools() {
  return (
    <Es>
      <section className="bg-slate-50 px-6 py-24">
        <div className="mx-auto max-w-5xl">
          <div className="mb-10 max-w-2xl">
            <h2 className="font-display text-4xl font-semibold tracking-tight text-slate-900 sm:text-5xl">
              <T text="The AI Tools That Built It" />
            </h2>
            <p className="mt-4 text-slate-600">
              <T text="Five AI tools, one codebase. Each played a different role in accelerating from idea to production." />
            </p>
          </div>
          <ul className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            {tools.map((tool) => (
              <li key={tool.name} className="rounded-2xl border border-slate-200 bg-white p-6">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="font-semibold text-slate-900">{tool.name}</span>
                  <span className="rounded-full border border-slate-200 px-2 py-0.5 text-xs text-slate-600">
                    <T text={tool.badge} />
                  </span>
                </div>
                <p className="mt-1 text-xs text-slate-500">{tool.maker}</p>
                <p className="mt-3 text-sm leading-relaxed text-slate-700">
                  <T text={tool.role} />
                </p>
              </li>
            ))}
          </ul>
        </div>
      </section>
    </Es>
  );
}
