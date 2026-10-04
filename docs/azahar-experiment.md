# Azahar V50S 가상 화면 시험

2026-10-04 후속 패널 검증: 합성기의 물리 디스플레이 ID와 projection을 대조해 커버 위쪽/본체 아래쪽 배정을 확인했다. 디스플레이 ID별 screencap도 전체 캔버스를 반환하므로 패널 사진 증거로 쓰지 않는다. 실제 회전·손가락 터치는 미검증이다. [세부 기록](panel-projection.md).

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
