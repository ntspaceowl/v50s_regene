# 컨트롤러 상태 보존 소스 감사 (2026-10-04)

현재 설치 상태를 확인했다. ReGene0.1.14, auto_pose=true, restore_pending=false이며 Azahar 설정은 Vulkan2배다. 이번 감사에서는 사용자가 통과한 실제 BT 입력/배치 시험을 반복하지 않았다.

## 직접 확인한 구현

- Citra: app/src/dev/regene/v50s/GameControllers.java는 현재 활성화된 외부 비가상 GAMEPAD/JOYSTICK 장치 전체를 열거하고, 목록이 비어 있지 않으면 연결로 판정한다. CitraDiagnosticService.java는 Hide Input Buttons 체크 상태가 목표 상태와 다를 때만 변경한다. 원래 숨겨진 상태면 소유권을 기록하지 않으며, 패드가 없어졌을 때 자신이 바꾼 상태만 복원한다. 메뉴 제어는 현재 선택한 Citra 게임 화면으로 제한한다.
- Azahar: InputOverlay.kt의 hasConnectedController는 inputDeviceIds.any로 같은 장치 조건을 확인한다. 추가/제거/변경 이벤트와 View 재연결에서 refreshControls를 호출한다. refreshControls는 저장된 EmulationMenuSettings.showOverlay를 변경하지 않고, 연결 조건에서 화면에 만들 overlay 항목만 제거한다. 연결된 장치가 하나라도 남으면 계속 숨기는 조건이다.
- melonDS: RuntimeLayoutView.kt는 managed controller 매핑 상태와 별개로 실제 inputDeviceIds.any 조건을 확인한다. 연결되면 모든 non-screen 구성요소를 숨기고 눌린 가상 버튼을 해제한다. 화면 구성요소는 isScreen 검사로 유지한다. isSoftInputVisible을 덮어쓰지 않으므로 수동 숨김 상태는 보존된다. InputDeviceListener는 View에 붙을 때 등록되고 분리될 때 해제된다.
- DraStic: 수정한 APK 없이 원본의 Disable mapped keys in overlay와 MX FLEX DUO 매핑을 사용한다. 기본 버튼과 메뉴의 실제 숨김/복원은 사용자 확인을 통과했다. 이 증거를 임의의 다른 매핑, 활성 Special Button I/II/III, 여러 패드 조합으로 확대하지 않는다.

이 감사는 구현 조건을 직접 확인한 결과다. 여러 물리 패드 동시 연결 시험 결과로 기록하지 않는다. 기존 단일 BT 실기기 결과와 함께 판단한다.

## 남은 전체 복구 검증

사용자는 PC 분리 및 커버 닫았다 열기 시험을 DraStic에서만 했다고 응답했다. 나머지 세 앱의 실제 커버 전환은 소스/화면 꺼짐 시험으로 대신 증명하지 않는다. 기본 상하 배치는 이미 네 앱 모두 통과했고, 현 시점에 신규 고장을 재현한 근거는 없다. 전체 설계의 커버 복구 시나리오를 완료로 주장하려면 물리 기기 조작의 관찰이 필요하다. 소프트웨어로 실제 케이스 닫힘/탈착을 대신하지 않는다.

## 최신 경계 보정 및 커버 확인

사용자가 네 앱 모두 커버 닫기·열기 문제없다고 확인했다. 앞선 커버 미확인 판정은 이를 따른다. 동시에 새 사진의 경계 침범/Drastic 배치 오류는 이전 화면 통과보다 우선한다. 설정 보정 후 새 실행 캡처를 확인했으며 상세는current-status.md, max-fit-profiles.md, drastic-max-fit.md에 기록했다. 이번 화면 보정의 실제 물리 패널 사용자 확인 전에는 목표 전체 완료로 판정하지 않는다. 기존 BT/회전/커버 시험은 반복하지 않는다.
