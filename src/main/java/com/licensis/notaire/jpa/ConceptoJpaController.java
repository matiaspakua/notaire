/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.Concepto;
import com.licensis.notaire.negocio.PlantillaPresupuesto;

/**
 *
 * @author User
 */
public class ConceptoJpaController implements Serializable, IPersistenciaJpa
{

    private static ConceptoJpaController instancia = null;
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public ConceptoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public Integer create(Concepto concepto) {
        Integer oid = null;

        if (concepto.getPlantillaPresupuestoList() == null)
        {
            concepto.setPlantillaPresupuestoList(new ArrayList<PlantillaPresupuesto>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();

            List<PlantillaPresupuesto> attachedPlantillaPresupuestoList = new ArrayList<PlantillaPresupuesto>();
            for (PlantillaPresupuesto plantillaPresupuestoListPlantillaPresupuestoToAttach : concepto.getPlantillaPresupuestoList())
            {
                plantillaPresupuestoListPlantillaPresupuestoToAttach = em.getReference(plantillaPresupuestoListPlantillaPresupuestoToAttach.getClass(), plantillaPresupuestoListPlantillaPresupuestoToAttach.getPlantillaPresupuestoPK());
                attachedPlantillaPresupuestoList.add(plantillaPresupuestoListPlantillaPresupuestoToAttach);
            }
            concepto.setPlantillaPresupuestoList(attachedPlantillaPresupuestoList);

            em.persist(concepto);

            for (PlantillaPresupuesto plantillaPresupuestoListPlantillaPresupuesto : concepto.getPlantillaPresupuestoList())
            {
                Concepto oldConceptoOfPlantillaPresupuestoListPlantillaPresupuesto = plantillaPresupuestoListPlantillaPresupuesto.getConcepto();
                plantillaPresupuestoListPlantillaPresupuesto.setConcepto(concepto);
                plantillaPresupuestoListPlantillaPresupuesto = em.merge(plantillaPresupuestoListPlantillaPresupuesto);
                if (oldConceptoOfPlantillaPresupuestoListPlantillaPresupuesto != null)
                {
                    oldConceptoOfPlantillaPresupuestoListPlantillaPresupuesto.getPlantillaPresupuestoList().remove(plantillaPresupuestoListPlantillaPresupuesto);
                    oldConceptoOfPlantillaPresupuestoListPlantillaPresupuesto = em.merge(oldConceptoOfPlantillaPresupuestoListPlantillaPresupuesto);
                }
            }
            em.getTransaction().commit();

            oid = concepto.getIdConcepto();
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }

        return oid;
    }

    public Boolean edit(Concepto concepto) throws ClassEliminatedException, ClassModifiedException, IllegalOrphanException, NonexistentEntityException {
        Boolean resultado = Boolean.FALSE;
        Integer version = ConstantesPersistencia.VERSION_INICIAL;
        Integer oldVersion = ConstantesPersistencia.VERSION_INICIAL;
        EntityManager em = null;

        em = getEntityManager();
        em.getTransaction().begin();

        Concepto persistentConcepto = em.find(Concepto.class, concepto.getIdConcepto());

        if (persistentConcepto != null)
        {
            version = persistentConcepto.getVersion(); // Version del Objeto en db
            oldVersion = concepto.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                if (em != null)
                {
                    em.close();
                }

                throw new ClassModifiedException();

            }
            else
            {
                List<PlantillaPresupuesto> plantillaPresupuestoListOld = persistentConcepto.getPlantillaPresupuestoList();
                List<PlantillaPresupuesto> plantillaPresupuestoListNew = concepto.getPlantillaPresupuestoList();
                List<String> illegalOrphanMessages = null;
                for (PlantillaPresupuesto plantillaPresupuestoListOldPlantillaPresupuesto : plantillaPresupuestoListOld)
                {
                    if (!plantillaPresupuestoListNew.contains(plantillaPresupuestoListOldPlantillaPresupuesto))
                    {
                        if (illegalOrphanMessages == null)
                        {
                            illegalOrphanMessages = new ArrayList<String>();
                        }
                        illegalOrphanMessages.add("You must retain PlantillaPresupuesto " + plantillaPresupuestoListOldPlantillaPresupuesto + " since its concepto field is not nullable.");
                    }
                }
                if (illegalOrphanMessages != null)
                {
                    throw new IllegalOrphanException(illegalOrphanMessages);
                }
                List<PlantillaPresupuesto> attachedPlantillaPresupuestoListNew = new ArrayList<PlantillaPresupuesto>();
                for (PlantillaPresupuesto plantillaPresupuestoListNewPlantillaPresupuestoToAttach : plantillaPresupuestoListNew)
                {
                    plantillaPresupuestoListNewPlantillaPresupuestoToAttach = em.getReference(plantillaPresupuestoListNewPlantillaPresupuestoToAttach.getClass(), plantillaPresupuestoListNewPlantillaPresupuestoToAttach.getPlantillaPresupuestoPK());
                    attachedPlantillaPresupuestoListNew.add(plantillaPresupuestoListNewPlantillaPresupuestoToAttach);
                }
                plantillaPresupuestoListNew = attachedPlantillaPresupuestoListNew;
                concepto.setPlantillaPresupuestoList(plantillaPresupuestoListNew);
                concepto = em.merge(concepto);
                for (PlantillaPresupuesto plantillaPresupuestoListNewPlantillaPresupuesto : plantillaPresupuestoListNew)
                {
                    if (!plantillaPresupuestoListOld.contains(plantillaPresupuestoListNewPlantillaPresupuesto))
                    {
                        Concepto oldConceptoOfPlantillaPresupuestoListNewPlantillaPresupuesto = plantillaPresupuestoListNewPlantillaPresupuesto.getConcepto();
                        plantillaPresupuestoListNewPlantillaPresupuesto.setConcepto(concepto);
                        plantillaPresupuestoListNewPlantillaPresupuesto = em.merge(plantillaPresupuestoListNewPlantillaPresupuesto);
                        if (oldConceptoOfPlantillaPresupuestoListNewPlantillaPresupuesto != null && !oldConceptoOfPlantillaPresupuestoListNewPlantillaPresupuesto.equals(concepto))
                        {
                            oldConceptoOfPlantillaPresupuestoListNewPlantillaPresupuesto.getPlantillaPresupuestoList().remove(plantillaPresupuestoListNewPlantillaPresupuesto);
                            oldConceptoOfPlantillaPresupuestoListNewPlantillaPresupuesto = em.merge(oldConceptoOfPlantillaPresupuestoListNewPlantillaPresupuesto);
                        }
                    }
                }

                em.getTransaction().commit();
                resultado = Boolean.TRUE;

                if (em != null)
                {
                    em.close();
                }
            }
        }
        else
        {
            throw new ClassEliminatedException();
        }

        return resultado;
    }

