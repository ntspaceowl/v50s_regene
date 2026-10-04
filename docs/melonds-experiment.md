# melonDS Surface 교체 시험

최신 설치 상태: 별도 시험판에는 EmulatorActivity의 doOnPreDraw 변경과 RuntimeLayoutView의 전체 패드 자동 숨김만 적용했다. Surface 교체 후보와 진단 로그는 제거했다. 아래는 원인 탐색 순서의 기록이며 최종 시험 결과는 마지막 절을 참조한다.

## 재현한 문제와 원인 후보

원본 melonDS 2.0.1 GH에서 ReGene 확장 상태로 레이아웃을 편집하고 저장한 뒤 기존 게임으로 돌아오면 두 게임 영역이 검게 보였다. PID15578은 유지됐고 HOME/최근 앱 복귀로 영상이 회복됐다. 이는 해결 완료가 아니며 기존 findings.md에 재현 결과가 있다.

공식 태그 `2.0.1`, 커밋 `5ec3648d68382dea9c17d6fbf02544e5c2c0afed`의 EmulatorSurfaceView에서 surfaceDestroyed는 surface=null만 설정한다. EGL windowSurface 정리는 다음 doFrame에서 surface==null인 경우에만 수행한다. 게임이 멈춘 사이 surfaceDestroyed와 surfaceCreated가 모두 발생하면 다음 프레임 시점에는 surface가 다시 non-null이므로 이전 EGL Surface가 재사용될 수 있다. 이는 관찰된 검은 화면의 원인 후보이며 아직 실기기에서 확정하지 않았다.

## 분리 시험

실험 패치는 Surface 생성/파괴를 surfaceReplaced 플래그로 보존한다. 다음 프레임의 render thread에서 이전 EGL Surface를 닫고 현재 Surface에 다시 연결한다. EGL context와 게임 상태는 유지한다. 패치: patches/melonds-surface-replacement.patch.

원본 설치본을 교체하지 않도록 debug applicationIdSuffix를 `.regeneprobe`로 설정했다. 원본 앱 설정/세이브의 private 경로를 수정하지 않는다. 네이티브 엔진은 설치본에서 추출한 ARM64 라이브러리를 사용한다. APK SHA256 `646EB4564C71B0817ACE6474E92C169DA16630D58826C43E0FE648896311EC94`는 공식 GitHub 2.0.1 release asset app-gitHub-prod-release.apk digest와 일치한다.

로컬 checkout research/melonds-build에서 CMake 연결 두 곳을 제외하고 ABI를 ARM64로 제한했다. prebuiltJniLibs 경로를 추가했다. 빌드: Android Studio JBR을 JAVA_HOME으로 설정하고 `gradlew.bat :app:assembleGitHubProdDebug --console=plain`.

현재 상태: 최초 분리 빌드가 4분23초에 성공했고 APK v2 서명과 별도 package를 확인한 뒤 설치했다. APK의 두 엔진 라이브러리 SHA256은 원본과 바이트 단위로 일치한다 (frontend 05E6C0FD06B4DBD1D9A5A915C9C92775924CB4DFEE9BD614BA9BFE20571CE149, engine D407419FD07D4A8832795411AE78D4FBC39E7594694A43454029B49E31F49BD9).

추가로 RuntimeLayoutView에 물리 GAMEPAD/JOYSTICK 장치 리스너와 일시적인 전체 가상패드 숨김을 구현했다. prefs나 수동 toggle 상태를 바꾸지 않으며 화면 컴포넌트와 터치 리스너는 유지한다. 가상 게임 버튼의 눌림을 별도로 추적하여 숨김 시 해당 입력만 해제한다. 패치: patches/melonds-controller-overlay.patch. 후속 빌드가 22초에 성공했고 동일한 별도 패키지로 업데이트 설치했다.

ReGene에는 이 시험 패키지가 설치된 경우에만 나타나는 선택 버튼과 package visibility를 추가했다. ReGene 0.1.3 재빌드 및 v2/v3 서명 검증을 통과하고 폰에 업데이트했다. 사용자 HG ROM을 별도 MelonReGeneTest/ROM 폴더에 복사하고 SAF로 선택했다. 시험판 PID11281에서 게임 영상이 출력되며 ReGene이 게임을 2340x2160으로 확장하는 것을 확인했다. 기본 레이아웃은 상하 배치가 아니므로 사용 가능한 최종 프로필은 아니다.

원본과 시험판 모두 settings_backup_restore_enabled=false로 백업 메뉴가 노출되지 않는다. 원본 설정을 추출하거나 수정하지 않았다. 시험판에 새 My Layout 프로필을 만들었다. 아직 layoutVariants가 빈 배열이며 실제 위치 편집을 저장한 프로필로 간주하지 않는다.

