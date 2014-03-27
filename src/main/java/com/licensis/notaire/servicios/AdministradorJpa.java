/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.servicios;

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
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Clase que se encarga de instanciar y administrar todos los JPA. Mantiene una lista de todos los
 * JPA instanciados, haciendo que el proceso de carga de los mismos (que inicialmente es muy lento)
 * solo tenga que realizarse una unica vez. Ademas mantiene una lista de todos los JPA, y los
 * metodos necesarios para poder recuperarlos.
 *
 * @author matias
 */
public class AdministradorJpa
{

    /**
     * Constante que puede ser utilizada para indicar que una entidad no se pudo persistir, obtener
     * de la persistencia.
     */
    public static final int ERROR = -1;
    private static AdministradorJpa instancia = null;
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("notairePU");
    private static Collection<IPersistenciaJpa> milistaJpas = null;

    /**
     * Constructor de {@link AdministradorJpa}. Internamente se inicializa y carga la lista de JPA.
     */
    private AdministradorJpa() {
        //  aqui ocurre todo el proceso de inicializacion de los JPA
        AdministradorJpa.cargarListaJpas();

    }

    /**
     * Metodo estatico para obtener la instancia actual de AdministradorJpa (simgleton)
     *
     * @return La instancia actual de AdministradorJpa.
     */
    public static AdministradorJpa getInstancia() {
        if (instancia == null)
        {
            instancia = new AdministradorJpa();
        }
        return instancia;
    }

    /**
     * Retorna la instancia de EntityManagerFactory.
     *
     * @return La instancia actual del EMF.
     */
    public static EntityManagerFactory getEmf() {
        return emf;
    }

    /**
     * Permite obtener un JPA especificando el nombre de mismo a traves del metodo del objeto
     * Class.getName().
     *
     * @param nombreClase El nombre del JPA a buscar. El nombre proviene del objeto Class.getName().
     * @return miJpa Un JPA (IPersistencia) si se encontro coincidencia.
     * @throws NonexistentJpaException Esta excepcion es lanzada si el JPA indicado no existe en la
     * lista de JPA's.
     */
    public IPersistenciaJpa obtenerJpa(String nombreClase) throws NonexistentJpaException {
        for (Iterator<IPersistenciaJpa> it = milistaJpas.iterator(); it.hasNext();)
        {
            IPersistenciaJpa iPersistenciaJpa = it.next();
            if (iPersistenciaJpa.getNombreJpa().contains(nombreClase))
            {
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

    /**
     * Metodo estatico que carga las instancia de todos los JPA una unica vez y los guarda en una
     * lista para poder ser recuperados posteriormente.
     */
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
