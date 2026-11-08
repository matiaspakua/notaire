import Link from "next/link";
import { AboutFacts } from "@/components/About";
import { loadAbout } from "@/lib/about";
import { pageMetadata } from "@/lib/site";

export const metadata = pageMetadata("/about/");

/** About (#1985): who built Notaire, only from sourced facts in lib/about.json. */
export default function AboutPage() {
  const about = loadAbout();
  return (
    <div className="mx-auto max-w-3xl px-4 pb-24 pt-16 sm:px-6">
      <p className="text-xs font-semibold uppercase tracking-[0.2em] text-brand-700">About</p>
      <h1 className="mt-4 font-display text-4xl font-semibold leading-[1.1] tracking-tight text-slate-900 sm:text-5xl">{about.name}</h1>
      <p className="mt-6 max-w-[65ch] text-lg leading-relaxed text-slate-700">
        The engineer behind Notaire. Everything below comes from a public source, linked next to it.
      </p>
      <div className="mt-10">
        <AboutFacts data={about} />
      </div>
      <p className="mt-10 flex flex-wrap gap-3">
        <Link href="/ai-sdlc/results/#outcomes" className="rounded-full bg-brand-700 px-5 py-2.5 text-sm font-semibold text-white hover:bg-brand-800">
          Delivery outcomes
        </Link>
        <Link href="/product/story/" className="rounded-full border border-slate-300 bg-white px-5 py-2.5 text-sm font-semibold text-slate-800 hover:border-brand-500">
          The story of the project
        </Link>
      </p>
    </div>
  );
}
