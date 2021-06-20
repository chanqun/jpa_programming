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