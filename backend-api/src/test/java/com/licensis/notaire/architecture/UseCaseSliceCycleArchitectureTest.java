package com.licensis.notaire.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

/**
 * Epic #1693 / issue #1725: no dependency cycles between {@code application.usecase.(*)..} slices
 * (ADR-030). Cycle reports are slice rings, not class-to-class pairs, so they use a dedicated
 * shrink-only baseline ({@code architecture/usecase-cycle-baseline.txt}).
 *
 * <p>Rewrite with {@code -Darchitecture.baseline=shrink} or {@code -Darchitecture.baseline=init}.
 */
@DisplayName("Use-case slices have no dependency cycles except a shrink-only baseline (#1725)")
class UseCaseSliceCycleArchitectureTest {

    private static final String ROOT = "com.licensis.notaire";
    private static final String USECASE = ROOT + ".application.usecase";
    private static final String BASELINE_RESOURCE = "/architecture/usecase-cycle-baseline.txt";
    private static final String BASELINE_FILE = "src/test/resources/architecture/usecase-cycle-baseline.txt";
    private static final String WRITE_MODE = System.getProperty("architecture.baseline", "");

    private static JavaClasses production;
    private static Set<String> baseline;

    @BeforeAll
    static void importProduction() throws IOException {
        production = new ClassFileImporter().withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages(ROOT);
        baseline = readBaseline();
    }

    @Test
    @DisplayName("R7 use-case slices do not form dependency cycles beyond the baseline")
    void useCaseSlicesHaveNoNewCycles() throws IOException {
        Set<String> found = findCycles(buildSliceGraph(production));
        if (!WRITE_MODE.isEmpty()) {
            writeBaseline(found);
            return;
        }
        Set<String> added = new TreeSet<>(found);
        added.removeAll(baseline);
        Set<String> fixed = new TreeSet<>(baseline);
        fixed.removeAll(found);

        assertThat(added).as("R7: new use-case slice cycles (fix them, do not add to the baseline)").isEmpty();
        assertThat(fixed).as("R7: baseline cycles that no longer exist (remove them: "
                + "mvn test -pl backend-api -Dtest=UseCaseSliceCycleArchitectureTest "
                + "-Darchitecture.baseline=shrink)")
                .isEmpty();
    }

    @Test
    @DisplayName("the cycle baseline file is sorted, has no duplicates and uses canonical rings")
    void baselineIsWellFormed() throws IOException {
        List<String> lines = Files.exists(Path.of(BASELINE_FILE))
                ? Files.readAllLines(Path.of(BASELINE_FILE)).stream()
                        .map(String::strip)
                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                        .toList()
                : List.of();

        assertThat(lines).isSorted().doesNotHaveDuplicates()
                .allMatch(line -> line.matches("\\S+( -> \\S+)+"));
        for (String line : lines) {
            assertThat(canonicalizeRing(parseRing(line))).isEqualTo(line);
        }
    }

    @Test
    @DisplayName("ratchet rejects a synthetic new cycle and a stale baseline entry")
    void ratchetRejectsNewAndStaleCycles() {
        Set<String> found = Set.of("a -> b -> a");
        Set<String> knownEmpty = Set.of();
        Set<String> knownStale = Set.of("a -> b -> a", "c -> d -> c");

        Set<String> added = new TreeSet<>(found);
        added.removeAll(knownEmpty);
        assertThat(added).containsExactly("a -> b -> a");

        Set<String> stale = new TreeSet<>(knownStale);
        stale.removeAll(found);
        assertThat(stale).containsExactly("c -> d -> c");
    }

    @Test
    @DisplayName("canonical ring form is stable regardless of start or direction")
    void canonicalRingIsStable() {
        assertThat(canonicalizeRing(List.of("document", "management", "document")))
                .isEqualTo("document -> management -> document");
        assertThat(canonicalizeRing(List.of("management", "document", "management")))
                .isEqualTo("document -> management -> document");
        assertThat(canonicalizeRing(List.of("c", "a", "b", "c")))
                .isEqualTo(canonicalizeRing(List.of("a", "b", "c", "a")));
        assertThat(canonicalizeRing(List.of("c", "b", "a", "c")))
                .isEqualTo(canonicalizeRing(List.of("a", "c", "b", "a")));
    }

    static Map<String, Set<String>> buildSliceGraph(JavaClasses classes) {
        Map<String, Set<String>> graph = new TreeMap<>();
        for (JavaClass type : classes) {
            String originSlice = sliceOf(type);
            if (originSlice == null) {
                continue;
            }
            graph.computeIfAbsent(originSlice, ignored -> new TreeSet<>());
            for (var dependency : type.getDirectDependenciesFromSelf()) {
                String targetSlice = sliceOf(dependency.getTargetClass());
                if (targetSlice != null && !targetSlice.equals(originSlice)) {
                    graph.get(originSlice).add(targetSlice);
                }
            }
        }
        return graph;
    }

    static String sliceOf(JavaClass type) {
        String pkg = type.getPackageName();
        if (!pkg.equals(USECASE) && !pkg.startsWith(USECASE + ".")) {
            return null;
        }
        String rest = pkg.substring(USECASE.length());
        if (rest.isEmpty()) {
            return null;
        }
        String[] parts = rest.substring(1).split("\\.", 2);
        return parts[0].isEmpty() ? null : parts[0];
    }

