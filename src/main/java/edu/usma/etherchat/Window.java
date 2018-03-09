package edu.usma.etherchat;

import com.github.sarxos.winreg.HKey;
import com.github.sarxos.winreg.RegistryException;
import com.github.sarxos.winreg.WindowsRegistry;
import com.sun.jna.Platform;
import edu.usma.etherchat.MessageReceiver.Message;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;

public class Window implements Runnable {

    private final MessageSender sender;
    private final MessageReceiver receiver;
    private final DeviceDescriptions deviceDescriptions = new DeviceDescriptions();

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
                } catch (RegistryException re) {
                    System.err.println("Could not find network adapter descriptions in registry: " + re.getMessage());
                }
            }
        }

        String get(String id) {
            return idToDescription.getOrDefault(id, id);
        }

        String getId(String description) {
            return descriptionToId.getOrDefault(description, description);
        }
    }

    public Window(MessageSender sender, MessageReceiver receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    @Override
    public void run() {
        JComboBox deviceComboBox = new JComboBox();
        deviceComboBox.addItem("Select a network device ...");

        try {
            Pcaps.findAllDevs()
                    .stream()
                    .map((device) -> deviceDescriptions.get(device.getName()))
                    .forEach((description) -> deviceComboBox.addItem(description));
        } catch (PcapNativeException ex) {
            alert(ex.getMessage());
            deviceComboBox.setEnabled(false);
        }

        deviceComboBox.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                deviceComboBox.setEnabled(false);
                String description = e.getItem().toString();
                sender.open(deviceDescriptions.getId(description));
                receiver.open(deviceDescriptions.getId(description));
            }
        });

        JTextArea messages = new JTextArea();
        messages.setLineWrap(true);
        JScrollPane jp = new JScrollPane(messages);

        new Timer(500, (e) -> {
            Message message = receiver.getMessage();

            if (message != null) {
                messages.append(message.getText());
                messages.append(System.getProperty("line.separator"));
            }
        }).start();

        JTextField input = new JTextField("Say something!");

        // this seems like a lot of work for a placeholder ...
        input.setForeground(Color.GRAY);

        input.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (input.getForeground() == Color.GRAY) {
                    input.setText("");
                    input.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (input.getText().isEmpty()) {
                    input.setForeground(Color.GRAY);
                    input.setText("Say something!");
                }
            }
        });

        input.addActionListener((ActionEvent e) -> {
            if (!input.getText().isEmpty()) {
                sender.send(input.getText());
                input.setText("");
            }
        });

        JFrame frame = new JFrame("EtherChat");

        frame.add(deviceComboBox, BorderLayout.NORTH);
        frame.add(jp, BorderLayout.CENTER);
        frame.add(input, BorderLayout.SOUTH);
        frame.pack();

        frame.setSize(1000, 500);
        frame.setLocationByPlatform(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void alert(String message) {
        JOptionPane.showMessageDialog(null, message);
    }
}
