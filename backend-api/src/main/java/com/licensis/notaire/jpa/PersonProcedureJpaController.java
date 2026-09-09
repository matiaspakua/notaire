/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.CreateEntityException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.exceptions.PreexistingEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.PersonProcedure;
import com.licensis.notaire.business.PersonProcedurePK;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

/**
 *
 * @author matias
 */
public class PersonProcedureJpaController implements Serializable, IPersistenciaJpa
{

    public PersonProcedureJpaController(EntityManagerFactory emf)
    {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager()
    {
        return emf.createEntityManager();
    }

    public void create(PersonProcedure personProcedure) throws PreexistingEntityException, CreateEntityException
    {
        if (personProcedure.getPersonProcedurePK() == null)
        {
            personProcedure.setPersonProcedurePK(new PersonProcedurePK());
        }
        personProcedure.getPersonProcedurePK().setFkIdClientPerson(personProcedure.getPerson().getPersonId());
        personProcedure.getPersonProcedurePK().setFkIdProcedure(personProcedure.getProcedure().getIdProcedure());
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Person person = personProcedure.getPerson();
            if (person != null)
            {
                person = em.getReference(person.getClass(), person.getPersonId());
                personProcedure.setPerson(person);
            }
            Procedure procedure = personProcedure.getProcedure();
            if (procedure != null)
            {
                procedure = em.getReference(procedure.getClass(), procedure.getIdProcedure());
                personProcedure.setProcedure(procedure);
            }
            em.persist(personProcedure);
            if (person != null)
            {
                person.getPersonProcedureList().add(personProcedure);
                person = em.merge(person);
            }
            if (procedure != null)
            {
                procedure.getPersonProcedureList().add(personProcedure);
                procedure = em.merge(procedure);
            }
            em.getTransaction().commit();
        }
        catch (Exception ex)
        {
            if (findPersonProcedure(personProcedure.getPersonProcedurePK()) != null)
            {
                throw new PreexistingEntityException("TramitesPersonas " + personProcedure + " already exists.", ex);
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
    public boolean createSimple(PersonProcedure personProcedure) throws PreexistingEntityException, Exception
    {
        boolean resultado = false;

        if (personProcedure.getPersonProcedurePK() == null)
        {
            personProcedure.setPersonProcedurePK(new PersonProcedurePK());
        }
        personProcedure.getPersonProcedurePK().setFkIdClientPerson(personProcedure.getPerson().getPersonId());
        personProcedure.getPersonProcedurePK().setFkIdProcedure(personProcedure.getProcedure().getIdProcedure());
        EntityManager em = null;

        Long dateLong = Calendar.getInstance().getTimeInMillis();
        Date date = new Date(dateLong);

        personProcedure.setNotes(date.toLocaleString());

        try
        {
            em = getEntityManager();
            em.getTransaction().begin();

            em.persist(personProcedure);

            em.getTransaction().commit();

            resultado = true;
        }
        catch (Exception ex)
        {
            if (findPersonProcedure(personProcedure.getPersonProcedurePK()) != null)
            {
                throw new PreexistingEntityException("TramitesPersonas " + personProcedure + " already exists.", ex);
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

    public void edit(PersonProcedure personProcedure) throws NonexistentEntityException, Exception
    {
        personProcedure.getPersonProcedurePK().setFkIdClientPerson(personProcedure.getPerson().getPersonId());
        personProcedure.getPersonProcedurePK().setFkIdProcedure(personProcedure.getProcedure().getIdProcedure());
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            PersonProcedure persistentPersonProcedure = em.find(PersonProcedure.class, personProcedure.getPersonProcedurePK());
            Person personOld = persistentPersonProcedure.getPerson();
            Person personNew = personProcedure.getPerson();
            Procedure procedureOld = persistentPersonProcedure.getProcedure();
            Procedure procedureNew = personProcedure.getProcedure();
            if (personNew != null)
            {
                personNew = em.getReference(personNew.getClass(), personNew.getPersonId());
                personProcedure.setPerson(personNew);
            }
            if (procedureNew != null)
            {
                procedureNew = em.getReference(procedureNew.getClass(), procedureNew.getIdProcedure());
                personProcedure.setProcedure(procedureNew);
            }
            personProcedure = em.merge(personProcedure);
            if (personOld != null && !personOld.equals(personNew))
            {
                personOld.getPersonProcedureList().remove(personProcedure);
                personOld = em.merge(personOld);
            }
            if (personNew != null && !personNew.equals(personOld))
            {
                personNew.getPersonProcedureList().add(personProcedure);
                personNew = em.merge(personNew);
            }
            if (procedureOld != null && !procedureOld.equals(procedureNew))
            {
                procedureOld.getPersonProcedureList().remove(personProcedure);
                procedureOld = em.merge(procedureOld);
            }
            if (procedureNew != null && !procedureNew.equals(procedureOld))
            {
                procedureNew.getPersonProcedureList().add(personProcedure);
                procedureNew = em.merge(procedureNew);
            }
            em.getTransaction().commit();
        }
        catch (Exception ex)
        {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0)
            {
                PersonProcedurePK id = personProcedure.getPersonProcedurePK();
                if (findPersonProcedure(id) == null)
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

    public void destroy(PersonProcedurePK id) throws NonexistentEntityException
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            PersonProcedure personProcedure;
            try
            {
                personProcedure = em.getReference(PersonProcedure.class, id);
                personProcedure.getPersonProcedurePK();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The tramitesPersonas with id " + id + " no longer exists.", enfe);
            }
            Person person = personProcedure.getPerson();
            if (person != null)
            {
                person.getPersonProcedureList().remove(personProcedure);
                person = em.merge(person);
            }
            Procedure procedure = personProcedure.getProcedure();
            if (procedure != null)
            {
                procedure.getPersonProcedureList().remove(personProcedure);
                procedure = em.merge(procedure);
            }
            em.remove(personProcedure);
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
    public int eliminarRecord(PersonProcedure record)
    {
        EntityManager em = getEntityManager();
        int rowCount = 0;
        try
        {
            PersonProcedure encontrado = (PersonProcedure) this.findClientProcedures(record.getPersonProcedurePK().getFkIdClientPerson(), record.getPersonProcedurePK().getFkIdProcedure()).get(0);

            if (encontrado != null)
            {

                em.getTransaction().begin();
                Query query = em.createQuery("DELETE FROM PersonProcedure t WHERE t.personProcedurePK.fkIdClientPerson = ?1 AND t.personProcedurePK.fkIdProcedure = ?2");
                query.setParameter(1, record.getPersonProcedurePK().getFkIdClientPerson());
                query.setParameter(2, record.getPersonProcedurePK().getFkIdProcedure());
                rowCount = query.executeUpdate();
                em.remove(record);
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

    public List<PersonProcedure> findPersonProcedureEntities()
    {
        return findPersonProcedureEntities(true, -1, -1);
    }

    public List<PersonProcedure> findPersonProcedureEntities(int maxResults, int firstResult)
    {
        return findPersonProcedureEntities(false, maxResults, firstResult);
    }

    private List<PersonProcedure> findPersonProcedureEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(PersonProcedure.class));
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

    public PersonProcedure findPersonProcedure(PersonProcedurePK id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(PersonProcedure.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getPersonProcedureCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<PersonProcedure> rt = cq.from(PersonProcedure.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public List<Procedure> findProceduresPerson(Integer idPerson)
    {
        EntityManager em = getEntityManager();
        List<Procedure> procedures = null;
        Query query = em.createNamedQuery("TramitesPersonas.findByFkIdPersonaCliente");
        query.setParameter("fkIdPersonaCliente", idPerson);

        procedures = query.getResultList();

        return procedures;
    }

    /**
     * Busca la relacion entre tramite y cliente (un registro en particualar)
     *
     * @param idPersona
     * @param idTramite
     * @return
     */
    public List<PersonProcedure> findClientProcedures(Integer idPerson, Integer idProcedure)
    {
        EntityManager em = getEntityManager();
        List<PersonProcedure> listaPersonProcedure = null;
        Query query = em.createNamedQuery("TramitesPersonas.findByTramiteCliente");
        query.setParameter("fkIdPersonaCliente", idPerson);
        query.setParameter("fkIdTramite", idProcedure);

        listaPersonProcedure = query.getResultList();

        return listaPersonProcedure;
    }

    @Override
    public String getNameJpa()
    {
        return this.getClass().getName();
    }
}
