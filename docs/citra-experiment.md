# Citra MMJ 자동 가상패드 숨김 시험

2026-10-04 현재: 소스 패치를 준비했으나 시험 APK 빌드는 미완료다. 휴대폰의 원본 Citra는 변경하지 않았다.

## 변경 범위

기준은 로컬 `research/citra-20251112.zip`의 Android 소스다. `InputOverlay.java`에 외부의 활성 GAMEPAD/JOYSTICK 장치 감지와 InputManager 리스너를 추가했다. 연결 시 전체 가상 입력 객체를 제거하고 연결 해제 시 기존 수동 표시 설정으로 다시 만든다. 사용자 설정은 변경하지 않는다. 숨김 직전에 가상패드에서 누른 입력만 해제하며, 하단 화면 터치를 담당하는 InputOverlayPointer 객체는 보존한다.

패치: [citra-controller-overlay.patch](patches/citra-controller-overlay.patch). 소스 준비만 완료됐으며 실행 검증을 통과한 패치가 아니다.

시험 환경은 applicationIdSuffix `.regeneprobe`, 기본 저장 폴더 `CitraReGeneProbe`로 분리했다. CMake 빌드 대신 기존 APK의 arm64 라이브러리 5개를 그대로 복사했다. 복사본의 SHA-256 일치도 확인했다. 아직 APK가 생성되지 않아 최종 APK의 네이티브 동일성은 미검증이다.

## 빌드에서 확인한 문제

JBR 21, Gradle 8.14.5, AGP 8.13.0, Android SDK 31에서 시험했다. 기존 소스 Manifest가 참조하는 `xml/shortcuts`가 없어 해당 메타데이터를 시험 작업 사본에서 제거한 뒤 Java 컴파일까지 진행했다.

이후 55개 Java 오류가 발생했다. 가상패드 위치용 `R.integer.BUTTON_A_X` 등의 리소스, 채팅 화면 ID, `NativeLibrary.SetBackgroundGLSL` 선언 등이 빠져 있다. 수정 전 InputOverlay로 되돌려 같은 빌드를 다시 실행했으며, 파일별 오류 내용과 누락 심볼을 비교한 결과 동일한 55개 오류였다. 새 연결 감지 메서드에서는 컴파일 오류가 보고되지 않았지만, 전체 빌드 성공을 대신하는 증거는 아니다.

로컬 로그: `research/citra-probe-build.log`, `research/citra-probe-baseline-build.log`. 수정 사본은 다시 복원했다. 시험 APK 설치/게임 실행/연결 및 해제/게임 터치는 전부 미검증이다.

다음 작업은 설치 APK와 일치하는 리소스 및 JNI 선언의 출처를 확인하는 것이다. 누락된 UI를 임의로 삭제하거나 원본 앱을 덮어쓰지 않는다.


## 설치 APK 리소스 비교 (2026-10-04 후속)

공식 Apktool 3.0.3으로 로컬 APK를 리소스만 추출했다(`research/citra-apk-resources`). 설치 APK의 리소스에도 공개 소스가 참조하는 BUTTON_A_X 계열의 integer와 fragment_emulation 레이아웃이 없고 activity_emulation 레이아웃이 존재했다. 따라서 소스의 누락 파일을 APK에서 단순히 복원해 빌드하는 접근으로는 현재 불일치를 해소할 수 없다. 소스와 실제 배포 APK의 Android 프런트엔드가 일치하지 않는다는 근거다.

기존 DEX disassembly에는 SetBackgroundGLSL(String)이 PUBLIC STATIC NATIVE로 존재하는 것도 확인했다. JNI 선언 하나를 복원하는 것만으로 전체 프런트엔드 호환성이 증명되지는 않는다. 원본 APK 리소스 전체 덮어쓰기나 관련 UI 삭제를 통한 강제 빌드는 진행하지 않았다. 다음 선택지는 설치본과 맞는 Android 소스 확보 또는 기존 APK 동작을 이용한 companion 제어 경로 검토다.


## 기존 앱의 실행 중 숨김 UI 확인 (2026-10-04 후속)

기존 Citra MMJ에서 BACK으로 게임 메뉴를 열고 Settings → Hide Input Buttons를 확인했다. `Edit Buttons/Toggle Controls` 항목은 DONE 버튼이 있는 배치 편집 화면으로 이동하며 전체 숨김 토글 자체는 Settings에 있다. 초기 checkbox는 꺼져 있었다.

