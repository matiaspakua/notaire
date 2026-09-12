/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;
import com.licensis.notaire.service.AdministradorJpa;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.business.AuditRecord;
import com.licensis.notaire.business.User;

/**
 *
 * @author juanca
 */
public class AuditRecordJpaController implements Serializable, IPersistenciaJpa {

    private AuditRecordJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }

    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;
    private static AuditRecordJpaController instancia = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public boolean create(AuditRecord auditRecord) {
        boolean resultado = false;

        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            User fkIdUser = auditRecord.getFkIdUser();
            if (fkIdUser != null) {
                fkIdUser = em.getReference(fkIdUser.getClass(), fkIdUser.getIdUser());
                auditRecord.setFkIdUser(fkIdUser);
            }
            em.persist(auditRecord);
            if (fkIdUser != null) {
                fkIdUser.getAuditRecordList().add(auditRecord);
                fkIdUser = em.merge(fkIdUser);
            }
            em.getTransaction().commit();
            resultado = true;
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return resultado;
    }

    public static AuditRecordJpaController getInstancia() {

        EntityManagerFactory emf = AdministradorJpa.getEmf();

        if (instancia == null || (instancia.emf == null && emf != null)) {
            instancia = new AuditRecordJpaController(null, emf);
        }
        return instancia;
    }

    public void edit(AuditRecord auditRecord) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            AuditRecord persistentAuditRecord = em.find(AuditRecord.class,
                    auditRecord.getIdAuditRecord());
            User fkIdUserOld = persistentAuditRecord.getFkIdUser();
            User fkIdUserNew = auditRecord.getFkIdUser();
            if (fkIdUserNew != null) {
                fkIdUserNew = em.getReference(fkIdUserNew.getClass(), fkIdUserNew.getIdUser());
                auditRecord.setFkIdUser(fkIdUserNew);
            }
            auditRecord = em.merge(auditRecord);
            if (fkIdUserOld != null && !fkIdUserOld.equals(fkIdUserNew)) {
                fkIdUserOld.getAuditRecordList().remove(auditRecord);
                fkIdUserOld = em.merge(fkIdUserOld);
            }
            if (fkIdUserNew != null && !fkIdUserNew.equals(fkIdUserOld)) {
                fkIdUserNew.getAuditRecordList().add(auditRecord);
                fkIdUserNew = em.merge(fkIdUserNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = auditRecord.getIdAuditRecord();
                if (findAuditRecord(id) == null) {
                    throw new NonexistentEntityException("The registroAuditoria with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            AuditRecord auditRecord;
            try {
                auditRecord = em.getReference(AuditRecord.class, id);
                auditRecord.getIdAuditRecord();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The registroAuditoria with id " + id + " no longer exists.",
                        enfe);
            }
            User fkIdUser = auditRecord.getFkIdUser();
            if (fkIdUser != null) {
                fkIdUser.getAuditRecordList().remove(auditRecord);
                fkIdUser = em.merge(fkIdUser);
            }
            em.remove(auditRecord);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<AuditRecord> findAuditRecordEntities() {
        return findAuditRecordEntities(true, -1, -1);
    }

    public List<AuditRecord> findAuditRecordEntities(int maxResults, int firstResult) {
        return findAuditRecordEntities(false, maxResults, firstResult);
    }

    private List<AuditRecord> findAuditRecordEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from AuditRecord as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public AuditRecord findAuditRecord(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(AuditRecord.class, id);
        } finally {
            em.close();
        }
    }

    public int getAuditRecordCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from AuditRecord as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public ArrayList<AuditRecord> searchRecordAuditoriasUser(User miUser) {
        List<AuditRecord> listaAudit = null;
        ArrayList<AuditRecord> userListaAudit = new ArrayList<AuditRecord>();
        Boolean flag = false;
        try {

            EntityManager em = getEntityManager();

            Query query = em.createNamedQuery("RegistroAuditoria.findAll");

            listaAudit = query.getResultList();

            // Filtro el resultado de la busqueda con el Usuario deseado
            for (int i = 0; i < listaAudit.size(); i++) {
                if (miUser.getIdUser().equals(listaAudit.get(i).getFkIdUser().getIdUser())) {
                    flag = true;
                    userListaAudit.add(listaAudit.get(i));
                }
            }

            if (!flag) {
                userListaAudit = null;
            }

        } catch (Exception e) {
            System.out.println("Error Metodo : getDtoRegistroAuditoria");
        }
        return userListaAudit;

    }

    @Override
    public String getNameJpa() {
        return this.getClass().getName();
    }
}
