# 옷장을 부탁해_SB01_TEAM10

---

### 📈 Test Coverage

[![codecov](https://codecov.io/gh/sb01-team10/sb01-otboo-team10/graph/badge.svg?token=SSZ7ZQBBXT)](https://codecov.io/gh/sb01-team10/sb01-otboo-team10)

---

## 🖐️ 팀 구성

| **한상은**                                                                                                                         | **강소율**                                                                                                                      | **이소영**                                                                                                                                  | **허원재**                                                                                                          |
|---------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| <div align="center"><img src="https://github.com/silvarge.png" width="150"/><br/>[@silvarge](https://github.com/silvarge)</div> | <div align="center"><img src="https://github.com/soyul9280.png" width="150"/><br/>[@soyul9280](https://github.com/soyul9280) | <div align="center"><img src="https://github.com/gitSoyoungLee.png" width="150"/><br/>[@gitSoyoungLee](https://github.com/gitSoyoungLee) | <div align="center"><img src="https://github.com/Oince.png" width="150"/><br/>[@Oince](https://github.com/Oince) |

---

## 💡 프로젝트 소개

<img width="1919" height="867" alt="image" src="https://github.com/user-attachments/assets/33552380-1f6a-4511-9b1b-10f687909e14" />

---

> 👗 오늘 뭐 입지? 고민은 그만!
>
> 이 서비스는 사용자가 등록한 옷과 날씨 데이터를 기반으로 개인 맞춤형 의상 조합을 추천해주는 플랫폼입니다.
> 추천된 의상은 OOTD 피드에 공유할 수 있고, 다른 사용자의 스타일을 팔로우, 좋아요, 댓글, DM을 통해 함께 즐길 수 있어요.
> 옷장을 부탁해는 오늘도 당신의 스타일을 책임집니다. 🌦👕

- 개인화 의상 및 아이템 추천 SaaS
- 날씨, 취향을 고려해 사용자가 보유한 의상 조합을 추천해주고, OOTD 피드, 팔로우 등의 소셜 기능을 갖춘 서비스

### 📌 프로젝트 정보

| 항목             | 내용                                                                                             |
|----------------|------------------------------------------------------------------------------------------------|
| **📆 프로젝트 기간** | 2025.06.30 ~ 2025.07.31                                                                        |
| **🔗 배포 링크**   | https://dev.otboo.shop/                                                                        |
| **🎬 시연 영상**   | [구글 드라이브 ](https://drive.google.com/file/d/1ZjCzH3yKtEAy6KNyDh82zTFcG_bK2LD2/view?usp=sharing) |
| **📋 협업 문서**   | [Notion 페이지](https://www.notion.so/SB01-10-21a97c15da088039b48ce74cc4fdbc12)                   |
| **📘 API 문서**  | [Swagger API](https://gitsoyounglee.github.io/otboo-swagger-ui/#/)               |
| **📑 발표 자료**   | [구글 드라이브](https://drive.google.com/file/d/1h6Hw1A_uIVSKq2PjLMk2efyT3duk4ROq/view?usp=sharing)  |

### 👗 제공 기능

### 주요 기능

- 날씨 및 사용자 특성에 따른 맞춤형 코디 추천
    - 메인 화면에서 전달되는 날씨 예보 정보와
      사용자 프로필에서 세팅하는 온도 민감도를 이용하여
      자체 구현 알고리즘 및 LLM API를 통해
      나의 옷장 속에서 나만의 OOTD를 추천 받을 수 있습니다.
- OOTD 피드를 통한 소셜 공유
    - 추천 받은 코디를 피드로 올려 나만의 OOTD를 공유할 수 있습니다.
      OOTD 피드들을 통해 다양한 OOTD를 감상하고,
      마음에 드는 사용자는 팔로우하여, 피드를 구독할 수 있습니다.
      팔로워의 피드가 새로 등록되면 알림을 통해 알려줍니다.

| 항목            | 상세 내용                                                             |
|---------------|-------------------------------------------------------------------|
| **의상 관리**     | - 의상 기본 CRUD <br/> - 구매 링크로 옷 정보 불러오기<br/> - 의상 속성 기본 CRUD        |
| **추천 관리**     | - 날씨에 따른 의상 OOTD 추천                                               |
| **날씨 관리**     | - 날씨 정보 조회, 날씨 위치 정보 조회                                           |
| **피드 관리**     | - 피드 기본 CRUD<br/> - 피드 좋아요<br/> - 피드 댓글 등록, 조회                    |
| **팔로우 관리**    | - 팔로우 생성, 취소<br/>- 팔로우, 팔로잉 목록, 팔로우 요약 정보 조회                      |
| **알림, DM 관리** | - 알림 목록 조회, 알림 읽음 처리<br/>- DM 주고 받기, 조회                           |
| **프로필 관리**    | - 회원가입<br/>- 사용자 프로필 수정<br/>- 관리자 기능: 사용자 권한, 계정 잠금 상태 변경, 전체 목록 조회 |
| **인증 관리**     | - 로그인, 로그아웃<br/>- 임시 비밀번호 발급<br/>- 토큰 재발급                         |

---

## ⚙️ 기술 스택

### 🖥️ 언어

![Java](https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white)

### 🧩 백엔드

![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring MVC](https://img.shields.io/badge/Spring%20MVC-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Batch](https://img.shields.io/badge/Spring%20Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Actuator](https://img.shields.io/badge/Spring%20Actuator-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Actuator-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Cache](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

![Caffeine Cache](https://img.shields.io/badge/Caffeine-6DB33F?style=for-the-badge&logo=java&logoColor=white)
![Testcontainers](https://img.shields.io/badge/Testcontainers-333?style=for-the-badge&logo=docker&logoColor=white)
![Junit](https://img.shields.io/badge/JUnit-25A162?style=for-the-badge&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-000?style=for-the-badge&logo=apachekafka)
![QueryDSL](https://img.shields.io/badge/QueryDSL-007ACC?style=for-the-badge&logo=java&logoColor=white)
![Google Gemini](https://img.shields.io/badge/google%20gemini-8E75B2?style=for-the-badge&logo=google%20gemini&logoColor=white)
![Selenium](https://img.shields.io/badge/-selenium-%43B02A?style=for-the-badge&logo=selenium&logoColor=white)
![Jsoup](https://img.shields.io/badge/Jsoup-F89820?style=for-the-badge&logoColor=white)

### 🔩 데이터베이스

![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)

### 🧑‍🔧 인프라

![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)
![Amazon S3](https://img.shields.io/badge/Amazon%20S3-FF9900?style=for-the-badge&logo=amazons3&logoColor=white)
![Amazon ECR](https://img.shields.io/badge/Amazon%20ECR-FF9900?style=for-the-badge&logo=amazon-aws&logoColor=white)
![Amazon ECS](https://img.shields.io/badge/Amazon%20ECS-FF9900?style=for-the-badge&logo=amazonecs&logoColor=white)
![Nginx](https://img.shields.io/badge/nginx-%23009639.svg?style=for-the-badge&logo=nginx&logoColor=white)

### 기타

![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![CodeRabbit](https://img.shields.io/badge/CodeRabbit-FF070A?style=for-the-badge&logoColor=white)
![Codecov](https://img.shields.io/badge/Codecov-F01F7A?style=for-the-badge&logo=codecov&logoColor=white)
![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)
![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)

---

## 👥  팀원 R&R

| <nobr>**팀원**</nobr> | **역할 및 기여**                                                                                                                                                                                                                                                                                                                                                              | 
|------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <nobr>**한상은**</nobr>  | **팀장** <br/>- 팀 운영 및 일정 관리, 발표 자료 메인 담당 <br/>**피드/ 위치 도메인 담당** <br/> - 피드 등록 및 좋아요, 댓글 CRUD<br/> - Kakao Geo API를 활용한 위경도 기반 행정동 데이터 수집 로직 구현 <br/> **날씨 도메인 담당** <br/>- 기상청 단기예보 API를 활용한 날씨 예보 응답 데이터 파싱 및 변환 로직 구현  <br>- 날씨 데이터 수집 배치 작업 수행 <br/> - 날씨 관련 이벤트 발생 시 알림 전송<br/> **인프라 및 CI/CD 파이프라인 세팅** <br/> - AWS를 이용한 서비스 배포 <br> - Github Acitons를 이용한 CI/CD 파이프라인 세팅 | 
| <nobr>**강소율**</nobr> | **속성/의상 도메인 담당** <br> - 구매링크 URL에서 옷 정보 추출 시 웹 크로링 구현 <br> **추천 도메인 담당** <br> - 자체 추천 알고리즘 설계 <br> - 추천 LLM 연동 <br> **공통 설정 담당** <br> - 의존성, 예외 처리 초기 세팅                                                                                                                                                                                                                 |
| <nobr>**이소영**</nobr> | **프로필 관리 도메인 담당** <br> **사용자 인증 및 보안 도메인 담당** <br> - Spring Security 기반 인증, 인가 설계 <br> - 커스텀 인증 필터, 핸들러 설계 <br> - JWT 기반 세션 구조 설계 및 토큰 관리 전략 수립 <br> - OAuth2 소셜 로그인 연동(구글, 카카오) <br> - 이메일 전송 로직 설계                                                                                                                                                                     |
| <nobr>**허원재**</nobr> | **팔로우 도메인 담당** <br> **DirectMessage 도메인 담당** <br> **알림 도메인 담당** <br> - Kafka 연동 <br> - Confluent Cloud 세팅 및 연동                                                                                                                                                                                                                                                           | 

---

## 🏢 아키텍처

<details>
<summary>📁 프로젝트 구조 보기</summary>

```
sb01-otboo-team10 # 루트 디렉토리
├─ Dockerfile
├─ docker-compose
├─ build.gradle
│
└─ src
   ├─ main # 메인 소스 코드
   │  ├─ java
   │  │  └─ com
   │  │     └─ codeit
   │  │        └─ weatherwear
   │  │           ├─ domain
   │  │           │   ├─ auth # 인증 도메인
   │  │           │   ├─ clothes # 옷 도메인
   │  │           │   ├─ directmessage # DM 도메인
   │  │           │   ├─ feed # 피드 도메인
   │  │           │   ├─ follow # 팔로우 도메인
   │  │           │   ├─ location # 위치 도메인
   │  │           │   ├─ notification # 알림 도메인
   │  │           │   ├─ ootd # OOTD 옷 추천 도메인
   │  │           │   ├─ security # 보안 도메인
   │  │           │   ├─ user # 프로필 도메인
   │  │           │   └─ weather # 날씨 도메인
   │  │           └─ global
   │  │               ├─ config # 설정 관리
   │  │               ├─ event  # 알림 이벤트
   │  │               ├─ exception # 예외 처리
   │  │               ├─ properties # 설정 프로퍼티 바인딩
   │  │               ├─ request # 공통 요청 DTO
   │  │               ├─ response # 공통 응답 DTO
   │  │               ├─ sse # SSE 관리
   │  │               └─ storage # S3 버킷 관리
   │  └─ resources
   │     ├─ application.yml # 공통 설정
   │     ├─ application-dev.yml # dev 개발 프로파일 설정
   │     ├─ application-prod.yml # prod 프로덕션 프로파일 설정
   │     ├─ template
   │     └─ static
   └─ test # 테스트 소스 코드
      ├─ java
      │  └─ com
      │     └─ codeit
      │        └─ weatherwear
      │           ├─ domain
      │           └─ glbal
      └─ resources
         ├─ application-test.yml    # 일반 테스트 프로파일
         ├─ application-reader-exception.yml # 배치 테스트 프로파일
         ├─ application-test-security.yml # 보안 테스트 프로파일
         └─ schema.sql
```

</details>

### 👗 의상 추천 알고리즘

<img width="642" height="395" alt="image" src="https://github.com/user-attachments/assets/d54e9f59-c77c-4478-9e45-398021ad344c" />

### 🌨️ 인프라 아키텍처

ALB → Nginx (80) → Spring Boot (8080) 구조로 구성되며,
또한, 외부 서비스로는 Kafka(Confluent Cloud)와 S3를 연동하여 데이터 스트리밍과 파일 저장을 처리합니다.

<img width="704" height="357" alt="image" src="https://github.com/user-attachments/assets/9392130e-c796-4881-9c09-b5478eb45885" />

### ⚙️ 배포 아키텍처

<img width="1591" height="679" alt="image" src="https://github.com/user-attachments/assets/85f074e3-3100-49b2-acbe-4aef83b6b558" />

### 📔 데이터베이스 ERD

<img width="3810" height="4804" alt="image" src="https://github.com/user-attachments/assets/7983e452-859b-4b52-81b3-0d7e3d9d7132" />




