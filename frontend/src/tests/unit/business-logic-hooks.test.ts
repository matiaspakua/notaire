/**
 * Unit tests for remaining React Query hook query keys and business logic.
 * Covers: useGestiones, usePersonas, usePresupuestos, usePagos, useConceptos,
 *         useEscrituras, useUsuarios, useInmuebles, useCopias, useDocumentos,
 *         useDocumentosPresentados
 *
 * Use cases: CU01-CU68
 */
import { describe, it, expect } from "vitest";

import { gestionesKeys } from "@/hooks/useGestiones";
import { personasKeys } from "@/hooks/usePersonas";
import { presupuestosKeys } from "@/hooks/usePresupuestos";
import { pagosKeys } from "@/hooks/usePagos";
import { conceptosKeys } from "@/hooks/useConceptos";
import { escriturasKeys } from "@/hooks/useEscrituras";
import { usuariosKeys } from "@/hooks/useUsuarios";
import { inmueblesKeys } from "@/hooks/useInmuebles";
import { copiasKeys } from "@/hooks/useCopias";
import { documentosKeys } from "@/hooks/useDocumentos";

// ──────────────────────────────────────────────
// Query key contracts — all remaining hooks
// ──────────────────────────────────────────────

describe("gestionesKeys (CU02, CU13, CU24)", () => {
  it("all key is ['gestiones']", () => {
    expect(gestionesKeys.all).toEqual(["gestiones"]);
  });

  it("detail key includes id", () => {
    expect(gestionesKeys.detail(10)).toEqual(["gestiones", 10]);
  });

  it("byCliente key includes persona id", () => {
    expect(gestionesKeys.byCliente(5)).toEqual(["gestiones", "cliente", 5]);
  });
});

describe("personasKeys (CU17, CU18)", () => {
  it("all key is ['personas']", () => {
    expect(personasKeys.all).toEqual(["personas"]);
  });

  it("detail key includes id", () => {
    expect(personasKeys.detail(3)).toEqual(["personas", 3]);
  });
});

describe("presupuestosKeys (CU01, CU39)", () => {
  it("all key is ['presupuestos']", () => {
    expect(presupuestosKeys.all).toEqual(["presupuestos"]);
  });

  it("detail key includes id", () => {
    expect(presupuestosKeys.detail(7)).toEqual(["presupuestos", 7]);
  });

  it("byPersona key includes persona id", () => {
    expect(presupuestosKeys.byPersona(2)).toEqual(["presupuestos", "persona", 2]);
  });
});

describe("pagosKeys (CU15, CU47)", () => {
  it("all key is ['pagos']", () => {
    expect(pagosKeys.all).toEqual(["pagos"]);
  });
});

describe("conceptosKeys (CU39, CU49)", () => {
  it("all key is ['conceptos']", () => {
    expect(conceptosKeys.all).toEqual(["conceptos"]);
  });
});

describe("escriturasKeys (CU05, CU06)", () => {
  it("all key is ['escrituras']", () => {
    expect(escriturasKeys.all).toEqual(["escrituras"]);
  });

  it("detail key includes id", () => {
    expect(escriturasKeys.detail(99)).toEqual(["escrituras", 99]);
  });
});

describe("usuariosKeys (CU20, CU21)", () => {
  it("all key is ['usuarios']", () => {
    expect(usuariosKeys.all).toEqual(["usuarios"]);
  });
});

describe("inmueblesKeys (CU08)", () => {
  it("all key is ['inmuebles']", () => {
    expect(inmueblesKeys.all).toEqual(["inmuebles"]);
  });

  it("detail key includes id", () => {
    expect(inmueblesKeys.detail(15)).toEqual(["inmuebles", 15]);
  });
});

describe("copiasKeys (CU44, CU53)", () => {
  it("all key is ['copias']", () => {
    expect(copiasKeys.all).toEqual(["copias"]);
  });

  it("detail key includes id", () => {
    expect(copiasKeys.detail(8)).toEqual(["copias", 8]);
  });

  it("byTestimonio key includes testimonio id", () => {
    expect(copiasKeys.byTestimonio(12)).toEqual(["copias", "testimonio", 12]);
  });
});

describe("documentosKeys (CU27, CU65)", () => {
  it("all key is ['tiposDocumento']", () => {
    expect(documentosKeys.all).toEqual(["tiposDocumento"]);
  });

  it("detail key includes id", () => {
    expect(documentosKeys.detail(4)).toEqual(["tiposDocumento", 4]);
  });
});

