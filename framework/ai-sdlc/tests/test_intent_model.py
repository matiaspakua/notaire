"""Failing tests for the intent entry point model (#2395).

Each test maps to a Scenario in the OpenSpec delta spec.
At this stage (Gate 2 — red) they are expected to fail with ImportError
because aisdlc.intent_model does not yet exist.
"""
from __future__ import annotations

import json
import sys
import textwrap
import unittest

from helpers import KIT, Repo

sys.path.insert(0, str(KIT))

# Realistic spec Markdown fixture — mirrors a real GitHub issue.
SPEC_FIXTURE = textwrap.dedent("""\
    # Add rate limiting to factory API

    The factory API currently has no rate limiting.  Operators can fire
    unlimited requests and exhaust the agent budget in seconds.

    We need a simple token-bucket limiter per actor that caps at 60
    requests per minute and returns 429 when exceeded.

    ## Acceptance criteria

    - [ ] New endpoint returns 429 after 60 requests in a minute
    - [ ] Rate limit header is included in every response
    - [ ] Configurable limit via profile [rate-limit] section
    - [ ] Per-actor isolation (different actors get separate buckets)

    ## References

    - [ADR-030 hexagonal](docs/200-architecture/202-ADR/ADR-030.md) (architecture)
    - [Threat model](https://example.org/tm) (security)
    - [Style guide](https://example.org/style)
""")

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"
"""


class IntentModelSubmitTest(unittest.TestCase):
    """Scenarios: submit prompt intent, submit reference intent, secrets refused,
    unknown reference system, store mirror no-op."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)

    def _ctx(self):
        from aisdlc.cli import load_context

        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    # --- Scenario: submit prompt intent ---
    def test_submit_prompt_intent(self):
        from aisdlc.intent_model import submit

        ctx = self._ctx()
        intent = submit(ctx, text="Refactor authentication module", actor="test-agent")
        self.assertEqual("prompt", intent["kind"])
        self.assertTrue(intent["id"].startswith("int-"))
        self.assertEqual("Refactor authentication module", intent["title"])
        self.assertEqual([], intent["acceptance"])
        self.assertEqual([], intent["run_ids"])
        # Persisted to .aisdlc/intents/<id>/current.json
        current = self.repo.root / ".aisdlc" / "intents" / intent["id"] / "current.json"
        self.assertTrue(current.is_file())
        stored = json.loads(current.read_text(encoding="utf-8"))
        self.assertEqual(intent["id"], stored["id"])

    # --- Scenario: submit reference intent without network calls ---
    def test_submit_reference_intent(self):
        from aisdlc.intent_model import submit

        ctx = self._ctx()
        intent = submit(ctx, ref="github:#2395", actor="test-agent")
        self.assertEqual("reference", intent["kind"])
        self.assertEqual("github", intent["source"]["system"])
        self.assertEqual("#2395", intent["source"]["ref"])

        # Jira reference
        intent2 = submit(ctx, ref="jira:PROJ-42", actor="test-agent")
        self.assertEqual("jira", intent2["source"]["system"])
        self.assertEqual("PROJ-42", intent2["source"]["ref"])

    # --- Scenario: secrets in intent text are refused ---
    def test_secrets_refused(self):
        from aisdlc.errors import KitError
        from aisdlc.intent_model import submit

        ctx = self._ctx()
        # Real secret-shaped values matching factory.SECRET_VALUES patterns
        secret_texts = [
            "Use token ghp_ABCDEFGHIJKLMNOP for auth",
            "API key is sk-1234567890abcdef",
            "AWS key AKIAIOSFODNN7EXAMPLE",
            "Header: Bearer eyJhbGciOiJIUzI1NiJ9.test",
        ]
        for text in secret_texts:
            with self.subTest(secret=text):
                with self.assertRaises(KitError) as cm:
                    submit(ctx, text=text, actor="test-agent")
                self.assertEqual("literal_secret", cm.exception.code)
                # Nothing persisted
                intents_dir = self.repo.root / ".aisdlc" / "intents"
                if intents_dir.is_dir():
                    self.assertEqual(0, len(list(intents_dir.iterdir())))

    # --- Scenario: unknown reference system is rejected ---
    def test_unknown_reference_system(self):
        from aisdlc.errors import KitError
        from aisdlc.intent_model import submit

        ctx = self._ctx()
        with self.assertRaises(KitError):
            submit(ctx, ref="bitbucket:PROJ-1", actor="test-agent")

    # --- Scenario: optional store mirror hook no-op ---
    def test_store_mirror_noop(self):
        from aisdlc.intent_model import submit

        ctx = self._ctx()
        # No store module installed — submit must succeed silently
        intent = submit(ctx, text="Store mirror should not fail", actor="test-agent")
        self.assertIsNotNone(intent["id"])


