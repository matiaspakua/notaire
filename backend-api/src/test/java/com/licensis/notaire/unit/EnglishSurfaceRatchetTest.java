package com.licensis.notaire.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Locks the English HTTP, dashboard, and schema surface (issue #1490, CU75).
 * Written first: it fails while Spanish resource segments and column names remain.
 */
@DisplayName("English source surface (#1490)")
class EnglishSurfaceRatchetTest {

    private static final Pattern MAPPING = Pattern.compile(
            "@(?:Request|Get|Post|Put|Delete|Patch)Mapping\\s*\\(\\s*(?:value\\s*=\\s*)?\"([^\"]+)\"");

    private static final Pattern SCHEMA_NAME = Pattern.compile(
            "@(?:Table|Column|JoinColumn|JoinTable)\\s*\\((?:[^)]*?\\bname\\s*=\\s*)\"([^\"]+)\"");

    private static final List<String> SPANISH_SEGMENTS = List.of(
            "usuarios", "usuario", "gestiones", "gestion", "tramites", "tramite",
            "presupuestos", "presupuesto", "escrituras", "escritura", "testimonio",
            "testimonios", "conceptos", "concepto", "pagos", "pago", "historial",
            "suplencia", "suplencias", "inmueble", "inmuebles", "carpetas", "carpeta",
            "cuadernos", "cuaderno", "reportes", "reporte", "copia", "personas",
            "persona", "administracion", "auditoria", "plantilla", "documento",
            "documentos", "minutas", "minuta", "protocolo", "estado-gestion",
            "tipo-tramite", "tipo-folio", "tipo-de-documento", "tipo-identificacion",
            "identificaciones", "documento-presentado", "plantilla-tramite",
            "plantilla-presupuestos", "plantilla-costos-documento",
            "movimiento-testimonio", "minutas-inscripcion", "protocolo-auxiliar",
            "caratula");

    private static final List<String> SPANISH_SCHEMA = List.of(
            "identificaciones", "nombre", "descripcion", "activo", "tipo",
            "posicion_x", "posicion_y", "condicion", "fecha_nacimiento", "numero",
            "fk_id_persona", "fk_id_tipo_identificacion", "fk_id_gestion",
            "fk_id_tramite", "fk_id_escritura", "fk_id_presupuesto",
            "fk_id_testimonio", "fk_id_usuario", "fk_estado_gestion_id",
            "fk_nodo_origen_id", "fk_nodo_destino_id");

    @Test
    @DisplayName("Should map every web controller path with English segments")
    void shouldMapControllersInEnglish() throws IOException {
        Path web = repoRoot().resolve(
                "backend-api/src/main/java/com/licensis/notaire/adapter/in/web");
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(web)) {
            files.filter(path -> path.toString().endsWith("Controller.java")).forEach(path -> {
                String text = read(path);
                Matcher matcher = MAPPING.matcher(text);
                while (matcher.find()) {
                    String mapping = matcher.group(1);
                    for (String segment : SPANISH_SEGMENTS) {
                        if (containsSegment(mapping, segment)) {
                            offenders.add(path.getFileName() + " " + mapping + " [" + segment + "]");
                        }
                    }
                }
            });
        }
        assertThat(offenders).isEmpty();
    }

    @Test
    @DisplayName("Should keep dashboard navigation hrefs in English")
    void shouldKeepDashboardHrefsInEnglish() {
        Path nav = repoRoot().resolve("frontend/src/lib/dashboard-nav.ts");
        String text = read(nav);
        List<String> offenders = new ArrayList<>();
        for (String segment : List.of(
                "personas", "gestiones", "presupuestos", "escrituras", "administracion",
                "testimonios", "pagos", "inmuebles", "suplencias", "reportes")) {
            if (containsSegment(text, segment)) {
                offenders.add(segment);
            }
        }
        assertThat(offenders).isEmpty();
    }

    @Test
    @DisplayName("Should map remaining tables and columns with English names")
    void shouldMapSchemaInEnglish() throws IOException {
        Path business = repoRoot().resolve(
                "backend-api/src/main/java/com/licensis/notaire/business");
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(business)) {
            files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                String text = read(path);
                Matcher matcher = SCHEMA_NAME.matcher(text);
                while (matcher.find()) {
                    String name = matcher.group(1);
                    if (SPANISH_SCHEMA.contains(name)) {
                        offenders.add(path.getFileName() + " " + name);
                    }
                }
            });
        }
        assertThat(offenders).isEmpty();
    }

    @Test
    @DisplayName("Should not ignore retired Spanish association names")
    void shouldNotIgnoreRetiredSpanishAssociations() throws IOException {
        Path business = repoRoot().resolve(
                "backend-api/src/main/java/com/licensis/notaire/business");
        List<String> retired = List.of(
                "fkIdEscritura",
                "fkIdTestimonio",
                "fkIdTipoIdentificacion",
                "foliosCopiasCollection",
                "movimientoTestimonioList",
                "copiaList",
                "\"copia\"");
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(business)) {
            files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                String text = read(path);
                for (String name : retired) {
                    if (text.contains(name)) {
                        offenders.add(path.getFileName() + " " + name);
                    }
                }
            });
        }
        assertThat(offenders).isEmpty();
        String folio = read(business.resolve("Folio.java"));
        assertThat(folio).contains("\"folioList\"");
        assertThat(folio).contains("\"tramiteList\"");
        assertThat(folio).contains("\"testimonioList\"");
    }

    private static boolean containsSegment(String text, String segment) {
        return Pattern.compile("(^|/)(?<![A-Za-z0-9_-])" + Pattern.quote(segment) + "(?![A-Za-z0-9_-])")
                .matcher(text)
                .find();
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read " + path, exception);
        }
    }

    private static Path repoRoot() {
        Path cursor = Path.of("").toAbsolutePath();
        while (cursor != null && !Files.isDirectory(cursor.resolve("backend-api"))) {
            cursor = cursor.getParent();
        }
        if (cursor == null) {
            throw new IllegalStateException("Repository root not found from " + Path.of("").toAbsolutePath());
        }
        return cursor;
    }
}
