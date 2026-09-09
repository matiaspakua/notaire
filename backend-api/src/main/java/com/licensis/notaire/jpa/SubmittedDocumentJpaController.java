/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.UserTransaction;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.business.Procedure;
import org.hibernate.StaleObjectStateException;

/**
 *
 * @author juanca
 */
public class SubmittedDocumentJpaController implements Serializable, IPersistenciaJpa
{

    public SubmittedDocumentJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public boolean create(SubmittedDocument submittedDocument) throws NonexistentEntityException
    {

        boolean flag = false; //Variable para saber el resultado de la transaccion
        int version = 0;    //Variable para Version en bd del Objeto  

        Procedure fkIdProcedure = submittedDocument.getFkIdProcedure();
        EntityManager em = null;

        try
        {
            em = getEntityManager();
            em.getTransaction().begin();

            flag = this.findSubmittedDocument(submittedDocument);

            if (flag)
            {
                throw new StaleObjectStateException(null, version);

            } else
            {
                if (fkIdProcedure != null)
                {
                    fkIdProcedure = em.getReference(fkIdProcedure.getClass(), fkIdProcedure.getIdProcedure());
                    submittedDocument.setFkIdProcedure(fkIdProcedure);
                }
                em.persist(submittedDocument);
                if (fkIdProcedure != null)
                {
                    fkIdProcedure.getSubmittedDocumentList().add(submittedDocument);
                    fkIdProcedure = em.merge(fkIdProcedure);
                }
                em.getTransaction().commit();
                flag = true;
            }

        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return flag;

    }

    public boolean edit(SubmittedDocument submittedDocument) throws NonexistentEntityException, ClassModifiedException
    {

        Boolean flag = false; //Variable para saber el resultado de la transaccion
        int oldVersion = 0; //Variable para Version en memoria del Objeto
        int version = 0;    //Variable para Version en bd del Objeto       

        EntityManager em = getEntityManager();

        SubmittedDocument persistSubmittedDocument = em.find(SubmittedDocument.class, submittedDocument.getIdSubmittedDocument());

        if (persistSubmittedDocument != null)
        {
            version = persistSubmittedDocument.getVersion(); // Version del Objeto en db
            oldVersion = submittedDocument.getVersion(); // Version del Objeto en memoria

            if (version != oldVersion) //Si son distintas "Alguien modifico el objeto"
            {
                throw new ClassModifiedException("El documento indicado ha sido modificado por otro usuario");

            } else
            {
                try
                {
                    em.getTransaction().begin();
                    SubmittedDocument persistentSubmittedDocument = em.find(SubmittedDocument.class, submittedDocument.getIdSubmittedDocument());
                    Procedure fkIdProcedureOld = persistentSubmittedDocument.getFkIdProcedure();
                    Procedure fkIdProcedureNew = submittedDocument.getFkIdProcedure();
                    if (fkIdProcedureNew != null)
                    {
                        fkIdProcedureNew = em.getReference(fkIdProcedureNew.getClass(), fkIdProcedureNew.getIdProcedure());
                        submittedDocument.setFkIdProcedure(fkIdProcedureNew);
                    }
                    submittedDocument = em.merge(submittedDocument);
                    if (fkIdProcedureOld != null && !fkIdProcedureOld.equals(fkIdProcedureNew))
                    {
                        fkIdProcedureOld.getSubmittedDocumentList().remove(submittedDocument);
                        fkIdProcedureOld = em.merge(fkIdProcedureOld);
                    }
                    if (fkIdProcedureNew != null && !fkIdProcedureNew.equals(fkIdProcedureOld))
                    {
                        fkIdProcedureNew.getSubmittedDocumentList().add(submittedDocument);
                        fkIdProcedureNew = em.merge(fkIdProcedureNew);
                    }
                    em.getTransaction().commit();
                    flag = true;
                }
                catch (Exception ex)
                {
                    String msg = ex.getLocalizedMessage();
                    if (msg == null || msg.length() == 0)
                    {
                        Integer id = submittedDocument.getIdSubmittedDocument();
                        if (findSubmittedDocument(id) == null)
                        {
                            throw new NonexistentEntityException("The documentoPresentado with id " + id + " no longer exists.");
                        }
                    }
                    throw ex;
                }

            }

        }
        return flag;
    }

    public void destroy(Integer id) throws NonexistentEntityException
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            SubmittedDocument submittedDocument;
            try
            {
                submittedDocument = em.getReference(SubmittedDocument.class, id);
                submittedDocument.getIdSubmittedDocument();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The documentoPresentado with id " + id + " no longer exists.", enfe);
            }
            Procedure fkIdProcedure = submittedDocument.getFkIdProcedure();
            if (fkIdProcedure != null)
            {
                fkIdProcedure.getSubmittedDocumentList().remove(submittedDocument);
                fkIdProcedure = em.merge(fkIdProcedure);
            }
            em.remove(submittedDocument);
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

    public List<SubmittedDocument> findSubmittedDocumentEntities()
    {
        return findSubmittedDocumentEntities(true, -1, -1);
    }

    public List<SubmittedDocument> findSubmittedDocumentEntities(int maxResults, int firstResult)
    {
        return findSubmittedDocumentEntities(false, maxResults, firstResult);
    }

    private List<SubmittedDocument> findSubmittedDocumentEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from SubmittedDocument as o");
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

    public SubmittedDocument findSubmittedDocument(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(SubmittedDocument.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getSubmittedDocumentCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from SubmittedDocument as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public List<SubmittedDocument> findDocumentsPresentados()
    {
        EntityManager em = getEntityManager();

        List<SubmittedDocument> listDocumentPresentados = null;

        Query query = em.createNamedQuery("DocumentoPresentado.findAll");
        listDocumentPresentados = query.getResultList();

        return listDocumentPresentados;
    }

    public List<SubmittedDocument> findDocumentsPorVencer()
    {
        EntityManager em = getEntityManager();
        List<SubmittedDocument> listaDocumentPorVencer = new ArrayList<>();

        Date dateActual = Calendar.getInstance().getTime();

        Query query = em.createNamedQuery("DocumentoPresentado.findByFechaVencimiento");
        query.setParameter("fechaVencimiento", dateActual);

        listaDocumentPorVencer = query.getResultList();

        return listaDocumentPorVencer;
    }

    private Boolean findSubmittedDocument(SubmittedDocument submittedDocument)
    {

        boolean flag = false;
        int idProcedure = submittedDocument.getFkIdProcedure().getIdProcedure();
        String nameDocument = submittedDocument.getName();

        EntityManager em = null;

        em = getEntityManager();
        em.getTransaction().begin();

        ArrayList<SubmittedDocument> listaDocumentPresentados = (ArrayList<SubmittedDocument>) this.findDocumentsPresentados();

        for (int i = 0; i < listaDocumentPresentados.size() && !flag; i++)
        {
            SubmittedDocument submittedDocumentPersist = listaDocumentPresentados.get(i);
            int idProcedurePersist = submittedDocumentPersist.getFkIdProcedure().getIdProcedure();
            String nameDocumentPersist = submittedDocumentPersist.getName();

            if (idProcedure == idProcedurePersist && nameDocument.equals(nameDocumentPersist))
            {
                flag = true;
            }

        }
        return flag;
    }

    public Boolean createDocumentExterno(SubmittedDocument document) throws NonexistentEntityException, ClassModifiedException
    {
        EntityManager em = null;
        Boolean flag = false;
        em = getEntityManager();
        em.getTransaction().begin();
        try
        {
            SubmittedDocument submittedDocument = new SubmittedDocument();

            submittedDocument.setVersion(document.getVersion());
            submittedDocument.setName(document.getName());
            submittedDocument.setDeliveredBy(document.getDeliveredBy());

            Procedure miProcedure = document.getFkIdProcedure();
            submittedDocument.setFkIdProcedure(miProcedure);

            em.persist(submittedDocument);
            em.flush();
            em.getTransaction().commit();
            flag = true;

            this.edit(submittedDocument);
        }
        finally
        {
            em.close();
        }
        return flag;
    }

    @Override
    public String getNameJpa()
    {
        return this.getClass().getName();
    }
}
