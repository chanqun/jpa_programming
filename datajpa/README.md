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


##### Auditing
엔티티를 생성, 변경할 떄 변경산 사람과 시간을 추적하고 싶을떄
- 등록일, 수정일, 등록자, 수정자
jpa 이벤트로 해결한 것, JpaBaseEntity
  
스프링 데이터 JPA 사용 annotation 쓰면됨

##### 페이징과 정렬
- page
- sort
- size 가능 

```kotlin
    @GetMapping("/members")
    fun list(@PageableDefault(size = 3) pageable: Pageable) : Page<Member> {
        // members?page=0&size=3&sort=id,desc&sort=username,desc 도 사용 가능하다
        return memberRepository.findAll(pageable)
    }
```
> default size 20 인데 바꾸고 싶을때
> data.web.pageable.default-page-size 10
> max-page-size 2000 로 설정할 수 있다.

페이지를 1부터 하고 싶을 때
MyPage 로 반환한다. --> 직접구현하는게 나을듯
```
spring.data.web.pageable.one-indexed-parameters: true
--> 페이지 0과 1이 같은 값이 나오게 됨, pageable내 객체가 안 맞음 !!!
```

##### 스프링 데이터 JPA 구현체 분석

@Repository
@Transactional(readOnly = true)

- save(entity)

##### 새로운 엔티티를 구별하는 방법

```kotlin
entityInformation.isNew() 
// 까지 타고 들어가서 id가 생기게 됨
```
Long인 경우 null, long인 경우 0으로 판단


@GeneratedValue 가 없을 경우 
```java
@Override
public boolean isNew() {
    return createdDate == null;
}
```

##### 나머지 기능들
- Specifications(명세)

스프링 데이터 JPA Criteria를 활용해서 이 개념을 사용할 수 있도록 지원
-> 실무에서 가능한 쓰지 마세요 (쓰기도 어렵네)

- Query By Example
- Projections
- 네이티브 쿼리
