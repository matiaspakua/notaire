// semgrep fixture — notaire-spring-csrf-disabled must fire on this file.
// Do NOT use this pattern in production code; CSRF must be enabled for
// cookie-authenticated browser flows. See CYBERSECURITY-PROGRAM.md.
package com.licensis.notaire.security.fixture;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@SuppressWarnings("all")
class DisabledCsrf {
    void badConfig(HttpSecurity http) throws Exception {
        // ruleid: notaire-spring-csrf-disabled
        http.csrf(csrf -> csrf.disable());
    }
}
