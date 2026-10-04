# 외부 컨트롤러 자동 가상패드 숨김

요구사항: Citra, melonDS, DraStic, Azahar에서 블루투스/USB-C 게임패드가 연결되면 가상패드 전체를 숨기고 마지막 장치가 분리되면 기존 표시 설정을 복원한다. 하단 DS/3DS 터치는 유지한다.

## 조사와 구현 상태

- Azahar 2126.1.2: showOverlay는 수동 표시 설정이다. 조사한 소스에서 장치 연결에 따른 자동 숨김은 발견하지 못했다. 분리 설치한 시험판에 InputManager 장치 리스너를 추가했다. GAMEPAD/JOYSTICK 입력을 제공하는 enabled/external/non-virtual 장치만 감지하며 LG 커버 터치 장치는 해당되지 않는다. 초기 연결 및 추가/제거/변경을 처리하고 prefs를 수정하지 않는 임시 표시 조건으로 전체 버튼/스틱을 숨긴다. 기존 터치 처리 View는 유지한다. 숨기기 전 가상패드의 입력을 중립 상태로 보내고 View 해제 때 리스너도 해제한다. 패치: patches/azahar-controller-overlay.patch. 원본 앱에는 적용하지 않았다.
- Citra MMJ 20251112: InputOverlay.ControllerHide는 수동 설정이다. 해당 소스의 대입 경로는 설정 변경 및 메뉴 표시/복원이며 연결 리스너에 의한 자동 숨김은 발견하지 못했다. 실제 설치본의 동작 확인과 별도 제어 경로 검토가 남았다.
- melonDS 2.0.1: [SoftInputBehaviour](https://github.com/rafaelvcaetano/melonDS-android/blob/2.0.1/app/src/main/java/me/magnum/melonds/domain/model/input/SoftInputBehaviour.kt)는 항상 표시, 기본 게임 버튼 숨김, 연결 컨트롤러에 매핑된 입력 숨김, 항상 숨김의 네 모드다. 전체 입력을 연결 여부로 숨기는 모드는 없다. [ConnectedControllerManager](https://github.com/rafaelvcaetano/melonDS-android/blob/2.0.1/app/src/main/java/me/magnum/melonds/ui/emulator/input/ConnectedControllerManager.kt)는 기본 게임 버튼의 매핑까지 검증한 뒤 연결 상태를 발행하므로 단순 물리 연결만으로 자동 숨김이 되는 것도 보장되지 않는다. 기존 옵션만으로 전체 부가 버튼 숨김 요구사항이 충족됐다고 판단하지 않는다.
- DraStic: 폐쇄 소스. 설치본 r2.6.0.4a APK 리소스에서 `str_set_disablemapped` = Disable mapped keys in overlay, 설명 = Buttons with a key mapping will not be active in the controller overlay를 확인했다. 이는 전체 숨김의 증거가 아니다. 게임 중 Show/Hide Virtual Buttons 기능도 있으며 실제 연결 및 복원 동작 확인이 남았다.

## 실제 장치

2026-10-04 MX FLEX DUO가 Android input device9로 연결됨을 확인했다. external=true, sources=0x01000711 (GAMEPAD/JOYSTICK). LG 커버는 sources=0x00001103으로 구분된다. 사용자는 USB 디버깅을 유지하기 위해 먼저 블루투스로 시험하기로 했다. USB-C는 아직 미검증이다.

상태: Azahar 로컬 시험 구현을 빌드/설치했고 런타임 확인 중. 네 앱 전체 완료가 아니다. 실제 분리/재연결, 초기 연결 실행, HOME 복귀, 하단 터치, 컨트롤러 게임 입력과 기존 수동 숨김 설정 복원이 각각 필요하다.

15:11 이후 시험: 이미 연결된 MX FLEX DUO를 유지한 채 ReGene에서 시험판을 실행해 커비를 시작했다. PID3351, azahar-controller-auto-hidden.png에서 게임 상/하 두 화면은 기존 좌표로 유지되고 전체 가상 버튼/스틱이 사라진 것을 확인했다. 사용자에게 실제 A 버튼 입력을 요청했으며 응답 대기 중이다. 타이틀 뒤 영상만으로 실제 입력을 통과로 판단하지 않는다 (자동 데모 가능). 장치 분리 시 복원은 아직 미검증이다. combo 입력 중 숨김 시 해제 보완을 추가로 빌드했지만 현재 설치본에는 해당 보완만 아직 반영되지 않았다.

후속 확인: 시험판 prefs에 HostAxis 매핑이 없었다. 매핑 전에 게임 A 입력 시험을 요청한 순서를 정정하고 실제 UI에서 게임 메뉴 → Settings → Gamepad → Auto-Map Controller를 열었다. 오른쪽 face 버튼의 실제 KeyEvent로 Nintendo/Xbox 배열과 d-pad 종류를 판별하는 다이얼로그다. 지금은 이 화면에서 사용자 버튼 입력을 기다리는 상태다. azahar-controller-auto-map-prompt.png. A 입력이 아직 확인되지 않은 원인을 자동 숨김의 입력 차단으로 단정하지 않는다.
