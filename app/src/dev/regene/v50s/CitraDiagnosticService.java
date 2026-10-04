package dev.regene.v50s;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.SystemClock;
import android.content.SharedPreferences;
import android.view.InputDevice;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;
import java.util.ArrayList;

/** Changes the existing Citra checkbox only during a selected ReGene game session. */
public final class CitraDiagnosticService extends AccessibilityService {
    private final Handler handler = new Handler();
    private String last = "";
    private int stage;
    private boolean target;
    private boolean closeOnly;
    private Boolean lastDesired, satisfied;
    private long deadline, nextAction, retryAfter;
    private final Runnable poll = new Runnable() {
        public void run() { inspect(); handler.postDelayed(this, 1000); }
    };
    @Override protected void onServiceConnected() {
        report("Citra 버튼 제어 연결됨");
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
            automate(root);
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
    private boolean controllerConnected() {
        for (int id : InputDevice.getDeviceIds()) {
            InputDevice device = InputDevice.getDevice(id);
            if (device != null && device.isEnabled() && device.isExternal() && !device.isVirtual()
                && (device.supportsSource(InputDevice.SOURCE_GAMEPAD)
                || device.supportsSource(InputDevice.SOURCE_JOYSTICK))) return true;
        }
        return false;
    }
    private AccessibilityNodeInfo exactLabel(AccessibilityNodeInfo root, String text) {
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(text);
        AccessibilityNodeInfo result = null;
        for (AccessibilityNodeInfo node : nodes) {
            if (result == null && node.isVisibleToUser() && text.contentEquals(node.getText() == null ? "" : node.getText()))
                result = AccessibilityNodeInfo.obtain(node);
            node.recycle();
        }
        return result;
    }
    private AccessibilityNodeInfo checkbox(AccessibilityNodeInfo label) {
        AccessibilityNodeInfo row = AccessibilityNodeInfo.obtain(label);
        try {
            for (int depth = 0; row != null && depth < 4; depth++) {
                AccessibilityNodeInfo result = singleCheckbox(row, 0);
                if (result != null) return result;
                AccessibilityNodeInfo parent = row.getParent(); row.recycle(); row = parent;
            }
            return null;
        } finally { if (row != null) row.recycle(); }
    }
    private boolean click(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo candidate = AccessibilityNodeInfo.obtain(node);
        try {
            for (int depth = 0; candidate != null && depth < 4; depth++) {
                if (candidate.isVisibleToUser() && candidate.isEnabled() && candidate.isClickable())
                    return candidate.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                AccessibilityNodeInfo parent = candidate.getParent(); candidate.recycle(); candidate = parent;
            }
            return false;
        } finally { if (candidate != null) candidate.recycle(); }
    }
    private void failed(String message) {
        stage = 0; retryAfter = SystemClock.elapsedRealtime() + 60000;
        report(message + " · 60초 후 재시도");
    }
    private void automate(AccessibilityNodeInfo root) {
        SharedPreferences prefs = getSharedPreferences("controller", 0);
        long now = SystemClock.elapsedRealtime();
        if (now < nextAction) return;
        if (!prefs.getBoolean("controller_running",false)
            || !"org.citra.emu".equals(prefs.getString("controller_selected",""))
            || !prefs.getString("foreground_screen","").equals("org.citra.emu/org.citra.emu.ui.EmulationActivity")) {
            stage = 0; return;
        }
        boolean desired = prefs.getBoolean("citra_test_mode",false) || controllerConnected();
        boolean owned = prefs.getBoolean("citra_hide_owned",false);
        if(lastDesired==null || lastDesired!=desired) { lastDesired=desired; satisfied=null; retryAfter=0; }
        if (stage > 0 && stage < 4 && desired != target) {
            target=desired;
            if(!desired && !owned) {
                // Nothing was changed: close our menu without changing a manual hidden state.
                if(!performGlobalAction(GLOBAL_ACTION_BACK)) { failed("Citra 설정 닫기 실패"); return; }
                closeOnly=true; stage=4; nextAction=now+700; return;
            }
            if(stage==3)stage=2;
        }
        if (stage != 0 && now > deadline) { failed("Citra 메뉴 제어 시간 초과"); return; }
        if (stage == 0) {
            if (now < retryAfter || (satisfied!=null && satisfied==desired) || (!desired && !owned)) return;
            // Never close an existing settings/editor/dialog merely to start automation.
            AccessibilityNodeInfo setting = exactLabel(root,"Settings");
            AccessibilityNodeInfo hide = exactLabel(root,"Hide Input Buttons");
            AccessibilityNodeInfo done = exactLabel(root,"DONE");
            boolean busy = setting != null || hide != null || done != null;
            if(setting!=null)setting.recycle(); if(hide!=null)hide.recycle(); if(done!=null)done.recycle();
            if(busy)return;
            target=desired; closeOnly=false; deadline=now+12000;
            if (!performGlobalAction(GLOBAL_ACTION_BACK)) { failed("Citra 메뉴 열기 실패"); return; }
            stage=1; nextAction=now+500; return;
        }
        if (stage == 1) {
            AccessibilityNodeInfo setting = exactLabel(root,"Settings");
            if(setting==null)return;
            try { if(!click(setting)) { failed("Citra Settings 선택 실패"); return; } }
            finally { setting.recycle(); }
            stage=2; nextAction=now+500; return;
        }
        if (stage == 4) {
            AccessibilityNodeInfo hide=exactLabel(root,"Hide Input Buttons");
            AccessibilityNodeInfo setting=exactLabel(root,"Settings");
            boolean stillOpen=hide!=null || setting!=null;
            if(hide!=null)hide.recycle(); if(setting!=null)setting.recycle();
            if(stillOpen)return;
            if(!target)prefs.edit().putBoolean("citra_hide_owned",false).apply();
            stage=0; satisfied=target; retryAfter=0; nextAction=now+1000;
            report(closeOnly ? "Citra 연결 상태 변경 · 메뉴 닫힘 확인"
                : target ? "Citra 설정 닫힘 · 가상패드 숨김 확인" : "Citra 설정 닫힘 · 기존 표시 복원 확인");
            return;
        }
        AccessibilityNodeInfo label=exactLabel(root,"Hide Input Buttons");
        if(label==null)return;
        AccessibilityNodeInfo check=checkbox(label);
        try {
            if(check==null) { failed("Citra 체크 상태 확인 실패"); return; }
            boolean checked=check.isChecked();
            if (stage==2 && checked!=target) {
                if(target && !owned) {
                    // Journal before a click so an interrupted change can be restored later.
                    if(!prefs.edit().putBoolean("citra_hide_owned",true).commit()) {
                        failed("Citra 복원 기록 저장 실패"); return;
                    }
                }
                if(!click(check)) { failed("Citra 숨김 설정 선택 실패"); return; }
                stage=3; nextAction=now+500; return;
            }
            if(checked!=target)return;
            if(!performGlobalAction(GLOBAL_ACTION_BACK)) { failed("Citra 설정 닫기 실패"); return; }
            stage=4; nextAction=now+700;
            report("Citra 체크 상태 확인 · 설정 닫힘 대기");
        } finally { if(check!=null)check.recycle(); label.recycle(); }
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
