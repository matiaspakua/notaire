/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.licensis.notaire.jpa.exceptions.CreateEntityException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.exceptions.PreexistingEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.negocio.TramitesPersonas;
import com.licensis.notaire.negocio.TramitesPersonasPK;

/**
 *
 * @author matias
 */
public class TramitesPersonasJpaController implements Serializable, IPersistenciaJpa
{

    public TramitesPersonasJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TramitesPersonas tramitesPersonas) throws PreexistingEntityException, CreateEntityException {
        if (tramitesPersonas.getTramitesPersonasPK() == null)
        {
            tramitesPersonas.setTramitesPersonasPK(new TramitesPersonasPK());
        }
        tramitesPersonas.getTramitesPersonasPK().setFkIdPersonaCliente(tramitesPersonas.getPersona().getIdPersona());
        tramitesPersonas.getTramitesPersonasPK().setFkIdTramite(tramitesPersonas.getTramite().getIdTramite());
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Persona persona = tramitesPersonas.getPersona();
            if (persona != null)
            {
                persona = em.getReference(persona.getClass(), persona.getIdPersona());
                tramitesPersonas.setPersona(persona);
            }
            Tramite tramite = tramitesPersonas.getTramite();
            if (tramite != null)
            {
                tramite = em.getReference(tramite.getClass(), tramite.getIdTramite());
                tramitesPersonas.setTramite(tramite);
            }
            em.persist(tramitesPersonas);
            if (persona != null)
            {
                persona.getTramitesPersonasList().add(tramitesPersonas);
                persona = em.merge(persona);
            }
            if (tramite != null)
            {
                tramite.getTramitesPersonasList().add(tramitesPersonas);
                tramite = em.merge(tramite);
            }
            em.getTransaction().commit();
        }
        catch (Exception ex)
        {
            if (findTramitesPersonas(tramitesPersonas.getTramitesPersonasPK()) != null)
            {
                throw new PreexistingEntityException("TramitesPersonas " + tramitesPersonas + " already exists.", ex);
            }
            throw new CreateEntityException("Error creating a entity of type: Tramites Personas");
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
    }

    /**
     * Crea un registro nuevo de tramites_persona.
     *
     * @param tramitesPersonas
     * @return Verdadero si se pudo crear el registro, falso en caso de que el registro este
     * repetido (error grave), u ocurra algun otro tipo de error.
     * @throws PreexistingEntityException
     * @throws Exception
     */
    public boolean createSimple(TramitesPersonas tramitesPersonas) throws PreexistingEntityException, Exception {
        boolean resultado = false;

        if (tramitesPersonas.getTramitesPersonasPK() == null)
        {
            tramitesPersonas.setTramitesPersonasPK(new TramitesPersonasPK());
        }
        tramitesPersonas.getTramitesPersonasPK().setFkIdPersonaCliente(tramitesPersonas.getPersona().getIdPersona());
        tramitesPersonas.getTramitesPersonasPK().setFkIdTramite(tramitesPersonas.getTramite().getIdTramite());
        EntityManager em = null;

        Long dateLong = Calendar.getInstance().getTimeInMillis();
        Date date = new Date(dateLong);

        tramitesPersonas.setObservaciones(date.toLocaleString());

        try
        {
            em = getEntityManager();
            em.getTransaction().begin();

            em.persist(tramitesPersonas);

            em.getTransaction().commit();

            resultado = true;
        }
        catch (Exception ex)
        {
            if (findTramitesPersonas(tramitesPersonas.getTramitesPersonasPK()) != null)
            {
                throw new PreexistingEntityException("TramitesPersonas " + tramitesPersonas + " already exists.", ex);
            }
            return false;

        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return resultado;
    }

    public void edit(TramitesPersonas tramitesPersonas) throws NonexistentEntityException, Exception {
        tramitesPersonas.getTramitesPersonasPK().setFkIdPersonaCliente(tramitesPersonas.getPersona().getIdPersona());
        tramitesPersonas.getTramitesPersonasPK().setFkIdTramite(tramitesPersonas.getTramite().getIdTramite());
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            TramitesPersonas persistentTramitesPersonas = em.find(TramitesPersonas.class, tramitesPersonas.getTramitesPersonasPK());
            Persona personaOld = persistentTramitesPersonas.getPersona();
            Persona personaNew = tramitesPersonas.getPersona();
            Tramite tramiteOld = persistentTramitesPersonas.getTramite();
            Tramite tramiteNew = tramitesPersonas.getTramite();
            if (personaNew != null)
            {
                personaNew = em.getReference(personaNew.getClass(), personaNew.getIdPersona());
                tramitesPersonas.setPersona(personaNew);
            }
            if (tramiteNew != null)
            {
                tramiteNew = em.getReference(tramiteNew.getClass(), tramiteNew.getIdTramite());
                tramitesPersonas.setTramite(tramiteNew);
            }
            tramitesPersonas = em.merge(tramitesPersonas);
            if (personaOld != null && !personaOld.equals(personaNew))
            {
                personaOld.getTramitesPersonasList().remove(tramitesPersonas);
                personaOld = em.merge(personaOld);
            }
            if (personaNew != null && !personaNew.equals(personaOld))
            {
                personaNew.getTramitesPersonasList().add(tramitesPersonas);
                personaNew = em.merge(personaNew);
            }
            if (tramiteOld != null && !tramiteOld.equals(tramiteNew))
            {
                tramiteOld.getTramitesPersonasList().remove(tramitesPersonas);
                tramiteOld = em.merge(tramiteOld);
            }
            if (tramiteNew != null && !tramiteNew.equals(tramiteOld))
            {
                tramiteNew.getTramitesPersonasList().add(tramitesPersonas);
                tramiteNew = em.merge(tramiteNew);
            }
            em.getTransaction().commit();
        }
        catch (Exception ex)
        {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0)
            {
                TramitesPersonasPK id = tramitesPersonas.getTramitesPersonasPK();
                if (findTramitesPersonas(id) == null)
                {
                    throw new NonexistentEntityException("The tramitesPersonas with id " + id + " no longer exists.");
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
    }

    public void destroy(TramitesPersonasPK id) throws NonexistentEntityException {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            TramitesPersonas tramitesPersonas;
            try
            {
                tramitesPersonas = em.getReference(TramitesPersonas.class, id);
                tramitesPersonas.getTramitesPersonasPK();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The tramitesPersonas with id " + id + " no longer exists.", enfe);
            }
            Persona persona = tramitesPersonas.getPersona();
            if (persona != null)
            {
                persona.getTramitesPersonasList().remove(tramitesPersonas);
                persona = em.merge(persona);
            }
            Tramite tramite = tramitesPersonas.getTramite();
            if (tramite != null)
            {
                tramite.getTramitesPersonasList().remove(tramitesPersonas);
                tramite = em.merge(tramite);
            }
            em.remove(tramitesPersonas);
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

//    public void destroy2(TramitesPersonasPK id) throws NonexistentEntityException {
//        EntityManager em = null;
//        try
//        {
//            em = getEntityManager();
//            em.getTransaction().begin();
//            TramitesPersonas tramitesPersonas;
//            try
//            {
//                tramitesPersonas = em.getReference(TramitesPersonas.class, id);
//                tramitesPersonas.getTramitesPersonasPK();
//            }
//            catch (EntityNotFoundException enfe)
//            {
//                throw new NonexistentEntityException("The tramitesPersonas with id " + id + " no longer exists.", enfe);
//            }
//            em.remove(tramitesPersonas);
//            em.getTransaction().commit();
//        }
//        finally
//        {
//            if (em != null)
//            {
//                em.close();
//            }
//        }
//    }
    public int eliminarRegistro(TramitesPersonas registro) {
        EntityManager em = getEntityManager();
        int rowCount = 0;
        try
        {
            TramitesPersonas encontrado = (TramitesPersonas) this.findTramitesClientes(registro.getTramitesPersonasPK().getFkIdPersonaCliente(), registro.getTramitesPersonasPK().getFkIdTramite()).get(0);

            if (encontrado != null)
            {

                em.getTransaction().begin();
                Query query = em.createQuery("DELETE FROM TramitesPersonas t WHERE t.tramitesPersonasPK.fkIdPersonaCliente = ?1 AND t.tramitesPersonasPK.fkIdTramite = ?2");
                query.setParameter(1, registro.getTramitesPersonasPK().getFkIdPersonaCliente());
                query.setParameter(2, registro.getTramitesPersonasPK().getFkIdTramite());
                rowCount = query.executeUpdate();
                em.remove(registro);
                em.getTransaction().commit();
            }
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return rowCount;
    }

    public List<TramitesPersonas> findTramitesPersonasEntities() {
        return findTramitesPersonasEntities(true, -1, -1);
    }

    public List<TramitesPersonas> findTramitesPersonasEntities(int maxResults, int firstResult) {
        return findTramitesPersonasEntities(false, maxResults, firstResult);
    }

    private List<TramitesPersonas> findTramitesPersonasEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try
        {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TramitesPersonas.class));
            Query q = em.createQuery(cq);
            if (!all)
            {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        }
        finally
        {
            em.close();
        }
    }

    public TramitesPersonas findTramitesPersonas(TramitesPersonasPK id) {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(TramitesPersonas.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getTramitesPersonasCount() {
        EntityManager em = getEntityManager();
        try
        {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TramitesPersonas> rt = cq.from(TramitesPersonas.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public List<Tramite> findTramitesPersona(Integer idPersona) {
        EntityManager em = getEntityManager();
        List<Tramite> tramites = null;
        Query query = em.createNamedQuery("TramitesPersonas.findByFkIdPersonaCliente");
        query.setParameter("fkIdPersonaCliente", idPersona);

        tramites = query.getResultList();

        return tramites;
    }

    /**
     * Busca la relacion entre tramite y cliente (un registro en particualar)
     *
     * @param idPersona
     * @param idTramite
     * @return
     */
    public List<TramitesPersonas> findTramitesClientes(Integer idPersona, Integer idTramite) {
        EntityManager em = getEntityManager();
        List<TramitesPersonas> listaTramitesPersonas = null;
        Query query = em.createNamedQuery("TramitesPersonas.findByTramiteCliente");
        query.setParameter("fkIdPersonaCliente", idPersona);
        query.setParameter("fkIdTramite", idTramite);

        listaTramitesPersonas = query.getResultList();

        return listaTramitesPersonas;
    }

    @Override
    public String getNombreJpa() {
        return this.getClass().getName();
    }
}
