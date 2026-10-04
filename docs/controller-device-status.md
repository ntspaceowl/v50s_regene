# ReGene 0.1.8 게임패드 연결 상태 표시

제어앱에서 Android가 현재 게임패드로 인식한 장치 이름과 대수를 1초마다 표시한다. 페어링 목록이나 Bluetooth 활성화만으로 연결되었다고 표시하지 않는다. 별도의 Bluetooth 권한은 추가하지 않았다.

`GameControllers`는 활성화된 외부 입력 장치 중 비가상 GAMEPAD 또는 JOYSTICK 장치를 선택한다. 기존 Citra 자동 숨김의 장치 판정도 같은 함수를 사용하도록 옮겼으며 판정 조건은 유지했다. LG 커버의 입력 소스 `0x00001103`은 여기에 해당하지 않는다. 표시가 연결됨이어도 에뮬레이터 매핑이나 실제 버튼 입력 성공을 의미하지 않는다.

## 실기기 검증

- V50S에 versionCode9/versionName0.1.8 설치, APK v2/v3 서명 검증 통과.
- MainActivity의 접근성 텍스트와 화면 캡처에서 `게임패드: 연결되지 않음` 확인.
- 같은 시점 Android input에 GAMEPAD/JOYSTICK 장치가 없고 Bluetooth는 STATE_DISCONNECTED, MX FLEX DUO는 bonded 목록에만 있다.
- 캡처: `test-results/2026-10-04/regene-018-controller-status.png`.
- APK SHA256: `26626F72593BEA92528D928EE7956DC76D0EF983D427AE489E99F7CBDD3B9274`.

실제 연결/분리 때 이름·대수가 변하는 시험과 여러 장치 동시 연결은 아직 미검증이다. 이번 변경은 상태 표시이며 네 에뮬레이터의 전체 가상패드 자동 숨김 완료를 뜻하지 않는다.
