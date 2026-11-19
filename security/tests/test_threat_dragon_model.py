#!/usr/bin/env python3
"""The Notaire Threat Dragon model matches the STRIDE register (CU78, #2009).

The schema is the upstream v2 file from OWASP/threat-dragon commit
30dc0ff6541b8bac075b107263bbe6e6764bcabc
(td.vue/src/assets/schema/threat-dragon-v2.schema.json). The official
sample td.vue/src/service/demo/v2-threat-model.json validates with this
checker (0 errors). The checker covers the keywords that schema uses:
type, required, properties, items, minimum, minLength, and maxLength.

Run with: python3 -m unittest security.tests.test_threat_dragon_model
"""

from __future__ import annotations

import json
import unittest
from pathlib import Path

import yaml

REPO = Path(__file__).resolve().parents[2]
MODEL = REPO / "docs" / "200-architecture" / "206-security" / "threat-model" / "notaire-system.threat-dragon.json"
SCHEMA = REPO / "docs" / "200-architecture" / "206-security" / "threat-model" / "threat-dragon-v2.schema.json"
REGISTER = REPO / "docs" / "200-architecture" / "206-security" / "threat-model" / "system.stride.yml"
SECRET_MARKERS = ("ghp_", "BEGIN PRIVATE KEY", "AKIA", "sk_live_")
EXPLOIT_MARKERS = ("sqlmap",)


def validate(node, schema, path="$"):
    errors = []
    if not isinstance(schema, dict):
        return errors
    if schema.get("nullable") and node is None:
        return errors
    expected = schema.get("type")
    if expected:
        names = expected if isinstance(expected, list) else [expected]
        py = {
            "object": dict,
            "array": list,
            "string": str,
            "integer": int,
            "number": (int, float),
            "boolean": bool,
        }
        matched = False
        for name in names:
            kind = py.get(name)
            if kind is None:
                errors.append(f"{path} unknown schema type {name}")
                continue
            if name in ("integer", "number") and isinstance(node, bool):
                continue
            if isinstance(node, kind):
                matched = True
        if not matched:
            errors.append(f"{path} expected {names} got {type(node).__name__}")
            return errors
    if isinstance(node, str):
        if "minLength" in schema and len(node) < schema["minLength"]:
            errors.append(f"{path} shorter than minLength")
        if "maxLength" in schema and len(node) > schema["maxLength"]:
            errors.append(f"{path} longer than maxLength")
    if isinstance(node, (int, float)) and not isinstance(node, bool) and "minimum" in schema:
        if node < schema["minimum"]:
            errors.append(f"{path} below minimum")
    if isinstance(node, dict):
        for key in schema.get("required") or []:
            if key not in node:
                errors.append(f"{path} missing {key}")
        props = schema.get("properties") or {}
        for key, value in node.items():
            if key in props:
                errors.extend(validate(value, props[key], f"{path}.{key}"))
    if isinstance(node, list) and isinstance(schema.get("items"), dict):
        for index, item in enumerate(node):
            errors.extend(validate(item, schema["items"], f"{path}[{index}]"))
    return errors


def threat_ids(model):
    found = []
    for diagram in model["detail"]["diagrams"]:
        for cell in diagram.get("cells") or []:
            for threat in (cell.get("data") or {}).get("threats") or []:
                found.append(threat.get("id"))
    return found


class ThreatDragonModelTest(unittest.TestCase):
    def test_model_matches_the_upstream_v2_schema_and_the_register(self):
        self.assertTrue(SCHEMA.is_file(), "vendored Threat Dragon v2 schema is missing")
        self.assertTrue(MODEL.is_file(), "notaire-system.threat-dragon.json is missing")
        schema = json.loads(SCHEMA.read_text(encoding="utf-8"))
        raw = MODEL.read_text(encoding="utf-8")
        model = json.loads(raw)
        errors = validate(model, schema)
        self.assertEqual([], errors)
        register = yaml.safe_load(REGISTER.read_text(encoding="utf-8"))
        expected = {row["id"] for row in register["requirements"]}
        actual = set(threat_ids(model))
        self.assertEqual(expected, actual)
        for marker in SECRET_MARKERS + EXPLOIT_MARKERS:
            self.assertNotIn(marker, raw)


if __name__ == "__main__":
    unittest.main()
