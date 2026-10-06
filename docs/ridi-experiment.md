# 리디 독서 프로필 (2026-10-06)

사용자 요청은 리디북스를 밀리의서재와 같은 좌우 독서 방식으로 처리하는 것이다. 실제 설치본은 com.initialcoms.ridi 26.9.1/code2026001589이며 열린 화면은 com.ridi.books.viewer.reader.pagebased.comic.ComicBookReaderActivity였다.

원본 리디 뷰어 설정 화면에서 ‘가로 모드에서 두 쪽 보기’와 ‘첫 페이지부터 두 쪽 보기’가 ON, ‘세로 모드에서 두 쪽 보기’가 OFF인 것을 확인했다. 이 설정은 변경하지 않았다. 책 뷰어의 SECURE 캡처 보호도 유지한다. 보호가 없는 설정 화면과 ReGene 런처만 임시 캡처했다.

ReGene 0.1.18/code19에 리디 독서 버튼과 세로 프로필을 추가했다. com.ridi.books 이름공간의 만화/PDF/EPUB 읽기 Activity를 정확히 지정하며, 실제 패키지 이름 com.initialcoms.ridi와 혼동하지 않는다. 서재·읽기 설정·웹툰·미검증 Bom 뷰어는 확장하지 않는다. 밀리와 게임 프로필은 유지한다.

런처의 리디 버튼은 열려 있던 만화에 복귀했다. 확장 요청 1회, ROTATION_0, 2160×2340 논리 크기, w822dp/h814dp 및 land 구성을 확인했다. 물리적으로 세로인 두 패널을 넓은 가로 화면으로 원본 리디가 처리한다.

실제 좌우 한 페이지씩 표시, 힌지 경계의 잘림 여부, 손가락 페이지 넘기기는 사용자 관찰 대기 중이다. 만화 시험 결과를 PDF/EPUB의 실제 출력 통과로 확대하지 않는다. 기존 게임 설정·밀리 설정·리디 책 데이터는 변경하지 않았다.

ContentProfileTest는 리디 세 가지 읽기 Activity, 설정/서재/웹툰 제외, 밀리와 Activity 분리, 리디 세로 자세 및 기존 네 게임의 회전 규칙을 검사한다. 기존 회귀 시험과 전체 빌드, APK v2/v3 서명 검증 및 V50S 설치가 통과했다.

HOME 후 restore_pending=false 및 본체 런처 전경을 확인했다. ReGene의 리디 버튼으로 같은 만화에 복귀하여 다시 2160×2340/land/ROTATION_0 및 확장 요청1회를 확인했다. 현재 책 화면에서 독서 확장과 auto_pose=true를 유지한다. 설치된 APK와 releases/ReGene.apk의 SHA256은 모두 1C00CBCC74BE00A236A5088592DE4F5B0A56CA385CBE42B16C649E1A830DA179이다.
