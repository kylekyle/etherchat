package edu.usma.etherchat;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.packet.Packet;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.EthernetPacket;

public class MessageReceiver implements Runnable {

    private PcapNetworkInterface device;
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

    void open(String name) {
        try {
            this.device = Pcaps.getDevByName(name);
        } catch (PcapNativeException ex) {
            Window.alert(ex.getMessage());
        }
        new Thread(this).start();
    }

    public Message getMessage() {
        return incoming.poll();
    }

    @Override
    public void run() {
        try {
            try (PcapHandle handle = device.openLive(EtherChat.SNAPLEN, PcapNetworkInterface.PromiscuousMode.NONPROMISCUOUS, EtherChat.READ_TIMEOUT);) {
                handle.setFilter("ether proto " + EtherChat.ETHERTYPE.valueAsString(), BpfProgram.BpfCompileMode.OPTIMIZE);

                try {
                    handle.loop(-1, new PacketListener() {
                        @Override
                        public void gotPacket(Packet packet) {
                            EthernetPacket ether = (EthernetPacket) packet.get(EthernetPacket.class);
                            String user = ether.getHeader().getSrcAddr().toString();
                            String text = new String(ether.getPayload().getRawData());
                            incoming.offer(new Message(user, text));
                        }
                    });
                } catch (InterruptedException ex) {
                    Window.alert(ex.getMessage());
                }
            }
        } catch (PcapNativeException | NotOpenException ex) {
            Window.alert(ex.getMessage());
        }
    }
}
