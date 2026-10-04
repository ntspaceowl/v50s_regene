# 재빌드 자료와 적용 패치

ReGene는 `app/src`와 `app/res`를 `app/build.ps1`로 빌드한다. `app/build/debug.keystore`는 기존 설치본과 같은 서명을 유지하는 데 필요하므로 빌드 출력 정리에서도 보존한다. `releases`에는 ReGene, Citra, Azahar, melonDS의 최종 APK를 하나씩 보관한다. 이 로컬 파일들은 Git에 포함하지 않는다. DraStic은 원본 설치본을 사용한다.

## Citra

`app/build-citra-controller-probe.ps1`은 `downloads/Citra_MMJ_20251112.apk`와 `research/apktool_3.0.3.jar`, ReGene 서명 키를 사용한다. 디코드 폴더는 매번 재생성한다. 현재 실사용본은 `org.citra.rgn`이며 Java/JNI 클래스 이름은 원본을 유지한다. `docs/patches/citra-controller-overlay.patch`는 다른 소스 트리에서 시도한 과거 후보이며 이 APK의 적용 패치가 아니다.

## Azahar

원본 기준은 [Azahar](https://github.com/azahar-emu/azahar)의 `9e6f523a57fac9564ac0bf8286db3c3702d301ec` (2126.1.2)이다. 로컬 `research/azahar-build`에 원본 Git 이력과 최종 수정 소스를 보존했다.

적용 순서와 파일:

1. `azahar-local-build.patch`: ARM64, 격리 패키지, 원본 네이티브 라이브러리 재사용.
2. `azahar-hidden-display.patch`: 숨은 가상 화면의 ON 상태 유지.
3. `azahar-controller-overlay.patch`: 실제 패드 감지 시 전체 가상패드 숨김과 눌린 입력 해제.
4. `azahar-resume-surface.patch`: 교체 Surface 이후 게임 재개.
5. `azahar-library-splash.patch`: 게임 종료 후 목록 화면 복구.

공식 APK에서 추출한 `src/android/app/prebuiltJniLibs`는 로컬에 보존한다. `JAVA_HOME`을 Android Studio JBR로 설정하고 `src/android/gradlew.bat :app:assembleVanillaDebug --console=plain`으로 빌드한다. Azahar 패치는 원본의 GPLv2-or-later 라이선스를 따른다. 원본 프로젝트에 대한 PR/이슈 제출은 이번 작업에 포함하지 않는다.

## melonDS

원본 기준은 [melonDS Android](https://github.com/rafaelvcaetano/melonDS-android)의 `5ec3648d68382dea9c17d6fbf02544e5c2c0afed`이다. 로컬 `research/melonds-build`에 원본 Git 이력과 수정 소스를 보존했다.

현재 적용 파일은 `melonds-local-build.patch`, `melonds-controller-overlay.patch`, `melonds-layout-predraw.patch`다. `melonds-surface-replacement.patch`는 실패한 과거 후보이며 최종 APK에 적용하지 않는다. `app/prebuiltJniLibs`의 원본 ARM64 라이브러리도 보존했다. JBR 환경에서 `gradlew.bat :app:assembleGitHubProdDebug --console=plain`으로 빌드한다. 외부 소스의 라이선스는 해당 프로젝트 원본을 따른다.

## 정리 후 보관 범위

원본 입력 APK 두 개, apktool, 수정된 소스, 원본 Git 이력, 네이티브 라이브러리, 서명 키, 최종 APK, 복구용 백업은 유지한다. 디컴파일 덤프, 중간 APK, 실험용 설정 복사본, 프로젝트별 Gradle/Kotlin 캐시, 소스 아카이브에 포함된 셰이더 캐시, 설치가 끝난 LG 드라이버 파일은 제거했다. 과거 문서의 캡처/로그 파일명은 당시 시험 기록이며 해당 파일은 사용자 요청으로 정리되어 현재 존재하지 않을 수 있다.
