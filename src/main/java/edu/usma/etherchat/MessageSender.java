package edu.usma.etherchat;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.UnknownPacket;
import org.pcap4j.util.MacAddress;

public class MessageSender implements Runnable {

    private PcapNetworkInterface device;
    private final BlockingQueue<String> outgoing = new LinkedBlockingQueue<>();

    void open(String name) {
        try {
            this.device = Pcaps.getDevByName(name);
        } catch (PcapNativeException ex) {
            Window.alert(ex.getMessage());
        }
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            MacAddress address = MacAddress.getByAddress(device.getLinkLayerAddresses().get(0).getAddress());

            try (PcapHandle handle = device.openLive(EtherChat.SNAPLEN, PcapNetworkInterface.PromiscuousMode.NONPROMISCUOUS, EtherChat.READ_TIMEOUT);) {
                while (true) {
                    try {
                        String message = "->" + outgoing.take();
                        UnknownPacket.Builder payload = new UnknownPacket.Builder();

                        EthernetPacket.Builder frame = new EthernetPacket.Builder()
                                .srcAddr(address)
                                .paddingAtBuild(true)
                                .type(EtherChat.ETHERTYPE)
                                .dstAddr(MacAddress.ETHER_BROADCAST_ADDRESS)
                                .payloadBuilder(payload.rawData(message.getBytes()));

                        handle.sendPacket(frame.build());
                    } catch (PcapNativeException | NotOpenException ex) {
                        Window.alert(ex.getMessage());
                    } catch (InterruptedException ignore) {
                    }
                }
            }
        } catch (PcapNativeException ex) {
            Window.alert(ex.getMessage());
        }
    }

    public void send(String message) {
        outgoing.offer(message);
    }
}