Hide Input Buttons를 켜고 설정 창을 닫았을 때 전체 가상패드가 사라졌으며 상·하 게임 영상과 PID22161가 유지됐다(`citra-hidden-game.png`, `citra-hidden-settings-checked.png`). 같은 항목을 다시 꺼 원래 표시 상태로 복원했다. `citra-restored-manual.png`에서 L/R, stick, d-pad, face, HOME, SELECT/START까지 다시 표시되고 PID22161가 유지됐다. 실제 게임패드 연결 이벤트로 실행한 시험이나 숨김 중 하단 게임 터치 통과는 아니다.

게임 영상 및 메모리 숫자가 계속 갱신될 때 uiautomator dump가 idle timeout으로 실패했다. 최초 메뉴에서는 텍스트 노드를 읽었지만 Settings checkbox의 접근성 노드/checked 상태는 아직 읽지 못했고 캡처로만 확인했다. companion 구현에서는 실제 AccessibilityService에서 체크 상태와 클릭 성공을 검증해야 하며 화면 좌표에만 의존한 자동 토글은 충분한 근거가 없다. 이전 수동 상태를 저장하고 임시 변경한 경우에만 복원하는 처리도 필요하다.

이번 첫 시도 중 16:47:53 PID20446의 NativeEmulation SIGSEGV, 16:48:31 PID21436의 InputOverlay Map.get null 오류가 발생했다. 둘의 원인을 단정하지 않는다. 재실행 PID22161에서는 메뉴 편집/숨김/복원 과정이 유지됐다. shell am stopservice는 오류를 반환했고 이후 dumpsys에서 ControllerService가 실행 중이었다. 따라서 이번 결과는 companion을 중지한 뒤 성공한 시험으로 해석하면 안 된다.


## ReGene 0.1.4 읽기 진단 구현 및 실기 확인

CitraDiagnosticService를 추가했다. 서비스 XML의 이벤트 대상은 org.citra.emu이며, 활성 창의 패키지를 다시 검사한 뒤 정확한 Hide Input Buttons 라벨을 찾는다. 가까운 부모 행에 체크 가능한 노드가 하나인 경우에만 checked 상태를 보고한다. 여러 후보가 있거나 상태를 찾지 못하면 추측하지 않는다. 메뉴 열기, 클릭, 제스처, 설정 변경 API는 구현하지 않았다. 해당 서비스의 접근성 능력은 창 콘텐츠 읽기(capabilities=1)다. 이벤트 및 1초 주기로 확인하고 동일한 상태의 로그/설정 쓰기는 생략한다. MainActivity에는 최근 진단 기록과 접근성 설정 링크를 추가했다.

빌드 및 v2/v3 서명 확인 후 휴대폰에 ReGene versionCode5/versionName0.1.4로 설치했다. 앱 첫 실행 뒤 LG 접근성 설정 목록에 서비스가 표시됐고 해당 서비스의 표준 사용/켜기 UI로 활성화했다. dumpsys accessibility에서 Bound services/Enabled services 포함, Crashed services 비어 있음을 확인했다. 다른 접근성 서비스는 변경하지 않았다.

실행 중 Citra Settings를 열어 ReGene 로그에서 16:59:17 OFF를 읽었다. ADB로 같은 설정 체크박스를 수동 조작해 16:59:57 ON, 다시 원래 상태로 되돌려17:00:00 OFF를 읽었다. Citra PID22161 유지. 이번 결과는 체크 상태 읽기의 OFF→ON→OFF 실기 검증이며, 서비스가 자동 클릭한 시험이나 물리 게임패드 이벤트 검증은 아니다. Citra 설정은 최종 OFF로 복원하고 창을 닫았다.

APK SHA-256: 401D02631A387520D5BB3FED96EB7AC5AF887921CBE7EA43AC01CEA86E6F8B9A. 진단은 활성화돼 있으며 실제 자동 숨김·복원 구현과 숨김 중 하단 터치 검증은 다음 단계다.


## ReGene 0.1.5 기존 Citra의 메뉴 자동 조작

ControllerService가 선택 앱/실행 여부/현재 Activity를 게시하고, Citra 서비스는 선택된 Citra EmulationActivity 및 활성 Citra 창일 때만 조작한다. 외부의 활성·비가상 GAMEPAD/JOYSTICK 장치 또는 명시적인 시험 모드에서 숨김을 요청한다. 버튼 상태는 체크박스를 읽고 semantic ACTION_CLICK으로 변경하며 고정 화면 좌표는 코드에 사용하지 않는다. BACK으로 메뉴 → Settings → Hide Input Buttons를 조작하고, checked 목표 상태가 확인된 뒤 설정 창을 닫는다. 기존 Settings/편집 화면이 열린 경우에는 시작을 미룬다. 다른 게임 내 대화상자 및 작업 중 사용자 개입 검증은 남았다.

