package com.licensis.notaire.adapter.out.persistence.person;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.application.port.out.person.PersonOpsPort.PersonCommand;
import com.licensis.notaire.application.port.out.person.PersonOpsPort.SaveResult;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.repository.IdentificationTypeRepository;

/**
 * Default DNI identification type for new people (issue #1813 / #1361).
 */
@DisplayName("PersonOpsAdapter default identification type")
class PersonOpsAdapterDefaultTypeTest {

    private final PersonService personService = mock(PersonService.class);
    private final IdentificationTypeRepository typeRepo = mock(IdentificationTypeRepository.class);
    private final PersonOpsAdapter adapter = new PersonOpsAdapter(personService, typeRepo);

    @Test
    @DisplayName("create should persist default DNI when catalog has no DNI yet")
    void createDefaultTypeWhenMissing() {
        when(typeRepo.findByName("DNI")).thenReturn(Optional.empty());
        IdentificationType created = new IdentificationType();
        created.setIdIdentificationType(1);
        created.setName("DNI");
        when(typeRepo.save(any(IdentificationType.class))).thenReturn(created);
        when(personService.save(any(Person.class))).thenAnswer(invocation -> {
            Person saved = invocation.getArgument(0);
            saved.setPersonId(42);
            return saved;
        });

        SaveResult result = adapter.create(new PersonCommand(
                "Juan", "Perez", "12345678", true, null, null, null, null, null, null, null, null, null, null,
                null, null, null));

        verify(typeRepo).save(any(IdentificationType.class));
        assertThat(result).isInstanceOf(SaveResult.Created.class);
    }
}
