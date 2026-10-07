"""TDD tests for CU-API-MATRIX.csv drift validator (#1064, CU76)."""

from __future__ import annotations

import csv
import re
import shutil
import subprocess
import tempfile
import textwrap
import unittest
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
SCRIPTS = REPO / "workspace" / "sdlc"
VALIDATOR = SCRIPTS / "validate-cu-api-matrix.py"
PREFLIGHT = SCRIPTS / "preflight.sh"
MATRIX = REPO / "docs" / "300-development" / "303-testing" / "CU-API-MATRIX.csv"

REQUIRED_BASES = (
    "/carpetas",
    "/cuadernos",
    "/minutas-inscripcion",
    "/plantilla-costos-documento",
    "/protocolo-auxiliar",
    "/roles",
    "/tipo-identificacion",
    "/tramites",
)

HEADER = [
    "CU_ID",
    "CU_Nombre",
    "Modulo",
    "Grado",
    "Entidad",
    "Operacion",
    "Controller",
    "HTTP_Method",
    "Endpoint",
    "Bruno_Test",
    "Endpoint_Status",
    "Bruno_Status",
    "Notas",
    "GitHub_Issue",
]


def _controller_java(name: str, mapping: str) -> str:
    return textwrap.dedent(
        f"""\
        package com.licensis.notaire.adapter.in.web.demo;

        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RestController;

        @RestController
        @RequestMapping("{mapping}")
        public class {name} {{
        }}
        """
    )


