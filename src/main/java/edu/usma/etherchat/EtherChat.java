package edu.usma.etherchat;

import java.util.List;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.namednumber.EtherType;

public class EtherChat {

    public static final int SNAPLEN = 65536; // bytes
    public static final int READ_TIMEOUT = 10; // ms
    public static final EtherType ETHERTYPE = new EtherType((short) 0xCAFE, "EtherChat");

    public static void main(String[] args) throws InterruptedException {
        DeviceDescriptions deviceDescriptions = new DeviceDescriptions();

        try {
            List<String> devices = Pcaps.findAllDevs()
                    .stream()
                    .map((dev) -> deviceDescriptions.get(dev.getName()))
                    .collect(Collectors.toList());

            MessageSender sender = new MessageSender();
            MessageReceiver receiver = new MessageReceiver();

            Window window = new Window(devices);

            window.onDeviceChange((description) -> {
                sender.open(deviceDescriptions.getId(description));
                receiver.open(deviceDescriptions.getId(description));
            });

            window.onMessageSend((message) -> {
                sender.send(message);
            });

            window.setMessageSupplier(() -> {
                return receiver.getMessage();
            });

            SwingUtilities.invokeLater(window);
        } catch (PcapNativeException ex) {
            Window.alert(ex.getMessage());
        }
    }
}