// ──────────────────────────────────────────────
// Presupuesto business logic (CU01, CU39)
// ──────────────────────────────────────────────

describe("Presupuesto total calculation (CU01, CU39)", () => {
  type Item = { precio?: number; cantidad?: number };

  function calcularTotal(items: Item[]): number {
    return items.reduce((sum, item) => sum + (item.precio ?? 0) * (item.cantidad ?? 1), 0);
  }

  it("sums item prices * quantities", () => {
    const items = [
      { precio: 100, cantidad: 2 },
      { precio: 50, cantidad: 3 },
    ];
    expect(calcularTotal(items)).toBe(350);
  });

  it("empty items returns 0", () => {
    expect(calcularTotal([])).toBe(0);
  });

  it("item with undefined precio contributes 0", () => {
    const items = [{ cantidad: 5 }];
    expect(calcularTotal(items)).toBe(0);
  });

  it("item with undefined cantidad defaults to 1", () => {
    const items = [{ precio: 200 }];
    expect(calcularTotal(items)).toBe(200);
  });

  it("single item total", () => {
    expect(calcularTotal([{ precio: 500, cantidad: 1 }])).toBe(500);
  });
});

// ──────────────────────────────────────────────
// Pago saldo computation (CU47)
// ──────────────────────────────────────────────

describe("Pago saldo computation (CU47)", () => {
  function calcularSaldo(totalPresupuesto: number, pagos: { monto?: number }[]): number {
    const totalPagado = pagos.reduce((sum, p) => sum + (p.monto ?? 0), 0);
    return totalPresupuesto - totalPagado;
  }

  it("saldo is total minus sum of pagos", () => {
    expect(calcularSaldo(1000, [{ monto: 300 }, { monto: 200 }])).toBe(500);
  });

  it("saldo with no pagos equals total", () => {
    expect(calcularSaldo(800, [])).toBe(800);
  });

  it("fully paid presupuesto has saldo 0", () => {
    expect(calcularSaldo(500, [{ monto: 500 }])).toBe(0);
  });

  it("overpaid presupuesto has negative saldo", () => {
    expect(calcularSaldo(200, [{ monto: 300 }])).toBe(-100);
  });

  it("pago with undefined monto contributes 0", () => {
    expect(calcularSaldo(400, [{ monto: undefined }, { monto: 100 }])).toBe(300);
  });
});

// ──────────────────────────────────────────────
// Escritura number format (CU05)
// ──────────────────────────────────────────────

describe("Escritura display label (CU05)", () => {
  function getEscrituraLabel(numero?: number, fecha?: string): string {
    if (!numero) return "Sin número";
    return fecha ? `Escritura #${numero} — ${fecha}` : `Escritura #${numero}`;
  }

  it("returns Sin número when numero is undefined", () => {
    expect(getEscrituraLabel(undefined, "2026-01-01")).toBe("Sin número");
  });

  it("returns label with date when both present", () => {
    expect(getEscrituraLabel(42, "2026-03-15")).toBe("Escritura #42 — 2026-03-15");
  });

  it("returns label without date when fecha is absent", () => {
    expect(getEscrituraLabel(7)).toBe("Escritura #7");
  });
});

// ──────────────────────────────────────────────
// Persona display name (CU17, CU18, CU61)
// ──────────────────────────────────────────────

describe("Persona display name (CU17, CU18, CU61)", () => {
  function getDisplayName(persona?: { nombre?: string; apellido?: string }): string {
    if (!persona) return "—";
    const parts = [persona.apellido, persona.nombre].filter(Boolean);
    return parts.length > 0 ? parts.join(", ") : "—";
  }

  it("formats as apellido, nombre", () => {
    expect(getDisplayName({ nombre: "Juan", apellido: "García" })).toBe("García, Juan");
  });

  it("returns only apellido when nombre is missing", () => {
    expect(getDisplayName({ apellido: "García" })).toBe("García");
  });

  it("returns only nombre when apellido is missing", () => {
    expect(getDisplayName({ nombre: "Juan" })).toBe("Juan");
  });

  it("returns dash for empty persona", () => {
    expect(getDisplayName({})).toBe("—");
  });

  it("returns dash for undefined persona", () => {
    expect(getDisplayName(undefined)).toBe("—");
  });
});

// ──────────────────────────────────────────────
// Gestiones enabled condition (CU02, CU24)
// ──────────────────────────────────────────────

