# ReGene 0.1.10 커버 식별 보완

기존 코드는 논리 displayId=1과 STATE_ON만으로 커버를 판단했다. 0.1.10은 공개 Display.getName()의 `Built-in Cover-Screen`과 STATE_ON을 확인한다. 논리 ID가 달라져도 이름이 같은 실제 커버를 감지하고, 다른 앱의 HiddenDisplay가 켜진 것만으로 커버가 있다고 판단하지 않는다.

2026-10-04 V50S의 현재 dumpsys display에서 본체는 `기본으로 제공되는 화면`, 커버는 `Built-in Cover-Screen`/물리 uniqueId local:4613331581648109316/STATE_ON, Azahar 가상 화면은 `HiddenDisplay`/논리 ID81로 확인했다. 물리 uniqueId는 조사 근거이며 코드에서 기기별 물리 ID를 하드코딩하지 않는다.

빌드와 APK v2/v3 서명 검증 통과. APK versionCode11/versionName0.1.10, SHA256 `C875146FBFBBD97023E33D17C03D2E933AFB482D66BB7CEB587DA55C735F2E6A`. 기존 0.1.9 APK를 app/build/regene-0.1.9-debug.apk로 보존했으며 해시 D2686EFDE18A4A82C3D8DCF22B3E8CF18E19BA45137552852E9CAE1CC5061C82와 일치한다.

현재 진행 중인 Azahar 물리 조작 시험을 방해하지 않도록 **새 APK는 아직 설치하지 않았다**. 실제 설치본은 dumpsys package에서 versionCode10/versionName0.1.9다. 변경 후 실제 커버 감지/분리·재연결은 미검증이며, 현재 커버 ID가 실제로 바뀌었다는 증거도 없다. 이 보완을 커버 재연결 시험 통과로 기록하지 않는다.

후속: DraStic 일괄 시험을 사용자가 모두 통과했다고 확인한 뒤0.1.10을 install -r로 설치했다. dumpsys package에서 versionCode11/versionName0.1.10을 확인하고 ReGene의 DRASTIC 실행으로 기존 DraSticEmuActivity에 복귀했다. controller_running=true/selected=com.dsemu.drastic/auto_pose=true이며, 평평한 자세에서 가로 자세 감지 대기를 표시했다. PC USB 분리, 홈 복귀, 화면 껐다 켜기와 커버 닫았다 열기, 가능한 경우 실제 USB-C 컨트롤러 시험을 한 묶음으로 요청했다. 해당 결과 및 실제 커버 탈착/논리 ID 변경은 아직 미검증이다.
