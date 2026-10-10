package com.licensis.notaire.adapter.in.web;

import com.licensis.notaire.adapter.in.web.person.PersonController;
import com.licensis.notaire.adapter.in.web.testimony.TestimonyController;
import com.licensis.notaire.adapter.in.web.testimony.TestimonyMovementController;
import com.licensis.notaire.application.usecase.person.PersonService;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import com.licensis.notaire.repository.TestimonyMovementRepository;
import com.licensis.notaire.repository.TestimonyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * Issue #579 (slice 4, testimony and person controllers; Owner decision Run 7): a create or
 * update whose data violates a database constraint is a client error and answers 400 with the
 * standard {@code ErrorResponse} and the {@code GlobalExceptionHandler} message, like the
 * catalogs (#1370). Other failures keep their status (409 on create and on person updates, 500 on
 * testimony and testimony-movement updates) and never echo the cause's text.
 */
@DisplayName("Testimony and person create/update constraint errors answer 400 (issue #579)")
class TestimonyPersonConstraintErrorsTest {

    private static final String CONSTRAINT_MESSAGE = "The submitted data violates a database constraint";
    private static final String SQL_LEAK = "could not execute statement [ERROR: null value in column "
            + "\"fecha_ingreso\" violates not-null constraint\n  Detail: Failing row contains (9, null, secret)]";

    /**
     * Builds a controller from its public constructor, with the given collaborators and a plain
     * mock for any other parameter, so the test does not depend on the exact constructor.
     */
    private static MockMvc mvc(Class<?> type, Map<Class<?>, Object> collaborators) throws Exception {
        Constructor<?> constructor = type.getConstructors()[0];
        Object[] args = new Object[constructor.getParameterCount()];
        for (int i = 0; i < args.length; i++) {
            Class<?> parameter = constructor.getParameterTypes()[i];
            args[i] = collaborators.containsKey(parameter) ? collaborators.get(parameter) : mock(parameter);
        }
        return standaloneSetup(constructor.newInstance(args)).build();
    }

    private static ResultActions send(MockMvc mvc, boolean update, String url, String body) throws Exception {
        var builder = update ? put(url) : post(url);
        return mvc.perform(builder.contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private static void assertConstraint400(ResultActions result) throws Exception {
        String body = result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(CONSTRAINT_MESSAGE))
                .andReturn().getResponse().getContentAsString();
        assertThat(body).doesNotContain("Failing row", "fecha_ingreso", "secret", "could not execute");
    }

    private static MockMvc movements(RuntimeException failure) throws Exception {
        TestimonyMovementRepository repo = mock(TestimonyMovementRepository.class);
        when(repo.findById(anyInt())).thenReturn(Optional.of(new TestimonyMovement()));
        when(repo.save(any())).thenThrow(failure);
        return mvc(TestimonyMovementController.class, Map.of(TestimonyMovementRepository.class, repo));
    }

    private static MockMvc testimonies(RuntimeException failure) throws Exception {
        TestimonyRepository repo = mock(TestimonyRepository.class);
        when(repo.findById(anyInt())).thenReturn(Optional.of(new Testimony()));
        when(repo.save(any())).thenThrow(failure);
        DeedRepository deeds = mock(DeedRepository.class);
        when(deeds.existsById(anyInt())).thenReturn(true);
        return mvc(TestimonyController.class, Map.of(TestimonyRepository.class, repo, DeedRepository.class, deeds));
    }

    private static MockMvc people(RuntimeException failure) throws Exception {
        PersonService service = mock(PersonService.class);
        Person existing = new Person();
        existing.setPersonId(1);
        when(service.findById(1)).thenReturn(Optional.of(existing));
        when(service.save(any())).thenThrow(failure);
        IdentificationTypeRepository types = mock(IdentificationTypeRepository.class);
        when(types.findById(anyInt())).thenReturn(Optional.of(new IdentificationType(1, "DNI")));
        return mvc(PersonController.class, Map.of(PersonService.class, service,
                IdentificationTypeRepository.class, types));
    }

    private static final String PERSON = "{\"firstName\":\"Ana\",\"lastName\":\"Paz\",\"identificationNumber\":\"1\","
            + "\"isClient\":false}";
    private static final String TESTIMONY = "{\"number\":1,\"deed\":{\"idDeed\":1,\"number\":1}}";

    @Test
    @DisplayName("POST /movimiento-testimonio answers 400, not 409, on a constraint violation")
    void movementCreate() throws Exception {
        assertConstraint400(send(movements(new DataIntegrityViolationException(SQL_LEAK)), false,
                "/api/v1/movimiento-testimonio", "{}"));
    }

    @Test
    @DisplayName("PUT /movimiento-testimonio/{id} answers 400, not 500, on a constraint violation")
    void movementUpdate() throws Exception {
        assertConstraint400(send(movements(new DataIntegrityViolationException(SQL_LEAK)), true,
                "/api/v1/movimiento-testimonio/1", "{}"));
    }

    @Test
    @DisplayName("POST /testimonio answers 400, not 409, on a constraint violation")
    void testimonyCreate() throws Exception {
        assertConstraint400(send(testimonies(new DataIntegrityViolationException(SQL_LEAK)), false,
                "/api/v1/testimonio", TESTIMONY));
    }

    @Test
    @DisplayName("PUT /testimonio/{id} answers 400, not 500, on a constraint violation")
    void testimonyUpdate() throws Exception {
        assertConstraint400(send(testimonies(new DataIntegrityViolationException(SQL_LEAK)), true,
                "/api/v1/testimonio/1", TESTIMONY));
    }

    @Test
    @DisplayName("POST /people answers 400, not 409, on a constraint violation")
    void personCreate() throws Exception {
        assertConstraint400(send(people(new DataIntegrityViolationException(SQL_LEAK)), false,
                "/api/v1/people", PERSON));
    }

    @Test
    @DisplayName("PUT /people/{id} answers 400, not 409, on a constraint violation")
    void personUpdate() throws Exception {
        assertConstraint400(send(people(new DataIntegrityViolationException(SQL_LEAK)), true,
                "/api/v1/people/1", PERSON));
    }

    @Test
    @DisplayName("other failures keep their status: 409 on create, 500 on testimony updates, 409 on person updates")
    void otherFailuresKeepTheirStatus() throws Exception {
        RuntimeException boom = new IllegalStateException(SQL_LEAK);
        send(movements(boom), false, "/api/v1/movimiento-testimonio", "{}").andExpect(status().isConflict());
        send(movements(boom), true, "/api/v1/movimiento-testimonio/1", "{}")
                .andExpect(status().isInternalServerError());
        send(testimonies(boom), false, "/api/v1/testimonio", TESTIMONY).andExpect(status().isConflict());
        send(testimonies(boom), true, "/api/v1/testimonio/1", TESTIMONY).andExpect(status().isInternalServerError());
        send(people(boom), false, "/api/v1/people", PERSON).andExpect(status().isConflict());
        send(people(boom), true, "/api/v1/people/1", PERSON).andExpect(status().isConflict());
    }
}
