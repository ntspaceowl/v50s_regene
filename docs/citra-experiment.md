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
