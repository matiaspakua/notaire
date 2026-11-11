package com.licensis.notaire.domain.mfa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.licensis.notaire.domain.user.AccountRole;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU78"})
@DisplayName("Login MFA policy decisions (CU78, ADR-032)")
class LoginMfaPolicyTest {

    private static final Set<AccountRole> ADMIN_AND_NOTARY = Set.of(AccountRole.ADMIN, AccountRole.NOTARY);

    @ParameterizedTest(name = "{0} {1} enrolled={2} -> {3}")
    @CsvSource({
        "OFF,ADMIN,true,SESSION",
        "OFF,ADMIN,false,SESSION",
        "OPTIONAL,ADMIN,true,CHALLENGE",
        "OPTIONAL,ADMIN,false,SESSION",
        "OPTIONAL,USER,true,CHALLENGE",
        "OPTIONAL,USER,false,SESSION",
        "ENFORCE,ADMIN,true,CHALLENGE",
        "ENFORCE,ADMIN,false,ENROLLMENT_REQUIRED",
        "ENFORCE,NOTARY,false,ENROLLMENT_REQUIRED",
        "ENFORCE,NOTARY,true,CHALLENGE",
        "ENFORCE,USER,false,SESSION",
        "ENFORCE,USER,true,CHALLENGE"
    })
    @DisplayName("decides session, challenge or enrolment by mode, role and enrolment")
    void shouldDecide(MfaPolicyMode mode, AccountRole role, boolean enrolled, LoginMfaDecision expected) {
        assertThat(new LoginMfaPolicy(mode, ADMIN_AND_NOTARY).decide(role, enrolled)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({"ADMIN,ADMIN", "administrador,ADMIN", " Admin ,ADMIN", "ESCRIBANO,NOTARY", "Secretario,USER", ",USER"})
    @DisplayName("maps user types to account roles")
    void shouldMapRole(String type, AccountRole expected) {
        assertThat(AccountRole.ofUserType(type)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "[{0}] -> {1}")
    @CsvSource({"ENFORCE,ENFORCE", "enforce,ENFORCE", "off,OFF", "Optional,OPTIONAL", "'',OPTIONAL"})
    @DisplayName("parses the mode, defaulting to OPTIONAL")
    void shouldParseMode(String raw, MfaPolicyMode expected) {
        assertThat(MfaPolicyMode.parse(raw)).isEqualTo(expected);
    }
}
