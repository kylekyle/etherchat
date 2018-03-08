package edu.usma.etherchat;

import com.github.sarxos.winreg.HKey;
import com.github.sarxos.winreg.RegistryException;
import com.github.sarxos.winreg.WindowsRegistry;
import com.sun.jna.Platform;
import java.util.HashMap;
import java.util.Map;

class DeviceDescriptions {

    private final Map<String, String> idToDescription = new HashMap();
    private final Map<String, String> descriptionToId = new HashMap();

    public DeviceDescriptions() {
        if (Platform.isWindows()) {
            WindowsRegistry reg = WindowsRegistry.getInstance();
            String devices = "SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e972-e325-11ce-bfc1-08002be10318}";

            try {
                for (String device : reg.readStringSubKeys(HKey.HKLM, devices)) {
                    String key = devices + "\\" + device;
                    String id = reg.readString(HKey.HKLM, key, "NetCfgInstanceId");
                    String description = reg.readString(HKey.HKLM, key, "DriverDesc");
                    if (id != null && description != null) {
                        idToDescription.put("\\Device\\NPF_" + id, description);
                        descriptionToId.put(description, "\\Device\\NPF_" + id);
                    }
                }
            } catch (RegistryException ignore) {
                System.err.println("Could not find network adapter descriptions in registry");
            }
        }
    }

    String getDescription(String id) {
        return idToDescription.getOrDefault(id, id);
    }

    String getId(String description) {
        return descriptionToId.getOrDefault(description, description);
    }
}
