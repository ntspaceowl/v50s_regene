# V50S 가로 확장의 패널 배정 확인

2026-10-04, ReGene0.1.8 / Azahar 분리 시험판 / PID29465. 자동 자세 판단을 잠시 끄고 저장된 가로 배치를 적용했다. 실물 회전으로 활성화된 시험은 아니다. 이후 자동 배치를 다시 켰다.

## 시스템에서 확인한 사실

`dumpsys SurfaceFlinger --display-id`와 `dumpsys display`를 대조했다.

| 패널 | 물리 디스플레이 ID | LG 장치 이름 |
|---|---|---|
| 본체 |4630946631802794625|기본으로 제공되는 화면|
| 커버 |4613331581648109316|Built-in Cover-Screen / ANX7530 DP|

확장 중 두 패널의 layerStack은0이고 방향은3이다. 논리 프레임은 두 패널 모두 `(0,0)-(2340,2160)`이다. 가로 물리 패널 크기는 각2340×1080이다.

- 본체의 `mCurrentDisplayRect`: `(0,-1076)-(2340,1080)`.
- 커버의 `mCurrentDisplayRect`: `(0,0)-(2340,2156)`.
- SurfaceFlinger의 `orientedDisplaySpace`도 같은 프레임을 기록한다. 본체·커버의 HWC layer 목록에 Azahar의 SurfaceView와 EmulationActivity가 모두 포함되고 powerMode는On이다.

이 배정에서 본체는 논리 캔버스의 아래쪽, 커버는 위쪽을 물리 표시 범위에 포함한다. 단순 중앙 y1080 분할과 정확히 일치하지는 않는다. 프레임 비율로 계산하면 커버의 표시 범위는 논리 y약0~1082, 본체는 약1078~2160이다. 이것은 시스템 프레임에서 계산한 범위이며 실제 패널 사진을 측정한 결과는 아니다. 게임 화면 사이의 검은 여백 안에 이 경계가 들어가는 현재 Azahar 레이아웃과 일치한다.

## 디스플레이별 캡처의 한계

`screencap -p -d <각 물리 ID>`로 두 장을 캡처했지만 **두 결과 모두2340×2160 전체 캔버스**였다. 영상 프레임 시각만 다르고 각 패널의 잘린 결과가 아니다. 파일명에 physical이라고 썼어도 패널별 표시를 촬영한 증거로 취급하지 않는다. 캡처만으로 두 화면의 실제 최종 출력이나 손가락 터치 정확도를 통과로 판정하지 않는다.

이번 시험은 합성기에서 커버 위쪽/본체 아래쪽 배정을 확인한 것이다. 실제 기기를 가로로 돌릴 때 활성화되는지, 실물 커버가 위에 놓인 방향에서 화면이 올바른지, 각 패널을 손으로 터치했을 때 좌표가 맞는지는 여전히 검증해야 한다.

## 증거 파일

`test-results/2026-10-04/`:

- `azahar-wide-display.txt`: DisplayManager의 패널 ID, viewport, layerStack과 projection.
- `azahar-wide-surfaceflinger.txt`: 합성기 projection과 각 HWC layer 목록.
- `azahar-physical-body.png`, `azahar-physical-cover.png`: 디스플레이 ID별 캡처의 한계를 확인한 전체 캔버스.

동일 프레임 배정이 다른 에뮬레이터에서도 유지되는지와 커버 분리·재연결 이후의 재설정은 이 시험에서 확인하지 않았다.
