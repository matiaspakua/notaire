import shutil
import time

from repolens.config import load_config
from repolens.embed import LsaEmbedder
from repolens.linker import is_test_path
from repolens.linker import test_stem as stem_of
from repolens.scanner import list_files
from repolens.service import KnowledgeGraph

from .conftest import edge_set, make_config, write_repo

SVC = "backend/src/main/java/com/acme/service/CodeVerificationService.java"
PORT = "backend/src/main/java/com/acme/port/VerifyCodeUseCase.java"
CTRL = "backend/src/main/java/com/acme/web/CodeController.java"
GW = "frontend/src/adapters/code-gateway.ts"


def test_scan_honours_gitignore_and_skips_secrets(kg):
    files = list_files(kg.cfg.sources[0], kg.cfg)
    assert "ignored/notes.txt" not in files and ".env" not in files
    assert GW in files and "data/people.csv" in files


def test_structural_edges(kg):
    edges = edge_set(kg)
    assert (f"{SVC}#CodeVerificationService", f"{PORT}#VerifyCodeUseCase", "inherits") in edges
    assert (f"{SVC}#CodeVerificationService.verify", f"{PORT}#VerifyCodeUseCase.verify", "inherits") in edges
    assert (f"{SVC}#CodeVerificationService", f"{SVC.replace('service/CodeVerificationService', 'port/CodeStorePort')}"
            "#CodeStorePort", "references") in edges
    assert (f"{SVC}", f"{PORT}#VerifyCodeUseCase", "imports") in edges


def test_cross_stack_route_edges(kg):
    edges = edge_set(kg)
    route = "route:POST /api/v1/codes/verify"
    assert (route, f"{CTRL}#CodeController.check", "handles") in edges
    assert (f"{GW}#codeGateway.verify", route, "http_call") in edges
    assert (f"{GW}#codeGateway", "frontend/src/core/ports.ts#CodeGateway", "inherits") in edges
    assert ("backend/src/test/java/com/acme/service/CodeVerificationServiceTest.java#"
            "CodeVerificationServiceTest.shouldVerify", route, "http_call") in edges
    assert ("config/app.yaml", route, "http_call") in edges
    assert any(s.startswith("docs/adr/") and d == route and k == "mentions" for s, d, k in edges)


def test_imports_tests_tables_docs_tags_and_cochange(kg):
    edges = edge_set(kg)
    assert (GW, "frontend/src/lib/api.ts", "imports") in edges
    assert ("frontend/src/adapters/code-gateway.test.ts", GW, "tests") in edges
    assert ("backend/src/test/java/com/acme/service/CodeVerificationServiceTest.java", SVC, "tests") in edges
    assert ("backend/src/main/resources/db/migration/V1__create_codes.sql", "table:codes", "defines_table") in edges
    assert ("backend/src/main/java/com/acme/persistence/CodeJpaEntity.java#CodeJpaEntity", "table:codes",
            "uses_table") in edges
    assert ("docs/adr/ADR-007-one-time-codes.md#decision", SVC, "links") in edges
    assert ("docs/adr/ADR-007-one-time-codes.md#decision", f"{CTRL}#CodeController", "mentions") in edges
    assert ("docs/adr/ADR-007-one-time-codes.md", "tag:adr:7", "tagged") in edges
    assert (f"{CTRL}", GW, "co_change") in edges or (GW, CTRL, "co_change") in edges
    assert (GW, "tag:issue:43", "changed_for") in edges
    assert ("frontend/src/components/VerifyForm.tsx#VerifyForm", "frontend/src/components/VerifyForm.tsx#CodeInput",
            "calls") in edges


