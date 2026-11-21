from pathlib import Path

from repolens.extractors import extract
from repolens.extractors.common import Options, chunk_lines, normalize_route
from repolens.scanner import detect

from .conftest import FILES

OPTS = Options("p", {"issue": r"(?<![\w&#/=])#(\d{2,6})\b", "adr": r"\bADR-(\d{1,4})\b"}, ["/api/v1", "/api"])


def run(repo: Path, rel: str):
    lang, family = detect(rel)
    return extract(OPTS, repo, rel, lang, family)


def refs(ex, kind):
    return {(r.src.split("#")[-1], r.target, r.extra) for r in ex.refs if r.kind == kind}


def test_java_symbols_routes_and_inheritance(repo):
    ex = run(repo, "backend/src/main/java/com/acme/web/CodeController.java")
    quals = {n.qualname for n in ex.nodes}
    assert {"CodeController", "CodeController.check", "CodeController.show"} <= quals
    assert ("CodeController.check", "/api/v1/codes/verify", "POST") in refs(ex, "route")
    assert ("CodeController.show", "/api/v1/codes/{}", "GET") in refs(ex, "route")
    assert ex.meta["package"] == "com.acme.web"
    svc = run(repo, "backend/src/main/java/com/acme/service/CodeVerificationService.java")
    assert ("CodeVerificationService", "VerifyCodeUseCase", "") in refs(svc, "inherit")
    signature = next(n.signature for n in svc.nodes if n.qualname == "CodeVerificationService.verify")
    assert signature.startswith("public boolean verify") and "@Override" not in signature


def test_java_doc_comment_and_table_annotation(repo):
    port = run(repo, "backend/src/main/java/com/acme/port/VerifyCodeUseCase.java")
    assert "one-time codes" in next(n.text for n in port.nodes if n.qualname == "VerifyCodeUseCase")
    entity = run(repo, "backend/src/main/java/com/acme/persistence/CodeJpaEntity.java")
    assert ("CodeJpaEntity", "codes", "uses") in refs(entity, "table")


def test_mockmvc_call_is_an_http_call(repo):
    ex = run(repo, "backend/src/test/java/com/acme/service/CodeVerificationServiceTest.java")
    assert ("CodeVerificationServiceTest.shouldVerify", "/api/v1/codes/verify", "POST") in refs(ex, "http_call")


def test_typescript_object_literal_gateway(repo):
    ex = run(repo, "frontend/src/adapters/code-gateway.ts")
    quals = {(n.kind, n.qualname) for n in ex.nodes}
    assert ("variable", "codeGateway") in quals and ("method", "codeGateway.verify") in quals
    assert ("codeGateway", "CodeGateway", "") in refs(ex, "inherit")
    assert ("codeGateway.verify", "/codes/verify", "POST") in refs(ex, "http_call")
    assert {r.target for r in ex.refs if r.kind == "import"} == {"@/lib/api", "@/core/ports"}


def test_tsx_components_and_jsx_usage(repo):
    ex = run(repo, "frontend/src/components/VerifyForm.tsx")
    assert {"VerifyForm", "CodeInput"} <= {n.name for n in ex.nodes}
    assert ("VerifyForm", "CodeInput", "") in refs(ex, "call")


def test_python_classes_functions_and_fastapi_route(repo):
    ex = run(repo, "tools/report.py")
    kinds = {(n.kind, n.qualname) for n in ex.nodes}
    assert {("class", "ReportBuilder"), ("method", "ReportBuilder.build"), ("function", "render_rows")} <= kinds
    assert ("report", "/api/v1/report", "GET") in refs(ex, "route")
    assert "verification report" in next(n.text for n in ex.nodes if n.qualname == "ReportBuilder")


def test_markdown_sections_links_mentions_tags(repo):
    ex = run(repo, "docs/adr/ADR-007-one-time-codes.md")
    sections = {n.qualname for n in ex.nodes if n.kind == "section"}
    assert "ADR-007 One-time codes > Decision" in sections
    links = {r.target for r in ex.refs if r.kind == "link"}
    assert "backend/src/main/java/com/acme/service/CodeVerificationService.java" in links
    assert "CodeController" in {r.target for r in ex.refs if r.kind == "mention"}
    assert {"issue:42", "adr:007"} <= {r.target for r in ex.refs if r.kind == "tag"}
    assert ("/api/v1/codes/verify", "POST") in {(r.target, r.extra) for r in ex.refs if r.kind == "http_call"}