처음 OFF에서 숨기는 경우에만 citra_hide_owned 복원 기록을 클릭 전에 commit한다. 이미 ON인 경우 복원 소유권을 얻지 않는다. 마지막 장치가 분리되면 소유한 변경만 OFF로 되돌린다. 앱 밖에서는 조작하지 않고 복원 기록을 유지해 Citra로 돌아왔을 때 처리한다. 전체 흐름은12초 제한, 실패 후 같은 요청은60초 대기하며 장치/시험 모드 상태가 바뀌면 대기 상태를 재평가한다. 확인된 같은 요청으로 매초 설정을 다시 여는 동작은 생략한다.

ReGene 0.1.5/code6 빌드와 v2/v3 서명 검증 뒤 설치했다. 제어앱의 Citra 가상패드 숨김 시험 버튼을 눌렀을 때 17:04:55 OFF →17:04:57 숨김 확인/ON 로그, 캡처 `citra-auto-hide-trial.png`에서 전체 패드가 사라졌다. owned=true/test_mode=true 확인. 시험 종료·복원 버튼을 눌렀을 때17:05:46 ON →17:05:47 OFF →17:05:48 복원 확인 로그와 `citra-auto-restore-trial.png`에서 가상패드 재표시를 확인했다. owned=false/test_mode=false로 종료했다. Citra PID22161가 유지되고 두 게임 영상이 계속 표시됐다.

이 시험은 실제 외부 장치를 가장한 ADB 주입이 아니라 앱에 명시적으로 제공한 시험 버튼으로 같은 메뉴 제어 경로를 실행한 것이다. 물리 BT/USB-C 연결, 최초 수동 ON 유지, 숨김 중 하단 터치, 중간 사용자 개입/대화상자/중지·재시작 검증은 여전히 미완료다. 원본 Citra APK는 수정하지 않았다.


## 0.1.5 숨김 중 하단 터치 / 최초 수동 숨김 보존 검증

가상 A 입력으로 Kirby 파일 선택 화면에 진입한 뒤 제어앱 숨김 시험을 실행했다. 전환 직후 캡처 `citra-hidden-file-menu.png`에는 아직 Settings와 이전 패드가 나왔고, 후속 `citra-hidden-file-menu-settled.png`에서 메뉴가 닫히고 패드가 모두 사라졌다. 현재 서비스의 숨김 확인 로그는 checked와 BACK 요청 성공 시점이므로 실제 설정 창 닫힘/패드 제거 프레임보다 먼저 기록될 수 있다. 후속 개선은 메뉴 닫힘을 확인한 뒤 완료 상태를 보고하는 것이다.

패드가 숨겨진 안정된 화면에서 하단 파일1 위치(700,1510)에1100ms 터치를 보내 모드 선택 화면으로 이동했다. 이후 특정 옵션인 디디디로 쿵쿵 위치(1530,1340)를 같은 방식으로 눌러 해당 모드 타이틀/시작 화면으로 진입했다. `citra-hidden-lower-touch-result.png`, `citra-hidden-dedede-touch.png`. 두 화면 영상과 패드 숨김이 유지되고 Citra PID22161 유지. 이는 ADB 터치 좌표 전달 검증이며 실제 손가락/패널 연결 시험을 대신하지 않는다.

시험 종료 버튼으로 제어앱의 숨김 소유권을 false로 돌린 뒤, Citra Settings에서 Hide Input Buttons를 직접 ON으로 설정했다. 이때 test_mode=false/owned=false였다. 그 상태로 제어앱 숨김 시험을 실행한 뒤에도 owned=false가 유지됐으며, 시험 종료 후 test_mode=false/owned=false와 패드 숨김이 유지됐다. `citra-preserved-original-hidden.png`. 다시 Settings를 열어17:14:33 로그 및 checked=ON 캡처로 수동 상태 유지도 확인했다.

마지막으로 이번 테스트를 위해 수동으로 바꾼 설정을 원래 OFF로 되돌렸다. prefs의 test_mode=false/owned=false 및17:14:51 OFF 로그, `citra-preserve-cleanup-restored.png`의 패드 재표시 확인. 기존 파일1의1% 상태를 선택해 메뉴만 탐색했으며 빈 파일 생성/삭제나 gameplay 진행은 하지 않았다. 물리 게임패드 연결·해제와 커버/회전/PC 없이 실행 검증은 남아 있다.


## ReGene0.1.6 설정 창 닫힘 확인 단계

checked 목표 상태 확인 → BACK 요청 뒤 별도 stage4에서 Hide Input Buttons/Settings 항목이 활성 Citra 창에서 사라질 때까지 기다린다. 이전의 완료 로그를 바로 내보내지 않고 닫힘 대기 상태를 먼저 보고한다. 복원 소유권도 해당 완료 단계에서 해제한다. 창이 닫히지 않으면 기존 전체 작업 제한에 따라 실패로 기록한다.