ReGene 0.1.3은 선택된 melonDS의 LayoutEditorActivity도 게임과 같은 지연/커버 조건으로 확장한다. 목록은 일반 화면으로 유지하며, 편집기 진입 후 wm size의 override 2160x2340과 UI의 하단 메뉴 y2034..2160을 확인했다. WideMode 빠른 설정 조작은 사용하지 않았다. 편집기 저장/목록 복귀는 확인했으나 실제 편집 후 게임 영상 복귀, 하단 터치, 컨트롤러 연결/분리는 아직 미검증이다. 다른 앱의 성공으로 melonDS 검증을 대신하지 않는다.

현재 연결 재확인에서는 dumpsys input에 MX FLEX DUO가 없다. 앞서 연결된 상태의 Azahar 전체 가상패드 숨김과 연결 해제 후 복원 증거는 controller-overlay.md에 기록했다. 재연결 및 실제 버튼 매핑은 사용자 응답을 기다린다.

## 상하 프로필과 편집 복귀 재현

편집기에서 L을 이동하고 저장하여 실제 variant 메타데이터를 얻었다: uiSize 2264x2160, mainScreenDisplay id0/type EXTERNAL/2340x2160, LANDSCAPE, inset 모두0. 시험판의 private layouts.json만 debug run-as로 수정하여 ReGene V50S 프로필을 만들었다. 상단/하단 화면은 각각 x492, 폭1280, 높이960, y50/y1150이며 모든 조작 컴포넌트는 y1150 이상이다. 원본 앱 설정은 수정하지 않았다.

프로필을 UI에서 선택하고 HG를 실행한 PID14255에서 2340x2160 상하 영상과 모든 버튼의 본체 영역 배치를 확인했다. 타이틀에서 가상 A(2170,1680)를 눌러 안내 메뉴로 진입했고, 하단의 모험의 목적(1150,1630)을 눌러 해당 설명으로 이동했다. 증거: melonds-probe-v50s-title-a.png, melonds-probe-v50s-touch-result.png. PC 입력 주입으로 확인했으며 실제 손가락/물리 패널 배치 검증을 대신하지 않는다.

같은 PID에서 Pause → Settings → Input → Layouts → Edit → Save and exit → 게임 복귀 경로를 시험했다. Surface 교체 패치가 설치되어 있어도 양쪽 영상이 검게 나타났다. PID14255와 가상패드는 유지됐다. 따라서 이 패치는 해결책으로 확정하지 않는다. 증거: melonds-probe-editor-return.png. 편집 시험 중 L 크기 슬라이더를 잘못 조작하여 복귀 프로필의 L은 130x130/y0가 됐으며, 이 시험 결과는 최종 본체 버튼 배치 승인 대상이 아니다. 재시험 전 준비해 둔 정상 프로필로 복원한다.

다음 원인 구분을 위해 trial source에 ReGeneRenderProbe 로그를 추가했다. Activity의 실제 화면 영역, Surface 생성/크기/파괴, 렌더러 vertex 수와 화면 크기를 기록한다. 렌더러 수정은 아직 추가하지 않았으며, 원본 엔진 라이브러리는 그대로다.

진단 빌드는 31초에 성공했고 서명 검증 후 시험 패키지에 업데이트했다. 정상 프로필을 복원한 PID15561에서 설정 화면만 다녀오는 경로로도 문제가 재현됐다. 16:13:04.631 로그에서 확장 후 top/bottom Rect가 모두 0x0이며, 다음 렌더 프레임에도 같은 값이 적용됐다. 따라서 레이아웃을 편집해야만 발생하는 문제가 아니다. 증거: melonds-zero-rect-probe.log.

setupSoftInput의 handler.post를 viewLayoutControls.doOnPreDraw로 바꾸는 후보 패치를 추가했다. 화면 컴포넌트가 측정/배치되기 전에 getRect를 읽는 순서 문제를 해결하려는 변경이다. patches/melonds-layout-predraw.patch에는 현재 진단 로그도 포함된다. 성공 여부는 별도 복귀 시험으로 검증한다.

## doOnPreDraw 시험 결과

후속 빌드는35초에 성공했고 서명 검증 후 설치했다. PID16397에서 설정 화면 진입/복귀 시 최종 top/bottom Rect가 x492/y50 또는1150/1280x960으로 전달되고 양쪽 영상이 출력됐다. 이어 게임 중 레이아웃 편집기로 들어가 L을 실제로 이동하여 저장했다. private layouts.json의 L y1228/131x131로 저장 변경을 확인했다. 게임 복귀 후 같은 PID16397에서 상하 영상, 본체 영역의 모든 버튼, 가상 A 및 모험의 목적에 대한 하단 터치가 정상 동작했다. 이 시험에서 HOME 우회 복구는 사용하지 않았다.

