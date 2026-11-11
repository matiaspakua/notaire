package com.licensis.notaire.config;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.licensis.notaire.domain.mfa.LoginMfaPolicy;
import com.licensis.notaire.domain.mfa.MfaPolicyMode;
import com.licensis.notaire.domain.user.AccountRole;

/** Composition root of the MFA login policy (ADR-032): default OPTIONAL keeps existing deployments unchanged. */
@Configuration
public class MfaPolicyConfig {

    @Bean
    public LoginMfaPolicy loginMfaPolicy(
            @Value("${notaire.mfa.policy:OPTIONAL}") String mode,
            @Value("${notaire.mfa.required-roles:ADMIN,NOTARY}") String requiredRoles,
            @Value("${notaire.mfa.encryption-key:}") String encryptionKey) {
        MfaPolicyMode parsed = MfaPolicyMode.parse(mode);
        if (parsed == MfaPolicyMode.ENFORCE && (encryptionKey == null || encryptionKey.isBlank())) {
            throw new IllegalStateException(
                    "notaire.mfa.policy=ENFORCE requires NOTAIRE_MFA_ENCRYPTION_KEY, otherwise no one could enrol");
        }
        return new LoginMfaPolicy(parsed, roles(requiredRoles));
    }

    private static Set<AccountRole> roles(String csv) {
        Set<AccountRole> roles = EnumSet.noneOf(AccountRole.class);
        Arrays.stream(csv.split(","))
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .filter(value -> Arrays.stream(AccountRole.values()).anyMatch(role -> role.name().equals(value)))
                .map(AccountRole::valueOf)
                .forEach(roles::add);
        return roles;
    }
}
