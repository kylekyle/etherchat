package edu.usma.etherchat;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;
import javax.swing.SwingUtilities;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

        Window window = new Window(messageQueue);
        MessageListener listener = new MessageListener(messageQueue);
        window.onDeviceChange((device) -> listener.startListeningOn(device));

        try {
            Pcaps.findAllDevs().forEach((PcapNetworkInterface t) -> {
                window.addDevice(t.getName());
            });
        } catch (PcapNativeException ex) {
            Window.alert(ex.getMessage());
        }

        SwingUtilities.invokeLater(window);
    }
}
