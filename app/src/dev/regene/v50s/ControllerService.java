package dev.regene.v50s;

import android.app.*;
import android.app.usage.*;
import android.content.*;
import android.hardware.display.DisplayManager;
import android.os.*;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;

public final class ControllerService extends Service {
    private final Handler handler=new Handler();
    private String selected="", foreground="", activity="", lastActivity="";
    private long cursor, budgetStart, cooldown, lastSet, launchUntil;
    private int attempts;
    private LgWide lg;
    private boolean ownsRotation;
    private int oldRotation,oldAuto;
    private android.content.SharedPreferences prefs;
    @Override public void onCreate() {
        super.onCreate(); prefs=getSharedPreferences("controller",0);
        NotificationManager nm=getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel("session","듀얼스크린 제어",NotificationManager.IMPORTANCE_LOW));
        Intent stop=new Intent(this,ControllerService.class).setAction("STOP");
        PendingIntent stopIntent=PendingIntent.getService(this,1,stop,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        startForeground(1,new Notification.Builder(this,"session").setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("ReGene 자동 제어 중").setContentText("듀얼스크린 상단 · 본체 하단")
            .addAction(new Notification.Action.Builder(null,"중지",stopIntent).build()).build());
        if(prefs.getBoolean("restore_pending",false)) {
            oldRotation=prefs.getInt("old_rotation",0);oldAuto=prefs.getInt("old_auto",1);ownsRotation=true;
            restoreRotation();
        }
        cursor=System.currentTimeMillis()-2000;
        try {lg=new LgWide(this);status("LG 화면 제어 연결됨");} catch(Exception e) {fail(e);stopSelf();}
    }
    @Override public int onStartCommand(Intent intent,int flags,int id) {
        if(intent==null || "STOP".equals(intent.getAction())) {stopSelf();return START_NOT_STICKY;}
        selected=intent.getStringExtra("package"); launchUntil=System.currentTimeMillis()+15000;
        if(selected==null || lg==null) {stopSelf();return START_NOT_STICKY;}
        handler.removeCallbacks(tick);handler.post(tick);return START_NOT_STICKY;
    }
    private void status(String message) {prefs.edit().putString("status",message).apply();Log.i("ReGene",message);}
    private void fail(Exception e) {Throwable cause=e.getCause()==null ? e : e.getCause();status("제어 오류: "+cause);}
    private void saveRotation() {
        if(ownsRotation)return;
        oldRotation=Settings.System.getInt(getContentResolver(),Settings.System.USER_ROTATION,0);
        oldAuto=Settings.System.getInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION,1);
        prefs.edit().putBoolean("restore_pending",true).putInt("old_rotation",oldRotation).putInt("old_auto",oldAuto).commit();
        ownsRotation=true;
    }
    private void restoreRotation() {
        if(!ownsRotation)return;
        Settings.System.putInt(getContentResolver(),Settings.System.USER_ROTATION,oldRotation);
        Settings.System.putInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION,oldAuto);
        ownsRotation=false;prefs.edit().putBoolean("restore_pending",false).apply();
    }
    private final Runnable tick=new Runnable(){public void run(){
        try {
            long now=System.currentTimeMillis();
            UsageEvents events=getSystemService(UsageStatsManager.class).queryEvents(cursor,now);
            UsageEvents.Event event=new UsageEvents.Event();
            while(events.hasNextEvent()) {events.getNextEvent(event);
                if(event.getEventType()==UsageEvents.Event.ACTIVITY_RESUMED && !"com.lge.secondlauncher".equals(event.getPackageName())) {
                    foreground=event.getPackageName();activity=event.getClassName();
                }
            }
            cursor=now;
            boolean cover=false;
            for(Display d:getSystemService(DisplayManager.class).getDisplays()) if(d.getDisplayId()==1 && d.getState()==Display.STATE_ON)cover=true;
            boolean active=selected.equals(foreground) && cover && getSystemService(PowerManager.class).isInteractive();
            if(active) {
                saveRotation();
                Settings.System.putInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION,0);
                Settings.System.putInt(getContentResolver(),Settings.System.USER_ROTATION,3);
                if(!activity.equals(lastActivity)) {lastActivity=activity; attempts=0; budgetStart=now;cooldown=0;status("실행 감지: "+selected+" / "+activity);}
                if(!lg.enabled() && now>=cooldown && now-lastSet>1000) {
                    if(now-budgetStart>15000){attempts=0;budgetStart=now;}
                    if(attempts>=6){cooldown=now+60000;status("화면 확장이 반복 해제되어 60초 대기합니다.");}
                    else {lg.set(true);lastSet=now;attempts++;status("상단/하단 확장 요청 "+attempts+" · "+selected);}
                }
            } else if(now>launchUntil || !foreground.equals(getPackageName())) {
                if(lg.enabled() && ownsRotation)lg.set(false);
                if(ownsRotation){restoreRotation();status("대상 앱 밖: 원래 화면 설정 복원");}
            }
        } catch(Exception e) {fail(e);handler.removeCallbacks(this);stopSelf();return;}
        handler.postDelayed(this,650);
    }};
    @Override public void onDestroy(){handler.removeCallbacks(tick);try{if(lg!=null && ownsRotation)lg.set(false);restoreRotation();}catch(Exception e){fail(e);}status("자동 제어 중지됨");super.onDestroy();}
    @Override public IBinder onBind(Intent intent){return null;}
}
