<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" style="height:64px;margin-right:32px"/>

# create using motion js an awesome animation workflow for administrative procedures document management, where like a data flow (from top to botton) the animation help you to easily identify for each step, the document needed, the status and the process transition to the next steps. Could existe forks that shall end in the same ending final state (closed). The anumation shall be amaizin, well curated, detailes and very professional, the user could click on any step of the workflow and know (with an pop up modal), all the details associated. The complete workflow, transitions, dependencies, status and other details are defined in other module of an application, this main form animation only show the full process as a glipse with amaizing animation and curated icons, text and colors. The workflow will be defined in a yaml file. the user shall write in the top, before the start step, a reference number of any documents, and the animation shall indicate the main " administrative procedures" that compose it, the actual status of each step, and highlight easily the actual document that the user write. The animation shall shown visually hoy the whole process is compose, linked, trace.

You want: a React/Next.js vertical workflow map, animated with Motion (Framer Motion’s successor), fed from a YAML workflow definition, clickable steps with modals, fork/merge paths, and a document reference search that highlights all related steps.[^1][^2][^3]

Below is a concrete architecture and code-level design.

***

## Overall architecture

- Frontend: Next.js + React, using Motion for React (`motion/react`) for animations.[^2][^3]
- Data: workflow defined in a YAML file, compiled to JSON and consumed by a `<WorkflowViewer>` React component.
- Responsibilities split: one module builds the canonical workflow graph; this viewer is read‑only, only visualizing it.

***

## YAML workflow schema

Define a minimal, explicit schema so the viewer is dumb and generic.

```yaml
# workflows/administrative_procedures.yaml
id: "administrative_procedures_v1"
title: "Administrative Procedures"
steps:
  - id: "start"
    kind: "start"
    label: "Start intake"
    icon: "start"
    status: "completed"        # pending | in_progress | blocked | completed | skipped
    documents_required: []
    documents_generated: []
    next: ["collect_docs"]

  - id: "collect_docs"
    kind: "task"
    label: "Collect documents"
    icon: "folder-open"
    status: "in_progress"
    documents_required: ["ID", "TaxCertificate"]
    documents_generated: ["CaseFile"]
    next: ["legal_review", "risk_assessment"]

  - id: "legal_review"
    kind: "task"
    label: "Legal review"
    icon: "scale"
    status: "pending"
    documents_required: ["CaseFile"]
    documents_generated: ["LegalReport"]
    next: ["final_signoff"]

  - id: "risk_assessment"
    kind: "task"
    label: "Risk assessment"
    icon: "shield"
    status: "pending"
    documents_required: ["CaseFile"]
    documents_generated: ["RiskReport"]
    next: ["final_signoff"]

  - id: "final_signoff"
    kind: "task"
    label: "Final sign-off"
    icon: "signature"
    status: "pending"
    documents_required: ["LegalReport", "RiskReport"]
    documents_generated: ["Resolution"]
    next: ["closed"]

  - id: "closed"
    kind: "end"
    label: "Closed"
    icon: "check-circle"
    status: "pending"
    documents_required: []
    documents_generated: []
    next: []
```


***

## TypeScript data model

```ts
// types/workflow.ts
export type StepStatus =
  | "pending"
  | "in_progress"
  | "blocked"
  | "completed"
  | "skipped"

export interface WorkflowStep {
  id: string
  kind: "start" | "task" | "end"
  label: string
  icon: string
  status: StepStatus
  documents_required: string[]
  documents_generated: string[]
  next: string[]
}

export interface WorkflowDefinition {
  id: string
  title: string
  steps: WorkflowStep[]
}
```

Convert YAML → JSON at build-time (e.g. with `yaml` loader or a small Node script) so the React bundle only imports JSON.

***

## Layout and graph structure

- Build a DAG from `steps` + `next`.
- Compute a layered top‑to‑bottom layout (e.g. simple Sugiyama‐like: assign depth by BFS level, then horizontally space siblings and forks).
- Store layout as `position: {x, y}` per step so the renderer only cares about coordinates.

