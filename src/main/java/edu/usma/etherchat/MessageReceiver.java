package edu.usma.etherchat;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.EthernetPacket;

public class MessageReceiver implements Runnable {

    private String device;
    private final BlockingQueue<Message> incoming = new LinkedBlockingQueue<>();

    class Message {

        private final String user;
        private final String text;

        public Message(String user, String text) {
            this.user = user;
            this.text = text;
        }

        public String getUser() {
            return user;
        }

        public String getText() {
            return text;
        }
    }

    void open(String device) {
        this.device = device;
        new Thread(this).start();
    }

    public Message getMessage() {
        try {
            return incoming.isEmpty() ? null : incoming.take();
        } catch (InterruptedException ignore) {
            return null;
        }
    }

    @Override
    public void run() {
        try {
            PcapNetworkInterface nif = Pcaps.getDevByName(device);

            try (PcapHandle handle = nif.openLive(EtherChat.SNAPLEN, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, EtherChat.READ_TIMEOUT);) {
                handle.setFilter("ether proto " + EtherChat.ETHERTYPE.valueAsString(), BpfProgram.BpfCompileMode.OPTIMIZE);

                handle.stream()
                        .map((packet) -> {
                            EthernetPacket ether = packet.get(EthernetPacket.class);
                            String text = new String(ether.getPayload().getRawData());
                            return new Message("packet", text);
                        })
                        .forEach((message) -> incoming.offer(message));

            }
        } catch (PcapNativeException | NotOpenException ex) {
            Window.alert(ex.getMessage());
        }
    }
}
