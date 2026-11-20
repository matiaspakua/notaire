import { pair } from "@/lib/i18n/es";

/** RepoLens showcase copy (#2315). English keys feed the ES dictionary via pair(). */

export const REPOLENS_BASICS = {
  eyebrow: pair("AI Factory · RepoLens", "AI Factory · RepoLens"),
  title: pair("RepoLens: local code knowledge for agents", "RepoLens: conocimiento local del código para agentes"),
  lead: pair(
    "RepoLens is a local code knowledge graph for AI agents. It indexes a worktree into typed symbols and edges, then answers locate questions over CLI, HTTP, or MCP — without sending code off the machine.",
    "RepoLens es un grafo local de conocimiento del código para agentes de IA. Indexa un worktree en símbolos y aristas tipados, y responde preguntas de localización por CLI, HTTP o MCP — sin enviar código fuera de la máquina.",
  ),
};

export const REPOLENS_SECTIONS = {
  basicsTitle: pair("What it is", "Qué es"),
  basicsBody: pair(
    "Agents waste turns grepping for the right file. RepoLens builds a local index once, then ranks files and symbols for a query so the agent starts closer to the change.",
    "Los agentes pierden turnos buscando el archivo correcto con grep. RepoLens construye un índice local una vez y ordena archivos y símbolos para una consulta, así el agente empieza más cerca del cambio.",
  ),
  howTitle: pair("How it works", "Cómo funciona"),
  howLead: pair(
    "Basics first, then the retrieval path. English technical terms stay in English where a translation would be worse.",
    "Primero lo básico, luego el camino de retrieval. Los términos técnicos en inglés se mantienen cuando una traducción empeora la claridad.",
  ),
  factoryTitle: pair("How it plugs into the factory", "Cómo se enchufa a la fábrica"),
  factoryLead: pair(
    "RepoLens is the repository-knowledge tool of the AI SDLC factory, not a product feature of Notaire.",
    "RepoLens es la herramienta de conocimiento del repositorio de la fábrica AI SDLC, no una función del producto Notaire.",
  ),
  loopTitle: pair("Self-improvement loop", "Bucle de auto-mejora"),
  loopLead: pair(
    "Every issue can measure locate work both ways. Friction becomes a fix in RepoLens or in the factory profile — without claiming a percentage when the sample is small.",
    "Cada issue puede medir el trabajo de localización de las dos formas. La fricción se convierte en un arreglo en RepoLens o en el perfil de la fábrica — sin inventar un porcentaje cuando la muestra es pequeña.",
  ),
  versionTitle: pair("This version", "Esta versión"),
  versionLead: pair(
    "The version and the notes below are read from the RepoLens package and its changelog when this site is built. They are not typed on the page.",
    "La versión y las notas de abajo se leen del paquete RepoLens y de su changelog cuando se construye este sitio. No se escriben en la página.",
  ),
  benchesTitle: pair("Latest committed benchmarks", "Últimos benchmarks commitidos"),
  benchesLead: pair(
    "Charts below are generated from the committed result files under framework/ai-sdlc/repolens/bench/results/. Nothing is typed by hand on this page.",
    "Los gráficos de abajo se generan desde los archivos de resultados commitidos en framework/ai-sdlc/repolens/bench/results/. Nada se escribe a mano en esta página.",
  ),
  pipelineTitle: pair("How RepoLens ranks code", "Cómo RepoLens ordena el código"),
  pipelineDesc: pair(
    "Five stages from the worktree to the agent: parse, store, embed, rank, then serve over CLI, MCP, or HTTP.",
    "Cinco etapas del worktree al agente: parsear, guardar, embedder, ordenar y servir por CLI, MCP o HTTP.",
  ),
  pipelineCaption: pair(
    "Step through the ranking pipeline. Numbers on this page still come only from the committed benches.",
    "Recorre el pipeline de ranking. Los números de esta página siguen saliendo solo de los benches commitidos.",
  ),
};

export const REPOLENS_STEPS = [
  {
    title: pair("Parse the tree", "Parsear el árbol"),
    body: pair(
      "tree-sitter (plus markdown, SQL, YAML, CSV, PDF helpers) extracts symbols and edges from the worktree.",
      "tree-sitter (más helpers de markdown, SQL, YAML, CSV y PDF) extrae símbolos y aristas del worktree.",
    ),
  },
  {
    title: pair("Store in SQLite + FTS5", "Guardar en SQLite + FTS5"),
    body: pair(
      "A local SQLite WAL database holds nodes, edges and full-text search. One index lives under .repolens/ in the worktree.",
      "Una base SQLite WAL local guarda nodos, aristas y búsqueda de texto completo. Un índice vive bajo .repolens/ en el worktree.",
    ),
  },
  {
    title: pair("Embed locally", "Embeddings locales"),
    body: pair(
      "Local embeddings (fastembed bge-small-en-v1.5 or LSA) land in a content-addressed vector cache under ~/.cache/repolens/vectors, shared across worktrees.",
      "Los embeddings locales (fastembed bge-small-en-v1.5 o LSA) van a una caché de vectores content-addressed en ~/.cache/repolens/vectors, compartida entre worktrees.",
    ),
  },
  {
    title: pair("Rank with typed Personalized PageRank", "Ordenar con Personalized PageRank tipado"),
    body: pair(
      "BM25, dense and exact anchors fuse (RRF), then typed Personalized PageRank walks the graph for hybrid or graph retrieval.",
      "Anclas BM25, densas y exactas se fusionan (RRF); luego Personalized PageRank tipado camina el grafo para retrieval híbrido o con grafo.",
    ),
  },
  {
    title: pair("Serve CLI / HTTP / MCP", "Servir CLI / HTTP / MCP"),
    body: pair(
      "The same index answers search and impact over the CLI, an HTTP API with a small UI, and MCP tools the factory stages can call.",
      "El mismo índice responde search e impact por CLI, una API HTTP con una UI pequeña, y herramientas MCP que llaman las etapas de la fábrica.",
    ),
  },
];