    public Boolean destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, ClassEliminatedException {
        Boolean resultado = Boolean.FALSE;

        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();

            Concepto persistentConcepto = em.find(Concepto.class, id);

            if (persistentConcepto != null)
            {
                Concepto concepto;

                concepto = em.getReference(Concepto.class, id);
                concepto.getIdConcepto();
                List<String> illegalOrphanMessages = null;
                List<PlantillaPresupuesto> plantillaPresupuestoListOrphanCheck = concepto.getPlantillaPresupuestoList();
                for (PlantillaPresupuesto plantillaPresupuestoListOrphanCheckPlantillaPresupuesto : plantillaPresupuestoListOrphanCheck)
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("This Concepto (" + concepto + ") cannot be destroyed since the PlantillaPresupuesto " + plantillaPresupuestoListOrphanCheckPlantillaPresupuesto + " in its plantillaPresupuestoList field has a non-nullable concepto field.");
                }
                if (illegalOrphanMessages != null)
                {
                    throw new IllegalOrphanException(illegalOrphanMessages);
                }

                em.remove(concepto);
                em.getTransaction().commit();
                resultado = Boolean.TRUE;
            }
            else
            {
                throw new ClassEliminatedException();
            }
        }
        catch (EntityNotFoundException enfe)
        {
            throw new NonexistentEntityException("The concepto with id " + id + " no longer exists.", enfe);
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

    public List<Concepto> findConceptoEntities() {
        return findConceptoEntities(true, -1, -1);
    }

    public List<Concepto> findConceptoEntities(int maxResults, int firstResult) {
        return findConceptoEntities(false, maxResults, firstResult);
    }

    private List<Concepto> findConceptoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from Concepto as o");
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

    public List<Concepto> findConceptoByNombre(String nombreConcepto) {
        
        EntityManager em = getEntityManager();
        List<Concepto> listaConcepto = new ArrayList<>();

        Query query = em.createNamedQuery("Concepto.findByNombre");
        query.setParameter("nombre", nombreConcepto);
        
        listaConcepto = query.getResultList();

        return listaConcepto;
    }

    public Concepto findConcepto(Integer id) {
        EntityManager em = getEntityManager();


        try
        {
            return em.find(Concepto.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getConceptoCount() {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from Concepto as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    @Override
    public String getNombreJpa() {
        return this.getClass().getName();
    }
}
