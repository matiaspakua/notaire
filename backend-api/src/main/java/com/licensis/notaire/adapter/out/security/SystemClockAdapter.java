package com.licensis.notaire.adapter.out.security;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.mfa.ClockPort;

@Component
public class SystemClockAdapter implements ClockPort {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
