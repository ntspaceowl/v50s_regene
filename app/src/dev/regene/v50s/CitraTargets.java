package dev.regene.v50s;

/** Explicit targets and separate restore journals for original and isolated Citra. */
final class CitraTargets {
    static boolean supports(String packageName) {
        return "org.citra.emu".equals(packageName) || "org.citra.rgn".equals(packageName);
    }
    static boolean game(String packageName, String screen) {
        return supports(packageName) && (packageName + "/org.citra.emu.ui.EmulationActivity").equals(screen);
    }
    static String ownershipKey(String packageName) {
        if (!supports(packageName)) throw new IllegalArgumentException("Unsupported Citra target");
        return "org.citra.emu".equals(packageName) ? "citra_hide_owned" : "citra_probe_hide_owned";
    }
}
