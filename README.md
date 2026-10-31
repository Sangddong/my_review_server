# 🗂️ PROJECT OUTLINE
### ✔️ 프로젝트명
- 나만의 체험단
### ✔️ 개발 배경
- 블로그, 인스타그램 등 다양한 플랫폼에 리뷰를 올리는 인플루언서들을 위한 **통합 리뷰 일정 관리 어플**입니다.
- 리뷰를 모집하는 플랫폼은 다양하게 나눠져 있어 그 일정을 모두 소화하고 관리하기 위해서는 인플루언서가 한 곳에서 별도로 관리해야 합니다.
- 이를 보다 효율적으로 관리할 수 있도록 지원하는 것이 **나만의 체험단** 앱입니다.
- 어떤 사이트에서 지원받았는지, 어느 플랫폼에 리뷰를 업로드해야 하는지, 마감일과 체험일은 언제인지 등을 보다 효율적으로 관리하고 통합하여 한 눈에 확인하고자 개발하였습니다.
### ✔️ 역할
- 개인 프로젝트로
- 기획 + 다자인 + 프론트&백엔드 개발 + 배포 모두 혼자 진행하였습니다 😂

# ⚙️ SYSTEM STRUCTURE
### 시스템 아키텍처
### ERD

# 💡 TECH STACK & WHY?
### ✔️ 사용 기술

| 구분 | 기술 |
|---|---|
| Backend | ![Java 21](https://img.shields.io/badge/Java_21-007396?style=flat-square&logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white) ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white) ![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-59666C?style=flat-square&logo=hibernate&logoColor=white) ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white) ![Flyway](https://img.shields.io/badge/Flyway-CC0200?style=flat-square&logo=flyway&logoColor=white) ![JWT](https://img.shields.io/badge/JWT-000000?style=flat-square&logo=jsonwebtoken&logoColor=white) |
| Frontend | ![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=flat-square&logo=typescript&logoColor=white) ![React](https://img.shields.io/badge/React-61DAFB?style=flat-square&logo=react&logoColor=black) ![Vite](https://img.shields.io/badge/Vite-646CFF?style=flat-square&logo=vite&logoColor=white) ![React Router](https://img.shields.io/badge/React_Router-CA4245?style=flat-square&logo=reactrouter&logoColor=white) ![Capacitor](https://img.shields.io/badge/Capacitor-119EFF?style=flat-square&logo=capacitor&logoColor=white) |
<br >

### ✔️ 왜 앱 푸시를 위해 **FCM**을 사용했는가?
  > **❓첫 방향성은 MQTT!**
  - 이전 회사에서 Saas 개발 시 화면에 알림을 띄우기 위해 MQTT를 사용했던 기억이 있어, 앱 푸시 기능을 개발하기 전 MQTT로 서버에서 전부 구현하여 관리하는 방향을 고려했습니다.
  - MQTT는 Publish/Subscribe 구조로 유연한 확장이 가능하며, QoS를 통한 신뢰성 있는 메세지 전달 그리고 경량 헤더로 네트워크 대역폭과 CPU를 적게 소모한다는 장점이 있었습니다.
  - 그러나 나만의 체험단은 스마트폰에서 사용할 앱 서비스이므로 어플이 꺼져있을 때에도 소켓이 유지되어야 하는데, MQTT가 메세지 전달을 보장하는 구간은 앱 프로세스가 살아있으며 브로커와 소켓이 연결된 동안입니다. 안드로이드는 Doze와 배터리 최적화 때문에 백그라운드 연결을 곧 죽이고, 아이폰은 일반 앱이 백그라운드에서 소켓을 유지하는 것 자체를 허용하지 않습니다.

  > **💡 FCM을 선택한 이유와 성과**
  - FCM 사용 시 앱 구동 여부에 관계 없이 알림을 전달할 수 있어 사용자 입장에서 일정 알림에 대한 신뢰도가 높아집니다.
  - 발송 API 호출만 연동하므로, 개발 속도가 빨라집니다

  > **🆚 MQTT와 FCM의 비교** <br >
  
  || MQTT | FCM |
  |---|---|---|
  | 맞는 상황 | 앱이 켜져 있고 소켓이 연결된 동안 | 앱이 꺼져 있어도 전달 |
  | 안드로이드 | 배터리 최적화가 백그라운드 연결을 끊음 | OS 푸시 채널로 전달 |
  | 아이폰 | 일반 앱의 백그라운드 소켓이 불가 | APNs로 전달 |
  | 서버 작업 | 브로커 운영 | 발송 API 호출 |
<br >

### ✔️ 헥사고날 아키텍처를 선택한 이유는?
  > 🐙 **요청의 in/out 경로가 많은 나만의 체험단**
  - 평소 익숙하게 사용한 방식은 컨틀롤러 /서비스 / 레포지토리로 나뉜 계층형 서비스였습니다.
  - 나만의 체험단 서비스의 경우 기획 단계에서 고려한 요청의 in/out이 발생하는 경로가 모두 2개 이상이었습니다.
    - in: `HTTP`, `Schedular`
    - out: `MySQL`, `Google•Naver•Kakao`, `FCM`
  - 요청의 in/out이 수정되어도 가운데 로직은 변경되지 않도록 입구와 출구만 갈아 끼우는 구조가 필요했습니다.
  
  > **🆚 계층형 구조와 헥사고날 아키텍처**<br >
  
  || 계층형 | 헥사고날 |
  |---|---|---|
  | 읽는 법 | 한 기능이 서비스 클래스 하나에 모임 | 동작 하나가 유즈케이스 파일 하나 |
  | 장점 | 파일이 적고, 익숙한 사람이 바로 찾음 | 입구와 바깥 시스템을 바꿔도 가운데 규칙이 남음 |
  | 단점 | 서비스가 커지면 DB, 외부 API, 규칙이 한 클래스에 섞임 | 인터페이스와 변환 클래스가 늘어 처음 읽는 비용이 큼 |
  | 잘 맞는 경우 | 입구가 HTTP뿐이고 저장소가 DB 하나일 때 | 입구가 여러 개이고 바깥 시스템도 여러 개일 때 |
  | 테스트 | 서비스가 JPA에 붙어 있으면 DB와 같이 검증하는 경우가 많음 | 규칙은 DB 없이, 외부 호출은 가짜 인터페이스로 검증 가능 |


알림함과 푸시를 나눈 이유. 발송 실패와 무관하게 목록이 남는 점이 이 서버에서 이미 구현된 결정입니다.
소셜 로그인 세 곳을 한곳으로 모은 이유. 제공자만 다르고 가입 절차는 같습니다.
소프트 삭제 뒤 스케줄러가 지우는 이유. 회원, 플랫폼, 알림이 바로 삭제되지 않습니다.
필수 플랫폼 완료를 컬럼으로 안 둔 이유. 등록 상태로부터 계산하는 값입니다.
# 🚀 TROUBLE SHOOTING

# 👩🏻‍💻 RETROSPECT
#### ✔️ 성과
- 협찬 서비스들을 한 곳에서 통합 관리할 수 있습니다.
- 사용자가 마감 직전, 마감 초과, 체험 예약일 미정 등을 한 눈에 확인할 수 있도록 색으로 표시합니다.
- 마감일과 체험일을 놓치지 않도록 주요 일정 정보를 스케줄러를 통해 확인하고, 사용자에게 앱 푸시로 알립니다.