```ts
interface PositionedStep extends WorkflowStep {
  x: number
  y: number
  incoming: string[]
}

export interface WorkflowLayout {
  steps: PositionedStep[]
  edges: { from: string; to: string }[]
}

// layout/workflowLayout.ts
export function computeLayout(def: WorkflowDefinition): WorkflowLayout {
  const byId = new Map(def.steps.map(s => [s.id, s]))
  const incoming = new Map<string, string[]>()
  def.steps.forEach(s => {
    s.next.forEach(n => {
      if (!incoming.has(n)) incoming.set(n, [])
      incoming.get(n)!.push(s.id)
    })
  })

  const levels = new Map<string, number>()
  const queue: string[] = []

  // Start nodes level 0
  def.steps
    .filter(s => s.kind === "start" || (incoming.get(s.id) ?? []).length === 0)
    .forEach(s => {
      levels.set(s.id, 0)
      queue.push(s.id)
    })

  while (queue.length) {
    const id = queue.shift()!
    const level = levels.get(id) ?? 0
    const step = byId.get(id)!
    step.next.forEach(n => {
      const existing = levels.get(n)
      const candidate = level + 1
      if (existing == null || candidate > existing) {
        levels.set(n, candidate)
        queue.push(n)
      }
    })
  }

  // group by level and assign x positions
  const levelsMap = new Map<number, string[]>()
  levels.forEach((lvl, id) => {
    if (!levelsMap.has(lvl)) levelsMap.set(lvl, [])
    levelsMap.get(lvl)!.push(id)
  })

  const positioned: PositionedStep[] = []
  const edges: { from: string; to: string }[] = []

  const levelGapY = 220
  const nodeGapX = 260

  Array.from(levelsMap.keys())
    .sort((a, b) => a - b)
    .forEach(level => {
      const ids = levelsMap.get(level)!
      ids.sort()
      const offsetX = -((ids.length - 1) * nodeGapX) / 2
      ids.forEach((id, index) => {
        const s = byId.get(id)!
        positioned.push({
          ...s,
          x: offsetX + index * nodeGapX,
          y: level * levelGapY,
          incoming: incoming.get(id) ?? [],
        })
        s.next.forEach(n => edges.push({ from: id, to: n }))
      })
    })

  return { steps: positioned, edges }
}
```


***

## Visual language

- Nodes: rounded cards with icon, title, status pill, small list of key documents.
- Edges: cubic Bézier SVG paths with subtle gradient + animated dot traveling along the path for “data flow” feel.
- Status → color mapping (example):
    - `completed`: green, solid border.
    - `in_progress`: blue, pulsing glow.
    - `blocked`: red, shaking or subtle vibration.
    - `pending`: gray, low emphasis.

***

## Motion design with Motion for React

Use Motion for React (`motion/react`) to animate node entrance, status changes, and edge flows.[^4][^2]

Key tools:

- `<motion.g>` / `<motion.div>` for nodes, with `initial`, `animate`, `whileHover` props.[^2]
- Layout transitions for rearranging when status or filters change, via `layout` prop.
- Reusable variants for statuses, hover, and “highlighted by search”.

Example node:

```tsx
// components/WorkflowNode.tsx
import { motion } from "motion/react"
import type { PositionedStep } from "../layout/workflowLayout"

const statusVariants = {
  pending:   { opacity: 0.6, scale: 1, boxShadow: "0 0 0px rgba(0,0,0,0)" },
  in_progress: {
    opacity: 1,
    scale: 1.02,
    boxShadow: "0 0 18px rgba(59,130,246,0.5)",
  },
  completed: {
    opacity: 1,
    scale: 1,
    boxShadow: "0 0 14px rgba(22,163,74,0.6)",
  },
  blocked: {
    opacity: 1,
    scale: 1,
    boxShadow: "0 0 14px rgba(220,38,38,0.7)",
  },
  skipped:   { opacity: 0.4, scale: 0.98, boxShadow: "0 0 0px rgba(0,0,0,0)" },
}

const highlightVariant = {
  rest: { outline: "0px solid rgba(234,179,8,0)" },
  highlighted: {
    outline: "3px solid rgba(234,179,8,1)",
    outlineOffset: 4,
    transition: { duration: 0.25 },
  },
}

interface Props {
  step: PositionedStep
  highlighted: boolean
  onClick: () => void
}

export function WorkflowNode({ step, highlighted, onClick }: Props) {
  return (
    <motion.div
      layout
      initial={{ opacity: 0, y: -20 }}
      animate={{ opacity: 1, y: 0 }}
      whileHover={{ scale: 1.04 }}
      variants={statusVariants}
      animate={step.status}
      data-step-id={step.id}
      onClick={onClick}
      style={{
        position: "absolute",
        left: step.x,
        top: step.y,
        transform: "translate(-50%, -50%)", // center on x,y
        borderRadius: 16,
        padding: 16,
        background: "rgba(15,23,42,0.96)",
        color: "white",
        border: "1px solid rgba(148,163,184,0.4)",
        cursor: "pointer",
        minWidth: 220,
      }}
    >
      <motion.div
        variants={highlightVariant}
        animate={highlighted ? "highlighted" : "rest"}
      >
        <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
          {/* wire icon set of your choice here */}
          <span>{step.icon}</span>
          <div style={{ fontWeight: 600 }}>{step.label}</div>
        </div>
        <div style={{ marginTop: 8, fontSize: 12, opacity: 0.85 }}>
          Status: {step.status}
        </div>
      </motion.div>
    </motion.div>
  )
}
```


***

## Edge animation

Render edges in an `<svg>` with animated “data flow” circles.

```tsx
// components/WorkflowEdges.tsx
import { motion } from "motion/react"
import type { WorkflowLayout } from "../layout/workflowLayout"

interface Props {
  layout: WorkflowLayout
}

export function WorkflowEdges({ layout }: Props) {
  const byId = new Map(layout.steps.map(s => [s.id, s]))

  return (
    <svg
      width="100%"
      height="100%"
      style={{ position: "absolute", inset: 0, pointerEvents: "none" }}
    >
      <defs>
        <linearGradient id="edgeGradient" x1="0" x2="1" y1="0" y2="0">
          <stop offset="0%" stopColor="#38bdf8" stopOpacity="0.2" />
          <stop offset="100%" stopColor="#22c55e" stopOpacity="0.9" />
        </linearGradient>
      </defs>

      {layout.edges.map(edge => {
        const from = byId.get(edge.from)!
        const to = byId.get(edge.to)!
        const midX = (from.x + to.x) / 2
        const path = `M ${from.x} ${from.y} C ${midX} ${from.y}, ${midX} ${to.y}, ${to.x} ${to.y}`

        return (
          <g key={`${edge.from}-${edge.to}`}>
            <path
              d={path}
              fill="none"
              stroke="url(#edgeGradient)"
              strokeWidth={2}
              strokeLinecap="round"
            />
            {/* moving dot */}
            <motion.circle
              r={4}
              fill="#eab308"
              initial={{ offsetDistance: "0%" }}
              animate={{ offsetDistance: "100%" }}
              transition={{
                duration: 2.4,
                repeat: Infinity,
                ease: "easeInOut",
              }}
              style={{
                offsetPath: `path("${path}")`,
              }}
            />
          </g>
        )
      })}
    </svg>
  )
}
```

This uses Motion’s ability to animate arbitrary CSS properties, including `offsetDistance` along a `path()`.[^1][^4]

***

## Main viewer component

Wire everything together in a container that handles search input and modal state.