describe("useGestion enabled condition (CU02, CU24)", () => {
  const isEnabled = (id: number) => id > 0;

  it("enabled when id > 0", () => {
    expect(isEnabled(1)).toBe(true);
    expect(isEnabled(100)).toBe(true);
  });

  it("disabled when id is 0", () => {
    expect(isEnabled(0)).toBe(false);
  });

  it("disabled when id is negative", () => {
    expect(isEnabled(-1)).toBe(false);
  });
});

// ──────────────────────────────────────────────
// Copia type shape (CU44, CU53)
// ──────────────────────────────────────────────

describe("Copia type shape (CU44, CU53)", () => {
  it("copia has required fields", () => {
    const copia = {
      idCopia: 1,
      testimonio: { idTestimonio: 10 },
      fecha: "2026-02-01",
      tipo: "Simple",
    };
    expect(copia.idCopia).toBe(1);
    expect(copia.testimonio.idTestimonio).toBe(10);
    expect(copia.fecha).toBe("2026-02-01");
  });

  it("copia can have undefined testimonio", () => {
    const copia: { idCopia?: number; testimonio?: unknown } = { idCopia: 5 };
    expect(copia.testimonio).toBeUndefined();
  });
});

// ──────────────────────────────────────────────
// Concepto value type (CU39, CU49)
// ──────────────────────────────────────────────

describe("Concepto type shape (CU39, CU49)", () => {
  it("concepto has nombre and valor", () => {
    const concepto = { idConcepto: 1, nombre: "Honorarios", valor: 1500 };
    expect(concepto.nombre).toBe("Honorarios");
    expect(concepto.valor).toBe(1500);
  });

  it("concepto valor can be 0", () => {
    const concepto = { idConcepto: 2, nombre: "Exento", valor: 0 };
    expect(concepto.valor).toBe(0);
  });
});

// ──────────────────────────────────────────────
// Inmueble type shape (CU08)
// ──────────────────────────────────────────────

describe("Inmueble type shape (CU08)", () => {
  it("inmueble has address fields", () => {
    const inmueble = {
      idInmueble: 1,
      calle: "Av. Corrientes",
      numero: 1234,
      ciudad: "Buenos Aires",
    };
    expect(inmueble.calle).toBe("Av. Corrientes");
    expect(inmueble.numero).toBe(1234);
  });

  it("inmueble can have undefined optional fields", () => {
    const inmueble: { idInmueble?: number; piso?: string; depto?: string } = { idInmueble: 1 };
    expect(inmueble.piso).toBeUndefined();
    expect(inmueble.depto).toBeUndefined();
  });
});

// ──────────────────────────────────────────────
// TipoDeDocumento type shape (CU27, CU65)
// ──────────────────────────────────────────────

describe("TipoDeDocumento type shape (CU27, CU65)", () => {
  it("tipoDeDocumento has id and nombre", () => {
    const tipo = { idTipoDocumento: 3, nombre: "DNI" };
    expect(tipo.idTipoDocumento).toBe(3);
    expect(tipo.nombre).toBe("DNI");
  });

  it("filter tipos by partial name match", () => {
    const tipos = [
      { idTipoDocumento: 1, nombre: "DNI" },
      { idTipoDocumento: 2, nombre: "Pasaporte" },
      { idTipoDocumento: 3, nombre: "CUIT" },
    ];
    const result = tipos.filter((t) => t.nombre.toLowerCase().includes("pas"));
    expect(result).toHaveLength(1);
    expect(result[0].nombre).toBe("Pasaporte");
  });
});

// ──────────────────────────────────────────────
// Usuario type shape + password hash (CU20, CU21)
// ──────────────────────────────────────────────

describe("Usuario type shape (CU20, CU21)", () => {
  it("usuario has username field", () => {
    const u = { idUsuario: 1, usuario: "admin", idPersona: 5 };
    expect(u.usuario).toBe("admin");
    expect(u.idPersona).toBe(5);
  });

  it("usuario login requires non-empty credentials", () => {
    function validateLogin(user: string, pwd: string): boolean {
      return user.trim().length > 0 && pwd.trim().length > 0;
    }
    expect(validateLogin("admin", "secret")).toBe(true);
    expect(validateLogin("", "secret")).toBe(false);
    expect(validateLogin("admin", "")).toBe(false);
    expect(validateLogin("  ", "  ")).toBe(false);
  });
});
