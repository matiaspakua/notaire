/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.ControllerNegocio;
import com.licensis.notaire.negocio.Copia;
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Person;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.Suplencia;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.negocio.TramitesPersonas;
import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.service.AdministradorJpa;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.UserTransaction;

/**
 *
 * @author matias
 */
public class PersonJpaController implements Serializable, IPersistenciaJpa {

    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;
    private static PersonJpaController instancia = null;

    private PersonJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public int create(Person persona) {
        int oid = 0;
        if (persona.getTramitesPersonasList() == null) {
            persona.setTramitesPersonasList(new ArrayList<TramitesPersonas>());
        }
        if (persona.getPresupuestoList() == null) {
            persona.setPresupuestoList(new ArrayList<Presupuesto>());
        }
        if (persona.getGestionDeEscrituraList() == null) {
            persona.setGestionDeEscrituraList(new ArrayList<GestionDeEscritura>());
        }
        if (persona.getFolioList() == null) {
            persona.setFolioList(new ArrayList<Folio>());
        }
        if (persona.getSuplenciaList() == null) {
            persona.setSuplenciaList(new ArrayList<Suplencia>());
        }
        if (persona.getSuplenciaList1() == null) {
            persona.setSuplenciaList1(new ArrayList<Suplencia>());
        }
        if (persona.getCopiaList() == null) {
            persona.setCopiaList(new ArrayList<Copia>());
        }
        if (persona.getUsuariosList() == null) {
            persona.setUsuariosList(new ArrayList<Usuario>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            TipoIdentificacion fkIdTipoIdentificacion = persona.getFkIdIdentificationType();
            if (fkIdTipoIdentificacion != null) {
                fkIdTipoIdentificacion = em.getReference(fkIdTipoIdentificacion.getClass(),
                        fkIdTipoIdentificacion.getIdTipoIdentificacion());
                persona.setFkIdIdentificationType(fkIdTipoIdentificacion);
            }
            List<TramitesPersonas> attachedTramitesPersonasList = new ArrayList<TramitesPersonas>();
            for (TramitesPersonas tramitesPersonasListTramitesPersonasToAttach : persona.getTramitesPersonasList()) {
                tramitesPersonasListTramitesPersonasToAttach = em.getReference(
                        tramitesPersonasListTramitesPersonasToAttach.getClass(),
                        tramitesPersonasListTramitesPersonasToAttach.getTramitesPersonasPK());
                attachedTramitesPersonasList.add(tramitesPersonasListTramitesPersonasToAttach);
            }
            persona.setTramitesPersonasList(attachedTramitesPersonasList);
            List<Presupuesto> attachedPresupuestoList = new ArrayList<Presupuesto>();
            for (Presupuesto presupuestoListPresupuestoToAttach : persona.getPresupuestoList()) {
                presupuestoListPresupuestoToAttach = em.getReference(presupuestoListPresupuestoToAttach.getClass(),
                        presupuestoListPresupuestoToAttach.getIdPresupuesto());
                attachedPresupuestoList.add(presupuestoListPresupuestoToAttach);
            }
            persona.setPresupuestoList(attachedPresupuestoList);
            List<GestionDeEscritura> attachedGestionesDeEscriturasList = new ArrayList<GestionDeEscritura>();
            for (GestionDeEscritura gestionesDeEscriturasListGestionesDeEscriturasToAttach : persona
                    .getGestionDeEscrituraList()) {
                gestionesDeEscriturasListGestionesDeEscriturasToAttach = em.getReference(
                        gestionesDeEscriturasListGestionesDeEscriturasToAttach.getClass(),
                        gestionesDeEscriturasListGestionesDeEscriturasToAttach.getIdGestion());
                attachedGestionesDeEscriturasList.add(gestionesDeEscriturasListGestionesDeEscriturasToAttach);
            }
            persona.setGestionDeEscrituraList(attachedGestionesDeEscriturasList);
            List<Folio> attachedFolioList = new ArrayList<Folio>();
            for (Folio folioListFolioToAttach : persona.getFolioList()) {
                folioListFolioToAttach = em.getReference(folioListFolioToAttach.getClass(),
                        folioListFolioToAttach.getIdFolio());
                attachedFolioList.add(folioListFolioToAttach);
            }
            persona.setFolioList(attachedFolioList);
            List<Suplencia> attachedSuplenciaList = new ArrayList<Suplencia>();
            for (Suplencia suplenciaListSuplenciaToAttach : persona.getSuplenciaList()) {
                suplenciaListSuplenciaToAttach = em.getReference(suplenciaListSuplenciaToAttach.getClass(),
                        suplenciaListSuplenciaToAttach.getIdSuplencia());
                attachedSuplenciaList.add(suplenciaListSuplenciaToAttach);
            }
            persona.setSuplenciaList(attachedSuplenciaList);
            List<Suplencia> attachedSuplenciaList1 = new ArrayList<Suplencia>();
            for (Suplencia suplenciaList1SuplenciaToAttach : persona.getSuplenciaList1()) {
                suplenciaList1SuplenciaToAttach = em.getReference(suplenciaList1SuplenciaToAttach.getClass(),
                        suplenciaList1SuplenciaToAttach.getIdSuplencia());
                attachedSuplenciaList1.add(suplenciaList1SuplenciaToAttach);
            }
            persona.setSuplenciaList1(attachedSuplenciaList1);
            List<Copia> attachedCopiaList = new ArrayList<Copia>();
            for (Copia copiaListCopiaToAttach : persona.getCopiaList()) {
                copiaListCopiaToAttach = em.getReference(copiaListCopiaToAttach.getClass(),
                        copiaListCopiaToAttach.getIdCopia());
                attachedCopiaList.add(copiaListCopiaToAttach);
            }
            persona.setCopiaList(attachedCopiaList);
            List<Usuario> attachedUsuarioList = new ArrayList<Usuario>();
            for (Usuario usuarioListUsuarioToAttach : persona.getUsuariosList()) {
                usuarioListUsuarioToAttach = em.getReference(usuarioListUsuarioToAttach.getClass(),
                        usuarioListUsuarioToAttach.getIdUsuario());
                attachedUsuarioList.add(usuarioListUsuarioToAttach);
            }
            persona.setUsuariosList(attachedUsuarioList);
            em.persist(persona);
            if (fkIdTipoIdentificacion != null) {
                fkIdTipoIdentificacion.getPersonaList().add(persona);
                fkIdTipoIdentificacion = em.merge(fkIdTipoIdentificacion);
            }
            for (TramitesPersonas tramitesPersonasListTramitesPersonas : persona.getTramitesPersonasList()) {
                Person oldPersonaOfTramitesPersonasListTramitesPersonas = tramitesPersonasListTramitesPersonas
                        .getPersona();
                tramitesPersonasListTramitesPersonas.setPersona(persona);
                tramitesPersonasListTramitesPersonas = em.merge(tramitesPersonasListTramitesPersonas);
                if (oldPersonaOfTramitesPersonasListTramitesPersonas != null) {
                    oldPersonaOfTramitesPersonasListTramitesPersonas.getTramitesPersonasList()
                            .remove(tramitesPersonasListTramitesPersonas);
                    oldPersonaOfTramitesPersonasListTramitesPersonas = em
                            .merge(oldPersonaOfTramitesPersonasListTramitesPersonas);
                }
            }
            for (Presupuesto presupuestoListPresupuesto : persona.getPresupuestoList()) {
                Person oldFkIdPersonaOfPresupuestoListPresupuesto = presupuestoListPresupuesto.getFkIdPersona();
                presupuestoListPresupuesto.setFkIdPersona(persona);
                presupuestoListPresupuesto = em.merge(presupuestoListPresupuesto);
                if (oldFkIdPersonaOfPresupuestoListPresupuesto != null) {
                    oldFkIdPersonaOfPresupuestoListPresupuesto.getPresupuestoList().remove(presupuestoListPresupuesto);
                    oldFkIdPersonaOfPresupuestoListPresupuesto = em.merge(oldFkIdPersonaOfPresupuestoListPresupuesto);
                }
            }
            for (GestionDeEscritura gestionesDeEscriturasListGestionesDeEscrituras : persona
                    .getGestionDeEscrituraList()) {
                Person oldFkIdPersonaEscribanoOfGestionesDeEscriturasListGestionesDeEscrituras = gestionesDeEscriturasListGestionesDeEscrituras
                        .getFkIdPersonaEscribano();
                gestionesDeEscriturasListGestionesDeEscrituras.setFkIdPersonaEscribano(persona);
                gestionesDeEscriturasListGestionesDeEscrituras = em
                        .merge(gestionesDeEscriturasListGestionesDeEscrituras);
                if (oldFkIdPersonaEscribanoOfGestionesDeEscriturasListGestionesDeEscrituras != null) {
                    oldFkIdPersonaEscribanoOfGestionesDeEscriturasListGestionesDeEscrituras.getGestionDeEscrituraList()
                            .remove(gestionesDeEscriturasListGestionesDeEscrituras);
                    oldFkIdPersonaEscribanoOfGestionesDeEscriturasListGestionesDeEscrituras = em
                            .merge(oldFkIdPersonaEscribanoOfGestionesDeEscriturasListGestionesDeEscrituras);
                }
            }
            for (Folio folioListFolio : persona.getFolioList()) {
                Person oldFkIdPersonaEscribanoOfFolioListFolio = folioListFolio.getFkIdPersonaEscribano();
                folioListFolio.setFkIdPersonaEscribano(persona);
                folioListFolio = em.merge(folioListFolio);
                if (oldFkIdPersonaEscribanoOfFolioListFolio != null) {
                    oldFkIdPersonaEscribanoOfFolioListFolio.getFolioList().remove(folioListFolio);
                    oldFkIdPersonaEscribanoOfFolioListFolio = em.merge(oldFkIdPersonaEscribanoOfFolioListFolio);
                }
            }
            for (Suplencia suplenciaListSuplencia : persona.getSuplenciaList()) {
                Person oldFkIdSuplenteOfSuplenciaListSuplencia = suplenciaListSuplencia.getFkIdSuplente();
                suplenciaListSuplencia.setFkIdSuplente(persona);
                suplenciaListSuplencia = em.merge(suplenciaListSuplencia);
                if (oldFkIdSuplenteOfSuplenciaListSuplencia != null) {
                    oldFkIdSuplenteOfSuplenciaListSuplencia.getSuplenciaList().remove(suplenciaListSuplencia);
                    oldFkIdSuplenteOfSuplenciaListSuplencia = em.merge(oldFkIdSuplenteOfSuplenciaListSuplencia);
                }
            }
            for (Suplencia suplenciaList1Suplencia : persona.getSuplenciaList1()) {
                Person oldFkIdSuplantadoOfSuplenciaList1Suplencia = suplenciaList1Suplencia.getFkIdSuplantado();
                suplenciaList1Suplencia.setFkIdSuplantado(persona);
                suplenciaList1Suplencia = em.merge(suplenciaList1Suplencia);
                if (oldFkIdSuplantadoOfSuplenciaList1Suplencia != null) {
                    oldFkIdSuplantadoOfSuplenciaList1Suplencia.getSuplenciaList1().remove(suplenciaList1Suplencia);
                    oldFkIdSuplantadoOfSuplenciaList1Suplencia = em.merge(oldFkIdSuplantadoOfSuplenciaList1Suplencia);
                }
            }
            for (Copia copiaListCopia : persona.getCopiaList()) {
                Person oldFkIdPersonaOfCopiaListCopia = copiaListCopia.getFkIdPersona();
                copiaListCopia.setFkIdPersona(persona);
                copiaListCopia = em.merge(copiaListCopia);
                if (oldFkIdPersonaOfCopiaListCopia != null) {
                    oldFkIdPersonaOfCopiaListCopia.getCopiaList().remove(copiaListCopia);
                    oldFkIdPersonaOfCopiaListCopia = em.merge(oldFkIdPersonaOfCopiaListCopia);
                }
            }
            for (Usuario usuarioListUsuario : persona.getUsuariosList()) {
                Person oldFkIdPersonaOfUsuarioListUsuario = usuarioListUsuario.getFkIdPersona();
                usuarioListUsuario.setFkIdPersona(persona);
                usuarioListUsuario = em.merge(usuarioListUsuario);
                if (oldFkIdPersonaOfUsuarioListUsuario != null) {
                    oldFkIdPersonaOfUsuarioListUsuario.getUsuariosList().remove(usuarioListUsuario);
                    oldFkIdPersonaOfUsuarioListUsuario = em.merge(oldFkIdPersonaOfUsuarioListUsuario);
                }
            }
            em.getTransaction().commit();
            oid = persona.getPersonId();
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return oid;
    }

    public void edit(Person persona) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Person persistentPersona = em.find(Person.class, persona.getPersonId());
            TipoIdentificacion fkIdTipoIdentificacionOld = persistentPersona.getFkIdIdentificationType();
            TipoIdentificacion fkIdTipoIdentificacionNew = persona.getFkIdIdentificationType();
            List<TramitesPersonas> tramitesPersonasListOld = persistentPersona.getTramitesPersonasList();
            List<TramitesPersonas> tramitesPersonasListNew = persona.getTramitesPersonasList();
            List<Presupuesto> presupuestoListOld = persistentPersona.getPresupuestoList();
            List<Presupuesto> presupuestoListNew = persona.getPresupuestoList();
            List<GestionDeEscritura> gestionesDeEscriturasListOld = persistentPersona.getGestionDeEscrituraList();
            List<GestionDeEscritura> gestionesDeEscriturasListNew = persona.getGestionDeEscrituraList();
            List<Folio> folioListOld = persistentPersona.getFolioList();
            List<Folio> folioListNew = persona.getFolioList();
            List<Suplencia> suplenciaListOld = persistentPersona.getSuplenciaList();
            List<Suplencia> suplenciaListNew = persona.getSuplenciaList();
            List<Suplencia> suplenciaList1Old = persistentPersona.getSuplenciaList1();
            List<Suplencia> suplenciaList1New = persona.getSuplenciaList1();
            List<Copia> copiaListOld = persistentPersona.getCopiaList();
            List<Copia> copiaListNew = persona.getCopiaList();
            List<Usuario> usuarioListOld = persistentPersona.getUsuariosList();
            List<Usuario> usuarioListNew = persona.getUsuariosList();
            List<String> illegalOrphanMessages = null;
            for (TramitesPersonas tramitesPersonasListOldTramitesPersonas : tramitesPersonasListOld) {
                if (!tramitesPersonasListNew.contains(tramitesPersonasListOldTramitesPersonas)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TramitesPersonas "
                            + tramitesPersonasListOldTramitesPersonas + " since its persona field is not nullable.");
                }
            }
            for (Presupuesto presupuestoListOldPresupuesto : presupuestoListOld) {
                if (!presupuestoListNew.contains(presupuestoListOldPresupuesto)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Presupuesto " + presupuestoListOldPresupuesto
                            + " since its fkIdPersona field is not nullable.");
                }
            }
            for (GestionDeEscritura gestionesDeEscriturasListOldGestionesDeEscrituras : gestionesDeEscriturasListOld) {
                if (!gestionesDeEscriturasListNew.contains(gestionesDeEscriturasListOldGestionesDeEscrituras)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add(
                            "You must retain GestionesDeEscrituras " + gestionesDeEscriturasListOldGestionesDeEscrituras
                                    + " since its fkIdPersonaEscribano field is not nullable.");
                }
            }
            for (Folio folioListOldFolio : folioListOld) {
                if (!folioListNew.contains(folioListOldFolio)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Folio " + folioListOldFolio
                            + " since its fkIdPersonaEscribano field is not nullable.");
                }
            }
            for (Suplencia suplenciaListOldSuplencia : suplenciaListOld) {
                if (!suplenciaListNew.contains(suplenciaListOldSuplencia)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Suplencia " + suplenciaListOldSuplencia
                            + " since its fkIdSuplente field is not nullable.");
                }
            }
            for (Suplencia suplenciaList1OldSuplencia : suplenciaList1Old) {
                if (!suplenciaList1New.contains(suplenciaList1OldSuplencia)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Suplencia " + suplenciaList1OldSuplencia
                            + " since its fkIdSuplantado field is not nullable.");
                }
            }
            for (Copia copiaListOldCopia : copiaListOld) {
                if (!copiaListNew.contains(copiaListOldCopia)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Copia " + copiaListOldCopia
                            + " since its fkIdPersona field is not nullable.");
                }
            }
            for (Usuario usuarioListOldUsuario : usuarioListOld) {
                if (!usuarioListNew.contains(usuarioListOldUsuario)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Usuario " + usuarioListOldUsuario
                            + " since its fkIdPersona field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (fkIdTipoIdentificacionNew != null) {
                fkIdTipoIdentificacionNew = em.getReference(fkIdTipoIdentificacionNew.getClass(),
                        fkIdTipoIdentificacionNew.getIdTipoIdentificacion());
                persona.setFkIdIdentificationType(fkIdTipoIdentificacionNew);
            }
            List<TramitesPersonas> attachedTramitesPersonasListNew = new ArrayList<TramitesPersonas>();
            for (TramitesPersonas tramitesPersonasListNewTramitesPersonasToAttach : tramitesPersonasListNew) {
                tramitesPersonasListNewTramitesPersonasToAttach = em.getReference(
                        tramitesPersonasListNewTramitesPersonasToAttach.getClass(),
                        tramitesPersonasListNewTramitesPersonasToAttach.getTramitesPersonasPK());
                attachedTramitesPersonasListNew.add(tramitesPersonasListNewTramitesPersonasToAttach);
            }
            tramitesPersonasListNew = attachedTramitesPersonasListNew;
            persona.setTramitesPersonasList(tramitesPersonasListNew);
            List<Presupuesto> attachedPresupuestoListNew = new ArrayList<Presupuesto>();
            for (Presupuesto presupuestoListNewPresupuestoToAttach : presupuestoListNew) {
                presupuestoListNewPresupuestoToAttach = em.getReference(
                        presupuestoListNewPresupuestoToAttach.getClass(),
                        presupuestoListNewPresupuestoToAttach.getIdPresupuesto());
                attachedPresupuestoListNew.add(presupuestoListNewPresupuestoToAttach);
            }
            presupuestoListNew = attachedPresupuestoListNew;
            persona.setPresupuestoList(presupuestoListNew);
            List<GestionDeEscritura> attachedGestionesDeEscriturasListNew = new ArrayList<GestionDeEscritura>();
            for (GestionDeEscritura gestionesDeEscriturasListNewGestionesDeEscriturasToAttach : gestionesDeEscriturasListNew) {
                gestionesDeEscriturasListNewGestionesDeEscriturasToAttach = em.getReference(
                        gestionesDeEscriturasListNewGestionesDeEscriturasToAttach.getClass(),
                        gestionesDeEscriturasListNewGestionesDeEscriturasToAttach.getIdGestion());
                attachedGestionesDeEscriturasListNew.add(gestionesDeEscriturasListNewGestionesDeEscriturasToAttach);
            }
            gestionesDeEscriturasListNew = attachedGestionesDeEscriturasListNew;
            persona.setGestionDeEscrituraList(gestionesDeEscriturasListNew);
            List<Folio> attachedFolioListNew = new ArrayList<Folio>();
            for (Folio folioListNewFolioToAttach : folioListNew) {
                folioListNewFolioToAttach = em.getReference(folioListNewFolioToAttach.getClass(),
                        folioListNewFolioToAttach.getIdFolio());
                attachedFolioListNew.add(folioListNewFolioToAttach);
            }
            folioListNew = attachedFolioListNew;
            persona.setFolioList(folioListNew);
            List<Suplencia> attachedSuplenciaListNew = new ArrayList<Suplencia>();
            for (Suplencia suplenciaListNewSuplenciaToAttach : suplenciaListNew) {
                suplenciaListNewSuplenciaToAttach = em.getReference(suplenciaListNewSuplenciaToAttach.getClass(),
                        suplenciaListNewSuplenciaToAttach.getIdSuplencia());
                attachedSuplenciaListNew.add(suplenciaListNewSuplenciaToAttach);
            }
            suplenciaListNew = attachedSuplenciaListNew;
            persona.setSuplenciaList(suplenciaListNew);
            List<Suplencia> attachedSuplenciaList1New = new ArrayList<Suplencia>();
            for (Suplencia suplenciaList1NewSuplenciaToAttach : suplenciaList1New) {
                suplenciaList1NewSuplenciaToAttach = em.getReference(suplenciaList1NewSuplenciaToAttach.getClass(),
                        suplenciaList1NewSuplenciaToAttach.getIdSuplencia());
                attachedSuplenciaList1New.add(suplenciaList1NewSuplenciaToAttach);
            }
            suplenciaList1New = attachedSuplenciaList1New;
            persona.setSuplenciaList1(suplenciaList1New);
            List<Copia> attachedCopiaListNew = new ArrayList<Copia>();
            for (Copia copiaListNewCopiaToAttach : copiaListNew) {
                copiaListNewCopiaToAttach = em.getReference(copiaListNewCopiaToAttach.getClass(),
                        copiaListNewCopiaToAttach.getIdCopia());
                attachedCopiaListNew.add(copiaListNewCopiaToAttach);
            }
            copiaListNew = attachedCopiaListNew;
            persona.setCopiaList(copiaListNew);
            List<Usuario> attachedUsuarioListNew = new ArrayList<Usuario>();
            for (Usuario usuarioListNewUsuarioToAttach : usuarioListNew) {
                usuarioListNewUsuarioToAttach = em.getReference(usuarioListNewUsuarioToAttach.getClass(),
                        usuarioListNewUsuarioToAttach.getIdUsuario());
                attachedUsuarioListNew.add(usuarioListNewUsuarioToAttach);
            }
            usuarioListNew = attachedUsuarioListNew;
            persona.setUsuariosList(usuarioListNew);
            persona = em.merge(persona);
            if (fkIdTipoIdentificacionOld != null && !fkIdTipoIdentificacionOld.equals(fkIdTipoIdentificacionNew)) {
                fkIdTipoIdentificacionOld.getPersonaList().remove(persona);
                fkIdTipoIdentificacionOld = em.merge(fkIdTipoIdentificacionOld);
            }
            if (fkIdTipoIdentificacionNew != null && !fkIdTipoIdentificacionNew.equals(fkIdTipoIdentificacionOld)) {
                fkIdTipoIdentificacionNew.getPersonaList().add(persona);
                fkIdTipoIdentificacionNew = em.merge(fkIdTipoIdentificacionNew);
            }
            for (TramitesPersonas tramitesPersonasListNewTramitesPersonas : tramitesPersonasListNew) {
                if (!tramitesPersonasListOld.contains(tramitesPersonasListNewTramitesPersonas)) {
                    Person oldPersonaOfTramitesPersonasListNewTramitesPersonas = tramitesPersonasListNewTramitesPersonas
                            .getPersona();
                    tramitesPersonasListNewTramitesPersonas.setPersona(persona);
                    tramitesPersonasListNewTramitesPersonas = em.merge(tramitesPersonasListNewTramitesPersonas);
                    if (oldPersonaOfTramitesPersonasListNewTramitesPersonas != null
                            && !oldPersonaOfTramitesPersonasListNewTramitesPersonas.equals(persona)) {
                        oldPersonaOfTramitesPersonasListNewTramitesPersonas.getTramitesPersonasList()
                                .remove(tramitesPersonasListNewTramitesPersonas);
                        oldPersonaOfTramitesPersonasListNewTramitesPersonas = em
                                .merge(oldPersonaOfTramitesPersonasListNewTramitesPersonas);
                    }
                }
            }
            for (Presupuesto presupuestoListNewPresupuesto : presupuestoListNew) {
                if (!presupuestoListOld.contains(presupuestoListNewPresupuesto)) {
                    Person oldFkIdPersonaOfPresupuestoListNewPresupuesto = presupuestoListNewPresupuesto
                            .getFkIdPersona();
                    presupuestoListNewPresupuesto.setFkIdPersona(persona);
                    presupuestoListNewPresupuesto = em.merge(presupuestoListNewPresupuesto);
                    if (oldFkIdPersonaOfPresupuestoListNewPresupuesto != null
                            && !oldFkIdPersonaOfPresupuestoListNewPresupuesto.equals(persona)) {
                        oldFkIdPersonaOfPresupuestoListNewPresupuesto.getPresupuestoList()
                                .remove(presupuestoListNewPresupuesto);
                        oldFkIdPersonaOfPresupuestoListNewPresupuesto = em
                                .merge(oldFkIdPersonaOfPresupuestoListNewPresupuesto);
                    }
                }
            }
            for (GestionDeEscritura gestionesDeEscriturasListNewGestionesDeEscrituras : gestionesDeEscriturasListNew) {
                if (!gestionesDeEscriturasListOld.contains(gestionesDeEscriturasListNewGestionesDeEscrituras)) {
                    Person oldFkIdPersonaEscribanoOfGestionesDeEscriturasListNewGestionesDeEscrituras = gestionesDeEscriturasListNewGestionesDeEscrituras
                            .getFkIdPersonaEscribano();
                    gestionesDeEscriturasListNewGestionesDeEscrituras.setFkIdPersonaEscribano(persona);
                    gestionesDeEscriturasListNewGestionesDeEscrituras = em
                            .merge(gestionesDeEscriturasListNewGestionesDeEscrituras);
                    if (oldFkIdPersonaEscribanoOfGestionesDeEscriturasListNewGestionesDeEscrituras != null
                            && !oldFkIdPersonaEscribanoOfGestionesDeEscriturasListNewGestionesDeEscrituras
                                    .equals(persona)) {
                        oldFkIdPersonaEscribanoOfGestionesDeEscriturasListNewGestionesDeEscrituras
                                .getGestionDeEscrituraList().remove(gestionesDeEscriturasListNewGestionesDeEscrituras);
                        oldFkIdPersonaEscribanoOfGestionesDeEscriturasListNewGestionesDeEscrituras = em
                                .merge(oldFkIdPersonaEscribanoOfGestionesDeEscriturasListNewGestionesDeEscrituras);
                    }
                }
            }
            for (Folio folioListNewFolio : folioListNew) {
                if (!folioListOld.contains(folioListNewFolio)) {
                    Person oldFkIdPersonaEscribanoOfFolioListNewFolio = folioListNewFolio.getFkIdPersonaEscribano();
                    folioListNewFolio.setFkIdPersonaEscribano(persona);
                    folioListNewFolio = em.merge(folioListNewFolio);
                    if (oldFkIdPersonaEscribanoOfFolioListNewFolio != null
                            && !oldFkIdPersonaEscribanoOfFolioListNewFolio.equals(persona)) {
                        oldFkIdPersonaEscribanoOfFolioListNewFolio.getFolioList().remove(folioListNewFolio);
                        oldFkIdPersonaEscribanoOfFolioListNewFolio = em
                                .merge(oldFkIdPersonaEscribanoOfFolioListNewFolio);
                    }
                }
            }
            for (Suplencia suplenciaListNewSuplencia : suplenciaListNew) {
                if (!suplenciaListOld.contains(suplenciaListNewSuplencia)) {
                    Person oldFkIdSuplenteOfSuplenciaListNewSuplencia = suplenciaListNewSuplencia.getFkIdSuplente();
                    suplenciaListNewSuplencia.setFkIdSuplente(persona);
                    suplenciaListNewSuplencia = em.merge(suplenciaListNewSuplencia);
                    if (oldFkIdSuplenteOfSuplenciaListNewSuplencia != null
                            && !oldFkIdSuplenteOfSuplenciaListNewSuplencia.equals(persona)) {
                        oldFkIdSuplenteOfSuplenciaListNewSuplencia.getSuplenciaList().remove(suplenciaListNewSuplencia);
                        oldFkIdSuplenteOfSuplenciaListNewSuplencia = em
                                .merge(oldFkIdSuplenteOfSuplenciaListNewSuplencia);
                    }
                }
            }
            for (Suplencia suplenciaList1NewSuplencia : suplenciaList1New) {
                if (!suplenciaList1Old.contains(suplenciaList1NewSuplencia)) {
                    Person oldFkIdSuplantadoOfSuplenciaList1NewSuplencia = suplenciaList1NewSuplencia
                            .getFkIdSuplantado();
                    suplenciaList1NewSuplencia.setFkIdSuplantado(persona);
                    suplenciaList1NewSuplencia = em.merge(suplenciaList1NewSuplencia);
                    if (oldFkIdSuplantadoOfSuplenciaList1NewSuplencia != null
                            && !oldFkIdSuplantadoOfSuplenciaList1NewSuplencia.equals(persona)) {
                        oldFkIdSuplantadoOfSuplenciaList1NewSuplencia.getSuplenciaList1()
                                .remove(suplenciaList1NewSuplencia);
                        oldFkIdSuplantadoOfSuplenciaList1NewSuplencia = em
                                .merge(oldFkIdSuplantadoOfSuplenciaList1NewSuplencia);
                    }
                }
            }
            for (Copia copiaListNewCopia : copiaListNew) {
                if (!copiaListOld.contains(copiaListNewCopia)) {
                    Person oldFkIdPersonaOfCopiaListNewCopia = copiaListNewCopia.getFkIdPersona();
                    copiaListNewCopia.setFkIdPersona(persona);
                    copiaListNewCopia = em.merge(copiaListNewCopia);
                    if (oldFkIdPersonaOfCopiaListNewCopia != null
                            && !oldFkIdPersonaOfCopiaListNewCopia.equals(persona)) {
                        oldFkIdPersonaOfCopiaListNewCopia.getCopiaList().remove(copiaListNewCopia);
                        oldFkIdPersonaOfCopiaListNewCopia = em.merge(oldFkIdPersonaOfCopiaListNewCopia);
                    }
                }
            }
            for (Usuario usuarioListNewUsuario : usuarioListNew) {
                if (!usuarioListOld.contains(usuarioListNewUsuario)) {
                    Person oldFkIdPersonaOfUsuarioListNewUsuario = usuarioListNewUsuario.getFkIdPersona();
                    usuarioListNewUsuario.setFkIdPersona(persona);
                    usuarioListNewUsuario = em.merge(usuarioListNewUsuario);
                    if (oldFkIdPersonaOfUsuarioListNewUsuario != null
                            && !oldFkIdPersonaOfUsuarioListNewUsuario.equals(persona)) {
                        oldFkIdPersonaOfUsuarioListNewUsuario.getUsuariosList().remove(usuarioListNewUsuario);
                        oldFkIdPersonaOfUsuarioListNewUsuario = em.merge(oldFkIdPersonaOfUsuarioListNewUsuario);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = persona.getPersonId();
                if (findPersona(id) == null) {
                    throw new NonexistentEntityException("The persona with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Person persona;
            try {
                persona = em.getReference(Person.class, id);
                persona.getPersonId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The persona with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<TramitesPersonas> tramitesPersonasListOrphanCheck = persona.getTramitesPersonasList();
            for (TramitesPersonas tramitesPersonasListOrphanCheckTramitesPersonas : tramitesPersonasListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages
                        .add("This Persona (" + persona + ") cannot be destroyed since the TramitesPersonas "
                                + tramitesPersonasListOrphanCheckTramitesPersonas
                                + " in its tramitesPersonasList field has a non-nullable persona field.");
            }
            List<Presupuesto> presupuestoListOrphanCheck = persona.getPresupuestoList();
            for (Presupuesto presupuestoListOrphanCheckPresupuesto : presupuestoListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Persona (" + persona + ") cannot be destroyed since the Presupuesto "
                        + presupuestoListOrphanCheckPresupuesto
                        + " in its presupuestoList field has a non-nullable fkIdPersona field.");
            }
            List<GestionDeEscritura> gestionesDeEscriturasListOrphanCheck = persona.getGestionDeEscrituraList();
            for (GestionDeEscritura gestionesDeEscriturasListOrphanCheckGestionesDeEscrituras : gestionesDeEscriturasListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Persona (" + persona
                        + ") cannot be destroyed since the GestionesDeEscrituras "
                        + gestionesDeEscriturasListOrphanCheckGestionesDeEscrituras
                        + " in its gestionesDeEscriturasList field has a non-nullable fkIdPersonaEscribano field.");
            }
            List<Folio> folioListOrphanCheck = persona.getFolioList();
            for (Folio folioListOrphanCheckFolio : folioListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Persona (" + persona + ") cannot be destroyed since the Folio "
                        + folioListOrphanCheckFolio
                        + " in its folioList field has a non-nullable fkIdPersonaEscribano field.");
            }
            List<Suplencia> suplenciaListOrphanCheck = persona.getSuplenciaList();
            for (Suplencia suplenciaListOrphanCheckSuplencia : suplenciaListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Persona (" + persona + ") cannot be destroyed since the Suplencia "
                        + suplenciaListOrphanCheckSuplencia
                        + " in its suplenciaList field has a non-nullable fkIdSuplente field.");
            }
            List<Suplencia> suplenciaList1OrphanCheck = persona.getSuplenciaList1();
            for (Suplencia suplenciaList1OrphanCheckSuplencia : suplenciaList1OrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Persona (" + persona + ") cannot be destroyed since the Suplencia "
                        + suplenciaList1OrphanCheckSuplencia
                        + " in its suplenciaList1 field has a non-nullable fkIdSuplantado field.");
            }
            List<Copia> copiaListOrphanCheck = persona.getCopiaList();
            for (Copia copiaListOrphanCheckCopia : copiaListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Persona (" + persona + ") cannot be destroyed since the Copia "
                        + copiaListOrphanCheckCopia + " in its copiaList field has a non-nullable fkIdPersona field.");
            }
            List<Usuario> usuarioListOrphanCheck = persona.getUsuariosList();
            for (Usuario usuarioListOrphanCheckUsuario : usuarioListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Persona (" + persona + ") cannot be destroyed since the Usuario "
                        + usuarioListOrphanCheckUsuario
                        + " in its usuarioList field has a non-nullable fkIdPersona field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            TipoIdentificacion fkIdTipoIdentificacion = persona.getFkIdIdentificationType();
            if (fkIdTipoIdentificacion != null) {
                fkIdTipoIdentificacion.getPersonaList().remove(persona);
                fkIdTipoIdentificacion = em.merge(fkIdTipoIdentificacion);
            }
            em.remove(persona);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Person> findPersonaEntities() {
        return findPersonaEntities(true, -1, -1);
    }

    public List<Person> findPersonaEntities(int maxResults, int firstResult) {
        return findPersonaEntities(false, maxResults, firstResult);
    }

    private List<Person> findPersonaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Persona as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }

            List<Person> listaPersonas = q.getResultList();

            for (Iterator<Person> it = listaPersonas.iterator(); it.hasNext();) {
                Person persona = it.next();
                persona.setFolioList(null);

                persona.setGestionDeEscrituraList(null);

                persona.setPresupuestoList(null);
                persona.setSuplenciaList(null);

                TramitesPersonasJpaController jpaTramitePersona = new TramitesPersonasJpaController(emf);
                persona.setTramiteList(jpaTramitePersona.findTramitesPersona(persona.getPersonId()));

                persona.setTramitesPersonasList(null);
                persona.setUsuariosList(null);

                // persona.setTramiteList(new ArrayList<Tramite>());
            }

            return listaPersonas;
        } finally {
            em.close();
        }
    }

    public Person findPersona(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Person.class, id);
        } finally {
            em.close();
        }
    }

    public int getPersonaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Person> rt = cq.from(Person.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public static PersonJpaController getInstancia() {

        EntityManagerFactory emf = AdministradorJpa.getEmf();

        if (instancia == null || (instancia.emf == null && emf != null)) {
            instancia = new PersonJpaController(null, emf);
        }
        return instancia;

    }

    public Boolean modificarPersona(Person pPersona) throws ClassModifiedException, ClassEliminatedException {

        Boolean flag = false; // Variable para saber el resultado de la transaccion
        int oldVersion = ConstantesPersistencia.VERSION_INICIAL; // Variable para Version en memoria del Objeto
        int version = ConstantesPersistencia.VERSION_INICIAL; // Variable para Version en bd del Objeto

        EntityManager em = getEntityManager();

        Person persistentPersona = em.find(Person.class, pPersona.getPersonId());

        if (persistentPersona != null) {
            version = persistentPersona.getVersion(); // Version del Objeto en db
            oldVersion = pPersona.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) // Si son distintas "Alguien modifico el objeto"
            {
                throw new ClassModifiedException("La persona indicada ya ha sido modificada");
            } else {
                try {

                    em.getTransaction().begin();

                    // Atributos Persona
                    persistentPersona.setFirstName(pPersona.getFirstName());
                    persistentPersona.setLastName(pPersona.getLastName());
                    persistentPersona.setPhone(pPersona.getPhone());
                    persistentPersona.setEmail(pPersona.getEmail());
                    persistentPersona.setFkIdIdentificationType(pPersona.getFkIdIdentificationType());
                    persistentPersona.setIdentificationNumber(pPersona.getIdentificationNumber());

                    /*
                     * Tira error cuando el registro de escribano vale NULL if
                     * (pPersona.getRegistroEscribano() != 0) {
                     * persistentPersona.setRegistroEscribano(pPersona.getRegistroEscribano()); }
                     *
                     */
                    em.getTransaction().commit();
                    em.close();
                } catch (PersistenceException ex) {
                    System.out.println("Error de Persistencia: Usuario JpaController metodo: modificarUsuario");
                    ex.printStackTrace();
                }
            }
        } else // Si fue eliminado se dispara una excepcion
        {
            throw new ClassEliminatedException("La persona indicada ha sido eliminada");
        }
        return flag;
    }

    public Boolean registrarEscribano(Person escribano) throws ClassModifiedException, NonexistentEntityException {

        Boolean resultado = false; // Variable para saber el resultado de la transaccion
        int oldVersion = ConstantesPersistencia.VERSION_INICIAL; // Variable para Version en memoria del Objeto
        int version = ConstantesPersistencia.VERSION_INICIAL; // Variable para Version en bd del Objeto

        EntityManager em = getEntityManager();

        Person persistentPersona = em.find(Person.class, escribano.getPersonId());

        if (persistentPersona != null) {
            version = persistentPersona.getVersion(); // Version del Objeto en db
            oldVersion = escribano.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) // Si son distintas "Alguien modifico el objeto"
            {
                if (em != null) {
                    em.close();
                }
                throw new ClassModifiedException();
            } else {
                em.getTransaction().begin();

                if (escribano.getNotaryRegistrationNumber() != 0) {
                    persistentPersona.setNotaryRegistrationNumber(escribano.getNotaryRegistrationNumber());
                }
                em.getTransaction().commit();
                resultado = true;

                if (em != null) {
                    em.close();
                }
            }
        } else // Si fue eliminado se dispara una excepcion
        {
            throw new NonexistentEntityException("No existe la persona indicada");
        }
        return resultado;
    }

    public Boolean modificarCliente(Person pCliente) throws ClassModifiedException, ClassEliminatedException {

        Boolean flag = false; // Variable para saber el resultado de la transaccion
        int oldVersion = 0; // Variable para Version en memoria del Objeto
        int version = 0; // Variable para Version en bd del Objeto

        EntityManager em = getEntityManager();

        Person persistentPersona = em.find(Person.class, pCliente.getPersonId());

        if (persistentPersona != null) {
            version = persistentPersona.getVersion(); // Version del Objeto en db
            oldVersion = pCliente.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) // Si son distintas "Alguien modifico el objeto"
            {
                throw new ClassModifiedException("El cliente indicado ya ha sido modificado");

            } else {
                try {
                    em.getTransaction().begin();

                    // Atributos Persona
                    persistentPersona.setFirstName(pCliente.getFirstName());
                    persistentPersona.setLastName(pCliente.getLastName());
                    persistentPersona.setPhone(pCliente.getPhone());
                    persistentPersona.setEmail(pCliente.getEmail());
                    persistentPersona.setFkIdIdentificationType(pCliente.getFkIdIdentificationType());
                    persistentPersona.setIdentificationNumber(pCliente.getIdentificationNumber());

                    // La version del objeto queda a cargo de Hivernate
                    // Atributos Cliente
                    persistentPersona.setNationality(pCliente.getNationality());
                    persistentPersona.setBirthDate(pCliente.getBirthDate());
                    persistentPersona.setTaxId(pCliente.getTaxId());
                    persistentPersona.setMaritalStatus(pCliente.getMaritalStatus());
                    persistentPersona.setMarriageCount(pCliente.getMarriageCount());
                    persistentPersona.setSex(pCliente.getSex());
                    persistentPersona.setOccupation(pCliente.getOccupation());
                    persistentPersona.setAddress(pCliente.getAddress());

                    persistentPersona.setIsClient(pCliente.getIsClient());

                    persistentPersona.setNotaryRegistrationNumber(pCliente.getNotaryRegistrationNumber());

                    em.getTransaction().commit();
                    em.close();
                    flag = true;
                } catch (Exception e) {
                    System.out.println("Error de Persistencia: Usuario JpaController metodo: modificarUsuario");
                }
            }
        } else // Si fue eliminado se dispara una excepcion
        {
            throw new ClassEliminatedException("El cliente indicado ha sido eliminado o no existe");
        }
        return flag;
    }

    /**
     * Metodo JPA que permite buscar si existe o no , una persona con el mismo tipo
     * de
     * Identificacion y numero.
     *
     * @param pNumeroIdentificacion
     * @param pTipoIdentificacion
     * @return Retorno una lista de personas
     */
    public Person findPersonaTipoNumeroIdentificacion(DtoPerson dtoPersona) { // No pueden repetirse un mismo numero y
                                                                                // tipo de identificacion

        EntityManager em = getEntityManager();
        List<Person> listaPersona = null;
        Person persona = null;
        // acocio el nombre de la identificacion con su id correspondiente, para la
        // busqueda
        // TODO: VIOLACION DE CAPAS!
        dtoPersona.getDtoTipoIdentificacion()
                .setIdTipoIdentificacion(ControllerNegocio.getInstancia().asociarFkTipoIdentificacion(dtoPersona));

        String numeroIdentificacion = dtoPersona.getIdentificationNumber();
        int idTipoIdentificacion = dtoPersona.getDtoTipoIdentificacion().getIdTipoIdentificacion();

        Query query = em.createNamedQuery("Persona.findByNumeroIdentificacion");
        query.setParameter("numeroIdentificacion", numeroIdentificacion);

        listaPersona = query.getResultList();

        if (!listaPersona.isEmpty()) {
            for (int i = 0; i < listaPersona.size(); i++) {
                if (listaPersona.get(i).getFkIdIdentificationType().getIdTipoIdentificacion() == idTipoIdentificacion) {
                    persona = listaPersona.get(i);
                }

            }
        }

        return persona;
    }

    /**
     * Metodo JPA, que permite buscar personas por aproximacion de nombre y apellido
     *
     * @param dtoPersona
     * @return
     */
    public List<Person> findPersonaNombreApellido(DtoPerson dtoPersona) { // No pueden repetirse un mismo numero y
                                                                            // tipo de identificacion

        EntityManager em = getEntityManager();

        List<Person> listaPersona = null;
        Person persona = null;
        String nombre = "%" + dtoPersona.getFirstName() + "%";
        String apellido = "%" + dtoPersona.getLastName() + "%";

        Query query = em.createNamedQuery("Persona.findByPersonaNombreApellido");
        query.setParameter("nombre", nombre);
        query.setParameter("apellido", apellido);

        listaPersona = query.getResultList();

        return listaPersona;
    }

    /**
     * Metodo que devuelve todas las personas, con su red de objetos cada una
     *
     * @return
     */
    public List<Person> findPersonas() {
        EntityManager em = getEntityManager();

        List<Person> listaPersona = null;
        Person persona = null;
        try {
            Query query = em.createNamedQuery("Persona.findAll");

            listaPersona = query.getResultList();
        } catch (PersistenceException ex) {
            ex.printStackTrace();
        }
        return listaPersona;
    }

    public Person findPersonaEscribano(Person miPersona) { // No pueden repetirse un mismo numero y tipo de
                                                             // identificacion

        EntityManager em = getEntityManager();

        List<Person> listaPersona = null;
        Person persona = null;

        Query query = em.createNamedQuery("Persona.findByRegistroEscribano");
        query.setParameter("registroEscribano", miPersona.getNotaryRegistrationNumber());

        listaPersona = query.getResultList();

        if (listaPersona != null) {
            persona = listaPersona.get(0);
        }

        return persona;
    }

    public Person findPersonaPorId(Integer idPersona) {
        EntityManager em = getEntityManager();

        Person persona = new Person();

        Query query = em.createNamedQuery("Persona.findByIdPersona");
        query.setParameter("idPersona", idPersona);

        persona = (Person) query.getSingleResult();

        return persona;
    }

    @Override
    public String getNombreJpa() {
        return this.getClass().getName();
    }
}
