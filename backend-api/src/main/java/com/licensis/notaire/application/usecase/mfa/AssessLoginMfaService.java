package com.licensis.notaire.application.usecase.mfa;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.mfa.AssessLoginMfaUseCase;
import com.licensis.notaire.application.port.out.mfa.MfaEnrollmentStorePort;
import com.licensis.notaire.domain.mfa.LoginMfaDecision;
import com.licensis.notaire.domain.mfa.LoginMfaPolicy;
import com.licensis.notaire.domain.mfa.MfaEnrollment;
import com.licensis.notaire.domain.user.AccountRole;

@Service
@Transactional(readOnly = true)
public class AssessLoginMfaService implements AssessLoginMfaUseCase {

    private final MfaEnrollmentStorePort store;
    private final LoginMfaPolicy policy;

    public AssessLoginMfaService(MfaEnrollmentStorePort store, LoginMfaPolicy policy) {
        this.store = store;
        this.policy = policy;
    }

    @Override
    public LoginMfaDecision assess(int userId, String userType) {
        boolean enrolled = store.findByUserId(userId).filter(MfaEnrollment::confirmed).isPresent();
        return policy.decide(AccountRole.ofUserType(userType), enrolled);
    }
}
