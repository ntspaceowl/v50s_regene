# Azahar V50S 가상 화면 시험

2026-10-04 후속 패널 검증: 합성기의 물리 디스플레이 ID와 projection을 대조해 커버 위쪽/본체 아래쪽 배정을 확인했다. 디스플레이 ID별 screencap도 전체 캔버스를 반환하므로 패널 사진 증거로 쓰지 않는다. 실제 회전·손가락 터치는 미검증이다. [세부 기록](panel-projection.md).

2026-10-04 실제 BT 후속: combo 해제 보완을 포함한 현재 설치 시험판에서 MX FLEX DUO가 device10 / sources0x01000711 / STATE_CONNECTED로 재연결됐다. PID29465 유지, 가상패드의 버튼·스틱·START/SELECT가 자동으로 사라졌다. 검증을 위해 자동 자세 판단을 잠시 끈 가로 배치에서 두 게임 화면이 유지된 것을 `azahar-bt-wide-settled.png`로 확인했다. 전환 직후 `azahar-bt-wide-hidden.png`는 검은 화면이므로 영상 유지의 증거가 아니다. 커버 분리·실제 회전은 이번 시험 범위가 아니다.

격리 시험판 SharedPreferences에는 Host 버튼/축 매핑이 아직 없다. 방향키 입력 관찰은 입력 없이 제한 시간 종료됐고 실제 게임 입력 성공을 판정하지 않았다. 이어 앱의 Settings → Gamepad → Auto-Map Controller를 열어 실물 오른쪽 face 버튼 입력을 요청했다. 이 관찰도 입력 없이 종료됐고 컨트롤러가 STATE_DISCONNECTED로 바뀌었다. Auto-Map을 취소했으며 Host 버튼/축 매핑은 저장되지 않았다. 연결과 표시 숨김을 입력 매핑 성공으로 간주하지 않는다.

연결 해제 후 Settings에서 게임으로 복귀했을 때 기존 L/R, D-pad, 스틱, ABXY, START/SELECT가 다시 표시되고 상·하 영상과 PID29465를 유지했다 (`azahar-bt-disconnected-restored.png`). 이는 **설정 화면에서 분리된 뒤 게임 복귀 시 표시 복원**의 확인이다. 게임 도중 분리 즉시 복원이나 원래 수동 숨김 상태를 유지하는 경우의 실물 장치 시험은 아니다. 숨김 중 하단 터치도 이번 BT 시험에서 아직 확인하지 않았다.

## 후속: 실물 자동 매핑과 연결 중 하단 터치

사용자가 다시 연결한 MX FLEX DUO(device11)로 Auto-Map Controller의 오른쪽 face 버튼을 눌렀다. 앱 로그는 keyCode97 / Xbox layout / axis d-pad / device=MX FLEX DUO를 기록했고 설정 창이 닫혔다. SharedPreferences에 HostAxis97→guestA700,96→guestB701과 두 스틱 및 방향키축15/16 등의 매핑이 저장됐다. 원본 Azahar/Citra 설정과 분리된 시험판 설정이다.

게임 복귀 후 사용자는 “상하좌우 다 잘돼”라고 확인했다. 해당 장치 getevent에서 HAT0X/HAT0Y 방향과 ABS_X 스틱 변화 및 중립 복귀도 관찰했다. 실물 방향 입력의 게임 동작은 사용자 확인과 장치 이벤트로 검증한 범위이며 모든 face/숄더/START/SELECT 버튼의 개별 게임 동작을 시험한 것은 아니다.

STATE_CONNECTED와 전체 패드 숨김을 유지한 상태에서 ADB 하단 터치 홀드(1540,1500,1000ms)를 보냈다. 상단 제목과 하단 선택 테두리가 빈 파일1에서3으로 바뀌었다 (`azahar-bt-hidden-lower-touch-settled.png`). 바로 앞 짧은 tap 직후 캡처는 파일1 그대로였으므로 그 캡처를 성공 증거로 쓰지 않는다. PID29465와 상·하 영상, 표시 숨김이 유지됐다. 실제 손가락 터치와 물리 회전은 별도 검증이 필요하다.

