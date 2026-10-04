package dev.regene.v50s;

public final class CitraTargetsTest {
    static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        check(CitraTargets.game("org.citra.rgn", "org.citra.rgn/org.citra.emu.ui.EmulationActivity"), "probe keeps original JNI class namespace");
        check(!CitraTargets.game("org.citra.emu", "org.citra.rgn/org.citra.emu.ui.EmulationActivity"), "never automate a different package's game");
        check(!CitraTargets.game("org.citra.rgn", "org.citra.rgn/org.citra.emu.ui.MainActivity"), "launcher is not a game");
        check(!CitraTargets.supports("org.citra.emu.other"), "exact allowlist only");
        check(CitraTargets.ownershipKey("org.citra.emu").equals("citra_hide_owned"), "preserve existing original journal");
        check(!CitraTargets.ownershipKey("org.citra.rgn").equals(CitraTargets.ownershipKey("org.citra.emu")), "probe cannot consume original restore journal");
        System.out.println("Citra target isolation tests passed");
    }
}
