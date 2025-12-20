/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.PlantillaTramite;
import com.licensis.notaire.negocio.TipoDeDocumento;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

/**
 *
 * @author User
 */
public class TipoDeDocumentoJpaController implements Serializable, IPersistenciaJpa
{

    public TipoDeDocumentoJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public int create(TipoDeDocumento tipoDeDocumento)
    {

        int oid = -1;

        if (tipoDeDocumento.getPlantillaTramiteList() == null)
        {
            tipoDeDocumento.setPlantillaTramiteList(new ArrayList<PlantillaTramite>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            List<PlantillaTramite> attachedPlantillaTramiteList = new ArrayList<PlantillaTramite>();
            for (PlantillaTramite plantillaTramiteListPlantillaTramiteToAttach : tipoDeDocumento.getPlantillaTramiteList())
            {
                plantillaTramiteListPlantillaTramiteToAttach = em.getReference(plantillaTramiteListPlantillaTramiteToAttach.getClass(), plantillaTramiteListPlantillaTramiteToAttach.getPlantillaTramitePK());
                attachedPlantillaTramiteList.add(plantillaTramiteListPlantillaTramiteToAttach);
            }
            tipoDeDocumento.setPlantillaTramiteList(attachedPlantillaTramiteList);
            em.persist(tipoDeDocumento);
            for (PlantillaTramite plantillaTramiteListPlantillaTramite : tipoDeDocumento.getPlantillaTramiteList())
            {
                TipoDeDocumento oldTipoDeDocumentoOfPlantillaTramiteListPlantillaTramite = plantillaTramiteListPlantillaTramite.getTipoDeDocumento();
                plantillaTramiteListPlantillaTramite.setTipoDeDocumento(tipoDeDocumento);
                plantillaTramiteListPlantillaTramite = em.merge(plantillaTramiteListPlantillaTramite);
                if (oldTipoDeDocumentoOfPlantillaTramiteListPlantillaTramite != null)
                {
                    oldTipoDeDocumentoOfPlantillaTramiteListPlantillaTramite.getPlantillaTramiteList().remove(plantillaTramiteListPlantillaTramite);
                    oldTipoDeDocumentoOfPlantillaTramiteListPlantillaTramite = em.merge(oldTipoDeDocumentoOfPlantillaTramiteListPlantillaTramite);
                }
            }
            em.getTransaction().commit();

            oid = tipoDeDocumento.getIdTipoDocumento();
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

    public Boolean edit(TipoDeDocumento tipoDeDocumento) throws ClassEliminatedException, ClassModifiedException, IllegalOrphanException, NonexistentEntityException
    {
        EntityManager em = null;
        Boolean modificado = false;
        Integer version = -1;
        Integer oldVersion = -1;

        em = getEntityManager();
        em.getTransaction().begin();

        TipoDeDocumento persistentTipoDeDocumento = em.find(TipoDeDocumento.class, tipoDeDocumento.getIdTipoDocumento());

        if (persistentTipoDeDocumento != null)
        {
            version = persistentTipoDeDocumento.getVersion(); // Version del Objeto en db
            oldVersion = tipoDeDocumento.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                if (em != null)
                {
                    em.close();
                }

                throw new ClassModifiedException();

            } else
            {

                List<PlantillaTramite> plantillaTramiteListOld = persistentTipoDeDocumento.getPlantillaTramiteList();
                List<PlantillaTramite> plantillaTramiteListNew = tipoDeDocumento.getPlantillaTramiteList();
                List<String> illegalOrphanMessages = null;
                for (PlantillaTramite plantillaTramiteListOldPlantillaTramite : plantillaTramiteListOld)
                {
                    if (!plantillaTramiteListNew.contains(plantillaTramiteListOldPlantillaTramite))
                    {
                        if (illegalOrphanMessages == null)
                        {
                            illegalOrphanMessages = new ArrayList<String>();
                        }
                        illegalOrphanMessages.add("You must retain PlantillaTramite " + plantillaTramiteListOldPlantillaTramite + " since its tipoDeDocumento field is not nullable.");
                    }
                }
                if (illegalOrphanMessages != null)
                {
                    throw new IllegalOrphanException(illegalOrphanMessages);
                }
                List<PlantillaTramite> attachedPlantillaTramiteListNew = new ArrayList<PlantillaTramite>();
                for (PlantillaTramite plantillaTramiteListNewPlantillaTramiteToAttach : plantillaTramiteListNew)
                {
                    plantillaTramiteListNewPlantillaTramiteToAttach = em.getReference(plantillaTramiteListNewPlantillaTramiteToAttach.getClass(), plantillaTramiteListNewPlantillaTramiteToAttach.getPlantillaTramitePK());
                    attachedPlantillaTramiteListNew.add(plantillaTramiteListNewPlantillaTramiteToAttach);
                }
                plantillaTramiteListNew = attachedPlantillaTramiteListNew;
                tipoDeDocumento.setPlantillaTramiteList(plantillaTramiteListNew);
                tipoDeDocumento = em.merge(tipoDeDocumento);
                for (PlantillaTramite plantillaTramiteListNewPlantillaTramite : plantillaTramiteListNew)
                {
                    if (!plantillaTramiteListOld.contains(plantillaTramiteListNewPlantillaTramite))
                    {
                        TipoDeDocumento oldTipoDeDocumentoOfPlantillaTramiteListNewPlantillaTramite = plantillaTramiteListNewPlantillaTramite.getTipoDeDocumento();
                        plantillaTramiteListNewPlantillaTramite.setTipoDeDocumento(tipoDeDocumento);
                        plantillaTramiteListNewPlantillaTramite = em.merge(plantillaTramiteListNewPlantillaTramite);
                        if (oldTipoDeDocumentoOfPlantillaTramiteListNewPlantillaTramite != null && !oldTipoDeDocumentoOfPlantillaTramiteListNewPlantillaTramite.equals(tipoDeDocumento))
                        {
                            oldTipoDeDocumentoOfPlantillaTramiteListNewPlantillaTramite.getPlantillaTramiteList().remove(plantillaTramiteListNewPlantillaTramite);
                            oldTipoDeDocumentoOfPlantillaTramiteListNewPlantillaTramite = em.merge(oldTipoDeDocumentoOfPlantillaTramiteListNewPlantillaTramite);
                        }
                    }
                }

                em.getTransaction().commit();
                modificado = true;
            }
        } else
        {
            throw new ClassEliminatedException();
        }

        if (em != null)
        {
            em.close();
        }

        return modificado;
    }

    public Boolean destroy(Integer id) throws ClassEliminatedException, IllegalOrphanException, NonexistentEntityException
    {
        EntityManager em = null;
        Boolean eliminado = false;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();

            TipoDeDocumento persistentDocumento = em.find(TipoDeDocumento.class, id);

            if (persistentDocumento != null)
            {

                TipoDeDocumento tipoDeDocumento;
                try
                {
                    tipoDeDocumento = em.getReference(TipoDeDocumento.class, id);
                    tipoDeDocumento.getIdTipoDocumento();
                }
                catch (EntityNotFoundException enfe)
                {
                    throw new NonexistentEntityException("The tipoDeDocumento with id " + id + " no longer exists.", enfe);
                }
                List<String> illegalOrphanMessages = null;
                List<PlantillaTramite> plantillaTramiteListOrphanCheck = tipoDeDocumento.getPlantillaTramiteList();
                for (PlantillaTramite plantillaTramiteListOrphanCheckPlantillaTramite : plantillaTramiteListOrphanCheck)
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("This TipoDeDocumento (" + tipoDeDocumento + ") cannot be destroyed since the PlantillaTramite " + plantillaTramiteListOrphanCheckPlantillaTramite + " in its plantillaTramiteList field has a non-nullable tipoDeDocumento field.");
                }
                if (illegalOrphanMessages != null)
                {
                    throw new IllegalOrphanException(illegalOrphanMessages);
                }
                em.remove(tipoDeDocumento);
                em.getTransaction().commit();
                eliminado = true;
            } else
            {
                throw new ClassEliminatedException();
            }
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }

        return eliminado;
    }

    public List<TipoDeDocumento> findTipoDeDocumentoEntities()
    {
        return findTipoDeDocumentoEntities(true, -1, -1);
    }

    public List<TipoDeDocumento> findTipoDeDocumentoEntities(int maxResults, int firstResult)
    {
        return findTipoDeDocumentoEntities(false, maxResults, firstResult);
    }

    private List<TipoDeDocumento> findTipoDeDocumentoEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from TipoDeDocumento as o");
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

    public TipoDeDocumento findTipoDeDocumento(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(TipoDeDocumento.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public List<TipoDeDocumento> findTipoDeDocumento(String nombre)
    {
        EntityManager em = getEntityManager();

        List<TipoDeDocumento> miTipoDeDocumento = null;

        try
        {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("TipoDeDocumento.findByNombre");

            //Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("nombre", nombre);

            miTipoDeDocumento = (List<TipoDeDocumento>) q.getResultList();

            return miTipoDeDocumento;

        }
        finally
        {
            em.close();
        }
    }

    public int getTipoDeDocumentoCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from TipoDeDocumento as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    @Override
    public String getNombreJpa()
    {
        return this.getClass().getName();
    }
}
