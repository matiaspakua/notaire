package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.FoliosCopias;
import com.licensis.notaire.negocio.PlantillaCostoDocumento;
import com.licensis.notaire.negocio.PlantillaPresupuesto;
import com.licensis.notaire.negocio.PlantillaTramite;
import com.licensis.notaire.negocio.TramitesPersonas;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.Persistable;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit coverage for {@link Persistable#isNew()} on the 5 entities with a client-assigned
 * {@code @EmbeddedId} composite key (design.md - Decision 2). Unlike the surrogate-key
 * group, the id here is never null, so {@code isNew()} is backed by a transient flag
 * flipped by the {@code @PostLoad}/{@code @PrePersist} lifecycle callback. That callback
 * is invoked here via reflection to simulate what the persistence provider does on
 * load/persist, without needing a Spring context or database.
 */
class PersistableEmbeddedIdEntitiesIsNewTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("entities")
    @DisplayName("isNew() is true for a freshly constructed entity, false after the persistence callback fires")
    void isNewReflectsLifecycleCallbackNotVersion(String name, Persistable<?> entity, String callbackMethodName)
            throws Exception {
        assertThat(entity.isNew()).isTrue();

        Method callback = entity.getClass().getDeclaredMethod(callbackMethodName);
        callback.setAccessible(true);
        callback.invoke(entity);

        assertThat(entity.isNew()).isFalse();
    }

    static Stream<Arguments> entities() {
        return Stream.of(
                Arguments.of("FoliosCopias", new FoliosCopias(), "markNotNew"),
                Arguments.of("PlantillaCostoDocumento", new PlantillaCostoDocumento(), "markNotNew"),
                Arguments.of("PlantillaPresupuesto", new PlantillaPresupuesto(), "markNotNew"),
                Arguments.of("PlantillaTramite", new PlantillaTramite(), "markNotNew"),
                Arguments.of("TramitesPersonas", new TramitesPersonas(), "markNotNew")
        );
    }
}
