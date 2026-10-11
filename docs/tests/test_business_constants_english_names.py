#!/usr/bin/env python3
"""BusinessConstants field names are English. Stored label values stay."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CONSTANTS = (
    ROOT
    / "backend-api"
    / "src"
    / "main"
    / "java"
    / "com"
    / "licensis"
    / "notaire"
    / "business"
    / "BusinessConstants.java"
)
ENGLISH = {
    "ManagementStatusInitial": '"Iniciada"',
    "ManagementStatusModified": '"Gestion Modificada"',
    "ManagementInProcedure": '"En Tramite"',
    "DocumentationComplete": '"Documentacion Completa"',
    "ManagementWithSignedDeed": '"Gestion con Escritura Firmada"',
    "ManagementWithUnsignedDeed": '"Gestion con Escritura Sin Firmar"',
    "ManagementWithVoidedDeed": '"Gestion con Escritura Anulada"',
    "ManagementWithDeedNotPassed": '"Gestion con Escritura No Paso"',
    "ManagementWithRegisteredDeed": '"Gestion con Escritura Inscripta"',
    "ManagementWithCompleteDocumentation": '"Documentacion Completa"',
    "ManagementArchived": '"Archivada"',
    "FolioStatusNew": '"Folio Nuevo"',
    "FolioStatusUsed": '"Folio Utilizado"',
    "FolioStatusDamaged": '"Folio Errose"',
    "ModuleUpdatePerson": '"Modificar Persona"',
    "ModuleUpdateClient": '"Modificar Cliente"',
    "DeedSigned": '"Firmada"',
    "DeedUnsigned": '"Sin Firmar"',
    "DeedVoided": '"Anulada"',
    "DeedNotPassed": '"No Paso"',
    "DeedRegistered": '"Inscripta"',
    "UserEmployee": '"Empleado"',
    "UserNotary": '"Escribano"',
    "DocumentationExternalEntity": '"Entidad Externa"',
    "DocumentationClient": '"Cliente"',
}
RETIRED = (
    "ManagementStatusINICIAL",
    "ManagementStatusMODIFICADA",
    "ManagementENProcedure",
    "DOCUMENTACION_COMPLETA",
    "ManagementCONDeedFIRMADA",
    "ManagementCONDeedSINFIRMAR",
    "ManagementCONDeedANULADA",
    "ManagementCONDeedNOPASO",
    "ManagementCONDOCUMENTACIONCOMPLETA",
    "ManagementARCHIVADA",
    "FolioTypeDESHABILITADO",
    "StatusFolioNUEVOS",
    "StatusFolioUTILIZADO",
    "StatusFolioERROSE",
    "ModuleMODIFICARPerson",
    "ModuleMODIFICARClient",
    "DeedFIRMADA",
    "DeedSINFIRMAR",
    "DeedANULADA",
    "DeedNOPASO",
    "UserEMPLEADO",
    "DOCUMENTACION_ENTIDAD_EXTERNA",
    "DOCUMENTACIONClient",
)


class BusinessConstantsEnglishNamesTest(unittest.TestCase):
    def test_field_names_are_english_and_values_stay(self):
        text = CONSTANTS.read_text(encoding="utf-8")
        for name, value in ENGLISH.items():
            self.assertIn(f"{name} = {value}", text, name)
        for retired in RETIRED:
            self.assertNotIn(retired, text, retired)
        self.assertIn("FolioTypeDisabled = false", text)

        live_roots = (
            ROOT / "backend-api" / "src",
            ROOT / "frontend" / "src",
        )
        for folder in live_roots:
            for path in folder.rglob("*"):
                if path.suffix not in {".java", ".ts", ".tsx"}:
                    continue
                body = path.read_text(encoding="utf-8")
                for retired in RETIRED:
                    self.assertNotIn(retired, body, f"{path} still names {retired}")


if __name__ == "__main__":
    unittest.main()
