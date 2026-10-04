package dev.regene.v50s;

import android.app.*;
import android.app.usage.*;
import android.content.*;
import android.hardware.display.DisplayManager;
import android.hardware.*;
import android.os.*;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;

public final class ControllerService extends Service implements SensorEventListener {
    // Both services run in the default app process. Persisted preferences cannot
    // prove a session survived an APK update or process death.
    private static boolean running;
    static boolean isRunning() {return running;}
    private final Handler handler=new Handler();
    private String selected="", foreground="", activity="", lastActivity="";
    private final ForegroundHistory foregroundHistory=new ForegroundHistory();
    private long cursor, budgetStart, cooldown, lastSet, launchUntil, activitySince;
    private int attempts;
    private LgWide lg;
    private boolean ownsRotation;
    private int oldRotation,oldAuto;
    private OrientationGuard guard;
    private SensorManager sensors;
    private RotationPolicy pose;
    private boolean settledPose;
    private boolean hadWide;
    private long poseChangedAt;
    private android.content.SharedPreferences prefs;
    private String idleStatus;
    @Override public void onCreate() {
        super.onCreate(); prefs=getSharedPreferences("controller",0);
        guard=new OrientationGuard(this);
        try {lg=new LgWide(this);} catch(Exception e) {fail(e);stopSelf();return;}
        NotificationManager nm=getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel("session","듀얼스크린 제어",NotificationManager.IMPORTANCE_LOW));
        Intent stop=new Intent(this,ControllerService.class).setAction("STOP");
        PendingIntent stopIntent=PendingIntent.getService(this,1,stop,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        startForeground(1,new Notification.Builder(this,"session").setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("ReGene 자동 제어 중").setContentText("듀얼스크린 상단 · 본체 하단")
            .addAction(new Notification.Action.Builder(null,"중지",stopIntent).build()).build());
        if(prefs.getBoolean("restore_pending",false)) {
            oldRotation=prefs.getInt("old_rotation",0);oldAuto=prefs.getInt("old_auto",1);ownsRotation=true;
            try {releaseDisplay();} catch(Exception e) {fail(e);stopSelf();return;}
        }
        cursor=System.currentTimeMillis()-2000;
        Display main=getSystemService(DisplayManager.class).getDisplay(0);
        pose=new RotationPolicy(main!=null && (main.getRotation()==1 || main.getRotation()==3));
        settledPose=pose.landscape();
        sensors=getSystemService(SensorManager.class);
        Sensor gravity=sensors.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if(gravity==null)gravity=sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(gravity!=null)sensors.registerListener(this,gravity,SensorManager.SENSOR_DELAY_NORMAL);
        else status("자세 센서 없음: 자동 배치를 끄고 가로 배치를 사용할 수 있습니다.");
        status("LG 화면 제어 연결됨");
    }
    @Override public int onStartCommand(Intent intent,int flags,int id) {
        if(intent==null || "STOP".equals(intent.getAction())) {stopSelf();return START_NOT_STICKY;}
        selected=intent.getStringExtra("package"); launchUntil=System.currentTimeMillis()+15000;
        if(selected==null || lg==null) {stopSelf();return START_NOT_STICKY;}
        running=true;
        prefs.edit().putString("controller_selected",selected).putBoolean("controller_running",true).apply();
        // Warm launches also need a fresh history query: the game may already be resumed.
        cursor=System.currentTimeMillis()-60000;
        handler.removeCallbacks(tick);handler.post(tick);return START_NOT_STICKY;
    }
    private void status(String message) {prefs.edit().putString("status",message).apply();Log.i("ReGene",message);}
    private void fail(Exception e) {Throwable cause=e.getCause()==null ? e : e.getCause();status("제어 오류: "+cause);}
    private void saveRotation() {
        if(ownsRotation)return;
        oldRotation=Settings.System.getInt(getContentResolver(),Settings.System.USER_ROTATION,0);
        oldAuto=Settings.System.getInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION,1);
        if(!prefs.edit().putBoolean("restore_pending",true).putInt("old_rotation",oldRotation).putInt("old_auto",oldAuto).commit())
            throw new IllegalStateException("Cannot save rotation restore state");
        ownsRotation=true;
    }
    private void restoreRotation() throws Exception {
        if(!ownsRotation)return;
        ReleaseActions.run(
            ()->{if(!Settings.System.putInt(getContentResolver(),Settings.System.USER_ROTATION,oldRotation))
                throw new IllegalStateException("Cannot restore user rotation");},
            ()->{if(!Settings.System.putInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION,oldAuto))
                throw new IllegalStateException("Cannot restore automatic rotation");});
        ownsRotation=false;prefs.edit().putBoolean("restore_pending",false).apply();
    }
    private void releaseDisplay() throws Exception {
        ReleaseActions.run(
            ()->{if(lg!=null && lg.enabled())lg.set(false);},
            ()->{if(guard!=null)guard.disable();},
            ()->restoreRotation());
    }
    private void restoreBodyTaskFocus() {
        // Azahar keeps its running game in the launcher task. Other emulators
        // may use separate tasks, so reopening their launcher can leave the game.
        if(!"org.azahar_emu.azahar.regeneprobe".equals(selected))return;
        Intent launch=getPackageManager().getLaunchIntentForPackage(selected);
        if(launch==null)return;
        ActivityOptions options=ActivityOptions.makeBasic();
        options.setLaunchDisplayId(Display.DEFAULT_DISPLAY);
        // Reorder the existing launcher task, preserving its running game.
        // LG can leave global key focus on its cover launcher after wake.
        startActivity(launch,options.toBundle());
        Log.i("ReGene","Restored body task focus: "+selected);
    }
    @Override public void onSensorChanged(SensorEvent event) {
        boolean before=pose.landscape();
        boolean after=pose.sample(event.values[0],event.values[1]);
        if(before!=after) {poseChangedAt=SystemClock.elapsedRealtime();Log.i("ReGene","Pose candidate="+after+" gravity="+event.values[0]+","+event.values[1]);}
    }
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy) {}
    private final Runnable tick=new Runnable(){public void run(){
        try {
            long now=System.currentTimeMillis();
            // UsageStats can publish a resume after the poll containing its timestamp.
            UsageEvents events=getSystemService(UsageStatsManager.class).queryEvents(Math.max(0,cursor-10000),now);
            UsageEvents.Event event=new UsageEvents.Event();
            while(events.hasNextEvent()) {events.getNextEvent(event);
                if(event.getEventType()==UsageEvents.Event.ACTIVITY_RESUMED)
                    foregroundHistory.resumed(event.getTimeStamp(),event.getPackageName(),event.getClassName());
            }
            foreground=foregroundHistory.packageName();activity=foregroundHistory.className();
            cursor=now;
            String screen=foreground+"/"+activity;
            if(!screen.equals(lastActivity)) {
                lastActivity=screen;activitySince=now;attempts=0;budgetStart=now;cooldown=0;
                prefs.edit().putString("foreground_screen",screen).apply();
                Log.i("ReGene","Foreground="+screen);
            }
            boolean cover=false;
            // Logical IDs may change after attachment; only the LG physical cover qualifies.
            for(Display d:getSystemService(DisplayManager.class).getDisplays())
                if("Built-in Cover-Screen".equals(d.getName()) && d.getState()==Display.STATE_ON)cover=true;
            if(SystemClock.elapsedRealtime()-poseChangedAt>600)settledPose=pose.landscape();
            boolean landscape=!prefs.getBoolean("auto_pose",true) || settledPose;
            boolean game=activity.endsWith(".EmulationActivity") || activity.endsWith(".EmulatorActivity") || activity.endsWith(".DraSticEmuActivity");
            boolean layoutEditor=(("me.magnum.melonds".equals(foreground) || "me.magnum.melonds.regeneprobe".equals(foreground))
                && activity.endsWith(".LayoutEditorActivity"))
                || ("com.dsemu.drastic".equals(foreground) && "com.dsemu.drastic.ui.Customizer".equals(activity));
            boolean active=selected.equals(foreground) && (game || layoutEditor) && now-activitySince>=2500 && cover && landscape && getSystemService(PowerManager.class).isInteractive();
            if(active) {
                saveRotation();
                guard.enable();
                if(Settings.System.getInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION,1)!=0)
                    Settings.System.putInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION,0);
                if(Settings.System.getInt(getContentResolver(),Settings.System.USER_ROTATION,0)!=3)
                    Settings.System.putInt(getContentResolver(),Settings.System.USER_ROTATION,3);
                boolean wide=lg.enabled();
                if(wide && !hadWide)restoreBodyTaskFocus();
                hadWide=wide;
                if(wide && idleStatus!=null)status("두 화면 배치 유지 · "+selected);
                idleStatus=null;
                if(!wide && now>=cooldown && now-lastSet>1000) {
                    if(now-budgetStart>15000){attempts=0;budgetStart=now;}
                    if(attempts>=6){cooldown=now+60000;status("화면 확장이 반복 해제되어 60초 대기합니다.");}
                    else {lg.set(true);lastSet=now;attempts++;status("상단/하단 확장 요청 "+attempts+" · "+selected);}
                }
            } else if(now>launchUntil || !foreground.equals(getPackageName())) {
                hadWide=false;
                releaseDisplay();
                String reason;
                if(!selected.equals(foreground))reason="다른 앱 사용 중: 화면 배치 대기";
                else if(!(game || layoutEditor))reason="게임 또는 레이아웃 편집 화면 대기";
                else if(!getSystemService(PowerManager.class).isInteractive())reason="화면 꺼짐: 화면 배치 대기";
                else if(!cover)reason="듀얼스크린이 켜지면 화면 배치를 재개합니다.";
                else if(!landscape)reason="가로 자세 감지 대기: 폰을 들어 가로로 돌려주세요.";
                else reason="게임 화면 준비 중";
                if(!reason.equals(idleStatus)){idleStatus=reason;status(reason);}
            }
        } catch(Exception e) {fail(e);handler.removeCallbacks(this);stopSelf();return;}
        handler.postDelayed(this,650);
    }};
    @Override public void onDestroy(){
        running=false;
        prefs.edit().putBoolean("controller_running",false).apply();handler.removeCallbacks(tick);
        try {
            ReleaseActions.run(
                ()->{if(sensors!=null)sensors.unregisterListener(this);},
                ()->releaseDisplay());
            status("자동 제어 중지됨");
        } catch(Exception e) {fail(e);}
        finally {super.onDestroy();}
    }
    @Override public IBinder onBind(Intent intent){return null;}
}
