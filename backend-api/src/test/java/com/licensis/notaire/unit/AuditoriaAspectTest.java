package com.licensis.notaire.unit;

import com.licensis.notaire.audit.AuditoriaAspect;
import com.licensis.notaire.api.EscrituraController;
import com.licensis.notaire.api.UsuarioController;
import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.repository.UsuarioRepository;
import com.licensis.notaire.service.RegistroAuditoriaService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AuditoriaAspect unit tests")
@ExtendWith(MockitoExtension.class)
class AuditoriaAspectTest {

    @Mock
    private RegistroAuditoriaService auditoriaService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature signature;

    private AuditoriaAspect aspect;

    private Usuario adminUser;

    @BeforeEach
    void setUp() {
        aspect = new AuditoriaAspect(auditoriaService, usuarioRepository);
        adminUser = new Usuario();
        adminUser.setIdUsuario(1);
        adminUser.setNombre("admin");
        adminUser.setEstado(true);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String username) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                username, "secret", AuthorityUtils.createAuthorityList("ROLE_USER"));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private void stubJoinPoint(Class<?> target, Method method, Object[] args) {
        // Mockito.mock(target) yields a proxy whose getClass().getSimpleName()
        // is e.g. "EscrituraController$MockitoMock$xxx". The aspect handles
        // proxy class names by walking up the superclass chain, so we get back
        // the original controller name.
        Object targetInstance = org.mockito.Mockito.mock(target);
        lenient().when(joinPoint.getTarget()).thenReturn(targetInstance);
        lenient().when(joinPoint.getSignature()).thenReturn(signature);
        lenient().when(joinPoint.getArgs()).thenReturn(args);
        lenient().when(signature.getMethod()).thenReturn(method);
        lenient().when(signature.getDeclaringType()).thenAnswer(invocation -> target);
    }

    @Test
    @DisplayName("Should persist audit record when authenticated user invokes a controller method")
    void shouldPersistAuditRecordWhenAuthenticatedUserInvokesController() throws NoSuchMethodException {
        authenticateAs("admin");
        when(usuarioRepository.findAll()).thenReturn(List.of(adminUser));

        Method method = EscrituraController.class.getDeclaredMethod("getById", Integer.class);
        stubJoinPoint(EscrituraController.class, method, new Object[]{1});

        aspect.auditAfterControllerInvocation(joinPoint);

        ArgumentCaptor<RegistroAuditoria> captor = ArgumentCaptor.forClass(RegistroAuditoria.class);
        verify(auditoriaService).save(captor.capture());
        RegistroAuditoria saved = captor.getValue();
        assertThat(saved.getModulo()).isEqualTo("Escrituras");
        assertThat(saved.getFkIdUsuario()).isSameAs(adminUser);
        assertThat(saved.getFecha()).isNotNull();
        assertThat(saved.getDetalleOperacion()).isNotBlank();
    }

    @Test
    @DisplayName("Should skip audit when no authentication present")
    void shouldSkipAuditWhenNoAuthentication() throws NoSuchMethodException {
        SecurityContextHolder.clearContext();
        Method method = EscrituraController.class.getDeclaredMethod("getById", Integer.class);
        stubJoinPoint(EscrituraController.class, method, new Object[]{1});

        aspect.auditAfterControllerInvocation(joinPoint);

        verify(auditoriaService, never()).save(any());
    }

    @Test
    @DisplayName("Should skip audit when authenticated as anonymousUser")
    void shouldSkipAuditWhenAnonymousUser() throws NoSuchMethodException {
        authenticateAs("anonymousUser");
        Method method = EscrituraController.class.getDeclaredMethod("getById", Integer.class);
        stubJoinPoint(EscrituraController.class, method, new Object[]{1});

        aspect.auditAfterControllerInvocation(joinPoint);

        verify(auditoriaService, never()).save(any());
    }

    @Test
    @DisplayName("Should skip audit when authenticated user not found in DB")
    void shouldSkipAuditWhenUserNotFound() throws NoSuchMethodException {
        authenticateAs("ghost");
        when(usuarioRepository.findAll()).thenReturn(List.of(adminUser));

        Method method = EscrituraController.class.getDeclaredMethod("getById", Integer.class);
        stubJoinPoint(EscrituraController.class, method, new Object[]{1});

        aspect.auditAfterControllerInvocation(joinPoint);

        verify(auditoriaService, never()).save(any());
    }

    @Test
    @DisplayName("Should swallow audit failures without throwing")
    void shouldSwallowAuditFailuresWithoutThrowing() throws NoSuchMethodException {
        authenticateAs("admin");
        when(usuarioRepository.findAll()).thenReturn(List.of(adminUser));
        lenient().when(auditoriaService.save(any()))
                .thenThrow(new RuntimeException("simulated DB failure"));

        Method method = EscrituraController.class.getDeclaredMethod("getById", Integer.class);
        stubJoinPoint(EscrituraController.class, method, new Object[]{1});

        // Should not throw — the aspect must never break business flows.
        aspect.auditAfterControllerInvocation(joinPoint);

        verify(auditoriaService).save(any());
    }

    @Test
    @DisplayName("Should resolve Usuarios module from UsuarioController")
    void shouldResolveUsuariosModuleFromUsuarioController() throws NoSuchMethodException {
        authenticateAs("admin");
        when(usuarioRepository.findAll()).thenReturn(List.of(adminUser));

        Method method = UsuarioController.class.getDeclaredMethod("getAllUsuarios");
        stubJoinPoint(UsuarioController.class, method, new Object[0]);

        aspect.auditAfterControllerInvocation(joinPoint);

        ArgumentCaptor<RegistroAuditoria> captor = ArgumentCaptor.forClass(RegistroAuditoria.class);
        verify(auditoriaService).save(captor.capture());
        assertThat(captor.getValue().getModulo()).isEqualTo("Usuarios");
    }

    @Test
    @DisplayName("Should expose controllerMethods pointcut method")
    void shouldExposeControllerMethodsPointcut() throws NoSuchMethodException {
        // Coverage for the empty pointcut method.
        aspect.controllerMethods();
    }
}
