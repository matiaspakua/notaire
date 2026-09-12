package com.licensis.notaire.unit;

import com.licensis.notaire.config.DataInitializer;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.business.User;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import com.licensis.notaire.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataInitializer — usuario admin por defecto")
class DataInitializerTest {

    private static final String BCRYPT_ADMIN = "$2a$12$stub.bcrypt.hash.for.admin.password";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PersonRepository personRepository;
    @Mock
    private IdentificationTypeRepository identificationTypeRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        dataInitializer = new DataInitializer(
                userRepository, personRepository, identificationTypeRepository, passwordEncoder);
        ReflectionTestUtils.setField(dataInitializer, "adminUsername", "admin");
        ReflectionTestUtils.setField(dataInitializer, "adminPassword", "admin");
    }

    @Test
    @DisplayName("Should not modify an already-existing admin user (issue #553)")
    void shouldNotModifyExistingAdminUser() {
        User existing = new User();
        existing.setName("admin");
        existing.setPassword("hash-ya-rotado-por-el-operador");
        existing.setStatus(false);
        when(userRepository.findByName("admin")).thenReturn(Optional.of(existing));

        dataInitializer.run(null);

        verify(userRepository, never()).save(any());
        verify(personRepository, never()).save(any());
        assertThat(existing.getPassword()).isEqualTo("hash-ya-rotado-por-el-operador");
        assertThat(existing.getStatus()).isFalse();
    }

    @Test
    @DisplayName("Should create admin user with BCrypt password when none exists")
    void shouldCreateAdminUserWhenNoneExists() {
        when(userRepository.findByName("admin")).thenReturn(Optional.empty());
        IdentificationType type = new IdentificationType();
        type.setName("DNI");
        when(identificationTypeRepository.findAll()).thenReturn(List.of(type));
        when(passwordEncoder.encode("admin")).thenReturn(BCRYPT_ADMIN);

        dataInitializer.run(null);

        verify(personRepository).save(any(Person.class));
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User created = captor.getValue();
        assertThat(created.getName()).isEqualTo("admin");
        assertThat(created.getPassword()).isEqualTo(BCRYPT_ADMIN);
        assertThat(created.getStatus()).isTrue();
        assertThat(created.getFkIdPerson()).isNotNull();
    }

    @Test
    @DisplayName("Should seed the configured username/password, not hardcoded literals (issue #651)")
    void shouldUseConfiguredAdminCredentials() {
        ReflectionTestUtils.setField(dataInitializer, "adminUsername", "custom-admin");
        ReflectionTestUtils.setField(dataInitializer, "adminPassword", "custom-pass");
        when(userRepository.findByName("custom-admin")).thenReturn(Optional.empty());
        IdentificationType type = new IdentificationType();
        type.setName("DNI");
        when(identificationTypeRepository.findAll()).thenReturn(List.of(type));
        when(passwordEncoder.encode("custom-pass")).thenReturn(BCRYPT_ADMIN);

        dataInitializer.run(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("custom-admin");
        assertThat(captor.getValue().getPassword()).isEqualTo(BCRYPT_ADMIN);
    }

    @Test
    @DisplayName("Should not propagate exceptions when persistence fails")
    void shouldNotPropagateExceptionsWhenPersistenceFails() {
        when(userRepository.findByName("admin")).thenThrow(new RuntimeException("DB down"));

        // Must not throw — startup should never be blocked by the seeder.
        dataInitializer.run(null);

        verify(userRepository, never()).save(any());
    }
}
