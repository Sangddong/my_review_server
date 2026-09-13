# 앱 설정 화면 명세

앱 **설정** 화면에 들어갈 항목을 서버 / 클라 / 스토어 역할로 구분한 기준 문서입니다.  
구현은 각 이슈에서 진행하고, 이 문서는 **정보 구조(IA)와 추적**이 목적입니다.

관련 이슈: [#101](https://github.com/Sangddong/my_review_server/issues/101)

---

## 화면 구조 (권장 IA)

```
설정
├── 계정
│   ├── 프로필 (닉네임, 이메일 표시)
│   ├── 로그인된 기기 (선택)
│   └── 회원 탈퇴
├── 알림
│   ├── OS 알림 권한 상태 (+ 설정 앱으로 이동)
│   ├── D3 (마감 3일 전)
│   ├── TODAY (오늘 예약)
│   └── OVERDUE (마감 초과)
├── 앱 정보
│   ├── 버전 / 빌드
│   ├── 이용약관
│   ├── 개인정보처리방침
│   └── 오픈소스 라이선스
└── (하단) 로그아웃 아웃  ← 토큰 DELETE + 로컬 JWT 삭제. 탈퇴와 다름
```

---

## 항목별 역할

범례: **서버** = API 필요 · **앱** = 클라이언트만 · **스토어** = 심사/등록에 필요

### 1. 계정

| 항목 | 역할 | API / 구현 | 상태 | 이슈 |
|---|---|---|---|---|
| 프로필 조회 | 서버 | `GET /api/me` | 미구현 | [#99](https://github.com/Sangddong/my_review_server/issues/99) |
| 닉네임 변경 | 서버 | `PATCH /api/me` | 미구현 | [#99](https://github.com/Sangddong/my_review_server/issues/99) |
| 로그인된 기기 목록 | 서버 | `GET /api/me/device-tokens` (토큰 값 마스킹) | 미구현 | [#96](https://github.com/Sangddong/my_review_server/issues/96) |
| 회원 탈퇴 | 서버 + 앱 | `DELETE /api/me` → 로컬 JWT 삭제 | **완료** | [#100](https://github.com/Sangddong/my_review_server/issues/100) / PR #102 |
| 로그아웃 | 앱 (+ 서버) | `DELETE /api/me/device-tokens` + JWT 폐기 | API는 완료, 앱 UX | — |

### 2. 알림

| 항목 | 역할 | API / 구현 | 상태 | 이슈 |
|---|---|---|---|---|
| 푸시 종류 on/off (D3, TODAY, OVERDUE) | 서버 | `GET/PATCH /api/me/notification-settings` | **완료** | [#81](https://github.com/Sangddong/my_review_server/issues/81) |
| 디바이스 토큰 등록/삭제 | 서버 + 앱 | `PUT/DELETE /api/me/device-tokens` | **완료** | [#70](https://github.com/Sangddong/my_review_server/issues/70) 계열 |
| OS 권한 상태 표시 · 거부 시 안내 | 앱 | 시스템 API | 미구현 | [#98](https://github.com/Sangddong/my_review_server/issues/98) |
| 권한 허용 시 설정 전부 on | 서버 + 앱 | 토큰 등록 시 settings upsert (서버) | 미구현 | [#98](https://github.com/Sangddong/my_review_server/issues/98) |
| 설정 앱으로 이동 | 앱 | iOS/Android 딥링크 | 앱 | #98과 함께 |

### 3. 앱 정보 · 스토어

| 항목 | 역할 | 비고 | 상태 | 이슈 |
|---|---|---|---|---|
| 앱 버전 / 빌드 번호 | 앱 | 패키지 메타데이터 | 앱 | — |
| 개인정보처리방침 링크 | 앱 + **스토어** | 공개 HTTPS URL 필요 | 미구현 | [#103](https://github.com/Sangddong/my_review_server/issues/103) |
| 이용약관 링크 | 앱 + 스토어(권장) | 동일 호스팅 | 미구현 | [#103](https://github.com/Sangddong/my_review_server/issues/103) |
| 오픈소스 라이선스 | 앱 | 스토어·법무 관행 | 앱 | — |
| 지원 이메일 / 한국 연락처 | 스토어 | Play Console 계정 정보 | 등록 시 | — |

### 4. 설정 밖이지만 스토어에 필요한 것

| 항목 | 비고 |
|---|---|
| 계정 삭제 경로 | 앱 설정에 `DELETE /api/me` 연결 (**서버 완료**). 웹 탈퇴 페이지는 가점 |
| 개인정보처리방침 URL | App Store Connect / Play 스토어 등록 필수에 가까움 → #103 |
| FCM / APNs 연동 | 실기기 푸시 → [#75](https://github.com/Sangddong/my_review_server/issues/75) |

---

## 서버 API 요약 (설정 관련)

| Method | Path | 용도 | 상태 |
|---|---|---|---|
| GET | `/api/me` | 프로필 | #99 |
| PATCH | `/api/me` | 닉네임 | #99 |
| DELETE | `/api/me` | 탈퇴 | **완료** |
| GET/PATCH | `/api/me/notification-settings` | 푸시 종류 on/off | **완료** |
| PUT/DELETE | `/api/me/device-tokens` | 토큰 등록/삭제 | **완료** |
| GET | `/api/me/device-tokens` | 기기 목록 | #96 |

---

## 구현 우선순위 (앱 설정 화면 기준)

1. **P1** — 탈퇴 UI 연결 (#100 API 완료) + 개인정보/약관 URL (#103)
2. **P2** — 프로필·닉네임 (#99), OS 권한 연동 (#98), 알림 토글(이미 API 있음)
3. **P3** — 로그인 기기 목록 (#96)

---

## 주의

- **로그아웃 ≠ 탈퇴.** 로그아웃은 현재 기기 토큰만 지우고, 탈퇴는 계정 soft delete + 모든 기기 토큰·OAuth 해제.
- OS 알림 **거부**와 앱 내 D3 **off**는 다름. 전자는 시스템 권한, 후자는 `notification-settings`.
- 저장된 수신 설정이 없으면 서버 기본값은 **전부 on** (`GetNotificationSettingsUseCase`).
