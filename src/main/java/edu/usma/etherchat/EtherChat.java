package edu.usma.etherchat;

import java.util.List;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.namednumber.EtherType;

public class EtherChat {

    public static final int SNAPLEN = 65536;
    public static final int READ_TIMEOUT = 10;
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
            
            SwingUtilities.invokeLater(new Window(devices, sender, receiver));
        } catch (PcapNativeException ex) {
            Window.alert(ex.getMessage());
        }
    }
}
