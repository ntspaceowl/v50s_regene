package dev.regene.v50s;

/** Each app decides when and in which physical pose its content may span panels. */
final class ContentProfile {
    static boolean book(String pkg) {return "kr.co.millie.millieshelf".equals(pkg);}
    static boolean content(String pkg, String activity) {
        if(book(pkg))return activity.equals(pkg+".kotlin.ui.viewer.pdf.ui.PdfViewerActivity")
            || activity.equals(pkg+".kotlin.ui.viewer.millieviewer.MillieViewerActivity");
        return activity.endsWith(".EmulationActivity") || activity.endsWith(".EmulatorActivity")
            || activity.endsWith(".DraSticEmuActivity")
            || (("me.magnum.melonds".equals(pkg) || "me.magnum.melonds.regeneprobe".equals(pkg))
                && activity.endsWith(".LayoutEditorActivity"))
            || ("com.dsemu.drastic".equals(pkg) && "com.dsemu.drastic.ui.Customizer".equals(activity));
    }
    static boolean poseMatches(String pkg, boolean landscape, boolean automatic) {
        return !automatic || (book(pkg) ? !landscape : landscape);
    }
    static int rotation(String pkg) {return book(pkg) ? 0 : 3;}
}