증거: melonds-predraw-return-probe.log, melonds-predraw-editor-return.png, melonds-predraw-after-edit-input.png, melonds-predraw-after-edit-touch.png. 현재 설치본은 Surface 교체 후보와 doOnPreDraw 수정, 전체 패드 자동 숨김 후보, 진단 로그를 함께 포함한다. Surface 교체 후보만으로는 실패한 반면 doOnPreDraw 추가 후 두 복귀 경로가 통과했으므로 화면 배치 읽기 시점이 이번 실패에 직접 관련된 증거를 확보했다. 아직 반복 HOME/화면 꺼짐/실제 회전/컨트롤러 접속 시험을 통과한 최종 릴리스로 간주하지 않는다.

## HOME 및 화면 꺼짐 시험

같은 PID16397에서 HOME → 최근 앱 → melonDS Dev 카드 복귀를 시험했다. 복귀 직후에는 일반 세로 화면이었고 ReGene 지연 적용 뒤 2340x2160 상하 프로필로 돌아왔다. 하단 터치(1520,1980)로 모험 설명이 다음 문장으로 넘어갔다. 증거: melonds-predraw-home-settled.png, melonds-home-touch-advance.png.

KEYCODE_SLEEP 후 dumpsys power의 mWakefulness=Asleep을 확인하고 KEYCODE_WAKEUP으로 Awake를 확인했다. 직후 두 차례 UI dump는 null root이며 window focus도 null이었다. keyguard showing=false지만 primary/external screen state는 TURNING_ON이었다. 이후 터치와 재확인에서 EmulatorActivity가 focus를 얻고 같은 PID16397의 상하 영상이 유지됐으며 하단 터치로 설명이 다음 문장으로 넘어갔다. 증거: melonds-predraw-wake.png, melonds-wake-touch-advance.png, melonds-home-wake-probe.log. 잠금/긴 절전/물리 전원 버튼의 모든 경로가 통과했다고 확대 해석하지 않는다.

Surface 교체 변경은 단독으로 실패했고 doOnPreDraw에 직접 원인 증거가 있으므로, 수정 범위를 줄이기 위해 시험 소스의 EmulatorSurfaceView와 DSRenderer를 공식 버전으로 되돌리고 진단 로그를 제거했다. 이제 기능 변경은 EmulatorActivity의 doOnPreDraw 및 RuntimeLayoutView의 전체 가상패드 자동 숨김뿐이다. 기존 Surface 패치 파일은 실패한 후보의 기록으로 남겨두며 새 APK에는 적용하지 않는다. 정리한 빌드는 별도 시험해야 한다.

## 최소 변경 빌드 결과

정리한 빌드는29초에 성공했고 서명 검증 후 설치했다. APK SHA256 D7C43E3AF057A4BB52DDC13D5960EA031B8CBA8472C6FEB869DDEA9076D94481. 두 엔진 라이브러리 해시는 앞서 검증한 원본과 동일하다. 기능 변경은 두 Kotlin 파일이며 로컬 빌드 설정 변경은 시험 패키지/원본 네이티브 라이브러리 사용을 위한 것이다.

PID18692에서 설정 진입/복귀 후 상단 영상이 출력됐다. 이어 레이아웃 편집기에서 L을 y1228에서1149로 실제 이동·저장하고 게임으로 복귀했다. 같은 PID18692의 상하 영상과 본체 가상패드를 확인했으며, 가상 A로 안내 메뉴에 진입하고 하단 모험의 목적(1150,1630)을 눌러 해당 설명으로 이동했다. Surface 교체 후보 없이도 이 경로가 통과했다. 증거: melonds-minimal-editor-return.png, melonds-minimal-after-edit-menu-settled.png, melonds-minimal-after-edit-touch.png. 타이틀→안내 전환 중 짧은 검은 화면은 후속 캡처에서 정상 메뉴로 넘어갔으며 지속적인 렌더 실패로 분류하지 않는다.

최소 빌드의 HOME/절전 재시험과 실제 회전/물리 터치/커버/BT·USB-C 컨트롤러 시험은 남아 있다. 앞 절 PID16397의 HOME·절전 결과를 이 빌드의 직접 검증으로 대신하지 않는다.


## 최종 최소 수정본의 HOME / 화면 껐다 켜기 확인 (2026-10-04 후속)

현재 설치된 두 파일 수정 APK(PID18692)로 HOME → 최근 앱 → melonDS Dev 카드를 선택했다. PID가 유지되고 2340×2160 합성 캡처에서 상·하 게임 영상 및 본체 영역의 가상패드 배치가 유지됐다. `melonds-clean-home-return.png`.

