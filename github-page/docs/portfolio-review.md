# Portfolio review of the Pages site (#1977)

A hiring manager's read of <https://matiaspakua.github.io/notaire/> as it was after #1976, and what
#1977 changed. The goal: in under a minute a reader should know who built Notaire, what it proves,
and where the proof is.

## What a hiring manager saw (before)

| # | Problem | Why it matters |
|---|---------|----------------|
| 1 | The hero said "The Story of Notaire". Nothing said who built it, in what role, or why it is worth reading. | The first screen is the only one most reviewers read. |
| 2 | The numbers that make the case (465 archived OpenSpec changes, hundreds of tests, a CI-checked API contract) were two or three clicks deep. | Proof that is not on the first screen does not get read. |
| 3 | No before/after. "Modernisation" was a claim; the 2014 starting point was only in the README. | Without a baseline, the size of the change cannot be judged. |
| 4 | No screenshots of the product. The site described the app and never showed it. | Reviewers want to see a working system, not only a pipeline. |
| 5 | The case study, the strongest page, was the sixth link in the AI Factory sidebar. | The best evidence was hard to find. |
| 6 | The hero entrance and a particle canvas ran on load and ignored `prefers-reduced-motion`; the gate flow looped on a 16 s cycle with no pause or step control; the other diagrams were static Mermaid. | Motion that the reader did not ask for hurts accessibility and reads as decoration. |

## What changed

- **Hero with a value proposition.** "Portfolio project · Matías Miguez", one sentence on what was done
  (a 2014 university project rebuilt with AI agents that cannot skip a step), and three actions: the
  AI factory, the case study, the code.
- **Proof strip.** Six counts read from the repository when the site is built
  (`lib/ai-sdlc-facts.ts`, `lib/tech-facts.ts`), each linking to the page that explains it. No number
  is typed by hand.
- **What this project shows.** Three cards: legacy modernisation, AI with guardrails, production
  engineering, each linking to its deep page.
- **How every change ships.** The interactive factory diagram on the landing page.
- **Before and after.** The 2014 column comes from the project README (Java 1.6 Swing, MySQL 5 with no
  schema versioning, MD5 passwords, no tests, no containers); today's column is read from the repo.
- **The product, running.** Four screenshots of the Next.js app on the local Docker stack, with demo
  data, labelled as such.
- **Interactive diagrams.** Hexagon, a traced payment request, the Flyway timeline, the contract
  pipeline, the test layers, the CI/CD triggers and the factory gates. They are reader-driven: nothing
  moves until the reader presses Play or a step; every step is announced in an `aria-live` region and
  cites its source file; with reduced motion the steps change instantly; below `md` the SVG gives way
  to the step list.
- The particle canvas, smooth-scroll and the looping gate animation are gone.

## Still open (Matias decides)

- **Live demo.** A hosted read-only demo would be the strongest proof; it needs hosting and a reset job.
- **Outcome metrics.** The site shows effort and guardrails (changes, tests, gates). Lead time or
  defect-escape numbers would show outcomes, but they need a source that can be read at build time.
- **Personal framing.** Whether to add a short "about me" with role and contact, or to keep the site
  about the project.
