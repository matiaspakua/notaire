"use client";

import Link from "next/link";

import { MermaidDiagram } from "@/components/MermaidDiagram";
import { usePreferences } from "@/components/site/Preferences";
import { modelView, modelViewHref } from "@/lib/architecture-model";
import { tr } from "@/lib/i18n/es";

type Props = { id: string; headingLevel?: 2 | 3 };

/** One view of the LikeC4 architecture model: generated Mermaid plus a link into the interactive site (#2483). */
export function ModelView({ id, headingLevel = 3 }: Props) {
  const { locale } = usePreferences();
  const view = modelView(id);
  const Heading = headingLevel === 2 ? "h2" : "h3";
  return (
    <figure className="space-y-3" id={`view-${id}`}>
      <Heading className="text-lg font-semibold text-neutral-900">{tr(locale, view.title)}</Heading>
      <p className="max-w-3xl text-sm leading-relaxed text-neutral-600">{tr(locale, view.description)}</p>
      <MermaidDiagram chart={view.mermaid} label={view.title} wide />
      <figcaption>
        <Link
          href={modelViewHref(id)}
          prefetch={false}
          className="text-sm font-semibold text-brand-700 underline decoration-brand-700/30 underline-offset-2 hover:decoration-current"
        >
          {tr(locale, "Open this view in the interactive model")}
        </Link>
      </figcaption>
    </figure>
  );
}
