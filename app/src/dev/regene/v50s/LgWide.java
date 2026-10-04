package dev.regene.v50s;

import android.content.Context;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.view.Display;
import java.lang.reflect.Method;

final class LgWide {
    private final Object manager;
    private final Method read, write;
    private final DisplayManager displays;
    LgWide(Context context) throws Exception {
        manager = context.getSystemService(Context.ACTIVITY_SERVICE);
        displays = context.getSystemService(DisplayManager.class);
        read = manager.getClass().getMethod("getWideScreenMode");
        write = manager.getClass().getMethod("setWideScreenMode", boolean.class);
    }
    boolean enabled() throws Exception {
        if ((Boolean) read.invoke(manager)) return true;
        Display main = displays.getDisplay(Display.DEFAULT_DISPLAY);
        if (main == null) return false;
        Point size = new Point(); main.getRealSize(size);
        Display.Mode mode = main.getMode();
        // LG can report false before its expanded logical display has contracted.
        return Math.min(size.x,size.y) > Math.min(mode.getPhysicalWidth(),mode.getPhysicalHeight());
    }
    void set(boolean enabled) throws Exception { write.invoke(manager, enabled); }
}
