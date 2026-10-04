package dev.regene.v50s;

import android.view.InputDevice;
import java.util.ArrayList;
import java.util.List;

/** Live Android input devices, excluding virtual devices and the LG cover touchscreen. */
final class GameControllers {
    static List<String> names() {
        List<String> names = new ArrayList<>();
        for (int id : InputDevice.getDeviceIds()) {
            InputDevice device = InputDevice.getDevice(id);
            if (device != null && device.isEnabled() && device.isExternal() && !device.isVirtual()
                && (device.supportsSource(InputDevice.SOURCE_GAMEPAD)
                || device.supportsSource(InputDevice.SOURCE_JOYSTICK))) names.add(device.getName());
        }
        return names;
    }

    static boolean connected() { return !names().isEmpty(); }

    static String status() {
        List<String> names = names();
        return names.isEmpty() ? "게임패드: 연결되지 않음"
            : "게임패드: " + String.join(", ", names) + " · " + names.size() + "대 인식";
    }
}
