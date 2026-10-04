package dev.regene.v50s;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.*;

public final class MainActivity extends Activity {
    private TextView state;
    private final Handler handler = new Handler();
    private final Runnable refresh = new Runnable() { public void run() {
        state.setText("앱 사용 정보: " + (usageAllowed() ? "허용" : "설정 필요")
            + " · 시스템 설정: " + (Settings.System.canWrite(MainActivity.this) ? "허용" : "설정 필요")
            + " · 가로 방향 유지: " + (Settings.canDrawOverlays(MainActivity.this) ? "허용" : "설정 필요")
            + "\n" + getSharedPreferences("controller",0).getString("status","에뮬레이터를 선택하세요.")
            + "\n" + GameControllers.status()
            );
        handler.postDelayed(this,1000);
    }};
    boolean usageAllowed() {
        return ((AppOpsManager)getSystemService(APP_OPS_SERVICE)).checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName()) == AppOpsManager.MODE_ALLOWED;
    }
    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        LinearLayout root = new LinearLayout(this); root.setOrientation(1); root.setPadding(28,36,28,28);
        TextView title = new TextView(this); title.setText("V50S ReGene"); title.setTextSize(28); root.addView(title);
        TextView intro = new TextView(this); intro.setText("듀얼스크린 = 상단\n본체 = 하단 터치 화면과 게임 버튼\n\n실행할 에뮬레이터를 선택하세요."); intro.setTextSize(18); root.addView(intro);
        state = new TextView(this); root.addView(state);
        Switch rotation = new Switch(this); rotation.setText("기기를 돌리면 자동 배치");
        rotation.setChecked(getSharedPreferences("controller",0).getBoolean("auto_pose",true));
        rotation.setOnCheckedChangeListener((view,checked)->getSharedPreferences("controller",0).edit().putBoolean("auto_pose",checked).apply());
        root.addView(rotation);
        TextView rotationHint=new TextView(this);rotationHint.setText("끄면 가로 배치를 유지합니다.");root.addView(rotationHint);
        button(root,"앱 사용 정보 허용",()->startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS,Uri.parse("package:"+getPackageName()))));
        button(root,"시스템 설정 변경 허용",()->startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,Uri.parse("package:"+getPackageName()))));
        button(root,"가로 방향 유지 허용",()->startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName()))));
        TextView guardHint=new TextView(this);guardHint.setText("DraStic 등의 방향 요청을 맞추려면 다른 앱 위 표시 권한이 필요합니다. 게임 터치는 가리지 않습니다.");root.addView(guardHint);
        String[][] apps = {{"Citra","org.citra.rgn"},{"Azahar","org.azahar_emu.azahar.regeneprobe"},{"melonDS","me.magnum.melonds.regeneprobe"},{"DraStic","com.dsemu.drastic"}};
        for (String[] app:apps) {
            Intent launch = getPackageManager().getLaunchIntentForPackage(app[1]);
            Button b = button(root, app[0]+(launch==null ? " · 미설치" : " 실행"),()->{
                if (!usageAllowed() || !Settings.System.canWrite(this) || !Settings.canDrawOverlays(this)) {
                    Toast.makeText(this,"앱 사용 정보, 시스템 설정 변경, 가로 방향 유지 권한을 먼저 허용하세요.",Toast.LENGTH_LONG).show(); return;
                }
                startForegroundService(new Intent(this,ControllerService.class).putExtra("package",app[1]));
                startActivity(launch);
            }); b.setEnabled(launch!=null);
        }
        button(root,"자동 제어 중지",()->stopService(new Intent(this,ControllerService.class)));
        button(root,"Citra 패드 자동 숨김 권한",()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        TextView note = new TextView(this); note.setText("가로로 돌리면 커버에는 상단 화면, 본체에는 하단 화면이 표시됩니다. 패드를 연결하면 가상패드가 자동으로 숨겨집니다."); root.addView(note);
        ScrollView scroll = new ScrollView(this); scroll.addView(root); setContentView(scroll);
    }
    private Button button(LinearLayout root,String text,Runnable action) { Button b=new Button(this); b.setText(text); b.setOnClickListener(v->action.run()); root.addView(b); return b; }
    @Override public void onResume() {super.onResume();handler.post(refresh);}
    @Override public void onPause() {handler.removeCallbacks(refresh);super.onPause();}
}
