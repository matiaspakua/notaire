import json
import os
import tempfile
import unittest

import helpers  # noqa: F401
import metrics


class MetricsTest(unittest.TestCase):
    def test_appends_one_json_line_per_gate_result(self):
        path = os.path.join(tempfile.mkdtemp(), "metrics.jsonl")
        metrics.append(path, "1063", "red", "FAIL rc=1 :: bash -c mvn test")
        metrics.append(path, "1063", "ci", "PASS :: PR #9")
        rows = [json.loads(line) for line in open(path)]
        self.assertEqual(len(rows), 2)
        self.assertEqual({k: rows[0][k] for k in ("issue", "gate", "result")},
                         {"issue": "1063", "gate": "red", "result": "FAIL"})
        self.assertEqual(rows[0]["detail"], "FAIL rc=1 :: bash -c mvn test")
        self.assertEqual(rows[1]["result"], "PASS")
        self.assertIn("ts", rows[1])


if __name__ == "__main__":
    unittest.main()
