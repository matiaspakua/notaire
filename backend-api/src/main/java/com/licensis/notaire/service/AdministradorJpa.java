package com.licensis.notaire.service;

import com.licensis.notaire.jpa.ConceptoJpaController;
import com.licensis.notaire.jpa.CopiaJpaController;
import com.licensis.notaire.jpa.DocumentoPresentadoJpaController;
import com.licensis.notaire.jpa.EscrituraJpaController;
import com.licensis.notaire.jpa.EstadoDeGestionJpaController;
import com.licensis.notaire.jpa.FolioJpaController;
import com.licensis.notaire.jpa.GestionDeEscrituraJpaController;
import com.licensis.notaire.jpa.HistorialJpaController;
import com.licensis.notaire.jpa.InmuebleJpaController;
import com.licensis.notaire.jpa.ItemJpaController;
import com.licensis.notaire.jpa.MovimientoTestimonioJpaController;
import com.licensis.notaire.jpa.PagoJpaController;
import com.licensis.notaire.jpa.PersonaJpaController;
import com.licensis.notaire.jpa.PlantillaPresupuestoJpaController;
import com.licensis.notaire.jpa.PlantillaTramiteJpaController;
import com.licensis.notaire.jpa.PresupuestoJpaController;
import com.licensis.notaire.jpa.RegistroAuditoriaJpaController;
import com.licensis.notaire.jpa.SuplenciaJpaController;
import com.licensis.notaire.jpa.TestimonioJpaController;
import com.licensis.notaire.jpa.TipoDeDocumentoJpaController;
import com.licensis.notaire.jpa.TipoDeFolioJpaController;
import com.licensis.notaire.jpa.TipoDeTramiteJpaController;
import com.licensis.notaire.jpa.TipoIdentificacionJpaController;
import com.licensis.notaire.jpa.TramiteJpaController;
import com.licensis.notaire.jpa.TramitesPersonasJpaController;
import com.licensis.notaire.jpa.UsuarioJpaController;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class AdministradorJpa {

    public static final int ERROR = -1;
    private static AdministradorJpa instancia = null;
    private static EntityManagerFactory emf = null;

    private static Collection<IPersistenciaJpa> milistaJpas = null;

    public static void setEmf(EntityManagerFactory factory) {
        emf = factory;
        if (instancia != null) {
            cargarListaJpas();
        }
    }

    private AdministradorJpa() {
        if (emf != null) {
            AdministradorJpa.cargarListaJpas();
        }
    }

    public static AdministradorJpa getInstancia() {
        if (AdministradorJpa.instancia == null) {
            instancia = new AdministradorJpa();
        }
        return instancia;
    }

    public static EntityManagerFactory getEmf() {
        return emf;
    }

    public IPersistenciaJpa obtenerJpa(String nombreClase) throws NonexistentJpaException {
        for (Iterator<IPersistenciaJpa> it = milistaJpas.iterator(); it.hasNext();) {
            IPersistenciaJpa iPersistenciaJpa = it.next();
            if (iPersistenciaJpa.getNombreJpa().contains(nombreClase)) {
                return iPersistenciaJpa;
            }
        }
        throw new NonexistentJpaException("El JPA indicado no existe.");

    }

    public static Collection<IPersistenciaJpa> getMilistaJpas() {
        return milistaJpas;
    }

    public static void setMilistaJpas(Collection<IPersistenciaJpa> milistaJpas) {
        AdministradorJpa.milistaJpas = milistaJpas;
    }

    private static void cargarListaJpas() {
        AdministradorJpa.milistaJpas = new ArrayList<>();
        AdministradorJpa.milistaJpas.add(new ConceptoJpaController(null, emf));

        AdministradorJpa.milistaJpas.add(new EstadoDeGestionJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new TipoDeFolioJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(PersonaJpaController.getInstancia());
        AdministradorJpa.milistaJpas.add(new SuplenciaJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(UsuarioJpaController.getInstancia());
        AdministradorJpa.milistaJpas.add(new FolioJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new TipoDeFolioJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new TipoDeDocumentoJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new TipoDeTramiteJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new PlantillaTramiteJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new TipoIdentificacionJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new HistorialJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new TramiteJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new GestionDeEscrituraJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(RegistroAuditoriaJpaController.getInstancia());
        AdministradorJpa.milistaJpas.add(new PresupuestoJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new ItemJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new InmuebleJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new PlantillaPresupuestoJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new TramitesPersonasJpaController(emf));
        AdministradorJpa.milistaJpas.add(new PagoJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new EscrituraJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new CopiaJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new TestimonioJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new MovimientoTestimonioJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new DocumentoPresentadoJpaController(null, emf));
    }
}
