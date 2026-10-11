# Motion Design for the Web

Skill for AI agents that build visually strong websites through motion design, creative coding, and interactive web design. It produces modern, immersive, fast, and maintainable web experiences with free and open-source tools.

---

## Purpose

This skill guides visually outstanding websites with a coherent stack for animation, typography, graphics, textures, composition, interaction, and performance.

Goals:

- Build premium, editorial, or immersive web experiences, including ones aimed at showcase standards.
- Prefer visual consistency, performance, accessibility, and technical control.
- Use mature open-source libraries that work together and stay maintainable.
- Turn motion-design and advanced UX principles into concrete implementation decisions.

It is not meant for generic corporate sites, template layouts, or interfaces without a clear visual direction.

---

## When to use this skill

Use this skill when the user asks for one of these outcomes:

- A striking, immersive, or highly visual website.
- A site with narrative scroll, microinteractions, and advanced animation.
- An Awwwards-style experience, a premium portfolio, or a high-end landing page.
- A site with 3D, WebGL, textures, sophisticated transitions, or interactive storytelling.
- A visual system where typography, motion, composition, and graphics share one art direction.

Do not use this skill as the main basis for:

- CRUD dashboards without visual ambition.
- Conventional administrative forms.
- Sites whose main goal is delivery speed without visual refinement.

---

## Expected result

The output should be a website with these properties:

- A clear visual direction that is not generic.
- Intentional motion, not decoration.
- Typography with a strong hierarchy.
- Visual rhythm between sections.
- Depth through layers, contrast, texture, light, or 3D when it fits.
- Navigation that stays understandable despite the visual richness.
- Reasonable performance and progressive degradation.
- Accessibility sufficient for real production.

The goal is not to "add effects." The goal is a coherent experience where design, interaction, and technology reinforce each other.

---

## Operating principles

### 1. The concept leads

Do not pick libraries, animations, or styles before the visual concept is fixed.

Define first:

- Site type: portfolio, landing, brand experience, editorial, product, or digital installation.
- Tone: sober, futuristic, brutalist, cinematic, tactile, experimental, elegant, or technical.
- Visual density: minimal, balanced, or intense.
- Role of motion: utilitarian, expressive, narrative, or immersive.
- Depth: flat, pseudo-3D, partial WebGL, or a 3D scene as the lead.

Every later decision should follow from that.

### 2. Motion must communicate

Every animation should do at least one of these jobs:

- Guide attention.
- Explain hierarchy.
- Give spatial continuity.
- Show cause and effect.
- Increase a sense of material or depth.
- Create narrative rhythm.

Remove any animation that does none of those jobs.

### 3. Composition comes before effects

A mediocre page with animations is still mediocre. Resolve these first:

- Grid.
- Typographic hierarchy.
- Spacing.
- Contrast.
- Reading order.
- Grouping.
- Proportion between blocks.

Add motion, textures, 3D, and effects only after that.

### 4. One or two strong ideas per screen

Avoid piling on showy resources. A strong screen usually rests on one dominant combination:

- Typography plus scroll reveal.
- Image or 3D plus camera or scroll.
- Translucent layers plus texture plus parallax.
- Animated graphics plus narrative.

Too many ideas at once destroy focus.

### 5. Performance is a creative constraint

Visual quality does not justify slow interactions, broken scroll, or jank. Performance is part of the design.

---

## Recommended stack

Use this coherent, modern, open-source stack by default.

### Application base

- Next.js: primary framework for modern sites, with routing, SSR, optimization, and a solid React fit.
- React: composition and interactivity.
- TypeScript: required when the project has medium or high complexity.
- Vite: a valid alternative for smaller or experimental sites that do not need SSR.

### Styles and visual system

- Tailwind CSS: speed, consistency, and token-based design.
- Modern native CSS: variables, container queries, clamp, color-mix, and nesting.
- OKLCH: recommended color space for more controllable palettes and gradients.

### Primary motion

- GSAP: primary animation engine.
- GSAP ScrollTrigger: sequences tied to scroll.
- Motion One: a light alternative for microinteractions or simpler projects.
- Framer Motion: useful in React for component transitions, and it should not replace GSAP when the focus is an advanced visual experience.

### Scroll and kinetic navigation

- Lenis: modern smooth scroll that works with GSAP.

### 3D, WebGL, and advanced visuals

- Three.js: the standard for 3D on the web.
- React Three Fiber: the recommended layer when the project uses React.
- Drei: utilities that speed up React Three Fiber work.
- OGL: a minimal alternative when you want more control and less abstraction.
- GLSL or custom WebGL shaders: distortions, noise, displacement, blobs, living backgrounds, and material transitions.

### Data visualization

- D3: high-end custom charts.
- Observable Plot: elegant visualization with less complexity.
- Chart.js: only when simplicity is required and a sophisticated visual direction is not.

### Typography

- Fontshare: preferred source for quality and less aesthetic saturation.
- Google Fonts: extra support for well-maintained variable families.
- Variable fonts: recommended for weight, width, and performance flexibility.

### Iconography and vectors

- Lucide: primary iconography.
- Inline SVG: required for logos, masks, paths, and elements that will be animated.
- Lottie only when there is a clear reason. Avoid heavy dependence on exported assets when the same solution can be built with SVG, CSS, or JS.

### Texture and depth

- SVG filters.
- CSS masks.
- mix-blend-mode.
- backdrop-filter.
- Procedural noise in SVG or canvas.
- Gradients in OKLCH.

---

## Recommended base install

For a React/Next.js stack:

```bash
npx create-next-app@latest my-site --typescript --eslint --app
cd my-site
npm install gsap @studio-freight/lenis three @react-three/fiber @react-three/drei lucide-react d3
npm install -D tailwindcss postcss autoprefixer
```
