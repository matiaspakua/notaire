/**
 * Issue #1502: the English identifier rename (#1495) also rewrote Spanish UI copy in
 * es.json ("Person actualizada", "Nuevo Notebook"). Code is English; es.json values stay
 * Spanish (CONSTITUTION: translate identifiers, not i18n values). This guard fails when an
 * es.json value contains an English entity name from the rename map.
 *
 * "Roles" is left out on purpose: it is also the Spanish plural ("Roles y Permisos").
 */
import { describe, expect, it } from "vitest";
import esMessages from "../../../messages/es.json";

const ENGLISH_ENTITY_NAMES =
  /\b(Person|People|Property|Properties|Substitutions?|Managements?|Budgets?|Deeds?|Notebooks?|Role|Concepts?|Testimon(?:y|ies)|Procedures?|Folders?|Payments?|Notar(?:y|ies)|Users?|Templates?|Documents?|Cop(?:y|ies)|Movements?|Reports?)\b/;

function flatten(node: unknown, prefix = ""): Array<[string, string]> {
  if (typeof node === "string") return [[prefix, node]];
  if (node && typeof node === "object") {
    return Object.entries(node as Record<string, unknown>).flatMap(([key, value]) =>
      flatten(value, prefix ? `${prefix}.${key}` : key),
    );
  }
  return [];
}

describe("es.json stays Spanish (#1502)", () => {
  it("has no English entity names from the #1495 rename in its values", () => {
    const leaks = flatten(esMessages)
      .filter(([, value]) => ENGLISH_ENTITY_NAMES.test(value))
      .map(([key, value]) => `${key} = ${value}`);
    expect(leaks).toEqual([]);
  });

  it("restores the reported examples", () => {
    expect(esMessages.personas.updated).toBe("Persona actualizada");
    expect(esMessages.inmuebles.created).toBe("Inmueble creado");
    expect(esMessages.suplencias.created).toBe("Suplencia registrada");
    expect(esMessages.gestiones.fields.persona).toBe("Persona");
    expect(esMessages.gestiones.fields.inmueble).toBe("Inmueble");
  });
});
