package com.licensis.notaire.application.port.in.mfa;

import com.licensis.notaire.domain.mfa.LoginMfaDecision;

public interface AssessLoginMfaUseCase {

    /** Decides, after a valid password, whether to issue a session, a challenge or require enrolment. */
    LoginMfaDecision assess(int userId, String userType);
}
