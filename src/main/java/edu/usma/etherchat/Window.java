package edu.usma.etherchat;

import com.github.sarxos.winreg.HKey;
import com.github.sarxos.winreg.RegistryException;
import com.github.sarxos.winreg.WindowsRegistry;
import com.sun.jna.Platform;
import edu.usma.etherchat.MessageReceiver.Message;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;

public class Window implements Runnable {

    private final MessageSender sender;
    private final MessageReceiver receiver;
    private final DeviceDescriptions deviceDescriptions = new DeviceDescriptions();

    public static final String[] COLORS = { // every color in the crayola 100 pack
        "#000000", "#03BB85", "#A78B00", "#FFC1CC", "#5E4330", "#0048ba",
        "#FCF686", "#FC74FD", "#0066FF", "#FFDF00", "#788193", "#006A93",
        "#C8A2C8", "#EED9C4", "#503E32", "#652DC1", "#AF593E", "#8B8680",
        "#514E49", "#867200", "#708EB3", "#C88A65", "#63B76C", "#D6AEDD",
        "#01A368", "#0A6B0D", "#1164B4", "#E2B631", "#BC8777", "#FDD5B1",
        "#FF5470", "#EE34D2", "#FF861F", "#8FD8D8", "#F4FA9F", "#6EEB6E",
        "#B2592D", "#D0FF14", "#87421F", "#BB3385", "#ED0A3F", "#A36F40",
        "#FED8B1", "#FFC800", "#497E48", "#B2BEB5", "#9DE093", "#6B3FA0",
        "#FF3F34", "#F653A6", "#C32148", "#CC99BA", "#6A2963", "#926F5B",
        "#FF7A00", "#33CC99", "#76D7EA", "#CA3435", "#01796F", "#00003B",
        "#E6335F", "#00B9FB", "#4F69C6", "#FFDB00", "#8359A3", "#FFCBA4",
        "#E90067", "#BC6CAC", "#03228E", "#6456B7", "#A50B5E", "#87FF2A",
        "#FF99CC", "#FF91A4", "#DCCCD7", "#B5A895", "#DB5079", "#F0E68C",
        "#FC80A5", "#FBE870", "#FA9D5A", "#404E5A", "#EBE1C2", "#C62D42",
        "#FDFF00", "#D9D6CF", "#C5E17A", "#FFAE42", "#6CDAE7", "#A6AAAE",
        "#FA9C44", "#F091A9", "#00755E", "#B99685", "#DA8A67", "#FF6E4A",
        "#FFFF66", "#0086A7", "#FD7C6E", "#2D383A", "#7A89B8", "#93CCEA",
        "#6F9940", "#FF007C"};

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
                    Logger.getLogger(Window.class.getName()).log(Level.WARNING, re.getMessage());
                }
            }
        }

        String get(String id) {
            return idToDescription.get(id) == null ? id : idToDescription.get(id);

        }

        String getId(String description) {
            return descriptionToId.get(description) == null ? description : descriptionToId.get(description);
        }
    }

    public Window(MessageSender sender, MessageReceiver receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    @Override
    public void run() {
        JTextPane pane = new JTextPane();
        JScrollPane scroll = new JScrollPane(pane);
        final HTMLEditorKit editor = new HTMLEditorKit();
        final HTMLDocument document = new HTMLDocument();

        pane.setEditable(false);
        pane.setEditorKit(editor);
        pane.setDocument(document);

        new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Message message = receiver.getMessage();

                if (message != null) {
                    try {
                        String color = COLORS[Math.abs(message.getUser().hashCode()) % COLORS.length];
                        String html = "<font color=" + color + "><b>" + message.getUser() + ":</b></font> " + message.getText();
                        editor.insertHTML(document, document.getLength(), html, 0, 0, null);
                    } catch (BadLocationException | IOException ex) {
                        Window.alert(ex.getMessage());
                    }
                }
            }
        }).start();

        final JTextField input = new JTextField("Say something!");
        input.setEnabled(false);

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

        input.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (!input.getText().isEmpty()) {
                    sender.send(input.getText());
                    input.setText("");
                }
            }
        });

        final JComboBox deviceComboBox = new JComboBox();
        deviceComboBox.addItem("Select a network device ...");

        try {
            for (PcapNetworkInterface device : Pcaps.findAllDevs()) {
                deviceComboBox.addItem(deviceDescriptions.get(device.getName()));
            }
        } catch (PcapNativeException ex) {
            alert(ex.getMessage());
            deviceComboBox.setEnabled(false);
        }

        deviceComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
                    input.setEnabled(true);
                    deviceComboBox.setEnabled(false);
                    String description = ie.getItem().toString();
                    sender.open(deviceDescriptions.getId(description));
                    receiver.open(deviceDescriptions.getId(description));
                }
            }
        });

        JFrame frame = new JFrame("EtherChat");

        frame.add(deviceComboBox, BorderLayout.NORTH);
        frame.add(scroll, BorderLayout.CENTER);
        frame.add(input, BorderLayout.SOUTH);
        frame.pack();

        frame.setSize(1000, 500);
        frame.setLocationByPlatform(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(frame);

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Window.class.getName()).log(Level.SEVERE, "Could not load native look and feel", ex);
        }

        frame.setVisible(true);
    }

    public static void alert(String message) {
        JOptionPane.showMessageDialog(null, message);
    }
}
