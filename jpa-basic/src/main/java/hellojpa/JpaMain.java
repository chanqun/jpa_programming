package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            Member member1 = new Member(150L, "A");
            Member member2 = new Member(150L, "B");

            em.persist(member1);
            em.persist(member2);
            System.out.println("===================");


            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}

/* 강의 1
    insert
    Member member = new Member();
    member.setId(2L);
    member.setName("HelloB");
    em.persist(member);

    수정
    Member findMember = em.find(Member.class, 1L);
    findMember.setName("HelloJPA");

    조회 나이가 18살 이상인 회원을 모두 검색하고 싶다면? --> JPQL이 해준다.
    List<Member> result = em.createQuery("select m from Member as m", Member.class)
    .setFirstResult(1)
    .setMaxResults(10)
    .getResultList();

    for (Member member : result) {
        System.out.println("member.name = " + member.getName());
    }
*/