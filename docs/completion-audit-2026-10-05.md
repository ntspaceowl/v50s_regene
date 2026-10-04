# 최종 실사용 완료 감사 (2026-10-05)

이 기록은 과거 문서의 대기 판정보다 우선한다. 목표는 현재 V50S에서 ReGene로 네 에뮬레이터를 선택하고 가로로 돌리면 커버에 상단, 본체에 하단 터치 화면이 표시되는 경험이다. 사용자 추가 요구인 비율 보존, 최대 표시, 렌더링 해상도, 패드 자동 숨김/해제 복원, 전역 설정, 설치본 및 작업 흔적 정리를 함께 감사했다.

| 요구 | 확인 근거 | 결과 |
|---|---|---|
| 네 앱 선택과 자동 가로 배치 | 사용자 승인 실행 흐름, ControllerService/LgWide/OrientationGuard 구현, 네 앱 회전 실기기 통과 | 완료 |
| 커버 상단·본체 하단 및 터치/게임 입력 | 네 앱 사용자 일괄 통과, 최신 화면 크기 네 앱 해결 응답 | 완료 |
| 원래 비율과 패널 최대 표시 | 실제2340×1080 두 패널, Citra/Azahar1795×1077 및1436×1077, melonDS1436×1077, DraStic 전역Landscape 1:1. 최신 사용자 통과 | 완료 |
| 전역 설정 | Citra config-mmj.ini, Azahar config.ini, melonDS 공유레이아웃과ROM별지정없음. DraStic 두 게임 별도 배치 삭제 후Global Layout 확인 및 새 실행 | 완료 |
| 외부 패드 전체 숨김 및 해제 복원 | 기존 네 앱 실제BT 사용자 통과와controller-state-audit.md의 소스 감사. 최신DraStic 수동 숨김은 별도로 복원, 새 프로세스에서도 일반 패드/메뉴 표시 | 완료 |
| Citra 자동 숨김 권한 | 이번 감사에서 누락을 직접 발견하고 이전 권한을 복원. Android Bound services 확인. 패드 미연결 게임에서 표시 복원 및citra_probe_hide_owned=false 확인 | 완료 |
| 권한 누락의 사용자 안내 | ReGene0.1.15 런처 상태에표시. 자체 접근성만 잠시 해제 후Citra 실행 시SettingsAccessibilityActivityMain 진입 확인, 즉시 원래 권한 목록 복원. 최종 런처'허용' 화면 확인 | 완료 |
| HOME/목록·게임 종료 후 확장 해제 | 기존 네 앱 시험 및후속 Citra 사용자 통과, 최종Azahar 반복 종료/목록·wake 포커스 자동 시험 | 완료 |
| 화면/커버 닫기·열기 복구 | 네 앱 모두 문제없다는 최신 사용자 응답 | 완료 |
| 초기 설정 이후 PC 없이 제어 | ControllerService는 Android 센서/전경 정보/LG API/설정 API를 호출하고 외부 PC 통신 경로가 없다. DraStic 실제USB분리 사용자 통과. 나머지3앱 USB분리 실측이라고 기록하지 않음 | 구조 확인 완료 |
| 렌더링 설정 | Citra/Azahar/melonDS2배, Azahar Vulkan, DraStic High-Resolution3D2배 설정 확인 | 완료 |
| 네 항목과 불필요 설치 제거 | 최신pm list packages에서ReGene+네 앱만 조회, 기존원본/WideMode 제외. releases 앱별APK1개 | 완료 |
| 캡처 등 작업 흔적 정리 | 이전PC 캡처 휴지통 이동 및Android 임시 제거, 이번 임시 PNG/XML도 제거. ROM/세이브/소스/서명키 보존 | 완료 |

ReGene 최종 설치 버전0.1.15/code16, APK SHA256 F5BA115ABF60981763D73F35FDF25D2623D3B96CFB1DB839E801BE587D754057. 휴대폰base.apk와releases/ReGene.apk 해시 일치, v2/v3 서명 검증 및 빌드 통과. auto_pose=true, restore_pending=false이며ReGene 목록으로 복귀했다.

사용자가USB-C 패드 시험은 제외했다. 네 앱의 현재 물리배치와 실제BT 결과 및후속 고장복원으로 요청한 실사용 목표를 완료한다. 여러 물리패드 동시 연결, 임의의 새 매핑과 활성DraStic 특수버튼 조합, 모든ROM의 장시간성능을 실제시험했다고 확대하지 않는다. DraStic 수동 표시 상태는 원본이ROM별로 저장하며 새게임 기본값은표시/터치활성0이다. 전역 화면 배치와 구분한다.

FGA 접근성 서비스 등 다른 앱의 설정은 보존했다. UI진단이기존접근성서비스를잠시억제하는특성으로FGA중단알림이관찰됐으며, 일반게임실행기능이FGA를중지하는구현은없다.
