## 자바 ORM 표준 JPA 프로그래밍 

[JPA소개](#JPA-소개)  
[JPA시작하기](#JPA-시작하기)  
[영속성 관리 - 내부 동작 방식](#영속성관리-내부-동작-관리)  

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

패러다임의 불일치
객체 vs 관계형 데이터베이스

객체를 영구 보관하는 다양한 저장소 DB

객체와 관계형 데이터베이스의 차이
1. 상속 
db에서 데이터를 조회할때 join해서 가져와야한다. 
2. 연관관계 
객체다운 모델링 - 팀id가 아닌 팀을 넣어야하는 것이 아닌가?
4. 데이터 타입
5. 데이터 식별 방법

##### JPA (Java Persistence API)
ORM (Object-relation mapping)

JPA 동작 - 저장
Entitiy Object를 넘기면 - Entity 분석 JDBC API - INSERT

#### JPA 소개
EJB라는 것이 있었는데 엉망이었음 - JPA (자바 표준)
                           하이버네이트(오픈소스)
                           
#### JPA는 표준 명세
- JPA는 인터페이스의 모음 - jpa는 인터페이스
- 하이버네이트, EclipseLink, DataNucleus 구현체

#### 생산성 - JPA와 CRUD
사상자체가 자바 컬렉션에 값을 넣었다가 뺐다가 하는 것과 같다.
저장 jpa.persist
조회 jpa.find
수정 member.setName
삭제 jpa.remove

jpa 동일한 트랜잭션에서 조회한 엔티티는 같음을 보장

#### JPA의 성능 최적화 기능
1. 1차 캐시와 동일성(identity)보장 - DB Isolation Level이 Read Commit이어도 애플리케이션에서 Repeatable Read 보장
3. 트랜잭션을 지원하는 쓰기 지연 (transactional write-behind) - JDBC BATCH SQL 기능을 가능하게 해줌 transaction.begin()
4. 지연 로딩(Lazy Loading) - 옵션하나로 지연로딩이 가능하다.

ORM은 객체와 RDB 두 기둥위에 있는 기술

### JPA 시작하기

