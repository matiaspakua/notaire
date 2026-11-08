package com.licensis.notaire.adapter.out.security;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.mfa.RandomBytesPort;

@Component
public class SecureRandomAdapter implements RandomBytesPort {

    private final SecureRandom random = new SecureRandom();

    @Override
    public byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }
}
