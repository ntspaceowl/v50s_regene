package dev.regene.v50s;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;
import java.util.ArrayList;

/** Read-only proof of the existing Citra setting before implementing UI automation. */
public final class CitraDiagnosticService extends AccessibilityService {
    private final Handler handler = new Handler();
    private String last = "";
    private final Runnable poll = new Runnable() {
        public void run() { inspect(); handler.postDelayed(this, 1000); }
    };
    @Override protected void onServiceConnected() {
        report("진단 연결됨 · Citra 실행 중 Settings를 열어 주세요.");
        handler.removeCallbacks(poll); handler.post(poll);
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if ("org.citra.emu".contentEquals(event.getPackageName() == null ? "" : event.getPackageName())) inspect();
    }
    private void report(String message) {
        if (message.equals(last)) return;
        last = message;
        getSharedPreferences("controller", 0).edit()
            .putString("citra_diagnostic", message)
            .putLong("citra_diagnostic_time", System.currentTimeMillis()).apply();
        Log.i("ReGeneCitra", message);
    }
    private void inspect() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        try {
            if (!"org.citra.emu".contentEquals(root.getPackageName() == null ? "" : root.getPackageName())) return;
            List<AccessibilityNodeInfo> labels = root.findAccessibilityNodeInfosByText("Hide Input Buttons");
            try {
                for (AccessibilityNodeInfo label : labels) {
                    if (!"Hide Input Buttons".contentEquals(label.getText() == null ? "" : label.getText())) continue;
                    AccessibilityNodeInfo row = AccessibilityNodeInfo.obtain(label);
                    try {
                        for (int depth = 0; row != null && depth < 4; depth++) {
                            AccessibilityNodeInfo checkbox = singleCheckbox(row, 0);
                            if (checkbox != null) {
                                try { report("Hide Input Buttons: " + (checkbox.isChecked() ? "ON" : "OFF")
                                    + " · 체크 상태 확인"); }
                                finally { checkbox.recycle(); }
                                return;
                            }
                            AccessibilityNodeInfo parent = row.getParent();
                            row.recycle(); row = parent;
                        }
                    } finally { if (row != null) row.recycle(); }
                    report("Hide Input Buttons 항목 발견 · 체크 상태는 확인되지 않음");
                    return;
                }
            } finally { for (AccessibilityNodeInfo label : labels) label.recycle(); }
        } finally { root.recycle(); }
    }
    private AccessibilityNodeInfo singleCheckbox(AccessibilityNodeInfo node, int depth) {
        List<AccessibilityNodeInfo> found = new ArrayList<>();
        collectCheckboxes(node, depth, found);
        if (found.size() == 1) return found.get(0);
        for (AccessibilityNodeInfo item : found) item.recycle();
        return null;
    }
    private void collectCheckboxes(AccessibilityNodeInfo node, int depth, List<AccessibilityNodeInfo> found) {
        if (node.isCheckable()) { found.add(AccessibilityNodeInfo.obtain(node)); return; }
        if (depth >= 3) return;
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) continue;
            try { collectCheckboxes(child, depth + 1, found); }
            finally { child.recycle(); }
        }
    }
    @Override public void onInterrupt() { report("진단 중단됨"); }
    @Override public void onDestroy() {
        handler.removeCallbacks(poll); report("진단 연결 해제됨"); super.onDestroy();
    }
}
