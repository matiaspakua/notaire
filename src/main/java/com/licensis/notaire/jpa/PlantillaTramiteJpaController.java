/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;
import javax.transaction.UserTransaction;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.exceptions.PreexistingEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.PlantillaTramite;
import com.licensis.notaire.negocio.PlantillaTramitePK;
import com.licensis.notaire.negocio.TipoDeDocumento;
import com.licensis.notaire.negocio.TipoDeTramite;

/**
 *
 * @author User
 */
public class PlantillaTramiteJpaController implements Serializable, IPersistenciaJpa
{

    public PlantillaTramiteJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public Boolean create(PlantillaTramite plantillaTramite) throws PreexistingEntityException {
        Boolean creada = false;
        if (plantillaTramite.getPlantillaTramitePK() == null)
        {
            plantillaTramite.setPlantillaTramitePK(new PlantillaTramitePK());
        }
        plantillaTramite.getPlantillaTramitePK().setFkIdTipoTramite(plantillaTramite.getTipoDeTramite().getIdTipoTramite());
        plantillaTramite.getPlantillaTramitePK().setFkIdTipoDocumento(plantillaTramite.getTipoDeDocumento().getIdTipoDocumento());
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            TipoDeTramite tipoDeTramite = plantillaTramite.getTipoDeTramite();
            if (tipoDeTramite != null)
            {
                tipoDeTramite = em.getReference(tipoDeTramite.getClass(), tipoDeTramite.getIdTipoTramite());
                plantillaTramite.setTipoDeTramite(tipoDeTramite);
            }
            TipoDeDocumento tipoDeDocumento = plantillaTramite.getTipoDeDocumento();
            if (tipoDeDocumento != null)
            {
                tipoDeDocumento = em.getReference(tipoDeDocumento.getClass(), tipoDeDocumento.getIdTipoDocumento());
                plantillaTramite.setTipoDeDocumento(tipoDeDocumento);
            }
            em.persist(plantillaTramite);
            if (tipoDeTramite != null)
            {
                tipoDeTramite.getPlantillaTramiteList().add(plantillaTramite);
                tipoDeTramite = em.merge(tipoDeTramite);
            }
            if (tipoDeDocumento != null)
            {
                tipoDeDocumento.getPlantillaTramiteList().add(plantillaTramite);
                tipoDeDocumento = em.merge(tipoDeDocumento);
            }
            em.getTransaction().commit();
            creada = true;
        }
        catch (Exception ex)
        {
            if (findPlantillaTramite(plantillaTramite.getPlantillaTramitePK()) != null)
            {
                throw new PreexistingEntityException("PlantillaTramite " + plantillaTramite + " already exists.", ex);
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
        return creada;
    }

    public void edit(PlantillaTramite plantillaTramite) throws NonexistentEntityException, Exception {
        plantillaTramite.getPlantillaTramitePK().setFkIdTipoTramite(plantillaTramite.getTipoDeTramite().getIdTipoTramite());
        plantillaTramite.getPlantillaTramitePK().setFkIdTipoDocumento(plantillaTramite.getTipoDeDocumento().getIdTipoDocumento());
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            PlantillaTramite persistentPlantillaTramite = em.find(PlantillaTramite.class, plantillaTramite.getPlantillaTramitePK());
            TipoDeTramite tipoDeTramiteOld = persistentPlantillaTramite.getTipoDeTramite();
            TipoDeTramite tipoDeTramiteNew = plantillaTramite.getTipoDeTramite();
            TipoDeDocumento tipoDeDocumentoOld = persistentPlantillaTramite.getTipoDeDocumento();
            TipoDeDocumento tipoDeDocumentoNew = plantillaTramite.getTipoDeDocumento();
            if (tipoDeTramiteNew != null)
            {
                tipoDeTramiteNew = em.getReference(tipoDeTramiteNew.getClass(), tipoDeTramiteNew.getIdTipoTramite());
                plantillaTramite.setTipoDeTramite(tipoDeTramiteNew);
            }
            if (tipoDeDocumentoNew != null)
            {
                tipoDeDocumentoNew = em.getReference(tipoDeDocumentoNew.getClass(), tipoDeDocumentoNew.getIdTipoDocumento());
                plantillaTramite.setTipoDeDocumento(tipoDeDocumentoNew);
            }
            plantillaTramite = em.merge(plantillaTramite);
            if (tipoDeTramiteOld != null && !tipoDeTramiteOld.equals(tipoDeTramiteNew))
            {
                tipoDeTramiteOld.getPlantillaTramiteList().remove(plantillaTramite);
                tipoDeTramiteOld = em.merge(tipoDeTramiteOld);
            }
            if (tipoDeTramiteNew != null && !tipoDeTramiteNew.equals(tipoDeTramiteOld))
            {
                tipoDeTramiteNew.getPlantillaTramiteList().add(plantillaTramite);
                tipoDeTramiteNew = em.merge(tipoDeTramiteNew);
            }
            if (tipoDeDocumentoOld != null && !tipoDeDocumentoOld.equals(tipoDeDocumentoNew))
            {
                tipoDeDocumentoOld.getPlantillaTramiteList().remove(plantillaTramite);
                tipoDeDocumentoOld = em.merge(tipoDeDocumentoOld);
            }
            if (tipoDeDocumentoNew != null && !tipoDeDocumentoNew.equals(tipoDeDocumentoOld))
            {
                tipoDeDocumentoNew.getPlantillaTramiteList().add(plantillaTramite);
                tipoDeDocumentoNew = em.merge(tipoDeDocumentoNew);
            }
            em.getTransaction().commit();
        }
        catch (Exception ex)
        {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0)
            {
                PlantillaTramitePK id = plantillaTramite.getPlantillaTramitePK();
                if (findPlantillaTramite(id) == null)
                {
                    throw new NonexistentEntityException("The plantillaTramite with id " + id + " no longer exists.");
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

    public Boolean destroy(PlantillaTramitePK id) throws NonexistentEntityException {
        EntityManager em = null;
        Boolean eliminado = false;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            PlantillaTramite plantillaTramite;
            try
            {
                plantillaTramite = em.getReference(PlantillaTramite.class, id);
                plantillaTramite.getPlantillaTramitePK();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The plantillaTramite with id " + id + " no longer exists.", enfe);
            }
            TipoDeTramite tipoDeTramite = plantillaTramite.getTipoDeTramite();
            if (tipoDeTramite != null)
            {
                tipoDeTramite.getPlantillaTramiteList().remove(plantillaTramite);
                tipoDeTramite = em.merge(tipoDeTramite);
            }
            TipoDeDocumento tipoDeDocumento = plantillaTramite.getTipoDeDocumento();
            if (tipoDeDocumento != null)
            {
                tipoDeDocumento.getPlantillaTramiteList().remove(plantillaTramite);
                tipoDeDocumento = em.merge(tipoDeDocumento);
            }
            em.remove(plantillaTramite);
            em.getTransaction().commit();
            eliminado = true;
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

    public List<PlantillaTramite> findPlantillaTramiteEntities() {
        return findPlantillaTramiteEntities(true, -1, -1);
    }

    public List<PlantillaTramite> findPlantillaTramiteEntities(int maxResults, int firstResult) {
        return findPlantillaTramiteEntities(false, maxResults, firstResult);
    }

    private List<PlantillaTramite> findPlantillaTramiteEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from PlantillaTramite as o");
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

    public PlantillaTramite findPlantillaTramite(PlantillaTramitePK id) {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(PlantillaTramite.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getPlantillaTramiteCount() {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from PlantillaTramite as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    /**
     * Busca y encuentra todas las Plantillas de Tramite de un Tipo de Tramite en particular.
     *
     * @param idTipoTramite, Id del Tipo de Tramite al cual pertenecen las Plantillas de Tramite a
     * buscar.
     * @return Una lista de las Plantillas de Tramite encontradas.
     */
    public List<PlantillaTramite> findPlantillasDeTramite(int idTipoTramite) {
        EntityManager em = getEntityManager();

        List<PlantillaTramite> misPlantillas = null;

        try
        {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("PlantillaTramite.findByFkIdTipoTramite");

            //Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("fkIdTipoTramite", idTipoTramite);

            misPlantillas = (List<PlantillaTramite>) q.getResultList();

            return misPlantillas;

        }
        finally
        {
            em.close();
        }
    }

    /**
     * Elimina una Plantilla de Tramite mediante un query DELETE.
     *
     * @param miPlantilla, la Plantilla de Tramite a eliminar.
     * @return True si se elimino, False de lo contrario.
     */
    public Boolean eliminarPlantillaTramite(PlantillaTramite miPlantilla) {

        Boolean eliminada = false;
        int deleted = 0;

        EntityManager em = this.getEntityManager();
        EntityTransaction tx = null;

        // Abro una nueva transaccion
        tx = em.getTransaction();
        tx.begin();

        // Creo el query
        Query query = em.createQuery("DELETE FROM PlantillaTramite p WHERE p.plantillaTramitePK.fkIdTipoTramite = ?1 AND  p.plantillaTramitePK.fkIdTipoDocumento = ?2");

        //Seteo el parametro 1 y 2
        query.setParameter(1, miPlantilla.getPlantillaTramitePK().getFkIdTipoTramite());
        query.setParameter(2, miPlantilla.getPlantillaTramitePK().getFkIdTipoDocumento());

        // Ejecuto el query
        deleted = query.executeUpdate();

        // Hago commit
        tx.commit();

        em.close();

        if (deleted > 0)
        {
            eliminada = true;
        }

        return eliminada;

    }

    @Override
    public String getNombreJpa() {
        return this.getClass().getName();
    }

    public List<PlantillaTramite> findPlantillasTramites() {

        EntityManager em = getEntityManager();

        List<PlantillaTramite> listaPlantillaTramites = null;

        Query query = em.createNamedQuery("PlantillaTramite.findAll");
        listaPlantillaTramites = query.getResultList();

        return listaPlantillaTramites;
    }
}
