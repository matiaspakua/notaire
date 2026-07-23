package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Guards against the JaCoCo coverage-floor numbers drifting out of sync again across
 * pom.xml, CLAUDE.md and .claude/rules/code-quality.md (see issue #588).
 */
class JacocoCoverageConfigConsistencyTest {

    private static final String POM_PATH = "pom.xml";
    private static final String CLAUDE_MD_PATH = "../CLAUDE.md";
    private static final String CODE_QUALITY_MD_PATH = "../.claude/rules/code-quality.md";
    private static final Pattern LINE_MINIMUM_PATTERN = Pattern.compile(
            "<counter>LINE</counter>\\s*<value>COVEREDRATIO</value>\\s*<minimum>([\\d.]+)</minimum>");
    private static final Pattern BRANCH_MINIMUM_PATTERN = Pattern.compile(
            "<counter>BRANCH</counter>\\s*<value>COVEREDRATIO</value>\\s*<minimum>([\\d.]+)</minimum>");

    @Test
    @DisplayName("Should not reference the nonexistent servicios package in JaCoCo exclusions")
    void shouldNotReferenceNonexistentServiciosPackageInPomExclusions() throws IOException {
        String pomContent = Files.readString(Paths.get(POM_PATH));

        assertThat(pomContent)
                .as("pom.xml JaCoCo exclusions must reference the real 'service' package, not 'servicios'")
                .doesNotContain("com/licensis/notaire/servicios/");
    }

    @Test
    @DisplayName("Should have the enforced coverage floor documented consistently in code-quality.md")
    void shouldHaveConsistentCoverageFloorAcrossDocsAndPom() throws IOException {
        String pomContent = Files.readString(Paths.get(POM_PATH));
        String codeQualityMd = Files.readString(Paths.get(CODE_QUALITY_MD_PATH));

        int linePercent = extractPercent(pomContent, LINE_MINIMUM_PATTERN);
        int branchPercent = extractPercent(pomContent, BRANCH_MINIMUM_PATTERN);

        String expectedFragment = linePercent + "% line / " + branchPercent + "% branch";

        assertThat(codeQualityMd)
                .as("code-quality.md is the single source of truth for the enforced floor and must match "
                        + "pom.xml's <minimum> values")
                .contains(expectedFragment);
    }

    @Test
    @DisplayName("Should not restate a stale enforced coverage floor in CLAUDE.md")
    void shouldNotRestateStaleCoverageFloorInClaudeMd() throws IOException {
        String claudeMd = Files.readString(Paths.get(CLAUDE_MD_PATH));

        assertThat(claudeMd)
                .as("CLAUDE.md must not duplicate a specific enforced-floor percentage (single source of truth "
                        + "is code-quality.md) — the historical '28% line / 14% branch' figure went stale once "
                        + "pom.xml's floor was raised in Phase 8")
                .doesNotContain("28% line / 14% branch");
    }

    private int extractPercent(String pomContent, Pattern pattern) {
        Matcher matcher = pattern.matcher(pomContent);
        assertThat(matcher.find()).as("pom.xml must declare a %s COVEREDRATIO minimum", pattern).isTrue();
        return Math.round(Float.parseFloat(matcher.group(1)) * 100);
    }
}
