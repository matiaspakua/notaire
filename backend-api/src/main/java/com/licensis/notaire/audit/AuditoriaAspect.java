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

import java.util.Date;
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
            String controllerName = resolveControllerSimpleName(joinPoint);
            String modulo = AuditModuleResolver.resolve(controllerName);

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String detalle = AuditOperationDescriber.describe(
                    signature.getMethod(), joinPoint.getArgs(), modulo);

            Usuario usuario = resolveCurrentUser().orElse(null);
            if (usuario == null) {
                log.debug("Skipping audit for {} — no authenticated user", controllerName);
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

    private Optional<Usuario> resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        String name = authentication.getName();
        if (name == null || name.isBlank() || "anonymousUser".equals(name)) {
            return Optional.empty();
        }
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getNombre() != null && u.getNombre().equalsIgnoreCase(name))
                .findFirst();
    }
}