증거: `azahar-real-auto-map.log`, `azahar-after-real-auto-map.xml`, `azahar-bt-ready-mapped.png`, `azahar-real-controller-directions.png`, `azahar-bt-hidden-lower-touch-settled.png` (test-results/2026-10-04).

## 확인한 사실

LM-V510N Android 12에서 일반 앱이 `DisplayManager.createVirtualDisplay`를 호출했다. 출력 Surface가 없는 가상 화면 생성 직후 LG 확장이 해제됐다. 같은 크기의 ImageReader Surface를 연결하고 프레임을 소비하면 가상 화면이 ON이었고 확장 상태와 2340×2160 크기가 유지됐다. 로그는 `test-results/2026-10-04/virtual-display-probe.log`에 있다.

OFF 화면이 이미 존재할 때 다시 확장하는 것은 성공했다. 따라서 OFF 화면의 존재만으로 언제나 확장이 불가능하다고 해석하지 않는다. Azahar의 Activity 재생성과 OFF 화면 재생성이 반복되는 경로를 게임에서 별도로 검증해야 한다.

## 시험 빌드

공식 태그 `2126.1.2`, 커밋 `9e6f523a57fac9564ac0bf8286db3c3702d301ec`의 `SecondaryDisplay.kt`만 시험 수정한다. 변경안은 [패치](patches/azahar-hidden-display.patch)에 있다. 숨은 가상 화면에 ImageReader를 연결하고 프레임을 닫으며 VD 해제 후 ImageReader도 닫는다. 배터리/메모리/렌더러 및 재생성 안정성은 미검증이다.

로컬 시험 패키지는 `org.azahar_emu.azahar.regeneprobe`로 원본 앱과 분리한다. 공식 APK의 ARM64 네이티브 라이브러리를 그대로 사용하며 Kotlin/Java 부분만 재빌드한다. 입력 APK SHA256: `919165CC140FABFB71E35BF7B19F3436D581EE068EAD06DFF1D8112D651F17FE`.

연구 checkout은 `research/azahar-build`, 빌드 경로는 `src/android`. 로컬 Gradle에서 CMake 설정 두 곳을 제외하고 ABI를 ARM64로 제한하며 `prebuiltJniLibs`에 공식 APK 라이브러리를 넣었다. `JAVA_HOME`을 Android Studio JBR로 설정하고 `gradlew.bat :app:assembleVanillaDebug --console=plain`을 실행한다. 원본의 화면 출력 및 Activity manifest 정책은 변경하지 않았다.

시험 빌드는 빌드/설치에 성공했다. OpenGL 커비에서 2340×2160 확장이 약 40초간 유지됐고 HOME/최근 앱 복귀 후 같은 프로세스에서 게임 영상과 확장이 회복됐다. 원본에서 관찰한 0.5초 내 반복 해제는 이 시험에서 나타나지 않았다. 물리 회전, 모든 버튼의 본체 배치, 게임 터치 및 나머지 lifecycle은 미완료다. 저장한 custom 좌표와 실제 렌더링이 다른 문제도 발견돼 설정 매핑 확인이 필요하다.

ReGene만으로 원본 Azahar가 해결됐다는 증거가 아니며, upstream에 제출하거나 공개 배포하지 않았다. 연구 checkout의 AI-POLICY.md는 자율 PR/이슈 제출을 금지하므로 향후 upstream 제안은 사람이 직접 검증하고 해당 정책에 맞춰 진행해야 한다.

## 추가 실험: 좌표 설정과 Surface 복귀

좌표 불일치의 원인을 확인했다. 공식 `default_ini.h`의 `[Layout]` 도중 `[Storage]`가 시작되고 custom 좌표는 그 이후에 기록된다. Java 설정 파서는 좌표 값을 읽지만 native config는 Layout 섹션에서만 읽어 기본값을 사용한다. 시험 폴더의 config.ini에서 Storage 헤더와 두 옵션을 Utility 직전으로 이동하여 나머지 값을 보존했다. 진단에서 native 좌표 300,50,1600,960 / 460,1140,1280,960을 확인했다. 원본 앱의 config는 변경하지 않았다.

