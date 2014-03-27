/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.DocumentoPresentado;
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Inmueble;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.negocio.TramitesPersonas;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;

/**
 *
 * @author matias
 */
public class TramiteJpaController implements Serializable, IPersistenciaJpa
{

    public TramiteJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public int create(Tramite tramite) throws IllegalOrphanException {
        int id = -1;
        if (tramite.getDocumentoPresentadoList() == null)
        {
            tramite.setDocumentoPresentadoList(new ArrayList<DocumentoPresentado>());
        }
        if (tramite.getTramitesPersonasList() == null)
        {
            tramite.setTramitesPersonasList(new ArrayList<TramitesPersonas>());
        }
        if (tramite.getPresupuestoList() == null)
        {
            tramite.setPresupuestoList(new ArrayList<Presupuesto>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Inmueble fkIdInmueble = tramite.getFkIdInmueble();
            if (fkIdInmueble != null)
            {
                fkIdInmueble = em.getReference(fkIdInmueble.getClass(), fkIdInmueble.getIdInmueble());
                tramite.setFkIdInmueble(fkIdInmueble);
            }
            Presupuesto fkIdPresupuesto = tramite.getFkIdPresupuesto();
            if (fkIdPresupuesto != null)
            {
                fkIdPresupuesto = em.getReference(fkIdPresupuesto.getClass(), fkIdPresupuesto.getIdPresupuesto());
                tramite.setFkIdPresupuesto(fkIdPresupuesto);
            }
            Escritura fkIdEscritura = tramite.getFkIdEscritura();
            if (fkIdEscritura != null)
            {
                fkIdEscritura = em.getReference(fkIdEscritura.getClass(), fkIdEscritura.getIdEscritura());
                tramite.setFkIdEscritura(fkIdEscritura);
            }
            GestionDeEscritura fkIdGestion = tramite.getFkIdGestion();
            if (fkIdGestion != null)
            {
                fkIdGestion = em.getReference(fkIdGestion.getClass(), fkIdGestion.getIdGestion());
                tramite.setFkIdGestion(fkIdGestion);
            }
            TipoDeTramite fkIdTipoTramite = tramite.getFkIdTipoTramite();
            if (fkIdTipoTramite != null)
            {
                fkIdTipoTramite = em.getReference(fkIdTipoTramite.getClass(), fkIdTipoTramite.getIdTipoTramite());
                tramite.setFkIdTipoTramite(fkIdTipoTramite);
            }
            List<DocumentoPresentado> attachedDocumentosPresentadoList = new ArrayList<DocumentoPresentado>();
            for (DocumentoPresentado documentosPresentadoListDocumentosPresentadoToAttach : tramite.getDocumentoPresentadoList())
            {
                documentosPresentadoListDocumentosPresentadoToAttach = em.getReference(documentosPresentadoListDocumentosPresentadoToAttach.getClass(), documentosPresentadoListDocumentosPresentadoToAttach.getIdDocumentoPresentado());
                attachedDocumentosPresentadoList.add(documentosPresentadoListDocumentosPresentadoToAttach);
            }
            tramite.setDocumentoPresentadoList(attachedDocumentosPresentadoList);
            List<TramitesPersonas> attachedTramitesPersonasList = new ArrayList<TramitesPersonas>();
            for (TramitesPersonas tramitesPersonasListTramitesPersonasToAttach : tramite.getTramitesPersonasList())
            {
                tramitesPersonasListTramitesPersonasToAttach = em.getReference(tramitesPersonasListTramitesPersonasToAttach.getClass(), tramitesPersonasListTramitesPersonasToAttach.getTramitesPersonasPK());
                attachedTramitesPersonasList.add(tramitesPersonasListTramitesPersonasToAttach);
            }
            tramite.setTramitesPersonasList(attachedTramitesPersonasList);
            List<Presupuesto> attachedPresupuestoList = new ArrayList<Presupuesto>();
            for (Presupuesto presupuestoListPresupuestoToAttach : tramite.getPresupuestoList())
            {
                presupuestoListPresupuestoToAttach = em.getReference(presupuestoListPresupuestoToAttach.getClass(), presupuestoListPresupuestoToAttach.getIdPresupuesto());
                attachedPresupuestoList.add(presupuestoListPresupuestoToAttach);
            }
            tramite.setPresupuestoList(attachedPresupuestoList);
            em.persist(tramite);
            if (fkIdInmueble != null)
            {
                fkIdInmueble.getTramiteList().add(tramite);
                fkIdInmueble = em.merge(fkIdInmueble);
            }
            if (fkIdPresupuesto != null)
            {
                Tramite oldFkIdTramiteOfFkIdPresupuesto = fkIdPresupuesto.getFkIdTramite();
                if (oldFkIdTramiteOfFkIdPresupuesto != null)
                {
                    oldFkIdTramiteOfFkIdPresupuesto.setFkIdPresupuesto(null);
                    oldFkIdTramiteOfFkIdPresupuesto = em.merge(oldFkIdTramiteOfFkIdPresupuesto);
                }
                fkIdPresupuesto.setFkIdTramite(tramite);
                fkIdPresupuesto = em.merge(fkIdPresupuesto);
            }
            if (fkIdEscritura != null)
            {
                fkIdEscritura.getTramiteList().add(tramite);
                fkIdEscritura = em.merge(fkIdEscritura);
            }
            if (fkIdGestion != null)
            {
                fkIdGestion.getTramiteList().add(tramite);
                fkIdGestion = em.merge(fkIdGestion);
            }
            if (fkIdTipoTramite != null)
            {
                fkIdTipoTramite.getTramiteList().add(tramite);
                fkIdTipoTramite = em.merge(fkIdTipoTramite);
            }
            for (DocumentoPresentado documentosPresentadoListDocumentosPresentado : tramite.getDocumentoPresentadoList())
            {
                Tramite oldFkIdTramiteOfDocumentosPresentadoListDocumentosPresentado = documentosPresentadoListDocumentosPresentado.getFkIdTramite();
                documentosPresentadoListDocumentosPresentado.setFkIdTramite(tramite);
                documentosPresentadoListDocumentosPresentado = em.merge(documentosPresentadoListDocumentosPresentado);
                if (oldFkIdTramiteOfDocumentosPresentadoListDocumentosPresentado != null)
                {
                    oldFkIdTramiteOfDocumentosPresentadoListDocumentosPresentado.getDocumentoPresentadoList().remove(documentosPresentadoListDocumentosPresentado);
                    oldFkIdTramiteOfDocumentosPresentadoListDocumentosPresentado = em.merge(oldFkIdTramiteOfDocumentosPresentadoListDocumentosPresentado);
                }
            }
            for (TramitesPersonas tramitesPersonasListTramitesPersonas : tramite.getTramitesPersonasList())
            {
                Tramite oldTramiteOfTramitesPersonasListTramitesPersonas = tramitesPersonasListTramitesPersonas.getTramite();
                tramitesPersonasListTramitesPersonas.setTramite(tramite);
                tramitesPersonasListTramitesPersonas = em.merge(tramitesPersonasListTramitesPersonas);
                if (oldTramiteOfTramitesPersonasListTramitesPersonas != null)
                {
                    oldTramiteOfTramitesPersonasListTramitesPersonas.getTramitesPersonasList().remove(tramitesPersonasListTramitesPersonas);
                    oldTramiteOfTramitesPersonasListTramitesPersonas = em.merge(oldTramiteOfTramitesPersonasListTramitesPersonas);
                }
            }
            for (Presupuesto presupuestoListPresupuesto : tramite.getPresupuestoList())
            {
                Tramite oldFkIdTramiteOfPresupuestoListPresupuesto = presupuestoListPresupuesto.getFkIdTramite();
                presupuestoListPresupuesto.setFkIdTramite(tramite);
                presupuestoListPresupuesto = em.merge(presupuestoListPresupuesto);
                if (oldFkIdTramiteOfPresupuestoListPresupuesto != null)
                {
                    oldFkIdTramiteOfPresupuestoListPresupuesto.getPresupuestoList().remove(presupuestoListPresupuesto);
                    oldFkIdTramiteOfPresupuestoListPresupuesto = em.merge(oldFkIdTramiteOfPresupuestoListPresupuesto);
                }
            }
            em.getTransaction().commit();
            id = tramite.getIdTramite();
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return id;
    }

    public boolean edit(Tramite tramite) throws IllegalOrphanException, NonexistentEntityException, Exception {
        Boolean modificado = false;
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Tramite persistentTramite = em.find(Tramite.class, tramite.getIdTramite());
            Inmueble fkIdInmuebleOld = persistentTramite.getFkIdInmueble();
            Inmueble fkIdInmuebleNew = tramite.getFkIdInmueble();
            Presupuesto fkIdPresupuestoOld = persistentTramite.getFkIdPresupuesto();
            Presupuesto fkIdPresupuestoNew = tramite.getFkIdPresupuesto();
            Escritura fkIdEscrituraOld = persistentTramite.getFkIdEscritura();
            Escritura fkIdEscrituraNew = tramite.getFkIdEscritura();
            GestionDeEscritura fkIdGestionOld = persistentTramite.getFkIdGestion();
            GestionDeEscritura fkIdGestionNew = tramite.getFkIdGestion();
            TipoDeTramite fkIdTipoTramiteOld = persistentTramite.getFkIdTipoTramite();
            TipoDeTramite fkIdTipoTramiteNew = tramite.getFkIdTipoTramite();
            List<DocumentoPresentado> documentosPresentadoListOld = persistentTramite.getDocumentoPresentadoList();
            List<DocumentoPresentado> documentosPresentadoListNew = tramite.getDocumentoPresentadoList();
            List<TramitesPersonas> tramitesPersonasListOld = persistentTramite.getTramitesPersonasList();
            List<TramitesPersonas> tramitesPersonasListNew = tramite.getTramitesPersonasList();
            List<Presupuesto> presupuestoListOld = persistentTramite.getPresupuestoList();
            List<Presupuesto> presupuestoListNew = tramite.getPresupuestoList();
            List<String> illegalOrphanMessages = null;
            if (fkIdPresupuestoOld != null && !fkIdPresupuestoOld.equals(fkIdPresupuestoNew))
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain Presupuesto " + fkIdPresupuestoOld + " since its fkIdTramite field is not nullable.");
            }
            for (DocumentoPresentado documentosPresentadoListOldDocumentosPresentado : documentosPresentadoListOld)
            {
                if (!documentosPresentadoListNew.contains(documentosPresentadoListOldDocumentosPresentado))
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain DocumentosPresentado " + documentosPresentadoListOldDocumentosPresentado + " since its fkIdTramite field is not nullable.");
                }
            }
            for (TramitesPersonas tramitesPersonasListOldTramitesPersonas : tramitesPersonasListOld)
            {
                if (!tramitesPersonasListNew.contains(tramitesPersonasListOldTramitesPersonas))
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TramitesPersonas " + tramitesPersonasListOldTramitesPersonas + " since its tramite field is not nullable.");
                }
            }
            for (Presupuesto presupuestoListOldPresupuesto : presupuestoListOld)
            {
                if (!presupuestoListNew.contains(presupuestoListOldPresupuesto))
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Presupuesto " + presupuestoListOldPresupuesto + " since its fkIdTramite field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (fkIdInmuebleNew != null)
            {
                fkIdInmuebleNew = em.getReference(fkIdInmuebleNew.getClass(), fkIdInmuebleNew.getIdInmueble());
                tramite.setFkIdInmueble(fkIdInmuebleNew);
            }
            if (fkIdPresupuestoNew != null)
            {
                fkIdPresupuestoNew = em.getReference(fkIdPresupuestoNew.getClass(), fkIdPresupuestoNew.getIdPresupuesto());
                tramite.setFkIdPresupuesto(fkIdPresupuestoNew);
            }
            if (fkIdEscrituraNew != null)
            {
                fkIdEscrituraNew = em.getReference(fkIdEscrituraNew.getClass(), fkIdEscrituraNew.getIdEscritura());
                tramite.setFkIdEscritura(fkIdEscrituraNew);
            }
            if (fkIdGestionNew != null)
            {
                fkIdGestionNew = em.getReference(fkIdGestionNew.getClass(), fkIdGestionNew.getIdGestion());
                tramite.setFkIdGestion(fkIdGestionNew);
            }
            if (fkIdTipoTramiteNew != null)
            {
                fkIdTipoTramiteNew = em.getReference(fkIdTipoTramiteNew.getClass(), fkIdTipoTramiteNew.getIdTipoTramite());
                tramite.setFkIdTipoTramite(fkIdTipoTramiteNew);
            }
            List<DocumentoPresentado> attachedDocumentosPresentadoListNew = new ArrayList<DocumentoPresentado>();
            for (DocumentoPresentado documentosPresentadoListNewDocumentosPresentadoToAttach : documentosPresentadoListNew)
            {
                documentosPresentadoListNewDocumentosPresentadoToAttach = em.getReference(documentosPresentadoListNewDocumentosPresentadoToAttach.getClass(), documentosPresentadoListNewDocumentosPresentadoToAttach.getIdDocumentoPresentado());
                attachedDocumentosPresentadoListNew.add(documentosPresentadoListNewDocumentosPresentadoToAttach);
            }
            documentosPresentadoListNew = attachedDocumentosPresentadoListNew;
            tramite.setDocumentoPresentadoList(documentosPresentadoListNew);
            List<TramitesPersonas> attachedTramitesPersonasListNew = new ArrayList<TramitesPersonas>();
            for (TramitesPersonas tramitesPersonasListNewTramitesPersonasToAttach : tramitesPersonasListNew)
            {
                tramitesPersonasListNewTramitesPersonasToAttach = em.getReference(tramitesPersonasListNewTramitesPersonasToAttach.getClass(), tramitesPersonasListNewTramitesPersonasToAttach.getTramitesPersonasPK());
                attachedTramitesPersonasListNew.add(tramitesPersonasListNewTramitesPersonasToAttach);
            }
            tramitesPersonasListNew = attachedTramitesPersonasListNew;
            tramite.setTramitesPersonasList(tramitesPersonasListNew);
            List<Presupuesto> attachedPresupuestoListNew = new ArrayList<Presupuesto>();
            for (Presupuesto presupuestoListNewPresupuestoToAttach : presupuestoListNew)
            {
                presupuestoListNewPresupuestoToAttach = em.getReference(presupuestoListNewPresupuestoToAttach.getClass(), presupuestoListNewPresupuestoToAttach.getIdPresupuesto());
                attachedPresupuestoListNew.add(presupuestoListNewPresupuestoToAttach);
            }
            presupuestoListNew = attachedPresupuestoListNew;
            tramite.setPresupuestoList(presupuestoListNew);
            tramite = em.merge(tramite);
            if (fkIdInmuebleOld != null && !fkIdInmuebleOld.equals(fkIdInmuebleNew))
            {
                fkIdInmuebleOld.getTramiteList().remove(tramite);
                fkIdInmuebleOld = em.merge(fkIdInmuebleOld);
            }
            if (fkIdInmuebleNew != null && !fkIdInmuebleNew.equals(fkIdInmuebleOld))
            {
                fkIdInmuebleNew.getTramiteList().add(tramite);
                fkIdInmuebleNew = em.merge(fkIdInmuebleNew);
            }
            if (fkIdPresupuestoNew != null && !fkIdPresupuestoNew.equals(fkIdPresupuestoOld))
            {
                Tramite oldFkIdTramiteOfFkIdPresupuesto = fkIdPresupuestoNew.getFkIdTramite();
                if (oldFkIdTramiteOfFkIdPresupuesto != null)
                {
                    oldFkIdTramiteOfFkIdPresupuesto.setFkIdPresupuesto(null);
                    oldFkIdTramiteOfFkIdPresupuesto = em.merge(oldFkIdTramiteOfFkIdPresupuesto);
                }
                fkIdPresupuestoNew.setFkIdTramite(tramite);
                fkIdPresupuestoNew = em.merge(fkIdPresupuestoNew);
            }
            if (fkIdEscrituraOld != null && !fkIdEscrituraOld.equals(fkIdEscrituraNew))
            {
                fkIdEscrituraOld.getTramiteList().remove(tramite);
                fkIdEscrituraOld = em.merge(fkIdEscrituraOld);
            }
            if (fkIdEscrituraNew != null && !fkIdEscrituraNew.equals(fkIdEscrituraOld))
            {
                fkIdEscrituraNew.getTramiteList().add(tramite);
                fkIdEscrituraNew = em.merge(fkIdEscrituraNew);
            }
            if (fkIdGestionOld != null && !fkIdGestionOld.equals(fkIdGestionNew))
            {
                fkIdGestionOld.getTramiteList().remove(tramite);
                fkIdGestionOld = em.merge(fkIdGestionOld);
            }
            if (fkIdGestionNew != null && !fkIdGestionNew.equals(fkIdGestionOld))
            {
                fkIdGestionNew.getTramiteList().add(tramite);
                fkIdGestionNew = em.merge(fkIdGestionNew);
            }
            if (fkIdTipoTramiteOld != null && !fkIdTipoTramiteOld.equals(fkIdTipoTramiteNew))
            {
                fkIdTipoTramiteOld.getTramiteList().remove(tramite);
                fkIdTipoTramiteOld = em.merge(fkIdTipoTramiteOld);
            }
            if (fkIdTipoTramiteNew != null && !fkIdTipoTramiteNew.equals(fkIdTipoTramiteOld))
            {
                fkIdTipoTramiteNew.getTramiteList().add(tramite);
                fkIdTipoTramiteNew = em.merge(fkIdTipoTramiteNew);
            }
            for (DocumentoPresentado documentosPresentadoListNewDocumentosPresentado : documentosPresentadoListNew)
            {
                if (!documentosPresentadoListOld.contains(documentosPresentadoListNewDocumentosPresentado))
                {
                    Tramite oldFkIdTramiteOfDocumentosPresentadoListNewDocumentosPresentado = documentosPresentadoListNewDocumentosPresentado.getFkIdTramite();
                    documentosPresentadoListNewDocumentosPresentado.setFkIdTramite(tramite);
                    documentosPresentadoListNewDocumentosPresentado = em.merge(documentosPresentadoListNewDocumentosPresentado);
                    if (oldFkIdTramiteOfDocumentosPresentadoListNewDocumentosPresentado != null && !oldFkIdTramiteOfDocumentosPresentadoListNewDocumentosPresentado.equals(tramite))
                    {
                        oldFkIdTramiteOfDocumentosPresentadoListNewDocumentosPresentado.getDocumentoPresentadoList().remove(documentosPresentadoListNewDocumentosPresentado);
                        oldFkIdTramiteOfDocumentosPresentadoListNewDocumentosPresentado = em.merge(oldFkIdTramiteOfDocumentosPresentadoListNewDocumentosPresentado);
                    }
                }
            }
            for (TramitesPersonas tramitesPersonasListNewTramitesPersonas : tramitesPersonasListNew)
            {
                if (!tramitesPersonasListOld.contains(tramitesPersonasListNewTramitesPersonas))
                {
                    Tramite oldTramiteOfTramitesPersonasListNewTramitesPersonas = tramitesPersonasListNewTramitesPersonas.getTramite();
                    tramitesPersonasListNewTramitesPersonas.setTramite(tramite);
                    tramitesPersonasListNewTramitesPersonas = em.merge(tramitesPersonasListNewTramitesPersonas);
                    if (oldTramiteOfTramitesPersonasListNewTramitesPersonas != null && !oldTramiteOfTramitesPersonasListNewTramitesPersonas.equals(tramite))
                    {
                        oldTramiteOfTramitesPersonasListNewTramitesPersonas.getTramitesPersonasList().remove(tramitesPersonasListNewTramitesPersonas);
                        oldTramiteOfTramitesPersonasListNewTramitesPersonas = em.merge(oldTramiteOfTramitesPersonasListNewTramitesPersonas);
                    }
                }
            }
            for (Presupuesto presupuestoListNewPresupuesto : presupuestoListNew)
            {
                if (!presupuestoListOld.contains(presupuestoListNewPresupuesto))
                {
                    Tramite oldFkIdTramiteOfPresupuestoListNewPresupuesto = presupuestoListNewPresupuesto.getFkIdTramite();
                    presupuestoListNewPresupuesto.setFkIdTramite(tramite);
                    presupuestoListNewPresupuesto = em.merge(presupuestoListNewPresupuesto);
                    if (oldFkIdTramiteOfPresupuestoListNewPresupuesto != null && !oldFkIdTramiteOfPresupuestoListNewPresupuesto.equals(tramite))
                    {
                        oldFkIdTramiteOfPresupuestoListNewPresupuesto.getPresupuestoList().remove(presupuestoListNewPresupuesto);
                        oldFkIdTramiteOfPresupuestoListNewPresupuesto = em.merge(oldFkIdTramiteOfPresupuestoListNewPresupuesto);
                    }
                }
            }
            em.getTransaction().commit();
            modificado = true;
        }
        catch (Exception ex)
        {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0)
            {
                Integer id = tramite.getIdTramite();
                if (findTramite(id) == null)
                {
                    throw new NonexistentEntityException("The tramite with id " + id + " no longer exists.");
                }
            }
            throw ex;
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return modificado;
    }

    public Boolean editTramite(Tramite tramiteModificado) {
        Boolean modificado = Boolean.FALSE;

        try
        {
            EntityManager em = getEntityManager();

            em.getTransaction().begin();

            Tramite tramiteViejo = em.find(Tramite.class, tramiteModificado.getIdTramite());

            tramiteViejo.setFkIdGestion(tramiteModificado.getFkIdGestion());
            tramiteViejo.setFkIdEscritura(tramiteModificado.getFkIdEscritura());

            em.getTransaction().commit();
            em.close();
            modificado = Boolean.TRUE;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return modificado;
    }

    public Boolean editTramite2(Tramite tramiteModificado) {
        Boolean modificado = Boolean.FALSE;

        try
        {
            EntityManager em = getEntityManager();

            em.getTransaction().begin();

            Tramite tramiteViejo = em.find(Tramite.class, tramiteModificado.getIdTramite());

            tramiteViejo.setFkIdGestion(tramiteModificado.getFkIdGestion());



            em.getTransaction().commit();
            em.close();
            modificado = Boolean.TRUE;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return modificado;
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Tramite tramite;
            try
            {
                tramite = em.getReference(Tramite.class, id);
                tramite.getIdTramite();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The tramite with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Presupuesto fkIdPresupuestoOrphanCheck = tramite.getFkIdPresupuesto();
            if (fkIdPresupuestoOrphanCheck != null)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Tramite (" + tramite + ") cannot be destroyed since the Presupuesto " + fkIdPresupuestoOrphanCheck + " in its fkIdPresupuesto field has a non-nullable fkIdTramite field.");
            }
            List<DocumentoPresentado> documentosPresentadoListOrphanCheck = tramite.getDocumentoPresentadoList();
            for (DocumentoPresentado documentosPresentadoListOrphanCheckDocumentosPresentado : documentosPresentadoListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Tramite (" + tramite + ") cannot be destroyed since the DocumentosPresentado " + documentosPresentadoListOrphanCheckDocumentosPresentado + " in its documentosPresentadoList field has a non-nullable fkIdTramite field.");
            }
            List<TramitesPersonas> tramitesPersonasListOrphanCheck = tramite.getTramitesPersonasList();
            for (TramitesPersonas tramitesPersonasListOrphanCheckTramitesPersonas : tramitesPersonasListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Tramite (" + tramite + ") cannot be destroyed since the TramitesPersonas " + tramitesPersonasListOrphanCheckTramitesPersonas + " in its tramitesPersonasList field has a non-nullable tramite field.");
            }
            List<Presupuesto> presupuestoListOrphanCheck = tramite.getPresupuestoList();
            for (Presupuesto presupuestoListOrphanCheckPresupuesto : presupuestoListOrphanCheck)
            {
                if (illegalOrphanMessages == null)
                {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Tramite (" + tramite + ") cannot be destroyed since the Presupuesto " + presupuestoListOrphanCheckPresupuesto + " in its presupuestoList field has a non-nullable fkIdTramite field.");
            }
            if (illegalOrphanMessages != null)
            {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Inmueble fkIdInmueble = tramite.getFkIdInmueble();
            if (fkIdInmueble != null)
            {
                fkIdInmueble.getTramiteList().remove(tramite);
                fkIdInmueble = em.merge(fkIdInmueble);
            }
            Escritura fkIdEscritura = tramite.getFkIdEscritura();
            if (fkIdEscritura != null)
            {
                fkIdEscritura.getTramiteList().remove(tramite);
                fkIdEscritura = em.merge(fkIdEscritura);
            }
            GestionDeEscritura fkIdGestion = tramite.getFkIdGestion();
            if (fkIdGestion != null)
            {
                fkIdGestion.getTramiteList().remove(tramite);
                fkIdGestion = em.merge(fkIdGestion);
            }
            TipoDeTramite fkIdTipoTramite = tramite.getFkIdTipoTramite();
            if (fkIdTipoTramite != null)
            {
                fkIdTipoTramite.getTramiteList().remove(tramite);
                fkIdTipoTramite = em.merge(fkIdTipoTramite);
            }
            em.remove(tramite);
            em.getTransaction().commit();
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
    }

    public List<Tramite> findTramiteEntities() {
        return findTramiteEntities(true, -1, -1);
    }

    public List<Tramite> findTramiteEntities(int maxResults, int firstResult) {
        return findTramiteEntities(false, maxResults, firstResult);
    }

    private List<Tramite> findTramiteEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from Tramite as o");

            if (!all)
            {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            List<Tramite> listaTramites = q.getResultList();

            for (Iterator<Tramite> it = listaTramites.iterator(); it.hasNext();)
            {
                Tramite tramite = it.next();
                tramite.setPersonaList(new ArrayList<Persona>());
                tramite.setDocumentoPresentadoList(new ArrayList<DocumentoPresentado>());
            }

            return listaTramites;
        }
        finally
        {
            em.close();
        }
    }

    public Tramite findTramite(Integer id) {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(Tramite.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public Tramite encontrarTramite(Integer id) {
        Tramite tramite = new Tramite();
        EntityManager em = getEntityManager();
        try
        {
            Tramite miTramite = em.find(Tramite.class, id);

            tramite.setIdTramite(miTramite.getIdTramite());
            tramite.setFkIdEscritura(miTramite.getFkIdEscritura());
            tramite.setFkIdGestion(miTramite.getFkIdGestion());
            tramite.setFkIdInmueble(miTramite.getFkIdInmueble());
            tramite.setFkIdPresupuesto(miTramite.getFkIdPresupuesto());
            tramite.setFkIdTipoTramite(miTramite.getFkIdTipoTramite());
            tramite.setObservaciones(miTramite.getObservaciones());
            tramite.setVersion(miTramite.getVersion());
        }
        finally
        {
            em.close();
        }
        return tramite;
    }

    public List<Tramite> encontrarTramitePresupuesto(Integer idPresupuesto) {

        EntityManager em = getEntityManager();

        List<Tramite> miTramite = null;

        try
        {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("Tramite.findByIdPresupuesto");

            //Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("idPresupuesto", idPresupuesto);

            miTramite = (List<Tramite>) q.getResultList();

        }
        finally
        {
            em.close();
        }
        return miTramite;
    }

    public int getTramiteCount() {
        EntityManager em = getEntityManager();
        try
        {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Tramite> rt = cq.from(Tramite.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public Boolean asociarPresupuesto(Tramite miTramite) {
        Boolean modificado = false;
        EntityManager em = this.getEntityManager();
        EntityTransaction tx = null;

        // Abro una nueva transaccion
        tx = em.getTransaction();
        tx.begin();

        Query q = em.createQuery("UPDATE Tramite t SET t.fkIdPresupuesto.idPresupuesto = :idPresupuesto WHERE t.idTramite = :id");
        q.setParameter("idPresupuesto", miTramite.getFkIdPresupuesto().getIdPresupuesto());
        q.setParameter("id", miTramite.getIdTramite());

        int updated = q.executeUpdate();

        tx.commit();

        em.close();

        if (updated > 0)
        {
            modificado = true;
        }

        return modificado;
    }

    @Override
    public String getNombreJpa() {
        return this.getClass().getName();
    }
}
