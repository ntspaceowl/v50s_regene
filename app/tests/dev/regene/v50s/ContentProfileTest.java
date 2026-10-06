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
        String ridi="com.initialcoms.ridi";
        check(ContentProfile.content(ridi,"com.ridi.books.viewer.reader.pagebased.comic.ComicBookReaderActivity"),"Ridi comic reader spans");
        check(ContentProfile.content(ridi,"com.ridi.books.viewer.reader.pagebased.pdf.PDFReaderActivity"),"Ridi PDF reader spans");
        check(ContentProfile.content(ridi,"com.ridi.books.viewer.reader.epub.EPubReaderActivity"),"Ridi EPUB reader spans");
        check(!ContentProfile.content(ridi,"com.ridi.books.viewer.reader.activity.ReaderSettingsActivity"),"Ridi settings must release");
        check(!ContentProfile.content(ridi,"com.ridi.books.viewer.reader.pagebased.comic.webtoon.WebtoonReaderActivity"),"Ridi webtoon excluded");
        check(!ContentProfile.content(ridi,"com.ridi.books.viewer.reader.bom.BomReaderActivity"),"unverified Ridi viewer excluded");
        check(!ContentProfile.content(ridi,"com.ridi.books.MainActivity"),"Ridi shelf must release");
        check(!ContentProfile.content(millie,"com.ridi.books.viewer.reader.pagebased.comic.ComicBookReaderActivity"),"reader profiles isolated");
        check(ContentProfile.rotation(ridi)==0 && ContentProfile.poseMatches(ridi,false,true),"Ridi upright mode");
        check(!ContentProfile.poseMatches(ridi,true,true),"Ridi sideways releases");
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