읽기 전용 native 진단 helper와 로그 코드는 제거하고 다시 빌드/설치했다. 모든 가상패드 위치를 본체 영역으로 저장하고 scale20을 적용했다. 15:01 시험 PID30330에서 실제 화면 배치, 가상 A 입력, 하단 OK 터치로 다음 질문으로 진행을 확인했다. 증거: azahar-clean-body-controls.png, azahar-clean-after-a.png, azahar-clean-lower-touch.png.

그러나 15:03 HOME/최근 앱 복귀에서 SIGSEGV가 발생했다. ANativeWindow_setBuffersGeometry → CreateWindowSurface → PollEvents 경로이며 게임 유지 복귀는 실패다. 로그: azahar-clean-return-crash.log. 앞선 한 회 성공을 보편적인 안정성으로 확대하지 않는다.

다음 시험은 EmulationFragment.onResume의 즉시 unpause를 기존 emulationState.run으로 대체한다. 저장된 Surface가 없으면 Surface 콜백까지 재개를 기다리도록 하는 최소 Kotlin 가설 수정이다. 네이티브 라이브러리는 동일하며, Surface 수명 문제 전체가 해결됐다는 주장은 실험 결과가 나오기 전에는 하지 않는다.

## 조합 입력 해제 보완 APK 설치

2026-10-04 후속 빌드에서 컨트롤러 감지로 가상패드를 제거하기 직전, 눌린 COMBO_BUTTON에 `ComboHelper.comboActivate(RELEASED)`를 보내는 보완을 포함했다. `:app:assembleVanillaDebug` 성공, APK v2 서명 검증 성공, 분리 시험 패키지 `org.azahar_emu.azahar.regeneprobe`에 `adb install -r` 성공. 원본 패키지는 별도로 유지됐다.

APK SHA256: `7EF590E14862D67B1215A39D5F75F6A9E260478FD0EF061CDF18D17756C5339D`. 설치된 base.apk의 장치 SHA256도 같았다. 이전 기록의 조합 입력 보완 미설치는 이 APK로 해소됐다. 빌드/설치 확인이며 실제 조합 버튼을 누르는 도중 컨트롤러 연결 및 게임 런타임 검증은 아직 아니다. 실물 컨트롤러가 다시 연결되면 입력 매핑과 함께 시험해야 한다.

## 새 APK의 실제 게임·복귀 시험

동일 SHA APK를 ReGene의 Azahar 시험판 버튼으로 실행했다. PID29465, 격리 ROM `/sdcard/AzaharTest/Kirby.cci`, 격리 사용자 폴더로 시험했다. 초기 로딩에서는 검은 화면이 있었으나 이어서 상/하 영상이 출력됐다 (`azahar-combo-build-settled.png`). 현재 저장 좌표로 상단 게임은 위쪽, 하단 게임과 가상패드는 아래쪽이며 캡처 크기는2340×2160이다. 물리 패널별 사진은 아니다.

- 하단 게임 안내창의 OK를 ADB 터치하여 다음 확인창으로 바뀜을 확인했다 (`azahar-new-build-lower-ok-touch.png`). StreetPass 등록은 아니요를 선택했다. 시험판의 빈 파일 메뉴만 사용했고 원본 세이브를 수정하지 않았다.
- 게임 메뉴 Settings 진입으로 일반 화면으로 돌아갔다. BACK으로 설정과 서랍을 닫은 뒤 다시 확장됐고 하단 파일2 터치로 상단 표시/하단 선택이 파일1→파일2로 바뀌었다 (`azahar-after-settings-file2-touch.png`). PID29465 유지.
- HOME 뒤 최근 앱의 Azahar 카드로 복귀했다. 두 영상과 패드 좌표가 유지됐고 하단 파일3 터치로 선택이2→3으로 바뀌었다 (`azahar-after-home-file3-touch.png`). PID29465 유지. 먼저 시도한 패키지 한정 MAIN/LAUNCHER Intent는 resolve 실패했으므로 그 명령을 성공 경로로 기록하지 않는다.
- ADB SLEEP223에서 PowerManager Asleep, WAKEUP224에서 Awake를 확인했다. 게임 전경 및 확장이 회복됐고 하단 파일1 터치로 선택이3→1로 바뀌었다 (`azahar-new-build-wake-touch.png`). PID29465 유지.

