package dev.regene.v50s;

public final class ContentProfileTest {
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void main(String[] args){
        String millie="kr.co.millie.millieshelf";
        check(ContentProfile.content(millie,millie+".kotlin.ui.viewer.pdf.ui.PdfViewerActivity"),"PDF reader spans");
        check(ContentProfile.content(millie,millie+".kotlin.ui.viewer.millieviewer.MillieViewerActivity"),"ebook reader spans");
        check(!ContentProfile.content(millie,millie+".kotlin.ui.MainTabActivity"),"bookshelf must release");
        check(!ContentProfile.content(millie,millie+".kotlin.ui.viewer.webtoon.ui.WebtoonViewerActivity"),"webtoon is not a spread reader");
        check(!ContentProfile.content(millie,"other.PdfViewerActivity"),"exact reader class only");
        check(ContentProfile.poseMatches(millie,false,true),"book upright expands");
        check(!ContentProfile.poseMatches(millie,true,true),"book sideways releases");
        check(ContentProfile.rotation(millie)==0,"book keeps vertical pages");
        for(String pkg:new String[]{"org.citra.rgn","org.azahar_emu.azahar.regeneprobe","me.magnum.melonds.regeneprobe","com.dsemu.drastic"}){
            check(ContentProfile.rotation(pkg)==3,"game rotation unchanged");
            check(ContentProfile.poseMatches(pkg,true,true),"game landscape expands");
            check(!ContentProfile.poseMatches(pkg,false,true),"game portrait releases");
            check(ContentProfile.poseMatches(pkg,false,false),"manual game mode unchanged");
        }
        check(ContentProfile.content("com.dsemu.drastic","com.dsemu.drastic.ui.Customizer"),"DraStic editor unchanged");
        check(ContentProfile.content("org.citra.rgn","org.citra.emu.ui.EmulationActivity"),"Citra namespace preserved");
        System.out.println("Content profile book/game regression tests passed");
    }
}
