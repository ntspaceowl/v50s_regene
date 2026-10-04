# Azahar V50S 가상 화면 시험

## 확인한 사실

LM-V510N Android 12에서 일반 앱이 `DisplayManager.createVirtualDisplay`를 호출했다. 출력 Surface가 없는 가상 화면 생성 직후 LG 확장이 해제됐다. 같은 크기의 ImageReader Surface를 연결하고 프레임을 소비하면 가상 화면이 ON이었고 확장 상태와 2340×2160 크기가 유지됐다. 로그는 `test-results/2026-10-04/virtual-display-probe.log`에 있다.

OFF 화면이 이미 존재할 때 다시 확장하는 것은 성공했다. 따라서 OFF 화면의 존재만으로 언제나 확장이 불가능하다고 해석하지 않는다. Azahar의 Activity 재생성과 OFF 화면 재생성이 반복되는 경로를 게임에서 별도로 검증해야 한다.

## 시험 빌드

공식 태그 `2126.1.2`, 커밋 `9e6f523a57fac9564ac0bf8286db3c3702d301ec`의 `SecondaryDisplay.kt`만 시험 수정한다. 변경안은 [패치](patches/azahar-hidden-display.patch)에 있다. 숨은 가상 화면에 ImageReader를 연결하고 프레임을 닫으며 VD 해제 후 ImageReader도 닫는다. 배터리/메모리/렌더러 및 재생성 안정성은 미검증이다.

로컬 시험 패키지는 `org.azahar_emu.azahar.regeneprobe`로 원본 앱과 분리한다. 공식 APK의 ARM64 네이티브 라이브러리를 그대로 사용하며 Kotlin/Java 부분만 재빌드한다. 입력 APK SHA256: `919165CC140FABFB71E35BF7B19F3436D581EE068EAD06DFF1D8112D651F17FE`.

연구 checkout은 `research/azahar-build`, 빌드 경로는 `src/android`. 로컬 Gradle에서 CMake 설정 두 곳을 제외하고 ABI를 ARM64로 제한하며 `prebuiltJniLibs`에 공식 APK 라이브러리를 넣었다. `JAVA_HOME`을 Android Studio JBR로 설정하고 `gradlew.bat :app:assembleVanillaDebug --console=plain`을 실행한다. 원본의 화면 출력 및 Activity manifest 정책은 변경하지 않았다.

시험 빌드는 빌드/설치에 성공했다. OpenGL 커비에서 2340×2160 확장이 약 40초간 유지됐고 HOME/최근 앱 복귀 후 같은 프로세스에서 게임 영상과 확장이 회복됐다. 원본에서 관찰한 0.5초 내 반복 해제는 이 시험에서 나타나지 않았다. 물리 회전, 모든 버튼의 본체 배치, 게임 터치 및 나머지 lifecycle은 미완료다. 저장한 custom 좌표와 실제 렌더링이 다른 문제도 발견돼 설정 매핑 확인이 필요하다.

ReGene만으로 원본 Azahar가 해결됐다는 증거가 아니며, upstream에 제출하거나 공개 배포하지 않았다. 연구 checkout의 AI-POLICY.md는 자율 PR/이슈 제출을 금지하므로 향후 upstream 제안은 사람이 직접 검증하고 해당 정책에 맞춰 진행해야 한다.