export const REPOLENS_FACTORY = [
  {
    title: pair("CLI calls can be a session", "Las llamadas CLI pueden ser una sesión"),
    body: pair(
      "From 0.6.2, a CLI search records REPOLENS_SESSION or CURSOR_CONVERSATION_ID. Without either, report still says 0 sessions and adoption n/a. That n/a is the measured state of the log before this release, not a claim that agents never searched.",
      "Desde 0.6.2, una búsqueda CLI registra REPOLENS_SESSION o CURSOR_CONVERSATION_ID. Sin ninguna de las dos, el reporte sigue diciendo 0 sesiones y adopción n/a. Esa n/a es el estado medido del log antes de esta versión, no una afirmación de que los agentes no buscaron.",
    ),
  },
  {
    title: pair("Stages plan, red, pre-pr", "Etapas plan, red, pre-pr"),
    body: pair(
      "The aisdlc profile attaches mcp = [\"repolens\"] on plan, red and pre-pr so agents can locate before they edit.",
      "El perfil aisdlc adjunta mcp = [\"repolens\"] en plan, red y pre-pr para que los agentes localicen antes de editar.",
    ),
  },
  {
    title: pair("Advisory signal gate", "Signal gate consultiva"),
    body: pair(
      "repolens report --gate reads usage.jsonl and can warn when locate work is invisible. It is advisory (required = false), not a hard merge block.",
      "repolens report --gate lee usage.jsonl y puede avisar cuando el trabajo de localización es invisible. Es consultiva (required = false), no un bloqueo duro de merge.",
    ),
  },
  {
    title: pair("Per-worktree index", "Índice por worktree"),
    body: pair(
      "Each worktree keeps its own .repolens/ store so parallel agent checkouts do not collide.",
      "Cada worktree guarda su propio almacén .repolens/ para que checkouts paralelos de agentes no choquen.",
    ),
  },
  {
    title: pair("Shared vector cache", "Caché de vectores compartida"),
    body: pair(
      "Embeddings are content-addressed under ~/.cache/repolens/vectors, so a new worktree can reuse vectors already computed on the machine.",
      "Los embeddings son content-addressed bajo ~/.cache/repolens/vectors, así un worktree nuevo puede reutilizar vectores ya calculados en la máquina.",
    ),
  },
];

export const REPOLENS_LOOP = [
  {
    title: pair("A/B locate per issue", "Localización A/B por issue"),
    body: pair(
      "Factory agents can run the same locate question with grep and with RepoLens, log calls, bytes and wall time, then keep both rows in factory-loop-comparison.md.",
      "Los agentes de la fábrica pueden correr la misma pregunta de localización con grep y con RepoLens, registrar llamadas, bytes y tiempo, y guardar ambas filas en factory-loop-comparison.md.",
    ),
  },
  {
    title: pair("Retrospective → product fix", "Retrospectiva → arreglo de producto"),
    body: pair(
      "Friction from live use became RepoLens 0.4.1 (CLI usage telemetry so report --gate sees CLI searches) and 0.4.2 (stop guessing ambiguous same-directory call edges).",
      "La fricción del uso en vivo se convirtió en RepoLens 0.4.1 (telemetría de uso CLI para que report --gate vea búsquedas CLI) y 0.4.2 (dejar de adivinar aristas de llamada ambiguas en el mismo directorio).",
    ),
  },
  {
    title: pair("Honest sample sizes", "Tamaños de muestra honestos"),
    body: pair(
      "The live paired sample is small. The committed comparison file says no percentage improvement is claimed when n is small — this page repeats that caveat.",
      "La muestra emparejada en vivo es pequeña. El archivo de comparación commitido dice que no se reclama una mejora porcentual cuando n es pequeña — esta página repite esa salvedad.",
    ),
  },
];

/** Pipeline diagram node / edge labels (#2318). Technical tokens stay in English. */
pair("Parse", "Parsear");
pair("Store", "Guardar");
pair("Embed", "Embed");
pair("Rank", "Ordenar");
pair("Serve", "Servir");
pair("symbols", "símbolos");
pair("chunks", "chunks");
pair("vectors", "vectores");
pair("hits", "hits");

/** Side-effect: register Spanish strings when this module is imported. */
void REPOLENS_BASICS;
void REPOLENS_SECTIONS;
void REPOLENS_STEPS;
void REPOLENS_FACTORY;
void REPOLENS_LOOP;
