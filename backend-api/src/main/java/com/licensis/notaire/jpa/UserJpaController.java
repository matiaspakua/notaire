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
import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.AuditRecord;
import com.licensis.notaire.business.User;

/**
 *
 * @author juanca
 */
public class UserJpaController implements Serializable, IPersistenciaJpa {

    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;
    private static UserJpaController instancia = null;

    private UserJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static UserJpaController getInstancia() {

        EntityManagerFactory emf = AdministradorJpa.getEmf();

        if (instancia == null || (instancia.emf == null && emf != null)) {
            instancia = new UserJpaController(null, emf);
        }
        return instancia;

    }

    public void create(User users) {
        if (users.getAuditRecordList() == null) {
            users.setAuditRecordList(new ArrayList<AuditRecord>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Person fkIdPerson = users.getFkIdPerson();
            if (fkIdPerson != null) {
                fkIdPerson = em.getReference(fkIdPerson.getClass(), fkIdPerson.getPersonId());
                users.setFkIdPerson(fkIdPerson);
            }
            List<AuditRecord> attachedAuditRecordList = new ArrayList<AuditRecord>();
            for (AuditRecord auditRecordListAuditRecordToAttach : users
                    .getAuditRecordList()) {
                auditRecordListAuditRecordToAttach = em.getReference(
                        auditRecordListAuditRecordToAttach.getClass(),
                        auditRecordListAuditRecordToAttach.getIdAuditRecord());
                attachedAuditRecordList.add(auditRecordListAuditRecordToAttach);
            }
            users.setAuditRecordList(attachedAuditRecordList);
            em.persist(users);
            if (fkIdPerson != null) {
                fkIdPerson.getUserList().add(users);
                fkIdPerson = em.merge(fkIdPerson);
            }
            for (AuditRecord auditRecordListAuditRecord : users.getAuditRecordList()) {
                User oldFkIdUserOfAuditRecordListAuditRecord = auditRecordListAuditRecord
                        .getFkIdUser();
                auditRecordListAuditRecord.setFkIdUser(users);
                auditRecordListAuditRecord = em.merge(auditRecordListAuditRecord);
                if (oldFkIdUserOfAuditRecordListAuditRecord != null) {
                    oldFkIdUserOfAuditRecordListAuditRecord.getAuditRecordList()
                            .remove(auditRecordListAuditRecord);
                    oldFkIdUserOfAuditRecordListAuditRecord = em
                            .merge(oldFkIdUserOfAuditRecordListAuditRecord);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(User users) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            User persistentUsers = em.find(User.class, users.getIdUser());
            Person fkIdPersonOld = persistentUsers.getFkIdPerson();
            Person fkIdPersonNew = users.getFkIdPerson();
            List<AuditRecord> auditRecordListOld = persistentUsers.getAuditRecordList();
            List<AuditRecord> auditRecordListNew = users.getAuditRecordList();
            List<String> illegalOrphanMessages = null;
            for (AuditRecord auditRecordListOldAuditRecord : auditRecordListOld) {
                if (!auditRecordListNew.contains(auditRecordListOldAuditRecord)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages
                            .add("You must retain RegistroAuditoria " + auditRecordListOldAuditRecord
                                    + " since its fkIdUsuario field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (fkIdPersonNew != null) {
                fkIdPersonNew = em.getReference(fkIdPersonNew.getClass(), fkIdPersonNew.getPersonId());
                users.setFkIdPerson(fkIdPersonNew);
            }
            List<AuditRecord> attachedAuditRecordListNew = new ArrayList<AuditRecord>();
            for (AuditRecord auditRecordListNewAuditRecordToAttach : auditRecordListNew) {
                auditRecordListNewAuditRecordToAttach = em.getReference(
                        auditRecordListNewAuditRecordToAttach.getClass(),
                        auditRecordListNewAuditRecordToAttach.getIdAuditRecord());
                attachedAuditRecordListNew.add(auditRecordListNewAuditRecordToAttach);
            }
            auditRecordListNew = attachedAuditRecordListNew;
            users.setAuditRecordList(auditRecordListNew);
            users = em.merge(users);
            if (fkIdPersonOld != null && !fkIdPersonOld.equals(fkIdPersonNew)) {
                fkIdPersonOld.getUserList().remove(users);
                fkIdPersonOld = em.merge(fkIdPersonOld);
            }
            if (fkIdPersonNew != null && !fkIdPersonNew.equals(fkIdPersonOld)) {
                fkIdPersonNew.getUserList().add(users);
                fkIdPersonNew = em.merge(fkIdPersonNew);
            }
            for (AuditRecord auditRecordListNewAuditRecord : auditRecordListNew) {
                if (!auditRecordListOld.contains(auditRecordListNewAuditRecord)) {
                    User oldFkIdUserOfAuditRecordListNewAuditRecord = auditRecordListNewAuditRecord
                            .getFkIdUser();
                    auditRecordListNewAuditRecord.setFkIdUser(users);
                    auditRecordListNewAuditRecord = em.merge(auditRecordListNewAuditRecord);
                    if (oldFkIdUserOfAuditRecordListNewAuditRecord != null
                            && !oldFkIdUserOfAuditRecordListNewAuditRecord.equals(users)) {
                        oldFkIdUserOfAuditRecordListNewAuditRecord.getAuditRecordList()
                                .remove(auditRecordListNewAuditRecord);
                        oldFkIdUserOfAuditRecordListNewAuditRecord = em
                                .merge(oldFkIdUserOfAuditRecordListNewAuditRecord);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = users.getIdUser();
                if (findUsers(id) == null) {
                    throw new NonexistentEntityException("The usuarios with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }

    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            User users;
            try {
                users = em.getReference(User.class, id);
                users.getIdUser();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The usuarios with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<AuditRecord> auditRecordListOrphanCheck = users.getAuditRecordList();
            for (AuditRecord auditRecordListOrphanCheckAuditRecord : auditRecordListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages
                        .add("This Usuarios (" + users + ") cannot be destroyed since the RegistroAuditoria "
                                + auditRecordListOrphanCheckAuditRecord
                                + " in its registroAuditoriaList field has a non-nullable fkIdUsuario field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Person fkIdPerson = users.getFkIdPerson();
            if (fkIdPerson != null) {
                fkIdPerson.getUserList().remove(users);
                fkIdPerson = em.merge(fkIdPerson);
            }
            em.remove(users);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<User> findUsersEntities() {
        return findUsersEntities(true, -1, -1);
    }

    public List<User> findUsersEntities(int maxResults, int firstResult) {
        return findUsersEntities(false, maxResults, firstResult);
    }

    private List<User> findUsersEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Usuarios as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public User findUsers(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }

    public int getUsersCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from Usuarios as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public List<User> searchUsers() {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("Usuario.findAll");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Busca un usuario por id de persona asociada.
     * Retorna null si no existe.
     */
    public User findUserByPerson(Integer idPerson) {
        if (idPerson == null)
            return null;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("Usuario.findByFkIdPersona");
            query.setParameter("idPersona", idPerson);
            @SuppressWarnings("unchecked")
            List<User> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    public Boolean modificarUser(User pUser) throws ClassModifiedException, ClassEliminatedException {

        Boolean flag = false; // Variable para saber el resultado de la transaccion
        int oldVersion = 0; // Variable para Version en memoria del Objeto
        int version = 0; // Variable para Version en bd del Objeto

        EntityManager em = getEntityManager();

        try {
            User persistenUser = em.find(User.class, pUser.getIdUser());

            if (persistenUser != null) {
                version = persistenUser.getVersion();
                oldVersion = pUser.getVersion();

                if (version != oldVersion) {
                    throw new ClassModifiedException("El usuario indicado ha sido modificado por otro usuario");
                } else {
                    em.getTransaction().begin();
                    persistenUser.setName(pUser.getName());
                    persistenUser.setPassword(pUser.getPassword());
                    persistenUser.setStatus(pUser.getStatus());
                    persistenUser.setType(pUser.getType());
                    em.getTransaction().commit();
                    flag = true;
                }
            } else {
                throw new ClassEliminatedException("El cliente indicado ya ha sido eliminado con anterioridad");
            }
        } finally {
            em.close();
        }

        return flag;
    }

    @Override
    public String getNameJpa() {
        return this.getClass().getName();
    }
}