def test_sql_tables(repo):
    ex = run(repo, "backend/src/main/resources/db/migration/V1__create_codes.sql")
    tables = {(r.target, r.extra.split("\t")[0]) for r in ex.refs if r.kind == "table"}
    assert ("codes", "defines") in tables and ("users", "uses") in tables


def test_csv_yaml_and_text(repo):
    csv_ex = run(repo, "data/people.csv")
    assert "columns: name, email, role" in csv_ex.nodes[0].signature
    yaml_ex = run(repo, "config/app.yaml")
    assert "service.verify_url=/api/v1/codes/verify" in yaml_ex.nodes[0].text
    assert "/api/v1/codes/verify" in {r.target for r in yaml_ex.refs if r.kind == "http_call"}


def test_tsconfig_paths_are_recorded(repo):
    ex = run(repo, "frontend/tsconfig.json")
    assert ex.meta["ts_paths"] == {"@/*": ["./src/*"]}


def test_binary_and_unparsable_fall_back(tmp_path):
    (tmp_path / "logo.png").write_bytes(b"\x89PNG\r\n\x1a\n\0\0")
    (tmp_path / "bad.json").write_text("{not json", encoding="utf-8")
    assert run(tmp_path, "logo.png").nodes[0].signature == "binary"
    assert run(tmp_path, "bad.json").nodes[0].text.startswith("{not json")


def test_pdf_without_or_with_pypdf(tmp_path):
    (tmp_path / "doc.pdf").write_bytes(b"%PDF-1.4 not really")
    ex = run(tmp_path, "doc.pdf")
    assert ex.nodes[0].kind == "file" and "pdf" in ex.nodes[0].text


def test_route_normalisation_and_chunks():
    assert normalize_route("/api/v1/people/${id}/docs?x=1") == "/api/v1/people/{}/docs"
    assert normalize_route("{{baseUrl}}/a/:id/") == "{}/a/{}"
    chunks = chunk_lines(["x" * 100] * 40, size=1000)
    assert chunks[0][:2] == (1, 10) and chunks[-1][1] == 40


def test_every_fixture_file_is_detected():
    families = {detect(p)[1] for p in FILES}
    assert {"code", "markdown", "sql", "structured", "tabular", "text"} <= families


def test_locals_of_anonymous_functions_are_not_module_symbols(tmp_path):
    (tmp_path / "suite.test.ts").write_text(
        "export const API_BASE = '/api';\n"
        "suite('Gateway', () => {\n"
        "  const local = createStub();\n"
        "  function helper() { const inner = 1; return inner; }\n"
        "  test('calls', () => { const value = local.get(); helper(); });\n"
        "});\n"
        "(function () { const hidden = 2; })();\n", encoding="utf-8")
    ex = run(tmp_path, "suite.test.ts")
    names = {n.name: n.kind for n in ex.nodes if n.kind != "file"}
    assert names == {"API_BASE": "variable", "helper": "function"}
    assert ("p:suite.test.ts", "createStub", "") in refs(ex, "call")


def test_license_headers_are_not_documentation(tmp_path):
    (tmp_path / "json.ts").write_text(
        "/*---------------------------------------------\n"
        " *  Copyright (c) Example Corp. All rights reserved.\n"
        " *  Licensed under the MIT License.\n"
        " *--------------------------------------------*/\n"
        "/** Scanner and parser for JSON with comments. */\n"
        "export function parse(text: string) { return text; }\n", encoding="utf-8")
    (tmp_path / "Factory.java").write_text(
        "/*\n * Copyright 2002-present the original author or authors.\n"
        " * Licensed under the Apache License, Version 2.0 (the \"License\");\n */\n\n"
        "package org.example;\n\n/** Chooses the proxy type. */\npublic class Factory {}\n", encoding="utf-8")
    ts = run(tmp_path, "json.ts")
    assert ts.nodes[0].text.startswith("Scanner and parser for JSON") and "Copyright" not in ts.nodes[0].text
    assert "parse" in ts.nodes[0].text
    assert next(n.text for n in ts.nodes if n.name == "parse") == "Scanner and parser for JSON with comments."
    java = run(tmp_path, "Factory.java")
    assert "License" not in java.nodes[0].text and "Factory" in java.nodes[0].text
    assert next(n.text for n in java.nodes if n.name == "Factory") == "Chooses the proxy type."