KEYCODE_SLEEP 뒤 dumpsys power의 Asleep, KEYCODE_WAKEUP 뒤 Awake를 확인했다. PID18692가 유지됐으며 두 게임 영상과 배치가 그대로 나왔다. `melonds-clean-wake-return.png`. 복귀 후 하단 게임의 터치 안내 위치(1520,1980)에 1100ms 입력을 두 번 보냈고 설명이 다음 문장으로 진행됐다. `melonds-clean-wake-touch.png`. 두 입력 중 어느 입력부터 수신됐는지는 이 시험으로 구분하지 않았으므로 첫 터치 즉시 응답이나 실제 손가락 터치 통과를 주장하지 않는다.

물리 회전/커버 개폐/USB 디버깅 없이 실행/게임패드 연결·해제 검증은 여전히 남아 있다.

## 실제 Bluetooth 시험 준비와 기본 매핑 확인

2026-10-04 후속 조사에서 설치 시험판의 files 목록에 controller_config.json이 없었다. SharedPreferencesSettingsRepository는 이 파일을 읽지 못하면 DefaultControllerConfigurationFactory로 기본 매핑을 생성한다. 기본값은 오른쪽 얼굴 버튼(keyCode97)을 A, 아래 얼굴 버튼(96)을 B, HAT_X/Y 및 왼쪽 스틱 X/Y를 방향키, L1/R1을 L/R, START/SELECT를 해당 DS 입력에 연결한다. 이는 앞서 Azahar에서 실제 MX FLEX DUO 입력으로 확인한 버튼/축과 일치하므로 다음 melonDS 시험은 재매핑 요청 없이 기본값의 실제 동작부터 확인한다. 소스와 저장 상태의 일치이며 실제 melonDS 버튼 입력 성공 증거는 아니다.

앞선 melonDS 실행 시도에서 가상패드가 보인 캡처는 연결 시험 실패로 판정하지 않는다. 당시 Bluetooth 로그에 연결 해제 이벤트가 있었고 후속 입력 장치 목록에 MX FLEX DUO가 없었다. 사용자가 무입력 시 컨트롤러가 자동으로 꺼진다고 확인했으므로, 앱을 먼저 준비하고 짧은 조작을 묶어 요청한 상태에서 연결 여부와 숨김을 함께 확인해야 한다. 앱이 컨트롤러 연결을 끊었다는 증거는 없다.

다음 확인 범위는 실제 연결 중 모든 가상 버튼 숨김, 기본 매핑의 방향/확인/취소, 본체 게임 터치, 마지막 장치 분리 시 이전 표시 복원이다. Azahar의 물리 회전 시험 요청이 진행 중이므로 현재 게임 화면을 바꾸지 않고 PC에서 이 준비만 수행했다.

## 실제 Bluetooth 연결 중 전체 숨김과 확장 확인

Azahar 물리 조작 성공 응답 뒤 ReGene의 MELONDS 시험판 실행 버튼으로 기존 HG 게임에 복귀했다. Bluetooth STATE_CONNECTED, MX FLEX DUO 입력 장치13, melonDS PID18692 유지 상태다. 초기 일반 가로 화면은 `melon-bt-physical-ready.png`, 확장 후 안정된2340×2160 상·하 게임 영상은 `melon-bt-physical-ready-settled.png`다. 후속 ReGene 상태는 해당 EmulatorActivity의 확장 요청1이다.

확장 캡처에서 일반 패드와 부가 가상 버튼이 모두 없고, 두 DS 게임 영상과 게임 자체의 분홍색 터치 버튼은 유지됐다. 따라서 실제 연결 중 전체 가상 버튼 숨김을 확인했다. 이 시점 본체 손가락 터치, 실제 패드 입력, 세로/가로 재회전, 연결 해제·재연결은 사용자에게 한 묶음으로 요청했으며 결과 대기 중이다. 게임 자체의 터치 버튼은 숨김 대상인 에뮬레이터 가상패드가 아니다.

후속 사용자 응답은 “1 2 3 4 전부 성공”이었다. 실제 커버=상단/본체=하단과 손가락 게임 터치, 패드 조작, 세로→가로 자동 복구, 연결 해제 후 버튼 복원 및 재연결 후 전체 숨김이 요청 범위에서 통과했다. 실제 입력 로그 melon-physical-batch-input.log와 조작 후 캡처 melon-physical-batch-after-buttons.png를 보존했다. 세로/가로 감지는19:22:44.391/19:22:47.404, 재확장19:22:48.381, Bluetooth 분리/연결은19:22:57.512/19:23:02.457이고 PID18692가 유지됐다. 끊긴 짧은 구간의 캡처는 없으며 버튼 복원은 사용자의 실제 관찰로 확인했다. 커버 분리/재연결, PC 없이 실행, USB-C 및 여러 컨트롤러는 여전히 미검증이다.
