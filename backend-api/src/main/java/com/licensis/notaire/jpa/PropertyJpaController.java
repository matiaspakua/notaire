/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.dto.DtoProperty;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.business.Procedure;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;

/**
 *
 * @author juanca
 */
public class PropertyJpaController implements Serializable, IPersistenciaJpa
{

    public PropertyJpaController(UserTransaction utx, EntityManagerFactory emf)
    {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager()
    {
        return emf.createEntityManager();
    }

    public int create(Property property)
    {
        int id = -1;
        if (property.getProcedureList() == null)
        {
            property.setProcedureList(new ArrayList<Procedure>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Procedure> attachedProcedureList = new ArrayList<Procedure>();
            for (Procedure procedureListProcedureToAttach : property.getProcedureList())
            {
                procedureListProcedureToAttach = em.getReference(procedureListProcedureToAttach.getClass(), procedureListProcedureToAttach.getIdProcedure());
                attachedProcedureList.add(procedureListProcedureToAttach);
            }
            property.setProcedureList(attachedProcedureList);
            em.persist(property);
            for (Procedure procedureListProcedure : property.getProcedureList())
            {
                Property oldFkIdPropertyOfProcedureListProcedure = procedureListProcedure.getFkIdProperty();
                procedureListProcedure.setFkIdProperty(property);
                procedureListProcedure = em.merge(procedureListProcedure);
                if (oldFkIdPropertyOfProcedureListProcedure != null)
                {
                    oldFkIdPropertyOfProcedureListProcedure.getProcedureList().remove(procedureListProcedure);
                    oldFkIdPropertyOfProcedureListProcedure = em.merge(oldFkIdPropertyOfProcedureListProcedure);
                }
            }
            em.getTransaction().commit();
            id = property.getIdProperty();
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

    public void edit(Property property) throws NonexistentEntityException, Exception
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Property persistentProperty = em.find(Property.class, property.getIdProperty());
            List<Procedure> procedureListOld = persistentProperty.getProcedureList();
            List<Procedure> procedureListNew = property.getProcedureList();
            List<Procedure> attachedProcedureListNew = new ArrayList<Procedure>();
            for (Procedure procedureListNewProcedureToAttach : procedureListNew)
            {
                procedureListNewProcedureToAttach = em.getReference(procedureListNewProcedureToAttach.getClass(), procedureListNewProcedureToAttach.getIdProcedure());
                attachedProcedureListNew.add(procedureListNewProcedureToAttach);
            }
            procedureListNew = attachedProcedureListNew;
            property.setProcedureList(procedureListNew);
            property = em.merge(property);
            for (Procedure procedureListOldProcedure : procedureListOld)
            {
                if (!procedureListNew.contains(procedureListOldProcedure))
                {
                    procedureListOldProcedure.setFkIdProperty(null);
                    procedureListOldProcedure = em.merge(procedureListOldProcedure);
                }
            }
            for (Procedure procedureListNewProcedure : procedureListNew)
            {
                if (!procedureListOld.contains(procedureListNewProcedure))
                {
                    Property oldFkIdPropertyOfProcedureListNewProcedure = procedureListNewProcedure.getFkIdProperty();
                    procedureListNewProcedure.setFkIdProperty(property);
                    procedureListNewProcedure = em.merge(procedureListNewProcedure);
                    if (oldFkIdPropertyOfProcedureListNewProcedure != null && !oldFkIdPropertyOfProcedureListNewProcedure.equals(property))
                    {
                        oldFkIdPropertyOfProcedureListNewProcedure.getProcedureList().remove(procedureListNewProcedure);
                        oldFkIdPropertyOfProcedureListNewProcedure = em.merge(oldFkIdPropertyOfProcedureListNewProcedure);
                    }
                }
            }
            em.getTransaction().commit();
        }
        catch (Exception ex)
        {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0)
            {
                Integer id = property.getIdProperty();
                if (findProperty(id) == null)
                {
                    throw new NonexistentEntityException("The inmueble with id " + id + " no longer exists.");
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

    public void destroy(Integer id) throws NonexistentEntityException
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Property property;
            try
            {
                property = em.getReference(Property.class, id);
                property.getIdProperty();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The inmueble with id " + id + " no longer exists.", enfe);
            }
            List<Procedure> procedureList = property.getProcedureList();
            for (Procedure procedureListProcedure : procedureList)
            {
                procedureListProcedure.setFkIdProperty(null);
                procedureListProcedure = em.merge(procedureListProcedure);
            }
            em.remove(property);
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

    public List<Property> findPropertyEntities()
    {
        return findPropertyEntities(true, -1, -1);
    }

    public List<Property> findPropertyEntities(int maxResults, int firstResult)
    {
        return findPropertyEntities(false, maxResults, firstResult);
    }

    private List<Property> findPropertyEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from Inmueble as o");
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

    public Property findProperty(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(Property.class, id);
        }
        finally
        {
            em.close();
        }
    }

    /**
     * Busca un inmueble por su nomenclatura catastral.
     *
     * @param dtoInmueble
     * @return el Inmueble encontrado.
     */
    public Property findProperty(DtoProperty dtoProperty)
    {

        EntityManager em = getEntityManager();

        List<Property> properties = null;
        Property miProperty = null;

        Query query = em.createNamedQuery("Inmueble.findByNomenclatura");
        query.setParameter("nomenclatura", dtoProperty.getCadastralDesignation());

        properties = query.getResultList();

        if (properties != null && !properties.isEmpty())
        {
            miProperty = properties.get(0);
        }

        return miProperty;
    }

    public int getPropertyCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from Inmueble as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    @Override
    public String getNameJpa()
    {
        return this.getClass().getName();
    }
}
