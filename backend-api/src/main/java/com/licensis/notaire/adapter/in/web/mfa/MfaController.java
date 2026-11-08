package com.licensis.notaire.adapter.in.web.mfa;

import java.security.Principal;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.licensis.notaire.application.port.in.mfa.DisableTotpUseCase;
import com.licensis.notaire.application.port.in.mfa.EnrollTotpUseCase;
import com.licensis.notaire.application.port.in.mfa.TotpEnrollmentStart;
import com.licensis.notaire.application.port.in.user.QueryUsersUseCase;
import com.licensis.notaire.domain.mfa.InvalidMfaCodeException;
import com.licensis.notaire.domain.user.LoginLockedException;
import com.licensis.notaire.observability.MetricsUtil;
import com.licensis.notaire.security.LoginAttemptService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * TOTP enrolment for the authenticated user only (ADR-029: single tenant, no object-level checks beyond own
 * account; ADR-032). Calls inbound ports only. Wrong codes share the login lockout; code values are never logged.
 */
@RestController
@RequestMapping("/api/v1/mfa")
@Tag(name = "MFA", description = "TOTP second-factor enrolment for the signed-in user")
public class MfaController {

    private static final Logger log = LoggerFactory.getLogger(MfaController.class);

    private final QueryUsersUseCase users;
    private final EnrollTotpUseCase enrol;
    private final DisableTotpUseCase disable;
    private final LoginAttemptService attempts;
    private final MetricsUtil metrics;

    public MfaController(QueryUsersUseCase users, EnrollTotpUseCase enrol, DisableTotpUseCase disable,
            LoginAttemptService attempts, MetricsUtil metrics) {
        this.users = users;
        this.enrol = enrol;
        this.disable = disable;
        this.attempts = attempts;
        this.metrics = metrics;
    }

    public record EnrollmentStartResponse(String secret, String otpauthUri) { }

    public record ConfirmRequest(@NotBlank String code) { }

    public record RecoveryCodesResponse(List<String> recoveryCodes) { }

    public record DisableRequest(String code, String recoveryCode) { }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Secret and otpauth URI, shown once"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "409", description = "Already enrolled"),
        @ApiResponse(responseCode = "429", description = "Account temporarily locked")
    })
    @PostMapping("/enrollment")
    @Operation(summary = "Begin TOTP enrolment (also reachable with the enrolment-scoped login token)")
    public ResponseEntity<EnrollmentStartResponse> begin(Principal principal) {
        return withUser(principal, id -> {
            TotpEnrollmentStart start = enrol.begin(id, principal.getName());
            return noStore(new EnrollmentStartResponse(start.secret(), start.otpauthUri()));
        });
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Enrolment confirmed; recovery codes shown once"),
        @ApiResponse(responseCode = "400", description = "Wrong code (counts toward lockout)"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "Enrolment not begun"),
        @ApiResponse(responseCode = "429", description = "Account temporarily locked")
    })
    @PostMapping("/enrollment/confirm")
    @Operation(summary = "Confirm TOTP enrolment with a current code")
    public ResponseEntity<RecoveryCodesResponse> confirm(Principal principal,
            @Valid @RequestBody ConfirmRequest request) {
        return withUser(principal, id -> checked(principal, () ->
                noStore(new RecoveryCodesResponse(enrol.confirm(id, request.code())))));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "MFA disabled"),
        @ApiResponse(responseCode = "400", description = "Wrong code (counts toward lockout)"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "No confirmed enrolment"),
        @ApiResponse(responseCode = "429", description = "Account temporarily locked")
    })
    @PostMapping("/disable")
    @Operation(summary = "Disable TOTP; requires a current TOTP or an unused recovery code")
    public ResponseEntity<Void> disable(Principal principal, @RequestBody DisableRequest request) {
        return withUser(principal, id -> checked(principal, () -> {
            disable.disable(id, request.code(), request.recoveryCode());
            log.info("MFA disabled for '{}'", principal.getName());
            return ResponseEntity.noContent().build();
        }));
    }

    private <T> ResponseEntity<T> withUser(Principal principal, IntFunction<ResponseEntity<T>> action) {
        String username = principal.getName();
        if (attempts.isLocked(username)) {
            metrics.incrementCounter("login", "locked");
            throw new LoginLockedException("Account temporarily locked after too many failed attempts.");
        }
        int id = users.findByName(username).orElseThrow(() -> new IllegalStateException("Unknown principal")).id();
        return action.apply(id);
    }

    private <T> T checked(Principal principal, Supplier<T> action) {
        try {
            return action.get();
        } catch (InvalidMfaCodeException e) {
            log.warn("MFA code rejected for '{}'", principal.getName());
            metrics.incrementCounter("login", "mfa_failed");
            attempts.onLoginFailed(principal.getName());
            throw e;
        }
    }

    private static <T> ResponseEntity<T> noStore(T body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}
