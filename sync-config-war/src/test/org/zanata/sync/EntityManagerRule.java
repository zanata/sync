package org.zanata.sync;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.rules.ExternalResource;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class EntityManagerRule extends ExternalResource {
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("syncDatasourcePU");
    private EntityManager em;

    @Override
    protected void before() throws Throwable {
        em = emf.createEntityManager();
        em.getTransaction().begin();
    }

    @Override
    protected void after() {
        em.getTransaction().rollback();
        if (em.isOpen()) {
            em.close();
        }
        em = null;
    }

    public EntityManager getEm() {
        return em;
    }
}
