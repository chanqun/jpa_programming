### 실전! 스프링 데이터 JPA

interface만 있는데 기능이 잘 작동한다!

@EnableJpaRepositories 를 해줘야하지만
spring boot가 다 해줌

spring이 
인터페이스 구현체를 만들어서 injection을 해줬음

--

쿼리 메소드 기능
spring boot 에서 다 찾아줌

--

jpa는
named query를 제공해줌 별로 안 씀
```kotlin
@NamedQuery(
        name="Memeber.findByName",
        query="select m from member m where m.name = :name"
)

// repository에서 사용할 떄
@Query(name="Member.findByName")
fun findByName(@Param("username") username: String) List<Member>
```

##### Query로 DTO 조회
```kotlin
@Query("select m.username from Member m")
fun findUsernameList(): List<String>
```


##### 파라미터 바인딩
위치기반 , 이름기반, collection
```kotlin
@Query("select m from Member m where m.username = :name")
 fun findMembers(@Param("name") username: String): Member

@Query("select m from Member m where m.username in :names")
fun findByNames(@Param("names") names: List<String>): List<Member>
```

##### 반환타입

- Optional, List<>, Member 등이 있음

##### 기존 JPA 페이징과 정렬
```kotlin
fun findByPage(age: Int, offset: Int, limit: Int) {
    return em.createQuery("select m from Member m where m.age = :age order by m.username desc")
        .setParameter("age", age)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList()
}

fun totalCount(age: Int) {
    return em.createQuery("select count(m) from Member m where m.age = :age", Long::class.java)
        .setParameter("age", age)
        .getSingleResult()
}
```

##### Spring JPA 페이징과 정렬
org.springframework.data.domain.Sort
org.springframework.data.domain.Pageable

```
org.springframework.data.domain.Page : 추가 count 쿼리 결과를 포함하는 페이징
org.springframework.data.domain.Slice : 추가 count 쿼리 없이 다음 페이지만 확인 가능(내부적으로 limit + 1)
List: 추가 count 쿼리 없이 결과만 반환
```

@Query에서 countQuery를 나눌 수 있음 - 카운트 쿼리는 쉽게 설정할 수 있음

api에서 엔티티를 넘기면 안 된다. dto로 넘겨라!


#### 벌크성 수정 쿼리
```kotlin
fun bulkAgePlus(age: Int) {
    return em.createQuery("update Member m set m.age = m.age + 1 where m.age >= :age")
        .setParameter("age", age)
        .excuteUpdate()
}
```

#### EntityGraph
jpql을 사용하지 않고 spring에서 제공하는 함수와 fetch join을 사용하고 싶다
@EntityGraph(attributePaths = {"team"})
Named로 설정하고 EntityGraph를 사용할 수 있음


#### JPA Hint & Lock


#### 확장 기능 사옹자 정의 리포지토리 구현
인터페이스를 직접 구현하는 경우 기능이 너무 많다!

- MyBatis, Querydsl 등 사용할 때

구현체를 꼭 MemberRepositoryImpl 이름으로 해줘야한다.

interface만들고 하는 것이 복잡하다.
핵심로직과 - dto 화면과 관계 있는 것은 분리하는 편이다!!!!!
-> 수정에 라이프사이클이 다르다

> 핵심 비지니스 로직과 아닌 것을 분리하는 것!, 커맨드를 분리하는 것 중요
