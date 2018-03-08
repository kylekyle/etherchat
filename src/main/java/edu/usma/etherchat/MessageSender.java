package edu.usma.etherchat;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.UnknownPacket;
import org.pcap4j.util.MacAddress;

public class MessageSender implements Runnable {

    private String device;
    private final BlockingQueue<String> outgoing = new LinkedBlockingQueue<>();

    void open(String device) {
        this.device = device;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            PcapNetworkInterface nif = Pcaps.getDevByName(device);
            System.out.println(nif.getLinkLayerAddresses().get(0));
            MacAddress address = MacAddress.getByAddress(nif.getLinkLayerAddresses().get(0).getAddress());

            try (PcapHandle handle = nif.openLive(EtherChat.SNAPLEN, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, EtherChat.READ_TIMEOUT);) {
                Stream.generate(() -> {
                    try {
                        return outgoing.take();
                    } catch (InterruptedException ie) {
                        return null;
                    }
                })
                        .filter(Objects::nonNull)
                        .forEach(message -> {
                            try {
                                EthernetPacket.Builder frame = new EthernetPacket.Builder()
                                        .srcAddr(address)
                                        .paddingAtBuild(true)
                                        .type(EtherChat.ETHERTYPE)
                                        .dstAddr(MacAddress.ETHER_BROADCAST_ADDRESS)
                                        .payloadBuilder(new UnknownPacket.Builder().rawData(message.getBytes()));

                                handle.sendPacket(frame.build());
                            } catch (PcapNativeException | NotOpenException ex) {
                                Window.alert(ex.getMessage());
                            }
                        });
            }
        } catch (PcapNativeException ex) {
            Window.alert(ex.getMessage());
        }
    }

    public void send(String message) {
        outgoing.offer(message);
    }
}
