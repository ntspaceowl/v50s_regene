package dev.regene.v50s;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.provider.Settings;
import android.view.*;

/** A non-touchable one-pixel window keeps the body in reverse landscape. */
final class OrientationGuard {
    private final Context context;
    private View window;
    OrientationGuard(Context context){this.context=context;}
    boolean enable() {
        if(window!=null)return true;
        if(!Settings.canDrawOverlays(context))return false;
        WindowManager.LayoutParams p=new WindowManager.LayoutParams(1,1,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT);
        p.screenOrientation=ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        p.gravity=Gravity.TOP|Gravity.LEFT;
        View candidate=new View(context);
        context.getSystemService(WindowManager.class).addView(candidate,p);
        window=candidate;
        return true;
    }
    void disable(){if(window!=null){context.getSystemService(WindowManager.class).removeView(window);window=null;}}
}
