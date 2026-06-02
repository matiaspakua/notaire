package com.licensis.notaire.audit;

import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.repository.UsuarioRepository;
import com.licensis.notaire.service.RegistroAuditoriaService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

/**
 * Spring AOP aspect that writes a business audit record to the
 * {@code registro_auditoria} table for every REST operation handled by
 * controllers in the {@code com.licensis.notaire.api} package.
 *
 * <p>The aspect is intentionally tolerant: any failure during the audit
 * write is logged at WARN level and swallowed, so functional flows are
 * never broken by audit-related issues.
 */
@Aspect
@Component
public class AuditoriaAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaAspect.class);

    /**
     * Header set by the Next.js frontend carrying the name of the currently
     * logged-in application user. Used to attribute audit records since the
     * REST API itself is stateless and does not establish a Spring Security
     * session for application logins.
     */
    public static final String ACTING_USER_HEADER = "X-Notaire-User";

    private final RegistroAuditoriaService auditoriaService;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaAspect(RegistroAuditoriaService auditoriaService, UsuarioRepository usuarioRepository) {
        this.auditoriaService = auditoriaService;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Pointcut that targets any public method declared in any class of the
     * {@code com.licensis.notaire.api} package. The
     * {@link RegistroAuditoriaController} itself is excluded to avoid
     * recursive auditing of audit reads.
     */
    @Pointcut(
        "execution(public * com.licensis.notaire.api..*Controller.*(..)) "
        + "&& !within(com.licensis.notaire.api.RegistroAuditoriaController)"
    )
    public void controllerMethods() {
        // Pointcut signature only.
    }

    @AfterReturning("controllerMethods()")
    public void auditAfterControllerInvocation(JoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            // Only business operations are audited: create / update / delete and
            // authentication events. Read-only queries (GET) are intentionally
            // skipped so the audit trail stays focused and is not flooded by the
            // many list refreshes the UI performs.
            if (!isAuditableOperation(method)) {
                return;
            }

            String controllerName = resolveControllerSimpleName(joinPoint);
            String modulo = AuditModuleResolver.resolve(controllerName);
            String detalle = AuditOperationDescriber.describe(method, joinPoint.getArgs(), modulo);

            Usuario usuario = resolveCurrentUser().orElse(null);
            if (usuario == null) {
                log.debug("Skipping audit for {} — no acting user", controllerName);
                return;
            }

            RegistroAuditoria registro = new RegistroAuditoria();
            registro.setFecha(new Date());
            registro.setModulo(modulo);
            registro.setDetalleOperacion(detalle);
            registro.setFkIdUsuario(usuario);

            auditoriaService.save(registro);
            log.debug("Audit record created — modulo={}, detalle={}, usuario={}",
                    modulo, detalle, usuario.getNombre());
        } catch (RuntimeException ex) {
            // Never break functional flows because of audit failures.
            log.warn("Could not write audit record: {}", ex.getMessage());
        }
    }

    /**
     * Resolves the original controller simple name. When the target is a
     * Spring CGLIB or Mockito proxy ({@code EscrituraController$$EnhancerBy...}
     * / {@code EscrituraController$MockitoMock$...}), we walk up to the
     * declaring superclass to recover the original name.
     */
    private String resolveControllerSimpleName(JoinPoint joinPoint) {
        Class<?> targetClass = joinPoint.getTarget().getClass();
        String simpleName = targetClass.getSimpleName();
        if (simpleName.contains("$$") || simpleName.contains("$Mockito")
                || simpleName.contains("EnhancerBy") || simpleName.contains("MockitoMock")) {
            Class<?> parent = targetClass.getSuperclass();
            if (parent != null && parent != Object.class) {
                simpleName = parent.getSimpleName();
            }
        }
        // Also fall back to the signature's declaring class if available.
        if (simpleName == null || simpleName.isBlank()) {
            simpleName = joinPoint.getSignature().getDeclaringType().getSimpleName();
        }
        return simpleName;
    }

    /**
     * Only audit state-changing business operations (create / update / delete)
     * and authentication events (login / logout). Read-only GET endpoints are
     * not auditable.
     */
    private boolean isAuditableOperation(Method method) {
        if (method == null) {
            return false;
        }
        String name = method.getName().toLowerCase(Locale.ROOT);
        if (name.contains("login") || name.contains("logout")) {
            return true;
        }
        return method.isAnnotationPresent(PostMapping.class)
                || method.isAnnotationPresent(PutMapping.class)
                || method.isAnnotationPresent(PatchMapping.class)
                || method.isAnnotationPresent(DeleteMapping.class);
    }

    /**
     * Resolves the acting user from (1) the Spring Security context, then
     * (2) the {@link #ACTING_USER_HEADER} request header set by the frontend.
     * The application login is stateless, so in practice (2) is the common path.
     */
    private Optional<Usuario> resolveCurrentUser() {
        String name = resolveActingUsername();
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getNombre() != null && u.getNombre().equalsIgnoreCase(name))
                .findFirst();
    }

    private String resolveActingUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String name = authentication.getName();
            if (name != null && !name.isBlank() && !"anonymousUser".equals(name)) {
                return name;
            }
        }
        return currentRequestHeader(ACTING_USER_HEADER);
    }

    private String currentRequestHeader(String headerName) {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest().getHeader(headerName);
            }
        } catch (RuntimeException ignored) {
            // No request bound to the current thread (e.g. async); not auditable.
        }
        return null;
    }
}
