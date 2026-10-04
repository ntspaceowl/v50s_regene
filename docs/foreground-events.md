# 전경 앱 기록 누락 보완

2026-10-04 ReGene0.1.6, Azahar 시험판 PID29465에서 발견했다. ReGene에서 auto_pose를 켜고 시험판 실행 버튼으로 기존 게임에 복귀했지만 `foreground_screen`은 ReGene MainActivity에 머물렀다. 실제 `dumpsys activity`는 Azahar EmulationActivity였고 `dumpsys usagestats`에는17:52:57의 해당 ACTIVITY_RESUMED가 있었다. 서비스는 실행 중이며 제어 오류나 종료 로그가 없었다.

기존 구현은 조회 끝 시각을 다음 조회의 시작 시각으로 옮겼다. 이미 지나간 시각의 이벤트가 뒤늦게 조회 가능해지면 놓칠 수 있는 경로다. 실제 내부 게시 지연 시간은 측정하지 않았으므로 이것만이 원인이라고 단정하지 않는다.

0.1.7에서10초 overlap 조회, 최신 타임스탬프를 보존하는 ForegroundHistory, warm 실행 시60초 history 재조회로 보완했다. 커버 런처는 계속 제외하며 한 조회의 최종 전경만 사용한다. 이전 게임 이벤트가 최신 Settings/HOME를 덮어쓰지 않는 테스트를 포함한다.

ForegroundHistoryTest와 기존 RotationPolicyTest 성공, APK 빌드 및 v2/v3 서명 검증 성공. 설치된 버전0.1.7/code8 확인. APK SHA256: `C8EFEBA52BB0C872E9CAAAE9357CEAB661A7D6F4D5953108BC0D69CF2F7309BB`.

설치 후 ReGene 실행 버튼으로 복귀한 전경 기록이 실제 Azahar EmulationActivity와 일치했다. 다시 ReGene으로 돌아왔을 때 MainActivity로 바뀌었고 시험판 버튼으로 재복귀했을 때 다시 게임 Activity로 기록됐다. Azahar PID29465 유지. auto_pose=true를 유지하며 실제 기기 회전 시험 준비 상태다. 이 결과는 실제 물리 회전·커버 복구·PC 분리·컨트롤러 자동 숨김 시험의 통과를 뜻하지 않는다.