class IntentModelSpecTest(unittest.TestCase):
    """Scenarios: parse markdown spec, spec missing title derived."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)

    def _ctx(self):
        from aisdlc.cli import load_context

        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    # --- Scenario: parse markdown spec into title, description, criteria, and typed references ---
    def test_parse_spec_markdown(self):
        from aisdlc.intent_model import parse_spec, submit

        parsed = parse_spec(SPEC_FIXTURE)
        self.assertEqual("Add rate limiting to factory API", parsed["title"])
        self.assertIn("rate limiting", parsed["text"].lower())
        self.assertGreater(len(parsed["acceptance"]), 0)
        self.assertTrue(any("429" in c for c in parsed["acceptance"]))
        self.assertTrue(any("header" in c.lower() for c in parsed["acceptance"]))

        refs = parsed["references"]
        ref_kinds = {r["label"]: r["kind"] for r in refs}
        self.assertEqual("architecture", ref_kinds.get("ADR-030 hexagonal"))
        self.assertEqual("security", ref_kinds.get("Threat model"))
        # Style guide has no explicit kind tag → inferred or "other"
        style_kind = ref_kinds.get("Style guide")
        self.assertIn(style_kind, ("other", "standard"))

        # Submit via spec
        ctx = self._ctx()
        spec_file = self.repo.root / "spec.md"
        spec_file.write_text(SPEC_FIXTURE, encoding="utf-8")
        intent = submit(ctx, spec=SPEC_FIXTURE, actor="test-agent")
        self.assertEqual("spec", intent["kind"])
        self.assertEqual("Add rate limiting to factory API", intent["title"])
        self.assertGreater(len(intent["acceptance"]), 0)
        self.assertGreater(len(intent["references"]), 0)

    # --- Scenario: spec file with missing title gets a derived title ---
    def test_spec_missing_title_derived(self):
        from aisdlc.intent_model import parse_spec

        spec_no_title = textwrap.dedent("""\
            This module needs a refactor because the error handling is scattered.

            ## Acceptance criteria

            - [ ] Centralize error handling
        """)
        parsed = parse_spec(spec_no_title)
        self.assertEqual("This module needs a refactor because the error handling is scattered.", parsed["title"])


class IntentModelVersioningAndLedgerTest(unittest.TestCase):
    """Scenario: intent versioning and ledger privacy event."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)

    def _ctx(self):
        from aisdlc.cli import load_context

        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    def test_intent_versioning_and_ledger(self):
        from aisdlc.intent_model import submit

        ctx = self._ctx()
        intent = submit(ctx, text="Version test", actor="test-agent")
        intent_id = intent["id"]

        # Version 1 file exists
        v1 = self.repo.root / ".aisdlc" / "intents" / intent_id / "versions" / "1.json"
        self.assertTrue(v1.is_file())
        v1_data = json.loads(v1.read_text(encoding="utf-8"))
        self.assertEqual(intent_id, v1_data["id"])

        # current.json matches version 1
        current = self.repo.root / ".aisdlc" / "intents" / intent_id / "current.json"
        current_data = json.loads(current.read_text(encoding="utf-8"))
        self.assertEqual(v1_data["id"], current_data["id"])

        # Ledger event: has id, kind, source, actor — but NOT the text
        ledger = self.repo.ledger()
        intent_events = [e for e in ledger if e.get("intent_id") == intent_id]
        self.assertGreater(len(intent_events), 0)
        event = intent_events[0]
        self.assertIn(intent_id, event.get("intent_id") or event.get("data", {}).get("id", ""))
        event_blob = json.dumps(event)
        self.assertNotIn("Version test", event_blob)


class IntentModelAttachRunTest(unittest.TestCase):
    """Test attach_run function."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)

    def _ctx(self):
        from aisdlc.cli import load_context

        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    def test_attach_run_appends_run_id(self):
        from aisdlc.intent_model import attach_run, submit

        ctx = self._ctx()
        intent = submit(ctx, text="Attach run test", actor="test-agent")
        self.assertEqual([], intent["run_ids"])

        updated = attach_run(ctx, intent["id"], "run-abc123")
        self.assertIn("run-abc123", updated["run_ids"])

        # A new version file is created
        v2 = self.repo.root / ".aisdlc" / "intents" / intent["id"] / "versions" / "2.json"
        self.assertTrue(v2.is_file())


class IntentModelListShowTest(unittest.TestCase):
    """Test list and show functions."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)

    def _ctx(self):
        from aisdlc.cli import load_context

        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    def test_list_returns_all_intents(self):
        from aisdlc.intent_model import list_intents, submit

        ctx = self._ctx()
        submit(ctx, text="First intent", actor="test-agent")
        submit(ctx, text="Second intent", actor="test-agent")

        result = list_intents(ctx)
        entries = result.get("entries", result) if isinstance(result, dict) else result
        self.assertGreaterEqual(len(entries), 2)

    def test_show_returns_intent_by_id(self):
        from aisdlc.errors import KitError
        from aisdlc.intent_model import show_intent, submit

        ctx = self._ctx()
        intent = submit(ctx, text="Show me", actor="test-agent")

        shown = show_intent(ctx, intent["id"])
        self.assertEqual(intent["id"], shown["id"])
        self.assertEqual("Show me", shown["title"])

        # Unknown id raises
        with self.assertRaises(KitError):
            show_intent(ctx, "int-nonexistent")


