package edu.usma.etherchat;

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

    public class Message {

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
                        .map(packet -> (EthernetPacket) packet.get(EthernetPacket.class))
                        .map(etherchat -> {
                            String text = new String(etherchat.getPayload().getRawData());
                            return new Message("packet", text);
                        })
                        .forEach(message -> incoming.offer(message));

            }
        } catch (NullPointerException ex) {
            Window.alert("That device is not connected to a network!");
        } catch (PcapNativeException | NotOpenException ex) {
            Window.alert(ex.getMessage());
        }
    }
}