def test_an_exact_symbol_skips_the_embedding_query_and_lists_its_caller(kg):
    """Exact identifiers were ~1.9s in embed_query while rg was milliseconds, and search alone
    recalled 1/6 of the Folio.getDto caller files. A single identifier that the name index
    already hit must not embed, and a caller linked by a calls edge must still be returned."""
    eng = kg.engine
    calls = {"n": 0}
    real = eng.embedder

    class Counting:
        def embed_query(self, text):
            calls["n"] += 1
            return real.embed_query(text)

    eng._embedder = Counting()
    eng._embedder_loaded = True
    hits = eng.search("CodeVerificationService", k=5)
    assert any(h.path.endswith("CodeVerificationService.java") for h in hits)
    assert calls["n"] == 0
    prose = eng.search("verify one-time code service", k=5)
    assert any(h.path.endswith("CodeVerificationService.java") for h in prose)
    assert calls["n"] == 1
    real_fts = eng.store.fts
    eng.store.fts = lambda match, limit: []
    try:
        callers = eng.search("codeGateway.verify", k=8, graph=False)
    finally:
        eng.store.fts = real_fts
    assert any(h.path == "frontend/src/components/VerifyForm.tsx" for h in callers)


def test_search_finds_and_explains(kg):
    hits = kg.engine.search("verify one-time code service", k=5)
    assert any(h.path == SVC for h in hits)
    files = kg.engine.search("which frontend gateway calls the codes verify endpoint", k=6, mode="files")
    paths = [h.path for h in files]
    assert GW in paths and CTRL in paths
    assert all(h.score > 0 for h in files)


def test_scope_and_kind_filters(kg):
    docs = kg.engine.search("one-time codes decision", k=5, scope="docs")
    assert docs and all(h.path.endswith(".md") or h.kind == "tag" for h in docs)
    classes = kg.engine.search("code", k=5, kinds=["class"])
    assert classes and all(h.kind == "class" for h in classes)
    assert kg.engine.search("zzzz-nothing-matches-qqq", k=5) == []


def test_graph_walk_adds_structural_neighbours(kg):
    query = "VerifyCodeUseCase"
    lexical = {h.path for h in kg.engine.search(query, k=10, mode="files", graph=False)}
    walked = {h.path for h in kg.engine.search(query, k=10, mode="files", graph=True)}
    assert SVC in walked and CTRL in walked
    assert walked - lexical or lexical == walked


def test_node_resolution(kg):
    eng = kg.engine
    assert eng.ids[eng.resolve("CodeVerificationService")].endswith("#CodeVerificationService")
    assert eng.ids[eng.resolve(f"{SVC}:16")].endswith("#CodeVerificationService.verify")
    assert eng.ids[eng.resolve("CodeController.java")].endswith(CTRL)
    info = eng.node("CodeController.check")
    assert "handles" in info["in"]


def test_impact_path_outline_snippet_neighbors(kg):
    eng = kg.engine
    impact = eng.impact("CodeStorePort")
    assert SVC in impact["files"] and "backend/src/main/java/com/acme/persistence/CodeJpaEntity.java" in impact["files"]
    assert "backend/src/test/java/com/acme/service/CodeVerificationServiceTest.java" in impact["tests"]
    path = eng.path_between(f"{GW}#codeGateway.verify", "CodeController.check")
    assert path["found"] and [h["edge"] for h in path["hops"]] == ["http_call", "handles"]
    assert eng.path_between(GW, "CodeJpaEntity")["found"]
    outline = eng.outline(CTRL)
    assert [i["name"] for i in outline["items"]][:2] == ["CodeController", "verifier"] or \
        "check" in [i["name"] for i in outline["items"]]
    snip = eng.snippet("CodeController.check")
    assert "verifier.verify" in snip["code"]
    nb = eng.neighbors("CodeController.check", kinds=["handles"], direction="in")
    assert any("route:" in n["id"] for n in nb["nodes"])


def test_incremental_reindex(tmp_path):
    repo = write_repo(tmp_path / "r")
    kg = KnowledgeGraph(make_config(repo, tmp_path / "g.db"))
    first = kg.index(log=lambda *_: None)
    assert first["sources"]["acme"]["extracted"] == first["sources"]["acme"]["files"]
    again = kg.index(log=lambda *_: None)
    assert again["sources"]["acme"]["extracted"] == 0
    (repo / "tools/report.py").write_text("def changed():\n    return 1\n", encoding="utf-8")
    (repo / "data/people.csv").unlink()
    time.sleep(0.01)
    third = kg.index(log=lambda *_: None)
    assert third["sources"]["acme"]["extracted"] == 1 and third["sources"]["acme"]["removed"] == 1
    assert kg.engine.resolve("changed") is not None
    kg.close()