class IntentModelSecurityTest(unittest.TestCase):
    """Tests for path traversal, secrets in title, prompt source, and CLI file ref."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)

    def _ctx(self):
        from aisdlc.cli import load_context

        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    # --- Fix 2: path traversal in show_intent ---
    def test_show_intent_rejects_path_traversal(self):
        from aisdlc.errors import KitError
        from aisdlc.intent_model import show_intent

        ctx = self._ctx()
        with self.assertRaises(KitError) as cm:
            show_intent(ctx, "../../etc/passwd")
        self.assertEqual("invalid_id", cm.exception.code)

    # --- Fix 2: path traversal in attach_run ---
    def test_attach_run_rejects_path_traversal(self):
        from aisdlc.errors import KitError
        from aisdlc.intent_model import attach_run

        ctx = self._ctx()
        with self.assertRaises(KitError) as cm:
            attach_run(ctx, "../../../x", "run-abc")
        self.assertEqual("invalid_id", cm.exception.code)

    # --- Fix 3: secrets in title are refused ---
    def test_secrets_in_title_refused(self):
        from aisdlc.errors import KitError
        from aisdlc.intent_model import submit

        ctx = self._ctx()
        with self.assertRaises(KitError) as cm:
            submit(ctx, text="safe text", title="Use ghp_ABCDEFGHIJKLMNOP", actor="test-agent")
        self.assertEqual("literal_secret", cm.exception.code)

    # --- Fix 4: prompt intent source is empty, not "cli" ---
    def test_prompt_intent_source_is_empty(self):
        from aisdlc.intent_model import submit

        ctx = self._ctx()
        intent = submit(ctx, text="Source test", actor="test-agent")
        self.assertEqual("prompt", intent["kind"])
        self.assertEqual("", intent["source"]["system"])
        self.assertEqual("", intent["source"]["ref"])

    # --- Fix 4: spec submitted via CLI --file records source_ref ---
    def test_cli_file_spec_records_source_ref(self):
        from aisdlc.intent_model import submit

        ctx = self._ctx()
        spec_path = self.repo.root / "spec.md"
        spec_path.write_text("# Test spec\n\nSome description.\n", encoding="utf-8")
        intent = submit(
            ctx,
            spec=spec_path.read_text(encoding="utf-8"),
            source_ref=str(spec_path),
            actor="test-agent",
        )
        self.assertEqual("spec", intent["kind"])
        self.assertEqual("file", intent["source"]["system"])
        self.assertEqual(str(spec_path), intent["source"]["ref"])


class IntentUniformRepresentationTest(unittest.TestCase):
    """Scenario: same intent produced from CLI, API, and MCP — uniform representation."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)

    def _ctx(self):
        from aisdlc.cli import load_context

        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    def test_uniform_representation(self):
        """The SAME input submitted via the service layer (used by CLI, API, MCP)
        yields stored intents equal except for id/created_at/run_ids."""
        from aisdlc.intent_model import submit

        ctx = self._ctx()
        shared_text = "Uniform test intent"

        # All three entry points call the same submit() — verify the schema is identical
        i1 = submit(ctx, text=shared_text, actor="cli-actor")
        i2 = submit(ctx, text=shared_text, actor="api-actor")
        i3 = submit(ctx, text=shared_text, actor="mcp-actor")

        # Normalize: drop id, created_at, created_by, run_ids, source.ref (file path may differ)
        def normalize(intent):
            n = {k: v for k, v in intent.items()
                 if k not in ("id", "created_at", "created_by", "run_ids")}
            if "source" in n and isinstance(n["source"], dict):
                n = dict(n)
                n["source"] = dict(n["source"])
                n["source"]["ref"] = ""
            return n

        self.assertEqual(normalize(i1), normalize(i2))
        self.assertEqual(normalize(i2), normalize(i3))
        self.assertEqual("prompt", i1["kind"])


if __name__ == "__main__":
    unittest.main()
