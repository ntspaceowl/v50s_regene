package dev.regene.v50s;

public final class ReleaseActionsTest {
    public static void main(String[] args) throws Exception {
        StringBuilder calls = new StringBuilder();
        Exception wide = new Exception("LG API failed");
        Exception overlay = new Exception("Overlay API failed");
        try {
            ReleaseActions.run(
                ()->{calls.append('W'); throw wide;},
                ()->{calls.append('O'); throw overlay;},
                ()->calls.append('R'));
            throw new AssertionError("A release failure must be reported");
        } catch (Exception e) {
            if(e!=wide || e.getSuppressed().length!=1 || e.getSuppressed()[0]!=overlay)
                throw new AssertionError("Preserve both release failures");
        }
        if(!"WOR".contentEquals(calls))throw new AssertionError("Rotation restoration skipped");
        calls.setLength(0);
        ReleaseActions.run(()->calls.append('W'), ()->calls.append('O'), ()->calls.append('R'));
        if(!"WOR".contentEquals(calls))throw new AssertionError("Normal release order changed");
        System.out.println("ReleaseActions fault isolation tests passed");
    }
}
