"""A small multi-language git repository indexed once per test session (LSA embedder: offline, deterministic)."""

from __future__ import annotations

import subprocess
from pathlib import Path

import pytest

from repolens.config import Config, Source
from repolens.service import KnowledgeGraph


@pytest.fixture(autouse=True)
def _isolated_home(tmp_path_factory, monkeypatch):
    """Never touch the user's ~/.cache/repolens (shared vector cache, models are read-only via HF cache)."""
    home = tmp_path_factory.mktemp("repolens-home")
    monkeypatch.setenv("REPOLENS_HOME", str(home))
    monkeypatch.setenv("REPOLENS_VECTOR_CACHE", str(home / "vectors"))
    monkeypatch.setenv("REPOLENS_REFRESH_SECONDS", "-1")
    monkeypatch.setenv("REPOLENS_DAEMON", "0")  # tests opt in to the resident service (tests/test_daemon.py)

FILES = {
    "backend/src/main/java/com/acme/port/VerifyCodeUseCase.java": """
package com.acme.port;

/** Verifies one-time codes for a user (#42). */
public interface VerifyCodeUseCase {
    boolean verify(int userId, String code);
}
""",
    "backend/src/main/java/com/acme/port/CodeStorePort.java": """
package com.acme.port;

public interface CodeStorePort {
    String find(int userId);
}
""",
    "backend/src/main/java/com/acme/service/CodeVerificationService.java": """
package com.acme.service;

import com.acme.port.CodeStorePort;
import com.acme.port.VerifyCodeUseCase;

@Service
public class CodeVerificationService implements VerifyCodeUseCase {
    private final CodeStorePort store;

    public CodeVerificationService(CodeStorePort store) {
        this.store = store;
    }

    @Override
    public boolean verify(int userId, String code) {
        return code.equals(store.find(userId));
    }
}
""",
    "backend/src/main/java/com/acme/web/CodeController.java": """
package com.acme.web;

import com.acme.port.VerifyCodeUseCase;

@RestController
@RequestMapping("/api/v1/codes")
public class CodeController {
    private final VerifyCodeUseCase verifier;

    public CodeController(VerifyCodeUseCase verifier) {
        this.verifier = verifier;
    }

    @PostMapping("/verify")
    public boolean check(@RequestBody Request body) {
        return verifier.verify(body.userId(), body.code());
    }

    @GetMapping("/{id}")
    public String show(@PathVariable int id) {
        return "code";
    }
}
""",
    "backend/src/main/java/com/acme/persistence/CodeJpaEntity.java": """
package com.acme.persistence;

import com.acme.port.CodeStorePort;

@Entity
@Table(name = "codes")
public class CodeJpaEntity implements CodeStorePort {
    public String find(int userId) {
        return null;
    }
}
""",
    "backend/src/test/java/com/acme/service/CodeVerificationServiceTest.java": """
package com.acme.service;

import com.acme.port.CodeStorePort;

class CodeVerificationServiceTest {
    void shouldVerify() {
        CodeVerificationService service = new CodeVerificationService(null);
        mockMvc.perform(post("/api/v1/codes/verify"));
    }
}
""",
    "backend/src/main/resources/db/migration/V1__create_codes.sql": """
-- Codes issued to users (#42)
CREATE TABLE IF NOT EXISTS codes (id SERIAL PRIMARY KEY, user_id INT REFERENCES users(id), code TEXT);
""",
    "frontend/tsconfig.json": '{ "compilerOptions": { "baseUrl": ".", "paths": { "@/*": ["./src/*"] } } }\n',
    "frontend/src/lib/api.ts": """
export async function apiPost<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(`/api/v1${path}`, { method: "POST", body: JSON.stringify(body) });
  return res.json();
}
""",
    "frontend/src/core/ports.ts": """
export interface CodeGateway {
  verify(code: string): Promise<boolean>;
}
""",
    "frontend/src/adapters/code-gateway.ts": """
import { apiPost } from "@/lib/api";
import type { CodeGateway } from "@/core/ports";

export const codeGateway: CodeGateway = {
  verify: (code) => apiPost<boolean>("/codes/verify", { code }),
};
""",
    "frontend/src/adapters/code-gateway.test.ts": """
import { codeGateway } from "./code-gateway";

it("verifies", async () => { await codeGateway.verify("123456"); });
""",
    "frontend/src/components/VerifyForm.tsx": """
import { codeGateway } from "@/adapters/code-gateway";

export function VerifyForm() {
  return <form onSubmit={() => codeGateway.verify("1")}><CodeInput /></form>;
}

function CodeInput() { return <input />; }
""",
    "tools/report.py": '''
"""Daily report of verified codes."""
from fastapi import FastAPI

app = FastAPI()


class ReportBuilder:
    """Builds the verification report."""

    def build(self) -> str:
        return render_rows()


def render_rows() -> str:
    return ""


@app.get("/api/v1/report")
def report():
    return ReportBuilder().build()
''',
    "docs/adr/ADR-007-one-time-codes.md": """# ADR-007 One-time codes

## Decision

Codes are verified by [the service](../../backend/src/main/java/com/acme/service/CodeVerificationService.java)
behind `CodeController` (issue #42). The endpoint is `POST /api/v1/codes/verify`.

## Consequences

Rotation is out of scope.
""",
    "data/people.csv": "name,email,role\nAna,ana@example.com,admin\nLuis,luis@example.com,user\n",
    "config/app.yaml": "service:\n  name: codes\n  verify_url: /api/v1/codes/verify\n",
    ".gitignore": "ignored/\n",
    "ignored/notes.txt": "should not be indexed\n",
    ".env": "SECRET=do-not-index\n",
}


def write_repo(root: Path) -> Path:
    for rel, text in FILES.items():
        path = root / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text.lstrip("\n"), encoding="utf-8")
    git = ["git", "-C", str(root), "-c", "user.name=t", "-c", "user.email=t@example.com"]
    subprocess.run([*git[:3], "init", "-q"], check=True)
    subprocess.run([*git, "add", "-A"], check=True)
    subprocess.run([*git, "commit", "-qm", "feat: codes (#42)"], check=True)
    for i in range(2):
        for rel in ("backend/src/main/java/com/acme/web/CodeController.java", "frontend/src/adapters/code-gateway.ts"):
            with open(root / rel, "a", encoding="utf-8") as fh:
                fh.write(f"// touch {i}\n")
        subprocess.run([*git, "commit", "-qam", f"fix: codes endpoint #{43 + i}"], check=True)
    return root


def make_config(root: Path, db: Path) -> Config:
    cfg = Config(db_path=db, sources=[Source("acme", root)])
    cfg.embedder = "lsa"
    cfg.workers = 1
    cfg.cochange_min_count = 2
    return cfg


@pytest.fixture(scope="session")
def repo(tmp_path_factory) -> Path:
    return write_repo(tmp_path_factory.mktemp("acme"))


@pytest.fixture(scope="session")
def kg(repo, tmp_path_factory) -> KnowledgeGraph:
    graph = KnowledgeGraph(make_config(repo, tmp_path_factory.mktemp("db") / "graph.db"))
    graph.index(log=lambda *_: None)
    yield graph
    graph.close()


def edge_set(kg: KnowledgeGraph) -> set[tuple[str, str, str]]:
    return {(s.split(":", 1)[1], d.split(":", 1)[1], k) for s, d, k, _w in kg.store.edges()}