```tsx
// components/WorkflowViewer.tsx
import { useMemo, useState } from "react"
import { motion, AnimatePresence } from "motion/react"
import type { WorkflowDefinition } from "../types/workflow"
import { computeLayout } from "../layout/workflowLayout"
import { WorkflowNode } from "./WorkflowNode"
import { WorkflowEdges } from "./WorkflowEdges"

interface Props {
  workflow: WorkflowDefinition
}

export function WorkflowViewer({ workflow }: Props) {
  const layout = useMemo(() => computeLayout(workflow), [workflow])

  const [selectedStepId, setSelectedStepId] = useState<string | null>(null)
  const [docRef, setDocRef] = useState("")
  const [focusedDoc, setFocusedDoc] = useState<string | null>(null)

  const highlightedStepIds = useMemo(() => {
    if (!focusedDoc) return new Set<string>()
    const ids = workflow.steps
      .filter(
        s =>
          s.documents_required.includes(focusedDoc) ||
          s.documents_generated.includes(focusedDoc),
      )
      .map(s => s.id)
    return new Set(ids)
  }, [workflow.steps, focusedDoc])

  function handleSearchSubmit(e: React.FormEvent) {
    e.preventDefault()
    const value = docRef.trim()
    if (!value) {
      setFocusedDoc(null)
      return
    }
    setFocusedDoc(value)
    // optional: scroll into view of first highlighted node
    // using document.querySelector and scrollIntoView
  }

  const selectedStep =
    selectedStepId && workflow.steps.find(s => s.id === selectedStepId)

  return (
    <div
      style={{
        position: "relative",
        width: "100%",
        height: "100%",
        overflow: "hidden",
        background:
          "radial-gradient(circle at top, #0f172a 0, #020617 45%, #020617 100%)",
        color: "white",
      }}
    >
      {/* top search bar */}
      <form
        onSubmit={handleSearchSubmit}
        style={{
          position: "absolute",
          top: 16,
          left: "50%",
          transform: "translateX(-50%)",
          display: "flex",
          gap: 8,
          zIndex: 10,
          alignItems: "center",
        }}
      >
        <input
          value={docRef}
          onChange={e => setDocRef(e.target.value)}
          placeholder="Reference number / document id..."
          style={{
            padding: "8px 12px",
            borderRadius: 999,
            border: "1px solid rgba(148,163,184,0.6)",
            background: "rgba(15,23,42,0.9)",
            color: "white",
            minWidth: 260,
          }}
        />
        <button
          type="submit"
          style={{
            padding: "8px 16px",
            borderRadius: 999,
            border: "none",
            background:
              "linear-gradient(90deg, #22c55e, #3b82f6, #a855f7)",
            color: "black",
            fontWeight: 600,
            cursor: "pointer",
          }}
        >
          Trace
        </button>
      </form>

      {/* canvas */}
      <div
        style={{
          position: "absolute",
          inset: 0,
          overflow: "auto",
          paddingTop: 80,
          paddingBottom: 80,
        }}
      >
        <div
          style={{
            position: "relative",
            width: "100%",
            height: "100%",
          }}
        >
          <WorkflowEdges layout={layout} />
          {layout.steps.map(step => (
            <WorkflowNode
              key={step.id}
              step={step}
              highlighted={highlightedStepIds.has(step.id)}
              onClick={() => setSelectedStepId(step.id)}
            />
          ))}
        </div>
      </div>

      {/* modal */}
      <AnimatePresence>
        {selectedStep && (
          <motion.div
            key="modal"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            style={{
              position: "absolute",
              inset: 0,
              background: "rgba(15,23,42,0.85)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              zIndex: 20,
            }}
            onClick={() => setSelectedStepId(null)}
          >
            <motion.div
              initial={{ scale: 0.8, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.9, opacity: 0 }}
              transition={{ type: "spring", stiffness: 220, damping: 22 }}
              style={{
                background: "#020617",
                borderRadius: 20,
                padding: 24,
                minWidth: 360,
                maxWidth: 520,
                boxShadow: "0 24px 80px rgba(0,0,0,0.7)",
              }}
              onClick={e => e.stopPropagation()}
            >
              <div style={{ display: "flex", justifyContent: "space-between" }}>
                <div style={{ fontSize: 18, fontWeight: 600 }}>
                  {selectedStep.label}
                </div>
                <div>{selectedStep.status}</div>
              </div>

              <div style={{ marginTop: 16, fontSize: 13, opacity: 0.9 }}>
                <div>Step ID: {selectedStep.id}</div>
                <div style={{ marginTop: 8 }}>
                  Documents required:{" "}
                  {selectedStep.documents_required.join(", ") || "None"}
                </div>
                <div style={{ marginTop: 4 }}>
                  Documents generated:{" "}
                  {selectedStep.documents_generated.join(", ") || "None"}
                </div>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}
```

