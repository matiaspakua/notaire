package com.licensis.notaire.unit;

import com.licensis.notaire.adapter.in.web.deed.DeedController;
import com.licensis.notaire.adapter.in.web.person.PersonController;
import com.licensis.notaire.application.usecase.audit.AuditRecordService;
import com.licensis.notaire.audit.AuditAspect;
import com.licensis.notaire.audit.AuditPersonalDataRead;
import com.licensis.notaire.business.AuditRecord;
import com.licensis.notaire.business.User;
import com.licensis.notaire.repository.UserRepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Audit of personal data reads (issue #1635)")
@ExtendWith(MockitoExtension.class)
class AuditPersonalDataReadTest {

    private static final String DNI = "12345678";

    @Mock
    private AuditRecordService auditService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature signature;

    private AuditAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new AuditAspect(auditService, userRepository);
        User admin = new User();
        admin.setIdUser(1);
        admin.setName("admin");
        lenient().when(userRepository.findAll()).thenReturn(List.of(admin));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", "secret", AuthorityUtils.createAuthorityList("ROLE_USER")));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    private void bindRequest(String uri, String query) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setQueryString(query);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private void invoke(Class<?> controller, Method method, Object... args) {
        lenient().when(joinPoint.getTarget()).thenReturn(org.mockito.Mockito.mock(controller));
        lenient().when(joinPoint.getSignature()).thenReturn(signature);
        lenient().when(joinPoint.getArgs()).thenReturn(args);
        lenient().when(signature.getMethod()).thenReturn(method);
        aspect.auditAfterControllerInvocation(joinPoint);
    }

    private AuditRecord savedRecord() {
        ArgumentCaptor<AuditRecord> captor = ArgumentCaptor.forClass(AuditRecord.class);
        verify(auditService, times(1)).save(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("Should write one row naming the person id on a single read")
    void shouldWriteOneRowWithIdOnSingleRead() throws Exception {
        bindRequest("/api/v1/people/7", null);

        invoke(PersonController.class, PersonController.class.getDeclaredMethod("getPersonById", Integer.class), 7);

        assertThat(savedRecord().getOperationDetail()).contains("7").contains("/api/v1/people/7");
    }

    @Test
    @DisplayName("Should write one aggregated row without the search terms on a search")
    void shouldWriteOneRowWithoutSearchTermsOnSearch() throws Exception {
        bindRequest("/api/v1/people/search", "identificationNumber=" + DNI);
        Method search = PersonController.class.getDeclaredMethod("searchPeople", String.class, String.class,
                String.class, Integer.class, Boolean.class);

        invoke(PersonController.class, search, null, null, DNI, null, null);

        assertThat(savedRecord().getOperationDetail()).doesNotContain(DNI).contains("/api/v1/people/search");
    }

    @Test
    @DisplayName("Should write one aggregated row on a list")
    void shouldWriteOneRowOnList() throws Exception {
        bindRequest("/api/v1/people", "page=0&size=20");

        invoke(PersonController.class, PersonController.class.getDeclaredMethod("getAllPeople", Pageable.class),
                Pageable.unpaged());

        assertThat(savedRecord().getOperationDetail()).contains("/api/v1/people");
    }

    @Test
    @DisplayName("Should keep unannotated reads unaudited")
    void shouldKeepUnannotatedReadsUnaudited() throws Exception {
        invoke(DeedController.class, DeedController.class.getDeclaredMethod("getAll", String.class, Pageable.class),
                null, Pageable.unpaged());

        verify(auditService, never()).save(any());
    }

    @Test
    @DisplayName("Should annotate every Person GET handler")
    void shouldAnnotateEveryPersonGetHandler() throws Exception {
        assertThat(PersonController.class.getDeclaredMethod("getPersonById", Integer.class))
                .matches(m -> m.isAnnotationPresent(AuditPersonalDataRead.class));
        assertThat(PersonController.class.getDeclaredMethod("getAllPeople", Pageable.class))
                .matches(m -> m.isAnnotationPresent(AuditPersonalDataRead.class));
        assertThat(PersonController.class.getDeclaredMethod("searchPeople", String.class, String.class, String.class,
                Integer.class, Boolean.class)).matches(m -> m.isAnnotationPresent(AuditPersonalDataRead.class));
    }
}
