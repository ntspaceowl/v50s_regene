package dev.regene.v50s;

import android.content.Context;
import java.lang.reflect.Method;

final class LgWide {
    private final Object manager;
    private final Method read, write;
    LgWide(Context context) throws Exception {
        manager = context.getSystemService(Context.ACTIVITY_SERVICE);
        read = manager.getClass().getMethod("getWideScreenMode");
        write = manager.getClass().getMethod("setWideScreenMode", boolean.class);
    }
    boolean enabled() throws Exception { return (Boolean) read.invoke(manager); }
    void set(boolean enabled) throws Exception { write.invoke(manager, enabled); }
}
