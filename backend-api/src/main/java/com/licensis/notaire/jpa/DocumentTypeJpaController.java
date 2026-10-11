/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.JpaPersistence;
import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.DocumentType;
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
 * @author User
 */
public class DocumentTypeJpaController implements Serializable, JpaPersistence
{

    public DocumentTypeJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public int create(DocumentType documentType)
    {

        int oid = -1;

        if (documentType.getProcedureTemplateList() == null)
        {
            documentType.setProcedureTemplateList(new ArrayList<ProcedureTemplate>());
        }
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            List<ProcedureTemplate> attachedProcedureTemplateList = new ArrayList<ProcedureTemplate>();
            for (ProcedureTemplate procedureTemplateListProcedureTemplateToAttach : documentType.getProcedureTemplateList())
            {
                procedureTemplateListProcedureTemplateToAttach = em.getReference(procedureTemplateListProcedureTemplateToAttach.getClass(), procedureTemplateListProcedureTemplateToAttach.getProcedureTemplatePK());
                attachedProcedureTemplateList.add(procedureTemplateListProcedureTemplateToAttach);
            }
            documentType.setProcedureTemplateList(attachedProcedureTemplateList);
            em.persist(documentType);
            for (ProcedureTemplate procedureTemplateListProcedureTemplate : documentType.getProcedureTemplateList())
            {
                DocumentType oldDocumentTypeOfProcedureTemplateListProcedureTemplate = procedureTemplateListProcedureTemplate.getDocumentType();
                procedureTemplateListProcedureTemplate.setDocumentType(documentType);
                procedureTemplateListProcedureTemplate = em.merge(procedureTemplateListProcedureTemplate);
                if (oldDocumentTypeOfProcedureTemplateListProcedureTemplate != null)
                {
                    oldDocumentTypeOfProcedureTemplateListProcedureTemplate.getProcedureTemplateList().remove(procedureTemplateListProcedureTemplate);
                    oldDocumentTypeOfProcedureTemplateListProcedureTemplate = em.merge(oldDocumentTypeOfProcedureTemplateListProcedureTemplate);
                }
            }
            em.getTransaction().commit();

            oid = documentType.getIdDocumentType();
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

    public Boolean edit(DocumentType documentType) throws ClassEliminatedException, ClassModifiedException, IllegalOrphanException, NonexistentEntityException
    {
        EntityManager em = null;
        Boolean modified = false;
        Integer version = -1;
        Integer oldVersion = -1;

        em = getEntityManager();
        em.getTransaction().begin();

        DocumentType persistentDocumentType = em.find(DocumentType.class, documentType.getIdDocumentType());

        if (persistentDocumentType != null)
        {
            version = persistentDocumentType.getVersion(); // Version of the object in the database
            oldVersion = documentType.getVersion(); // Version of the object in memory

            if (version != oldVersion) // Versions differ: someone else modified the object
            {
                if (em != null)
                {
                    em.close();
                }

                throw new ClassModifiedException();

            } else
            {

                List<ProcedureTemplate> procedureTemplateListOld = persistentDocumentType.getProcedureTemplateList();
                List<ProcedureTemplate> procedureTemplateListNew = documentType.getProcedureTemplateList();
                List<String> illegalOrphanMessages = null;
                for (ProcedureTemplate procedureTemplateListOldProcedureTemplate : procedureTemplateListOld)
                {
                    if (!procedureTemplateListNew.contains(procedureTemplateListOldProcedureTemplate))
                    {
                        if (illegalOrphanMessages == null)
                        {
                            illegalOrphanMessages = new ArrayList<String>();
                        }
                        illegalOrphanMessages.add("You must retain ProcedureTemplate " + procedureTemplateListOldProcedureTemplate + " since its documentType field is not nullable.");
                    }
                }
                if (illegalOrphanMessages != null)
                {
                    throw new IllegalOrphanException(illegalOrphanMessages);
                }
                List<ProcedureTemplate> attachedProcedureTemplateListNew = new ArrayList<ProcedureTemplate>();
                for (ProcedureTemplate procedureTemplateListNewProcedureTemplateToAttach : procedureTemplateListNew)
                {
                    procedureTemplateListNewProcedureTemplateToAttach = em.getReference(procedureTemplateListNewProcedureTemplateToAttach.getClass(), procedureTemplateListNewProcedureTemplateToAttach.getProcedureTemplatePK());
                    attachedProcedureTemplateListNew.add(procedureTemplateListNewProcedureTemplateToAttach);
                }
                procedureTemplateListNew = attachedProcedureTemplateListNew;
                documentType.setProcedureTemplateList(procedureTemplateListNew);
                documentType = em.merge(documentType);
                for (ProcedureTemplate procedureTemplateListNewProcedureTemplate : procedureTemplateListNew)
                {
                    if (!procedureTemplateListOld.contains(procedureTemplateListNewProcedureTemplate))
                    {
                        DocumentType oldDocumentTypeOfProcedureTemplateListNewProcedureTemplate = procedureTemplateListNewProcedureTemplate.getDocumentType();
                        procedureTemplateListNewProcedureTemplate.setDocumentType(documentType);
                        procedureTemplateListNewProcedureTemplate = em.merge(procedureTemplateListNewProcedureTemplate);
                        if (oldDocumentTypeOfProcedureTemplateListNewProcedureTemplate != null && !oldDocumentTypeOfProcedureTemplateListNewProcedureTemplate.equals(documentType))
                        {
                            oldDocumentTypeOfProcedureTemplateListNewProcedureTemplate.getProcedureTemplateList().remove(procedureTemplateListNewProcedureTemplate);
                            oldDocumentTypeOfProcedureTemplateListNewProcedureTemplate = em.merge(oldDocumentTypeOfProcedureTemplateListNewProcedureTemplate);
                        }
                    }
                }

                em.getTransaction().commit();
                modified = true;
            }
        } else
        {
            throw new ClassEliminatedException();
        }

        if (em != null)
        {
            em.close();
        }

        return modified;
    }

    public Boolean destroy(Integer id) throws ClassEliminatedException, IllegalOrphanException, NonexistentEntityException
    {
        EntityManager em = null;
        Boolean removed = false;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();

            DocumentType persistentDocument = em.find(DocumentType.class, id);

            if (persistentDocument != null)
            {

                DocumentType documentType;
                try
                {
                    documentType = em.getReference(DocumentType.class, id);
                    documentType.getIdDocumentType();
                }
                catch (EntityNotFoundException enfe)
                {
                    throw new NonexistentEntityException("The document type with id " + id + " no longer exists.", enfe);
                }
                List<String> illegalOrphanMessages = null;
                List<ProcedureTemplate> procedureTemplateListOrphanCheck = documentType.getProcedureTemplateList();
                for (ProcedureTemplate procedureTemplateListOrphanCheckProcedureTemplate : procedureTemplateListOrphanCheck)
                {
                    if (illegalOrphanMessages == null)
                    {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("This DocumentType (" + documentType + ") cannot be destroyed since the ProcedureTemplate " + procedureTemplateListOrphanCheckProcedureTemplate + " in its procedureTemplateList field has a non-nullable documentType field.");
                }
                if (illegalOrphanMessages != null)
                {
                    throw new IllegalOrphanException(illegalOrphanMessages);
                }
                em.remove(documentType);
                em.getTransaction().commit();
                removed = true;
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

        return removed;
    }

    public List<DocumentType> findDocumentTypeEntities()
    {
        return findDocumentTypeEntities(true, -1, -1);
    }

    public List<DocumentType> findDocumentTypeEntities(int maxResults, int firstResult)
    {
        return findDocumentTypeEntities(false, maxResults, firstResult);
    }

    private List<DocumentType> findDocumentTypeEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from DocumentType as o");
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

    public DocumentType findDocumentType(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(DocumentType.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public List<DocumentType> findDocumentType(String name)
    {
        EntityManager em = getEntityManager();

        List<DocumentType> documentType = null;

        try
        {
            // Named query declared on the entity.
            Query q = em.createNamedQuery("DocumentType.findByName");

            // Bind the named query parameter.
            q.setParameter("name", name);

            documentType = (List<DocumentType>) q.getResultList();

            return documentType;

        }
        finally
        {
            em.close();
        }
    }

    public int getDocumentTypeCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from DocumentType as o");
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
