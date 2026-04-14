# 재고키퍼 (JaegoKeeper Backend)

<p align="center">
  <img src="https://dummyimage.com/1200x500/f3f4f6/111827&text=JaegoKeeper+Service+Image+(Replace+Later)" alt="jaegokeeper-main" width="900" />
</p>

> 점포 운영자가 재고, 알바, 스케줄, 요청을 한 곳에서 관리할 수 있도록 만든 Spring MVC 기반 백엔드 서비스

<br/>

## 목차
- [팀원](#팀원)
- [기술 스택](#기술-스택)
- [프로젝트 관련 주소](#프로젝트-관련-주소)
- [배포 아키텍처](#배포-아키텍처)
- [ERD](#erd)
- [서비스 소개](#서비스-소개)
- [서비스의 필요성](#서비스의-필요성)
- [서비스 핵심 기능](#서비스-핵심-기능)
- [프로젝트 구조](#프로젝트-구조)
- [최근 개선 사항](#최근-개선-사항)

---

## 팀원

<h3 align="center">Collaborators</h3>

<h4 align="center">Backend (Jachodan/jachodan-spring)</h4>

<div align="center">

| 이승환 | 이하성 | 박소정 |
| --- | --- | --- |
| [@hwanzanghagetne](https://github.com/hwanzanghagetne) | [@revy7289](https://github.com/revy7289) | [@ssojeongg](https://github.com/ssojeongg) |
| <img src="https://github.com/hwanzanghagetne.png" width="100" /> | <img src="https://github.com/revy7289.png" width="100" /> | <img src="https://github.com/ssojeongg.png" width="100" /> |

</div>

<h4 align="center">Frontend (Jachodan/jachodan-next)</h4>

<div align="center">

| 김수연 | 정재훈 |
| --- | --- |
| [@kimsudang](https://github.com/kimsudang) | [@jaehunGit](https://github.com/jaehunGit) |
| <img src="https://github.com/kimsudang.png" width="100" /> | <img src="https://github.com/jaehunGit.png" width="100" /> |

</div>

---

## 기술 스택

### Backend
![java 17](https://img.shields.io/badge/-Java%2017-ED8B00?style=flat-square&logo=java&logoColor=white)
![spring mvc 5.3](https://img.shields.io/badge/Spring%20MVC%205.3-6DB33F?style=flat-square&logo=spring&logoColor=white)
![mybatis 3.5](https://img.shields.io/badge/MyBatis%203.5-BE1E2D?style=flat-square&logoColor=white)
![mysql 8.0](https://img.shields.io/badge/MySQL%208.0-005C84?style=flat-square&logo=mysql&logoColor=white)
![swagger](https://img.shields.io/badge/Swagger%202.9.2-85EA2D?style=flat-square&logo=swagger&logoColor=black)

### Frontend
![react](https://img.shields.io/badge/React%2019-61DAFB?style=flat-square&logo=react&logoColor=black)
![nextjs 16](https://img.shields.io/badge/Next.js%2016-000000?style=flat-square&logo=nextdotjs&logoColor=white)
![typescript](https://img.shields.io/badge/TypeScript-3178C6?style=flat-square&logo=typescript&logoColor=white)
![nextauth](https://img.shields.io/badge/NextAuth.js-000000?style=flat-square&logo=auth0&logoColor=white)

### Infra / Deploy
![nginx](https://img.shields.io/badge/Nginx-009639?style=flat-square&logo=nginx&logoColor=white)
![tomcat 9](https://img.shields.io/badge/Tomcat%209-F8DC75?style=flat-square&logo=apachetomcat&logoColor=black)
![aws ec2](https://img.shields.io/badge/AWS%20EC2-FF9900?style=flat-square&logo=amazonec2&logoColor=white)
![maven](https://img.shields.io/badge/Maven-1C1C1C?style=flat-square&logo=apachemaven&logoColor=white)

---

## 프로젝트 관련 주소

<div align="center">

| 문서 |
|:---:|
| [프론트엔드 배포 주소](https://github.com/Jachodan/jachodan-next) |
| [백엔드 API 문서](http://98.84.8.224/swagger-ui.html) |
| [프로젝트 노션](https://www.notion.so/Jachodan-228b76bc91b880c2b4e6c54facfd6395) |

</div>

---

## 배포 아키텍처

<p align="center">
  <img src="https://dummyimage.com/1200x520/eef2ff/1f2937&text=Nginx(80/443)+%E2%86%92+Tomcat(8080)+%E2%86%92+Spring+MVC+%E2%86%92+MySQL" alt="deployment-architecture" width="900" />
</p>

- 외부 요청: `Nginx`
- 애플리케이션: `Tomcat 9`에 `WAR` 배포
- DB: `MySQL 8`
- 인증: `HttpSession` + `SessionInterceptor` + 서비스 계층 점포 권한 검증

---

## ERD

<p align="center">
  <img src="https://dummyimage.com/1200x520/fef3c7/1f2937&text=ERD+Image+(Replace+Later)" alt="jaegokeeper-erd" width="900" />
</p>

> TODO: 실제 ERD 이미지로 교체 예정

---

## 서비스 소개

재고키퍼는 소상공인/매장 운영 환경에서 자주 발생하는 운영 관리 분산 문제를 해결하기 위한 서비스입니다.

- 인증 후 본인 점포 데이터만 접근하도록 스코프를 강제
- 재고, 입출고, 요청, 알바, 스케줄 업무를 통합 관리
- 이미지/게시판 기능으로 운영 커뮤니케이션 보완

---

## 서비스의 필요성

### 문제 1. 운영 데이터가 여러 채널에 흩어짐
- 재고는 메모/엑셀, 근무는 메신저, 요청은 구두 전달로 관리되는 경우가 많아 누락이 발생합니다.

### 해결
- 점포 단일 시스템에서 재고/요청/알바/스케줄을 함께 관리하여 정보 단절을 줄입니다.

<br/>

### 문제 2. 점포 데이터 권한 경계가 쉽게 무너질 수 있음
- 멀티 점포 구조에서 권한 검증이 일관되지 않으면 타 점포 데이터 노출 위험이 생깁니다.

### 해결
- 인터셉터 + 서비스 계층 이중 검증으로 `storeId` 접근 권한을 강제합니다.

<br/>

### 문제 3. 인증/회원가입 플로우와 실사용 플로우의 불일치
- 소셜 로그인, 로컬 로그인, 이메일 인증이 분리되어 있으면 사용자 혼선과 운영 이슈가 커집니다.

### 해결
- 로컬/소셜/이메일 인증 흐름을 문서화하고 예외 응답을 일관화하여 운영 안정성을 높입니다.

---

## 서비스 핵심 기능

### 1) 인증/회원
- 로컬 로그인, 소셜 로그인(Google/Kakao/Naver)
- 이메일 인증 기반 회원가입(Onboarding)
- 세션 기반 인증 + 로그인 상태 검증 API

### 2) 점포 운영
- 상품(Item) 등록/수정/조회
- 재고(Stock) 입출고 및 수량 관리
- 요청(Request) 등록/조회/처리

### 3) 알바/스케줄
- 알바 등록/수정/상태 관리
- 스케줄 등록 및 근무 흐름 관리

### 4) 커뮤니티/파일
- 게시판(Board) CRUD
- 이미지 업로드/조회

---

## 프로젝트 구조

<div align="center">

| 패키지 | 역할 |
|:---:|:---|
| `auth` | 로그인/세션/소셜 인증 |
| `onboarding` | 사장 회원가입 플로우 |
| `item` / `stock` / `request` | 점포 운영 핵심 도메인 |
| `alba` / `schedule` | 인력/근무 관리 |
| `board` | 게시판 기능 |
| `image` | 이미지 업로드/조회 |
| `store` / `user` / `email` | 점포/회원/메일 기능 |
| `exception` | `ErrorCode`, 전역 예외 처리 |
| `config` | Async, Swagger, Mail 설정 |
| `mappers` | MyBatis SQL 매퍼 XML |

</div>

<details>
<summary>패키지 트리 보기</summary>

```text
src/main/java/com/jaegokeeper
├─ auth
├─ onboarding
├─ item / stock / request
├─ alba / schedule
├─ board
├─ image
├─ store / user / email
├─ exception
└─ config

src/main/resources/mappers
```

</details>

---

## 최근 개선 사항

- Item/Stock/Request 도메인에 점포 권한 검증 패턴 일관화
- 이메일 인증 예외 처리 강화(Null 반환 케이스 방어)
- 이미지 저장 경로 정책 개선(상대 경로 저장 + 실패 cleanup)
- 알바 등록 흐름 정리(컨트롤러 업로드 로직 축소, 서비스 orchestration 강화)
