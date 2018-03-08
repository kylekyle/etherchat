package edu.usma.etherchat;

import java.util.concurrent.BlockingQueue;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.Pcaps;

public class MessageListener implements Runnable {

    private static final int READ_TIMEOUT = 10; // [ms]
    private static final int SNAPLEN = 65536; // [bytes]

    private String device;
    private final BlockingQueue<Message> messageQueue;

    MessageListener(BlockingQueue<Message> messageQueue) {
        this.messageQueue = messageQueue;
    }

    void startListeningOn(String device) {
        this.device = device;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            PcapNetworkInterface nif = Pcaps.getDevByName(device);

            try (PcapHandle handle = nif.openLive(SNAPLEN, PromiscuousMode.PROMISCUOUS, READ_TIMEOUT);) {
                handle.setFilter("ether dst FF:FF:FF:FF:FF:FF", BpfProgram.BpfCompileMode.OPTIMIZE);

                handle.stream()
                        .map((packet) -> new Message("packet", packet.toHexString()))
                        .forEach((message) -> messageQueue.offer(message));

            }
        } catch (PcapNativeException | NotOpenException ex) {
            Window.alert(ex.getMessage());
        }
    }
}
