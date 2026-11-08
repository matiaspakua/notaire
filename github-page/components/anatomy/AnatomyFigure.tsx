"use client";

import { useEffect, useMemo, useRef } from "react";
import { plateHtml } from "@/components/iso/iso-kit";
import { StepControls, type Step } from "@/components/diagrams/StepControls";
import { useStepper } from "@/components/diagrams/useStepper";
import { Code } from "@/components/FactoryUI";
import type { FigureData } from "./figure-types";
import "@/components/iso/iso.css";
import "@/components/figures/figures.css";
import "./anatomy.css";

/**
 * An Anatomy figure (#2089): the machine is static SVG built at build time. This component only moves
 * its named parts, dims the stations that are not being explained and writes the card readout.
 * Nothing plays on its own; the reader steps with Prev, Next, Play or a click on a station, and
 * the step list below is the text version of the drawing.
 */
export function AnatomyFigure({ data }: { data: FigureData }) {
  const root = useRef<HTMLDivElement>(null);
  const touched = useRef(false);
  const s = useStepper(data.steps.length, 4200);

  const html = useMemo(
    () =>
      plateHtml({
        fig: data.fig,
        title: data.title,
        hint: data.hint,
        readout: data.steps[0].readout,
        keys: data.keys,
        caption: data.caption,
        body: data.svg,
      }),
    [data],
  );

  useEffect(() => {
    const host = root.current;
    const svg = host?.querySelector("svg");
    if (!host || !svg) return;
    const step = data.steps[s.index];
    // The whole machine shows until the reader first moves; only then are other stations dimmed.
    if (s.index !== 0) touched.current = true;
    svg.querySelectorAll<SVGElement>("[data-station]").forEach((el) => {
      if (!touched.current || Number(el.dataset.station) === step.station) el.removeAttribute("data-dim");
      else el.setAttribute("data-dim", "");
    });
    for (const [part, pose] of Object.entries(step.pose)) {
      const el = svg.querySelector<SVGElement>(`[data-part="${part}"]`);
      if (!el) continue;
      if (pose.transform !== undefined) el.style.transform = pose.transform;
      if (pose.opacity !== undefined) el.style.opacity = String(pose.opacity);
      if (pose.d !== undefined) el.setAttribute("d", pose.d);
    }
    const readout = host.querySelector("[data-readout]");
    if (readout) readout.textContent = step.readout;
  }, [s.index, data]);

  useEffect(() => {
    const host = root.current;
    if (!host) return;
    const onClick = (event: Event) => {
      const target = (event.target as Element | null)?.closest<HTMLElement>("[data-station]");
      if (!target) return;
      const index = data.steps.findIndex((st) => st.station === Number(target.dataset.station));
      if (index >= 0) s.jump(index);
    };
    host.addEventListener("click", onClick);
    return () => host.removeEventListener("click", onClick);
  }, [data.steps, s.jump]); // eslint-disable-line react-hooks/exhaustive-deps

  const steps: Step[] = data.steps.map((st) => ({
    title: st.title,
    body: (
      <>
        {st.text}
        <span className="mt-2 block text-xs text-slate-600">
          Where to look: <Code>{st.where}</Code>
        </span>
      </>
    ),
  }));

  return (
    <section className={`iso anatomy${s.reduced ? " anatomy-still" : ""}`} data-theme="light" aria-label={`${data.fig}: ${data.title}`}>
      <div ref={root} dangerouslySetInnerHTML={{ __html: html }} />
      <StepControls id={data.id} steps={steps} s={s} label={data.title} />
    </section>
  );
}