모든 증거 파일은 `test-results/2026-10-04/`에 있으며 PID 로그는 `azahar-new-build-runtime.log`다. 시작 중 Surface/EGL 경고 및 Activity transaction 경고도 있었다. 현재 시험에서 영상·터치·동일 PID 유지가 확인됐다는 제한적인 결과이며 모든 게임/반복 lifecycle 안정성의 증거는 아니다.

ReGene의 auto_pose=false 벤치 조건이다. 실제 기기 회전, 커버 분리·연결, 실제 손가락, PC USB 분리 및 BT/USB-C 게임패드 입력/자동 숨김·복원과 눌린 조합 입력 해제는 남았다. 실제 MX FLEX DUO는 이 시험 당시 Bluetooth DISCONNECTED였으므로 컨트롤러 연결 시험으로 해석하지 않는다.

## 후속: 실제 게임 전경에서 Bluetooth 연결 해제 복원

후속 실제 MX FLEX DUO 연결 및 매핑/방향 입력 결과는 controller-overlay 관련 기록을 참조한다. 이번에는 설정 메뉴를 열거나 게임 화면을 전환하지 않고 연결 해제를 관찰했다.

- 해제 전 Bluetooth STATE_CONNECTED와 활성 MX FLEX DUO 입력 장치, Azahar EmulationActivity 전경, PID29465를 확인했다. `azahar-batch-before-input.png`에서 가상패드가 없고 빈 파일3 게임 영상이 출력됐다.
- 19:07:34.762의 BluetoothHidHostService 로그에서 해당 장치의 상태2→0 전환을 확인했다. 후속 Bluetooth STATE_DISCONNECTED, 입력 장치 목록의 MX FLEX DUO 없음, EmulationActivity 전경 및 PID29465 유지를 확인했다. 해제 원인을 로그만으로 단정하지 않는다.
- `azahar-ingame-idle-disconnect-restored.png`에서 L/R, 방향키, 스틱, ABXY, START/SELECT가 다시 표시됐다. 빈 파일3 게임 영상도 유지됐다. PC에서 메뉴를 여는 복구 동작 없이 표시가 복원됐으며, 연결 전 사용하던 가상패드 설정을 바꾸지 않았다. 상태 증거는 `azahar-ingame-idle-disconnect.log`다.

이 시점은 auto_pose=true에서 실제 가로 자세를 아직 감지하지 못한 일반 세로 화면(1080×2340)이다. 따라서 게임 전경의 실제 연결 해제 후 가상패드 복원을 확인한 것으로 한정한다. 확장 상태의 즉시 복원, 재연결 후 재숨김, 실제 회전/패널 배치/손가락/USB-C 및 원래 수동 숨김 상태에서의 실제 연결 시험을 이 증거로 대신하지 않는다.

## 실제 재연결 및 사용자 물리 조작 확인

MX FLEX DUO 재연결 후 Bluetooth STATE_CONNECTED, 활성 입력 장치13, Azahar PID29465를 확인했다. `azahar-ingame-reconnected-hidden.png`에서 같은 게임의 가상패드가 다시 사라졌다. 게임 메뉴를 열거나 프로세스를 다시 시작하지 않았으며 앞선 분리·복원과 같은 세션이다.

사용자는 요청한 실제 가로 회전의 커버=상단/본체=하단 배치, 본체 손가락 터치, 패드 확인·취소 버튼 시험에 대해 **성공**이라고 응답했다. ReGene 로그에서도 19:19:15.288에 실제 중력 x=-8.160335/y=-1.4970897의 landscape 후보를 감지했고 19:19:16.590에 확장 요청1을 기록했다. `azahar-user-physical-success.png`를 게임 전경에서 보존했으며 이 시점 auto_pose=true, 실제 설치 제어앱0.1.9다. 물리 패널 배치와 손가락/버튼 성공은 사용자 관찰 증거이며 합성 캡처만으로 대신한 판단이 아니다.

Azahar 시험판의 이 실제 회전·배치·손가락·확인/취소 시험은 통과했다. 다른 세 앱, 커버 분리·재연결, PC 없이 실행, USB-C, 모든 게임/반복 lifecycle 검증의 완료를 뜻하지 않는다.
