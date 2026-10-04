package dev.regene.v50s;

/** Accept overlapping UsageEvents without replaying an older app over a newer one. */
final class ForegroundHistory {
    private long newest = Long.MIN_VALUE;
    private String packageName = "", className = "";
    void resumed(long timestamp, String pkg, String activity) {
        if (pkg == null || activity == null || "com.lge.secondlauncher".equals(pkg)
                || timestamp < newest) return;
        newest = timestamp;
        packageName = pkg;
        className = activity;
    }
    String packageName() { return packageName; }
    String className() { return className; }
}
