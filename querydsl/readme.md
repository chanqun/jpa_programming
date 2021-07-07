### Querydsl

자바파일이기 때문에 컴파일 시점에서 오류를 발견할 수 있다.

querydsl 설정을하고 assermble, compile, querydsl
build/generated/source/kapt/main/같은위치/QHello
에 QHello가 생긴다. --> git에 올리면 안돼

검색 조건
>
> eq, ne, eq.not, istNotNull, in, notIn, between, goe, gt, loe, lt, like, contains, startsWith



#### subquery
from 절의 서브쿼리 한계 
- JPQL은 지원하지 않음, 하이버네이트가 지원해줘서 가능

from 절의 서브쿼리 해결방안
1. 서브쿼리는 Join으로 변경 (가능한 상황도 있고, 불가능한 상황도 있음)
2. 애플리케이션에서 쿼리를 2번 분리해서 실행
3. nativeSQL을 사용

--> DB는 데이터만 가져오고 로직은 애플리케이션에서만 사용하면 되지 않나 --> 복잡한 쿼리를 만들 수 있음
(SQL antipattern 이라는 책도 있음)