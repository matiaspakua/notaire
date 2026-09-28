package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
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
    private static final String CONSTITUTION_MD_PATH = "../CONSTITUTION.md";
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

        double linePercent = extractPercent(pomContent, LINE_MINIMUM_PATTERN);
        double branchPercent = extractPercent(pomContent, BRANCH_MINIMUM_PATTERN);

        String expectedFragment = linePercent + "% line / " + branchPercent + "% branch";

        assertThat(codeQualityMd)
                .as("code-quality.md is the single source of truth for the enforced floor and must match "
                        + "pom.xml's <minimum> values")
                .contains(expectedFragment);

        // The ratchet floor should match the pom.xml values
        assertThat(linePercent).as("Enforced ratchet floor should match pom.xml line minimum").isEqualTo(80);
        assertThat(branchPercent).as("Enforced ratchet floor should match pom.xml branch minimum").isEqualTo(65);
    }

    @Test
    @DisplayName("Should enforce the raised coverage floor (80% line / 65% branch)")
    void shouldEnforceRaisedCoverageFloor() throws IOException {
        String pomContent = Files.readString(Paths.get(POM_PATH));

        double linePercent = extractPercent(pomContent, LINE_MINIMUM_PATTERN);
        double branchPercent = extractPercent(pomContent, BRANCH_MINIMUM_PATTERN);

        assertThat(linePercent).as("Line coverage floor must be 80%").isEqualTo(80);
        assertThat(branchPercent).as("Branch coverage floor must be 65%").isEqualTo(65);
    }

    @Test
    @DisplayName("Should match CONSTITUTION.md floor to pom.xml")
    void shouldMatchConstitutionFloorToPom() throws IOException {
        String pomContent = Files.readString(Paths.get(POM_PATH));
        String constitutionMd = Files.readString(Paths.get(CONSTITUTION_MD_PATH));

        double linePercent = extractPercent(pomContent, LINE_MINIMUM_PATTERN);
        double branchPercent = extractPercent(pomContent, BRANCH_MINIMUM_PATTERN);

        // Check CONSTITUTION.md floor values match pom.xml
        assertThat(constitutionMd)
                .as("CONSTITUTION.md floor values must match pom.xml")
                .contains(linePercent + "% line / " + branchPercent + "% branch");
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

    private int extractPercent(String pomContent, Pattern pattern) throws IOException {
        Matcher matcher = pattern.matcher(pomContent);
        assertThat(matcher.find()).as("pom.xml must declare a %s COVEREDRATIO minimum", pattern).isTrue();
        return Math.round(Float.parseFloat(matcher.group(1)) * 100);
    }
}
