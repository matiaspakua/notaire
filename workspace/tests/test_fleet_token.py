#!/usr/bin/env python3
"""Guards #1648 (CU76/CU78): fleet agents get a GitHub App token, or a loud fallback."""

from __future__ import annotations

import base64
import json
import os
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
SCRIPT = REPO_ROOT / "workspace" / "sdlc" / "fleet-token.sh"


def b64url_decode(part: str) -> bytes:
    return base64.urlsafe_b64decode(part + "=" * (-len(part) % 4))


@unittest.skipUnless(shutil.which("openssl"), "openssl required")
class FleetTokenTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.mkdtemp(prefix="fleet-")
        self.addCleanup(shutil.rmtree, self.tmp, ignore_errors=True)
        self.key = os.path.join(self.tmp, "app.pem")
        subprocess.run(["openssl", "genrsa", "-out", self.key, "2048"], check=True, capture_output=True)

    def run_script(self, *args: str, **env: str) -> subprocess.CompletedProcess[str]:
        clean = {k: v for k, v in os.environ.items() if not k.startswith(("FLEET_APP_", "GH_TOKEN"))}
        return subprocess.run(["bash", str(SCRIPT), *args], capture_output=True, text=True, env={**clean, **env})

    def test_jwt_is_rs256_signed_by_the_app_key(self):
        result = self.run_script("--jwt-only", FLEET_APP_ID="12345", FLEET_APP_PRIVATE_KEY=Path(self.key).read_text())
        self.assertEqual(result.returncode, 0, result.stderr)
        header, payload, signature = result.stdout.strip().split(".")
        self.assertEqual(json.loads(b64url_decode(header))["alg"], "RS256")
        claims = json.loads(b64url_decode(payload))
        self.assertEqual(claims["iss"], "12345")
        self.assertLessEqual(claims["exp"] - claims["iat"], 600)
        pub = os.path.join(self.tmp, "pub.pem")
        subprocess.run(["openssl", "rsa", "-in", self.key, "-pubout", "-out", pub], check=True, capture_output=True)
        sig = os.path.join(self.tmp, "sig")
        Path(sig).write_bytes(b64url_decode(signature))
        verify = subprocess.run(
            ["openssl", "dgst", "-sha256", "-verify", pub, "-signature", sig],
            input=f"{header}.{payload}".encode(), capture_output=True,
        )
        self.assertEqual(verify.returncode, 0, verify.stdout + verify.stderr)

    def test_without_app_credentials_it_falls_back_loudly(self):
        result = self.run_script()
        self.assertEqual(result.returncode, 0)
        self.assertEqual(result.stdout.strip(), "")
        self.assertIn("fleet App not configured", result.stderr)


if __name__ == "__main__":
    unittest.main()
