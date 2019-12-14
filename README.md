# Twitter-Clone-BackEnd

> `Spring WebFlux`를 공부하기 위해 진행한 트위터 클론 프로젝트 저장소입니다.

## 사용언어 / 라이브러리
- [Java8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [SpringBoot >= 2.2.1](https://start.spring.io/)
- WebFlux 사용

## API
- [링크](https://www.notion.so/ks14/API-3bebab565db14bac92425232bcbeaf7c)

## 사용방법

1. 압축 파일 다운로드
2. 압축 풀기
3. `IntelliJ`에서 열기
4. `TwitterCloneApplication.java`실행 OR `terminal` -> `gradle bootRun`


### JWT사용법
1. API를 참고하여, 로그인 후 `token`을 반환받는다.
  - `Token Example`
    - `eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjpbIlVTRVIiXSwic3ViIjoidGVzdEBnbWFpbC5jb20iLCJpYXQiOjE1NzYyMTk5OTAsImV4cCI6MTU3NjIzNzk5MH0.RE_C_mT4Ev-gkuUD0j3MlIlMjMQlIrG4yW76tIsqujfbEC2LNHjeiv2M5cexYR9WOsXLJkFW4BjVOIf29iAjzw`

2. 토큰 앞에 "Bearer(공백 1칸)"를 붙인다.
  - `Bearer eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjpbIlVTRVIiXSwic3ViIjoidGVzdEBnbWFpbC5jb20iLCJpYXQiOjE1NzYyMTk5OTAsImV4cCI6MTU3NjIzNzk5MH0.RE_C_mT4Ev-gkuUD0j3MlIlMjMQlIrG4yW76tIsqujfbEC2LNHjeiv2M5cexYR9WOsXLJkFW4BjVOIf29iAjzw`

3. `HEADER`의 `Authorization`의 `value`로 설정해 요청한다.
