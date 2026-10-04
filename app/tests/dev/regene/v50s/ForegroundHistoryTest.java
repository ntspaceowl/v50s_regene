package dev.regene.v50s;

public final class ForegroundHistoryTest {
    static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        ForegroundHistory history = new ForegroundHistory();
        history.resumed(1000, "dev.regene.v50s", "MainActivity");
        // Resume arrives in the next query, even though its timestamp precedes that query.
        history.resumed(1400, "org.azahar_emu.azahar.regeneprobe", "EmulationActivity");
        check(history.packageName().endsWith("regeneprobe"), "late game resume must be accepted");
        history.resumed(1000, "dev.regene.v50s", "MainActivity");
        check(history.packageName().endsWith("regeneprobe"), "overlap must not rewind to launcher");
        history.resumed(1500, "com.lge.secondlauncher", "AllAppsLauncherExtension");
        check(history.className().equals("EmulationActivity"), "cover launcher must not replace body");
        history.resumed(1600, "org.azahar_emu.azahar.regeneprobe", "SettingsActivity");
        history.resumed(1400, "org.azahar_emu.azahar.regeneprobe", "EmulationActivity");
        check(history.className().equals("SettingsActivity"), "late older game must not expand settings");
        history.resumed(1700, "com.lge.launcher3", "LauncherExtension");
        check(history.packageName().equals("com.lge.launcher3"), "body HOME must release expansion");
        history.resumed(1800, null, "Unknown");
        history.resumed(1800, "other", null);
        check(history.packageName().equals("com.lge.launcher3"), "incomplete events must be ignored");
        System.out.println("ForegroundHistory tests passed");
    }
}