def test_multi_project_store(tmp_path, repo):
    other = tmp_path / "other"
    shutil.copytree(repo, other, ignore=shutil.ignore_patterns(".git"))
    cfg = make_config(repo, tmp_path / "multi.db")
    cfg.sources.append(type(cfg.sources[0])("other", other))
    kg = KnowledgeGraph(cfg)
    kg.index(log=lambda *_: None)
    assert {p["name"] for p in kg.store.projects()} == {"acme", "other"}
    hits = kg.engine.search("code verification service", k=10, project="other")
    assert hits and all(h.project == "other" for h in hits)
    kg.close()


def test_config_file_resolves_relative_paths(tmp_path):
    (tmp_path / "proj").mkdir()
    cfg_file = tmp_path / "repolens.toml"
    cfg_file.write_text('[index]\ndb = "store/g.db"\n[[source]]\nname = "x"\npath = "proj"\n'
                        '[ranking]\ndemote = ["old/**"]\nrestart = 0.3\n[ranking.weights]\ncalls = 2.0\n'
                        '[ranking.tuning]\nanchors = 4\n', encoding="utf-8")
    cfg = load_config(cfg_file)
    assert cfg.db_path == tmp_path / "store/g.db" and cfg.sources[0].path == tmp_path / "proj"
    assert cfg.demote == ["old/**"] and cfg.edge_weights["calls"] == 2.0
    assert cfg.tuning == {"anchors": 4.0, "restart": 0.3}


def test_profile_tuning_overrides_engine_defaults(repo, tmp_path):
    cfg = make_config(repo, tmp_path / "t.db")
    cfg.tuning = {"anchors": 3.0, "doc_anchors": 0.0, "restart": 0.3}
    kg = KnowledgeGraph(cfg)
    kg.index(log=lambda *_: None)
    assert kg.engine.tuning["anchors"] == 3.0 and kg.engine.tuning["rrf_k"] == 60
    assert kg.engine.search("verify one-time code service", k=5)
    kg.close()


def test_lsa_round_trip():
    texts = ["verify totp code", "persist enrollment table", "verify code service", "render report rows"]
    emb = LsaEmbedder(dim=8)
    emb.fit(texts)
    again = LsaEmbedder(dim=8, state=emb.dump())
    q = again.embed_query("verify code")
    sims = emb.embed_docs(texts) @ q
    assert int(sims.argmax()) in (0, 2)


def test_test_path_heuristics():
    assert is_test_path("src/test/java/FooTest.java") and is_test_path("a/b.test.ts") and is_test_path("test_x.py")
    assert not is_test_path("src/main/java/Foo.java")
    assert stem_of("MfaEnrollmentPersistencePgIntegrationTest.java") == "MfaEnrollmentPersistence"
    assert stem_of("mfa-http-gateway.test.ts") == "mfa-http-gateway"


def test_test_files_rank_lower_unless_the_query_asks_for_tests(kg):
    def files(query):
        return [h.path.rsplit("/", 1)[-1] for h in kg.engine.search(query, k=4, mode="files")]

    test_file = "CodeVerificationServiceTest.java"
    assert test_file not in files("code verification service")[:2]
    assert test_file in files("tests of the code verification service")[:2]
    assert files("CodeVerificationServiceTest")[0] == test_file


def test_sparse_pagerank_matches_the_exact_walk(kg):
    import numpy as np

    eng = kg.engine
    seeds = {eng.idx["acme:frontend/src/adapters/code-gateway.ts"]: 1.0}
    exact_nodes, exact_mass = eng.ppr(seeds)
    old = eng.tuning["ppr_sparse_from"]
    eng.tuning["ppr_sparse_from"] = 0
    try:
        nodes, mass = eng.ppr(seeds, eps=1e-9)
        top_sparse = list(nodes[np.argsort(-mass)][:10])
        assert eng.search("codes verify endpoint", k=3)
    finally:
        eng.tuning["ppr_sparse_from"] = old
    assert top_sparse == list(exact_nodes[np.argsort(-exact_mass)][:10])
    assert abs(mass.sum() - exact_mass.sum()) < 1e-3


