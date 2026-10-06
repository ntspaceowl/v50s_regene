package dev.regene.v50s;

/** A reading launch grants expansion only until the user leaves that app. */
final class LaunchSession {
    private final String selected;
    private final long startedAt;
    private final boolean bounded;
    private long newest;
    private boolean entered, ended;

    LaunchSession(String selected,long startedAt,boolean bounded) {
        this.selected=selected;this.startedAt=startedAt;this.bounded=bounded;
        newest=startedAt;
    }
    void resumed(long timestamp,String pkg) {
        if(!bounded || pkg==null || pkg.isEmpty() || "com.lge.secondlauncher".equals(pkg)
            || timestamp<newest)return;
        newest=timestamp;
        if(selected.equals(pkg))entered=true;
        else if(entered)ended=true;
    }
    boolean shouldStop(long now) {
        return bounded && (ended || (!entered && now-startedAt>15000));
    }
}
