package com.licensis.notaire.application.port.out.mfa;

import java.time.Instant;

public interface ClockPort {

    Instant now();
}