This gives you: full‑screen animated flow, clickable nodes, animated modal, search highlighting, and branch/merge visualization using Motion’s layout and transition primitives.[^2]

***

## Document reference behaviour

Binding between reference input and workflow:

- On submit, interpret the reference as either a canonical document ID or a mapped alias; your back‑end module should resolve that to a canonical `documentId`.
- The viewer then:
    - Highlights all steps where `documents_required` or `documents_generated` includes that ID (yellow outline + pulsing glow).
    - Optionally animates a subtle path highlighting from the earliest relevant step through to `closed` using stronger edge gradient and faster moving dots.
    - Optionally auto-focuses camera (scroll, or pan/zoom if you wrap the canvas in a transform) to the most “current” step for that document.

***

## Forks and convergence

- Forks: same `from` node with multiple `next` IDs; layout spreads those children horizontally on the next level, edges fan out visibly.
- Convergence: multiple `incoming` IDs into the same `to` node; edges converge visually, and the modal for that node lists all predecessor steps, so the user sees exactly which branches must complete before progressing.
- You can additionally encode dependency semantics in status: a converging node only becomes `in_progress` or `unblocked` when all incoming steps are `completed`, and Motion animates the status change with a morph in color and glow.

***

## Integration with your workflow module

- Your existing module that defines “complete workflow, transitions, dependencies, status” should expose a clean JSON API: `getWorkflowForDocument(ref: string) → { workflow, effectiveStatuses, currentStepIds }`.
- The viewer stays stateless: it receives `workflow` + `statuses` and re-renders; Motion’s layout animations will smoothly transition nodes when statuses change over time (e.g. live updates from the backend).[^4][^2]
<span style="display:none">[^10][^11][^12][^13][^14][^15][^16][^17][^18][^19][^20][^21][^22][^23][^24][^25][^26][^27][^28][^29][^30][^31][^32][^33][^34][^35][^36][^37][^38][^39][^40][^41][^42][^43][^44][^45][^46][^47][^48][^49][^5][^50][^51][^52][^53][^54][^55][^56][^57][^58][^59][^6][^60][^61][^62][^63][^7][^8][^9]</span>

<div align="center">⁂</div>

[^1]: https://motion.dev/docs/quick-start

[^2]: https://motion.dev/docs/react

[^3]: https://github.com/matiaspakua/notaire

[^4]: https://motion.dev/docs

[^5]: https://www.jmir.org/2026/1/e86078

[^6]: http://www.thieme-connect.de/DOI/DOI?10.1055/s-0041-1736625

[^7]: https://movementdisorders.onlinelibrary.wiley.com/doi/10.1002/mdc3.70287

[^8]: https://ejournal.unibo.ac.id/index.php/DNursing/article/view/1273

[^9]: https://genius.inspira.or.id/index.php/indogenius/article/view/713

[^10]: https://genius.inspira.or.id/index.php/indogenius/article/view/689

[^11]: https://judikatif-upiyptk.org/ojs/index.php/judikatif/article/view/108

[^12]: https://jakk.candle.or.id/index.php/jakk/article/view/83