    /**
     * Returns canonical closed rings for every elementary cycle in the directed slice graph.
     * Self-loops are recorded as {@code slice -> slice}.
     */
    static Set<String> findCycles(Map<String, Set<String>> graph) {
        Set<String> cycles = new TreeSet<>();
        for (String start : graph.keySet()) {
            for (String neighbour : graph.getOrDefault(start, Set.of())) {
                if (neighbour.equals(start)) {
                    cycles.add(start + " -> " + start);
                }
            }
        }
        List<String> nodes = new ArrayList<>(graph.keySet());
        for (int i = 0; i < nodes.size(); i++) {
            String start = nodes.get(i);
            Deque<String> path = new ArrayDeque<>();
            Set<String> onPath = new HashSet<>();
            path.addLast(start);
            onPath.add(start);
            dfsCycles(start, start, graph, path, onPath, cycles, nodes.subList(0, i + 1).stream()
                    .collect(Collectors.toCollection(HashSet::new)));
        }
        return cycles;
    }

    private static void dfsCycles(String start, String current, Map<String, Set<String>> graph, Deque<String> path,
            Set<String> onPath, Set<String> cycles, Set<String> blockedStarts) {
        for (String next : graph.getOrDefault(current, Set.of())) {
            if (next.equals(start) && path.size() >= 2) {
                List<String> ring = new ArrayList<>(path);
                ring.add(start);
                cycles.add(canonicalizeRing(ring));
                continue;
            }
            if (blockedStarts.contains(next) || onPath.contains(next) || next.compareTo(start) < 0) {
                continue;
            }
            path.addLast(next);
            onPath.add(next);
            dfsCycles(start, next, graph, path, onPath, cycles, blockedStarts);
            path.removeLast();
            onPath.remove(next);
        }
    }

    static String canonicalizeRing(List<String> ring) {
        if (ring.size() < 2) {
            throw new IllegalArgumentException("ring too short: " + ring);
        }
        List<String> nodes = new ArrayList<>(ring);
        if (nodes.get(0).equals(nodes.get(nodes.size() - 1))) {
            nodes.remove(nodes.size() - 1);
        }
        if (nodes.size() == 1) {
            return nodes.get(0) + " -> " + nodes.get(0);
        }
        int minIndex = 0;
        for (int i = 1; i < nodes.size(); i++) {
            if (nodes.get(i).compareTo(nodes.get(minIndex)) < 0) {
                minIndex = i;
            }
        }
        List<String> forward = rotate(nodes, minIndex);
        List<String> reverseNodes = new ArrayList<>(nodes);
        Collections.reverse(reverseNodes);
        int revMin = 0;
        for (int i = 1; i < reverseNodes.size(); i++) {
            if (reverseNodes.get(i).compareTo(reverseNodes.get(revMin)) < 0) {
                revMin = i;
            }
        }
        List<String> reverse = rotate(reverseNodes, revMin);
        List<String> chosen = compareSequences(forward, reverse) <= 0 ? forward : reverse;
        List<String> closed = new ArrayList<>(chosen);
        closed.add(chosen.get(0));
        return String.join(" -> ", closed);
    }

    private static List<String> rotate(List<String> nodes, int start) {
        List<String> rotated = new ArrayList<>(nodes.size());
        for (int i = 0; i < nodes.size(); i++) {
            rotated.add(nodes.get((start + i) % nodes.size()));
        }
        return rotated;
    }

    private static int compareSequences(List<String> left, List<String> right) {
        int n = Math.min(left.size(), right.size());
        for (int i = 0; i < n; i++) {
            int cmp = left.get(i).compareTo(right.get(i));
            if (cmp != 0) {
                return cmp;
            }
        }
        return Integer.compare(left.size(), right.size());
    }

    static List<String> parseRing(String line) {
        String[] parts = line.split(" -> ");
        List<String> nodes = new ArrayList<>();
        for (String part : parts) {
            nodes.add(part.strip());
        }
        return nodes;
    }

    private static Set<String> readBaseline() throws IOException {
        try (InputStream stream = UseCaseSliceCycleArchitectureTest.class.getResourceAsStream(BASELINE_RESOURCE)) {
            if (stream == null) {
                return new TreeSet<>();
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8).lines()
                    .map(String::strip)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .collect(Collectors.toCollection(TreeSet::new));
        }
    }

    private static void writeBaseline(Set<String> found) throws IOException {
        Set<String> next = new TreeSet<>(found);
        if ("shrink".equals(WRITE_MODE)) {
            next.retainAll(baseline);
        } else if (!"init".equals(WRITE_MODE)) {
            throw new IllegalArgumentException("architecture.baseline must be 'init' or 'shrink'");
        }
        Path path = Path.of(BASELINE_FILE);
        Files.createDirectories(path.getParent());
        String body = next.isEmpty()
                ? "# Use-case slice cycle baseline (issue #1725). Empty means no known cycles.\n"
                : String.join("\n", next) + "\n";
        Files.write(path, body.getBytes(StandardCharsets.UTF_8));
    }
}
