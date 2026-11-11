package com.licensis.notaire.domain.mfa;

import java.util.Set;

import com.licensis.notaire.domain.user.AccountRole;

/** What a password-verified login must do next (ADR-032 decision 6). */
public record LoginMfaPolicy(MfaPolicyMode mode, Set<AccountRole> requiredRoles) {

    public LoginMfaPolicy {
        requiredRoles = Set.copyOf(requiredRoles);
    }

    public LoginMfaDecision decide(AccountRole role, boolean hasConfirmedEnrollment) {
        if (mode == MfaPolicyMode.OFF) {
            return LoginMfaDecision.SESSION;
        }
        if (hasConfirmedEnrollment) {
            return LoginMfaDecision.CHALLENGE;
        }
        boolean mustEnrol = mode == MfaPolicyMode.ENFORCE && requiredRoles.contains(role);
        return mustEnrol ? LoginMfaDecision.ENROLLMENT_REQUIRED : LoginMfaDecision.SESSION;
    }
}
