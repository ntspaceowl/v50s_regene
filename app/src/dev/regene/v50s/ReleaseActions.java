package dev.regene.v50s;

/** Try every release step even when one device API fails. */
final class ReleaseActions {
    interface Action { void run() throws Exception; }

    static void run(Action... actions) throws Exception {
        Exception failure = null;
        for (Action action : actions) {
            try { action.run(); }
            catch (Exception e) {
                if (failure == null) failure = e;
                else if (failure != e) failure.addSuppressed(e);
            }
        }
        if (failure != null) throw failure;
    }
}
