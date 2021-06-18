## 자바 ORM 표준 JPA 프로그래밍

[JPA소개](#JPA-소개)  
[JPA시작하기](#JPA-시작하기)  
[영속성 관리 - 내부 동작 방식](#영속성-관리-_-내부-동작-방식)  
[엔티티 매핑](#엔티티-매핑)  
[다양한 연관관계 매핑](#다양한-연관관계-매핑)  
[고급매핑](#고급-매핑)  
[프록시와 연관관계 관리](#프록시와-연관관계-관리)  
[값 타입](#값-타입)  
[객체지향 쿼리 언어](#객체지향-쿼리-언어)

### JPA 소개

#### JPA 실무에서 어려운 이유

- 처음 JPA나 스프링 데이터 JPA를 만나면?
- SQL 자동화, 수십줄의 코드가 한 두줄로
- 실무에 바로 도입하면?
- 예제들은 보통 테이블이 한 두개로 단순함
- 실무는 수십 개 이상의 복잡한 객체와 테이블 사용

목표 - 객체와 테이블 설계

- 객체와 테이블을 제대로 설계하고 매핑하는 방법
- 기본 키와 외래 키 매핑
- 1:N, N:1, 1:1, N:M 매핑
- 실무 노하우 + 성능까지 고려

목표 - JPA 내부 동작 방식 이해

- JPA의 내부 동작 방식을 이해하지 못하고 사용
- JPA 내부 동작 방식을 그림과 코드로 자세히 설명
- JPA가 어떤 SQL을 만들어 내는지 이해
- JPA가 언제 SQL을 실행하는지 이해

JPA로 시간이 남아 더 많은 Test Code를 만들 수 있고 구조를 개선할 수 있었다.

#### SQL 중심적인 개발의 문제점

지금 시대는 객체를 관계형 DB에 관리
> 무한 반복, 지루한 코드
> CRUD

패러다임의 불일치 객체 vs 관계형 데이터베이스

객체를 영구 보관하는 다양한 저장소 DB

객체와 관계형 데이터베이스의 차이

1. 상속 db에서 데이터를 조회할때 join해서 가져와야한다.
2. 연관관계 객체다운 모델링 - 팀id가 아닌 팀을 넣어야하는 것이 아닌가?
4. 데이터 타입
5. 데이터 식별 방법

##### JPA (Java Persistence API)

ORM (Object-relation mapping)

JPA 동작 - 저장 Entitiy Object를 넘기면 - Entity 분석 JDBC API - INSERT

#### JPA 소개

EJB라는 것이 있었는데 엉망이었음 - JPA (자바 표준)
하이버네이트(오픈소스)

#### JPA는 표준 명세

- JPA는 인터페이스의 모음 - jpa는 인터페이스
- 하이버네이트, EclipseLink, DataNucleus 구현체

#### 생산성 - JPA와 CRUD

사상자체가 자바 컬렉션에 값을 넣었다가 뺐다가 하는 것과 같다. 저장 jpa.persist 조회 jpa.find 수정 member.setName 삭제 jpa.remove

jpa 동일한 트랜잭션에서 조회한 엔티티는 같음을 보장

#### JPA의 성능 최적화 기능

1. 1차 캐시와 동일성(identity)보장 - DB Isolation Level이 Read Commit이어도 애플리케이션에서 Repeatable Read 보장
3. 트랜잭션을 지원하는 쓰기 지연 (transactional write-behind) - JDBC BATCH SQL 기능을 가능하게 해줌 transaction.begin()
4. 지연 로딩(Lazy Loading) - 옵션하나로 지연로딩이 가능하다.

ORM은 객체와 RDB 두 기둥위에 있는 기술

### JPA 시작하기

#### JPA 구동 방식

Persistence -> 생성 -> EntitiyManagerFactory -> EntitiyManager 생성

​ |

1. 설정 정보 조회

​ |

META-INF/persistence.xml

#### JPQL

- 테이블이 아닌 객체를 대상으로 검색하는 객체 지향 쿼리
- SQL을 추상화해서 특정 데이터베이스 SQL에 의존 X
- JPQL을 한마디로 정의하면 객체 지향 SQL
- JPQL은 뒤에서 아주 자세히 다룸

### 영속성 관리 _ 내부 동작 방식

JAP에서 가장 중요한 2가지

- 객체와 관계형 데이터베이스 매핑
- 영속성 컨텍스트
    - JPA를 이해하는데 가장 중요한 용어
    - 엔티티를 영구 저장하는 환경이라는 뜻
    - EntityManager.persist(entity);

> 영속성 컨텍스트는 논리적인 개념
>
> 눈에 보이지 않는다.
>
> 엔티티 매니저를 통해서 영속성 컨텍스트에 접근

#### 엔티티의 생명주기

- 비영속 (new/transient)
- 영속 (managed)
- 준영속 (detached)
- 삭제 (removed)

```java
//1. 비영속
Member member = new Member();
member.setId("member1");
member.setName(회원1);

//2. 영속
em.persist(member); //관리가 되는 상태

//3. 준영속
em.detatch(member); //영속성 컨텍스트에서 다시 지움

em.remove(member)

```

#### !!!!!!!!!!!!영속성 컨텍스트의 이점

- 1차 캐시

DB가 아닌 1차 캐시를 찾는다.

- 동일성 보장
- 트랜잭션을 지원하는 쓰기 지연
- 변경 감지

엔티티 수정을하면 em.update(member1)이 있어야 할 것 같지만 저절로 감지

flush를 하면 엔티티와 스냅샷(최초로 불러온 상태)를 비교하고 update쿼리를 db에 반영하고 commit한다.

- 지연 로딩

#### 플러시

영속성 컨텍스트의 변경내용을 데이터베이스에 반영

- 변경 감지
- 수정된 엔티티 쓰기 지연 SQL 저장소에 등록
- 쓰기 지연 SQL 저장소의 쿼리를 데이터베이스에 전송 (등록, 수정, 삭제)

영속성 컨텍스트를 플러시하는 방법

em.flush()
commit할때 JPQL 쿼리 실행

> 플러시 모드 옵션
>
> FlushModeType.AUTO auto (default)
>
> FlushModeType.COMMIT 커밋할때만

!!!

영속성 컨텍스트를 비우지 않음

영속성 컨텍스트의 변경내용을 데이터베이스에 동기화

트랜잭션이라는 작업 단위가 중요 -> 커밋 직전에만 동기화하면 됨

#### 준영속 상태

- 영속 -> 준영속
- 영속 상태의 엔티티가 영속성 컨텍스트에서 분리 (detatched)
- 영속성 컨텍스트가 제공하는 기능을 사용 못 함

```
em.detatch(entity)
em.clear()
em.close()
```

### 엔티티 매핑

#### 객체와 테이블 매핑

@Entity, @Table

필드와 컬럼 매핑

@Column

기본 키 매핑

@Id

연관관계 매핑:

@ManyToOne, @JoinColumn

##### @Entity

> @Entity가 붙은 클래스는 JPA가 관리, 엔티티라 한다.
>
> JPA를 사용해서 테이블과 매핑할 클래스는 @Entity필수
>
> !주의
>
> - 기본 생성자 필수 publi or protected
> - final클래스, enum, interface, inner 클래스 사용 X
> - 저장할 필드에 final사용 X

#### 데이터베이스 스키마 자동 생성

- DDL을 애플리케이션 실행 시점에 자동 생성
- 테이블 중심 -> 객체 중심
- 데이터베이스 방언을 활용해서 데이터베이스에 맞는 적절한 DDL생성
- 이렇게 생성된 DDL은 개발 장비에서만 사용
- 생성된 DDL은 운영서버에서는 사용하지 않거나 적절히 다듬은 후 사용

> hibernate.hbm2ddl.auto option

> 개발
>
> create : drop + create
> create-drop : 끝난 후 drop (test-case 실행시)
> update: 변경만 적용 지우는 것은 안된다.

> staging
> validate : 엔티티와 테이블이 정상 매핑 확인
> none : 사용하지 않음

##### DDL 생성 가능

DDL 생성 기능은 DDL을 실행할때만 사용되고 JPA의 실행 로직에는 영향 주지 않음

@Column(unique = true, length = 10) alter table이 실행 됨

#### 필드와 컬럼 매핑

> @Column 컬럼 매핑
>
> name 필드와 매핑할 테이블의 컬럼 이름 객체의 필드 이름
>
> insertable, updatable 등록, 변경 가능 여부 TRUE
>
> nullable(DDL) null 값의 허용 여부를 설정한다. false로 설정하면 DDL 생성 시에 not null 제약조건이 붙는다. unique(DDL) @Table의 uniqueConstraints와 같지만 한 컬럼에 간단히 유니크 제 약조건을 걸 때 사용한다.
>
> columnDefinition (DDL)  데이터베이스 컬럼 정보를 직접 줄 수 있다. ex) varchar(100) default ‘EMPTY' 필드의 자바 타입과 방언 정보를 사용해 서 적절한 컬럼 타입 length(DDL) 문자 길이 제약조건, String 타입에만 사용한다. 255 precision, scale(DDL)  BigDecimal 타입에서 사용한다(BigInteger도 사용할 수 있다).
>
>  precision은 소수점을 포함한 전체 자 릿수를, scale은 소수의 자릿수 다. 참고로 double, float 타입에는 적용되지 않는다. 아주 큰 숫자나 정 밀한 소수를 다루어야 할 때만 사용한다. precision=19, scale=2
>
> @Temporal 날짜 타입 매핑
>
> @Enumberated enum 타입 매핑
>
> @Lob BLOB CLOB 매핑
>
> @Transient 매핑 무시

#### 기본 키 매핑

직접 할당: @Id

자동 생성: @GeneratedValue

- IDENTITY : 데이터베이스에 위임, MYSQL

!! null이 와서 id 값을 db에 넣어봐야 id 값을 확은 가능하다 JPA 입장에서는 em.persist(member) 호출하자마자 insert를 날린다. 이시점에 영속성 컨텍스트에 id를 가져옴

모아서 insert가 불가능하다.

- SEQUENCE: 데이터베이스 시퀀스 오브젝트 사용, ORACLE, H2

50개 nextcall 미리하고 사용 allocationSize 50

- TABLE : 키 생성용 테이블 사용, 모든 DB에서 사용 : 모든 데이터베이스에 적용 가능
- AUTO : 방언에 따라 자동 지정, 기본값

##### 요구사항 분석과 기본 매핑

- 회원은 상품을 주문할 수 있다.
- 주문 시 여러 종류의 상품을 선택할 수 있따.

```java
//관계형 DB에 맞춘 설계
@Entity
@Table(name = "ORDERS")
public class Order {

    @Id
    @GeneratedValue
    @Column(name = "ORDER_ID")
    private Long id;

    @Column(name = "MEMBER_ID")
    private Long memberId; 

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}
```

### 연관관계 매핑 기초

#### 단방향 연관관계

- 객체와 테이블 연관관계의 차이를 이해
- 객체의 참조와 테이블의 외래 키를 매핑
- 용어 이해
    - 방향 : 단방향, 양방향
    - 다중성 : (N:1, 1:N, 1:1, N:M)
    - 연관관계의 주인 : 객체 양방향 연관관계는 관리 주인이 필요

> jpateam > Member.class

#### 양방향 연관관계와 연관관계의 주인

##### 연관관계의 주인과 mappedBy

- mappedBy
- 객체와 테이블간에 연관관계를 맺는 차이를 이해해야한다.
    - 객체 연관관계 = 2개
        - 회원 -> 팀 연관관계 1개 (단방향)
        - 팀 -> 회원 연관관계 1개 (단방향)
    - 테이블 연관관계 = 1개
        - 회원 <-> 팀의 연관관계 1개 (양방향)

##### 객체의 양방향 관계

객체의 양방향 관계는 사실 양방향 관계가 아닌 단방향 관계 2개

객체를 양방향으로 참조하려면 단방향 연관관계를 2개 만들어야함

##### 테이블의 양방향 연관관계

테이블은 외래키 값 하나로 두 테이블의 연관관례를 관리

**!!!! 둘 중 하나로 외래 키를 관리해야한다!**

##### 연관관계의 주인

> 양방향 매핑 규칙
>
> - 객체의 두 관계중 하나를 연관관계의 주인으로 지정
> - 연관관계의 주인만이 외래 키를 관리 (등록, 수정)
> - 주인이 아닌쪽은 읽기만 가능
> - 주인은 mappedBy 속성 사용X
> - 주인이 아니면 mappedBy 속성으로 주인 지정

##### 누구를 주인으로?

- 외래 키가 있는 곳을 주인으로 정해라
- 여기서는 Member.team이 연관관계의 주인 - 나중에 보면 성능이슈도 있을 수 있음

![img4](./image/image4.png)

#### 양방향 매핑시 가장 많이 하는 실수

연관관계의 주인에 값을 입력하지 않음

- 순수 객체 상태를 고려해서 항상 양쪽에 값을 설정하자
- 연관관계 편의 메소드를 생성하자

```java
public void changeTeam(Team team) {
    this.team = team;
    team.getMembers().add(this);
}
```

- 양방향 매핑시에 무한 루프를 조심하자
    - toString(), lombok,
    - JSON 생성 라이브러리 - controller에는 entity를 반환하지 말아라!!!!!!!! DTO로 변경해서 반환해라

##### 양방향 매핑 정리

- 단방향 매핑만으로도 이미 연관관계 매핑은 완료
- 양방향 매핑은 반대 방향으로 조회 기능이 추가된 것 뿐
- JPQL에서 역방향으로 탐색할 일이 많음
- 단방향 매핑을 잘하고 양방향은 필요할 때 추가해도 됨

![img4](./image/image5.png)

### 다양한 연관관계 매핑

- 다중성
- 단방향, 양방향
- 연관관계의 주인

#### 다대일 [N:1]

- 외래 키가 있는 쪽이 연관관계의 주인
- 양쪽을 서로 참조하도록 개발
- mappedBy 속성 X

#### 일대다[1:N]

- 일대다 단방향은 일대다에서 일이 연관관계의 주인
- 테이블 일대다 관계는 항상 다쪽에 외래 키가 있음
- 객체와 테이블의 차이 때문에 반대편 테이블의 외래 키를 관리하는 특이한 구조
- @JoinColumn을 꼭 사용해야함 그렇지 않으면 조인 테이블 방식을 사용

#### 일대일[1:1]

- 주 테이블이나 대상 테이블 중에 외래 키 선택 가능
- 외래 키에 데이터에비스 유니크 제약조건 추가

> 주 테이블에 외래 키
>
>​ • 주 객체가 대상 객체의 참조를 가지는 것 처럼 주 테이블에 외래 키를 두고 대상 테이블을 찾음
>
>​ • 객체지향 개발자 선호
>
>​ • JPA 매핑 편리
>
>​ • 장점: 주 테이블만 조회해도 대상 테이블에 데이터가 있는지 확인 가능
>
>​ • 단점: 값이 없으면 외래 키에 null 허용
>
>대상 테이블에 외래 키
>
>​ • 대상 테이블에 외래 키가 존재
>
>​ • 전통적인 데이터베이스 개발자 선호
>
>​ • 장점: 주 테이블과 대상 테이블을 일대일에서 일대다 관계로 변경할 때 테이블 구조 유지
>
>​ • 단점: 프록시 기능의 한계로 지연 로딩으로 설정해도 항상 즉시 로딩됨(프록시는 뒤에서 설명)

#### 다대다 [N:M]

- 관계형 데이터베이스는 정규화된 테이블 2개로 다대다 관계를 표현할 수 없음
- 연결 테이블을 추가해서 일대다, 다대일 관계로 풀어내야함
- 객체는 컬렉션을 사용해서 객체 2개로 다대다 관계가능

> MemberProduct를 만들고 entity로 승격한다.
> ManyToOne, OneToMany

### 고급 매핑

#### 상속관계 매핑

- 관계형 데이터베이스는 상속 관계 X
- 슈퍼타입 서브타입 관계라는 모델링 기법이 객체 상속과 유사
- 상속관계 매핑 : 객체의 상속과 구조와 DB의 슈퍼타입 서브타입 관계를 매핑

#### 주요 어노테이션

- @Inheritance(strategy = InheritanceType.XXX)

    - JOINED : 조인전략

      > 장점
      >
      > - 테이블 정규화
      > - 외래 키 참조 무결성 제약조건 활용가능
      > - 저장공간 효율화
      >
      > 단점
      >
      > - 조회시 조인을 많이 사용, 성능 저하
      > - 조회 쿼리가 복잡함
      > - 데이터 저장시 INSERT SQL 2번 호출

    - SINGLE_TABLE : 단일 테이블 전략

      > 장점
      >
      > - 조인이 필요 없으므로 일반적으로 조회 성능이 빠름
      > - 조회 쿼리가 단순
      >
      > 단점
      >
      > - 자식 엔티티가 매핑한 컬럼은 모두 null 허용
      > - 단일 테이블에 모든 것을 저장하므로 테이블이 커질 수 있다. 상황에 따라서 조회 성능이 오히려 느려질 수 있다.

    - TABLE_PER_CLASS : 구현 클래스마다 테이블 전략

      > 장점
      >
      > - 서브 타입을 명확하게 구분해서 처리할 때 효과적
      > - not null 제약조건 사용 가능
      >
      > 단점
      >
      > - 여러 자식 테이블을 함께 조회할 때 성능이 느림(UNION SQL 필요)
      > - 자식 테이블을 통합해서 쿼리하기 어려움

- @DiscriminatorColumn (name="DTYPE") : 무엇 때문에 들어왔는지 같이 표시해줌
- @DiscriminatorValue("A")

#### @MappedSuperclass

- 공통 매핑 정보가 필요할 때

> - 테이블과 관계 없고, 단순히 엔티티가 공통으로 사용하는 매핑 정보를 모으는 역할
> - 주로 등록일, 수정일, 등록자, 수정자 같은 전체 엔티티에서 공통으로 적용하는 정보를 모을 때 사용
> - 참고: @Entity 클래스는 엔티티나 @MappedSuperclass로 지정한 클래스만 상속 가능

### 프록시와 연관관계 관리

- 프록시 기초

  > em.find() : 데이터베이스를 통해서 실제 엔티티 객체 조회
  >
  > em.getReference() : 데이터베이스 조회를 미루는 가짜 (프록시) 엔티티 객체 조회

- 프록시 특징

  > 실제 클래스를 상속 받아서 만들어짐
  >
  > 실제 클래스와 겉 모양이 같다.
  >
  > 사용하는 입장에서는 진짜 객체인지 프록시 객체인지 구분하지 않고 사용하면 됨 (이론상)

  > - 프록시 객체는 처음 사용할 때 한 번만 초기화
  >
  >- 프록시 객체를 초기화 할 때, 프록시 객체가 실제 엔티티로 바뀌는 것은 아님, 초기화되면 프록시 객체를 통해서 실제 엔티티에 접근 가능
  >
  >- 프록시 객체는 원본 엔티티를 상속 받음, 따라서 타입 체크시 주의
     >
     >  (==이 아닌 , instance of)
  >
  >- 영속성 컨텍스트에 찾는 엔티티가 이미 있으면 em.getReference()를 호출해서 실제 엔티티 반환
  >
  >- 영속성 컨텍스트의 도움을 받을 수 없는 준영속 상태일 때, 프록시를 초기화하면 문제 발생
     >
     >  (org.hibernate.LazyInitializationException 예외)
  >
  >!!! 신기한점
  >
  >getReference 를 해서 proxy 했음
  >
  >find도 proxy를 반환 왜냐 jpa에서는 == 을 true로 반환해주게하려고

- 프록시 확인

  > - 프록시 인스턴스의 초기화 여부 확인
  >
  > PersistenceUnitUtil.isLoaded(Object entity)
  >
  > - 프록시 클래스 확인 방법
  >
  > entity.getClass().getName()
  >
  > - 프록시 강제 초기화
  >
  > Hibernate.initialize()
  >
  > - JPA 표준은 강제 초기화 없음
  >
  > member.getName() - 강제호출하면됨

#### 즉시 로딩과 지연 로딩

지연 로딩 LAZY을 사용해서 프록시로 조회

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "TEAM_ID")
private Team team;
```

즉시 로딩 - 멤버 팀을 거의 같이 사용

```java
@ManyToOne(fetch = FetchType.EAGER)
```

#### 프록시와 즉시로딩 주의

- 가급적 지연 로딩만 사용
- 즉시 로딩을 적용하면 예상하지 못한 SQL이 발생
- 즉시 로딩은 JPQL에서 N + 1 문제를 일으킨다.
- @ManyToOne, @OneToOne 은 기본이 즉시 로딩 -> LAZY로 설정
- @OneToMany, @ManyToMany는 기본이 지연 로딩

#### 지연 로딩 활용 - 실무

- 모든 연관관계에 지연 로딩
- 실무에서 즉시 로딩 X
- JPQL fetch 조인이나, 엔티티 그래프 기능을 사용
- 즉시 로딩은 상상하지 못한 쿼리가 나감

#### 영속성 전이(CASCADE)와 고아 객체

- 특정 엔티티를 영속 상태로 만들 때 연관된 엔티티도 함께 영속 상태로 만들고 싶을 때
- 부모 엔티티를 저장할 때 자식 엔티티도 함께 저장

주의!

- 영속성 전이는 연관관계를 매핑하는 것과 아무 관련이 없음
- 엔티티를 영속화할 때 연관된 엔티티도 함께 영속화하는 편리함을 제공할 분

ALL: 모두 적용 • PERSIST: 영속 • REMOVE: 삭제 • MERGE: 병합 • REFRESH: REFRESH • DETACH: DETACH

#### 고아 객체

- 고아 객체 제거 : 부모 엔티티와 연관관계가 끊어진 자식 엔티티를 자동으로 삭제

  orphanRemoval = true

#### 주의!

- 참조하는 곳이 하나일 때 사용해야함!
- 특정 엔티티가 개인 소유할 때 사용
- OneToOne, OneToMany만 사용

#### 영속성 전이 + 고아 객체, 생명주기

- CascadeType.ALL + orphanRemovel=true

- 스스로 생명주기를 관리하는 엔티티는 em.persist()로 영속화, em.remove()로 제거

- 두 옵션을 모두 활성화 하면 부모 엔티티를 통해서 자식의 생명 주기를 관리할 수 있음

- 도메인 주도 설계(DDD)의 Aggregate Root개념을 구현할 때 유용

### 값 타입

#### 기본 값 타입

- JPA는 엔티티 타입
    - @Entity로 정의하는 객체
    - 데이터가 변해도 식별자로 지속해서 추적 가능
    - 예) 회원 엔티티의 키나 나이 값을 변경해도 식별자로 인식 가능
- 값 타입으로 나눔
    - int, Integer, String처럼 단순히 값으로 사용하는 자바 기본 타입이나 객체
    - 식별자가 없고 값만 있으므로 변경시 추적 불가
    - 예) 숫자 100을 200으로 변경하면 완전히 다른 값으로 대체

#### 값 타입 분류

##### 기본값 타입

- 기본 타입은 항상 값을 복사함
- Integer 같은 래퍼 클래스나 String 같은 특수한 클래스는 공유 가능한 객체이지만 변경 X

##### 임베디드 타입 (복합 값 타입) - x,y 좌표 같은 것

- 새로운 값 타입을 직접 정의할 수 있음
- JPA는 임베디드 타입이라 함
- 주로 기본 값 타입을 모아서 만들어서 복합 값 타입이라고도 함
- int, String과 같은 값 타입

> @Embeddable : 값 타입을 정의하는 곳에 표시
>
> @Embedded : 값 타입을 사용하는 곳에 표시
>
> 기본 생성자 필수
>
> 장점
>
> - 재사용
> - 높은 응집도
> - Period.isWork()처럼 해당 값 타입만 사용하는 의미 있는 메소드를 만들 수 있음
> - 임베디드 타입을 포함한 모든 값 타입은, 값 타입을 소유한 엔티티에 생명주기를 의존함

##### 임베디드 타입과 매핑

- 임베디드 타입은 엔티티의 값일 뿐이다.

- 임베디드 타입을 사용하기 전과 후에 매핑하는 테이블은 같다
- 객테와 테이블을 아주 세밀하게 매핑하는 것이 가능
- 잘 설계한 ORM 애플리케이션은 매핑한 테이블의 수보다 클래스의 수가 더 많음

> @AttributeOverride: 속성 재정의
>
> - 한 엔티티에서 같은 값 타입을 사용
> - @AttributeOverrides, @AttributeOverride를 사용해서 컬럼 명 속성을 재정의

- 임베디드 타입이 null 이면 값들도 모두 null

##### 값 타입과 불변 객체

값 타입은 복잡한 객체 세상을 조금이라도 단순화하려고 만든 개념이다. 따라서 값 타입은 단순하고 안전하게 다룰 수 있어야 한다.

- 임베디드 타입 같은 값 타입을 여러 엔티티에서 공유하면 위험함
- side effect 발생
- 값을 복사해서 사용해야한다.

##### 객체 타입의 한계

- 항상 값을 복사해서 사용하면 공유 참조로 인해 발생하는 부작용을 피할 수 있따.
- 문제는 임베디드 타입처럼 직접 정의한 값 타입은 자바의 기본 타입이 아니라 객체 타입이다.
- 자바 기본 타입에 값을 대입하면 값을 복사한다.
- 객체 타입은 참조 값을 직접 대입하는 것을 막을 방법이 없다.
- 객체의 공유 참조는 피할 수 없다.

##### 불변 객체

- 객체 타입을 수정할 수 없게 만들면 부작용을 원천 차단

- 값 타입은 불변객체 (immutable object)로 설계해야함
- 불변 객체 : 생성 시점 이후 절대 값을 변경할 수 없는 객체
- 생성자로만 값을 설정하고 setter를 만들지 않으면 됨
- Integer String 같은 경우 자바가 제공하는 대표적인 불변 객체

##### 값 타입의 비교

값 타입: 인스턴스가 달라도 그 안에 값이 같으면 같은 것으로 봐야함

> 동일성(==)과 동등성(equals()) 구분

equals 기본 제공 하는 메소드를 사용하는 것이 좋음 (equals override 하면 hashCode도 override함)

##### 값 타입 컬렉션

- 값 타입을 하나 이상 저장할 때 사용
- @ElementCollection, @CollectionTable 사용
- 데이터베이스는 컬렉션을 같은 테이블에 저장할 수 없다.
- 컬렉션을 저장하기 위한 별도의 테이블이 필요함

##### 값 타입 컬렉션의 제약사항

- 값 타입은 엔티티와 다르게 식별자 개념이 없다.
- 값은 변경하면 추적이 어렵다
- 값 타입 컬렉션에 변경 사항이 발생하면, 주인 엔티티와 연관된 모든 데이터를 삭제하고, 값 타입 컬렉션에 있는 현재 값을 모두 다시 저장한다.
- 값 타입 컬렉션을 매핑하는 테이블은 모든 컬럼을 묶어서 기본키를 구성해야 함 : null 입력 X, 중복 저장 X

##### 값 타입 컬렉션 대안

- 실무에서는 상황에 따라 값 타입 컬렉션 대신에 일대다 관계를 고려
- 일대다 관계를 위한 엔티티를 만들고, 여기에서 값 타입을 사용
- 영속성 전이 + 고아 객체 제거를 사용해서 값 타입 컬렉션 처럼 사용

### 객체지향 쿼리 언어

JPA는 다양한 쿼리 방법을 지원

##### JPQL

> JPA를 사용하면 엔티티 객체를 중심으로 개발
>
> 문제는 검색 쿼리
>
> 검색을 할 때도 테이블이 아닌 엔티티 객체를 대상으로 검색
>
> 모든 DB데이터를 객체로 변환해서 검색하는 것은 불가능
>
> 애플리케이션이 필요한 데이터만 DB에서 불러오려면 결국 검색 조건이 포함된 SQL이 필요

- JPA는 SQL을 추상화한 JPQL이라는 객체 지향 쿼리 언어 제공
- SQL과 문법 유사, SELECT, FROM, WHERE, GROUP BY, HAVING, JOIN 지원
- JPQL은 엔티티 객체를 대상으로 쿼리
- SQL은 데이터베이스 테이블을 대상으로 쿼리

```java
List<Member> result = em.createQuery(
	"select m From Member m where m.name like '%kim'",
    Member.class
).getResultList();
// 동적쿼리 만들기 어려움
```

##### JPA Criteria

```java
//Criteria 사용 준비
CriteriaBuilder cb = em.getCriteriaBuilder(); 
CriteriaQuery<Member> query = cb.createQuery(Member.class); 

//루트 클래스 (조회를 시작할 클래스)
Root<Member> m = query.from(Member.class); 

//쿼리 생성 
CriteriaQuery<Member> cq = query.select(m).where(cb.equal(m.get("username"), “kim”)); 
List<Member> resultList = em.createQuery(cq).getResultList();
```

##### QueryDSL **

- 문자가 아닌 자바코드로 JPQL을 작성할 수 있음
- JPQL 빌더 역할
- 컴파일 시점 문법 오류를 찾을 수 있음
- 동적쿼리 작성 편리함

```java
//JPQL
//select m from Member m where m.age > 18
JPAFactoryQuery query = new JPAQueryFactory(em);
QMember m = QMember.member;

List<Member> list =
    query.selectFrom(m)
    		.where(m.age.gt(18))
    		.orderBy(m.name.desc())
    		.fetch();
```

##### 네이티브 SQL

- JPA가 제공하는 SQL을 직접 사용하는 기능
- JPQL로 해결할 수 없는 특정 데이터베이스에 의존적인 기능

##### JDBC API 직접 사용, MyBatis, SpringJdbcTemplate 함께 사용

- 영속성 컨텍스트를 적절한 시점에 강제로 플러시 필요

#### JPQL (Java Persistence Query Language)

##### JPQL 소개

- JPQL은 객체지향 쿼리 언어다. 따라서 테이블을 대상으로 쿼리하는 것이 아니라 엔티티 객체를 대상으로 쿼리한다.
- JPQL은 SQL을 추상화해서 특정데이터베이스 SQL에 의존하지 않는다.
- JPQL은 결국 SQL로 변환된다.

##### JPQL 문법

- select m from Member as m where m.age > 18
- 엔티티와 속성은 대소문자 구분O
- JPQL 키워드는 대소문자 구분X
- 엔티티 이름 사용, 테이블 이름이 아님
- 별칭은 필수(m)

##### TypeQuery, Query

- TypeQuery : 반환 타입이 명확할 때 사용
- Query : 반환 타입이 명확하지 않을 때 사용

> 결과 조회 API
>
> - query.getResultList() : 결과가 하나 이상일 때, 리스트 반환
    >
- 결과가 없으면 빈 리스트 반환
> - query.getSingleResult() : 결과가 정확히 하나, 단일 객체 반환
    >
- 결과가 없으면 : javax.persistence.NoResultException
>   - 둘 이상이면 : javax.persistence.NonUniqueResultException

```java
Member result = em.createQuery("select m from Member m where m.username = :username", Member.class).setParameter("username", "member1").getSingleResult();
```

##### 프로젝션

- SELECT 절에 조회할 대상을 지정하는 것
- 프로젝션 대상 : 엔티티, 임베디드 타입, 스칼라 타입(숫자, 문자등 기본 데이터 타입)

```
SELECT m FROM Member m -> 엔티티 프로젝션
SELECT m.team FROM Member m -> 엔티티 프로젝션 - 묵시적 join
SELECT m.address FROM Member m -> 임베디드 타입 프로젝션
SELECT m.username, m.age FROM Member m -> 스칼라 타입 프로젝션
```

##### 프로젝션 - 여러 값 조회

> SELECT m.username, m.age FROM Member m
>
> • 1. Query 타입으로 조회
>
> • 2. Object[] 타입으로 조회
>
> • 3. new 명령어로 조회
>
> 단순 값을 DTO로 바로 조회 SELECT new jpabook.jpql.UserDTO(m.username, m.age) FROM  Member m 패키지 명을 포함한 전체 클래스 명 입력
> 순서와 타입이 일치하는 생성자 필요

##### 페이징 API

- JPA는 페이징을 다음 두 API로 추상화
- setFirstResult(int startPosition) : 조회 시작 위치 (0부터 시작)
- setMaxResults(int maxResult) : 조회할 데이터 수

```java
String jpql = "select m from Member m order by m.name desc";
 List<Member> resultList = em.createQuery(jpql, Member.class)
 .setFirstResult(10)
 .setMaxResults(20)
 .getResultList();
```

##### 조인

```
내부 조인:
SELECT m FROM Member m [INNER] JOIN m.team t
외부 조인:
SELECT m FROM Member m LEFT [OUTER] JOIN m.team t 
세타 조인: 
select count(m) from Member m, Team t where m.username = t.name
```

##### 서브 쿼리

```
나이가 평균보다 많은 회원
select m from Member m
where m.age > (select avg(m2.age) from Member m2) 
한 건이라도 주문한 고객
select m from Member m
where (select count(o) from Order o where m = o.member) > 0
```

##### 서브 쿼리 지원 함수

[NOT] EXISTS (subquery): 서브쿼리에 결과가 존재하면 참

• {ALL | ANY | SOME} (subquery)

• ALL 모두 만족하면 참

• ANY, SOME: 같은 의미, 조건을 하나라도 만족하면 참

[NOT] IN (subquery): 서브쿼리의 결과 중 하나라도 같은 것이 있으면 참

##### JPA 서브 쿼리 한계

- JPA는 WHERE, HAVING 절에서만 서브 쿼리 사용 가능
- SELECT 절도 가능(하이버네이트에서 지원)
- FROM 절의 서브 쿼리는 현재 JPQL에서 불가능
    - 조인으로 풀 수 있으면 풀어서 해결

##### JPQL 타입 표현

> 문자: ‘HELLO’, ‘She’’s’
>
>숫자: 10L(Long), 10D(Double), 10F(Float)
>
>Boolean: TRUE, FALSE
>
>ENUM: jpabook.MemberType.Admin (패키지명 포함)
>
>엔티티 타입: TYPE(m) = Member (상속 관계에서 사용)

##### JPQL 기타

> SQL과 문법이 같은 식
>
>EXISTS, IN
>
>AND, OR, NOT
>
>=, >, >=, <, <=, <>
>
>BETWEEN, LIKE, IS NULL
>
>#####  

##### 조건식

> COALESCE: 하나씩 조회해서 null이 아니면 반환
>
> NULLIF: 두 값이 같으면 null 반환, 다르면 첫번째 값 반환

##### JPQL 기본 함수

> CONCAT
>
>SUBSTRING
>
>TRIM
>
>LOWER, UPPER
>
>LENGTH
>
>LOCATE
>
>ABS, SQRT, MOD
>
>SIZE, INDEX(JPA 용도)

##### 사용자 정의 함수 호출

> 하이버네이트는 사용전 방언에 추가해야 한다.
>
> 사용하는 DB 방언을 상속받고, 사용자 정의 함수를 등록한 다.
>
> select function('group_concat', i.name) from Item i

##### 경로 표현식

!! join 쿼리가 묵시적으로 나가기 때문에 join이 나타나게 짜면 좋다.

--- 명시적 조인 사용하기

상태필드 - 단순히 값을 저장하기 위한 필드 m.username 연관필드 - 연관관계를 위한 필드 단일 값 연관 필드 : 묵시적 내부 조인 발생, 탐색 o m.team.name (many to many, one to
one)
컬렉션 값 연관 경로 : 묵시적 내부 조인 발생, 탐색 x -> FROM 절에서 명시적 조인을 통해 별칭을 얻으면 별칭을 통해 탐색 가능

##### JPQL fetch join

- SQL 조인 종류 X
- JPQL에서 성능 최적화를 위해 제공하는 기능
- 연관된 엔티티나 컬렉션을 SQL 한 번에 함께 조회하는 기능
- join fetch 명령어 사용

!!!

```sql
JPQL
SELECT m FROM Member m join jetch m.team
SQL
SELECT M.*, T.* FROM MEMBER M INNER JOIN TEAM T ON M.TEAM_ID = T.ID
```

회원 100명 -> N + 1 문제가 발생한다. fetch를 하면 query 한 번으로 다 가져온다.

fetch join하면 컬렉션 데이터가 뻥튀기 된다. 디비 입장에서는 1:N 이면 데이터가 뻥튀기 된다.

DISTINCT를 추가하면 전부 가져온 뒤 collection에서 중복을 해결해준다.

##### 페치 조인과 일반 조인의 차이

- 일반 조인 실행시 연관된 엔티티를 함께 조회하지 않음
- JPQL은 결과를 반환할 때 연관관계 고려X
- SELECT 절에 지정한 엔티티만 조회

### 페치 조인의 특징과 한계 !!!!!!!!!!!!!

- 페치 조인 대상에는 별칭을 줄 수 없다.
- 둘 아상의 컬렉션은 페치 조인 할 수 없다.
- 컬렉션을 페치 조인하면 페이징 API를 사용할 수 없다.
    - 경고를 남기고 메모리에서 페이징 : 일대일, 다대일 같은 단일 값 연관 필드들은 페치 조인해도 페이징 가능
    - 엔티티에 @BatchSize(100)으로 하면 in해서 필요한 개수를 가져온다. >> global setting 으로 할 수 있다.
      (hibernate.default_batch_fetch_size)

- 연과된 엔티티들을 SQL 한 번으로 조회 - 성능 최적화
- 엔티티에 직접 적용하는 글로벌 로딩 전략보다 우선한 -- @OneToMany(fetch = FetchType.LAZY) //글로벌 로딩 전략
- 실무에서 글로벌 로딩 전략은 모두 지연 로딩
- 최적화가 필요한 곳은 페치 조인 적용
  (데이터 씽크 맞추는 대량 api - fetch 조인하고 @BatchSize 40분 -> 4분도 안 걸리게 됐다.)
  선언적으로

##### JPQL - 다형성 쿼리

```sql
select i from item i where type(i) IN (Book, Movie)
type(i) -> dtype으로 바뀜

select i from item i where treat(i as book).author = "kim"
-> select i.* from item i where i.dtype = 'b' and i.author = "kim"
```

##### JPQL - 엔티티 직접 사용

- JPQL에서 엔티티르 직접 사용하면 SQL에서 해당 엔티티의 기본 키 값을 사용

```sql
select count(m.id) from Member m // 엔티티의 아이디를 사용
select count(m) from Member m // 엔티티를 직저 사용
--> 결국 id를 이용해서 만드는 쿼리로 바뀜
```

##### JPQL - Named쿼리 - 정적 쿼리

- 미리 정의해서 이름을 부여해두고 사용하는 JPQL
- 정적 쿼리
- application 로딩 시점에 sql로 parsing해서 캐싱하고 있음
- 애플리케이션 로딩 시점에 쿼리를 검증

spring data jpa 에서는 @Query 사용하면 named query로 등록 된다.

##### 벌크 연산

- 재고가 10개 미망인 모든 상품의 가격을 10% 상승하려면?
- JPA 변경 감지 기능으로 실행하려면 너무 많은 SQL 실행

```java
em.createQuery("update Member m set m.age = 20")
    .excuteUpdate()
```

!!! 벌크 연산 주의

- 벌크 연산은 영속성 컨텍스트를 무시하고 데이터베이스에 직접 쿼리
- 벌크 연산을 먼저 실행
- 벌크 연산 수행 후 영속성 컨텍스트 초기화(!!)