def _write_matrix(path: Path, rows: list[dict[str, str]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", newline="", encoding="utf-8") as fh:
        writer = csv.DictWriter(fh, fieldnames=HEADER)
        writer.writeheader()
        for row in rows:
            writer.writerow({key: row.get(key, "") for key in HEADER})


def _row(**kwargs: str) -> dict[str, str]:
    base = {key: "" for key in HEADER}
    base.update(kwargs)
    return base


class ValidateCuApiMatrixTest(unittest.TestCase):
    def setUp(self) -> None:
        self.root = Path(tempfile.mkdtemp(prefix="cu-api-matrix-"))
        self.addCleanup(shutil.rmtree, self.root, ignore_errors=True)
        web = (
            self.root
            / "backend-api"
            / "src"
            / "main"
            / "java"
            / "com"
            / "licensis"
            / "notaire"
            / "adapter"
            / "in"
            / "web"
        )
        web.mkdir(parents=True)
        (web / "BudgetController.java").write_text(
            _controller_java("BudgetController", "/api/v1/presupuestos"), encoding="utf-8"
        )
        (web / "NotebookController.java").write_text(
            _controller_java("NotebookController", "/api/v1/cuadernos"), encoding="utf-8"
        )
        (web / "ProcedureFolderController.java").write_text(
            _controller_java("ProcedureFolderController", "/api/v1/carpetas"), encoding="utf-8"
        )
        (web / "RegistrationDraftController.java").write_text(
            _controller_java("RegistrationDraftController", "/api/v1/minutas-inscripcion"),
            encoding="utf-8",
        )
        (web / "DocumentCostTemplateController.java").write_text(
            _controller_java("DocumentCostTemplateController", "/api/v1/plantilla-costos-documento"),
            encoding="utf-8",
        )
        (web / "AuxiliaryProtocolController.java").write_text(
            _controller_java("AuxiliaryProtocolController", "/api/v1/protocolo-auxiliar"),
            encoding="utf-8",
        )
        (web / "RoleController.java").write_text(
            _controller_java("RoleController", "/api/v1/roles"), encoding="utf-8"
        )
        (web / "IdentificationTypeController.java").write_text(
            _controller_java("IdentificationTypeController", "/api/v1/tipo-identificacion"),
            encoding="utf-8",
        )
        (web / "ProcedureController.java").write_text(
            _controller_java("ProcedureController", "/api/v1/tramites"), encoding="utf-8"
        )
        self.matrix = self.root / "docs" / "300-development" / "303-testing" / "CU-API-MATRIX.csv"

    def _run(self, matrix: Path | None = None) -> subprocess.CompletedProcess[str]:
        self.assertTrue(VALIDATOR.is_file(), "validator script must exist")
        args = ["python3", str(VALIDATOR), "--root", str(self.root)]
        if matrix is not None:
            args.extend(["--matrix", str(matrix)])
        return subprocess.run(args, capture_output=True, text=True)

    def _good_rows(self) -> list[dict[str, str]]:
        rows = [
            _row(
                CU_ID="CU01",
                Controller="BudgetController",
                HTTP_Method="POST",
                Endpoint="/api/v1/presupuestos",
                Bruno_Test="budgets/01-create.yml",
                Endpoint_Status="OK",
                Bruno_Status="OK",
                GitHub_Issue="#154",
            ),
            _row(
                CU_ID="CU80",
                Controller="NotebookController",
                HTTP_Method="GET",
                Endpoint="/api/v1/cuadernos",
                Bruno_Test="notebooks/",
                Endpoint_Status="OK",
                Bruno_Status="OK",
                GitHub_Issue="#311",
            ),
            _row(
                CU_ID="CU85",
                Controller="ProcedureFolderController",
                HTTP_Method="GET",
                Endpoint="/api/v1/carpetas",
                Bruno_Test="procedure-folders/",
                Endpoint_Status="OK",
                Bruno_Status="OK",
                GitHub_Issue="#953",
            ),
            _row(
                CU_ID="CU82",
                Controller="RegistrationDraftController",
                HTTP_Method="POST",
                Endpoint="/api/v1/minutas-inscripcion",
                Bruno_Test="registration-drafts/",
                Endpoint_Status="OK",
                Bruno_Status="OK",
                GitHub_Issue="#313",
            ),
            _row(
                CU_ID="CU39",
                Controller="DocumentCostTemplateController",
                HTTP_Method="POST",
                Endpoint="/api/v1/plantilla-costos-documento",
                Bruno_Test="document-cost-templates/",
                Endpoint_Status="OK",
                Bruno_Status="OK",
                GitHub_Issue="#192",
            ),
            _row(
                CU_ID="CU81",
                Controller="AuxiliaryProtocolController",
                HTTP_Method="GET",
                Endpoint="/api/v1/protocolo-auxiliar/folios-disponibles",
                Bruno_Test="auxiliary-protocol/",
                Endpoint_Status="OK",
                Bruno_Status="OK",
                GitHub_Issue="#312",
            ),
            _row(
                CU_ID="CU76",
                Controller="RoleController",
                HTTP_Method="GET",
                Endpoint="/api/v1/roles",
                Bruno_Test="roles/",
                Endpoint_Status="OK",
                Bruno_Status="OK",
                GitHub_Issue="#1064",
            ),
            _row(
                CU_ID="CU17",
                Controller="IdentificationTypeController",
                HTTP_Method="GET",
                Endpoint="/api/v1/tipo-identificacion",
                Bruno_Test="identification-types/",
                Endpoint_Status="OK",
                Bruno_Status="OK",
                GitHub_Issue="#170",
            ),
            _row(
                CU_ID="CU02",
                Controller="ProcedureController",
                HTTP_Method="GET",
                Endpoint="/api/v1/tramites",
                Bruno_Test="procedures/",
                Endpoint_Status="OK",
                Bruno_Status="OK",
                GitHub_Issue="#155",
            ),
            _row(
                CU_ID="CU05",
                Controller="BudgetController",
                HTTP_Method="POST",
                Endpoint="/api/v1/presupuestos",
                Bruno_Test="MISSING",
                Endpoint_Status="OK",
                Bruno_Status="NO-TEST",
                Notas="Bruno gap tracked in #953",
                GitHub_Issue="#158",
            ),
            _row(
                CU_ID="CU74",
                Controller="N/A",
                HTTP_Method="N/A",
                Endpoint="N/A",
                Bruno_Test="N/A",
                Endpoint_Status="N/A",
                Bruno_Status="N/A",
                GitHub_Issue="#298",
            ),
        ]
        return rows

    def test_should_reject_stale_spanish_controller_name(self) -> None:
        rows = self._good_rows()
        rows[0]["Controller"] = "PresupuestoController"
        _write_matrix(self.matrix, rows)
        result = self._run()
        self.assertNotEqual(result.returncode, 0, result.stdout + result.stderr)
        self.assertIn("PresupuestoController", result.stdout + result.stderr)

    def test_should_accept_current_english_controller_names(self) -> None:
        _write_matrix(self.matrix, self._good_rows())
        result = self._run()
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_should_reject_missing_required_resource_base(self) -> None:
        rows = [r for r in self._good_rows() if "/carpetas" not in r["Endpoint"]]
        _write_matrix(self.matrix, rows)
        result = self._run()
        self.assertNotEqual(result.returncode, 0, result.stdout + result.stderr)
        self.assertIn("/carpetas", result.stdout + result.stderr)

    def test_should_pass_when_all_required_resource_bases_present(self) -> None:
        _write_matrix(self.matrix, self._good_rows())
        result = self._run()
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        combined = result.stdout + result.stderr
        for base in REQUIRED_BASES:
            self.assertNotIn(f"missing required resource base: {base}", combined)

    def test_should_reject_status_word_in_bruno_test(self) -> None:
        rows = self._good_rows()
        rows[0]["Bruno_Test"] = "DONE"
        _write_matrix(self.matrix, rows)
        result = self._run()
        self.assertNotEqual(result.returncode, 0, result.stdout + result.stderr)
        self.assertRegex(result.stdout + result.stderr, r"Bruno_Test|DONE")

    def test_should_accept_path_and_sentinel_bruno_test_values(self) -> None:
        _write_matrix(self.matrix, self._good_rows())
        result = self._run()
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_should_reject_missing_bruno_without_issue_953(self) -> None:
        rows = self._good_rows()
        rows[9]["Notas"] = "no bruno yet"
        rows[9]["GitHub_Issue"] = "#158"
        _write_matrix(self.matrix, rows)
        result = self._run()
        self.assertNotEqual(result.returncode, 0, result.stdout + result.stderr)
        self.assertIn("#953", result.stdout + result.stderr)

    def test_should_list_matrix_validator_in_preflight(self) -> None:
        result = subprocess.run(
            ["bash", str(PREFLIGHT), "--list"],
            cwd=str(REPO),
            capture_output=True,
            text=True,
        )
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        combined = result.stdout + result.stderr
        self.assertRegex(combined, r"cu-api matrix|CU-API matrix|validate-cu-api-matrix", re.I)

    def test_should_fail_on_current_repo_matrix_until_refreshed(self) -> None:
        """Red proof: stale Spanish names on mainline CSV must fail before refresh.

        After the CSV refresh in this change, this assertion flips to expect success.
        The method name is kept for TDD history; behavior tracks repo readiness.
        """
        self.assertTrue(MATRIX.is_file())
        result = subprocess.run(
            ["python3", str(VALIDATOR), "--root", str(REPO)],
            capture_output=True,
            text=True,
        )
        text = MATRIX.read_text(encoding="utf-8")
        stale = "PersonaController" in text or "PresupuestoController" in text
        if stale:
            self.assertNotEqual(
                result.returncode,
                0,
                "stale Spanish controllers still present; validator must fail",
            )
        else:
            self.assertEqual(result.returncode, 0, result.stdout + result.stderr)


if __name__ == "__main__":
    unittest.main()
