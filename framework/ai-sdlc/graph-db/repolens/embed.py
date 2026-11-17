"""Embedding backends. `fastembed` (ONNX, local) by default; `lsa` needs only numpy/scipy; `none` disables."""

from __future__ import annotations

import importlib.util
import io
import json
import os
from collections import Counter
from pathlib import Path

import numpy as np

from .model import Node, split_identifier, text_hash

SKIP_KINDS = frozenset({"dir", "project", "external"})
PARALLEL_FROM = 5000
# Embedding text length: 300 chars matched 600 on both task sets and embeds ~30% faster (README, Performance).
EMBED_CHARS = 300


def embed_text(n: Node) -> str:
    words = " ".join(split_identifier(n.qualname or n.name))
    path = " ".join(split_identifier(n.path.replace("/", " "))) if n.path else ""
    return f"{n.kind} {words}. {n.signature} {n.text} ({path})"[:EMBED_CHARS]


class FastEmbedder:
    """fastembed (ONNX) model, loaded on first use: an index run with nothing to embed never pays for it."""

    def __init__(self, model: str, workers: int = 0):
        self.model_name = model
        self.name = f"fastembed:{model}"
        self.workers = workers
        self._model = None

    @property
    def model(self):
        if self._model is None:
            from fastembed import TextEmbedding

            cache = Path(os.environ.get("REPOLENS_MODEL_CACHE", Path.home() / ".cache" / "repolens" / "models"))
            cache.mkdir(parents=True, exist_ok=True)
            self._model = TextEmbedding(self.model_name, cache_dir=str(cache))
        return self._model

    def needs_fit(self) -> bool:
        return False

    def embed_stream(self, texts: list[str], chunk: int = 4096):
        """Yield normalised vectors in input order, `chunk` at a time. Large jobs use one pool of worker
        processes for the whole stream; small jobs stay in-process (pool start-up would dominate)."""
        workers = self.workers or min(8, max(1, (os.cpu_count() or 2) // 2))
        parallel = workers if len(texts) >= PARALLEL_FROM and workers > 1 else None
        buf: list[np.ndarray] = []
        for vec in self.model.embed(texts, batch_size=16, parallel=parallel):
            buf.append(vec)
            if len(buf) >= chunk:
                yield _normalize(np.asarray(buf, dtype=np.float32))
                buf = []
        if buf:
            yield _normalize(np.asarray(buf, dtype=np.float32))

    def embed_docs(self, texts: list[str]) -> np.ndarray:
        return np.vstack(list(self.embed_stream(texts)))

    def embed_query(self, text: str) -> np.ndarray:
        vec = next(iter(self.model.query_embed(text)))
        return _normalize(np.asarray(vec, dtype=np.float32)[None, :])[0]


class LsaEmbedder:
    """TF-IDF over identifier-split tokens, reduced with truncated SVD. Fitted on the indexed corpus."""

    def __init__(self, dim: int = 256, state: bytes | None = None):
        self.dim = dim
        self.name = f"lsa:{dim}"
        self.vocab: dict[str, int] = {}
        self.idf = np.zeros(0, dtype=np.float32)
        self.components = np.zeros((0, 0), dtype=np.float32)
        if state:
            self.load(state)

    def needs_fit(self) -> bool:
        return True

    @staticmethod
    def _tokens(text: str) -> list[str]:
        return [t for t in split_identifier(text) if len(t) > 1]

    def fit(self, texts: list[str]) -> None:
        from scipy.sparse.linalg import svds

        df: Counter = Counter()
        docs = [self._tokens(t) for t in texts]
        for toks in docs:
            df.update(set(toks))
        n = len(docs)
        terms = [t for t, c in df.most_common(60000) if c >= 2 or n < 50]
        self.vocab = {t: i for i, t in enumerate(terms)}
        self.idf = np.log((1 + n) / (1 + np.array([df[t] for t in terms], dtype=np.float32))) + 1
        mat = self._tfidf(docs)
        k = max(1, min(self.dim, min(mat.shape) - 1))
        if min(mat.shape) <= 1:
            self.components = np.eye(1, mat.shape[1], dtype=np.float32)
            return
        _u, _s, vt = svds(mat.astype(np.float64), k=k)
        self.components = vt.astype(np.float32)

    def _tfidf(self, docs: list[list[str]]):
        from scipy.sparse import csr_matrix

        rows, cols, vals = [], [], []
        for r, toks in enumerate(docs):
            for t, c in Counter(toks).items():
                j = self.vocab.get(t)
                if j is not None:
                    rows.append(r)
                    cols.append(j)
                    vals.append((1 + np.log(c)) * self.idf[j])
        mat = csr_matrix((vals, (rows, cols)), shape=(len(docs), max(1, len(self.vocab))), dtype=np.float32)
        norms = np.sqrt(np.asarray(mat.multiply(mat).sum(axis=1)).ravel())
        norms[norms == 0] = 1
        return mat.multiply(1 / norms[:, None]).tocsr()

    def embed_docs(self, texts: list[str]) -> np.ndarray:
        mat = self._tfidf([self._tokens(t) for t in texts])
        return _normalize(np.asarray(mat @ self.components.T, dtype=np.float32))

    def embed_stream(self, texts: list[str], chunk: int = 4096):
        for start in range(0, len(texts), chunk):
            yield self.embed_docs(texts[start:start + chunk])

    def embed_query(self, text: str) -> np.ndarray:
        return self.embed_docs([text])[0]

    def dump(self) -> bytes:
        buf = io.BytesIO()
        np.savez_compressed(buf, idf=self.idf, components=self.components,
                            vocab=np.frombuffer(json.dumps(self.vocab).encode(), dtype=np.uint8))
        return buf.getvalue()

    def load(self, state: bytes) -> None:
        data = np.load(io.BytesIO(state))
        self.idf = data["idf"]
        self.components = data["components"]
        self.vocab = json.loads(bytes(data["vocab"]).decode())


def _normalize(mat: np.ndarray) -> np.ndarray:
    norms = np.linalg.norm(mat, axis=1, keepdims=True)
    norms[norms == 0] = 1
    return mat / norms


def make_embedder(backend: str, model: str, dim: int, state: bytes | None = None, workers: int = 0):
    if backend == "none":
        return None
    if backend in ("auto", "fastembed"):
        if importlib.util.find_spec("fastembed") is not None:
            return FastEmbedder(model, workers)
        if backend == "fastembed":
            raise ImportError("fastembed is not installed: pip install 'repolens[embed]' or use backend = \"lsa\"")
    return LsaEmbedder(dim, state)


MIN_PCA_VECTORS = 2000
PCA_SAMPLE = 30000


class Projection:
    """PCA basis fitted once per model: v -> normalise((v - mean) @ components)."""

    def __init__(self, mean: np.ndarray, components: np.ndarray):
        self.mean = mean.astype(np.float32)
        self.components = components.astype(np.float32)

    @classmethod
    def fit(cls, vecs: np.ndarray, dims: int, sample: int = 30000) -> Projection:
        step = max(1, len(vecs) // sample)
        x = vecs[::step].astype(np.float32)
        mean = x.mean(axis=0)
        _u, _s, vt = np.linalg.svd(x - mean, full_matrices=False)
        return cls(mean, vt[:dims].T)

    def apply(self, vecs: np.ndarray) -> np.ndarray:
        return _normalize((np.asarray(vecs, dtype=np.float32) - self.mean) @ self.components)

    def dump(self) -> bytes:
        buf = io.BytesIO()
        np.savez(buf, mean=self.mean, components=self.components)
        return buf.getvalue()

    @classmethod
    def load(cls, blob: bytes) -> Projection:
        data = np.load(io.BytesIO(blob))
        return cls(data["mean"], data["components"])


class ProjectedEmbedder:
    def __init__(self, base, projection: Projection):
        self.base, self.projection, self.name = base, projection, base.name

    def embed_query(self, text: str) -> np.ndarray:
        return self.projection.apply(self.base.embed_query(text)[None, :])[0]


def _projection(store, name: str) -> Projection | None:
    blob = store.get_meta("pca")
    return Projection.load(blob) if blob and store.get_meta("pca_for") == name else None


def embed_store(store, cfg, log=print) -> dict:
    """Embed every distinct node text that has no vector yet (LSA is refitted on every run, in seconds).
    Texts are embedded shortest first so each batch pads to a similar length. Dense model vectors are
    reduced with a PCA basis fitted once (`[embed] reduce`), which shrinks storage, memory and scans."""
    embedder = make_embedder(cfg.embedder, cfg.embed_model, cfg.embed_dim, workers=cfg.embed_workers)
    if embedder is None:
        store.clear_vectors()
        store.set_meta("embedder", "none")
        return {"embedder": "none", "embedded": 0}
    if store.get_meta("embedder") != embedder.name:
        store.clear_vectors()
    if embedder.needs_fit():
        if store.get_meta("embedder") == embedder.name and store.count_vectors() \
                and next(store.missing_vectors(SKIP_KINDS), None) is None:
            return {"embedder": embedder.name, "embedded": 0}
        unique = {text_hash(embed_text(n)): embed_text(n) for n in store.nodes() if n.kind not in SKIP_KINDS}
        embedder.fit(list(unique.values()))
        store.set_meta("lsa_state", embedder.dump())
        store.clear_vectors()
    else:
        unique = {text_hash(t): t for t in (embed_text(n) for n in store.missing_vectors(SKIP_KINDS))}
    todo = sorted(unique.items(), key=lambda kv: len(kv[1]))
    reduce = 0 if embedder.needs_fit() else cfg.embed_reduce
    projection = _projection(store, embedder.name) if reduce else None
    done = 0
    for vecs in embedder.embed_stream([t for _, t in todo]) if todo else ():
        hashes = [h for h, _ in todo[done:done + len(vecs)]]
        store.put_vectors(hashes, projection.apply(vecs) if projection else vecs)
        done += len(vecs)
        if len(todo) > len(vecs):
            log(f"  embedded {done}/{len(todo)}")
    if reduce and projection is None and store.count_vectors() >= MIN_PCA_VECTORS:
        sample = store.sample_vectors(PCA_SAMPLE)
        if sample.shape[1] > reduce:
            projection = Projection.fit(sample, reduce)
            del sample
            total = 0
            for hashes, vecs in store.vector_chunks():  # bounded memory: one chunk at a time
                store.put_vectors(hashes, projection.apply(vecs))
                total += len(hashes)
            store.set_meta("pca", projection.dump())
            store.set_meta("pca_for", embedder.name)
            store.vacuum()
            log(f"  reduced {total} vectors to {reduce} dimensions")
    if todo:
        store.prune_vectors()
    store.set_meta("embedder", embedder.name)
    return {"embedder": embedder.name, "embedded": len(todo), "dims": reduce if projection else None}


def query_embedder(store, cfg):
    name = store.get_meta("embedder") or "none"
    if name == "none":
        return None
    if name.startswith("lsa:"):
        return LsaEmbedder(int(name.split(":")[1]), store.get_meta("lsa_state"))
    base = make_embedder("fastembed", name.split(":", 1)[1], cfg.embed_dim)
    projection = _projection(store, name)
    return ProjectedEmbedder(base, projection) if projection else base
