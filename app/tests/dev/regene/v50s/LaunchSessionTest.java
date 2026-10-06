package dev.regene.v50s;

public final class LaunchSessionTest {
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void main(String[] args){
        String book="com.initialcoms.ridi";
        LaunchSession s=new LaunchSession(book,1000,true);
        s.resumed(900,"com.lge.launcher3");s.resumed(1000,"dev.regene.v50s");
        check(!s.shouldStop(15000),"launcher and older events must not end cold launch");
        s.resumed(1100,book);s.resumed(1200,book);
        s.resumed(1300,"com.lge.secondlauncher");
        check(!s.shouldStop(20000),"reader, shelf, settings and cover launcher keep entered session");
        s.resumed(1400,"com.lge.launcher3");s.resumed(1500,book);
        check(s.shouldStop(1500),"HOME then rapid direct return in same poll must end");
        LaunchSession warm=new LaunchSession(book,2000,true);
        warm.resumed(1400,"com.lge.launcher3");warm.resumed(1500,book);
        warm.resumed(2100,book);warm.resumed(2050,"other.app");
        check(!warm.shouldStop(2200),"fresh ReGene launch ignores old overlap and late older events");
        warm.resumed(2300,"other.app");
        check(warm.shouldStop(2300),"switching apps ends book session");
        LaunchSession timeout=new LaunchSession(book,1000,true);
        check(timeout.shouldStop(16001),"failed launch must not leave reading permission armed");
        LaunchSession game=new LaunchSession("org.citra.rgn",1000,false);
        game.resumed(1100,"org.citra.rgn");game.resumed(1200,"com.lge.launcher3");
        check(!game.shouldStop(20000),"game HOME/resume behavior unchanged");
        System.out.println("Reading launch-session regression tests passed");
    }
}
