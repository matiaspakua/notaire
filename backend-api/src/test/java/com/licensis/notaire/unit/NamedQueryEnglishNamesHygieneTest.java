package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Static inventory for issue #1022: {@code @NamedQuery} name strings and
 * {@code createNamedQuery} call sites must use English entity prefixes (and
 * English method tails on Folio/Item/Person). Also catches the latent
 * {@code Persona.*} vs {@code Person.*} mismatch.
 */
@DisplayName("NamedQuery English names hygiene (issue #1022)")
class NamedQueryEnglishNamesHygieneTest {

    private static final Path BACKEND_ROOT = resolveBackendRoot();

    private static final Pattern NAMED_QUERY_NAME =
            Pattern.compile("@NamedQuery\\s*\\(\\s*name\\s*=\\s*\"([^\"]+)\"");

    private static final Pattern CREATE_NAMED_QUERY =
            Pattern.compile("createNamedQuery\\s*\\(\\s*\"([^\"]+)\"");

    /** Spanish entity prefixes deferred from #973 / forbidden by #1022. */
    private static final String[] FORBIDDEN_PREFIXES = {
            "Escritura.",
            "Usuario.",
            "RegistroAuditoria.",
            "Concepto.",
            "Testimonio.",
            "Suplencia.",
            "Copia.",
            "Pago.",
            "Presupuesto.",
            "Tramite.",
            "TipoDeTramite.",
            "TipoDeDocumento.",
            "DocumentoPresentado.",
            "PlantillaTramite.",
            "PlantillaPresupuesto.",
            "GestionDeEscritura.",
            "Historial.",
            "Inmueble.",
            "Identificacion.",
            "TipoIdentificacion.",
            "MovimientoTestimonio.",
            "TramitesPersonas.",
            "Cuaderno.",
            "FoliosCopias.",
            "TipoDeFolio.",
            "EstadoDeGestion.",
            "Persona."
    };

    /** Spanish method tails that must not remain on already-English prefixes. */
    private static final String[] FORBIDDEN_ENGLISH_PREFIX_TAILS = {
            // Keep Spanish inventory strings (do not Englishize this list).
            "Folio.findByNumero",
            "Folio.findByAnio",
            "Folio.findByAnioAndRegistro",
            "Item.findByValor",
            "Item.findByPorcentaje",
            "Item.findByPresupuesto",
            "Person.findByIdPersona",
            "Person.findByNumeroIdentificacion",
            "Person.findBySexo",
            "Person.findByFechaNacimiento",
            "Person.findByNumeroNupcias",
            "Person.findByRegistroEscribano",
            "Person.findByEsCliente",
            "Person.findByPersonaNombreApellido"
    };

    @Test
    @DisplayName("Should have zero Spanish NamedQuery entity prefixes in business entities")
    void shouldHaveZeroSpanishNamedQueryPrefixesInBusinessEntities() throws IOException {
        Path businessDir = BACKEND_ROOT.resolve("src/main/java/com/licensis/notaire/business");
        assertThat(Files.isDirectory(businessDir)).as("business package directory").isTrue();

        List<String> violations = new ArrayList<>();
        for (Path javaFile : listJavaFiles(businessDir)) {
            String content = Files.readString(javaFile, StandardCharsets.UTF_8);
            Matcher matcher = NAMED_QUERY_NAME.matcher(content);
            while (matcher.find()) {
                String name = matcher.group(1);
                for (String prefix : FORBIDDEN_PREFIXES) {
                    if (name.startsWith(prefix)) {
                        violations.add(relativize(javaFile) + ": @NamedQuery name=\"" + name + "\"");
                    }
                }
            }
        }

        assertThat(violations)
                .as("Spanish @NamedQuery prefixes remaining (issue #1022)")
                .isEmpty();
    }

    @Test
    @DisplayName("Should have zero Spanish or Persona createNamedQuery call sites")
    void shouldHaveZeroSpanishOrPersonaCreateNamedQueryCallSites() throws IOException {
        Path srcRoot = BACKEND_ROOT.resolve("src");
        assertThat(Files.isDirectory(srcRoot)).as("src directory").isTrue();

        List<String> violations = new ArrayList<>();
        for (Path javaFile : listJavaFiles(srcRoot)) {
            String content = Files.readString(javaFile, StandardCharsets.UTF_8);
            Matcher matcher = CREATE_NAMED_QUERY.matcher(content);
            while (matcher.find()) {
                String name = matcher.group(1);
                for (String prefix : FORBIDDEN_PREFIXES) {
                    if (name.startsWith(prefix)) {
                        violations.add(relativize(javaFile) + ": createNamedQuery(\"" + name + "\")");
                    }
                }
            }
        }

        assertThat(violations)
                .as("Spanish / Persona createNamedQuery call sites remaining (issue #1022)")
                .isEmpty();
    }

    @Test
    @DisplayName("Should Englishize Folio/Item/Person NamedQuery method tails")
    void shouldEnglishizeFolioItemPersonNamedQueryMethodTails() throws IOException {
        Path businessDir = BACKEND_ROOT.resolve("src/main/java/com/licensis/notaire/business");
        Path srcRoot = BACKEND_ROOT.resolve("src");

        List<String> names = new ArrayList<>();
        for (Path javaFile : listJavaFiles(businessDir)) {
            collectMatches(Files.readString(javaFile, StandardCharsets.UTF_8), NAMED_QUERY_NAME, names);
        }
        for (Path javaFile : listJavaFiles(srcRoot)) {
            collectMatches(Files.readString(javaFile, StandardCharsets.UTF_8), CREATE_NAMED_QUERY, names);
        }

        List<String> violations = new ArrayList<>();
        for (String name : names) {
            for (String forbidden : FORBIDDEN_ENGLISH_PREFIX_TAILS) {
                if (name.equals(forbidden)) {
                    violations.add(name);
                }
            }
        }

        assertThat(violations)
                .as("Spanish Folio/Item/Person NamedQuery method tails remaining (issue #1022)")
                .isEmpty();
    }

    private static void collectMatches(String content, Pattern pattern, List<String> out) {
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            out.add(matcher.group(1));
        }
    }

    private static List<Path> listJavaFiles(Path root) throws IOException {
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(p -> p.toString().endsWith(".java")).sorted().toList();
        }
    }

    private static String relativize(Path file) {
        return BACKEND_ROOT.relativize(file).toString().replace('\\', '/');
    }

    private static Path resolveBackendRoot() {
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        if (Files.isDirectory(cwd.resolve("src/main/java/com/licensis/notaire/business"))) {
            return cwd;
        }
        Path nested = cwd.resolve("backend-api");
        if (Files.isDirectory(nested.resolve("src/main/java/com/licensis/notaire/business"))) {
            return nested;
        }
        return cwd;
    }
}
