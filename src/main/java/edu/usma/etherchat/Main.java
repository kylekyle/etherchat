package edu.usma.etherchat;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.SwingUtilities;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        DeviceDescriptions deviceDescriptions = new DeviceDescriptions();
        BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

        Window window = new Window(messageQueue);
        MessageListener listener = new MessageListener(messageQueue);
        window.onDeviceChange((description) -> {
            listener.startListeningOn(deviceDescriptions.getId(description));
        });

        try {
            Pcaps.findAllDevs().forEach((PcapNetworkInterface t) -> {
                String result = deviceDescriptions.getDescription(t.getName());
                window.addDevice(deviceDescriptions.getDescription(t.getName()));
            });
        } catch (PcapNativeException ex) {
            Window.alert(ex.getMessage());
        }

        SwingUtilities.invokeLater(window);
    }
}