def test_an_ambiguous_same_directory_call_is_not_guessed(tmp_path):
    """A call named getDto must not be attached to every getDto in the folder (#2307).

    A uniquely named method in that same folder still links. A typed receiver (`Deed deed`) links exactly
    its own method (#2336); a receiver of unknown type (a lambda parameter) is still not guessed.
    """
    root = tmp_path / "repo"
    src = root / "src"
    src.mkdir(parents=True)
    (src / "Deed.java").write_text(
        "package p;\npublic class Deed {\n"
        "  public String getDto() { return \"deed\"; }\n"
        "  public String onlyOnce() { return \"once\"; }\n}\n", encoding="utf-8")
    (src / "Folio.java").write_text(
        "package p;\npublic class Folio {\n  public String getDto() { return \"folio\"; }\n}\n", encoding="utf-8")
    (src / "Reader.java").write_text(
        "package p;\npublic class Reader {\n"
        "  public String show(Deed deed) { return deed.getDto(); }\n"
        "  public void each(java.util.List<Object> all) { all.forEach(x -> x.getDto()); }\n"
        "  public String once(Deed deed) { return deed.onlyOnce(); }\n}\n", encoding="utf-8")
    kg = KnowledgeGraph(make_config(root, tmp_path / "g.db"))
    kg.index(embed=False, log=lambda *_: None)
    edges = edge_set(kg)

    def targets(method: str) -> set[str]:
        return {dst for src_id, dst, kind in edges if kind == "calls" and src_id.endswith(method)}

    typed = targets("#Reader.show")
    assert any(item.endswith("#Deed.getDto") for item in typed)
    assert not any(item.endswith("#Folio.getDto") for item in typed)
    guessed = targets("#Reader.each")
    assert not any(item.endswith("#Deed.getDto") or item.endswith("#Folio.getDto") for item in guessed)
    assert any(item.endswith("#Deed.onlyOnce") for item in targets("#Reader.once"))


def test_an_unknown_name_is_not_answered_with_a_guess(kg):
    eng = kg.engine
    assert eng.resolve("CodeService") is None and eng.impact("CodeService") is None
    text = eng.missing("CodeService", "CodeController")
    assert text.startswith("not found: 'CodeService'") and "CodeVerificationService" in text
    assert "'CodeController'" not in text


def _first(hits, predicate):
    return next((n for n, h in enumerate(hits) if predicate(h)), len(hits))


def _search_without_doc_factor(kg, query):
    saved = kg.engine.tuning.get("doc_factor")
    kg.engine.tuning["doc_factor"] = 1.0
    try:
        return kg.engine.search(query, k=8)
    finally:
        if saved is None:
            del kg.engine.tuning["doc_factor"]
        else:
            kg.engine.tuning["doc_factor"] = saved


def test_a_question_about_code_ranks_the_code_above_the_prose_that_describes_it(kg):
    """Recorded miss (#2452): asked for a component, the spec sections that describe it ranked first."""
    query = "one-time codes verification function"
    is_doc = lambda h: h.kind == "section"  # noqa: E731
    is_verify = lambda h: h.name == "verify" and h.kind == "method"  # noqa: E731
    unweighted = _search_without_doc_factor(kg, query)
    weighted = kg.engine.search(query, k=8)
    assert _first(unweighted, is_doc) < _first(unweighted, is_verify)
    assert _first(weighted, is_verify) < _first(weighted, is_doc)


def test_a_question_that_names_documentation_keeps_its_weight(kg):
    query = "one-time codes verification function ADR"
    unweighted = [(h.id, h.score) for h in _search_without_doc_factor(kg, query)]
    assert [(h.id, h.score) for h in kg.engine.search(query, k=8)] == unweighted