[^13]: https://tidesandcurrents.noaa.gov/myports/animation/docs/overview-summary-Motion.js.html

[^14]: https://docs.axyz-design.com/article/motion-flow-mode/

[^15]: https://www.reinaldo.pt/posts/processing-storyboard-case-study

[^16]: https://news.ycombinator.com/item?id=28616043

[^17]: https://www.youtube.com/watch?v=5LVfwQyclzo

[^18]: https://www.frontend.fyi/course/motion

[^19]: https://knaap.dev/posts/a-short-introduction-to-motion-one/

[^20]: https://www.reddit.com/r/AfterEffects/comments/jbo1kb/creating_flow_animation/

[^21]: https://reactflow.dev

[^22]: https://bestofjs.org/projects/motion-one

[^23]: https://dribbble.com/search/flow-animation

[^24]: https://github.com/thomasdufourd/motion-one-react-animations-poc

[^25]: https://www.reddit.com/r/javascript/comments/pslk89/motion_one_the_web_animations_api_for_everyone/

[^26]: https://www.youtube.com/watch?v=STq7LFohs3A

[^27]: https://www.youtube.com/watch?v=9-fO_2xTpgY

[^28]: https://www.tandfonline.com/doi/full/10.1080/09588221.2022.2083176

[^29]: https://ieeexplore.ieee.org/document/11200949/

[^30]: https://ieeexplore.ieee.org/document/11200926/

[^31]: https://ieeexplore.ieee.org/document/11200685/

[^32]: https://ieeexplore.ieee.org/document/11200588/

[^33]: https://ieeexplore.ieee.org/document/11200930/

[^34]: https://ieeexplore.ieee.org/document/10704396/

[^35]: https://journals.sagepub.com/doi/10.1177/14687941211045192

[^36]: https://www.wix.com/playground/post/motion-made-simple-timeline-animation-api

[^37]: https://stackoverflow.com/questions/71890064/question-about-running-multiple-animations-in-sequence-with-framer-motion

[^38]: https://github.com/motiondivision/motion

[^39]: https://docs.spline.design/designing-in-3-d/timeline-animation

[^40]: https://www.reddit.com/r/FigmaDesign/comments/1jfqj79/animating_multiple_elements_with_one_click/

[^41]: https://www.shiralbin.com/timeline-animation-api

[^42]: https://motion.dev/docs/react-layout-animations

[^43]: https://www.framer.com/dictionary/framer-motion

[^44]: https://developer.mozilla.org/es/docs/Web/API/Animation/timeline

[^45]: https://www.youtube.com/watch?v=1Xvk3s5mEjg

[^46]: https://magicui.design/blog/framer-motion-react

[^47]: https://arxiv.org/pdf/2210.14419.pdf

[^48]: https://arxiv.org/pdf/2103.03198.pdf

[^49]: https://www.getprog.ai/profile/20072974

[^50]: https://paacademy.com/course/cinematic-architecture-ai-powered-design-workflows

[^51]: https://github.com/notaryproject/.github

[^52]: https://iconscout.com/jp/lottie-animations/workflow

[^53]: https://github.com/notaryproject/notaryproject.dev

[^54]: https://www.hablarenarte.com/es/proyecto/id/notar-v-resolucin

[^55]: https://github.com/matiaspakua/tech.notes.io/actions

[^56]: https://www.animationsherpa.com

[^57]: https://github.com/notaryproject

[^58]: https://www.youtube.com/watch?v=wjaPUrF-tXY

[^59]: https://gist.github.com/patak-dev/a6f1858670b54ebf54d7e241c65dc5d4

[^60]: https://www.youtube.com/watch?v=oLxMIJpmPQo

[^61]: https://github.com/mathias/mathias.github.com/actions

[^62]: https://www.youtube.com/playlist?list=PLkwFGvHb8KtvhTVYxhsJJKRScxf8pss24

[^63]: https://github.com/notaryproject/notary

