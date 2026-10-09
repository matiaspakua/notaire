package com.licensis.notaire.adapter.in.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Issue #1315 (Owner decision, Run 7): a DELETE handler returns the success status its OpenAPI
 * annotations document. The contract documents {@code 204 No Content} for deletes; 17 handlers
 * returned {@code 200 OK}, and {@code DELETE /roles/usuarios/{idUser}} returned 204 while the
 * contract (springdoc default, no annotation) said 200.
 *
 * <p>For every {@code @DeleteMapping} method the 2xx codes in its {@code @ApiResponse}
 * annotations (springdoc's default {@code 200} when there are none) must equal the 2xx codes
 * the method body returns.
 */
class DeleteStatusMatchesContractTest {

    private static final Path WEB = Path.of("src/main/java/com/licensis/notaire/adapter/in/web");
    private static final Pattern DELETE = Pattern.compile("@DeleteMapping\\b");
    private static final Pattern DOCUMENTED = Pattern.compile("responseCode\\s*=\\s*\"(2\\d\\d)\"");

    @Test
    @DisplayName("every DELETE handler returns exactly the 2xx status it documents")
    void deleteHandlersReturnTheDocumentedStatus() throws IOException {
        List<String> mismatches = new ArrayList<>();
        int checked = 0;
        try (Stream<Path> files = Files.walk(WEB)) {
            for (Path file : files.filter(p -> p.toString().endsWith("Controller.java")).sorted().toList()) {
                String src = Files.readString(file);
                Matcher m = DELETE.matcher(src);
                while (m.find()) {
                    checked++;
                    String annotations = annotationBlock(src, m.start());
                    String body = methodBody(src, m.end());
                    Set<String> documented = new TreeSet<>();
                    Matcher d = DOCUMENTED.matcher(annotations);
                    while (d.find()) {
                        documented.add(d.group(1));
                    }
                    if (documented.isEmpty()) {
                        documented.add("200");
                    }
                    Set<String> returned = returnedSuccessCodes(body);
                    if (!returned.equals(documented)) {
                        mismatches.add(WEB.relativize(file) + " line " + line(src, m.start())
                                + ": documents " + documented + " but returns " + returned);
                    }
                }
            }
        }
        assertThat(checked).as("DELETE handlers found").isGreaterThan(25);
        assertThat(mismatches).as("DELETE handlers whose success status differs from the contract").isEmpty();
    }

    /** Text between the end of the previous member and the mapping annotation. */
    private static String annotationBlock(String src, int mappingStart) {
        int prev = Math.max(src.lastIndexOf("}\n", mappingStart), src.lastIndexOf(";\n", mappingStart));
        return src.substring(Math.max(prev, 0), mappingStart)
                + src.substring(mappingStart, src.indexOf('{', mappingStart));
    }

    /** The handler body: from the first '{' after the signature to its matching '}'. */
    private static String methodBody(String src, int from) {
        int open = src.indexOf('{', src.indexOf(')', src.indexOf("public", from)));
        int depth = 0;
        for (int i = open; i < src.length(); i++) {
            char c = src.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}' && --depth == 0) {
                return src.substring(open, i + 1);
            }
        }
        return src.substring(open);
    }

    private static Set<String> returnedSuccessCodes(String body) {
        Set<String> codes = new TreeSet<>();
        if (body.contains("ResponseEntity.ok(") || body.contains("HttpStatus.OK)")) {
            codes.add("200");
        }
        if (body.contains("ResponseEntity.noContent(") || body.contains("HttpStatus.NO_CONTENT")) {
            codes.add("204");
        }
        if (body.contains("ResponseEntity.accepted(") || body.contains("HttpStatus.ACCEPTED")) {
            codes.add("202");
        }
        return codes;
    }

    private static int line(String src, int offset) {
        return (int) src.substring(0, offset).chars().filter(c -> c == '\n').count() + 1;
    }
}
