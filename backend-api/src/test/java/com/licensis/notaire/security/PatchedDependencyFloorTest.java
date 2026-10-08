package com.licensis.notaire.security;

import org.apache.catalina.util.ServerInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Issue #1327 (CU78): the runtime classpath must not carry library versions with known
 * HIGH/CRITICAL CVEs that have a fix. Each check reads the version the library reports
 * about itself, so it holds whatever pins or Spring Boot upgrades produce it.
 *
 * <p>Fixes ship per release line (for example Jackson 2.21.7 and 2.22.3, while 2.22.0-2.22.2
 * stay vulnerable), so the floors are keyed by {@code major.minor}. A line newer than every
 * listed one passes; an older or unlisted line fails and needs a fresh review.
 */
@DisplayName("Patched dependency floors (#1327)")
class PatchedDependencyFloorTest {

    @Test
    @DisplayName("Embedded Tomcat is patched for CVE-2026-65182, CVE-2026-65905 and CVE-2026-68525")
    void tomcatIsPatched() {
        assertPatched("tomcat-embed-core", ServerInfo.getServerNumber(), Map.of("11.0", 25));
    }

    @Test
    @DisplayName("Jackson 2 databind/core are patched for CVE-2026-68497, -89407, -89425, -91776, -91777")
    void jackson2IsPatched() {
        Map<String, Integer> floors = Map.of("2.21", 7, "2.22", 3);
        assertPatched("jackson-databind 2", com.fasterxml.jackson.databind.cfg.PackageVersion.VERSION.toString(),
                floors);
        assertPatched("jackson-core 2", com.fasterxml.jackson.core.json.PackageVersion.VERSION.toString(), floors);
    }

    @Test
    @DisplayName("Jackson 3 databind/core are patched for CVE-2026-68497, -89407, -89425, -91776, -91777")
    void jackson3IsPatched() {
        Map<String, Integer> floors = Map.of("3.1", 7, "3.2", 3);
        assertPatched("jackson-databind 3", tools.jackson.databind.cfg.PackageVersion.VERSION.toString(), floors);
        assertPatched("jackson-core 3", tools.jackson.core.json.PackageVersion.VERSION.toString(), floors);
    }

    @Test
    @DisplayName("commons-collections 2.x/3.x (CVE-2015-7501 InvokerTransformer gadget) is not on the classpath")
    void commonsCollectionsGadgetIsAbsent() {
        assertThatThrownBy(() -> Class.forName("org.apache.commons.collections.functors.InvokerTransformer"))
                .isInstanceOf(ClassNotFoundException.class);
    }

    /**
     * Fails unless {@code version} is at or above the floor for its release line, or on a
     * line newer than every listed one.
     */
    private static void assertPatched(String library, String version, Map<String, Integer> floorsByLine) {
        int[] v = numbers(version);
        String line = v[0] + "." + v[1];
        Integer floor = floorsByLine.get(line);
        boolean newerLine = floorsByLine.keySet().stream()
                .map(PatchedDependencyFloorTest::numbers)
                .allMatch(l -> v[0] > l[0] || (v[0] == l[0] && v[1] > l[1]));
        assertThat(newerLine || (floor != null && v[2] >= floor))
                .as("%s %s must be at least %s.%d on its line (floors: %s)", library, version, line,
                        floor == null ? -1 : floor, floorsByLine)
                .isTrue();
    }

    private static int[] numbers(String version) {
        String[] parts = version.split("[^0-9]+");
        int[] out = Arrays.stream(parts).filter(p -> !p.isEmpty()).mapToInt(Integer::parseInt).toArray();
        return Arrays.copyOf(out, Math.max(out.length, 3));
    }
}
