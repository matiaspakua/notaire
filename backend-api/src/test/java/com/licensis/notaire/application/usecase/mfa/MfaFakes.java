package com.licensis.notaire.application.usecase.mfa;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.licensis.notaire.application.port.out.mfa.ClockPort;
import com.licensis.notaire.application.port.out.mfa.MfaEnrollmentStorePort;
import com.licensis.notaire.application.port.out.mfa.RandomBytesPort;
import com.licensis.notaire.application.port.out.user.PasswordHasherPort;
import com.licensis.notaire.domain.mfa.MfaEnrollment;

final class MfaFakes {

    private MfaFakes() {
    }

    static final class InMemoryStore implements MfaEnrollmentStorePort {
        final Map<Integer, MfaEnrollment> rows = new HashMap<>();

        @Override
        public Optional<MfaEnrollment> findByUserId(int userId) {
            return Optional.ofNullable(rows.get(userId));
        }

        @Override
        public MfaEnrollment save(MfaEnrollment enrollment) {
            rows.put(enrollment.userId(), enrollment);
            return enrollment;
        }

        @Override
        public void deleteByUserId(int userId) {
            rows.remove(userId);
        }
    }

    static final class FixedClock implements ClockPort {
        Instant now;

        FixedClock(Instant now) {
            this.now = now;
        }

        @Override
        public Instant now() {
            return now;
        }
    }

    static final class CountingRandom implements RandomBytesPort {
        private final AtomicInteger counter = new AtomicInteger(1);

        @Override
        public byte[] randomBytes(int length) {
            byte[] bytes = new byte[length];
            int seed = counter.getAndIncrement();
            for (int i = 0; i < length; i++) {
                bytes[i] = (byte) (seed * 31 + i);
            }
            return bytes;
        }
    }

    static final class ReversingHasher implements PasswordHasherPort {
        @Override
        public String encode(String rawPassword) {
            return "$2h$" + new StringBuilder(rawPassword).reverse();
        }

        @Override
        public boolean passwordMatches(String rawPassword, String storedHash) {
            return encode(rawPassword).equals(storedHash);
        }

        @Override
        public boolean isBcryptHash(String hash) {
            return hash != null && hash.startsWith("$2");
        }
    }
}
