package com.licensis.notaire.application.usecase.mfa;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.mfa.EnrollTotpUseCase;
import com.licensis.notaire.application.port.in.mfa.TotpEnrollmentStart;
import com.licensis.notaire.application.port.out.mfa.ClockPort;
import com.licensis.notaire.application.port.out.mfa.MfaEnrollmentStorePort;
import com.licensis.notaire.application.port.out.mfa.RandomBytesPort;
import com.licensis.notaire.application.port.out.user.PasswordHasherPort;
import com.licensis.notaire.domain.mfa.Base32Codec;
import com.licensis.notaire.domain.mfa.InvalidMfaCodeException;
import com.licensis.notaire.domain.mfa.MfaAlreadyEnrolledException;
import com.licensis.notaire.domain.mfa.MfaEnrollment;
import com.licensis.notaire.domain.mfa.MfaNotEnrolledException;
import com.licensis.notaire.domain.mfa.TotpAlgorithm;

@Service
@Transactional
public class TotpEnrollmentService implements EnrollTotpUseCase {

    private static final String ISSUER = "Notaire";
    private static final int SECRET_BYTES = 20;
    private static final int RECOVERY_CODE_COUNT = 10;
    private static final int RECOVERY_CODE_LENGTH = 10;
    private static final String CROCKFORD = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";

    private final MfaEnrollmentStorePort store;
    private final RandomBytesPort random;
    private final ClockPort clock;
    private final PasswordHasherPort hasher;

    public TotpEnrollmentService(MfaEnrollmentStorePort store, RandomBytesPort random, ClockPort clock,
            PasswordHasherPort hasher) {
        this.store = store;
        this.random = random;
        this.clock = clock;
        this.hasher = hasher;
    }

    @Override
    public TotpEnrollmentStart begin(int userId, String accountName) {
        if (store.findByUserId(userId).filter(MfaEnrollment::confirmed).isPresent()) {
            throw new MfaAlreadyEnrolledException();
        }
        String secret = Base32Codec.encode(random.randomBytes(SECRET_BYTES));
        store.save(MfaEnrollment.begin(userId, secret));
        String uri = "otpauth://totp/" + ISSUER + ":" + accountName + "?secret=" + secret + "&issuer=" + ISSUER
                + "&algorithm=SHA1&digits=" + TotpAlgorithm.DIGITS + "&period=" + TotpAlgorithm.PERIOD_SECONDS;
        return new TotpEnrollmentStart(secret, uri);
    }

    @Override
    public List<String> confirm(int userId, String code) {
        MfaEnrollment pending = store.findByUserId(userId).filter(e -> !e.confirmed())
                .orElseThrow(MfaNotEnrolledException::new);
        OptionalLong step = TotpAlgorithm.matchingStep(Base32Codec.decode(pending.secretBase32()), code,
                TotpAlgorithm.stepAt(clock.now()));
        if (step.isEmpty()) {
            throw new InvalidMfaCodeException();
        }
        List<String> codes = newRecoveryCodes();
        store.save(pending.confirm(step.getAsLong(), codes.stream().map(hasher::encode).toList()));
        return codes;
    }

    private List<String> newRecoveryCodes() {
        List<String> codes = new ArrayList<>(RECOVERY_CODE_COUNT);
        while (codes.size() < RECOVERY_CODE_COUNT) {
            String candidate = recoveryCode();
            if (!codes.contains(candidate)) {
                codes.add(candidate);
            }
        }
        return codes;
    }

    private String recoveryCode() {
        byte[] bytes = random.randomBytes(RECOVERY_CODE_LENGTH);
        StringBuilder code = new StringBuilder(RECOVERY_CODE_LENGTH);
        for (byte b : bytes) {
            code.append(CROCKFORD.charAt(b & 31));
        }
        return code.toString();
    }
}
