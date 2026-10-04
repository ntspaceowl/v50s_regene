# DraStic 기존 설치본 시험

2026-10-04, V50S Android 12, 설치본 `com.dsemu.drastic` r2.6.0.4a. 원본 APK를 수정하거나 설정을 덮어쓰지 않았다.

## 기존 UI에서 확인한 경로

- 게임 중 Android `KEYCODE_MENU`로 Game Menu가 열린다. 이 화면과 Options의 설정 항목은 접근성 텍스트 노드로 읽힌다.
- Options → Virtual Gamepad에는 Always show Start and Select buttons, Menu-Button Position, Special Button I/II/III 등이 있다. 이번 시험에서 옵션은 변경하지 않았다.
- Options → External Controller의 현재 Select Key Mapping은 None이다. Disable mapped keys in overlay 설명은 매핑된 버튼의 활성화를 막는다는 내용이며 전체 표시 숨김을 증명하지 않는다. 현재 체크는 OFF다.
- 게임 화면 하단 삼각형을 누르면 원형 빠른 메뉴가 열린다. 왼쪽 위 게임패드 아이콘으로 기본 패드 표시를 전환할 수 있다. 원형 메뉴의 아이콘과 상태는 이번 uiautomator 덤프에서 텍스트/설명 노드로 노출되지 않았다.

## 실제 숨김·복원

PID13399를 유지한 채 원형 메뉴에서 게임패드 아이콘을 눌렀다. L/R, D-pad, ABXY, START/SELECT가 사라졌고 두 게임 화면은 유지됐다. 그러나 **하단 메뉴 삼각형은 남았다**. 따라서 사용자의 전체 가상 버튼 숨김 요구사항이 충족된 것은 아니다.

- 숨김: `test-results/2026-10-04/drastic-manual-hide.png`
- 숨김 상태에서 원형 메뉴 재진입: `drastic-hidden-menu.png`. 게임패드 아이콘의 붉은 취소선이 사라진 상태를 시각적으로 확인했다.
- 다시 같은 아이콘을 눌러 기존 표시 상태로 복원: `drastic-manual-restored.png`. PID13399 유지.

숨김 중 하단 게임 화면 중앙에 ADB 터치 홀드를 보냈으나 튜토리얼 화면이 바뀌지 않았다 (`drastic-hidden-touch.png`). 이 시험은 하단 터치 통과 증거가 아니다. 게임의 진행 조건, DraStic 터치 활성화 상태, 좌표 변환을 추가로 구분해야 한다. 실제 손가락 터치도 미검증이다.

## 자동화 판단과 남은 작업

Citra처럼 체크박스 상태를 읽어 이전 수동 상태를 보존하는 자동화를 현재 확인한 노드만으로 구현할 수 없다. 좌표로 토글만 반복하면 최초 숨김 상태를 알 수 없어 연결 해제 때 잘못 복원할 수 있다. 접근 가능한 별도 상태/명시적 설정 경로를 더 조사해야 한다.

메뉴 삼각형까지 숨기는 옵션의 조건과 게임 내 메뉴 복귀 수단, 실물 컨트롤러 매핑 및 실제 입력, 숨김 중 하단 터치, HOME/설정 복귀와 실제 기기 회전·커버 동작은 미완료다. 이번 시험 당시 MX FLEX DUO는 bonded 목록에만 있었으며 Bluetooth `STATE_DISCONNECTED`였고 게임패드 입력 장치가 없었다.