작업 중 요구 상태가 달라질 때 stage를0으로 초기화해서 스스로 연 메뉴를 남기는 경로도 보완했다. 변경 소유권이 없고 숨김 요청이 해제되면 체크 상태를 바꾸지 않고 메뉴만 닫으며, 이미 임시 변경했다면 목표 체크 상태를 다시 맞춘다. 닫힘 확인 중 상태 변화는 현재 닫힘을 마무리하고 다음 요청에서 처리한다. 이 중간 상태 변경 분기는 아직 실기로 검증하지 않았다.

0.1.6/code7 빌드, v2/v3 서명 확인, 설치 후 명시적 시험 버튼으로 다시 확인했다. 숨김 로그17:18:43 닫힘 대기 →17:18:44 닫힘/숨김 확인, 복원 로그17:19:38 닫힘 대기 →17:19:39 닫힘/복원 확인. 캡처 citra-016-close-confirm-hide.png / citra-016-close-confirm-restore.png에서 설정 메뉴가 사라진 뒤 각각 전체 패드 제거/재표시를 확인했다. Citra PID22161 유지 및 두 게임 영상 유지. 종료 후 test_mode=false/owned=false 확인.

APK SHA-256 C7A48E66A9018DFBF224999BE5C95ACAD2599262A3E237667DAAF3FF1559E8A9. 실제 물리 장치 및 미확인 대화상자/사용자 개입, 회전·커버·PC 없이 실행은 여전히 별도 검증 대상이다.

## 실제 물리 시험: 세 항목 통과, 컨트롤러 제거 시 충돌

2026-10-04 후속 실제 설치본 org.citra.emu/MMJ20251112 시험에서 사용자는 패드 방향/확인/취소, 커버=상단·본체=하단과 손가락 터치, 세로→가로 복귀의1·2·3을 통과했다고 확인했다. 네 번째 컨트롤러 껐다 켜기에서는 게임이 종료되고 Citra 목록으로 돌아왔다. 따라서 Citra 연결 해제 자동 복원은 실패다.

정상 게임 PID22321에서19:26:48.302 입력 장치 구성 변경0x60이 발생했고19:26:48.827 EmulationActivity relaunch의 finishDrawing,19:26:49.092 NativeEmulation 스레드 SIGSEGV가 이어졌다. 새로운 PID22585는 MainActivity였다. ReGene의 합성 BACK 입력은19:26:49.170으로 fatal 뒤였다. 해당 관측의 직접 충돌을 이 BACK 입력이 먼저 유발했다고 볼 수 없다.

설치 APK의 EmulationActivity configChanges는0xD80(orientation/screenSize/screenLayout/smallestScreenSize)이며 keyboardHidden=0x20와 navigation=0x40을 처리하지 않는다. 컨트롤러 제거로 두 비트가 바뀌어 게임 Activity를 재생성했고 네이티브 실행과 충돌한 것이 유력한 원인이다. 수정 후보는 keyboard/keyboardHidden/navigation 변경을 Activity에서 처리하도록 선언하는 것이다. 이 후보를 아직 빌드·설치하거나 해결됐다고 확인하지 않았다. 최초 별도 실행의 IsSearchingForAmiibos 충돌은 다른 스택이며 동일 원인으로 단정하지 않는다.

증거: citra-physical-batch-input.log, citra-physical-batch-observed.png, citra-controller-disconnect-crash.log, citra-controller-disconnect-regene.log, citra-relaunch-disconnect-sequence.log, citra-connected-launch-crash.log (test-results/2026-10-04). 복원 소유권 citra_hide_owned=true는 유지하며 원래 수동 설정을 무시하고 기록을 지우지 않는다.

후속: app/build-citra-controller-probe.ps1로 기존 설치 APK 기반 분리 시험판을 빌드했다. 동작 수정은 EmulationActivity configChanges에 keyboard/keyboardHidden/navigation 추가(0xDF0)이며, 시험 격리를 위해 패키지를 org.citra.rgn, provider authority를 해당 패키지로, 공용 사용자 폴더 리터럴을 citra-rgn으로 바꿨다. Java/JNI 클래스 이름은 유지한다. 다섯 네이티브 라이브러리는 원본 APK와 바이트 단위로 같고 v2/v3 서명 검증에 통과했다. APK SHA256 EE5C2C5B9CD4B7595F665E39B1B141F36B82DDF9995595658C5F3A899592BEA3. 현재 PC 분리 시험을 방해하지 않도록 설치하지 않았다. 별도 폴더와 네이티브 경로의 런타임 확인, ReGene 시험판 선택·숨김 연동 및 실제 연결 해제 재시험은 남아 있다. 빌드 성공을 충돌 해결로 간주하지 않는다.
