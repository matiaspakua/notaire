package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.Concepto;
import com.licensis.notaire.negocio.Copia;
import com.licensis.notaire.negocio.Cuaderno;
import com.licensis.notaire.negocio.DocumentoPresentado;
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Inmueble;
import com.licensis.notaire.negocio.MinutaInscripcion;
import com.licensis.notaire.negocio.MovimientoTestimonio;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.negocio.Rol;
import com.licensis.notaire.negocio.Suplencia;
import com.licensis.notaire.negocio.Testimonio;
import com.licensis.notaire.negocio.TipoDeDocumento;
import com.licensis.notaire.negocio.TipoDeFolio;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.negocio.WorkflowDefinition;
import com.licensis.notaire.negocio.WorkflowNode;
import com.licensis.notaire.negocio.WorkflowTransition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.Persistable;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit coverage for {@link Persistable#isNew()} on the 25 entities with a surrogate
 * {@code @GeneratedValue(IDENTITY)} integer key (design.md — Decision 1). No Spring
 * context or database is needed: {@code isNew()} is derived purely from the id field.
 */
class PersistableIdentityEntitiesIsNewTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("entities")
    @DisplayName("isNew() is true for a freshly constructed entity, false once the id is set")
    void isNewReflectsIdentityNotVersion(String name, Supplier<Persistable<Integer>> factory,
                                          Function<Persistable<Integer>, Persistable<Integer>> withIdSet) {
        Persistable<Integer> freshlyConstructed = factory.get();
        assertThat(freshlyConstructed.isNew()).isTrue();

        Persistable<Integer> withId = withIdSet.apply(factory.get());
        assertThat(withId.isNew()).isFalse();
    }

    static Stream<Arguments> entities() {
        return Stream.of(
                identityCase("Concepto", Concepto::new, (Concepto e) -> e.setIdConcepto(1)),
                identityCase("Copia", Copia::new, (Copia e) -> e.setIdCopia(1)),
                identityCase("Cuaderno", Cuaderno::new, (Cuaderno e) -> e.setIdCuaderno(1)),
                identityCase("DocumentoPresentado", DocumentoPresentado::new,
                        (DocumentoPresentado e) -> e.setIdDocumentoPresentado(1)),
                identityCase("Escritura", Escritura::new, (Escritura e) -> e.setIdEscritura(1)),
                identityCase("EstadoDeGestion", EstadoDeGestion::new, (EstadoDeGestion e) -> e.setIdEstadoGestion(1)),
                identityCase("Folio", Folio::new, (Folio e) -> e.setIdFolio(1)),
                identityCase("GestionDeEscritura", GestionDeEscritura::new,
                        (GestionDeEscritura e) -> e.setIdGestion(1)),
                identityCase("Inmueble", Inmueble::new, (Inmueble e) -> e.setIdInmueble(1)),
                identityCase("MinutaInscripcion", MinutaInscripcion::new,
                        (MinutaInscripcion e) -> e.setIdMinutaInscripcion(1)),
                identityCase("MovimientoTestimonio", MovimientoTestimonio::new,
                        (MovimientoTestimonio e) -> e.setIdMovimientoTestimonio(1)),
                identityCase("Persona", Persona::new, (Persona e) -> e.setIdPersona(1)),
                identityCase("Presupuesto", Presupuesto::new, (Presupuesto e) -> e.setIdPresupuesto(1)),
                identityCase("RegistroAuditoria", RegistroAuditoria::new,
                        (RegistroAuditoria e) -> e.setIdRegistroAuditoria(1)),
                identityCase("Rol", Rol::new, (Rol e) -> e.setIdRol(1)),
                identityCase("Suplencia", Suplencia::new, (Suplencia e) -> e.setIdSuplencia(1)),
                identityCase("Testimonio", Testimonio::new, (Testimonio e) -> e.setIdTestimonio(1)),
                identityCase("TipoDeDocumento", TipoDeDocumento::new, (TipoDeDocumento e) -> e.setIdTipoDocumento(1)),
                identityCase("TipoDeFolio", TipoDeFolio::new, (TipoDeFolio e) -> e.setIdTipoFolio(1)),
                identityCase("TipoDeTramite", TipoDeTramite::new, (TipoDeTramite e) -> e.setIdTipoTramite(1)),
                identityCase("TipoIdentificacion", TipoIdentificacion::new,
                        (TipoIdentificacion e) -> e.setIdTipoIdentificacion(1)),
                identityCase("Usuario", Usuario::new, (Usuario e) -> e.setIdUsuario(1)),
                identityCase("WorkflowDefinition", WorkflowDefinition::new, (WorkflowDefinition e) -> e.setId(1)),
                identityCase("WorkflowNode", WorkflowNode::new, (WorkflowNode e) -> e.setId(1)),
                identityCase("WorkflowTransition", WorkflowTransition::new, (WorkflowTransition e) -> e.setId(1))
        );
    }

    @SuppressWarnings("unchecked")
    private static <T extends Persistable<Integer>> Arguments identityCase(
            String name, Supplier<T> factory, java.util.function.Consumer<T> idSetter) {
        Function<Persistable<Integer>, Persistable<Integer>> withIdSet = entity -> {
            idSetter.accept((T) entity);
            return entity;
        };
        return Arguments.of(name, (Supplier<Persistable<Integer>>) factory, withIdSet);
    }
}
