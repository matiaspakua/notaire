"""Service layer returns data and raises typed KitError; it never prints (#2252, A.1)."""
from __future__ import annotations

import contextlib
import io
import sys
import unittest

from helpers import KIT, Repo

sys.path.insert(0, str(KIT))

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"
"""

from aisdlc import catalogs, workflows  # noqa: E402
from aisdlc.context import Context  # noqa: E402
from aisdlc.errors import CODES, KitError  # noqa: E402
from aisdlc.profile import ProfileError  # noqa: E402


def context_for(repo: Repo) -> Context:
    from aisdlc.cli import load_context

    return load_context(repo.root, None)


class ServiceLayerTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.ctx = context_for(self.repo)

    def call(self, fn, *args, **kwargs):
        out = io.StringIO()
        with contextlib.redirect_stdout(out):
            result = fn(*args, **kwargs)
        self.assertEqual("", out.getvalue(), "service functions must not print")
        return result

    def code_of(self, fn, *args, **kwargs) -> KitError:
        with self.assertRaises(KitError) as raised:
            self.call(fn, *args, **kwargs)
        self.assertIn(raised.exception.code, CODES)
        return raised.exception

    def test_kit_error_is_a_profile_error_with_code_and_detail(self):
        err = KitError("not_found", "gone")
        self.assertIsInstance(err, ProfileError)
        self.assertEqual(("not_found", "gone", "gone"), (err.code, err.detail, str(err)))

    def test_workflow_service_returns_dicts(self):
        created = self.call(workflows.create, self.ctx, "w1", summary="s")
        self.assertEqual(("w1", "draft", 1), (created["id"], created["governance"], created["version"]))
        self.assertEqual(["w1"], [e["id"] for e in self.call(workflows.list_workflows, self.ctx)["entries"]])
        self.assertEqual("w1", self.call(workflows.show, self.ctx, "w1")["id"])
        moved = self.call(workflows.transition, self.ctx, "w1", "define")
        self.assertEqual("defined", moved["governance"])

    def test_catalog_service_returns_dicts(self):
        added = self.call(catalogs.add, self.ctx, "agents", "a1", ref=None, summary="x")
        self.assertEqual(("a1", 1), (added["id"], added["version"]))
        updated = self.call(catalogs.update, self.ctx, "agents", "a1", ref=None, summary="y")
        self.assertEqual(2, updated["version"])
        self.assertEqual("a1", self.call(catalogs.show, self.ctx, "agents", "a1")["id"])
        self.assertEqual([1, 2], [v["version"] for v in self.call(catalogs.history, self.ctx, "agents", "a1")["versions"]])

    def test_catalog_errors_are_coded(self):
        self.call(catalogs.add, self.ctx, "agents", "a1", ref=None, summary="")
        self.assertEqual("already_exists", self.code_of(catalogs.add, self.ctx, "agents", "a1", ref=None, summary="").code)
        self.assertEqual("not_found", self.code_of(catalogs.show, self.ctx, "agents", "zz").code)
        self.assertEqual("not_found", self.code_of(catalogs.history, self.ctx, "agents", "zz").code)
        self.assertEqual("not_found", self.code_of(catalogs.update, self.ctx, "agents", "zz", ref=None, summary="").code)
        self.assertEqual("invalid_id", self.code_of(catalogs.show, self.ctx, "agents", "a/b").code)
        self.assertEqual("unknown_kind", self.code_of(catalogs.show, self.ctx, "nope", "a1").code)

    def test_workflow_errors_are_coded(self):
        self.call(workflows.create, self.ctx, "w1", agents=["ghost"])
        self.assertEqual("illegal_transition", self.code_of(workflows.transition, self.ctx, "w1", "approve").code)
        self.assertEqual("unknown_transition", self.code_of(workflows.transition, self.ctx, "w1", "fly").code)
        self.assertEqual("refs_unresolved", self.code_of(workflows.transition, self.ctx, "w1", "define").code)
        self.assertEqual("not_found", self.code_of(workflows.show, self.ctx, "zz").code)
        self.assertEqual("already_exists", self.code_of(workflows.create, self.ctx, "w1").code)

    def test_approve_errors_are_coded(self):
        self.call(workflows.create, self.ctx, "w2")
        self.call(workflows.transition, self.ctx, "w2", "define")
        self.call(workflows.transition, self.ctx, "w2", "request_validation", waive_reason="r")
        self.assertEqual("approve_fail_closed", self.code_of(workflows.transition, self.ctx, "w2", "approve").code)
        self.assertEqual(
            "reason_required",
            self.code_of(workflows.transition, self.ctx, "w2", "approve", owner_override=True).code,
        )
        approved = self.call(workflows.transition, self.ctx, "w2", "approve", owner_override=True, reason="ok")
        self.assertEqual("approved", approved["governance"])

    def test_waive_reason_is_coded(self):
        self.call(workflows.create, self.ctx, "w3")
        self.call(workflows.transition, self.ctx, "w3", "define")
        self.assertEqual(
            "waive_reason_required",
            self.code_of(workflows.transition, self.ctx, "w3", "request_validation").code,
        )

    def test_missing_catalogs_config_is_coded(self):
        bare = Repo(self, '[project]\nname = "demo"\npolicy = "POLICY.md"\n')
        self.assertEqual("no_catalogs", self.code_of(catalogs.show, context_for(bare), "agents", "a").code)


if __name__ == "__main__":
    unittest.main()
