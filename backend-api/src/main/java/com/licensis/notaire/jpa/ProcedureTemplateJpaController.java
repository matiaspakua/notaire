/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.exceptions.PreexistingEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.ProcedureTemplatePK;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.ProcedureType;
import java.io.Serializable;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;

/**
 *
 * @author User
 */
public class ProcedureTemplateJpaController implements Serializable, IPersistenciaJpa
{

    public ProcedureTemplateJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public Boolean create(ProcedureTemplate procedureTemplate) throws PreexistingEntityException
    {
        Boolean creada = false;
        if (procedureTemplate.getProcedureTemplatePK() == null)
        {
            procedureTemplate.setProcedureTemplatePK(new ProcedureTemplatePK());
        }
        procedureTemplate.getProcedureTemplatePK().setFkIdProcedureType(procedureTemplate.getProcedureType().getIdProcedureType());
        procedureTemplate.getProcedureTemplatePK().setFkIdDocumentType(procedureTemplate.getDocumentType().getIdDocumentType());
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            ProcedureType procedureType = procedureTemplate.getProcedureType();
            if (procedureType != null)
            {
                procedureType = em.getReference(procedureType.getClass(), procedureType.getIdProcedureType());
                procedureTemplate.setProcedureType(procedureType);
            }
            DocumentType documentType = procedureTemplate.getDocumentType();
            if (documentType != null)
            {
                documentType = em.getReference(documentType.getClass(), documentType.getIdDocumentType());
                procedureTemplate.setDocumentType(documentType);
            }
            em.persist(procedureTemplate);
            if (procedureType != null)
            {
                procedureType.getProcedureTemplateList().add(procedureTemplate);
                procedureType = em.merge(procedureType);
            }
            if (documentType != null)
            {
                documentType.getProcedureTemplateList().add(procedureTemplate);
                documentType = em.merge(documentType);
            }
            em.getTransaction().commit();
            creada = true;
        }
        catch (Exception ex)
        {
            if (findProcedureTemplate(procedureTemplate.getProcedureTemplatePK()) != null)
            {
                throw new PreexistingEntityException("PlantillaTramite " + procedureTemplate + " already exists.", ex);
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

    public void edit(ProcedureTemplate procedureTemplate) throws NonexistentEntityException, Exception
    {
        procedureTemplate.getProcedureTemplatePK().setFkIdProcedureType(procedureTemplate.getProcedureType().getIdProcedureType());
        procedureTemplate.getProcedureTemplatePK().setFkIdDocumentType(procedureTemplate.getDocumentType().getIdDocumentType());
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            ProcedureTemplate persistentProcedureTemplate = em.find(ProcedureTemplate.class, procedureTemplate.getProcedureTemplatePK());
            ProcedureType procedureTypeOld = persistentProcedureTemplate.getProcedureType();
            ProcedureType procedureTypeNew = procedureTemplate.getProcedureType();
            DocumentType documentTypeOld = persistentProcedureTemplate.getDocumentType();
            DocumentType documentTypeNew = procedureTemplate.getDocumentType();
            if (procedureTypeNew != null)
            {
                procedureTypeNew = em.getReference(procedureTypeNew.getClass(), procedureTypeNew.getIdProcedureType());
                procedureTemplate.setProcedureType(procedureTypeNew);
            }
            if (documentTypeNew != null)
            {
                documentTypeNew = em.getReference(documentTypeNew.getClass(), documentTypeNew.getIdDocumentType());
                procedureTemplate.setDocumentType(documentTypeNew);
            }
            procedureTemplate = em.merge(procedureTemplate);
            if (procedureTypeOld != null && !procedureTypeOld.equals(procedureTypeNew))
            {
                procedureTypeOld.getProcedureTemplateList().remove(procedureTemplate);
                procedureTypeOld = em.merge(procedureTypeOld);
            }
            if (procedureTypeNew != null && !procedureTypeNew.equals(procedureTypeOld))
            {
                procedureTypeNew.getProcedureTemplateList().add(procedureTemplate);
                procedureTypeNew = em.merge(procedureTypeNew);
            }
            if (documentTypeOld != null && !documentTypeOld.equals(documentTypeNew))
            {
                documentTypeOld.getProcedureTemplateList().remove(procedureTemplate);
                documentTypeOld = em.merge(documentTypeOld);
            }
            if (documentTypeNew != null && !documentTypeNew.equals(documentTypeOld))
            {
                documentTypeNew.getProcedureTemplateList().add(procedureTemplate);
                documentTypeNew = em.merge(documentTypeNew);
            }
            em.getTransaction().commit();
        }
        catch (Exception ex)
        {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0)
            {
                ProcedureTemplatePK id = procedureTemplate.getProcedureTemplatePK();
                if (findProcedureTemplate(id) == null)
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

    public Boolean destroy(ProcedureTemplatePK id) throws NonexistentEntityException
    {
        EntityManager em = null;
        Boolean eliminado = false;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            ProcedureTemplate procedureTemplate;
            try
            {
                procedureTemplate = em.getReference(ProcedureTemplate.class, id);
                procedureTemplate.getProcedureTemplatePK();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The plantillaTramite with id " + id + " no longer exists.", enfe);
            }
            ProcedureType procedureType = procedureTemplate.getProcedureType();
            if (procedureType != null)
            {
                procedureType.getProcedureTemplateList().remove(procedureTemplate);
                procedureType = em.merge(procedureType);
            }
            DocumentType documentType = procedureTemplate.getDocumentType();
            if (documentType != null)
            {
                documentType.getProcedureTemplateList().remove(procedureTemplate);
                documentType = em.merge(documentType);
            }
            em.remove(procedureTemplate);
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

    public List<ProcedureTemplate> findProcedureTemplateEntities()
    {
        return findProcedureTemplateEntities(true, -1, -1);
    }

    public List<ProcedureTemplate> findProcedureTemplateEntities(int maxResults, int firstResult)
    {
        return findProcedureTemplateEntities(false, maxResults, firstResult);
    }

    private List<ProcedureTemplate> findProcedureTemplateEntities(boolean all, int maxResults, int firstResult)
    {
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

    public ProcedureTemplate findProcedureTemplate(ProcedureTemplatePK id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(ProcedureTemplate.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getProcedureTemplateCount()
    {
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
    public List<ProcedureTemplate> findPlantillasDeProcedure(int idProcedureType)
    {
        EntityManager em = getEntityManager();

        List<ProcedureTemplate> misPlantillas = null;

        try
        {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("PlantillaTramite.findByFkIdTipoTramite");

            //Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("fkIdTipoTramite", idProcedureType);

            misPlantillas = (List<ProcedureTemplate>) q.getResultList();

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
    public Boolean eliminarProcedureTemplate(ProcedureTemplate miTemplate)
    {

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
        query.setParameter(1, miTemplate.getProcedureTemplatePK().getFkIdProcedureType());
        query.setParameter(2, miTemplate.getProcedureTemplatePK().getFkIdDocumentType());

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
    public String getNameJpa()
    {
        return this.getClass().getName();
    }

    public List<ProcedureTemplate> findPlantillasProcedures()
    {

        EntityManager em = getEntityManager();

        List<ProcedureTemplate> listaTemplateProcedures = null;

        Query query = em.createNamedQuery("PlantillaTramite.findAll");
        listaTemplateProcedures = query.getResultList();

        return listaTemplateProcedures;
    }
}
